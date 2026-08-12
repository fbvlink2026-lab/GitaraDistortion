#include <oboe/Oboe.h>
#include <cmath>
#include <jni.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "GITARA", __VA_ARGS__)

// ✅ HULING GUMAGANA — TATLONG PIHITAN LANG MUNA + MASTER
static float gMasterVolume = 0.75f;
static float gVolume = 0.5f;
static float gTone = 0.5f;
static float gDistortion = 2.0f;

static std::shared_ptr<oboe::AudioStream> stream;

class DistortionCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* buffer = static_cast<float*>(data);
        
        for (int i = 0; i < numFrames; i++) {
            float input = buffer[i];

            // ✅ DISTORTION
            float drive = gDistortion;
            input = tanhf(input * drive);

            // ✅ TONE (LOW-PASS)
            static float last = 0.0f;
            float alpha = 0.05f + gTone * 0.15f;
            input = alpha * input + (1.0f - alpha) * last;
            last = input;

            // ✅ VOLUME + MASTER
            input *= gVolume * gMasterVolume;

            // ✅ LIMIT
            buffer[i] = input > 1.0f ? 1.0f : input < -1.0f ? -1.0f : input;
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static DistortionCallback callback;

// ✅ TATLONG PIHITAN + MASTER — TUGMA SA KOTLIN
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setMasterVolume(JNIEnv*, jobject, float v) {
    gMasterVolume = v < 0.05f ? 0.05f : v > 1.0f ? 1.0f : v;
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setVolumeLevel(JNIEnv*, jobject, float v) {
    gVolume = v;
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setToneLevel(JNIEnv*, jobject, float v) {
    gTone = v;
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDistortionGain(JNIEnv*, jobject, float v) {
    gDistortion = 1.0f + v * 4.0f;
}

// ✅ SIMULA — TAMA NA ANG OBOE DIRECTION!
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Input); // ✅ BASA MUNA NG MIKROPONO
    builder.setSampleRate(44100);
    builder.setChannelCount(1);
    builder.setFormat(oboe::AudioFormat::Float);
    builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
    builder.setDataCallback(&callback);
    
    oboe::Result res = builder.openStream(stream);
    if (res == oboe::Result::OK && stream) {
        stream->requestStart();
        LOGI("✅ AUDIO ENGINE NAKABUKAS!");
    } else {
        LOGI("❌ HINDI MAKABUKAS: %s", oboe::convertToText(res));
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(JNIEnv*, jobject) {
    if (stream) {
        stream->requestStop();
        stream->close();
        stream.reset();
    }
}
