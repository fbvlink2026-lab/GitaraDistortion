#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>
#include <vector>

#define LOG_TAG "GITARA"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// ✅ MGA PIHITAN — LAHAT TATLO: VOLUME, TONE, REVERB
static float gVolume    = 0.70f;  // 🔊 LAKAS NG TUNOG
static float gTone      = 0.50f;  // 🎵 KULAY NG TUNOG — MALINAW NA BOSES
static float gReverb    = 0.30f;  // 🌊 LALIM / ESPASYO — PARANG SA HALL
static float gNoiseGate = 0.04f;  // ✅ PATAY NG INGAY

// ✅ BUFFER PARA SA TONE FILTER
static float prevLpf = 0.0f;

// ✅ REVERB — PAGPAPALALIM NG TUNOG (DELAY + PAGHINA)
static const int REVERB_LENGTH = 1024; // LAKI NG ESPASYO
static float reverbBuffer[REVERB_LENGTH] = {0};
static int reverbIndex = 0;
static const float REVERB_DECAY = 0.6f; // BILIS NG PAGHINA

std::vector<float> audioBuffer;
std::mutex bufferMutex;
static std::shared_ptr<oboe::AudioStream> inputStream;
static std::shared_ptr<oboe::AudioStream> outputStream;

// ✅ INPUT — BASAHIN ANG MIKROFONO
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

// ✅ OUTPUT — AYUSIN ANG TUNOG → VOLUME → TONE → REVERB → ILABAS
class OutputCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* out = static_cast<float*>(data);
        std::lock_guard<std::mutex> lock(bufferMutex);

        float alpha = gTone * 0.85f + 0.05f;

        for (int i = 0; i < numFrames; i++) {
            float input = 0.0f;
            if (i < audioBuffer.size()) input = audioBuffer[i];

            // ✅ NOISE GATE — PATAY ANG INGAY KUNG WALANG TUMUTUGTOG
            if (std::fabs(input) < gNoiseGate) input = 0.0f;

            // ✅ TONE FILTER — MALINAW NA BOSES
            // 0.0 = Puro BASS → 0.5 = Balansado → 1.0 = Puro Treble
            float lpf = alpha * prevLpf + (1.0f - alpha) * input;
            float hpf = input - lpf;
            float dry = lpf * (1.0f - gTone) * 1.8f + hpf * gTone * 1.2f;
            prevLpf = lpf;

            // ✅ REVERB — DAGDAG NA ESPASYO AT LALIM
            float reverbIn = dry;
            float reverbOut = reverbBuffer[reverbIndex];
            reverbBuffer[reverbIndex] = reverbIn + reverbOut * REVERB_DECAY;
            reverbIndex = (reverbIndex + 1) % REVERB_LENGTH;

            // ✅ PAGHALUHIN: TUNOG + REVERB
            float wet = dry * (1.0f - gReverb) + reverbOut * gReverb * 0.5f;

            // ✅ VOLUME — BALANSADO AT HINDI PUMUTOK
            out[i] = wet * gVolume * 0.9f;
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static InputCallback inputCallback;
static OutputCallback outputCallback;

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    if (inputStream || outputStream) return;

    // ✅ INPUT — PARA LUMABAS ANG MENSAHE NG MIKROFONO
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

    // ✅ OUTPUT — ILABAS ANG AYUS NA TUNOG
    builder.setDirection(oboe::Direction::Output)
           ->setCallback(&outputCallback);

    result = builder.openStream(outputStream);
    if (result != oboe::Result::OK) {
        LOGI("❌ OUTPUT ERROR: %s", oboe::convertToText(result));
        return;
    }

    inputStream->requestStart();
    outputStream->requestStart();
    LOGI("✅ VOLUME+TONE+REVERB — NAKA-ON!");
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (inputStream) { inputStream->stop(); inputStream->close(); inputStream.reset(); }
    if (outputStream) { outputStream->stop(); outputStream->close(); outputStream.reset(); }
    audioBuffer.clear();
    prevLpf = 0.0f;
    // ✅ LINISIN ANG REVERB BUFFER
    for (int i = 0; i < REVERB_LENGTH; i++) reverbBuffer[i] = 0.0f;
    LOGI("⏹️ NAKA-OFF");
}

#define SET_FUNC(NAME) \
extern "C" JNIEXPORT void JNICALL \
Java_com_gitaradistortion_MainActivity_set##NAME(JNIEnv*, jobject, jfloat v) { g##NAME = v; }

SET_FUNC(Volume)
SET_FUNC(Tone)
SET_FUNC(Reverb)
