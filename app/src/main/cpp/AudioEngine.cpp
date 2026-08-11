#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>

#define LOG_TAG "GITARA"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static float gVolume = 0.5f;
static float gTone = 0.5f;
static float gDistortion = 0.5f;
static float gGain = 1.0f;
static float prevSample = 0.0f;

static std::shared_ptr<oboe::AudioStream> inputStream;
static std::shared_ptr<oboe::AudioStream> outputStream;

class AudioCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream* stream, void* data, int32_t numFrames) override {
        
        float* buffer = static_cast<float*>(data);
        
        // ✅ BASAHIN ANG TUNOG NG GITARA → PALITAD → ILABAS!
        for (int i = 0; i < numFrames; i++) {
            float input = buffer[i];          // BASAHIN MULA SA MIKROFONO
            float processed = input * gGain;   // ⚡ DAGDAG-LAKAS
            
            // 💥 DISTORTION — PUMUTOL NA TUNOG
            float drive = 1.0f + gDistortion * 4.0f;
            processed = std::tanh(processed * drive);
            
            // 🎵 TONE — MALINAW O MALAMBOT
            if (gTone < 1.0f) {
                processed = processed * gTone + prevSample * (1.0f - gTone);
                prevSample = processed;
            }
            
            // 🔊 VOLUME — ILABAS ANG TUNOG
            buffer[i] = processed * gVolume * 0.8f;
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static AudioCallback callback;

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    if (outputStream) return;

    // ✅ PAGBASA NG TUNOG MULA SA MIKROFONO
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Input)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setSampleRate(44100)
           ->setFormat(oboe::AudioFormat::Float)
           ->setChannelCount(1);
    
    auto result = builder.openStream(inputStream);
    if (result != oboe::Result::OK) {
        LOGI("❌ HINDI MA-BASA ANG MIKROFONO: %s", oboe::convertToText(result));
        return;
    }

    // ✅ PAGLALABAS NG TUNOG NA MAY DISTORTION
    builder.setDirection(oboe::Direction::Output)
           ->setCallback(&callback);
    
    result = builder.openStream(outputStream);
    if (result == oboe::Result::OK) {
        inputStream->start();
        outputStream->start();
        LOGI("✅ NAKA-ON! MAY TUNOG NA!");
    } else {
        LOGI("❌ HINDI MA-LABAS ANG TUNOG: %s", oboe::convertToText(result));
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (inputStream) { inputStream->stop(); inputStream->close(); inputStream.reset(); }
    if (outputStream) { outputStream->stop(); outputStream->close(); outputStream.reset(); }
    LOGI("⏹️ NAKA-OFF");
}

#define SET_FUNC(NAME) \
extern "C" JNIEXPORT void JNICALL \
Java_com_gitaradistortion_MainActivity_set##NAME(JNIEnv*, jobject, jfloat v) { g##NAME = v; }

SET_FUNC(Volume)
SET_FUNC(Tone)
SET_FUNC(Distortion)
SET_FUNC(Gain)
