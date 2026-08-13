package com.gitaradistortion

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class PedalBoardFragment : Fragment() {
    private var cabinetVisible = false
    private lateinit var mainRow: LinearLayout
    private lateinit var activeArea: LinearLayout
    private lateinit var cabinetArea: LinearLayout
    private lateinit var cabinetInner: LinearLayout
    private lateinit var pedalRowsContainer: LinearLayout
    private lateinit var prefs: SharedPreferences
    private var startX = 0f

    private val activePedals = mutableSetOf<String>()

    private val allPedals = listOf(
        Triple("noisegate", "🚧 NOISE GATE", 0xFF44DD88.toInt()),
        Triple("volume", "🔊 VOLUME", 0xFF4488FF.toInt()),
        Triple("gain", "⚡ GAIN", 0xFFFFDD22.toInt()),
        Triple("overdrive", "🟠 OVERDRIVE", 0xFFFFAA22.toInt()),
        Triple("distortion", "🔴 DISTORTION", 0xFFFF4422.toInt()),
        Triple("chorus", "🫧 CHORUS", 0xFF66AAFF.toInt()),
        Triple("delay", "⏱️ DELAY", 0xFFAA88FF.toInt()),
        Triple("wah", "🎵 WAH-WAH", 0xFFFF66AA.toInt()),
        Triple("reverb", "🌊 REVERB", 0xFF44CCDD.toInt()),
        Triple("amp", "🔊 AMP", 0xFFFFBB44.toInt())
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val ctx = inflater.context
        prefs = ctx.getSharedPreferences("GitaraPresets", Context.MODE_PRIVATE)

        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.setPadding(6,4,6,4)

        val presetBar = LinearLayout(ctx)
        presetBar.orientation = LinearLayout.HORIZONTAL
        presetBar.gravity = Gravity.CENTER
        presetBar.setBackgroundColor(0xFF1A1A2E.toInt())
        presetBar.setPadding(4,4,4,4)

        val presets = listOf("Clean","Blues","Rock","Metal")
        presets.forEach { name ->
            val btn = Button(ctx)
            btn.text = name
            btn.setTextColor(Color.WHITE)
            btn.setBackgroundColor(0xFF2A2A4E.toInt())
            btn.textSize = 11f
            btn.setPadding(8,2,8,2)
            btn.setOnClickListener {
                AudioMixer.applyPreset(name)
                Toast.makeText(ctx,"✅ PRESET: $name",Toast.LENGTH_SHORT).show()
                updatePedalConnections()
            }
            presetBar.addView(btn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        val saveBtn = Button(ctx)
        saveBtn.text = "💾 SAVE"
        saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(0xFF226644.toInt())
        saveBtn.textSize = 11f
        saveBtn.setPadding(6,2,6,2)
        saveBtn.setOnClickListener {
            prefs.edit().apply {
                putFloat("masterLevel", AudioMixer.masterLevel)
                putBoolean("ngOn",AudioMixer.noiseGateOn)
                putFloat("ngTh",AudioMixer.noiseGateThreshold)
                putFloat("ngRel",AudioMixer.noiseGateRelease)
                putBoolean("volOn",AudioMixer.volumeOn)
                putFloat("volLvl",AudioMixer.volumeLevel)
                putBoolean("gainOn",AudioMixer.gainOn)
                putFloat("gainAmt",AudioMixer.gainAmount)
                putBoolean("odOn",AudioMixer.overdriveOn)
                putFloat("odDr",AudioMixer.overdriveDrive)
                putFloat("odLvl",AudioMixer.overdriveLevel)
                putBoolean("distOn",AudioMixer.distortionOn)
                putFloat("distGn",AudioMixer.distortionGain)
                putFloat("distTn",AudioMixer.distortionTone)
                putBoolean("chorOn",AudioMixer.chorusOn)
                putFloat("chorSp",AudioMixer.chorusSpeed)
                putFloat("chorDp",AudioMixer.chorusDepth)
                putBoolean("delOn",AudioMixer.delayOn)
                putFloat("delTm",AudioMixer.delayTime)
                putFloat("delFb",AudioMixer.delayFeedback)
                putBoolean("wahOn",AudioMixer.wahOn)
                putFloat("wahPos",AudioMixer.wahPosition)
                putFloat("wahQ",AudioMixer.wahResonance)
                putFloat("wahLvl",AudioMixer.wahLevel)
                putBoolean("revOn",AudioMixer.reverbOn)
                putFloat("revMx",AudioMixer.reverbMix)
                putFloat("revDc",AudioMixer.reverbDecay)
                putBoolean("ampOn",AudioMixer.ampOn)
                putFloat("ampGn",AudioMixer.ampGain)
                putFloat("ampRp",AudioMixer.ampResponse)
                putString("activePedals",activePedals.joinToString(","))
            }.apply()
            Toast.makeText(ctx,"✅ NAISAVE ANG PRESET!",Toast.LENGTH_SHORT).show()
        }
        presetBar.addView(saveBtn)
        root.addView(presetBar)

        val masterBar = LinearLayout(ctx)
        masterBar.orientation = LinearLayout.HORIZONTAL
        masterBar.gravity = Gravity.CENTER
        masterBar.setBackgroundColor(0xFF220000.toInt())
        masterBar.setPadding(8,4,8,4)
        val masterLabel = TextView(ctx)
        masterLabel.text = "🔴 MASTER"
        masterLabel.textSize = 14f
        masterLabel.setTextColor(0xFFFF4444.toInt())
        masterLabel.setPadding(0,0,12,0)
        masterBar.addView(masterLabel)
        val masterBtn = Button(ctx)
        masterBtn.text = "🟢 ON"
        masterBtn.setTextColor(Color.WHITE)
        masterBtn.setBackgroundColor(0xFF228833.toInt())
        masterBtn.textSize = 12f
        masterBtn.setPadding(16,2,16,2)
        masterBtn.minWidth = 100
        masterBtn.setOnClickListener {
            val isOn = masterBtn.text == "🟢 ON"
            MainActivity.setMasterEnabledGlobal(!isOn)
            if(isOn) {
                masterBtn.text = "⚪ OFF"
                masterBtn.setBackgroundColor(0xFF882222.toInt())
                masterLabel.setTextColor(0xFF888888.toInt())
            } else {
                masterBtn.text = "🟢 ON"
                masterBtn.setBackgroundColor(0xFF228833.toInt())
                masterLabel.setTextColor(0xFFFF4444.toInt())
            }
        }
        masterBar.addView(masterBtn)
        root.addView(masterBar)

        val title = TextView(ctx)
        title.text = "🎛️ PEDAL BOARD — HILAHIN PAKALIWA PARA SA CABINET"
        title.textSize = 11f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0,4,0,4)
        root.addView(title)

        mainRow = LinearLayout(ctx)
        mainRow.orientation = LinearLayout.HORIZONTAL
        mainRow.setOnTouchListener { _, e ->
            when(e.action) {
                MotionEvent.ACTION_DOWN -> startX = e.rawX
                MotionEvent.ACTION_MOVE -> {
                    val d = startX - e.rawX
                    if(d > 80 && !cabinetVisible) showCabinet()
                    if(d < -80 && cabinetVisible) hideCabinet()
                }
            }
            true
        }

        activeArea = LinearLayout(ctx)
        activeArea.orientation = LinearLayout.VERTICAL
        activeArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3f)
        activeArea.setPadding(2,2,6,2)
        activeArea.setBackgroundColor(0xFF1A1A1A.toInt())
        val emptyHint = TextView(ctx)
        emptyHint.text = "📦 BUKAS ANG CABINET → PUMILI NG PEDAL"
        emptyHint.setTextColor(0xFF666666.toInt())
        emptyHint.gravity = Gravity.CENTER
        emptyHint.textSize = 12f
        activeArea.addView(emptyHint)
        pedalRowsContainer = LinearLayout(ctx)
        pedalRowsContainer.orientation = LinearLayout.VERTICAL
        activeArea.addView(pedalRowsContainer)
        mainRow.addView(activeArea)

        cabinetArea = LinearLayout(ctx)
        cabinetArea.orientation = LinearLayout.VERTICAL
        cabinetArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0f)
        cabinetArea.setBackgroundColor(0xFF2A2A2A.toInt())
        cabinetArea.setPadding(6,6,6,6)
        cabinetArea.visibility = View.GONE
        val cabTitle = TextView(ctx)
        cabTitle.text = "📦 CABINET — PILIIN ANG PEDAL"
        cabTitle.textSize = 11f
        cabTitle.setTextColor(0xFFCCCC66.toInt())
        cabTitle.gravity = Gravity.CENTER
        cabTitle.setPadding(0,4,0,6)
        cabinetArea.addView(cabTitle)
        val cabScroll = ScrollView(ctx)
        cabinetInner = LinearLayout(ctx)
        cabinetInner.orientation = LinearLayout.VERTICAL
        allPedals.forEach { (id, label, color) ->
            val tv = TextView(ctx)
            tv.text = label
            tv.setTextColor(color)
            tv.setBackgroundColor(0xFF333333.toInt())
            tv.setPadding(12,12,12,12)
            tv.textSize = 13f
            tv.gravity = Gravity.CENTER
            tv.tag = id
            tv.setOnClickListener { togglePedal(id, tv, color) }
            cabinetInner.addView(tv)
        }
        cabScroll.addView(cabinetInner)
        cabinetArea.addView(cabScroll)
        mainRow.addView(cabinetArea)
        root.addView(mainRow)
        return root
    }

    private fun togglePedal(pedalId:String, btn:TextView, color:Int) {
        if(activePedals.contains(pedalId)) {
            activePedals.remove(pedalId)
            btn.setTextColor(color)
            btn.setBackgroundColor(0xFF333333.toInt())
        } else {
            if(activePedals.size >= 6) {
                Toast.makeText(context,"❌ HANGGANG 6 PEDAL LANG!",Toast.LENGTH_SHORT).show()
                return
            }
            activePedals.add(pedalId)
            btn.setTextColor(0xFF777777.toInt())
            btn.setBackgroundColor(0xFF111111.toInt())
        }
        updateLayout()
        updatePedalConnections()
    }

    private fun updateLayout() {
        val ctx = context ?: return
        pedalRowsContainer.removeAllViews()
        if(activePedals.isEmpty()) return

        val list = activePedals.toList()
        for(i in list.indices step 3) {
            val row = LinearLayout(ctx)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER
            row.setPadding(2,4,2,4)
            for(j in i until minOf(i+3,list.size)) {
                val v = createPedalView(list[j], ctx)
                row.addView(v, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
            pedalRowsContainer.addView(row)
        }
    }

    private fun createPedalView(pid:String, ctx:Context): View = when(pid) {
        "noisegate" -> NoiseGatePedal().makeView(ctx)
        "volume" -> VolumePedal().makeView(ctx)
        "gain" -> GainPedal().makeView(ctx)
        "overdrive" -> OverdrivePedal().makeView(ctx)
        "distortion" -> DistortionPedal().makeView(ctx)
        "chorus" -> ChorusPedal().makeView(ctx)
        "delay" -> DelayPedal().makeView(ctx)
        "wah" -> WahPedal().makeView(ctx)
        "reverb" -> ReverbPedal().makeView(ctx)
        "amp" -> AmpPedal().makeView(ctx)
        else -> TextView(ctx)
    }

    private fun updatePedalConnections() {
        activePedals.forEach { pid ->
            when(pid) {
                "noisegate" -> AudioMixer.setNoiseGate(true, AudioMixer.noiseGateThreshold, AudioMixer.noiseGateRelease)
                "volume" -> AudioMixer.setVolume(true, AudioMixer.volumeLevel)
                "gain" -> AudioMixer.setGain(true, AudioMixer.gainAmount)
                "overdrive" -> AudioMixer.setOverdrive(true, AudioMixer.overdriveDrive, AudioMixer.overdriveLevel)
                "distortion" -> AudioMixer.setDistortion(true, AudioMixer.distortionGain, AudioMixer.distortionTone)
                "chorus" -> AudioMixer.setChorus(true, AudioMixer.chorusSpeed, AudioMixer.chorusDepth)
                "delay" -> AudioMixer.setDelay(AudioMixer.delayTime, AudioMixer.delayFeedback)
                "wah" -> AudioMixer.setWah(true, AudioMixer.wahPosition, AudioMixer.wahResonance, AudioMixer.wahLevel)
                "reverb" -> AudioMixer.setReverb(true, AudioMixer.reverbMix, AudioMixer.reverbDecay)
                "amp" -> AudioMixer.setAmp(true, AudioMixer.ampGain, AudioMixer.ampResponse)
            }
        }
    }

    private fun showCabinet() {
        cabinetVisible = true
        activeArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f)
        cabinetArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f)
        cabinetArea.visibility = View.VISIBLE
    }
    private fun hideCabinet() {
        cabinetVisible = false
        activeArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3f)
        cabinetArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0f)
        cabinetArea.visibility = View.GONE
    }
}
