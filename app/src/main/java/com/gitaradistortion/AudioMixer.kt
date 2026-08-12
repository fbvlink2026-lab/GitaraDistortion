package com.gitaradistortion

import kotlin.math.abs
import kotlin.math.exp

object AudioMixer {
    private const val SAFE_LIMIT = 0.9f

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

    fun process(input: Float): Float {
        try {
            var signal = input

            if (noiseGateOn && abs(signal) < noiseGateThreshold) {
                signal = 0f
            }

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
        } catch (_: Exception) {
            return 0f
        }
    }

    private fun tanh(x: Float): Float {
        val ex = exp(x)
        val enx = exp(-x)
        return (ex - enx) / (ex + enx)
    }

    // ✅ MGA GLOBAL NA TAWAG — PARA SA PEDAL BOARD!
    fun setNoiseGateEnabled(enabled: Boolean) { noiseGateOn = enabled }
    fun setNoiseGateLevel(level: Float) { noiseGateThreshold = level }
    fun setVolumeEnabled(enabled: Boolean) { volumeOn = enabled }
    fun setVolumeLevel(level: Float) { volumeLevel = level }
}
