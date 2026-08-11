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
static float gChorus = 0.0f;
static float gReverb = 0.0f;
static float prevSample = 0.0f;

class AudioCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* buffer = static_cast<float*>(data);
        for (int i = 0; i < numFrames; i++) {
            float x = buffer[i];
            
            // ⚡ GAIN
            float processed = x * gGain;
            
            // 💥 DISTORTION
            processed = std::tanh(processed * (1.0f + gDistortion * 4.0f));
            
            // 🎵 TONE
            if (gTone < 1.0f) {
                processed = processed * gTone + prevSample * (1.0f - gTone);
                prevSample = processed;
            }
            
            // 🔊 VOLUME
            buffer[i] = processed * gVolume * 0.8f;
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

#define SET_FUNC(NAME) \
extern "C" JNIEXPORT void JNICALL \
Java_com_gitaradistortion_MainActivity_set##NAME(JNIEnv*, jobject, jfloat v) { g##NAME = v; }

SET_FUNC(Volume)
SET_FUNC(Tone)
SET_FUNC(Distortion)
SET_FUNC(Gain)
SET_FUNC(Chorus)
SET_FUNC(Reverb)
