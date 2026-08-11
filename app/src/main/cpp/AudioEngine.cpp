#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>

#define LOG_TAG "GITARA"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// ✅ MGA PIHITAN
static float gVolume    = 0.7f;
static float gTone      = 0.5f;   // 🎵 TONE — ITO ANG AYUSIN NATIN
static float gNoiseGate  = 0.04f; // ✅ PAMPATAY NG INGAY — MAS MALINAW!

// ✅ BUFFER PARA SA TONE FILTER
static float prevLpf = 0.0f;

static std::shared_ptr<oboe::AudioStream> stream;

// ✅ ISANG CALLBACK LANG — TULAD NG TONEBRIDGE!
class AudioCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* buffer = static_cast<float*>(data);

        for (int i = 0; i < numFrames; i++) {
            float input = buffer[i];

            // ✅ NOISE GATE — PATAYIN ANG INGAY KUNG MAHINA ANG TUGTOG
            if (std::fabs(input) < gNoiseGate) {
                input = 0.0f;
            }

            // ✅ TONE FILTER — TULAD NG TONEBRIDGE!
            // Tone = 0.0 → Puro BASS (malambot)
            // Tone = 0.5 → KATINIG (natural)
            // Tone = 1.0 → PURO TREBLE (matalas)
            float alpha = gTone * 0.85f + 0.05f;
            float lpf = alpha * prevLpf + (1.0f - alpha) * input;
            float hpf = input - lpf;
            float processed = lpf * (1.0f - gTone) * 2.0f + hpf * gTone * 1.3f;

            prevLpf = lpf;

            // ✅ ILABAS
            buffer[i] = processed * gVolume;
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static AudioCallback callback;

extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    if (stream) return;

    // ✅ ISANG STREAM LANG — BASAHIN AT ILABAS NANG SABAY! TULAD NG TONEBRIDGE!
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output)
           ->setSampleRate(48000)          // ✅ MAS MATAAS NA KALIDAD
           ->setFormat(oboe::AudioFormat::Float)
           ->setChannelCount(1)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setUsage(oboe::Usage::Media)
           ->setContentType(oboe::ContentType::Music)
           ->setCallback(&callback);

    auto result = builder.openStream(stream);
    if (result == oboe::Result::OK) {
        stream->requestStart();
        LOGI("✅ TONEBRIDGE MODE — NAKA-ON!");
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
        prevLpf = 0.0f;
        LOGI("⏹️ NAKA-OFF");
    }
}

SET_FUNC(Volume)
SET_FUNC(Tone)
