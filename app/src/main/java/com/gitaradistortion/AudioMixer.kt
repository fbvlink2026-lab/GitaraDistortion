package com.gitaradistortion

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.tanh
import kotlin.math.sin
import kotlin.math.PI

object AudioMixer {
    private const val SAFE_LIMIT = 0.9f

    var masterOn = true
    var masterLevel = 0.6f

    var noiseGateOn = true
    var noiseGateThreshold = 0.04f
    var noiseGateRelease = 0.3f

    var volumeOn = true
    var volumeLevel = 0.75f

    var gainOn = false
    var gainAmount = 0.5f

    var overdriveOn = false
    var overdriveDrive = 0.5f
    var overdriveLevel = 0.7f

    var distortionOn = false
    var distortionGain = 0.6f
    var distortionTone = 0.5f

    var chorusOn = false
    var chorusSpeed = 0.3f
    var chorusDepth = 0.5f
    private var chorusPhase = 0.0

    var delayOn = false
    var delayTime = 0.4f
    var delayFeedback = 0.3f
    private val delayBuffer = FloatArray(9600)
    private var delayIndex = 0

    var wahOn = false
    var reverbOn = false
    var reverbMix = 0.25f
    var reverbDecay = 0.4f
    var ampOn = false
    var ampGain = 0.5f
    var ampResponse = 0.6f
    var wahPosition = 0.5f
    var wahResonance = 0.5f
    var wahLevel = 0.7f

    fun setMasterEnabled(e:Boolean) { masterOn=e }
    fun setNoiseGate(e:Boolean,v:Float,r:Float) { noiseGateOn=e; noiseGateThreshold=v.coerceIn(0.005f,0.15f); noiseGateRelease=r.coerceIn(0.05f,0.8f) }
    fun setVolume(e:Boolean,v:Float) { volumeOn=e; volumeLevel=v.coerceIn(0.05f,1f) }
    fun setGain(e:Boolean,v:Float) { gainOn=e; gainAmount=v.coerceIn(0f,1f) }
    fun setOverdrive(e:Boolean,drive:Float,lvl:Float) { overdriveOn=e; overdriveDrive=drive.coerceIn(0f,1f); overdriveLevel=lvl.coerceIn(0f,1f) }
    fun setDistortion(e:Boolean,gain:Float,tone:Float) { distortionOn=e; distortionGain=gain.coerceIn(0f,1f); distortionTone=tone.coerceIn(0f,1f) }
    fun setChorus(e:Boolean,speed:Float,depth:Float) { chorusOn=e; chorusSpeed=speed.coerceIn(0f,1f); chorusDepth=depth.coerceIn(0f,1f) }
    fun setDelay(e:Float,fb:Float) { delayOn=e>0.5f; delayTime=e.coerceIn(0.1f,0.8f); delayFeedback=fb.coerceIn(0f,0.7f) }
    fun setWah(e:Boolean,pos:Float,q:Float,lvl:Float) { wahOn=e; wahPosition=pos.coerceIn(0f,1f); wahResonance=q.coerceIn(0f,1f); wahLevel=lvl.coerceIn(0.1f,1.2f) }
    fun setReverb(e:Boolean,mix:Float,decay:Float) { reverbOn=e; reverbMix=mix.coerceIn(0f,1f); reverbDecay=decay.coerceIn(0.1f,0.8f) }
    fun setAmp(e:Boolean,gain:Float,resp:Float) { ampOn=e; ampGain=gain.coerceIn(0f,1f); ampResponse=resp.coerceIn(0.2f,1f) }

    fun process(input:Float):Float {
        if(!masterOn) return 0f
        var sig=input

        if(noiseGateOn && abs(sig)<noiseGateThreshold) sig=0f
        if(volumeOn) sig*=volumeLevel
        if(gainOn) sig*=(1f+gainAmount*0.6f)

        if(overdriveOn && overdriveDrive>0.01f) {
            val d=1.5f+overdriveDrive*2f
            sig=tanh(sig*d)/d*(0.5f+overdriveLevel*0.7f)
        }

        if(distortionOn && distortionGain>0.01f) {
            val d=1f+distortionGain*2.5f
            sig=tanh(sig*d)/d*(0.7f+distortionTone*0.5f)
        }

        if(chorusOn && chorusDepth>0.05f) {
            chorusPhase += chorusSpeed*0.05
            if(chorusPhase>PI*2) chorusPhase-=PI*2
            val mod=1f+sin(chorusPhase)*chorusDepth*0.15f
            sig = (sig + sig*mod)*0.5f
        }

        if(delayOn && delayTime>0.1f) {
            val delaySamples=(delayTime*48000).toInt().coerceAtMost(delayBuffer.size-1)
            val delayed=delayBuffer[(delayIndex-delaySamples+delayBuffer.size)%delayBuffer.size]
            val out=sig + delayed*delayFeedback
            delayBuffer[delayIndex]=out
            delayIndex=(delayIndex+1)%delayBuffer.size
            sig=out*0.7f
        }

        if(wahOn) {
        if(reverbOn && reverbMix>0.05f) {
            val rt = reverbDecay * 1200
            var feedback = 0f
            val delay = (rt).toInt()
            sig = sig * (1f-reverbMix) + sig * feedback * reverbMix
        }
        if(ampOn && ampGain>0.05f) {
            val curve = 0.7f + ampResponse * 0.6f
            val drive = 1f + ampGain * 2f
            sig = kotlin.math.tanh(sig * drive / curve) * curve * 0.85f
        }
            val q = 0.25f + wahResonance * 0.6f
        if(reverbOn && reverbMix>0.05f) {
            val rt = reverbDecay * 1200
            var feedback = 0f
            val delay = (rt).toInt()
            sig = sig * (1f-reverbMix) + sig * feedback * reverbMix
        }
        if(ampOn && ampGain>0.05f) {
            val curve = 0.7f + ampResponse * 0.6f
            val drive = 1f + ampGain * 2f
            sig = kotlin.math.tanh(sig * drive / curve) * curve * 0.85f
        }
            val boost = 0.8f + wahLevel * 0.5f
        if(reverbOn && reverbMix>0.05f) {
            val rt = reverbDecay * 1200
            var feedback = 0f
            val delay = (rt).toInt()
            sig = sig * (1f-reverbMix) + sig * feedback * reverbMix
        }
        if(ampOn && ampGain>0.05f) {
            val curve = 0.7f + ampResponse * 0.6f
            val drive = 1f + ampGain * 2f
            sig = kotlin.math.tanh(sig * drive / curve) * curve * 0.85f
        }
            sig = sig * boost * (0.5f + wahPosition * q)
        if(reverbOn && reverbMix>0.05f) {
            val rt = reverbDecay * 1200
            var feedback = 0f
            val delay = (rt).toInt()
            sig = sig * (1f-reverbMix) + sig * feedback * reverbMix
        }
        if(ampOn && ampGain>0.05f) {
            val curve = 0.7f + ampResponse * 0.6f
            val drive = 1f + ampGain * 2f
            sig = kotlin.math.tanh(sig * drive / curve) * curve * 0.85f
        }
        }
        if(reverbOn && reverbMix>0.05f) {
            val rt = reverbDecay * 1200
            var feedback = 0f
            val delay = (rt).toInt()
            sig = sig * (1f-reverbMix) + sig * feedback * reverbMix
        }
        if(ampOn && ampGain>0.05f) {
            val curve = 0.7f + ampResponse * 0.6f
            val drive = 1f + ampGain * 2f
            sig = kotlin.math.tanh(sig * drive / curve) * curve * 0.85f
        }

        sig*=masterLevel
        return sig.coerceIn(-SAFE_LIMIT,SAFE_LIMIT)
    }

    fun applyPreset(name:String) {
        when(name) {
            "Clean" -> {
                noiseGateOn=true; noiseGateThreshold=0.02f; noiseGateRelease=0.3f
                volumeOn=true; volumeLevel=0.85f
                gainOn=false; overdriveOn=false; distortionOn=false
                chorusOn=false; delayOn=false; wahOn=false
                reverbOn=false
                ampOn=false
            }
            "Blues" -> {
                noiseGateOn=true; noiseGateThreshold=0.03f; noiseGateRelease=0.3f
                volumeOn=true; volumeLevel=0.7f
                gainOn=true; gainAmount=0.35f
                overdriveOn=true; overdriveDrive=0.45f; overdriveLevel=0.65f
                distortionOn=false
                chorusOn=true; chorusSpeed=0.25f; chorusDepth=0.35f
                delayOn=true; delayTime=0.3f; delayFeedback=0.25f
                wahOn=false
                reverbOn=false
                ampOn=false
            }
            "Rock" -> {
                noiseGateOn=true; noiseGateThreshold=0.04f; noiseGateRelease=0.3f
                volumeOn=true; volumeLevel=0.65f
                gainOn=true; gainAmount=0.55f
                overdriveOn=true; overdriveDrive=0.6f; overdriveLevel=0.7f
                distortionOn=true; distortionGain=0.5f; distortionTone=0.5f
                chorusOn=false
                delayOn=true; delayTime=0.35f; delayFeedback=0.3f
                wahOn=false
                reverbOn=false
                ampOn=false
            }
            "Metal" -> {
                noiseGateOn=true; noiseGateThreshold=0.06f; noiseGateRelease=0.3f
                volumeOn=true; volumeLevel=0.55f
                gainOn=true; gainAmount=0.75f
                overdriveOn=false
                distortionOn=true; distortionGain=0.85f; distortionTone=0.65f
                chorusOn=false
                delayOn=true; delayTime=0.45f; delayFeedback=0.35f
                wahOn=true; wahPosition=0.7f; wahResonance=0.6f; wahLevel=0.85f
                reverbOn=true; reverbMix=0.3f; reverbDecay=0.4f
                ampOn=true; ampGain=0.4f; ampResponse=0.6f
            }
        }
    }
}
