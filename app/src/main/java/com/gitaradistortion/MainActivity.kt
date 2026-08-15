package com.gitaradistortion

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private var currentPage = 0
    private var cabinetPageIndex = 0
    private var startX = 0f
    private var isSwiping = false
    private lateinit var mainPage: LinearLayout
    private lateinit var pageContainer: LinearLayout
    private lateinit var savePresetName: EditText
    private lateinit var mainPowerBtn: Button
    private val knobViews = mutableListOf<Pair<KnobView, TextView>>()
    private val prefs by lazy { getSharedPreferences("GitaraPresets", Context.MODE_PRIVATE) }

    private var activePresetName: String? = null

    private val pedalColors = listOf(
        "🔴 PULA" to 0xFFFF4422.toInt(),
        "🟠 KULAY-ORANGE" to 0xFFFF9900.toInt(),
        "🟡 DILAW" to 0xFFFFCC00.toInt(),
        "🟢 BERDE" to 0xFF22CC44.toInt(),
        "🔵 ASUL" to 0xFF2266FF.toInt(),
        "🟣 LILA" to 0xFF9944FF.toInt(),
        "⚪ PUTI" to 0xFFE0E0E0.toInt(),
        "⚪ PILAK" to 0xFFC0C0C0.toInt(),
        "🟤 KAYUMANGGI" to 0xFF885522.toInt(),
        "⚫ ITIM" to 0xFF333333.toInt()
    )

    private val fxList = listOf(
        Triple("🚧 NOISE GATE", -0x3BBB78, { AudioMixer.noiseGate }),
        Triple("🎵 TONE", -0x0033BC, { AudioMixer.tone }),
        Triple("⚡ GAIN", -0x0022DD, { AudioMixer.gain }),
        Triple("🟠 OVERDRIVE", -0x0066DD, { AudioMixer.overdrive }),
        Triple("🔴 DISTORTION", -0x00BBBD, { AudioMixer.distortion }),
        Triple("⚫ FUZZ", -0x99BBDD, { AudioMixer.fuzz }),
        Triple("🫧 CHORUS", -0x995500, { AudioMixer.chorus }),
        Triple("✨ FLANGER", -0xBB1155, { AudioMixer.flanger }),
        Triple("🌀 PHASER", -0x559900, { AudioMixer.phaser }),
        Triple("📳 TREMOLO", -0x009977, { AudioMixer.tremolo }),
        Triple("🎶 VIBRATO", -0x773300, { AudioMixer.vibrato }),
        Triple("⏱️ DELAY", -0x777700, { AudioMixer.delay }),
        Triple("🌊 REVERB", -0x333322, { AudioMixer.reverb }),
        Triple("🎵 WAH", -0x009955, { AudioMixer.wah }),
        Triple("🔊 AMP", -0x0044BB, { AudioMixer.ampType }),
        Triple("🔵 BASS", -0xBB7700, { AudioMixer.bass }),
        Triple("🟡 MID", -0x0033FF, { AudioMixer.mid }),
        Triple("🟢 TREBLE", -0x3BBB78, { AudioMixer.treble }),
        Triple("🎚️ MASTER", -0x000001, { AudioMixer.masterVolume })
    )

    private fun getFixedDefaultPresets(): List<PedalPreset> {
        return listOf(
            PedalPreset("Clean", 0xFF22AA44.toInt(), isOn = false,
                ng=0.0f, tone=0.0f, gain=0.0f, od=0.0f, dist=0.0f, fuzz=0.0f,
                chorus=0.0f, flanger=0.0f, phaser=0.0f, trem=0.0f, vib=0.0f,
                delay=0.0f, reverb=0.0f, wah=0.0f, amp=0.0f,
                bass=0.0f, mid=0.0f, treble=0.0f, master=0.0f),
            PedalPreset("Blues", 0xFFFF8800.toInt(), isOn = false,
                ng=0.0f, tone=0.0f, gain=0.0f, od=0.0f, dist=0.0f, fuzz=0.0f,
                chorus=0.0f, flanger=0.0f, phaser=0.0f, trem=0.0f, vib=0.0f,
                delay=0.0f, reverb=0.0f, wah=0.0f, amp=0.0f,
                bass=0.0f, mid=0.0f, treble=0.0f, master=0.0f),
            PedalPreset("Rock", 0xFFFF4422.toInt(), isOn = false,
                ng=0.0f, tone=0.0f, gain=0.0f, od=0.0f, dist=0.0f, fuzz=0.0f,
                chorus=0.0f, flanger=0.0f, phaser=0.0f, trem=0.0f, vib=0.0f,
                delay=0.0f, reverb=0.0f, wah=0.0f, amp=0.0f,
                bass=0.0f, mid=0.0f, treble=0.0f, master=0.0f),
            PedalPreset("Metal", 0xFF222222.toInt(), isOn = false,
                ng=0.0f, tone=0.0f, gain=0.0f, od=0.0f, dist=0.0f, fuzz=0.0f,
                chorus=0.0f, flanger=0.0f, phaser=0.0f, trem=0.0f, vib=0.0f,
                delay=0.0f, reverb=0.0f, wah=0.0f, amp=0.0f,
                bass=0.0f, mid=0.0f, treble=0.0f, master=0.0f)
        )
    }

    private fun loadUserPresets(): MutableList<PedalPreset> {
        val list = mutableListOf<PedalPreset>()
        try {
            val json = prefs.getString("user_pedal_presets", "[]")
            val arr = JSONArray(json)
            val defaultNames = listOf("Clean", "Blues", "Rock", "Metal")
            for(i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val name = o.getString("name")
                if(name !in defaultNames) {
                    list.add(PedalPreset(
                        name = name,
                        color = o.optInt("color", 0xFFFF8800.toInt()),
                        isOn = o.optBoolean("isOn", false),
                        ng = o.optDouble("ng", 0.0).toFloat(),
                        tone = o.optDouble("tone", 0.0).toFloat(),
                        gain = o.optDouble("gain", 0.0).toFloat(),
                        od = o.optDouble("od", 0.0).toFloat(),
                        dist = o.optDouble("dist", 0.0).toFloat(),
                        fuzz = o.optDouble("fuzz", 0.0).toFloat(),
                        chorus = o.optDouble("chorus", 0.0).toFloat(),
                        flanger = o.optDouble("flanger", 0.0).toFloat(),
                        phaser = o.optDouble("phaser", 0.0).toFloat(),
                        trem = o.optDouble("trem", 0.0).toFloat(),
                        vib = o.optDouble("vib", 0.0).toFloat(),
                        delay = o.optDouble("delay", 0.0).toFloat(),
                        reverb = o.optDouble("reverb", 0.0).toFloat(),
                        wah = o.optDouble("wah", 0.0).toFloat(),
                        amp = o.optDouble("amp", 0.0).toFloat(),
                        bass = o.optDouble("bass", 0.0).toFloat(),
                        mid = o.optDouble("mid", 0.0).toFloat(),
                        treble = o.optDouble("treble", 0.0).toFloat(),
                        master = o.optDouble("master", 0.0).toFloat()))
                }
            }
        } catch(_:Exception) {}
        return list
    }

    private fun saveAllUserPresets(userPresets: List<PedalPreset>) {
        val arr = JSONArray()
        userPresets.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("user_pedal_presets", arr.toString()).apply()
    }

    private fun saveCurrentStateToActivePreset() {
        val name = activePresetName ?: return
        val allPresets = getAllPresets()
        val idx = allPresets.indexOfFirst { it.name == name }
        if(idx < 4) return

        val preset = allPresets[idx]
        preset.ng = AudioMixer.noiseGate
        preset.tone = AudioMixer.tone
        preset.gain = AudioMixer.gain
        preset.od = AudioMixer.overdrive
        preset.dist = AudioMixer.distortion
        preset.fuzz = AudioMixer.fuzz
        preset.chorus = AudioMixer.chorus
        preset.flanger = AudioMixer.flanger
        preset.phaser = AudioMixer.phaser
        preset.trem = AudioMixer.tremolo
        preset.vib = AudioMixer.vibrato
        preset.delay = AudioMixer.delay
        preset.reverb = AudioMixer.reverb
        preset.wah = AudioMixer.wah
        preset.amp = AudioMixer.ampType
        preset.bass = AudioMixer.bass
        preset.mid = AudioMixer.mid
        preset.treble = AudioMixer.treble
        preset.master = AudioMixer.masterVolume

        val userPresets = loadUserPresets()
        userPresets[idx - 4] = preset
        saveAllUserPresets(userPresets)
    }

    private fun loadPresetToMainMixer(preset: PedalPreset) {
        AudioMixer.noiseGate = preset.ng
        AudioMixer.tone = preset.tone
        AudioMixer.gain = preset.gain
        AudioMixer.overdrive = preset.od
        AudioMixer.distortion = preset.dist
        AudioMixer.fuzz = preset.fuzz
        AudioMixer.chorus = preset.chorus
        AudioMixer.flanger = preset.flanger
        AudioMixer.phaser = preset.phaser
        AudioMixer.tremolo = preset.trem
        AudioMixer.vibrato = preset.vib
        AudioMixer.delay = preset.delay
        AudioMixer.reverb = preset.reverb
        AudioMixer.wah = preset.wah
        AudioMixer.ampType = preset.amp
        AudioMixer.bass = preset.bass
        AudioMixer.mid = preset.mid
        AudioMixer.treble = preset.treble
        AudioMixer.masterVolume = preset.master
        updateAllKnobs()
    }

    private fun zeroAllKnobs() {
        AudioMixer.noiseGate = 0.0f
        AudioMixer.tone = 0.0f
        AudioMixer.gain = 0.0f
        AudioMixer.overdrive = 0.0f
        AudioMixer.distortion = 0.0f
        AudioMixer.fuzz = 0.0f
        AudioMixer.chorus = 0.0f
        AudioMixer.flanger = 0.0f
        AudioMixer.phaser = 0.0f
        AudioMixer.tremolo = 0.0f
        AudioMixer.vibrato = 0.0f
        AudioMixer.delay = 0.0f
        AudioMixer.reverb = 0.0f
        AudioMixer.wah = 0.0f
        AudioMixer.ampType = 0.0f
        AudioMixer.bass = 0.0f
        AudioMixer.mid = 0.0f
        AudioMixer.treble = 0.0f
        AudioMixer.masterVolume = 0.0f
        updateAllKnobs()
    }

    private fun updateAllKnobs() {
        val vals = floatArrayOf(
            AudioMixer.noiseGate, AudioMixer.tone, AudioMixer.gain,
            AudioMixer.overdrive, AudioMixer.distortion, AudioMixer.fuzz,
            AudioMixer.chorus, AudioMixer.flanger, AudioMixer.phaser,
            AudioMixer.tremolo, AudioMixer.vibrato, AudioMixer.delay,
            AudioMixer.reverb, AudioMixer.wah, AudioMixer.ampType,
            AudioMixer.bass, AudioMixer.mid, AudioMixer.treble,
            AudioMixer.masterVolume)
        for(i in knobViews.indices) {
            val (knob, pct) = knobViews[i]
            knob.value = vals[i]
            pct.text = "${(vals[i]*100).toInt()}%"
        }
    }

    private fun getAllPresets(): MutableList<PedalPreset> {
        return (getFixedDefaultPresets() + loadUserPresets()).toMutableList()
    }

    private fun turnOffAllPresetsAndZeroMain() {
        saveCurrentStateToActivePreset()
        val allPresets = getAllPresets()
        allPresets.forEach { it.isOn = false }
        activePresetName = null
        zeroAllKnobs()
        updateMainPowerUI(false)
        refreshAllPresetButtons()
    }

    private fun updateMainPowerUI(isOn: Boolean) {
        if(isOn) {
            AudioMixer.setAllOn(true)
            mainPowerBtn.text = "🟢 ON"
            mainPowerBtn.setBackgroundColor(0xFF22EE22.toInt())
            if(!AudioEngine.isRunning()) AudioEngine.start(this@MainActivity)
        } else {
            AudioMixer.setAllOn(false)
            mainPowerBtn.text = "🔴 OFF"
            mainPowerBtn.setBackgroundColor(-0xDD7733)
            AudioEngine.stop()
        }
    }

    private fun saveLastState() {
        val allPresets = getAllPresets()
        val active = allPresets.find { it.isOn }
        if(active != null) {
            prefs.edit().putString("last_active_preset", active.name).apply()
        } else {
            prefs.edit().remove("last_active_preset").apply()
        }
    }

    private fun restoreLastState() {
        val lastName = prefs.getString("last_active_preset", null) ?: return
        val allPresets = getAllPresets()
        val preset = allPresets.find { it.name == lastName } ?: return
        preset.isOn = true
        activePresetName = preset.name
        loadPresetToMainMixer(preset)
        updateMainPowerUI(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        buildUI()
        restoreLastState()
    }

    override fun onPause() {
        super.onPause()
        saveLastState()
        saveCurrentStateToActivePreset()
    }

    private fun buildUI() {
        val root = findViewById<LinearLayout>(R.id.rootLayout)
        root.setBackgroundColor(-0xEDF0EF)
        pageContainer = LinearLayout(this)
        pageContainer.orientation = LinearLayout.HORIZONTAL
        pageContainer.layoutParams = LinearLayout.LayoutParams(-1, -1)
        buildMainPage()
        buildCabinetPages()
        root.addView(pageContainer)
        pageContainer.setOnTouchListener { _, e ->
            when(e.action) {
                MotionEvent.ACTION_DOWN -> { startX = e.rawX; isSwiping = false }
                MotionEvent.ACTION_UP -> {
                    val d = startX - e.rawX
                    val w = resources.displayMetrics.widthPixels
                    val MIN_SWIPE = w * 0.25f
                    if(!isSwiping && abs(d) > MIN_SWIPE) {
                        isSwiping = true
                        if(d > 0) { if(currentPage == 0) goToCabinetPage() else goNextCabinetPage() }
                        else goPrevCabinetPage()
                    }
                }
            }
            true
        }
    }

    private fun buildMainPage() {
        val w = resources.displayMetrics.widthPixels
        mainPage = LinearLayout(this)
        mainPage.orientation = LinearLayout.VERTICAL
        mainPage.layoutParams = LinearLayout.LayoutParams(w, -1)
        mainPage.setPadding(8,8,8,8)

        val t = TextView(this)
        t.text = "🎛️ MAIN MIXER PANEL"
        t.textSize = 20f; t.setTextColor(-0x0033BC)
        t.gravity = Gravity.CENTER; t.setPadding(0,8,0,4)
        mainPage.addView(t)

        val h = TextView(this)
        h.text = "👉 SWIPE PAKALIWA → CABINET PEDALS"
        h.textSize = 12f; h.setTextColor(-0x777778)
        h.gravity = Gravity.CENTER; h.setPadding(0,0,0,8)
        mainPage.addView(h)

        val scroll = ScrollView(this)
        val grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL

        for(row in fxList.indices step 3) {
            val rowLay = LinearLayout(this)
            rowLay.orientation = LinearLayout.HORIZONTAL
            rowLay.gravity = Gravity.CENTER
            for(k in row until minOf(row+3, fxList.size)) {
                val (label, color, _) = fxList[k]
                val col = LinearLayout(this)
                col.orientation = LinearLayout.VERTICAL
                col.gravity = Gravity.CENTER

                val knob = KnobView(this)
                knob.baseColor = color
                knob.value = fxList[k].third()
                val label = TextView(this)
                label.text = fxList[k].first
                label.setTextColor(Color.parseColor("#222222"))
                label.textSize = 12f
                label.gravity = Gravity.CENTER
                val pct = TextView(this)
                pct.text = "${(knob.value*100).toInt()}%"
                pct.setTextColor(Color.BLACK)
                pct.textSize = 11f

                val idx = k
                knob.onChange = { v ->
                    pct.text = "${(v*100).toInt()}%"
                    when(idx) {
                        0 -> AudioMixer.noiseGate = v
                        1 -> AudioMixer.tone = v
                        2 -> AudioMixer.gain = v
                        3 -> AudioMixer.overdrive = v
                        4 -> AudioMixer.distortion = v
                        5 -> AudioMixer.fuzz = v
                        6 -> AudioMixer.chorus = v
                        7 -> AudioMixer.flanger = v
                        8 -> AudioMixer.phaser = v
                        9 -> AudioMixer.tremolo = v
                        10 -> AudioMixer.vibrato = v
                        11 -> AudioMixer.delay = v
                        12 -> AudioMixer.reverb = v
                        13 -> AudioMixer.wah = v
                        14 -> AudioMixer.ampType = v
                        15 -> AudioMixer.bass = v
                        16 -> AudioMixer.mid = v
                        17 -> AudioMixer.treble = v
                        18 -> AudioMixer.masterVolume = v
                    }
                    saveCurrentStateToActivePreset()
                }

