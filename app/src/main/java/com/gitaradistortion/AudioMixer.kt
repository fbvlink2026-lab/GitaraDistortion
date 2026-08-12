package com.gitaradistortion

// 🎸 iRig/Tonebridge Style — Tamang daloy + Kontroladong lakas
object AudioMixer {
    private const val SAFE_LIMIT = 0.9f // 🛡️ HINDI LALAMPAS DITO!

    // 📊 Bawat antas — mula sa mga pihitan
    var noiseGateThreshold = 0.04f
    var volumeLevel = 0.75f
    var gainAmount = 0.5f
    var distortionAmount = 0.3f
    var toneAmount = 0.5f
    var masterLevel = 0.6f

    var noiseGateOn = false
    var volumeOn = true
    var gainOn = false
    var distortionOn = false
    var toneOn = false

    // ✅ TAMANG DALOY — KATULAD NG TONEBRIDGE!
    fun process(input: Float): Float {
        try {
            var signal = input

            // 1️⃣ NOISE GATE — patayin kung mahina
            if (noiseGateOn && abs(signal) < noiseGateThreshold) {
                signal = 0f
            }

            // 2️⃣ VOLUME
            if (volumeOn) signal *= volumeLevel

            // 3️⃣ GAIN
            if (gainOn) signal *= (1f + gainAmount * 0.6f)

            // 4️⃣ DISTORTION
            if (distortionOn && distortionAmount > 0.01f) {
                val drive = 1f + distortionAmount * 2.5f
                val x = signal * drive
                signal = tanh(x) / drive * (1f + distortionAmount * 0.25f)
            }

            // 5️⃣ TONE
            if (toneOn) signal *= (0.7f + toneAmount * 0.6f)

            // 🔒 MASTER — LIGTAS NA LIMIT
            signal *= masterLevel
            signal = signal.coerceIn(-SAFE_LIMIT, SAFE_LIMIT)

            return signal
        } catch (_: Exception) {
            return 0f // ✅ KAPAG MAY MALI → WALANG TUNOG, WALANG SABOG!
        }
    }

    private fun tanh(x: Float): Float {
        val ex = Math.exp(x.toDouble()).toFloat()
        val enx = Math.exp(-x.toDouble()).toFloat()
        return (ex - enx) / (ex + enx)
    }
}
