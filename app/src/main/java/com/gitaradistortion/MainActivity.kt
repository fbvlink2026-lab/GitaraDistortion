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
    private lateinit var pageContainer: LinearLayout
    private lateinit var savePresetName: EditText
    private val prefs by lazy { getSharedPreferences("GitaraPresets", Context.MODE_PRIVATE) }

    // ✅ BUILT-IN PRESETS
    private fun getBuiltInPresets() = listOf(
        PedalPreset("Clean", -0x3399BB, volume=0.75f, effect=0.20f, noiseGate=0.05f, isOn=true, desc="Malinaw"),
        PedalPreset("Blues", -0x99BB3D, volume=0.80f, effect=0.45f, noiseGate=0.08f, isOn=true, desc="Mainit"),
        PedalPreset("Rock", -0x66DDDD, volume=0.85f, effect=0.70f, noiseGate=0.12f, isOn=true, desc="Matigas"),
        PedalPreset("Metal", -0xDDDDDD, volume=0.90f, effect=0.95f, noiseGate=0.20f, isOn=true, desc="Mabigat")
    )

    private val PEDALS_PER_PAGE = 20

    private fun loadUserPresets(): MutableList<PedalPreset> {
        val list = mutableListOf<PedalPreset>()
        try {
            val json = prefs.getString("user_presets_v3", "[]")
            val arr = JSONArray(json)
            for(i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(PedalPreset(
                    name = o.getString("name"),
                    color = o.optInt("color", -0xBB7733),
                    volume = o.optDouble("vol", 0.5).toFloat(),
                    effect = o.optDouble("fx", 0.5).toFloat(),
                    noiseGate = o.optDouble("ng", 0.05).toFloat(),
                    isOn = o.optBoolean("on", true),
                    desc = o.optString("desc", "Aking Preset")
                ))
            }
        } catch(_:Exception) {}
        return list
    }

    private fun saveAllPresets(all: List<PedalPreset>) {
        val arr = JSONArray()
        all.forEach { p ->
            arr.put(JSONObject().apply {
                put("name", p.name)
                put("color", p.color)
                put("vol", p.volume.toDouble())
                put("fx", p.effect.toDouble())
                put("ng", p.noiseGate.toDouble())
                put("on", p.isOn)
                put("desc", p.desc)
            })
        }
        prefs.edit().putString("user_presets_v3", arr.toString()).apply()
    }

    private fun saveNewPreset(name:String, vol:Float, fx:Float, ng:Float):Boolean {
        if(name.isBlank() || name in listOf("Clean","Blues","Rock","Metal")) return false
        val all = loadUserPresets()
        all.add(PedalPreset(name, pickColor(), vol, fx, ng, true, "Aking Preset"))
        saveAllPresets(all)
        return true
    }

    private fun applyPedal(p:PedalPreset) {
        if(p.isOn) {
            AudioMixer.masterVolume = p.volume
            if(p.effect > 0.01f) {
                AudioMixer.overdrive = p.effect
                AudioMixer.distortion = p.effect
                AudioMixer.fuzz = p.effect * 0.8f
                AudioMixer.chorus = p.effect * 0.6f
                AudioMixer.flanger = p.effect * 0.5f
                AudioMixer.phaser = p.effect * 0.4f
                AudioMixer.tremolo = p.effect * 0.3f
                AudioMixer.vibrato = p.effect * 0.3f
                AudioMixer.delay = p.effect * 0.5f
                AudioMixer.reverb = p.effect * 0.4f
            }
            AudioMixer.noiseGate = p.noiseGate
            if(!AudioEngine.isRunning()) AudioEngine.start(this)
        } else {
            AudioMixer.masterVolume = 0.05f
            AudioMixer.overdrive = 0f
            AudioMixer.noiseGate = 0.01f
        }
    }

    private val colors = listOf(-0xBB7734, -0x33BB78, -0x7733BB, -0xBB3377, -0x33BBBB, -0xBBBB33, -0x77BB33, -0x3377BB)
    private var colorIdx = 0
    private fun pickColor():Int = colors[colorIdx++ % colors.size]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        buildUI()
    }

    private fun buildUI() {
        val root = findViewById<LinearLayout>(R.id.rootLayout)
        root.setBackgroundColor(-0x1A1A1A)

        pageContainer = LinearLayout(this)
        pageContainer.orientation = LinearLayout.HORIZONTAL
        pageContainer.layoutParams = LinearLayout.LayoutParams(-1, -1)

        // ✅ PAGE 0: BUONG MAIN MIXER PANEL — LAHAT NG 19 PIHITAN! WALANG BINAGO!
        buildMainMixerPage()

        // ✅ PAGE 1+: CABINET — I-SAVE + MGA NAKA-SAVE NA PEDAL
        buildAllCabinetPages()

        root.addView(pageContainer)

        pageContainer.setOnTouchListener { _, e ->
            when(e.action) {
                MotionEvent.ACTION_DOWN -> { startX = e.rawX; isSwiping = false }
                MotionEvent.ACTION_UP -> {
                    val dist = abs(startX - e.rawX)
                    val minSwipe = resources.displayMetrics.widthPixels * 0.25f
                    if(!isSwiping && dist > minSwipe) {
                        isSwiping = true
                        if(startX > e.rawX) {
                            if(currentPage == 0) goToCabinetPage() else goNextCabinetPage()
                        } else goPrevCabinetPage()
                    }
                }
            }
            true
        }
    }

    // ✅ BUONG MAIN MIXER — IBINABALIK ANG LAHAT NG 19 PIHITAN!
    private fun buildMainMixerPage() {
        val w = resources.displayMetrics.widthPixels
        val mainPage = LinearLayout(this)
        mainPage.orientation = LinearLayout.VERTICAL
        mainPage.layoutParams = LinearLayout.LayoutParams(w, -1)
        mainPage.setBackgroundColor(-0x1A1A1A)
        mainPage.setPadding(6,6,6,6)

        val title = TextView(this)
        title.text = "🎛️ MAIN PEDAL BOARD"
        title.textSize = 22f; title.setTextColor(-0xFFCC00)
        title.gravity = Gravity.CENTER; title.setPadding(0,8,0,4)
        mainPage.addView(title)

        val hint = TextView(this)
        hint.text = "👉 SWIPE PAKALIWA → CABINET (I-SAVE + Mga Pedal)"
        hint.textSize = 11f; hint.setTextColor(-0xAAAAAB)
        hint.gravity = Gravity.CENTER; hint.setPadding(0,0,0,8)
        mainPage.addView(hint)

        val scroll = ScrollView(this)
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(4,4,4,4)

        // ✅ UNANG HANAY — VOLUME, TONE, GAIN, NOISE GATE
        content.addView(buildKnobRow("🔊 VOLUME", -0x00DD55, AudioMixer::masterVolume, { AudioMixer.masterVolume = it }))
        content.addView(buildKnobRow("🎵 TONE", -0xFFCC00, AudioMixer::tone, { AudioMixer.tone = it }))
        content.addView(buildKnobRow("⚡ GAIN", -0xFF6600, AudioMixer::gain, { AudioMixer.gain = it }))
        content.addView(buildKnobRow("🚧 NOISE GATE", -0x888889, AudioMixer::noiseGate, { AudioMixer.noiseGate = it }))

        // ✅ PANGALAWANG HANAY — DISTORTION FAMILY
        content.addView(buildKnobRow("🔥 OVERDRIVE", -0xDD4400, AudioMixer::overdrive, { AudioMixer.overdrive = it }))
        content.addView(buildKnobRow("💥 DISTORTION", -0xCC2200, AudioMixer::distortion, { AudioMixer.distortion = it }))
        content.addView(buildKnobRow("🧶 FUZZ", -0xAA0022, AudioMixer::fuzz, { AudioMixer.fuzz = it }))

        // ✅ PANGATLONG HANAY — MODULATION
        content.addView(buildKnobRow("🎶 CHORUS", -0x2288DD, AudioMixer::chorus, { AudioMixer.chorus = it }))
        content.addView(buildKnobRow("🌊 FLANGER", -0x22AADD, AudioMixer.flanger, { AudioMixer.flanger = it }))
        content.addView(buildKnobRow("🌀 PHASER", -0x4488CC, AudioMixer.phaser, { AudioMixer.phaser = it }))
        content.addView(buildKnobRow("📳 TREMOLO", -0x66AA66, AudioMixer.tremolo, { AudioMixer.tremolo = it }))
        content.addView(buildKnobRow("🫨 VIBRATO", -0x88BB44, AudioMixer.vibrato, { AudioMixer.vibrato = it }))

        // ✅ PANG-APAT NA HANAY — TIME & SPACE
        content.addView(buildKnobRow("⏱️ DELAY", -0x6666CC, AudioMixer.delay, { AudioMixer.delay = it }))
        content.addView(buildKnobRow("🏛️ REVERB", -0x8866DD, AudioMixer.reverb, { AudioMixer.reverb = it }))

        scroll.addView(content)
        mainPage.addView(scroll)
        pageContainer.addView(mainPage)
    }

    // ✅ TUMUTULONG — BUUIN ANG ISANG HANAY NG PIHITAN
    private fun buildKnobRow(labelText:String, color:Int, getValue:()->Float, setValue:(Float)->Unit): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(8,4,8,4)
        row.setBackgroundColor(-0x2A2A2B)

        val lbl = TextView(this)
        lbl.text = labelText
        lbl.textSize = 13f
        lbl.setTextColor(Color.WHITE)
        lbl.setPadding(8,4,8,4)
        row.addView(lbl, LinearLayout.LayoutParams(0, -1, 0.4f))

        val knob = KnobView(this)
        knob.baseColor = color
        knob.value = getValue()
        row.addView(knob, LinearLayout.LayoutParams(70, 70))

        val valTxt = TextView(this)
        valTxt.text = "${(getValue()*100).toInt()}%"
        valTxt.textSize = 12f
        valTxt.setTextColor(color)
        valTxt.setPadding(8,4,8,4)
        row.addView(valTxt, LinearLayout.LayoutParams(0, -1, 0.3f))

        knob.onChange = {
            setValue(it)
            valTxt.text = "${(it*100).toInt()}%"
        }
        return row
    }

    // ✅ BUUIN ANG LAHAT NG CABINET PAGE
    private fun buildAllCabinetPages() {
        val allPresets = getBuiltInPresets() + loadUserPresets()
        val totalPages = (allPresets.size + PEDALS_PER_PAGE - 1) / PEDALS_PER_PAGE
        val scrW = resources.displayMetrics.widthPixels

        for(pageIdx in 0 until totalPages) {
            val start = pageIdx * PEDALS_PER_PAGE
            val end = minOf(start + PEDALS_PER_PAGE, allPresets.size)
            val pagePedals = allPresets.subList(start, end)

            val cabPage = LinearLayout(this)
            cabPage.orientation = LinearLayout.VERTICAL
            cabPage.layoutParams = LinearLayout.LayoutParams(scrW, -1)
            cabPage.setBackgroundColor(-0x1A1A1A)
            cabPage.setPadding(6,6,6,6)

            val topBar = LinearLayout(this)
            topBar.orientation = LinearLayout.HORIZONTAL
            topBar.gravity = Gravity.CENTER_VERTICAL
            topBar.setPadding(4,4,4,4)

            val backBtn = Button(this)
            backBtn.text = "⬅️ MAIN"
            backBtn.textSize = 13f
            backBtn.setTextColor(Color.WHITE)
            backBtn.setBackgroundColor(-0xDD3333)
            backBtn.setPadding(12,6,12,6)
            backBtn.setOnClickListener { goToMainPage() }
            topBar.addView(backBtn)

            val pageTitle = TextView(this)
            pageTitle.text = "📦 CABINET — PAGE ${pageIdx+1} / $totalPages"
            pageTitle.textSize = 15f
            pageTitle.setTextColor(-0xFFCC00)
            pageTitle.setPadding(12,0,0,0)
            topBar.addView(pageTitle, LinearLayout.LayoutParams(0,-1,1f))
            cabPage.addView(topBar)

            // ✅ I-SAVE BAGONG PEDAL — NASA CABINET LANG!
            val saveBox = LinearLayout(this)
            saveBox.orientation = LinearLayout.VERTICAL
            saveBox.setBackgroundColor(-0x2A2A2B)
            saveBox.setPadding(10,10,10,10)
            saveBox.setPadding(8,4,8,4)

            val saveTitle = TextView(this)
            saveTitle.text = "💾 I-SAVE BAGONG PEDAL"
            saveTitle.textSize = 14f; saveTitle.setTextColor(-0xFFCC00)
            saveTitle.setPadding(0,0,0,6)
            saveBox.addView(saveTitle)

            savePresetName = EditText(this)
            savePresetName.hint = "Pangalan ng Pedal"
            savePresetName.setTextColor(Color.WHITE)
            savePresetName.setHintTextColor(-0x888889)
            savePresetName.setBackgroundColor(-0x3A3A3B)
            savePresetName.setPadding(10,6,10,6)
            saveBox.addView(savePresetName)

            val saveKnobRow = LinearLayout(this)
            saveKnobRow.orientation = LinearLayout.HORIZONTAL
            saveKnobRow.gravity = Gravity.CENTER
            val vKnob = KnobView(this).apply { baseColor=-0x00DD55; value=AudioMixer.masterVolume }
            val fKnob = KnobView(this).apply { baseColor=-0xFF8800; value=AudioMixer.overdrive }
            val nKnob = KnobView(this).apply { baseColor=-0x777777; value=AudioMixer.noiseGate }
            val vTxt = TextView(this).apply { text="${(vKnob.value*100).toInt()}%"; setTextColor(-0x00DD55); textSize=10f }
            val fTxt = TextView(this).apply { text="${(fKnob.value*100).toInt()}%"; setTextColor(-0xFF8800); textSize=10f }
            val nTxt = TextView(this).apply { text="${(nKnob.value*100).toInt()}%"; setTextColor(-0x777777); textSize=10f }
            vKnob.onChange = { vTxt.text="${(it*100).toInt()}%" }
            fKnob.onChange = { fTxt.text="${(it*100).toInt()}%" }
            nKnob.onChange = { nTxt.text="${(it*100).toInt()}%" }

            val items = listOf(
                Triple("🔊 VOLUME", vKnob, vTxt),
                Triple("⚡ EFFECT", fKnob, fTxt),
                Triple("🚧 NOISE GATE", nKnob, nTxt)
            )
            items.forEach { (lbl, k, t) ->
                val col = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER }
                col.addView(k, LinearLayout.LayoutParams(50,50))
                col.addView(t)
                val l = TextView(this).apply { text=lbl; textSize=9f; setTextColor(-0xAAAAAB); gravity=Gravity.CENTER }
                col.addView(l)
                saveKnobRow.addView(col, LinearLayout.LayoutParams(0,-1,1f))
            }
            saveBox.addView(saveKnobRow)

            val saveBtn = Button(this)
            saveBtn.text = "💾 I-SAVE"
            saveBtn.setBackgroundColor(-0x339933)
            saveBtn.setTextColor(Color.WHITE)
            saveBtn.setOnClickListener {
                val nm = savePresetName.text.toString().trim()
                if(nm.isBlank()) {
                    Toast.makeText(this,"❌ Ilagay ang pangalan!",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val allPresetsNow = getBuiltInPresets() + loadUserPresets()
                val pos = allPresetsNow.size % PEDALS_PER_PAGE
                val pageNum = (allPresetsNow.size / PEDALS_PER_PAGE) + 1
                val isNewPage = pos == 0 && allPresetsNow.size > 0
                if(saveNewPreset(nm, vKnob.value, fKnob.value, nKnob.value)) {
                    Toast.makeText(this, if(isNewPage) "✅ BAGONG PAGE $pageNum!" else "✅ NAISAVE! PAGE $pageNum — PEDAL #${pos+1}", Toast.LENGTH_LONG).show()
                    savePresetName.text.clear()
                    pageContainer.removeViews(1, pageContainer.childCount-1)
                    buildAllCabinetPages()
                } else Toast.makeText(this,"❌ Pangalan na gamit o blangko!",Toast.LENGTH_SHORT).show()
            }
            saveBox.addView(saveBtn)
            cabPage.addView(saveBox)

            val info = TextView(this)
            info.text = "📌 ${pagePedals.size}/20 • 10 Kaliwa • 10 Kanan • Auto-Save habang pini-pihit"
            info.textSize = 10f; info.setTextColor(-0x888889)
            info.gravity = Gravity.CENTER
            cabPage.addView(info)

            val col1 = mutableListOf<PedalPreset>()
            val col2 = mutableListOf<PedalPreset>()
            pagePedals.forEachIndexed { i, p -> if(i%2==0) col1.add(p) else col2.add(p) }

            val rowContainer = LinearLayout(this)
            rowContainer.orientation = LinearLayout.HORIZONTAL

            listOf(col1 to "KALIWA", col2 to "KANAN").forEach { (pedals, _) ->
                val colScroll = ScrollView(this)
                val colLay = LinearLayout(this)
                colLay.orientation = LinearLayout.VERTICAL
                colLay.setPadding(4,4,4,4)

                pedals.forEach { pedal ->
                    val card = LinearLayout(this)
                    card.orientation = LinearLayout.VERTICAL
                    card.setBackgroundColor(pedal.color)
                    card.setPadding(6,4,6,4)
                    card.gravity = Gravity.CENTER

                    val power = Button(this)
                    power.text = if(pedal.isOn) "💡 ON" else "⚫ OFF"
                    power.textSize = 10f
                    power.setTextColor(Color.WHITE)
                    power.setBackgroundColor(if(pedal.isOn) -0x009933 else -0x555555)
                    power.setPadding(6,1,6,1)
                    power.setOnClickListener {
                        pedal.isOn = !pedal.isOn
                        power.text = if(pedal.isOn) "💡 ON" else "⚫ OFF"
                        power.setBackgroundColor(if(pedal.isOn) -0x009933 else -0x555555)
                        applyPedal(pedal)
                        saveAllPresets(getBuiltInPresets() + loadUserPresets())
                        Toast.makeText(this@MainActivity, "✅ ${pedal.name}: ${if(pedal.isOn) "NABUHAY!" else "NAMATAY!"}", Toast.LENGTH_SHORT).show()
                    }
                    card.addView(power)

                    val name = TextView(this)
                    name.text = pedal.name
                    name.textSize = 13f
                    name.setTextColor(Color.WHITE)
                    name.gravity = Gravity.CENTER
                    name.setPadding(0,1,0,1)
                    card.addView(name)

                    val kRow = LinearLayout(this)
                    kRow.orientation = LinearLayout.HORIZONTAL
                    kRow.gravity = Gravity.CENTER

                    val vCol = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER }
                    val vKnobP = KnobView(this).apply { baseColor=-0x00DD55; value=pedal.volume }
                    val vTxtP = TextView(this).apply { text="${(pedal.volume*100).toInt()}%"; textSize=7f; setTextColor(-0x00DD55) }
                    vKnobP.onChange = {
                        pedal.volume = it
                        vTxtP.text = "${(it*100).toInt()}%"
                        AudioMixer.masterVolume = it
                        saveAllPresets(getBuiltInPresets() + loadUserPresets())
                    }
                    vCol.addView(vKnobP, LinearLayout.LayoutParams(32,32))
                    vCol.addView(vTxtP)
                    kRow.addView(vCol, LinearLayout.LayoutParams(0,-1,1f))

                    val fCol = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER }
                    val fKnobP = KnobView(this).apply { baseColor=-0xFF8800; value=pedal.effect }
                    val fTxtP = TextView(this).apply { text="${(pedal.effect*100).toInt()}%"; textSize=7f; setTextColor(-0xFF8800) }
                    fKnobP.onChange = {
                        pedal.effect = it
                        fTxtP.text = "${(it*100).toInt()}%"
                        if(it > 0.01f) {
                            AudioMixer.overdrive = it
                            AudioMixer.distortion = it
                            AudioMixer.fuzz = it * 0.8f
                            AudioMixer.chorus = it * 0.6f
                            AudioMixer.flanger = it * 0.5f
                            AudioMixer.phaser = it * 0.4f
                            AudioMixer.delay = it * 0.5f
                            AudioMixer.reverb = it * 0.4f
                        }
                        saveAllPresets(getBuiltInPresets() + loadUserPresets())
                    }
                    fCol.addView(fKnobP, LinearLayout.LayoutParams(32,32))
                    fCol.addView(fTxtP)
                    kRow.addView(fCol, LinearLayout.LayoutParams(0,-1,1f))

                    val nCol = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER }
                    val nKnobP = KnobView(this).apply { baseColor=-0x777777; value=pedal.noiseGate }
                    val nTxtP = TextView(this).apply { text="${(pedal.noiseGate*100).toInt()}%"; textSize=7f; setTextColor(-0x777777) }
                    nKnobP.onChange = {
                        pedal.noiseGate = it
                        nTxtP.text = "${(it*100).toInt()}%"
                        AudioMixer.noiseGate = it
                        saveAllPresets(getBuiltInPresets() + loadUserPresets())
                    }
                    nCol.addView(nKnobP, LinearLayout.LayoutParams(32,32))
                    nCol.addView(nTxtP)
                    kRow.addView(nCol, LinearLayout.LayoutParams(0,-1,1f))

                    card.addView(kRow)

                    card.setOnClickListener {
                        applyPedal(pedal)
                        Toast.makeText(this,"✅ ${pedal.name} — NAKA-APLAY!",Toast.LENGTH_SHORT).show()
                        if(!AudioEngine.isRunning()) AudioEngine.start(this@MainActivity)
                    }

                    colLay.addView(card, LinearLayout.LayoutParams(-2, -2).apply { setMargins(0,2,0,2) })
                }

                colScroll.addView(colLay)
                rowContainer.addView(colScroll, LinearLayout.LayoutParams(0, -1, 1f))
            }
            cabPage.addView(rowContainer)
            pageContainer.addView(cabPage)
        }
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
        else if(currentPage > 0) { goToMainPage() }
        else Toast.makeText(this,"✅ UNAANG PAGE NA!",Toast.LENGTH_SHORT).show()
    }
    private fun goToMainPage() { currentPage=0; cabinetPageIndex=0; pageContainer.scrollTo(0,0) }

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

// ✅ PEDAL DATA — var = PWEDE BAGUHIN!
class PedalPreset(
    val name:String,
    val color:Int,
    var volume:Float,
    var effect:Float,
    var noiseGate:Float,
    var isOn:Boolean,
    val desc:String
)
