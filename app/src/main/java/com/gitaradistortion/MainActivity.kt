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
        "⚫ ITIM" to 0xFF1A1A1A.toInt()
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
                ng=0.1f, tone=0.3f, gain=0.5f, od=0.0f, dist=0.0f, fuzz=0.0f,
                chorus=0.0f, flanger=0.0f, phaser=0.0f, trem=0.0f, vib=0.0f,
                delay=0.0f, reverb=0.2f, wah=0.0f, amp=0.5f,
                bass=0.5f, mid=0.5f, treble=0.5f, master=0.7f),
            PedalPreset("Blues", 0xFFFF8800.toInt(), isOn = false,
                ng=0.2f, tone=0.5f, gain=0.6f, od=0.3f, dist=0.1f, fuzz=0.0f,
                chorus=0.1f, flanger=0.0f, phaser=0.0f, trem=0.1f, vib=0.0f,
                delay=0.2f, reverb=0.3f, wah=0.2f, amp=0.5f,
                bass=0.6f, mid=0.5f, treble=0.4f, master=0.8f),
            PedalPreset("Rock", 0xFFFF4422.toInt(), isOn = false,
                ng=0.3f, tone=0.6f, gain=0.8f, od=0.5f, dist=0.5f, fuzz=0.2f,
                chorus=0.2f, flanger=0.1f, phaser=0.1f, trem=0.2f, vib=0.1f,
                delay=0.3f, reverb=0.2f, wah=0.3f, amp=0.6f,
                bass=0.6f, mid=0.5f, treble=0.5f, master=0.85f),
            PedalPreset("Metal", 0xFF222222.toInt(), isOn = false,
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
                        color = o.optInt("color", 0xFFFF8800.toInt()),
                        isOn = false,
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

    private fun getAllPresets(): MutableList<PedalPreset> {
        return (getFixedDefaultPresets() + loadUserPresets()).toMutableList()
    }

    private fun refreshAllPresetButtons() {
        pageContainer.removeViews(1, pageContainer.childCount-1)
        buildCabinetPages()
    }

    private fun forceMainPowerOn() {
        AudioMixer.setAllOn(true)
        mainPowerBtn.text = "🟢 ON"
        mainPowerBtn.setBackgroundColor(0xFF22EE22.toInt())
        if(!AudioEngine.isRunning()) AudioEngine.start(this)
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
                    val activePreset = getAllPresets().find { it.name == activePresetName && it.isOn }
                    if(activePreset != null && activePreset.name !in listOf("Clean","Blues","Rock","Metal")) {
                        saveMainMixerToPreset(activePreset)
                        val userPresets = loadUserPresets()
                        val idx = getAllPresets().indexOfFirst { it.name == activePresetName } - 4
                        if(idx >= 0 && idx < userPresets.size) {
                            userPresets[idx] = activePreset
                            saveAllUserPresets(userPresets)
                        }
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
        mainPowerBtn.text = "🔴 OFF"; mainPowerBtn.setTextColor(Color.WHITE)
        mainPowerBtn.setBackgroundColor(-0xDD7733); mainPowerBtn.textSize = 13f
        mainPowerBtn.setOnClickListener {
            val isOn = AudioMixer.isAllOn()
            if(isOn) {
                AudioMixer.setAllOn(false)
                mainPowerBtn.text = "🔴 OFF"
                mainPowerBtn.setBackgroundColor(-0xDD7733)
                activePresetName = null
                val allPresets = getAllPresets()
                val defaultNames = listOf("Clean", "Blues", "Rock", "Metal")
                val userPresets = loadUserPresets()
                allPresets.forEachIndexed { i, p ->
                    if(p.isOn) {
                        p.isOn = false
                        if(i >= defaultNames.size) {
                            val uIdx = i - defaultNames.size
                            if(uIdx < userPresets.size) saveMainMixerToPreset(userPresets[uIdx])
                        }
                    }
                }
                saveAllUserPresets(userPresets)
                refreshAllPresetButtons()
                AudioEngine.stop()
                Toast.makeText(this,"⚫ MAIN OFF — LAHAT PRESET NAKA-OFF!",Toast.LENGTH_SHORT).show()
            } else {
                AudioMixer.setAllOn(true)
                mainPowerBtn.text = "🟢 ON"
                mainPowerBtn.setBackgroundColor(0xFF22EE22.toInt())
                if(!AudioEngine.isRunning()) AudioEngine.start(this)
                Toast.makeText(this,"🟢 MAIN ON!",Toast.LENGTH_SHORT).show()
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

        val colorPickerBtn = Button(this)
        colorPickerBtn.text = "🎨 KULAY"
        colorPickerBtn.setTextColor(Color.WHITE)
        colorPickerBtn.setBackgroundColor(0xFF666666.toInt())
        colorPickerBtn.textSize = 11f
        var selectedColor = 0xFFFF8800.toInt()
        colorPickerBtn.setOnClickListener {
            showColorPickerDialog { color ->
                selectedColor = color
                colorPickerBtn.setBackgroundColor(color)
                Toast.makeText(this,"✅ Napili na kulay!",Toast.LENGTH_SHORT).show()
            }
        }
        bar.addView(colorPickerBtn)

        val saveBtn = Button(this)
        saveBtn.text = "💾 SAVE"; saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(-0xBB7733); saveBtn.textSize = 12f
        saveBtn.setOnClickListener {
            val n = savePresetName.text.toString().trim()
            val defaultNames = listOf("Clean", "Blues", "Rock", "Metal")
            if(n.isBlank()) { Toast.makeText(this,"❌ Ilagay ang pangalan!",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if(n in defaultNames) { Toast.makeText(this,"❌ Hindi pwedeng Default Preset!",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val userPresets = loadUserPresets()
            if(userPresets.any { it.name == n }) { Toast.makeText(this,"❌ May preset na ganyang pangalan!",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val newPreset = PedalPreset(name=n, color=selectedColor, isOn=false)
            saveMainMixerToPreset(newPreset)
            userPresets.add(newPreset)
            saveAllUserPresets(userPresets)
            Toast.makeText(this,"✅ NAISAVE: $n!\n✅ May sariling kulay na!",Toast.LENGTH_LONG).show()
            savePresetName.text.clear()
            pageContainer.removeViews(1, pageContainer.childCount-1)
            buildCabinetPages()
        }
        bar.addView(saveBtn)
        mainPage.addView(bar)
        pageContainer.addView(mainPage)
    }

    private fun showColorPickerDialog(onColorSelected: (Int) -> Unit) {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("🎨 PILIIN ANG KULAY NG PABALAT")
        val items = pedalColors.map { it.first }.toTypedArray()
        dialog.setItems(items) { _, which ->
            onColorSelected(pedalColors[which].second)
        }
        dialog.show()
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
            hint.text = "👆 PILI KULAY BAGO MAG-SAVE! TONEBRIDGE STYLE!"
            hint.textSize = 11f
            hint.setTextColor(-0x888889)
            hint.gravity = Gravity.CENTER
            hint.setPadding(0,4,0,8)
            cabPage.addView(hint)
            pagePresets.forEach { preset -> cabPage.addView(buildBigPedalView(preset)) }
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

        // ✅ POWER BUTTON — ITIM=OFF, BERDE=ON
        val powerBtn = Button(this)
        powerBtn.text = if(preset.isOn) "💡 NAKA-ON" else "⚫ NAKA-OFF"
        powerBtn.textSize = 16f
        powerBtn.setTextColor(Color.WHITE)
        // ✅ AYOS NA ANG ILAW — ITIM KAPAG OFF, BERDE KAPAG ON!
        powerBtn.setBackgroundColor(if(preset.isOn) 0xFF22EE22.toInt() else 0xFF1A1A1A.toInt())
        powerBtn.setPadding(24,8,24,8)
        powerBtn.setOnClickListener {
            val datiActive = activePresetName

            if(preset.isOn) {
                preset.isOn = false
                // ✅ ILAW → ITIM
                powerBtn.text = "⚫ NAKA-OFF"
                powerBtn.setBackgroundColor(0xFF1A1A1A.toInt())

                val idx = allPresets.indexOfFirst { it.name == preset.name }
                if(idx >= defaultCount) {
                    val userPresets = loadUserPresets()
                    val uIdx = idx - defaultCount
                    if(uIdx < userPresets.size) { saveMainMixerToPreset(userPresets[uIdx]); saveAllUserPresets(userPresets) }
                }
                activePresetName = null
                refreshAllPresetButtons()
                Toast.makeText(this,"⚫ ${preset.name} — NAKA-OFF!\n✅ NAISAVE NA!",Toast.LENGTH_SHORT).show()
            } else {
                forceMainPowerOn()

                if(datiActive != null) {
                    val datiIdx = allPresets.indexOfFirst { it.name == datiActive }
                    if(datiIdx >= 0) {
                        val datiPreset = allPresets[datiIdx]
                        datiPreset.isOn = false
                        if(datiIdx >= defaultCount) {
                            val userPresets = loadUserPresets()
                            val uIdx = datiIdx - defaultCount
                            if(uIdx < userPresets.size) { saveMainMixerToPreset(userPresets[uIdx]); saveAllUserPresets(userPresets) }
                        }
                    }
                }

                preset.isOn = true
                // ✅ ILAW → BERDE UMIILAW
                powerBtn.text = "💡 NAKA-ON"
                powerBtn.setBackgroundColor(0xFF22EE22.toInt())

                activePresetName = preset.name
                loadPresetToMainMixer(preset)
                refreshAllPresetButtons()
                Toast.makeText(this,"✅ ${preset.name} — NAKA-ON!\n✅ LAHAT PIHITAN AY AYON SA PRESET!",Toast.LENGTH_SHORT).show()
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

        // ✅ VOLUME KNOB
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
            if(preset.isOn) { AudioMixer.masterVolume = v; updateAllKnobs() }
            val idx = allPresets.indexOfFirst { it.name == preset.name }
            if(idx >= defaultCount) {
                val userPresets = loadUserPresets()
                val uIdx = idx - defaultCount
                if(uIdx < userPresets.size) { userPresets[uIdx].master = v; saveAllUserPresets(userPresets) }
            }
        }
        volRow.addView(volLabel); volRow.addView(volKnob); volRow.addView(volPct)
        volRow.setPadding(0,8,0,8)
        pedal.addView(volRow)

        // ✅ EFFECTS KNOB
        val fxRow = LinearLayout(this)
        fxRow.orientation = LinearLayout.HORIZONTAL
        fxRow.gravity = Gravity.CENTER_VERTICAL
        val fxLabel = TextView(this)
        fxLabel.text = "🎛️ EFFECTS"
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
            if(preset.isOn) { AudioMixer.distortion = v; updateAllKnobs() }
            val idx = allPresets.indexOfFirst { it.name == preset.name }
            if(idx >= defaultCount) {
                val userPresets = loadUserPresets()
                val uIdx = idx - defaultCount
                if(uIdx < userPresets.size) { userPresets[uIdx].dist = v; saveAllUserPresets(userPresets) }
            }
        }
        fxRow.addView(fxLabel); fxRow.addView(fxKnob); fxRow.addView(fxPct)
        fxRow.setPadding(0,8,0,8)
        pedal.addView(fxRow)

        // ✅ NOISE GATE KNOB
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
            if(preset.isOn) { AudioMixer.noiseGate = v; updateAllKnobs() }
            val idx = allPresets.indexOfFirst { it.name == preset.name }
            if(idx >= defaultCount) {
                val userPresets = loadUserPresets()
                val uIdx = idx - defaultCount
                if(uIdx < userPresets.size) { userPresets[uIdx].ng = v; saveAllUserPresets(userPresets) }
            }
        }
        ngRow.addView(ngLabel); ngRow.addView(ngKnob); ngRow.addView(ngPct)
        ngRow.setPadding(0,8,0,8)
        pedal.addView(ngRow)

        return pedal
    }

    private fun goToCabinetPage() { currentPage=1; cabinetPageIndex=0; pageContainer.scrollTo(resources.displayMetrics.widthPixels,0) }
    private fun goNextCabinetPage() {
        val w = resources.displayMetrics.widthPixels
        val maxPage = pageContainer.childCount - 1
        if(cabinetPageIndex + 1 < maxPage) { cabinetPageIndex += 1; pageContainer.scrollTo(w*(1+cabinetPageIndex),0) }
        else Toast.makeText(this,"✅ HULING PAGE NA!",Toast.LENGTH_SHORT).show()
    }
    private fun goPrevCabinetPage() {
        if(cabinetPageIndex > 0) { cabinetPageIndex -= 1; pageContainer.scrollTo(resources.displayMetrics.widthPixels*(1+cabinetPageIndex),0) }
        else if(currentPage > 0) goToMainPage()
        else Toast.makeText(this,"✅ UNAANG PAGE NA!",Toast.LENGTH_SHORT).show()
    }
    private fun goToMainPage() { currentPage=0; cabinetPageIndex=0; pageContainer.scrollTo(0,0) }

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
    override fun onDestroy() { super.onDestroy(); AudioEngine.stop() }
}

class PedalPreset(
    val name: String,
    var color: Int,
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
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name); put("color", color); put("on", isOn)
        put("ng", ng); put("tone", tone); put("gain", gain); put("od", od); put("dist", dist); put("fuzz", fuzz)
        put("chorus", chorus); put("flanger", flanger); put("phaser", phaser); put("trem", trem); put("vib", vib)
        put("delay", delay); put("reverb", reverb); put("wah", wah); put("amp", amp)
        put("bass", bass); put("mid", mid); put("treble", treble); put("master", master)
    }
}
