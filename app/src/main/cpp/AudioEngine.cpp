#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>

#define LOG_TAG "GITARA"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// ✅ MGA PIHITAN
static float gVolume    = 0.80f;
static float gTone      = 0.50f;
static float gReverb    = 0.25f;
static float gNoiseGate = 0.03f;
static float prevLpf    = 0.0f;

// ✅ REVERB BUFFER
static const int REVERB_LENGTH = 800;
static float reverbBuffer[REVERB_LENGTH] = {0};
static int reverbIndex = 0;
static const float REVERB_DECAY = 0.55f;

static std::shared_ptr<oboe::AudioStream> inputStream;
static std::shared_ptr<oboe::AudioStream> outputStream;

// ✅ KARANIWANG BUFFER — DIREKTA ANG KOPYA!
static float sharedBuffer[2048];
static std::atomic<bool> hasNewData{false};
static std::mutex bufferMutex;

// ✅ INPUT — BASAHIN → DIREKTA SA BUFFER
class InputCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* in = static_cast<float*>(data);
        std::lock_guard<std::mutex> lock(bufferMutex);
        
        // ✅ KOPYAHIN AGAD — WALANG HULI!
        int count = numFrames < 2048 ? numFrames : 2048;
        for (int i = 0; i < count; i++) {
            sharedBuffer[i] = in[i];
        }
        hasNewData = true;
        return oboe::DataCallbackResult::Continue;
    }
};

// ✅ OUTPUT — KUNIN SA BUFFER → AYUSIN → ILABAS
class OutputCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* out = static_cast<float*>(data);
        std::lock_guard<std::mutex> lock(bufferMutex);

        float alpha = gTone * 0.85f + 0.05f;

        for (int i = 0; i < numFrames; i++) {
            float input = 0.0f;
            
            // ✅ KUNIN ANG TUNOG — KUNG MAY BAGONG DATOS
            if (hasNewData && i < 2048) {
                input = sharedBuffer[i];
            }

            // ✅ NOISE GATE
            if (std::fabs(input) < gNoiseGate) input = 0.0f;

            // ✅ TONE
            float lpf = alpha * prevLpf + (1.0f - alpha) * input;
            float hpf = input - lpf;
            float dry = lpf * (1.0f - gTone) * 1.8f + hpf * gTone * 1.2f;
            prevLpf = lpf;

            // ✅ REVERB
            float reverbOut = reverbBuffer[reverbIndex];
            reverbBuffer[reverbIndex] = dry + reverbOut * REVERB_DECAY;
            reverbIndex = (reverbIndex + 1) % REVERB_LENGTH;

            // ✅ PAGHALUHIN + VOLUME
            float wet = dry * (1.0f - gReverb) + reverbOut * gReverb * 0.5f;
            out[i] = wet * gVolume * 0.95f;
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

    // ✅ INPUT — MIKROFONO
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Input)
           ->setSampleRate(48000)
           ->setFormat(oboe::AudioFormat::Float)
           ->setChannelCount(1)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setCallback(&inputCallback);

    auto result = builder.openStream(inputStream);
    if (result != oboe::Result::OK) {
        LOGI("❌ INPUT: %s", oboe::convertToText(result));
        return;
    }

    // ✅ OUTPUT — SPEAKER
    builder.setDirection(oboe::Direction::Output)
           ->setCallback(&outputCallback);

    result = builder.openStream(outputStream);
    if (result != oboe::Result::OK) {
        LOGI("❌ OUTPUT: %s", oboe::convertToText(result));
        return;
    }

    inputStream->requestStart();
    outputStream->requestStart();
    LOGI("✅ NAKA-ON! DAPAT MAY TUNOG NA!");
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (inputStream) { inputStream->stop(); inputStream->close(); inputStream.reset(); }
    if (outputStream) { outputStream->stop(); outputStream->close(); outputStream.reset(); }
    prevLpf = 0.0f;
    for (int i = 0; i < REVERB_LENGTH; i++) reverbBuffer[i] = 0.0f;
    hasNewData = false;
    LOGI("⏹️ NAKA-OFF");
}

#define SET_FUNC(NAME) \
extern "C" JNIEXPORT void JNICALL \
Java_com_gitaradistortion_MainActivity_set##NAME(JNIEnv*, jobject, jfloat v) { g##NAME = v; }

SET_FUNC(Volume)
SET_FUNC(Tone)
SET_FUNC(Reverb)
