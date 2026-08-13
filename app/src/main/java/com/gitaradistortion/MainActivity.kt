package com.gitaradistortion

import android.Manifest
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

class MainActivity : AppCompatActivity() {
    private var currentPage = 0 // 0 = Main Mixer, 1 = Cabinet
    private var startX = 0f
    private lateinit var mainPage: LinearLayout
    private lateinit var cabinetPage: LinearLayout
    private lateinit var pageContainer: LinearLayout
    private val knobViews = mutableListOf<Pair<KnobView, TextView>>()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        buildUI()
    }

    private fun buildUI() {
        val root = findViewById<LinearLayout>(R.id.rootLayout)
        root.setBackgroundColor(0xFF121212.toInt())

        // ✅ PAGE CONTAINER — DALAWANG PAGE MAGKATABI
        pageContainer = LinearLayout(this)
        pageContainer.orientation = LinearLayout.HORIZONTAL
        pageContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        // ==========================================
        // 🎛️ PAGE 1: MAIN MIXER PANEL
        // ==========================================
        mainPage = LinearLayout(this)
        mainPage.orientation = LinearLayout.VERTICAL
        mainPage.layoutParams = LinearLayout.LayoutParams(
            resources.displayMetrics.widthPixels,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        mainPage.setPadding(8,8,8,8)

        val mainTitle = TextView(this)
        mainTitle.text = "🎛️ MAIN MIXER PANEL"
        mainTitle.textSize = 20f
        mainTitle.setTextColor(0xFFFFCC00.toInt())
        mainTitle.gravity = Gravity.CENTER
        mainTitle.setPadding(0,8,0,4)
        mainPage.addView(mainTitle)

        val mainHint = TextView(this)
        mainHint.text = "👉 SWIPE PAKALIWA → PUMUNTA SA PRESETS CABINET"
        mainHint.textSize = 12f
        mainHint.setTextColor(0xFF888888.toInt())
        mainHint.gravity = Gravity.CENTER
        mainHint.setPadding(0,0,0,8)
        mainPage.addView(mainHint)

        val scroll = ScrollView(this)
        val grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL
        val perRow = 3
        for(row in fxList.indices step perRow) {
            val rowLay = LinearLayout(this)
            rowLay.orientation = LinearLayout.HORIZONTAL
            rowLay.gravity = Gravity.CENTER
            rowLay.setPadding(2,4,2,4)
            for(k in row until minOf(row+perRow, fxList.size)) {
                val (label, color, getVal) = fxList[k]
                val col = LinearLayout(this)
                col.orientation = LinearLayout.VERTICAL
                col.gravity = Gravity.CENTER
                col.setPadding(4,4,4,4)

                val knob = KnobView(this)
                knob.baseColor = color
                knob.value = getVal()
                val pct = TextView(this)
                pct.text = "${(knob.value*100).toInt()}%"
                pct.setTextColor(color)
                pct.textSize = 11f
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
                lbl.text = label
                lbl.setTextColor(color)
                lbl.textSize = 9f
                lbl.gravity = Gravity.CENTER
                col.addView(lbl)
                rowLay.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                knobViews.add(knob to pct)
            }
            grid.addView(rowLay)
        }
        scroll.addView(grid)
        mainPage.addView(scroll)

        // ✅ MASTER BAR — ITAAS SA MAIN PAGE
        val masterBar = LinearLayout(this)
        masterBar.orientation = LinearLayout.HORIZONTAL
        masterBar.gravity = Gravity.CENTER
        masterBar.setBackgroundColor(0xFF220000.toInt())
        masterBar.setPadding(8,8,8,8)

        val masterBtn = Button(this)
        masterBtn.text = "🟢 ON"
        masterBtn.setTextColor(Color.WHITE)
        masterBtn.setBackgroundColor(0xFF228833.toInt())
        masterBtn.textSize = 13f
        masterBtn.setPadding(16,8,16,8)
        masterBtn.setOnClickListener {
            val isOn = AudioMixer.isAllOn()
            AudioMixer.setAllOn(!isOn)
            if(isOn) {
                masterBtn.text = "🔴 OFF"
                masterBtn.setBackgroundColor(0xFF882222.toInt())
            } else {
                masterBtn.text = "🟢 ON"
                masterBtn.setBackgroundColor(0xFF228833.toInt())
                if(!AudioEngine.isRunning()) AudioEngine.start(this)
            }
        }
        masterBar.addView(masterBtn)

        val saveName = EditText(this)
        saveName.hint = "Pangalan Preset"
        saveName.setTextColor(Color.WHITE)
        saveName.setHintTextColor(0xFF888888.toInt())
        saveName.textSize = 12f
        saveName.setBackgroundColor(0xFF333333.toInt())
        saveName.setPadding(8,4,8,4)
        saveName.minWidth = 100
        saveName.gravity = Gravity.CENTER
        masterBar.addView(saveName, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(12,0,12,0) })

        val saveBtn = Button(this)
        saveBtn.text = "💾 SAVE"
        saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(0xFF226644.toInt())
        saveBtn.textSize = 12f
        saveBtn.setPadding(12,8,12,8)
        saveBtn.setOnClickListener {
            val name = saveName.text.toString().trim()
            if(name.isEmpty()) {
                Toast.makeText(this, "❌ Ilagay ang pangalan ng Preset!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AudioMixer.savePreset(this, name)
            Toast.makeText(this, "✅ NAISAVE: $name!", Toast.LENGTH_SHORT).show()
            saveName.text.clear()
        }
        masterBar.addView(saveBtn)
        mainPage.addView(masterBar)

        // ==========================================
        // 📦 PAGE 2: CABINET PANEL — PEDALS HILERA PAHABA!
        // ==========================================
        cabinetPage = LinearLayout(this)
        cabinetPage.orientation = LinearLayout.VERTICAL
        cabinetPage.layoutParams = LinearLayout.LayoutParams(
            resources.displayMetrics.widthPixels,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        cabinetPage.setBackgroundColor(0xFF1A1A1A.toInt())
        cabinetPage.setPadding(8,8,8,8)

        // ✅ ⬅️ ARROW SA ITAAS KALIWA — BUMALIK SA MAIN!
        val topBar = LinearLayout(this)
        topBar.orientation = LinearLayout.HORIZONTAL
        topBar.gravity = Gravity.START or Gravity.CENTER_VERTICAL
        topBar.setPadding(4,4,4,12)

        val backArrow = Button(this)
        backArrow.text = "⬅️"
        backArrow.textSize = 20f
        backArrow.setTextColor(Color.WHITE)
        backArrow.setBackgroundColor(0xFF333333.toInt())
        backArrow.setPadding(12,4,12,4)
        backArrow.setOnClickListener { goToMainPage() }
        topBar.addView(backArrow)

        val cabTitle = TextView(this)
        cabTitle.text = "📦 PRESETS CABINET"
        cabTitle.textSize = 20f
        cabTitle.setTextColor(0xFFCCCC66.toInt())
        cabTitle.gravity = Gravity.CENTER
        cabTitle.setPadding(16,0,0,0)
        topBar.addView(cabTitle, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        cabinetPage.addView(topBar)

        val cabHint = TextView(this)
        cabHint.text = "👉 PUMILI NG PRESET → KUSANG AYUS LAHAT NG PIHITAN!"
        cabHint.textSize = 12f
        cabHint.setTextColor(0xFF888888.toInt())
        cabHint.gravity = Gravity.CENTER
        cabHint.setPadding(0,0,0,12)
        cabinetPage.addView(cabHint)

        // ✅ PRESET PEDALS — HILERA PAHABA! MAGKAKATABI!
        val cabScroll = ScrollView(this)
        val pedalRow = LinearLayout(this)
        pedalRow.orientation = LinearLayout.HORIZONTAL
        pedalRow.gravity = Gravity.CENTER
        pedalRow.setPadding(4,4,4,4)

        listOf(
            Triple("Clean", 0xFF226644.toInt(), "Malinaw"),
            Triple("Blues", 0xFF664422.toInt(), "Mainit"),
            Triple("Rock", 0xFF992222.toInt(), "Matigas"),
            Triple("Metal", 0xFF222222.toInt(), "Mabigat")
        ).forEach { (name, color, desc) ->
            val pedal = LinearLayout(this)
            pedal.orientation = LinearLayout.VERTICAL
            pedal.setBackgroundColor(color)
            pedal.setPadding(16,12,16,12)
            pedal.gravity = Gravity.CENTER
            pedal.setPadding(8,12,8,12)
            pedal.setOnClickListener {
                AudioMixer.applyPreset(name)
                updateKnobsFromPreset()
                goToMainPage()
                Toast.makeText(this, "✅ PRESET: $name — NAAYOS LAHAT!", Toast.LENGTH_SHORT).show()
                if(!AudioEngine.isRunning()) AudioEngine.start(this)
            }

            val lbl = TextView(this)
            lbl.text = name
            lbl.textSize = 15f
            lbl.setTextColor(Color.WHITE)
            lbl.gravity = Gravity.CENTER
            lbl.setPadding(0,4,0,2)
            pedal.addView(lbl)

            val sub = TextView(this)
            sub.text = desc
            sub.textSize = 9f
            sub.setTextColor(0xAAFFFFFF.toInt())
            sub.gravity = Gravity.CENTER
            pedal.addView(sub)

            pedalRow.addView(pedal, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(6,4,6,4) })
        }
        cabScroll.addView(pedalRow)
        cabinetPage.addView(cabScroll)

        // ✅ ILAGAY DALAWANG PAGE SA CONTAINER
        pageContainer.addView(mainPage)
        pageContainer.addView(cabinetPage)
        root.addView(pageContainer)

        // ✅ SWIPE PAKALIWA = PUMUNTA SA CABINET
        pageContainer.setOnTouchListener { _, e ->
            when(e.action) {
                MotionEvent.ACTION_DOWN -> startX = e.rawX
                MotionEvent.ACTION_MOVE -> {
                    val d = startX - e.rawX
                    if(d > 120 && currentPage == 0) goToCabinetPage()
                }
            }
            true
        }
    }

    private fun goToCabinetPage() {
        currentPage = 1
        pageContainer.scrollTo(resources.displayMetrics.widthPixels, 0)
    }
    private fun goToMainPage() {
        currentPage = 0
        pageContainer.scrollTo(0, 0)
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

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Handa na! Swipe pakaliwa para sa Presets!", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }
    override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray) {
        super.onRequestPermissionsResult(r,p,g)
        if(r==123 && g.firstOrNull()==PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Pahintulot natanggap!", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroy() { super.onDestroy(); AudioEngine.stop() }
}
