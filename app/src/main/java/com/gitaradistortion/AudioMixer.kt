package com.gitaradistortion

import kotlin.math.abs
import kotlin.math.exp

object AudioMixer {
    private const val SAFE_LIMIT = 0.9f

    // ✅ MASTER — PAMATAY NG LAHAT!
    var masterOn = true
    var masterLevel = 0.6f

    var noiseGateThreshold = 0.04f
    var volumeLevel = 0.75f
    var gainAmount = 0.5f
    var distortionAmount = 0.3f
    var toneAmount = 0.5f

    var noiseGateOn = true
    var volumeOn = true
    var gainOn = false
    var distortionOn = false
    var toneOn = false

    // ✅ MASTER CONTROL
    fun setMasterEnabled(enabled: Boolean) { masterOn = enabled }

    fun updateNoiseGateEnabled(enabled: Boolean) { noiseGateOn = enabled }
    fun updateNoiseGateThreshold(value: Float) { noiseGateThreshold = value }
    fun updateVolumeEnabled(enabled: Boolean) { volumeOn = enabled }
    fun updateVolumeLevel(value: Float) { volumeLevel = value }
    fun updateGainEnabled(enabled: Boolean) { gainOn = enabled }
    fun updateGainLevel(value: Float) { gainAmount = value }

    fun process(input: Float): Float {
        try {
            // ✅ KAPAG MASTER OFF — WALANG TUNOG LAHAT!
            if (!masterOn) return 0f

            var signal = input

            if (noiseGateOn && abs(signal) < noiseGateThreshold) signal = 0f
            if (volumeOn) signal *= volumeLevel
            if (gainOn) signal *= (1f + gainAmount * 0.6f)

            if (distortionOn && distortionAmount > 0.01f) {
                val drive = 1f + distortionAmount * 2.5f
                val x = signal * drive
                signal = tanh(x) / drive * (1f + distortionAmount * 0.25f)
            }

            if (toneOn) signal *= (0.7f + toneAmount * 0.6f)

            signal *= masterLevel
            signal = signal.coerceIn(-SAFE_LIMIT, SAFE_LIMIT)
            return signal
        } catch (_: Exception) { return 0f }
    }

    private fun tanh(x: Float): Float {
        val ex = exp(x)
        val enx = exp(-x)
        return (ex - enx) / (ex + enx)
    }
}
