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
    private lateinit var mainPowerBtn: Button // ✅ REFERENCE SA MAIN POWER BUTTON
    private val knobViews = mutableListOf<Pair<KnobView, TextView>>()
    private val prefs by lazy { getSharedPreferences("GitaraPresets", Context.MODE_PRIVATE) }

    // ✅ MAIN MIXER — 19 PIHITAN — WALANG BABAGUHIN!
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

    private fun getBuiltInPresets(): MutableList<PedalPreset> {
        return mutableListOf(
            PedalPreset("Clean", -0x3399BB, isOn = false, vol = 0.7f, fx = 0.2f, ng = 0.1f),
            PedalPreset("Blues", -0x99BB3D, isOn = false, vol = 0.8f, fx = 0.4f, ng = 0.2f),
            PedalPreset("Rock", -0x66DDDD, isOn = false, vol = 0.85f, fx = 0.7f, ng = 0.3f),
            PedalPreset("Metal", -0xDDDDDD, isOn = false, vol = 0.9f, fx = 0.95f, ng = 0.5f)
        )
    }

    private fun loadUserPresets(): MutableList<PedalPreset> {
        val list = mutableListOf<PedalPreset>()
        try {
            val json = prefs.getString("user_pedal_presets", "[]")
            val arr = JSONArray(json)
            for(i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(PedalPreset(
                    name = o.getString("name"),
                    color = o.optInt("color", -0xBB7733),
                    isOn = o.optBoolean("on", false),
                    vol = o.optDouble("vol", 0.5).toFloat(),
                    fx = o.optDouble("fx", 0.5).toFloat(),
                    ng = o.optDouble("ng", 0.5).toFloat()
                ))
            }
        } catch(_:Exception) {}
        return list
    }

    private fun saveAllPresets(allPresets: List<PedalPreset>) {
        val arr = JSONArray()
        allPresets.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("user_pedal_presets", arr.toString()).apply()
    }

    private fun applyPresetToMainMixer(preset: PedalPreset) {
        AudioMixer.masterVolume = preset.vol
        AudioMixer.noiseGate = preset.ng
        val fxStrength = preset.fx
        if(AudioMixer.overdrive > 0f || fxStrength > 0.05f) AudioMixer.overdrive = fxStrength
        if(AudioMixer.distortion > 0f || fxStrength > 0.05f) AudioMixer.distortion = fxStrength
        if(AudioMixer.fuzz > 0f || fxStrength > 0.05f) AudioMixer.fuzz = fxStrength * 0.8f
        if(AudioMixer.chorus > 0f || fxStrength > 0.05f) AudioMixer.chorus = fxStrength * 0.6f
        if(AudioMixer.flanger > 0f || fxStrength > 0.05f) AudioMixer.flanger = fxStrength * 0.6f
        if(AudioMixer.phaser > 0f || fxStrength > 0.05f) AudioMixer.phaser = fxStrength * 0.5f
        if(AudioMixer.tremolo > 0f || fxStrength > 0.05f) AudioMixer.tremolo = fxStrength * 0.4f
        if(AudioMixer.reverb > 0f || fxStrength > 0.05f) AudioMixer.reverb = fxStrength * 0.5f
        if(AudioMixer.delay > 0f || fxStrength > 0.05f) AudioMixer.delay = fxStrength * 0.5f
        if(AudioMixer.wah > 0f || fxStrength > 0.05f) AudioMixer.wah = fxStrength * 0.7f
        updateKnobsFromPreset()
    }

    private fun updateKnobsFromPreset() {
        val vals = floatArrayOf(
            AudioMixer.noiseGate, AudioMixer.tone, AudioMixer.gain,
            AudioMixer.overdrive, AudioMixer.distortion, AudioMixer.fuzz,
            AudioMixer.chorus, AudioMixer.flanger, AudioMixer.phaser,
            AudioMixer.tremolo, AudioMixer.vibrato, AudioMixer.delay,
            AudioMixer.reverb, AudioMixer.wah, AudioMixer.ampType,
            AudioMixer.bass, AudioMixer.mid, AudioMixer.treble,
            AudioMixer.masterVolume
        )
        for(i in knobViews.indices) {
            val (knob, pct) = knobViews[i]
            knob.value = vals[i]
            pct.text = "${(vals[i]*100).toInt()}%"
        }
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
                        if(d > 0) { if(currentPage==0) goToCabinetPage() else goNextCabinetPage() }
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
                pct.text = "${(knob.value*100).toInt()}%"; pct.setTextColor(color); pct.textSize=11f
                knob.onChange = { v ->
                    pct.text = "${(v*100).toInt()}%"
                    when(k) {
                        0->AudioMixer.noiseGate=v;1->AudioMixer.tone=v;2->AudioMixer.gain=v
                        3->AudioMixer.overdrive=v;4->AudioMixer.distortion=v;5->AudioMixer.fuzz=v
                        6->AudioMixer.chorus=v;7->AudioMixer.flanger=v;8->AudioMixer.phaser=v
                        9->AudioMixer.tremolo=v;10->AudioMixer.vibrato=v;11->AudioMixer.delay=v
                        12->AudioMixer.reverb=v;13->AudioMixer.wah=v;14->AudioMixer.ampType=v
                        15->AudioMixer.bass=v;16->AudioMixer.mid=v;17->AudioMixer.treble=v
                        18->AudioMixer.masterVolume=v
                    }
                }
                col.addView(knob, LinearLayout.LayoutParams(80,80))
                col.addView(pct)
                val lbl = TextView(this)
                lbl.text=label; lbl.setTextColor(color); lbl.textSize=9f; lbl.gravity=Gravity.CENTER
                col.addView(lbl)
                rowLay.addView(col, LinearLayout.LayoutParams(0,-1,1f))
                knobViews.add(knob to pct)
            }
            grid.addView(rowLay)
        }
        scroll.addView(grid)
        mainPage.addView(scroll)

        // ✅ MAIN POWER BUTTON — I-SAVE ANG REFERENCE!
        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER
        bar.setBackgroundColor(-0xDDDDFF)
        bar.setPadding(8,8,8,8)

        mainPowerBtn = Button(this) // ✅ I-SAVE ANG REFERENCE
        mainPowerBtn.text="🟢 ON"; mainPowerBtn.setTextColor(Color.WHITE)
        mainPowerBtn.setBackgroundColor(-0xDD7733); mainPowerBtn.textSize=13f
        mainPowerBtn.setOnClickListener {
            val isOn = AudioMixer.isAllOn()
            AudioMixer.setAllOn(!isOn)
            if(isOn) { mainPowerBtn.text="🔴 OFF"; mainPowerBtn.setBackgroundColor(-0xDD3333) }
            else { mainPowerBtn.text="🟢 ON"; mainPowerBtn.setBackgroundColor(-0x33DD33); AudioEngine.start(this) }
        }
        bar.addView(mainPowerBtn)

        savePresetName = EditText(this)
        savePresetName.hint="Pangalan Preset"
        savePresetName.setTextColor(Color.WHITE); savePresetName.setHintTextColor(-0x888889)
        savePresetName.setBackgroundColor(-0xDDDDDE); savePresetName.setPadding(8,4,8,4)
        bar.addView(savePresetName, LinearLayout.LayoutParams(0,-1,1f).apply { setMargins(12,0,12,0) })

        val saveBtn = Button(this)
        saveBtn.text="💾 SAVE"; saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(-0xBB7733); saveBtn.textSize=12f
        saveBtn.setOnClickListener {
            val n = savePresetName.text.toString().trim()
            if(n.isBlank()) {
                Toast.makeText(this,"❌ Ilagay ang pangalan!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val all = loadUserPresets()
            if(all.any { it.name == n } || n in listOf("Clean","Blues","Rock","Metal")) {
                Toast.makeText(this,"❌ May pangalan na!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            all.add(PedalPreset(n, pickColor(), false, 0.5f, 0.5f, AudioMixer.noiseGate))
            saveAllPresets(all)
            Toast.makeText(this,"✅ NAISAVE: $n!",Toast.LENGTH_LONG).show()
            savePresetName.text.clear()
            pageContainer.removeViews(1, pageContainer.childCount-1)
            buildCabinetPages()
        }
        bar.addView(saveBtn)
        mainPage.addView(bar)
        pageContainer.addView(mainPage)
    }

    // ==========================================
    // ✅ CABINET — 2 PEDALS LANG BAWAT PAGE! BUONG SCREEN!
    // ==========================================
    private fun buildCabinetPages() {
        val allPresets = getBuiltInPresets() + loadUserPresets()
        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels
        val PER_PAGE = 2 // ✅ 2 PEDALS LANG — BUONG SCREEN!

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
            backBtn.setPadding(16, 8, 16, 8)
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
            hint.text = "👆 SWIPE ←→ | 2 PEDALS LANG — LAKI AT KITANG-KITA!"
            hint.textSize = 11f
            hint.setTextColor(-0x888889)
            hint.gravity = Gravity.CENTER
            hint.setPadding(0,4,0,8)
            cabPage.addView(hint)

            // ✅ 2 PEDALS — PATAYO SA ISA'T ISA, BUONG SCREEN SAKOP!
            pagePresets.forEach { preset ->
                cabPage.addView(buildBigPedalView(preset, allPresets))
            }

            pageContainer.addView(cabPage)
        }
    }

    // ==========================================
    // ✅ MALAKING PEDAL VIEW — SAKOP BUONG SCREEN!
    // ==========================================
    private fun buildBigPedalView(preset: PedalPreset, allPresets: List<PedalPreset>): LinearLayout {
        val w = resources.displayMetrics.widthPixels
        val pedal = LinearLayout(this)
        pedal.orientation = LinearLayout.VERTICAL
        pedal.setBackgroundColor(preset.color)
        pedal.setPadding(16,12,16,12)
        pedal.gravity = Gravity.CENTER
        pedal.layoutParams = LinearLayout.LayoutParams(w - 48, 0, 1f) // ✅ MALAKI!

        // ✅ 💡 POWER BUTTON — SABAY SA MAIN POWER!
        val powerBtn = Button(this)
        powerBtn.text = if(preset.isOn) "💡 NAKA-ON" else "⚫ NAKA-OFF"
        powerBtn.textSize = 16f
        powerBtn.setTextColor(Color.WHITE)
        powerBtn.setBackgroundColor(if(preset.isOn) 0xFF22CC22.toInt() else 0xFF333333.toInt())
        powerBtn.setPadding(24, 8, 24, 8)
        powerBtn.setOnClickListener {
            preset.isOn = !preset.isOn
            powerBtn.text = if(preset.isOn) "💡 NAKA-ON" else "⚫ NAKA-OFF"
            powerBtn.setBackgroundColor(if(preset.isOn) 0xFF22CC22.toInt() else 0xFF333333.toInt())

            // ✅ SABAY ANG MAIN POWER BUTTON!
            if(preset.isOn) {
                AudioMixer.setAllOn(true)
                mainPowerBtn.text = "🟢 ON"
                mainPowerBtn.setBackgroundColor(-0x33DD33)
                applyPresetToMainMixer(preset)
                Toast.makeText(this,"✅ ${preset.name} — NAKA-ON!",Toast.LENGTH_SHORT).show()
                if(!AudioEngine.isRunning()) AudioEngine.start(this)
            } else {
                AudioMixer.setAllOn(false)
                mainPowerBtn.text = "🔴 OFF"
                mainPowerBtn.setBackgroundColor(-0xDD7733)
                Toast.makeText(this,"⚫ ${preset.name} — NAKA-OFF!",Toast.LENGTH_SHORT).show()
            }

            saveAllPresets(allPresets)
        }
        pedal.addView(powerBtn)

        // ✅ PANGALAN NG PEDAL — MALAKI
        val name = TextView(this)
        name.text = preset.name
        name.textSize = 22f
        name.setTextColor(Color.WHITE)
        name.gravity = Gravity.CENTER
        name.setPadding(0,8,0,8)
        pedal.addView(name)

        // ✅ 🎚️ VOLUME KNOB — MALAKI
        val volRow = LinearLayout(this)
        volRow.orientation = LinearLayout.HORIZONTAL
        volRow.gravity = Gravity.CENTER_VERTICAL
        val volLabel = TextView(this); volLabel.text="🎚️ VOLUME"; volLabel.textSize=14f; volLabel.setTextColor(Color.WHITE); volLabel.setPadding(0,0,12,0)
        val volKnob = KnobView(this); volKnob.baseColor = 0xFFFFCC00.toInt(); volKnob.value = preset.vol
        volKnob.layoutParams = LinearLayout.LayoutParams(64,64)
        val volPct = TextView(this); volPct.text="${(preset.vol*100).toInt()}%"; volPct.textSize=14f; volPct.setTextColor(Color.WHITE); volPct.setPadding(12,0,0,0)
        volKnob.onChange = { v ->
            preset.vol = v
            volPct.text = "${(v*100).toInt()}%"
            if(preset.isOn) { AudioMixer.masterVolume = v; updateKnobsFromPreset() }
            saveAllPresets(allPresets)
        }
        volRow.addView(volLabel); volRow.addView(volKnob); volRow.addView(volPct)
        volRow.setPadding(0,8,0,8)
        pedal.addView(volRow)

        // ✅ 🎛️ EFFECT KNOB — MALAKI
        val fxRow = LinearLayout(this)
        fxRow.orientation = LinearLayout.HORIZONTAL
        fxRow.gravity = Gravity.CENTER_VERTICAL
        val fxLabel = TextView(this); fxLabel.text="🎛️ EFFECT"; fxLabel.textSize=14f; fxLabel.setTextColor(Color.WHITE); fxLabel.setPadding(0,0,12,0)
        val fxKnob = KnobView(this); fxKnob.baseColor = 0xFFFF4422.toInt(); fxKnob.value = preset.fx
        fxKnob.layoutParams = LinearLayout.LayoutParams(64,64)
        val fxPct = TextView(this); fxPct.text="${(preset.fx*100).toInt()}%"; fxPct.textSize=14f; fxPct.setTextColor(Color.WHITE); fxPct.setPadding(12,0,0,0)
        fxKnob.onChange = { v ->
            preset.fx = v
            fxPct.text = "${(v*100).toInt()}%"
            if(preset.isOn) {
                val fxStrength = v
                if(AudioMixer.overdrive > 0f || v > 0.05f) AudioMixer.overdrive = fxStrength
                if(AudioMixer.distortion > 0f || v > 0.05f) AudioMixer.distortion = fxStrength
                if(AudioMixer.fuzz > 0f || v > 0.05f) AudioMixer.fuzz = fxStrength * 0.8f
                if(AudioMixer.chorus > 0f || v > 0.05f) AudioMixer.chorus = fxStrength * 0.6f
                if(AudioMixer.flanger > 0f || v > 0.05f) AudioMixer.flanger = fxStrength * 0.6f
                if(AudioMixer.phaser > 0f || v > 0.05f) AudioMixer.phaser = fxStrength * 0.5f
                if(AudioMixer.reverb > 0f || v > 0.05f) AudioMixer.reverb = fxStrength * 0.5f
                updateKnobsFromPreset()
            }
            saveAllPresets(allPresets)
        }
        fxRow.addView(fxLabel); fxRow.addView(fxKnob); fxRow.addView(fxPct)
        fxRow.setPadding(0,8,0,8)
        pedal.addView(fxRow)

        // ✅ 🚧 NOISE GATE KNOB — MALAKI
        val ngRow = LinearLayout(this)
        ngRow.orientation = LinearLayout.HORIZONTAL
        ngRow.gravity = Gravity.CENTER_VERTICAL
        val ngLabel = TextView(this); ngLabel.text="🚧 NOISE GATE"; ngLabel.textSize=14f; ngLabel.setTextColor(Color.WHITE); ngLabel.setPadding(0,0,12,0)
        val ngKnob = KnobView(this); ngKnob.baseColor = 0xFF44DD88.toInt(); ngKnob.value = preset.ng
        ngKnob.layoutParams = LinearLayout.LayoutParams(64,64)
        val ngPct = TextView(this); ngPct.text="${(preset.ng*100).toInt()}%"; ngPct.textSize=14f; ngPct.setTextColor(Color.WHITE); ngPct.setPadding(12,0,0,0)
        ngKnob.onChange = { v ->
            preset.ng = v
            ngPct.text = "${(v*100).toInt()}%"
            if(preset.isOn) { AudioMixer.noiseGate = v; updateKnobsFromPreset() }
            saveAllPresets(allPresets)
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
        if(cabinetPageIndex + 1 < maxPage) { cabinetPageIndex++; pageContainer.scrollTo(w*(1+cabinetPageIndex),0) }
        else Toast.makeText(this,"✅ HULING PAGE NA!",Toast.LENGTH_SHORT).show()
    }
    private fun goPrevCabinetPage() {
        if(cabinetPageIndex > 0) { cabinetPageIndex--; pageContainer.scrollTo(resources.displayMetrics.widthPixels*(1+cabinetPageIndex),0) }
        else if(currentPage > 0) goToMainPage()
        else Toast.makeText(this,"✅ UNAANG PAGE NA!",Toast.LENGTH_SHORT).show()
    }
    private fun goToMainPage() { currentPage=0; cabinetPageIndex=0; pageContainer.scrollTo(0,0) }

    private val colors = listOf(-0xBB7734, -0x33BB78, -0x7733BB, -0xBB3377, -0x33BBBB, -0xBBBB33)
    private var colorIdx = 0
    private fun pickColor():Int = colors[colorIdx++ % colors.size]

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this,"✅ Handa na! Swipe pakaliwa → Cabinet!",Toast.LENGTH_LONG).show()
        else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),123)
    }
    override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray) {
        super.onRequestPermissionsResult(r,p,g)
        if(r==123 && g.firstOrNull()==PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this,"✅ Pahintulot natanggap!",Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() { super.onDestroy(); AudioEngine.stop() }
}

class PedalPreset(
    val name: String,
    val color: Int,
    var isOn: Boolean = false,
    var vol: Float = 0.5f,
    var fx: Float = 0.5f,
    var ng: Float = 0.5f
) {
    fun toJson() = JSONObject().apply {
        put("name", name)
        put("color", color)
        put("on", isOn)
        put("vol", vol)
        put("fx", fx)
        put("ng", ng)
    }
}
