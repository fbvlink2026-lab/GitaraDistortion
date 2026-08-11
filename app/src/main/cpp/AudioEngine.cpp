#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>
#include <vector>

#define LOG_TAG "GITARA"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static float gVolume = 0.6f;
static float gTone = 0.5f;
static float gDistortion = 0.7f;
static float gGain = 1.5f;
static float prevSample = 0.0f;

// ✅ BUFFER — LALAGYAN ANG TUNOG MULA SA MIKROFONO BAGO ILABAS
std::vector<float> audioBuffer;
std::mutex bufferMutex;

static std::shared_ptr<oboe::AudioStream> inputStream;
static std::shared_ptr<oboe::AudioStream> outputStream;

// ✅ BASAHIN ANG TUNOG MULA SA MIKROFONO → ILAGAY SA BUFFER
class InputCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* in = static_cast<float*>(data);
        std::lock_guard<std::mutex> lock(bufferMutex);
        
        audioBuffer.resize(numFrames);
        for (int i = 0; i < numFrames; i++) {
            audioBuffer[i] = in[i];  // ✅ I-SAVE ANG TUNOG NG GITARA
        }
        return oboe::DataCallbackResult::Continue;
    }
};

// ✅ KUNIN SA BUFFER → PALITAD → ILABAS
class OutputCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* out = static_cast<float*>(data);
        std::lock_guard<std::mutex> lock(bufferMutex);
        
        for (int i = 0; i < numFrames; i++) {
            float input = 0.0f;
            if (i < audioBuffer.size()) input = audioBuffer[i];
            
            // ✅ DITO GAGAWIN ANG DISTORTION
            float processed = input * gGain;
            float drive = 1.0f + gDistortion * 5.0f;
            processed = std::tanh(processed * drive);
            
            // 🎵 TONE
            if (gTone < 1.0f) {
                processed = processed * gTone + prevSample * (1.0f - gTone);
                prevSample = processed;
            }
            
            // 🔊 ILABAS
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

    // ✅ INPUT — BASAHIN ANG MIKROFONO
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
    LOGI("✅ MIKROFONO NAKABASA!");

    // ✅ OUTPUT — ILABAS ANG TUNOG NA MAY DISTORTION
    builder.setDirection(oboe::Direction::Output)
           ->setCallback(&outputCallback);

    result = builder.openStream(outputStream);
    if (result != oboe::Result::OK) {
        LOGI("❌ OUTPUT ERROR: %s", oboe::convertToText(result));
        return;
    }

    // ✅ SIMULAN — MUNA INPUT, SUNOD OUTPUT
    inputStream->requestStart();
    outputStream->requestStart();
    LOGI("✅ TUNOG NAKA-ON! MAY DISTORTION NA!");
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (inputStream) { inputStream->stop(); inputStream->close(); inputStream.reset(); }
    if (outputStream) { outputStream->stop(); outputStream->close(); outputStream.reset(); }
    audioBuffer.clear();
    LOGI("⏹️ NAKA-OFF");
}

#define SET_FUNC(NAME) \
extern "C" JNIEXPORT void JNICALL \
Java_com_gitaradistortion_MainActivity_set##NAME(JNIEnv*, jobject, jfloat v) { g##NAME = v; }

SET_FUNC(Volume)
SET_FUNC(Tone)
SET_FUNC(Distortion)
SET_FUNC(Gain)
