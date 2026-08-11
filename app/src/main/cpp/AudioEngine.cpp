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

static std::shared_ptr<oboe::AudioStream> stream;

class AudioCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* buffer = static_cast<float*>(data);
        
        // ✅ BASAHIN → PALITAD → ILABAS — SA ISANG GALAWAN LANG! TULAD NG TONEBRIDGE!
        for (int i = 0; i < numFrames; i++) {
            float input = buffer[i];          // 🎤 BASAHIN MULA SA MIKROFONO/GITARA
            float processed = input;
            
            // ⚡ DAGDAG-LAKAS
            processed *= gGain * 2.0f;
            
            // 💥 DISTORTION — PUMUTOL ANG TUNOG
            float drive = 1.0f + gDistortion * 5.0f;
            processed = std::tanh(processed * drive);
            
            // 🎵 TONE — MALINAW O MALAMBOT
            if (gTone < 1.0f) {
                processed = processed * gTone + prevSample * (1.0f - gTone);
                prevSample = processed;
            }
            
            // 🔊 ILABAS ANG TUNOG
            buffer[i] = processed * gVolume * 0.9f;
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static AudioCallback callback;

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    if (stream) return;

    // ✅ ISANG STREAM LANG — BASAHIN AT ILABAS NANG SABAY! TAMA ITO!
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output)
           ->setSampleRate(44100)
           ->setFormat(oboe::AudioFormat::Float)
           ->setChannelCount(1)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setUsage(oboe::Usage::Media)
           ->setContentType(oboe::ContentType::Music)
           ->setCallback(&callback);

    auto result = builder.openStream(stream);
    if (result == oboe::Result::OK) {
        stream->requestStart();
        LOGI("✅ TUNOG NAKA-ON! ISAKSAK ANG GITARA!");
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
        LOGI("⏹️ NAKA-OFF");
    }
}

#define SET_FUNC(NAME) \
extern "C" JNIEXPORT void JNICALL \
Java_com_gitaradistortion_MainActivity_set##NAME(JNIEnv*, jobject, jfloat v) { g##NAME = v; }

SET_FUNC(Volume)
SET_FUNC(Tone)
SET_FUNC(Distortion)
SET_FUNC(Gain)
