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

class MainActivity : AppCompatActivity() {
    private var currentPage = 0 // 0=Main Mixer, 1+=Cabinet Page
    private var cabinetPageIndex = 0
    private var startX = 0f
    private lateinit var mainPage: LinearLayout
    private lateinit var cabinetContainer: LinearLayout
    private lateinit var pageContainer: LinearLayout
    private lateinit var savePresetName: EditText
    private val knobViews = mutableListOf<Pair<KnobView, TextView>>()
    private val prefs by lazy { getSharedPreferences("GitaraPresets", Context.MODE_PRIVATE) }

    // ✅ LAHAT NG FX — 19 PIHITAN
    private val fxList = listOf(
        Triple("🚧 NOISE GATE", 0xFF44DD88.toInt(), { AudioMixer.noiseGate }),
        Triple("🎵 TONE", 0xFFFFCC44.toInt(), { AudioMixer.tone }),
        Triple("⚡ GAIN", 0xFFFFDD22.toInt(), { AudioMixer.gain }),
        Triple("🟠 OVERDRIVE", 0xFFFF9922.toInt(), { AudioMixer.overdrive }),
        Triple("🔴 DISTORTION", 0xFFFF4422.toInt(), { AudioMixer.distortion }),
        Triple("⚫ FUZZ", 0xFF664422.toInt(), { AudioMixer.fuzz }),
        Triple("🫧 CHORUS", 0xFF66AAFF.toInt(), { AudioMixer.chorus }),
        Triple("✨ FLANGER", 0xFFFF44DD.toInt(), { AudioMixer.flanger }),
        Triple("🌀 PHASER", 0xFFAA66FF.toInt(), { AudioMixer.phaser }),
        Triple("📳 TREMOLO", 0xFFFF6688.toInt(), { AudioMixer.tremolo }),
        Triple("🎶 VIBRATO", 0xFF88CCFF.toInt(), { AudioMixer.vibrato }),
        Triple("⏱️ DELAY", 0xFF8888FF.toInt(), { AudioMixer.delay }),
        Triple("🌊 REVERB", 0xFF44CCDD.toInt(), { AudioMixer.reverb }),
        Triple("🎵 WAH", 0xFFFF66AA.toInt(), { AudioMixer.wah }),
        Triple("🔊 AMP", 0xFFFFBB44.toInt(), { AudioMixer.ampType }),
        Triple("🔵 BASS", 0xFF4488FF.toInt(), { AudioMixer.bass }),
        Triple("🟡 MID", 0xFFFFCC00.toInt(), { AudioMixer.mid }),
        Triple("🟢 TREBLE", 0xFF44DD88.toInt(), { AudioMixer.treble }),
        Triple("🎚️ MASTER", 0xFFFFFFFF.toInt(), { AudioMixer.masterVolume })
    )

    // ✅ BUILT-IN PRESETS
    private fun getBuiltInPresets() = listOf(
        PresetData("Clean", 0xFF226644.toInt(), desc="Malinaw"),
        PresetData("Blues", 0xFF664422.toInt(), desc="Mainit"),
        PresetData("Rock", 0xFF992222.toInt(), desc="Matigas"),
        PresetData("Metal", 0xFF222222.toInt(), desc="Mabigat")
    )

    // ✅ NAKASAVE NA PRESETS NG USER
    private fun loadUserPresets(): MutableList<PresetData> {
        val list = mutableListOf<PresetData>()
        try {
            val json = prefs.getString("user_presets", "[]")
            val arr = JSONArray(json)
            for(i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(PresetData(
                    name = o.getString("name"),
                    color = o.optInt("color", 0xFF4488CC.toInt()),
                    desc = o.optString("desc", "Aking Preset")
                ))
            }
        } catch(_:Exception) {}
        return list
    }

    // ✅ I-SAVE ANG PRESET NG USER
    private fun saveCurrentAsPreset(name:String):Boolean {
        if(name.isBlank() || name in listOf("Clean","Blues","Rock","Metal")) return false
        val list = loadUserPresets()
        list.add(PresetData(name, pickColor(), "Aking Preset"))
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("user_presets", arr.toString()).apply()

        // ✅ I-SAVE ANG MGA HALAGA NG PIHITAN
        val e = prefs.edit()
        e.putFloat("preset_${name}_ng", AudioMixer.noiseGate)
        e.putFloat("preset_${name}_tone", AudioMixer.tone)
        e.putFloat("preset_${name}_gain", AudioMixer.gain)
        e.putFloat("preset_${name}_od", AudioMixer.overdrive)
        e.putFloat("preset_${name}_dist", AudioMixer.distortion)
        e.putFloat("preset_${name}_fuzz", AudioMixer.fuzz)
        e.putFloat("preset_${name}_chorus", AudioMixer.chorus)
        e.putFloat("preset_${name}_flange", AudioMixer.flanger)
        e.putFloat("preset_${name}_phaser", AudioMixer.phaser)
        e.putFloat("preset_${name}_trem", AudioMixer.tremolo)
        e.putFloat("preset_${name}_vib", AudioMixer.vibrato)
        e.putFloat("preset_${name}_delay", AudioMixer.delay)
        e.putFloat("preset_${name}_rev", AudioMixer.reverb)
        e.putFloat("preset_${name}_wah", AudioMixer.wah)
        e.putFloat("preset_${name}_amp", AudioMixer.ampType)
        e.putFloat("preset_${name}_bass", AudioMixer.bass)
        e.putFloat("preset_${name}_mid", AudioMixer.mid)
        e.putFloat("preset_${name}_treble", AudioMixer.treble)
        e.putFloat("preset_${name}_master", AudioMixer.masterVolume)
        e.apply()
        return true
    }

    private fun applyAnyPreset(name:String) {
        if(name in listOf("Clean","Blues","Rock","Metal")) {
            AudioMixer.applyPreset(name)
            return
        }
        val ng = prefs.getFloat("preset_${name}_ng", 0.5f)
        AudioMixer.noiseGate = ng
        AudioMixer.tone = prefs.getFloat("preset_${name}_tone", 0.5f)
        AudioMixer.gain = prefs.getFloat("preset_${name}_gain", 0.5f)
        AudioMixer.overdrive = prefs.getFloat("preset_${name}_od", 0.5f)
        AudioMixer.distortion = prefs.getFloat("preset_${name}_dist", 0.5f)
        AudioMixer.fuzz = prefs.getFloat("preset_${name}_fuzz", 0.5f)
        AudioMixer.chorus = prefs.getFloat("preset_${name}_chorus", 0.5f)
        AudioMixer.flanger = prefs.getFloat("preset_${name}_flange", 0.5f)
        AudioMixer.phaser = prefs.getFloat("preset_${name}_phaser", 0.5f)
        AudioMixer.tremolo = prefs.getFloat("preset_${name}_trem", 0.5f)
        AudioMixer.vibrato = prefs.getFloat("preset_${name}_vib", 0.5f)
        AudioMixer.delay = prefs.getFloat("preset_${name}_delay", 0.5f)
        AudioMixer.reverb = prefs.getFloat("preset_${name}_rev", 0.5f)
        AudioMixer.wah = prefs.getFloat("preset_${name}_wah", 0.5f)
        AudioMixer.ampType = prefs.getFloat("preset_${name}_amp", 0.5f)
        AudioMixer.bass = prefs.getFloat("preset_${name}_bass", 0.5f)
        AudioMixer.mid = prefs.getFloat("preset_${name}_mid", 0.5f)
        AudioMixer.treble = prefs.getFloat("preset_${name}_treble", 0.5f)
        AudioMixer.masterVolume = prefs.getFloat("preset_${name}_master", 0.5f)
    }

    private val colors = listOf(0xFF4488CC,0xFFCC4488,0xFF44CC88,0xFFCC8844,0xFF8844CC,0xFFCCCC44)
    private var colorIdx=0
    private fun pickColor():Int = colors[colorIdx++ % colors.size].toInt() and 0xFFFFFF or 0xFF000000.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        buildUI()
    }

    private fun buildUI() {
        val root = findViewById<LinearLayout>(R.id.rootLayout)
        root.setBackgroundColor(0xFF121212.toInt())

        pageContainer = LinearLayout(this)
        pageContainer.orientation = LinearLayout.HORIZONTAL
        pageContainer.layoutParams = LinearLayout.LayoutParams(-1,-1)

        buildMainPage()
        buildAllCabinetPages()

        root.addView(pageContainer)

        // ✅ SWIPE — LAHAT NG PAGE GUMAGANA!
        pageContainer.setOnTouchListener { _, e ->
            when(e.action) {
                MotionEvent.ACTION_DOWN -> startX = e.rawX
                MotionEvent.ACTION_MOVE -> {
                    val d = startX - e.rawX
                    if(d > 120) {
                        if(currentPage == 0) goToCabinetPage(0)
                        else goNextCabinetPage()
                    }
                    if(d < -120) {
                        if(currentPage > 0) goPrevCabinetPage()
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
        mainPage.layoutParams = LinearLayout.LayoutParams(w,-1)
        mainPage.setPadding(8,8,8,8)

        val t = TextView(this)
        t.text = "🎛️ MAIN MIXER PANEL"
        t.textSize = 20f; t.setTextColor(0xFFFFCC00.toInt())
        t.gravity = Gravity.CENTER; t.setPadding(0,8,0,4)
        mainPage.addView(t)

        val h = TextView(this)
        h.text = "👉 SWIPE PAKALIWA → PRESETS CABINET"
        h.textSize = 12f; h.setTextColor(0xFF888888.toInt())
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

        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.gravity = Gravity.CENTER
        bar.setBackgroundColor(0xFF220000.toInt())
        bar.setPadding(8,8,8,8)

        val masterBtn = Button(this)
        masterBtn.text="🟢 ON"; masterBtn.setTextColor(Color.WHITE)
        masterBtn.setBackgroundColor(0xFF228833.toInt()); masterBtn.textSize=13f
        masterBtn.setOnClickListener {
            val isOn = AudioMixer.isAllOn()
            AudioMixer.setAllOn(!isOn)
            if(isOn) { masterBtn.text="🔴 OFF"; masterBtn.setBackgroundColor(0xFF882222.toInt()) }
            else { masterBtn.text="🟢 ON"; masterBtn.setBackgroundColor(0xFF228833.toInt()); AudioEngine.start(this) }
        }
        bar.addView(masterBtn)

        savePresetName = EditText(this)
        savePresetName.hint="Pangalan Preset"
        savePresetName.setTextColor(Color.WHITE); savePresetName.setHintTextColor(0xFF888888.toInt())
        savePresetName.setBackgroundColor(0xFF333333.toInt()); savePresetName.setPadding(8,4,8,4)
        bar.addView(savePresetName, LinearLayout.LayoutParams(0,-1,1f).apply { setMargins(12,0,12,0) })

        val saveBtn = Button(this)
        saveBtn.text="💾 SAVE"; saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(0xFF226644.toInt()); saveBtn.textSize=12f
        saveBtn.setOnClickListener {
            val n = savePresetName.text.toString().trim()
            if(n.isBlank()) {
                Toast.makeText(this,"❌ Ilagay ang pangalan!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(saveCurrentAsPreset(n)) {
                Toast.makeText(this,"✅ NAISAVE: $n — NAKUHA NA SA CABINET!",Toast.LENGTH_SHORT).show()
                savePresetName.text.clear()
                pageContainer.removeViews(1, pageContainer.childCount-1)
                buildAllCabinetPages()
            } else Toast.makeText(this,"❌ Hindi pwede o may pangalan na!",Toast.LENGTH_SHORT).show()
        }
        bar.addView(saveBtn)
        mainPage.addView(bar)
        pageContainer.addView(mainPage)
    }

    private fun buildAllCabinetPages() {
        val allPresets = getBuiltInPresets() + loadUserPresets()
        val w = resources.displayMetrics.widthPixels
        val perPage = 2 // ✅ DALAWANG PRESET BAWAT HILERA/PAGE
        cabinetContainer = LinearLayout(this)
        cabinetContainer.orientation = LinearLayout.HORIZONTAL

        for(pageIdx in 0..allPresets.size step perPage) {
            val pagePresets = allPresets.drop(pageIdx).take(perPage)
            if(pagePresets.isEmpty()) break

            val cabPage = LinearLayout(this)
            cabPage.orientation = LinearLayout.VERTICAL
            cabPage.layoutParams = LinearLayout.LayoutParams(w,-1)
            cabPage.setBackgroundColor(0xFF1A1A1A.toInt())
            cabPage.setPadding(8,8,8,8)

            val topBar = LinearLayout(this)
            topBar.orientation = LinearLayout.HORIZONTAL
            topBar.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            val backBtn = Button(this)
            backBtn.text="⬅️"; backBtn.textSize=20f; backBtn.setTextColor(Color.WHITE)
            backBtn.setBackgroundColor(0xFF333333.toInt()); backBtn.setPadding(12,4,12,4)
            backBtn.setOnClickListener { goToMainPage() }
            topBar.addView(backBtn)

            val t = TextView(this)
            t.text = "📦 CABINET — PAGE ${(pageIdx/perPage)+1}"
            t.textSize=18f; t.setTextColor(0xFFCCCC66.toInt())
            t.gravity=Gravity.CENTER; t.setPadding(16,0,0,0)
            topBar.addView(t, LinearLayout.LayoutParams(0,-1,1f))
            cabPage.addView(topBar)

            val h = TextView(this)
            h.text = "👉 SWIPE PAKALIWA/PABALIK = LIPAT PAGE | ⬅️ = BALIK MAIN"
            h.textSize=11f; h.setTextColor(0xFF777777.toInt())
            h.gravity=Gravity.CENTER; h.setPadding(0,4,0,8)
            cabPage.addView(h)

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER
            row.setPadding(4,8,4,4)
            pagePresets.forEach { preset ->
                val pedal = LinearLayout(this)
                pedal.orientation = LinearLayout.VERTICAL
                pedal.setBackgroundColor(preset.color)
                pedal.setPadding(20,16,20,16)
                pedal.gravity = Gravity.CENTER
                pedal.setOnClickListener {
                    applyAnyPreset(preset.name)
                    updateKnobsFromPreset()
                    goToMainPage()
                    Toast.makeText(this,"✅ PRESET: ${preset.name} — NAAYOS LAHAT!",Toast.LENGTH_SHORT).show()
                    if(!AudioEngine.isRunning()) AudioEngine.start(this@MainActivity)
                }
                val lbl = TextView(this)
                lbl.text=preset.name; lbl.textSize=18f; lbl.setTextColor(Color.WHITE); lbl.gravity=Gravity.CENTER
                pedal.addView(lbl)
                val sub = TextView(this)
                sub.text=preset.desc; sub.textSize=10f; sub.setTextColor(0xAAFFFFFF.toInt()); sub.gravity=Gravity.CENTER
                pedal.addView(sub)
                row.addView(pedal, LinearLayout.LayoutParams(0,-1,1f).apply { setMargins(8,4,8,4) })
            }
            cabPage.addView(row)
            cabinetContainer.addView(cabPage)
        }
        pageContainer.addView(cabinetContainer)
    }

    private fun goToCabinetPage(idx:Int=0) { currentPage=1; cabinetPageIndex=idx; pageContainer.scrollTo(resources.displayMetrics.widthPixels*(1+idx),0) }
    private fun goNextCabinetPage() {
        val w = resources.displayMetrics.widthPixels
        val maxPage = cabinetContainer.childCount
        if(cabinetPageIndex+1 < maxPage) { cabinetPageIndex++; pageContainer.scrollTo(w*(1+cabinetPageIndex),0) }
    }
    private fun goPrevCabinetPage() {
        if(cabinetPageIndex>0) { cabinetPageIndex--; pageContainer.scrollTo(resources.displayMetrics.widthPixels*(1+cabinetPageIndex),0) }
        else if(currentPage>0) goToMainPage()
    }
    private fun goToMainPage() { currentPage=0; cabinetPageIndex=0; pageContainer.scrollTo(0,0) }

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

// ✅ Tulong para sa Preset Data
class PresetData(val name:String, val color:Int, val desc:String="") {
    fun toJson() = org.json.JSONObject().apply {
        put("name",name); put("color",color); put("desc",desc)
    }
}
