#include <oboe/Oboe.h>
#include <cmath>
#include <jni.h>
#include <android/log.h>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "GITARA", __VA_ARGS__)

// === HELPER: C++ version ng coerceIn ===
template<typename T>
inline T clampVal(T v, T min, T max) {
    return v < min ? min : v > max ? max : v;
}

// ✅ MASTER
static float gMasterVolume = 0.75f;

// ✅ PANEL 1
static float gVolume = 0.5f;
static bool gVolumeEnabled = false;
static float gTone = 0.5f;
static bool gToneEnabled = false;
static float gReverbMix = 0.25f;
static float gReverbDecay = 0.5f;
static bool gReverbEnabled = false;
static float gGateThresh = 0.04f;
static float gGateRelease = 0.5f;
static bool gGateEnabled = false;

// ✅ PANEL 2
static float gGainDrive = 0.5f;
static float gGainLevel = 0.5f;
static bool gGainEnabled = false;
static float gOverdriveGain = 0.0f;
static float gOverdriveTone = 0.5f;
static float gOverdriveLevel = 0.5f;
static bool gOverdriveEnabled = false;
static float gDistGain = 0.0f;
static float gDistTone = 0.5f;
static float gDistLevel = 0.5f;
static bool gDistEnabled = false;
static float gPhaserRate = 0.5f;
static float gPhaserDepth = 0.5f;
static float gPhaserMix = 0.3f;
static bool gPhaserEnabled = false;

// ✅ PANEL 3 — DELAY + WAH-WAH
static float gDelayTime = 0.35f;
static float gDelayFeedback = 0.21f;
static float gDelayMix = 0.3f;
static bool gDelayEnabled = false;
static float gWahFreq = 0.5f;
static float gWahRange = 0.5f;
static bool gWahEnabled = false;

// ✅ BUFFER AT ESTADO
static float delayBuffer[48000] = {0.0f};
static int delayIndex = 0;
static float prevGate = 0.0f;
static float reverbBuffer[2][24000] = {{0.0f},{0.0f}};
static int reverbPos = 0;
static float wahPhase = 0.0f;
static float lastWah = 0.0f;
static float phaserPhase = 0.0f;

static std::shared_ptr<oboe::AudioStream> stream;

class DistortionCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream*, void* data, int32_t numFrames) override {
        
        float* buffer = static_cast<float*>(data);
        
        for (int i = 0; i < numFrames; i++) {
            float input = buffer[i];

            // === NOISE GATE ===
            if (gGateEnabled) {
                float level = fabs(input);
                float threshold = gGateThresh * 0.3f;
                if (level < threshold) {
                    prevGate *= 1.0f - gGateRelease * 0.05f;
                    input *= prevGate;
                } else {
                    prevGate = prevGate + 0.08f > 1.0f ? 1.0f : prevGate + 0.08f;
                    input *= prevGate;
                }
            }

            // === VOLUME ===
            if (gVolumeEnabled) input *= (0.2f + gVolume * 1.8f);

            // === TONE ===
            if (gToneEnabled) {
                static float last = 0.0f;
                float alpha = 0.02f + gTone * 0.18f;
                input = alpha * input + (1.0f - alpha) * last;
                last = input;
            }

            // === GAIN ===
            if (gGainEnabled) input *= (0.5f + gGainDrive * 3.5f) * (0.3f + gGainLevel * 0.7f);

            // === OVERDRIVE ===
            if (gOverdriveEnabled && gOverdriveGain > 0.01f) {
                float drive = 1.0f + gOverdriveGain * 4.0f;
                input = tanhf(input * drive) / drive;
                static float odLast = 0.0f;
                float odAlpha = 0.05f + gOverdriveTone * 0.15f;
                input = odAlpha * input + (1.0f - odAlpha) * odLast;
                odLast = input;
                input *= (0.3f + gOverdriveLevel * 0.7f);
            }

            // === DISTORTION ===
            if (gDistEnabled && gDistGain > 0.01f) {
                float drive = 1.0f + gDistGain * 5.0f;
                input = input < -1.0f/drive ? -1.0f : 
                        input > 1.0f/drive ? 1.0f : sinf(input * drive * (float)M_PI) / drive;
                static float distLast = 0.0f;
                float dAlpha = 0.05f + gDistTone * 0.15f;
                input = dAlpha * input + (1.0f - dAlpha) * distLast;
                distLast = input;
                input *= (0.3f + gDistLevel * 0.7f);
            }

            // === PHASER ===
            if (gPhaserEnabled && gPhaserMix > 0.01f) {
                phaserPhase += 0.001f + gPhaserRate * 0.005f;
                if (phaserPhase > (float)M_PI * 2.0f) phaserPhase -= (float)M_PI * 2.0f;
                float lfo = sinf(phaserPhase) * gPhaserDepth;
                float delayed = sinf(input * 4.0f + lfo);
                input = input * (1.0f - gPhaserMix) + delayed * gPhaserMix * 0.5f;
            }

            // === WAH-WAH ===
            if (gWahEnabled) {
                float baseFreq = 200.0f + gWahFreq * 1800.0f;
                float range = gWahRange * 800.0f;
                wahPhase += 2.0f * (float)M_PI * 0.5f / 44100.0f;
                float lfo = (sinf(wahPhase) + 1.0f) * 0.5f;
                float cutoff = baseFreq + lfo * range;
                float alpha = cutoff / 22050.0f;
                input = alpha * input + (1.0f - alpha) * lastWah;
                lastWah = input;
            }

            // === DELAY / ECHO ===
            if (gDelayEnabled) {
                int delaySamples = (int)(gDelayTime * 44100.0f);
                float delayed = delayBuffer[(delayIndex - delaySamples + 48000) % 48000];
                float dry = input;
                input = dry * (1.0f - gDelayMix) + delayed * gDelayMix;
                delayBuffer[delayIndex] = dry + delayed * gDelayFeedback;
                delayIndex = (delayIndex + 1) % 48000;
            }

            // === REVERB ===
            if (gReverbEnabled && gReverbMix > 0.01f) {
                int d1 = (int)(gReverbDecay * 8000.0f) + 1000;
                float fb = gReverbDecay * 0.5f;
                float rv = reverbBuffer[0][(reverbPos - d1 + 24000) % 24000];
                reverbBuffer[0][reverbPos] = input + rv * fb;
                float outRv = reverbBuffer[0][(reverbPos - d1/2 + 24000) % 24000];
                input = input * (1.0f - gReverbMix) + outRv * gReverbMix;
                reverbPos = (reverbPos + 1) % 24000;
            }

            // === MASTER VOLUME ===
            input *= gMasterVolume;

            // I-saferi para hindi pumutok ang tunog
            buffer[i] = input > 1.0f ? 1.0f : input < -1.0f ? -1.0f : input;
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static DistortionCallback callback;

// ✅ MASTER
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setMasterVolume(JNIEnv*, jobject, float v) {
    gMasterVolume = clampVal(v, 0.05f, 1.0f);
}

// ✅ PANEL 1
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setVolumeLevel(JNIEnv*, jobject, float v) {
    gVolume = v;
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setVolumeEnabled(JNIEnv*, jobject, jboolean e) {
    gVolumeEnabled = e;
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setToneLevel(JNIEnv*, jobject, float v) {
    gTone = v;
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setToneEnabled(JNIEnv*, jobject, jboolean e) {
    gToneEnabled = e;
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setReverbMix(JNIEnv*, jobject, float v) {
    gReverbMix = clampVal(v, 0.0f, 0.8f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setReverbDecay(JNIEnv*, jobject, float v) {
    gReverbDecay = clampVal(v, 0.1f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setReverbEnabled(JNIEnv*, jobject, jboolean e) {
    gReverbEnabled = e;
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setNoiseGateThresh(JNIEnv*, jobject, float v) {
    gGateThresh = clampVal(v, 0.0f, 0.5f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setNoiseGateRelease(JNIEnv*, jobject, float v) {
    gGateRelease = clampVal(v, 0.1f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setNoiseGateEnabled(JNIEnv*, jobject, jboolean e) {
    gGateEnabled = e;
}

// ✅ PANEL 2
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setGainDrive(JNIEnv*, jobject, float v) {
    gGainDrive = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setGainLevel(JNIEnv*, jobject, float v) {
    gGainLevel = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setGainEnabled(JNIEnv*, jobject, jboolean e) {
    gGainEnabled = e;
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setOverdriveGain(JNIEnv*, jobject, float v) {
    gOverdriveGain = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setOverdriveTone(JNIEnv*, jobject, float v) {
    gOverdriveTone = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setOverdriveLevel(JNIEnv*, jobject, float v) {
    gOverdriveLevel = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setOverdriveEnabled(JNIEnv*, jobject, jboolean e) {
    gOverdriveEnabled = e;
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDistortionGain(JNIEnv*, jobject, float v) {
    gDistGain = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDistortionTone(JNIEnv*, jobject, float v) {
    gDistTone = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDistortionLevel(JNIEnv*, jobject, float v) {
    gDistLevel = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDistortionEnabled(JNIEnv*, jobject, jboolean e) {
    gDistEnabled = e;
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setPhaserRate(JNIEnv*, jobject, float v) {
    gPhaserRate = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setPhaserDepth(JNIEnv*, jobject, float v) {
    gPhaserDepth = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setPhaserMix(JNIEnv*, jobject, float v) {
    gPhaserMix = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setPhaserEnabled(JNIEnv*, jobject, jboolean e) {
    gPhaserEnabled = e;
}

// ✅ PANEL 3 — DELAY + WAH-WAH
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDelayTime(JNIEnv*, jobject, float v) {
    gDelayTime = clampVal(v, 0.1f, 0.8f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDelayFeedback(JNIEnv*, jobject, float v) {
    gDelayFeedback = clampVal(v, 0.1f, 0.6f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDelayMix(JNIEnv*, jobject, float v) {
    gDelayMix = clampVal(v, 0.0f, 0.8f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setDelayEnabled(JNIEnv*, jobject, jboolean e) {
    gDelayEnabled = e;
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setWahFreq(JNIEnv*, jobject, float v) {
    gWahFreq = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setWahRange(JNIEnv*, jobject, float v) {
    gWahRange = clampVal(v, 0.0f, 1.0f);
}
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_setWahEnabled(JNIEnv*, jobject, jboolean e) {
    gWahEnabled = e;
}

// ✅ SIMULA AT TIGIL
extern "C" JNIEXPORT void JNICALL
Java_com_gitaradistortion_MainActivity_startAudioEngine(JNIEnv*, jobject) {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::InputOutput);
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
        LOGI("❌ HINDI MAKABUKAS NG AUDIO STREAM");
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
