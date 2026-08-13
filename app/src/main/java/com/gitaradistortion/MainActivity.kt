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
    private val PEDALS_PER_PAGE = 20
    private val KNOB_COUNT = 3

    private val fxList = listOf(
        { AudioMixer.overdrive }, { AudioMixer.distortion }, { AudioMixer.fuzz },
        { AudioMixer.chorus }, { AudioMixer.flanger }, { AudioMixer.phaser },
        { AudioMixer.tremolo }, { AudioMixer.vibrato }, { AudioMixer.delay },
        { AudioMixer.reverb }, { AudioMixer.wah }, { AudioMixer.gain },
        { AudioMixer.tone }, { AudioMixer.bass }, { AudioMixer.mid }, { AudioMixer.treble }
    )

    private fun getBuiltInPresets() = listOf(
        PedalPreset("Clean", -0x3399BB, vol=0.75f, fx=0.20f, ng=0.05f, isOn=true, desc="Malinaw"),
        PedalPreset("Blues", -0x99BB3D, vol=0.80f, fx=0.45f, ng=0.08f, isOn=true, desc="Mainit"),
        PedalPreset("Rock", -0x66DDDD, vol=0.85f, fx=0.70f, ng=0.12f, isOn=true, desc="Matigas"),
        PedalPreset("Metal", -0xDDDDDD, vol=0.90f, fx=0.95f, ng=0.20f, isOn=true, desc="Mabigat")
    )

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

    private fun saveAllPresets(list:List<PedalPreset>) {
        val arr = JSONArray()
        list.forEach { p ->
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

    private fun applyPedalToMain(p:PedalPreset) {
        if(!p.isOn) {
            AudioMixer.masterVolume = 0.05f
            AudioMixer.noiseGate = 0.01f
            fxList.forEach { getFx -> if(getFx() > 0.01f) setFx(getFx(), 0f) }
            return
        }
        AudioMixer.masterVolume = p.volume
        AudioMixer.noiseGate = p.noiseGate
        fxList.forEach { getFx ->
            val current = getFx()
            if(current > 0.01f) setFx(getFx(), p.effect)
        }
        if(!AudioEngine.isRunning()) AudioEngine.start(this)
    }

    private fun setFx(get:()->Float, v:Float) {
        when {
            get() === AudioMixer.overdrive -> AudioMixer.overdrive = v
            get() === AudioMixer.distortion -> AudioMixer.distortion = v
            get() === AudioMixer.fuzz -> AudioMixer.fuzz = v
            get() === AudioMixer.chorus -> AudioMixer.chorus = v
            get() === AudioMixer.flanger -> AudioMixer.flanger = v
            get() === AudioMixer.phaser -> AudioMixer.phaser = v
            get() === AudioMixer.tremolo -> AudioMixer.tremolo = v
            get() === AudioMixer.vibrato -> AudioMixer.vibrato = v
            get() === AudioMixer.delay -> AudioMixer.delay = v
            get() === AudioMixer.reverb -> AudioMixer.reverb = v
            get() === AudioMixer.wah -> AudioMixer.wah = v
            get() === AudioMixer.gain -> AudioMixer.gain = v
            get() === AudioMixer.tone -> AudioMixer.tone = v
            get() === AudioMixer.bass -> AudioMixer.bass = v
            get() === AudioMixer.mid -> AudioMixer.mid = v
            get() === AudioMixer.treble -> AudioMixer.treble = v
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
        root.setBackgroundColor(-0xEDF0EF)

        pageContainer = LinearLayout(this)
        pageContainer.orientation = LinearLayout.HORIZONTAL
        pageContainer.layoutParams = LinearLayout.LayoutParams(-1, -1)

        buildMainPage()
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

    private fun buildMainPage() {
        val w = resources.displayMetrics.widthPixels
        val mainPage = LinearLayout(this)
        mainPage.orientation = LinearLayout.VERTICAL
        mainPage.layoutParams = LinearLayout.LayoutParams(w, -1)
        mainPage.setPadding(8,8,8,8)

        val title = TextView(this)
        title.text = "🎛️ MAIN PEDAL BOARD"
        title.textSize = 20f; title.setTextColor(-0x0033BC)
        title.gravity = Gravity.CENTER; title.setPadding(0,8,0,4)
        mainPage.addView(title)

        val hint = TextView(this)
        hint.text = "👉 SWIPE PAKALIWA → CABINET (20 Max/Page)"
        hint.textSize = 12f; hint.setTextColor(-0x777778)
        hint.gravity = Gravity.CENTER; hint.setPadding(0,0,0,8)
        mainPage.addView(hint)

        val saveBox = LinearLayout(this)
        saveBox.orientation = LinearLayout.VERTICAL
        saveBox.setBackgroundColor(-0xE5E5E6)
        saveBox.setPadding(12,12,12,12)

        val saveTitle = TextView(this)
        saveTitle.text = "💾 I-SAVE BAGONG PEDAL"
        saveTitle.textSize = 15f; saveTitle.setTextColor(-0x0033BC)
        saveTitle.setPadding(0,0,0,8)
        saveBox.addView(saveTitle)

        savePresetName = EditText(this)
        savePresetName.hint = "Pangalan ng Pedal"
        savePresetName.setTextColor(Color.WHITE)
        savePresetName.setHintTextColor(-0x888889)
        savePresetName.setBackgroundColor(-0xDDDDDE)
        savePresetName.setPadding(10,6,10,6)
        saveBox.addView(savePresetName)

        val saveKnobRow = LinearLayout(this)
        saveKnobRow.orientation = LinearLayout.HORIZONTAL
        saveKnobRow.gravity = Gravity.CENTER
        val vKnob = KnobView(this).apply { baseColor=-0x00BB55; value=0.75f }
        val fKnob = KnobView(this).apply { baseColor=-0xBB5500; value=0.50f }
        val nKnob = KnobView(this).apply { baseColor=-0x555555; value=0.05f }
        val vTxt = TextView(this).apply { text="75%"; setTextColor(-0x00BB55); textSize=10f }
        val fTxt = TextView(this).apply { text="50%"; setTextColor(-0xBB5500); textSize=10f }
        val nTxt = TextView(this).apply { text="5%"; setTextColor(-0x555555); textSize=10f }
        vKnob.onChange = { vTxt.text="${(it*100).toInt()}%" }
        fKnob.onChange = { fTxt.text="${(it*100).toInt()}%" }
        nKnob.onChange = { nTxt.text="${(it*100).toInt()}%" }
        listOf("🔊 VOLUME" to vKnob to vTxt, "⚡ EFFECT" to fKnob to fTxt, "🚧 NOISE GATE" to nKnob to nTxt).forEach { (lbl, k, t) ->
            val col = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER }
            col.addView(k, LinearLayout.LayoutParams(60,60))
            col.addView(t)
            val l = TextView(this).apply { text=lbl; textSize=9f; setTextColor(-0xAAAAAB); gravity=Gravity.CENTER }
            col.addView(l)
            saveKnobRow.addView(col, LinearLayout.LayoutParams(0,-1,1f))
        }
        saveBox.addView(saveKnobRow)

        val saveBtn = Button(this)
        saveBtn.text = "💾 I-SAVE BAGONG PEDAL"
        saveBtn.setBackgroundColor(-0x339933)
        saveBtn.setTextColor(Color.WHITE)
        saveBtn.setOnClickListener {
            val nm = savePresetName.text.toString().trim()
            if(nm.isBlank()) {
                Toast.makeText(this,"❌ Ilagay ang pangalan!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val allPresets = getBuiltInPresets() + loadUserPresets()
            val pos = allPresets.size % PEDALS_PER_PAGE
            val pageNum = (allPresets.size / PEDALS_PER_PAGE) + 1
            if(saveNewPreset(nm, vKnob.value, fKnob.value, nKnob.value)) {
                Toast.makeText(this,"✅ NAISAVE! PAGE $pageNum — PEDAL #${pos+1}",Toast.LENGTH_LONG).show()
                savePresetName.text.clear()
                pageContainer.removeViews(1, pageContainer.childCount-1)
                buildAllCabinetPages()
            } else Toast.makeText(this,"❌ Pangalan na gamit o blangko!",Toast.LENGTH_SHORT).show()
        }
        saveBox.addView(saveBtn)
        mainPage.addView(saveBox)

        val tip = TextView(this)
        tip.text = "\n📌 CABINET: $PEDALS_PER_PAGE Max/Page • 10 Kaliwa + 10 Kanan\n📌 Pinihit sa Pedal → AGAD NAI-SAVE + SUMASABAY SA MAIN!\n📌 VOLUME=MASTER • EFFECT=LAHAT NG FX • NOISE GATE=NOISE GATE"
        tip.textSize = 12f; tip.setTextColor(-0x777778)
        tip.setPadding(12,12,12,12)
        mainPage.addView(tip)

        pageContainer.addView(mainPage)
    }

    private fun buildAllCabinetPages() {
        val builtIn = getBuiltInPresets()
        val user = loadUserPresets()
        val allPresets = builtIn + user
        val totalPages = (allPresets.size + PEDALS_PER_PAGE - 1) / PEDALS_PER_PAGE
        val scrW = resources.displayMetrics.widthPixels

        for(pageIdx in 0 until totalPages) {
            val start = pageIdx * PEDALS_PER_PAGE
            val end = minOf(start + PEDALS_PER_PAGE, allPresets.size)
            val pagePedals = allPresets.subList(start, end)

            val cabPage = LinearLayout(this)
            cabPage.orientation = LinearLayout.VERTICAL
            cabPage.layoutParams = LinearLayout.LayoutParams(scrW, -1)
            cabPage.setBackgroundColor(-0xE5E5E6)
            cabPage.setPadding(6,6,6,6)

            val topBar = LinearLayout(this)
            topBar.orientation = LinearLayout.HORIZONTAL
            topBar.gravity = Gravity.CENTER_VERTICAL
            topBar.setPadding(4,4,4,4)

            val backBtn = Button(this)
            backBtn.text = "⬅️ BALIK"
            backBtn.textSize = 13f
            backBtn.setTextColor(Color.WHITE)
            backBtn.setBackgroundColor(-0xDD3333)
            backBtn.setPadding(12,6,12,6)
            backBtn.setOnClickListener { goToMainPage() }
            topBar.addView(backBtn)

            val pageTitle = TextView(this)
            pageTitle.text = "📦 CABINET — PAGE ${pageIdx+1} / $totalPages"
            pageTitle.textSize = 15f
            pageTitle.setTextColor(-0x0033BC)
            pageTitle.setPadding(12,0,0,0)
            topBar.addView(pageTitle, LinearLayout.LayoutParams(0,-1,1f))
            cabPage.addView(topBar)

            val info = TextView(this)
            info.text = "📌 ${pagePedals.size}/$PEDALS_PER_PAGE • 10 Kaliwa + 10 Kanan • AWATOMATIKONG SAVE"
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

                pedals.forEachIndexed { idx, pedal ->
                    val card = LinearLayout(this)
                    card.orientation = LinearLayout.VERTICAL
                    card.setBackgroundColor(pedal.color)
                    card.setPadding(8,6,8,6)
                    card.gravity = Gravity.CENTER

                    val power = Button(this)
                    power.text = if(pedal.isOn) "💡 ON" else "⚫ OFF"
                    power.textSize = 11f
                    power.setTextColor(Color.WHITE)
                    power.setBackgroundColor(if(pedal.isOn) -0x009933 else -0x555555)
                    power.setPadding(8,2,8,2)
                    power.setOnClickListener {
                        pedal.isOn = !pedal.isOn
                        power.text = if(pedal.isOn) "💡 ON" else "⚫ OFF"
                        power.setBackgroundColor(if(pedal.isOn) -0x009933 else -0x555555)
                        applyPedalToMain(pedal)
                        saveAllPresets(builtIn + user)
                        Toast.makeText(this@MainActivity, "✅ ${pedal.name}: ${if(pedal.isOn) "💡 BUHAY" else "⚫ PATAY"}", Toast.LENGTH_SHORT).show()
                    }
                    card.addView(power)

                    val name = TextView(this)
                    name.text = pedal.name
                    name.textSize = 14f
                    name.setTextColor(Color.WHITE)
                    name.gravity = Gravity.CENTER
                    name.setPadding(0,2,0,2)
                    card.addView(name)

                    val kRow = LinearLayout(this)
                    kRow.orientation = LinearLayout.HORIZONTAL
                    kRow.gravity = Gravity.CENTER

                    val vKnob = KnobView(this).apply { baseColor=-0x00DD55; value=pedal.volume }
                    val vTxt = TextView(this).apply { text="${(pedal.volume*100).toInt()}%"; textSize=8f; setTextColor(-0x00DD55) }
                    vKnob.onChange = { newVal ->
                        pedal.volume = newVal
                        vTxt.text = "${(newVal*100).toInt()}%"
                        AudioMixer.masterVolume = newVal
                        saveAllPresets(builtIn + user)
                    }
                    val vBox = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER }
                    vBox.addView(vKnob, LinearLayout.LayoutParams(36,36))
                    vBox.addView(vTxt)
                    kRow.addView(vBox, LinearLayout.LayoutParams(0,-1,1f))

                    val fKnob = KnobView(this).apply { baseColor=-0xFF8800; value=pedal.effect }
                    val fTxt = TextView(this).apply { text="${(pedal.effect*100).toInt()}%"; textSize=8f; setTextColor(-0xFF8800) }
                    fKnob.onChange = { newVal ->
                        pedal.effect = newVal
                        fTxt.text = "${(newVal*100).toInt()}%"
                        fxList.forEach { getFx -> if(getFx() > 0.01f) setFx(getFx(), newVal) }
                        saveAllPresets(builtIn + user)
                    }
                    val fBox = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER }
                    fBox.addView(fKnob, LinearLayout.LayoutParams(36,36))
                    fBox.addView(fTxt)
                    kRow.addView(fBox, LinearLayout.LayoutParams(0,-1,1f))

                    val nKnob = KnobView(this).apply { baseColor=-0x777777; value=pedal.noiseGate }
                    val nTxt = TextView(this).apply { text="${(pedal.noiseGate*100).toInt()}%"; textSize=8f; setTextColor(-0x777777) }
                    nKnob.onChange = { newVal ->
                        pedal.noiseGate = newVal
                        nTxt.text = "${(newVal*100).toInt()}%"
                        AudioMixer.noiseGate = newVal
                        saveAllPresets(builtIn + user)
                    }
                    val nBox = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER }
                    nBox.addView(nKnob, LinearLayout.LayoutParams(36,36))
                    nBox.addView(nTxt)
                    kRow.addView(nBox, LinearLayout.LayoutParams(0,-1,1f))

                    card.addView(kRow)
                    card.setOnClickListener { applyPedalToMain(pedal) }
                    colLay.addView(card, LinearLayout.LayoutParams(-2, -2).apply { setMargins(0,4,0,4) })
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

class PedalPreset(
    val name:String,
    val color:Int,
    var volume:Float,
    var effect:Float,
    var noiseGate:Float,
    var isOn:Boolean,
    val desc:String
)
