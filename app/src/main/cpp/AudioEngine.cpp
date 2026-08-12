#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>

#define LOG_TAG "GITARA"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// ✅ MGA ANTAS — HINDI NAGBABAGO KAHIT NAKA-OFF!
static float gVolumeLevel    = 0.75f;
static float gToneLevel      = 0.50f;
static float gReverbLevel    = 0.25f;
static float gOverdriveLevel = 0.00f;
static float gDistortionLevel= 0.00f;
static float gGainLevel      = 1.00f;
static float gPhaserLevel    = 0.00f;

// ✅ ON/OFF — HIIWALAY SA ANTAS!
static bool gEnableVolume    = true;
static bool gEnableTone      = true;
static bool gEnableReverb    = true;
static bool gEnableOverdrive = false;
static bool gEnableDistortion= false;
static bool gEnableGain      = false;
static bool gEnablePhaser    = false;

// ✅ PALAGING NAKA-ON — HINDI NABABAGO
static const float gNoiseGate = 0.04f;

// ✅ MGA BUFFER
static float prevLpf = 0.0f;
static const int REVERB_LENGTH = 800;
static float reverbBuffer[REVERB_LENGTH] = {0};
static int reverbIndex = 0;
static const float REVERB_DECAY = 0.55f;

static const int PHASER_STAGES = 6;
static float phaserBuffer[PHASER_STAGES][200] = {{0}};
static int phaserPos[PHASER_STAGES] = {0};
static float phaserLfo = 0.0f;
static const float PHASER_LFO_SPEED = 0.08f;

static float sharedBuffer[2048];
static std::atomic<bool> hasNewData{false};
static std::mutex bufferMutex;

static std::shared_ptr<oboe::AudioStream> inputStream;
static std::shared_ptr<oboe::AudioStream> outputStream;

class InputCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        float* in = static_cast<float*>(data);
        std::lock_guard<std::mutex> lock(bufferMutex);
        int count = numFrames < 2048 ? numFrames : 2048;
        for (int i = 0; i < count; i++) sharedBuffer[i] = in[i];
        hasNewData = true;
        return oboe::DataCallbackResult::Continue;
    }
};

// ✅ TAMANG DALUYAN — KAPAG NAKA-ON LANG TUMATALAB!
class OutputCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* out = static_cast<float*>(data);
        std::lock_guard<std::mutex> lock(bufferMutex);

        float alpha = gToneLevel * 0.85f + 0.05f;
        phaserLfo += PHASER_LFO_SPEED;

        for (int i = 0; i < numFrames; i++) {
            float input = 0.0f;
            if (hasNewData && i < 2048) input = sharedBuffer[i];

            // 🚧 1. NOISE GATE — PALAGING NAKA-ON
            if (std::fabs(input) < gNoiseGate) input = 0.0f;

            float proc = input;

            // ⚡ 2. GAIN — KAPAG NAKA-ON LANG
            if (gEnableGain) proc *= gGainLevel;

            // 🔥 3. OVERDRIVE — KAPAG NAKA-ON LANG
            if (gEnableOverdrive && gOverdriveLevel > 0.01f) {
                float drive = 1.0f + gOverdriveLevel * 3.0f;
                proc = std::sin(proc * drive) * (1.0f - gOverdriveLevel * 0.3f) + proc * gOverdriveLevel * 0.3f;
            }

            // 💥 4. DISTORTION — KAPAG NAKA-ON LANG
            if (gEnableDistortion && gDistortionLevel > 0.01f) {
                float drive = 1.0f + gDistortionLevel * 4.0f;
                proc = std::tanh(proc * drive);
            }

            // 🎵 5. TONE — KAPAG NAKA-ON LANG
            if (gEnableTone) {
                float lpf = alpha * prevLpf + (1.0f - alpha) * proc;
                float hpf = proc - lpf;
                proc = lpf * (1.0f - gToneLevel) * 1.8f + hpf * gToneLevel * 1.2f;
                prevLpf = lpf;
            }

            // 🫧 6. PHASER — KAPAG NAKA-ON LANG
            if (gEnablePhaser && gPhaserLevel > 0.01f) {
                float lfo = (std::sin(phaserLfo) + 1.0f) * 0.4f + 0.1f;
                float wet = proc;
                for (int s = 0; s < PHASER_STAGES; s++) {
                    int delay = (int)(lfo * 40.0f) + 5;
                    phaserBuffer[s][phaserPos[s]] = wet;
                    int read = (phaserPos[s] - delay + 200) % 200;
                    float dly = phaserBuffer[s][read];
                    wet = wet * 0.7f + dly * 0.3f;
                    phaserPos[s] = (phaserPos[s] + 1) % 200;
                }
                proc = proc * (1.0f - gPhaserLevel) + wet * gPhaserLevel;
            }

            // 🌊 7. REVERB — KAPAG NAKA-ON LANG
            if (gEnableReverb) {
                float reverbOut = reverbBuffer[reverbIndex];
                reverbBuffer[reverbIndex] = proc + reverbOut * REVERB_DECAY;
                reverbIndex = (reverbIndex + 1) % REVERB_LENGTH;
                proc = proc * (1.0f - gReverbLevel) + reverbOut * gReverbLevel * 0.5f;
            }

            // 🔊 8. VOLUME — KAPAG NAKA-ON LANG
            if (gEnableVolume) proc *= gVolumeLevel * 0.95f;

            out[i] = proc;
        }
        hasNewData = false;
        return oboe::DataCallbackResult::Continue;
    }
};

static InputCallback inputCallback;
static OutputCallback outputCallback;

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    if (inputStream || outputStream) return;

    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Input)
           ->setSampleRate(48000)
           ->setFormat(oboe::AudioFormat::Float)
           ->setChannelCount(1)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setCallback(&inputCallback);

    auto result = builder.openStream(inputStream);
    if (result != oboe::Result::OK) return;

    builder.setDirection(oboe::Direction::Output)->setCallback(&outputCallback);
    result = builder.openStream(outputStream);
    if (result != oboe::Result::OK) return;

    inputStream->requestStart();
    outputStream->requestStart();
    LOGI("✅ MAY ON/OFF NA BAWAT EPEKTO!");
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (inputStream) { inputStream->stop(); inputStream->close(); inputStream.reset(); }
    if (outputStream) { outputStream->stop(); outputStream->close(); outputStream.reset(); }
    prevLpf = phaserLfo = reverbIndex = 0;
    for (int i = 0; i < REVERB_LENGTH; i++) reverbBuffer[i] = 0;
    for (int s = 0; s < PHASER_STAGES; s++) {
        for (int i = 0; i < 200; i++) phaserBuffer[s][i] = 0;
        phaserPos[s] = 0;
    }
    hasNewData = false;
}

// ✅ ITAKDA ANG ANTAS — HINDI NAIIBANG KAHIT NAKA-OFF!
#define SET_LEVEL(NAME) \
extern "C" JNIEXPORT void JNICALL \
Java_com_gitaradistortion_MainActivity_set##NAME##Level(JNIEnv*, jobject, jfloat v) { g##NAME##Level = v; }

// ✅ ITUKA ANG ON/OFF — HIIWALAY SA ANTAS!
#define SET_SWITCH(NAME) \
extern "C" JNIEXPORT void JNICALL \
Java_com_gitaradistortion_MainActivity_set##NAME##Enabled(JNIEnv*, jobject, jboolean e) { gEnable##NAME = (e != JNI_FALSE); }

SET_LEVEL(Volume)
SET_LEVEL(Tone)
SET_LEVEL(Reverb)
SET_LEVEL(Overdrive)
SET_LEVEL(Distortion)
SET_LEVEL(Gain)
SET_LEVEL(Phaser)

SET_SWITCH(Volume)
SET_SWITCH(Tone)
SET_SWITCH(Reverb)
SET_SWITCH(Overdrive)
SET_SWITCH(Distortion)
SET_SWITCH(Gain)
SET_SWITCH(Phaser)
