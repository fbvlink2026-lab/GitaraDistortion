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
            PedalPreset("Clean", -0x3399BB, isOn = false,
                ng=0.1f, tone=0.3f, gain=0.5f, od=0.0f, dist=0.0f, fuzz=0.0f,
                chorus=0.0f, flanger=0.0f, phaser=0.0f, trem=0.0f, vib=0.0f,
                delay=0.0f, reverb=0.2f, wah=0.0f, amp=0.5f,
                bass=0.5f, mid=0.5f, treble=0.5f, master=0.7f),
            PedalPreset("Blues", -0x99BB3D, isOn = false,
                ng=0.2f, tone=0.5f, gain=0.6f, od=0.3f, dist=0.1f, fuzz=0.0f,
                chorus=0.1f, flanger=0.0f, phaser=0.0f, trem=0.1f, vib=0.0f,
                delay=0.2f, reverb=0.3f, wah=0.2f, amp=0.5f,
                bass=0.6f, mid=0.5f, treble=0.4f, master=0.8f),
            PedalPreset("Rock", -0x66DDDD, isOn = false,
                ng=0.3f, tone=0.6f, gain=0.8f, od=0.5f, dist=0.5f, fuzz=0.2f,
                chorus=0.2f, flanger=0.1f, phaser=0.1f, trem=0.2f, vib=0.1f,
                delay=0.3f, reverb=0.2f, wah=0.3f, amp=0.6f,
                bass=0.6f, mid=0.5f, treble=0.5f, master=0.85f),
            PedalPreset("Metal", -0xDDDDDD, isOn = false,
                ng=0.5f, tone=0.7f, gain=0.95f, od=0.8f, dist=0.9f, fuzz=0.5f,
                chorus=0.3f, flanger=0.2f, phaser=0.2f, trem=0.3f, vib=0.2f,
                delay=0.4f, reverb=0.1f, wah=0.4f, amp=0.7f,
                bass=0.7f, mid=0.5f, treble=0.6f, master=0.9f)
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
                        color = o.optInt("color", -0xBB7733),
                        isOn = o.optBoolean("on", false),
                        ng = o.optDouble("ng", 0.5).toFloat(),
                        tone = o.optDouble("tone", 0.5).toFloat(),
                        gain = o.optDouble("gain", 0.5).toFloat(),
                        od = o.optDouble("od", 0.0).toFloat(),
                        dist = o.optDouble("dist", 0.5).toFloat(),
                        fuzz = o.optDouble("fuzz", 0.0).toFloat(),
                        chorus = o.optDouble("chorus", 0.0).toFloat(),
                        flanger = o.optDouble("flanger", 0.0).toFloat(),
                        phaser = o.optDouble("phaser", 0.0).toFloat(),
                        trem = o.optDouble("trem", 0.0).toFloat(),
                        vib = o.optDouble("vib", 0.0).toFloat(),
                        delay = o.optDouble("delay", 0.0).toFloat(),
                        reverb = o.optDouble("reverb", 0.0).toFloat(),
                        wah = o.optDouble("wah", 0.0).toFloat(),
                        amp = o.optDouble("amp", 0.5).toFloat(),
                        bass = o.optDouble("bass", 0.5).toFloat(),
                        mid = o.optDouble("mid", 0.5).toFloat(),
                        treble = o.optDouble("treble", 0.5).toFloat(),
                        master = o.optDouble("master", 0.5).toFloat()))
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

    private fun saveMainMixerToPreset(preset: PedalPreset) {
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

    private fun getAllPresets(): List<PedalPreset> {
        return getFixedDefaultPresets() + loadUserPresets()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        buildUI()
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
                val (label, color, getVal) = fxList[k]
                val col = LinearLayout(this)
                col.orientation = LinearLayout.VERTICAL
                col.gravity = Gravity.CENTER
                val knob = KnobView(this)
                knob.baseColor = color; knob.value = getVal()
                val pct = TextView(this)
                pct.text = "${(knob.value*100).toInt()}%"; pct.setTextColor(color); pct.textSize = 11f
                knob.onChange = { v ->
                    pct.text = "${(v*100).toInt()}%"
                    when(k) {
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
                }
                col.addView(knob, LinearLayout.LayoutParams(80,80))
                col.addView(pct)
                val lbl = TextView(this)
                lbl.text = label; lbl.setTextColor(color); lbl.textSize = 9f; lbl.gravity = Gravity.CENTER
                col.addView(lbl)
                rowLay.addView(col, LinearLayout.LayoutParams(0,-1,1f))
                knobViews.add(knob to pct)
            }
            grid.addView(rowLay)
        }
        scroll.addView(grid)
        mainPage.addView(scroll)
        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER
        bar.setBackgroundColor(-0xDDDDFF)
        bar.setPadding(8,8,8,8)
        mainPowerBtn = Button(this)
        mainPowerBtn.text = "🟢 ON"; mainPowerBtn.setTextColor(Color.WHITE)
        mainPowerBtn.setBackgroundColor(-0xDD7733); mainPowerBtn.textSize = 13f
        mainPowerBtn.setOnClickListener {
            val isOn = AudioMixer.isAllOn()
            AudioMixer.setAllOn(!isOn)
            if(isOn) {
                mainPowerBtn.text = "🔴 OFF"
                mainPowerBtn.setBackgroundColor(-0xDD3333)
            } else {
                mainPowerBtn.text = "🟢 ON"
                mainPowerBtn.setBackgroundColor(-0x33DD33)
                AudioEngine.start(this)
            }
        }
        bar.addView(mainPowerBtn)
        savePresetName = EditText(this)
        savePresetName.hint = "Pangalan Preset"
        savePresetName.setTextColor(Color.WHITE)
        savePresetName.setHintTextColor(-0x888889)
        savePresetName.setBackgroundColor(-0xDDDDDE)
        savePresetName.setPadding(8,4,8,4)
        bar.addView(savePresetName, LinearLayout.LayoutParams(0,-1,1f).apply { setMargins(12,0,12,0) })
        val saveBtn = Button(this)
        saveBtn.text = "💾 SAVE"; saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(-0xBB7733); saveBtn.textSize = 12f
        saveBtn.setOnClickListener {
            val n = savePresetName.text.toString().trim()
            val defaultNames = listOf("Clean", "Blues", "Rock", "Metal")
            if(n.isBlank()) {
                Toast.makeText(this,"❌ Ilagay ang pangalan!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(n in defaultNames) {
                Toast.makeText(this,"❌ Hindi pwedeng pangalan ng Default Preset!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userPresets = loadUserPresets()
            if(userPresets.any { it.name == n }) {
                Toast.makeText(this,"❌ May preset na ganyang pangalan!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newPreset = PedalPreset(name=n, color=pickColor(), isOn=false)
            saveMainMixerToPreset(newPreset)
            userPresets.add(newPreset)
            saveAllUserPresets(userPresets)
            Toast.makeText(this,"✅ NAISAVE: $n!\nKasalukuyang halaga ang na-save!",Toast.LENGTH_LONG).show()
            savePresetName.text.clear()
            pageContainer.removeViews(1, pageContainer.childCount-1)
            buildCabinetPages()
        }
        bar.addView(saveBtn)
        mainPage.addView(bar)
        pageContainer.addView(mainPage)
    }

    private fun buildCabinetPages() {
        val allPresets = getAllPresets()
        val w = resources.displayMetrics.widthPixels
        val PER_PAGE = 2
        for(pageIdx in 0 until allPresets.size step PER_PAGE) {
            val pagePresets = allPresets.subList(pageIdx, minOf(pageIdx+PER_PAGE, allPresets.size))
            val cabPage = LinearLayout(this)
            cabPage.orientation = LinearLayout.VERTICAL
            cabPage.layoutParams = LinearLayout.LayoutParams(w, -1)
            cabPage.setBackgroundColor(-0xE5E5E6)
            cabPage.setPadding(12,8,12,8)
            val topBar = LinearLayout(this)
            topBar.orientation = LinearLayout.HORIZONTAL
            topBar.gravity = Gravity.CENTER_VERTICAL
            topBar.setPadding(4,4,4,8)
            val backBtn = Button(this)
            backBtn.text = "⬅️ MAIN"
            backBtn.textSize = 14f
            backBtn.setTextColor(Color.WHITE)
            backBtn.setBackgroundColor(-0xDD3333)
            backBtn.setPadding(16,8,16,8)
            backBtn.setOnClickListener { goToMainPage() }
            topBar.addView(backBtn)
            val pageNum = (pageIdx / PER_PAGE) + 1
            val totalPages = ((allPresets.size + PER_PAGE - 1) / PER_PAGE)
            val title = TextView(this)
            title.text = "📦 PEDAL CABINET — PAGE $pageNum / $totalPages"
            title.textSize = 16f
            title.setTextColor(-0x0033BC)
            title.setPadding(16,0,0,0)
            topBar.addView(title, LinearLayout.LayoutParams(0,-1,1f))
            cabPage.addView(topBar)
            val hint = TextView(this)
            hint.text = "👆 SWIPE ←→ | Pindutin ON → EKSAKTONG HALAGA SA MAIN MIXER!"
            hint.textSize = 11f
            hint.setTextColor(-0x888889)
            hint.gravity = Gravity.CENTER
            hint.setPadding(0,4,0,8)
            cabPage.addView(hint)
            pagePresets.forEach { preset ->
                cabPage.addView(buildBigPedalView(preset))
            }
            pageContainer.addView(cabPage)
        }
    }

    private fun buildBigPedalView(preset: PedalPreset): LinearLayout {
        val w = resources.displayMetrics.widthPixels
        val allPresets = getAllPresets()
        val defaultCount = 4
        val pedal = LinearLayout(this)
        pedal.orientation = LinearLayout.VERTICAL
        pedal.setBackgroundColor(preset.color)
        pedal.setPadding(16,12,16,12)
        pedal.gravity = Gravity.CENTER
        pedal.layoutParams = LinearLayout.LayoutParams(w - 48, 0, 1f)
        val powerBtn = Button(this)
        powerBtn.text = if(preset.isOn) "💡 NAKA-ON" else "⚫ NAKA-OFF"
        powerBtn.textSize = 16f
        powerBtn.setTextColor(Color.WHITE)
        powerBtn.setBackgroundColor(if(preset.isOn) 0xFF22CC22.toInt() else 0xFF333333.toInt())
        powerBtn.setPadding(24,8,24,8)
        powerBtn.setOnClickListener {
            preset.isOn = !preset.isOn
            powerBtn.text = if(preset.isOn) "💡 NAKA-ON" else "⚫ NAKA-OFF"
            powerBtn.setBackgroundColor(if(preset.isOn) 0xFF22CC22.toInt() else 0xFF333333.toInt())
            if(preset.isOn) {
                loadPresetToMainMixer(preset)
                AudioMixer.setAllOn(true)
                mainPowerBtn.text = "🟢 ON"
                mainPowerBtn.setBackgroundColor(-0x33DD33)
                Toast.makeText(this,"✅ ${preset.name} — NAKA-ON!\nEksaktong halaga na-save → Main Mixer!",Toast.LENGTH_SHORT).show()
                if(!AudioEngine.isRunning()) AudioEngine.start(this)
            } else {
                val idx = allPresets.indexOfFirst { it.name == preset.name }
                if(idx >= defaultCount) {
                    val userPresets = loadUserPresets()
                    val uIdx = idx - defaultCount
                    if(uIdx < userPresets.size) {
                        saveMainMixerToPreset(userPresets[uIdx])
                        saveAllUserPresets(userPresets)
                    }
                }
                AudioMixer.setAllOn(false)
                mainPowerBtn.text = "🔴 OFF"
                mainPowerBtn.setBackgroundColor(-0xDD7733)
                Toast.makeText(this,"⚫ ${preset.name} — NAI-SAVE AT NAKA-OFF!",Toast.LENGTH_SHORT).show()
            }
        }
        pedal.addView(powerBtn)
        val name = TextView(this)
        name.text = preset.name
        name.textSize = 22f
        name.setTextColor(Color.WHITE)
        name.gravity = Gravity.CENTER
        name.setPadding(0,8,0,8)
        pedal.addView(name)
        val volRow = LinearLayout(this)
        volRow.orientation = LinearLayout.HORIZONTAL
        volRow.gravity = Gravity.CENTER_VERTICAL
        val volLabel = TextView(this)
        volLabel.text = "🎚️ VOLUME"
        volLabel.textSize = 14f
        volLabel.setTextColor(Color.WHITE)
        volLabel.setPadding(0,0,12,0)
        val volKnob = KnobView(this)
        volKnob.baseColor = 0xFFFFCC00.toInt()
        volKnob.value = preset.master
        volKnob.layoutParams = LinearLayout.LayoutParams(64,64)
        val volPct = TextView(this)
        volPct.text = "${(preset.master*100).toInt()}%"
        volPct.textSize = 14f
        volPct.setTextColor(Color.WHITE)
        volPct.setPadding(12,0,0,0)
        volKnob.onChange = { v ->
            preset.master = v
            volPct.text = "${(v*100).toInt()}%"
            val idx = allPresets.indexOfFirst { it.name == preset.name }
            if(idx >= defaultCount) {
                val userPresets = loadUserPresets()
                val uIdx = idx - defaultCount
                if(uIdx < userPresets.size) {
                    userPresets[uIdx].master = v
                    saveAllUserPresets(userPresets)
                }
            }
            if(preset.isOn) {
                AudioMixer.masterVolume = v
                updateAllKnobs()
            }
        }
        volRow.addView(volLabel)
        volRow.addView(volKnob)
        volRow.addView(volPct)
        volRow.setPadding(0,8,0,8)
        pedal.addView(volRow)
        val fxRow = LinearLayout(this)
        fxRow.orientation = LinearLayout.HORIZONTAL
        fxRow.gravity = Gravity.CENTER_VERTICAL
        val fxLabel = TextView(this)
        fxLabel.text = "🎛️ DISTORTION"
        fxLabel.textSize = 14f
        fxLabel.setTextColor(Color.WHITE)
        fxLabel.setPadding(0,0,12,0)
        val fxKnob = KnobView(this)
        fxKnob.baseColor = 0xFFFF4422.toInt()
        fxKnob.value = preset.dist
        fxKnob.layoutParams = LinearLayout.LayoutParams(64,64)
        val fxPct = TextView(this)
        fxPct.text = "${(preset.dist*100).toInt()}%"
        fxPct.textSize = 14f
        fxPct.setTextColor(Color.WHITE)
        fxPct.setPadding(12,0,0,0)
        fxKnob.onChange = { v ->
            preset.dist = v
            fxPct.text = "${(v*100).toInt()}%"
            val idx = allPresets.indexOfFirst { it.name == preset.name }
            if(idx >= defaultCount) {
                val userPresets = loadUserPresets()
                val uIdx = idx - defaultCount
                if(uIdx < userPresets.size) {
                    userPresets[uIdx].dist = v
                    saveAllUserPresets(userPresets)
                }
            }
            if(preset.isOn) {
                AudioMixer.distortion = v
                updateAllKnobs()
            }
        }
        fxRow.addView(fxLabel)
        fxRow.addView(fxKnob)
        fxRow.addView(fxPct)
        fxRow.setPadding(0,8,0,8)
        pedal.addView(fxRow)
        val ngRow = LinearLayout(this)
        ngRow.orientation = LinearLayout.HORIZONTAL
        ngRow.gravity = Gravity.CENTER_VERTICAL
        val ngLabel = TextView(this)
        ngLabel.text = "🚧 NOISE GATE"
        ngLabel.textSize = 14f
        ngLabel.setTextColor(Color.WHITE)
        ngLabel.setPadding(0,0,12,0)
        val ngKnob = KnobView(this)
        ngKnob.baseColor = 0xFF44DD88.toInt()
        ngKnob.value = preset.ng
        ngKnob.layoutParams = LinearLayout.LayoutParams(64,64)
        val ngPct = TextView(this)
        ngPct.text = "${(preset.ng*100).toInt()}%"
        ngPct.textSize = 14f
        ngPct.setTextColor(Color.WHITE)
        ngPct.setPadding(12,0,0,0)
        ngKnob.onChange = { v ->
            preset.ng = v
            ngPct.text = "${(v*100).toInt()}%"
            val idx = allPresets.indexOfFirst { it.name == preset.name }
            if(idx >= defaultCount) {
                val userPresets = loadUserPresets()
                val uIdx = idx - defaultCount
                if(uIdx < userPresets.size) {
                    userPresets[uIdx].ng = v
                    saveAllUserPresets(userPresets)
                }
            }
            if(preset.isOn) {
                AudioMixer.noiseGate = v
                updateAllKnobs()
            }
        }
        ngRow.addView(ngLabel)
        ngRow.addView(ngKnob)
        ngRow.addView(ngPct)
        ngRow.setPadding(0,8,0,8)
        pedal.addView(ngRow)
        return pedal
    }

    private fun goToCabinetPage() { currentPage=1; cabinetPageIndex=0; pageContainer.scrollTo(resources.displayMetrics.widthPixels,0) }
    private fun goNextCabinetPage() {
        val w = resources.displayMetrics.widthPixels
        val maxPage = pageContainer.childCount - 1
        if(cabinetPageIndex + 1 < maxPage) {
            cabinetPageIndex += 1
            pageContainer.scrollTo(w*(1+cabinetPageIndex),0)
        } else {
            Toast.makeText(this,"✅ HULING PAGE NA!",Toast.LENGTH_SHORT).show()
        }
    }
    private fun goPrevCabinetPage() {
        if(cabinetPageIndex > 0) {
            cabinetPageIndex -= 1
            pageContainer.scrollTo(resources.displayMetrics.widthPixels*(1+cabinetPageIndex),0)
        } else if(currentPage > 0) {
            goToMainPage()
        } else {
            Toast.makeText(this,"✅ UNAANG PAGE NA!",Toast.LENGTH_SHORT).show()
        }
    }
    private fun goToMainPage() { currentPage=0; cabinetPageIndex=0; pageContainer.scrollTo(0,0) }

    private val colors = listOf(-0xBB7734, -0x33BB78, -0x7733BB, -0xBB3377, -0x33BBBB, -0xBBBB33)
    private var colorIdx = 0
    private fun pickColor(): Int {
        return colors[colorIdx++ % colors.size]
    }

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"✅ Handa na! Swipe pakaliwa → Cabinet!",Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),123)
        }
    }
    override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray) {
        super.onRequestPermissionsResult(r,p,g)
        if(r == 123 && g.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"✅ Pahintulot natanggap!",Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        AudioEngine.stop()
    }
}

class PedalPreset(
    val name: String,
    val color: Int,
    var isOn: Boolean = false,
    var ng: Float = 0.5f,
    var tone: Float = 0.5f,
    var gain: Float = 0.5f,
    var od: Float = 0.0f,
    var dist: Float = 0.5f,
    var fuzz: Float = 0.0f,
    var chorus: Float = 0.0f,
    var flanger: Float = 0.0f,
    var phaser: Float = 0.0f,
    var trem: Float = 0.0f,
    var vib: Float = 0.0f,
    var delay: Float = 0.0f,
    var reverb: Float = 0.0f,
    var wah: Float = 0.0f,
    var amp: Float = 0.5f,
    var bass: Float = 0.5f,
    var mid: Float = 0.5f,
    var treble: Float = 0.5f,
    var master: Float = 0.5f
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("color", color)
            put("on", isOn)
            put("ng", ng)
            put("tone", tone)
            put("gain", gain)
            put("od", od)
            put("dist", dist)
            put("fuzz", fuzz)
            put("chorus", chorus)
            put("flanger", flanger)
            put("phaser", phaser)
            put("trem", trem)
            put("vib", vib)
            put("delay", delay)
            put("reverb", reverb)
            put("wah", wah)
            put("amp", amp)
            put("bass", bass)
            put("mid", mid)
            put("treble", treble)
            put("master", master)
        }
    }
}
