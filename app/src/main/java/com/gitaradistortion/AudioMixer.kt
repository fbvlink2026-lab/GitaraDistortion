package com.gitaradistortion

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.abs
import kotlin.math.tanh
import kotlin.math.sin
import kotlin.math.PI

object AudioMixer {
    private const val SAFE_LIMIT = 0.95f
    private lateinit var prefs: SharedPreferences

    var masterOn = false  // ✅ HINDI AGAD BUKAS
    var masterVolume = 0.5f

    var noiseGate   = 0.5f
    var tone        = 0.5f
    var gain        = 0.5f
    var overdrive   = 0.5f
    var distortion  = 0.5f
    var fuzz        = 0.5f
    var chorus      = 0.5f
    var phaser      = 0.5f
    var tremolo     = 0.5f
    var vibrato     = 0.5f
    var delay       = 0.5f
    var reverb      = 0.5f
    var wah         = 0.5f
    var ampType     = 0.5f
    var bass        = 0.5f
    var mid         = 0.5f
    var treble      = 0.5f

    private val delayBuf = FloatArray(9600)
    private var delayIdx = 0
    private var phase = 0.0; private var tremPhase = 0.0; private var vibPhase = 0.0

    fun init(ctx: Context) { prefs = ctx.getSharedPreferences("GitaraPresets", 0) }
    fun setAllOn(e:Boolean) { masterOn = e }
    fun isAllOn():Boolean = masterOn

    fun applyPreset(name:String) {
        when(name) {
            "Clean" -> { noiseGate=0.2f;tone=0.5f;gain=0.25f;overdrive=0.0f;distortion=0.0f;fuzz=0.0f;chorus=0.15f;phaser=0.0f;tremolo=0.0f;vibrato=0.0f;delay=0.15f;reverb=0.25f;wah=0.5f;ampType=0.3f;bass=0.5f;mid=0.5f;treble=0.55f;masterVolume=0.5f }
            "Blues" -> { noiseGate=0.3f;tone=0.55f;gain=0.45f;overdrive=0.45f;distortion=0.15f;fuzz=0.0f;chorus=0.35f;phaser=0.15f;tremolo=0.1f;vibrato=0.15f;delay=0.3f;reverb=0.4f;wah=0.55f;ampType=0.5f;bass=0.55f;mid=0.6f;treble=0.5f;masterVolume=0.5f }
            "Rock" -> { noiseGate=0.4f;tone=0.6f;gain=0.65f;overdrive=0.6f;distortion=0.55f;fuzz=0.2f;chorus=0.2f;phaser=0.3f;tremolo=0.15f;vibrato=0.0f;delay=0.4f;reverb=0.35f;wah=0.5f;ampType=0.65f;bass=0.6f;mid=0.55f;treble=0.6f;masterVolume=0.5f }
            "Metal" -> { noiseGate=0.6f;tone=0.7f;gain=0.85f;overdrive=0.3f;distortion=0.85f;fuzz=0.5f;chorus=0.0f;phaser=0.4f;tremolo=0.0f;vibrato=0.0f;delay=0.5f;reverb=0.45f;wah=0.7f;ampType=0.85f;bass=0.7f;mid=0.5f;treble=0.7f;masterVolume=0.5f }
        }
    }

    fun savePreset(name:String) {
        prefs.edit().apply {
            putFloat("${name}_ng",noiseGate); putFloat("${name}_tone",tone)
            putFloat("${name}_gain",gain); putFloat("${name}_od",overdrive)
            putFloat("${name}_dist",distortion); putFloat("${name}_fuzz",fuzz)
            putFloat("${name}_chorus",chorus); putFloat("${name}_phaser",phaser)
            putFloat("${name}_trem",tremolo); putFloat("${name}_vib",vibrato)
            putFloat("${name}_delay",delay); putFloat("${name}_rev",reverb)
            putFloat("${name}_wah",wah); putFloat("${name}_amp",ampType)
            putFloat("${name}_bass",bass); putFloat("${name}_mid",mid)
            putFloat("${name}_treble",treble); putFloat("${name}_master",masterVolume)
            putStringSet("__list__", getPresetNames().toMutableSet().apply { add(name) })
        }.apply()
    }

    fun getPresetNames():Set<String> = prefs.getStringSet("__list__", setOf("Clean","Blues","Rock","Metal"))!!
    fun getAllValues():FloatArray = floatArrayOf(noiseGate,tone,gain,overdrive,distortion,fuzz,chorus,phaser,tremolo,vibrato,delay,reverb,wah,ampType,bass,mid,treble,masterVolume)

    fun process(input:Float):Float {
        if(!masterOn) return 0f
        var sig = input
        val ngTh = noiseGate * 0.15f
        if(abs(sig) < ngTh) sig = 0f
        sig *= 1f + gain * 1.2f
        val t = tone * 0.5f + 0.25f
        sig = sig * (0.7f + bass*0.3f) * (0.8f + mid*0.2f) * (0.6f + treble*0.4f*t)
        if(overdrive>0.05f){val d=1f+overdrive*2.5f;sig=tanh(sig*d)/d*(0.5f+overdrive*0.5f)}
        if(distortion>0.05f){val d=1f+distortion*3f;sig=tanh(sig*d)/d*(0.6f+distortion*0.4f)}
        if(fuzz>0.05f){val d=1f+fuzz*3.5f;sig=(if(sig*d>0.7f)0.7f else if(sig*d<-0.7f)-0.7f else sig*d)*(0.5f+fuzz*0.4f)}
        if(chorus>0.05f){phase+=chorus*0.06;if(phase>PI*2)phase-=PI*2;sig=(sig+sig*(1f+(sin(phase)*chorus*0.15f).toFloat()))*0.5f}
        if(phaser>0.05f){phase+=phaser*0.04;if(phase>PI*2)phase-=PI*2;sig*=0.7f+(sin(phase)*phaser*0.35f).toFloat()}
        if(tremolo>0.05f){tremPhase+=tremolo*0.08;if(tremPhase>PI*2)tremPhase-=PI*2;sig*=0.7f+(sin(tremPhase)*0.5f*tremolo).toFloat()}
        if(vibrato>0.05f){vibPhase+=vibrato*0.07;if(vibPhase>PI*2)vibPhase-=PI*2;sig*=0.85f+(sin(vibPhase)*0.3f*vibrato).toFloat()}
        if(delay>0.1f){val sp=(delay*1800f).toInt().coerceAtMost(delayBuf.size-1)
            val fb=delayBuf[(delayIdx-sp+delayBuf.size)%delayBuf.size]
            val out=sig+fb*0.35f;delayBuf[delayIdx]=out;delayIdx=(delayIdx+1)%delayBuf.size;sig=out*0.7f}
        if(reverb>0.05f){val sp=(reverb*1500f).toInt().coerceAtMost(960)
            val fb=delayBuf[(delayIdx-sp+delayBuf.size)%delayBuf.size]
            sig=sig*(1f-reverb*0.6f)+fb*0.3f*reverb}
        if(wah>0.05f)sig*=0.6f+wah*0.8f
        val curve=0.55f+ampType*0.75f;sig=tanh(sig/curve)*curve*0.9f
        sig*=masterVolume
        return sig.coerceIn(-SAFE_LIMIT,SAFE_LIMIT)
    }
}
