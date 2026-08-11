#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>

#define LOG_TAG "GITARA"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static float gVolume = 0.5f;
static float gTone = 0.5f;
static float gDistortion = 0.5f;

class AudioCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* buffer = static_cast<float*>(data);
        for (int i = 0; i < numFrames; i++) {
            float x = buffer[i];
            float d = 1.0f + gDistortion * 4.0f;
            buffer[i] = std::tanh(x * d) * gVolume;
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static AudioCallback callback;
static std::shared_ptr<oboe::AudioStream> stream;

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    if (stream) return;
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setCallback(&callback);
    auto result = builder.openStream(stream);
    if (result == oboe::Result::OK) {
        stream->requestStart();
        LOGI("✅ AUDIO NAKA-ON!");
    } else {
        LOGI("❌ ERROR: %s", oboe::convertToText(result));
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (stream) {
        stream->stop();
        stream->close();
        stream.reset();
        LOGI("⏹️ AUDIO NAKA-OFF");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setVolume(JNIEnv*, jobject, jfloat v) { gVolume = v; }
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setTone(JNIEnv*, jobject, jfloat v) { gTone = v; }
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDistortion(JNIEnv*, jobject, jfloat v) { gDistortion = v; }
