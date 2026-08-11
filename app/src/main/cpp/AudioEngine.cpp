#include <oboe/Oboe.h>
#include <cmath>
#include <jni.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "GITARA", __VA_ARGS__)

static float gVolume = 0.5f;
static float gTone = 0.5f;
static float gDistortion = 2.0f;
static float gGain = 1.0f;
static float gChorus = 0.0f;
static float gReverb = 0.0f;
static float prevSample = 0.0f;

class DistortionCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* buffer = static_cast<float*>(data);
        
        for (int i = 0; i < numFrames; i++) {
            float input = buffer[i];
            float processed = input * gGain;
            processed = std::tanh(processed * gDistortion);
            if (gTone < 1.0f) {
                processed = processed * gTone + prevSample * (1.0f - gTone);
                prevSample = processed;
            }
            buffer[i] = processed * gVolume * 0.8f;
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static DistortionCallback callback;
static std::shared_ptr<oboe::AudioStream> stream;

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output)
           ->setSampleRate(44100)
           ->setFormat(oboe::AudioFormat::Float)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setCallback(&callback);
    auto result = builder.openStream(stream);
    if (result == oboe::Result::OK) {
        stream->requestStart();
        LOGI("✅ NAKA-ON ANG AUDIO!");
    } else {
        LOGI("⚠️ HINDI MA-BUKAS: %s", oboe::convertToText(result));
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (stream) {
        stream->stop();
        stream->close();
        stream.reset();
        LOGI("⏹️ NAKA-OFF ANG AUDIO");
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
