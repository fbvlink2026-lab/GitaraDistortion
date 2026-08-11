#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>
#include <vector>

#define LOG_TAG "GITARA"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// ✅ MGA PIHITAN — TONE LANG ANG GAGANA, IBA NAKA-PATAY MUNA
static float gVolume    = 0.7f;
static float gTone      = 0.5f;   // 🎵 TONE — SIMULA DITO
static float gDistortion= 0.0f;   // ❌ PATAY MUNA
static float gGain      = 1.0f;   // ❌ PATAY MUNA
static float gNoiseGate = 0.05f;  // ✅ DAGDAG — PAMPATAY NG INGAY!

// ✅ BUFFER AT VARIABLES PARA SA TONE FILTER
std::vector<float> audioBuffer;
static float prevLpf = 0.0f;  // Low-pass (malambot/mababa)
static float prevHpf = 0.0f;  // High-pass (matalas/mataas)
std::mutex bufferMutex;

static std::shared_ptr<oboe::AudioStream> inputStream;
static std::shared_ptr<oboe::AudioStream> outputStream;

class InputCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* in = static_cast<float*>(data);
        std::lock_guard<std::mutex> lock(bufferMutex);
        audioBuffer.resize(numFrames);
        for (int i = 0; i < numFrames; i++) {
            audioBuffer[i] = in[i];
        }
        return oboe::DataCallbackResult::Continue;
    }
};

class OutputCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* out = static_cast<float*>(data);
        std::lock_guard<std::mutex> lock(bufferMutex);
        
        // ✅ KUWENTA NG TONE FILTER
        // Tone = 0.0 → Puro MABABA (malambot/bass)
        // Tone = 0.5 → KATINIG (pantay)
        // Tone = 1.0 → PURO MATAAS (matalas/treble)
        float alpha = gTone * 0.85f + 0.05f;

        for (int i = 0; i < numFrames; i++) {
            float input = 0.0f;
            if (i < audioBuffer.size()) input = audioBuffer[i];

            // ✅ NOISE GATE — PATAYIN ANG INGAY KUNG MAHINA ANG TUGTOG
            if (std::fabs(input) < gNoiseGate) {
                input = 0.0f;
            }

            // ✅ TONE FILTER — DITO GINAGAWA ANG KULAY NG TUNOG
            float lpf = alpha * prevLpf + (1.0f - alpha) * input;  // mababa
            float hpf = input - lpf;                                // mataas
            float processed = lpf * (1.0f - gTone) * 2.0f + hpf * gTone * 1.2f;

            prevLpf = lpf;

            // ✅ VOLUME LANG ANG ILABAS
            out[i] = processed * gVolume;
        }
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
           ->setSampleRate(44100)
           ->setFormat(oboe::AudioFormat::Float)
           ->setChannelCount(1)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setCallback(&inputCallback);

    auto result = builder.openStream(inputStream);
    if (result != oboe::Result::OK) {
        LOGI("❌ INPUT ERROR: %s", oboe::convertToText(result));
        return;
    }

    builder.setDirection(oboe::Direction::Output)->setCallback(&outputCallback);
    result = builder.openStream(outputStream);
    if (result != oboe::Result::OK) {
        LOGI("❌ OUTPUT ERROR: %s", oboe::convertToText(result));
        return;
    }

    inputStream->requestStart();
    outputStream->requestStart();
    LOGI("✅ TONE MODE — NAKA-ON!");
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (inputStream) { inputStream->stop(); inputStream->close(); inputStream.reset(); }
    if (outputStream) { outputStream->stop(); outputStream->close(); outputStream.reset(); }
    audioBuffer.clear();
    prevLpf = prevHpf = 0.0f;
    LOGI("⏹️ NAKA-OFF");
}

// ✅ TONE LANG ANG GAGANA — IBA HINDI PA
#define SET_TONE(NAME) \
extern "C" JNIEXPORT void JNICALL \
Java_com_gitaradistortion_MainActivity_set##NAME(JNIEnv*, jobject, jfloat v) { g##NAME = v; }

SET_TONE(Volume)
SET_TONE(Tone)
