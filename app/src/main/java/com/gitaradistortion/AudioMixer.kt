package com.gitaradistortion

import kotlin.math.abs
import kotlin.math.tanh
import kotlin.math.sin
import kotlin.math.PI

object AudioMixer {
    private const val SAFE_LIMIT = 0.95f

    // ✅ MASTER
    var masterOn = true
    var masterVolume = 0.5f

    // ✅ LAHAT NG FX — 50% DEFAULT — HIGIT SA 12!
    var noiseGate   = 0.5f   // Threshold
    var tone        = 0.5f   // Tono
    var gain        = 0.5f   // Lakas bago distorsyon
    var overdrive   = 0.5f   // Malambot na distorsyon
    var distortion  = 0.5f   // Matigas na distorsyon
    var fuzz        = 0.5f   // Lubog na tunog
    var chorus      = 0.5f   // Maririnig na marami
    var phaser      = 0.5f   // Lumilipad na tunog
    var tremolo     = 0.5f   // Pataas-pababa na lakas
    var vibrato     = 0.5f   // Umuugoy na tono
    var delay       = 0.5f   // Paulit-ulit na tunog
    var reverb      = 0.5f   // Alingawngaw ng silid
    var wah         = 0.5f   // Parang boses
    var ampType     = 0.5f   // Uri ng Amplifier
    var bass        = 0.5f   // Mababang tono
    var mid         = 0.5f   // Gitnang tono
    var treble      = 0.5f   // Mataas na tono

    // ✅ BUFFER PARA SA DELAY/CHORUS
    private val delayBuf = FloatArray(9600)
    private var delayIdx = 0
    private var phase = 0.0
    private var tremPhase = 0.0
    private var vibPhase = 0.0

    fun setAllOn(e:Boolean) { masterOn = e }
    fun isAllOn():Boolean = masterOn

    // ✅ I-APLAY ANG PRESET — KUSANG AYUS LAHAT NG PIHITAN!
    fun applyPreset(name:String) {
        when(name) {
            "Clean" -> {
                noiseGate=0.2f; tone=0.5f; gain=0.25f; overdrive=0.0f; distortion=0.0f; fuzz=0.0f
                chorus=0.15f; phaser=0.0f; tremolo=0.0f; vibrato=0.0f
                delay=0.15f; reverb=0.25f; wah=0.5f; ampType=0.3f
                bass=0.5f; mid=0.5f; treble=0.55f
            }
            "Blues" -> {
                noiseGate=0.3f; tone=0.55f; gain=0.45f; overdrive=0.45f; distortion=0.15f; fuzz=0.0f
                chorus=0.35f; phaser=0.15f; tremolo=0.1f; vibrato=0.15f
                delay=0.3f; reverb=0.4f; wah=0.55f; ampType=0.5f
                bass=0.55f; mid=0.6f; treble=0.5f
            }
            "Rock" -> {
                noiseGate=0.4f; tone=0.6f; gain=0.65f; overdrive=0.6f; distortion=0.55f; fuzz=0.2f
                chorus=0.2f; phaser=0.3f; tremolo=0.15f; vibrato=0.0f
                delay=0.4f; reverb=0.35f; wah=0.5f; ampType=0.65f
                bass=0.6f; mid=0.55f; treble=0.6f
            }
            "Metal" -> {
                noiseGate=0.6f; tone=0.7f; gain=0.85f; overdrive=0.3f; distortion=0.85f; fuzz=0.5f
                chorus=0.0f; phaser=0.4f; tremolo=0.0f; vibrato=0.0f
                delay=0.5f; reverb=0.45f; wah=0.7f; ampType=0.85f
                bass=0.7f; mid=0.5f; treble=0.7f
            }
        }
    }

    // ✅ I-SAVE ANG KASALUKUYANG SETTING BILANG PRESET
    fun savePreset(ctx: android.content.Context, name:String) {
        ctx.getSharedPreferences("GitaraPresets",0).edit().apply {
            putFloat("${name}_ng",noiseGate); putFloat("${name}_tone",tone)
            putFloat("${name}_gain",gain); putFloat("${name}_od",overdrive)
            putFloat("${name}_dist",distortion); putFloat("${name}_fuzz",fuzz)
            putFloat("${name}_chorus",chorus); putFloat("${name}_phaser",phaser)
            putFloat("${name}_trem",tremolo); putFloat("${name}_vib",vibrato)
            putFloat("${name}_delay",delay); putFloat("${name}_rev",reverb)
            putFloat("${name}_wah",wah); putFloat("${name}_amp",ampType)
            putFloat("${name}_bass",bass); putFloat("${name}_mid",mid)
            putFloat("${name}_treble",treble)
        }.apply()
    }

    // ✅ PAGPROSESO NG TUNOG — WALANG LATENCY!
    fun process(input:Float):Float {
        if(!masterOn) return 0f
        var sig = input

        // 🚧 NOISE GATE
        val ngTh = noiseGate * 0.15f
        if(abs(sig) < ngTh) sig = 0f

        // ⚡ GAIN
        sig *= 1f + gain * 1.2f

        // 🎵 TONE EQ
        val t = tone * 0.5f + 0.25f

        // 🎚️ BASS / MID / TREBLE
        sig = sig * (0.7f + bass*0.3f) * (0.8f + mid*0.2f) * (0.6f + treble*0.4f*t)

        // 🟠 OVERDRIVE
        if(overdrive > 0.05f) {
            val d = 1f + overdrive * 2.5f
            sig = tanh(sig * d) / d * (0.5f + overdrive * 0.5f)
        }

        // 🔴 DISTORTION
        if(distortion > 0.05f) {
            val d = 1f + distortion * 3f
            sig = tanh(sig * d) / d * (0.6f + distortion * 0.4f)
        }

        // ⚫ FUZZ
        if(fuzz > 0.05f) {
            val d = 1f + fuzz * 3.5f
            sig = (if(sig*d > 0.7f) 0.7f else if(sig*d < -0.7f) -0.7f else sig*d) * (0.5f + fuzz*0.4f)
        }

        // 🫧 CHORUS
        if(chorus > 0.05f) {
            phase += chorus * 0.06; if(phase > PI*2) phase -= PI*2
            val m = (sin(phase) * chorus * 0.15f).toFloat()
            sig = (sig + sig * (1f+m)) * 0.5f
        }

        // 🌀 PHASER
        if(phaser > 0.05f) {
            phase += phaser * 0.04; if(phase > PI*2) phase -= PI*2
            sig *= 0.7f + (sin(phase) * phaser * 0.35f).toFloat()
        }

        // 📳 TREMOLO
        if(tremolo > 0.05f) {
            tremPhase += tremolo * 0.08; if(tremPhase > PI*2) tremPhase -= PI*2
            sig *= 0.7f + (sin(tremPhase)*0.5f*tremolo).toFloat()
        }

        // 🎶 VIBRATO
        if(vibrato > 0.05f) {
            vibPhase += vibrato * 0.07; if(vibPhase > PI*2) vibPhase -= PI*2
            sig *= 0.85f + (sin(vibPhase)*0.3f*vibrato).toFloat()
        }

        // ⏱️ DELAY
        if(delay > 0.1f) {
            val sp = (delay * 1800f).toInt().coerceAtMost(delayBuf.size-1)
            val fb = delayBuf[(delayIdx - sp + delayBuf.size) % delayBuf.size]
            val out = sig + fb * 0.35f
            delayBuf[delayIdx] = out
            delayIdx = (delayIdx + 1) % delayBuf.size
            sig = out * 0.7f
        }

        // 🌊 REVERB
        if(reverb > 0.05f) {
            val sp = (reverb * 1500f).toInt().coerceAtMost(960)
            val fb = delayBuf[(delayIdx - sp + delayBuf.size) % delayBuf.size]
            sig = sig * (1f - reverb*0.6f) + fb * 0.3f * reverb
        }

        // 🎵 WAH
        if(wah > 0.05f) {
            sig *= 0.6f + wah * 0.8f
        }

        // 🔊 AMP SIMULATION
        val curve = 0.55f + ampType * 0.75f
        sig = tanh(sig / curve) * curve * 0.9f

        // 🎚️ MASTER VOLUME
        sig *= masterVolume

        return sig.coerceIn(-SAFE_LIMIT, SAFE_LIMIT)
    }
}
