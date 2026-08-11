#include <oboe/Oboe.h>
#include <cmath>
#include <jni.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "GITARA", __VA_ARGS__)

static float gVolume = 0.5f;
static float gTone = 0.5f;
static float gDistortion = 2.0f;
static float prevSample = 0.0f;
static std::shared_ptr<oboe::AudioStream> stream;

class DistortionCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* buffer = static_cast<float*>(data);
        
        for (int i = 0; i < numFrames; i++) {
            float input = buffer[i];
            
            // DISTORTION — TANH() = ANG TUNAY NA PUMIPUTOL NG ALON!
            float processed = std::tanh(input * gDistortion);
            
            // TONE — MALAMBOT O MATINIS
            if (gTone < 1.0f) {
                processed = processed * gTone + prevSample * (1.0f - gTone);
                prevSample = processed;
            }
            
            // VOLUME — PALAKIIN O BAWASAN
            buffer[i] = processed * gVolume * 0.8f;
        }
        
        return oboe::DataCallbackResult::Continue;
    }
};

static DistortionCallback callback;

// ============== MGA UTOS MULA SA ANDROID ==============
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(
    JNIEnv*, jobject) {

    static DistortionCallback cb;  // ✅ ILIPAT DITO SA LOOB!

    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output)
           ->setSampleRate(44100)
           ->setFormat(oboe::AudioFormat::Float)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setCallback(&cb);

        auto result = builder.openStream(stream);
    if (result == oboe::Result::OK) {
        stream->requestStart();
        LOGI("✅ NAKA-ON! TUMUGTOG KA NA!");
    } else {
        LOGI("⚠️ HINDI MA-BUKAS ANG AUDIO: %s", oboe::convertToText(result));
    }
    
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_stopAudioEngine(
    JNIEnv*, jobject) {
    
    if (stream) {
        stream->stop();
        stream->close();
        stream.reset();
        LOGI("⏹️ NAKA-OFF");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setVolume(
    JNIEnv*, jobject, jfloat value) { gVolume = value; }

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setTone(
    JNIEnv*, jobject, jfloat value) { gTone = value; }

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDistortion(
    JNIEnv*, jobject, jfloat value) { gDistortion = value; }
