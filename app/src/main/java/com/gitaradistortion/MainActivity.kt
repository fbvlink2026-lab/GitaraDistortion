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
    private var cabinetOpen = false
    private var startX = 0f
    private lateinit var mainPanel: LinearLayout
    private lateinit var cabinetContainer: LinearLayout
    private lateinit var cabinetPanel: LinearLayout
    private val knobViews = mutableListOf<Pair<KnobView, TextView>>()

    // ✅ LAHAT NG FX — MIXER STYLE — MAY PANGALAN + KULAY
    private val fxList = listOf(
        Triple("🚧 NOISE GATE", 0xFF44DD88.toInt(), { AudioMixer.noiseGate }),
        Triple("🎵 TONE", 0xFFFFCC44.toInt(), { AudioMixer.tone }),
        Triple("⚡ GAIN", 0xFFFFDD22.toInt(), { AudioMixer.gain }),
        Triple("🟠 OVERDRIVE", 0xFFFF9922.toInt(), { AudioMixer.overdrive }),
        Triple("🔴 DISTORTION", 0xFFFF4422.toInt(), { AudioMixer.distortion }),
        Triple("⚫ FUZZ", 0xFF664422.toInt(), { AudioMixer.fuzz }),
        Triple("🫧 CHORUS", 0xFF66AAFF.toInt(), { AudioMixer.chorus }),
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

        // ✅ PAMAGAT
        val title = TextView(this)
        title.text = "🎸 GITARA FX — MIXER & PRESETS"
        title.textSize = 18f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0,8,0,4)
        root.addView(title)

        val hint = TextView(this)
        hint.text = "👆 PIHITIN ANG MIXER • SWIPE PAKALIWA = PRESETS CABINET"
        hint.textSize = 11f
        hint.setTextColor(0xFF888888.toInt())
        hint.gravity = Gravity.CENTER
        hint.setPadding(0,0,0,6)
        root.addView(hint)

        // ✅ DALAWANG PANEL — MAIN MIXER + PRESETS CABINET
        val twoPanels = LinearLayout(this)
        twoPanels.orientation = LinearLayout.HORIZONTAL
        twoPanels.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        )

        // ==========================================
        // 🎛️ MAIN PANEL — MIXER STYLE — LAHAT NG PIHITAN
        // ==========================================
        mainPanel = LinearLayout(this)
        mainPanel.orientation = LinearLayout.VERTICAL
        mainPanel.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3f)
        mainPanel.setBackgroundColor(0xFF121212.toInt())
        mainPanel.setPadding(4,4,4,4)

        val scroll = ScrollView(this)
        val grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL
        val perRow = 3
        for(row in fxList.indices step perRow) {
            val rowLay = LinearLayout(this)
            rowLay.orientation = LinearLayout.HORIZONTAL
            rowLay.gravity = Gravity.CENTER
            for(k in row until minOf(row+perRow, fxList.size)) {
                val (label, color, getVal) = fxList[k]
                val col = LinearLayout(this)
                col.orientation = LinearLayout.VERTICAL
                col.gravity = Gravity.CENTER
                col.setPadding(2,4,2,4)

                val knob = KnobView(this)
                knob.baseColor = color
                knob.value = getVal()
                val pct = TextView(this)
                pct.text = "${(knob.value*100).toInt()}%"
                pct.setTextColor(color)
                pct.textSize = 10f
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
                        7 -> AudioMixer.phaser = v
                        8 -> AudioMixer.tremolo = v
                        9 -> AudioMixer.vibrato = v
                        10 -> AudioMixer.delay = v
                        11 -> AudioMixer.reverb = v
                        12 -> AudioMixer.wah = v
                        13 -> AudioMixer.ampType = v
                        14 -> AudioMixer.bass = v
                        15 -> AudioMixer.mid = v
                        16 -> AudioMixer.treble = v
                        17 -> AudioMixer.masterVolume = v
                    }
                }
                col.addView(knob, LinearLayout.LayoutParams(72,72))
                col.addView(pct)
                val lbl = TextView(this)
                lbl.text = label
                lbl.setTextColor(color)
                lbl.textSize = 8f
                lbl.gravity = Gravity.CENTER
                col.addView(lbl)
                rowLay.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                knobViews.add(knob to pct)
            }
            grid.addView(rowLay)
        }
        scroll.addView(grid)
        mainPanel.addView(scroll)

        // ✅ PINAKAIBABA — ON/OFF LAHAT + SAVE PRESET
        val masterBar = LinearLayout(this)
        masterBar.orientation = LinearLayout.HORIZONTAL
        masterBar.gravity = Gravity.CENTER
        masterBar.setBackgroundColor(0xFF220000.toInt())
        masterBar.setPadding(6,6,6,6)
        masterBar.setPadding(4,8,4,8)

        val masterBtn = Button(this)
        masterBtn.text = "🟢 ON"
        masterBtn.setTextColor(Color.WHITE)
        masterBtn.setBackgroundColor(0xFF228833.toInt())
        masterBtn.textSize = 12f
        masterBtn.setPadding(12,6,12,6)
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
        saveName.hint = "Pangalan"
        saveName.setTextColor(Color.WHITE)
        saveName.setHintTextColor(0xFF888888.toInt())
        saveName.textSize = 11f
        saveName.setBackgroundColor(0xFF333333.toInt())
        saveName.setPadding(8,2,8,2)
        saveName.minWidth = 90
        saveName.gravity = Gravity.CENTER
        masterBar.addView(saveName, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(8,0,8,0) })

        val saveBtn = Button(this)
        saveBtn.text = "💾 SAVE"
        saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(0xFF226644.toInt())
        saveBtn.textSize = 11f
        saveBtn.setPadding(8,6,8,6)
        saveBtn.setOnClickListener {
            val name = saveName.text.toString().trim()
            if(name.isEmpty()) {
                Toast.makeText(this, "❌ Ilagay ang pangalan ng Preset!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AudioMixer.savePreset(this, name)
            Toast.makeText(this, "✅ NAISAVE: $name — Nasa Cabinet na!", Toast.LENGTH_SHORT).show()
            saveName.text.clear()
        }
        masterBar.addView(saveBtn)
        mainPanel.addView(masterBar)

        // ==========================================
        // 📦 CABINET — PEDAL STYLE KATULAD NG TONEBRIDGE
        // ==========================================
        cabinetContainer = LinearLayout(this)
        cabinetContainer.orientation = LinearLayout.HORIZONTAL
        cabinetContainer.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f)
        cabinetContainer.visibility = View.GONE
        cabinetPanel = LinearLayout(this)
        cabinetPanel.orientation = LinearLayout.VERTICAL
        cabinetPanel.setBackgroundColor(0xFF1A1A1A.toInt())
        cabinetPanel.setPadding(8,8,8,8)

        val cabTitle = TextView(this)
        cabTitle.text = "📦 PRESETS — PILIIN ANG TUNOG"
        cabTitle.textSize = 13f
        cabTitle.setTextColor(0xFFCCCC66.toInt())
        cabTitle.gravity = Gravity.CENTER
        cabTitle.setPadding(0,8,0,12)
        cabinetPanel.addView(cabTitle)

        val cabScroll = ScrollView(this)
        val cabList = LinearLayout(this)
        cabList.orientation = LinearLayout.VERTICAL

        // ✅ MGA PRESET — PEDAL STYLE! KATULAD NG TONEBRIDGE!
        listOf(
            Triple("Clean", 0xFF226644.toInt(), "Malinaw na tunog"),
            Triple("Blues", 0xFF664422.toInt(), "Mainit na tono"),
            Triple("Rock", 0xFF992222.toInt(), "Matigas na tunog"),
            Triple("Metal", 0xFF222222.toInt(), "Mabigat na distorsyon")
        ).forEach { (name, color, desc) ->
            val pedal = LinearLayout(this)
            pedal.orientation = LinearLayout.VERTICAL
            pedal.setBackgroundColor(color)
            pedal.setPadding(12,12,12,12)
            pedal.gravity = Gravity.CENTER
            pedal.setPadding(0,4,0,4)
            pedal.setOnClickListener {
                AudioMixer.applyPreset(name)
                updateKnobsFromPreset()
                closeCabinet()
                Toast.makeText(this, "✅ PRESET: $name — KUSANG NAAYOS LAHAT NG PIHITAN!", Toast.LENGTH_SHORT).show()
                if(!AudioEngine.isRunning()) AudioEngine.start(this)
            }

            val lbl = TextView(this)
            lbl.text = name
            lbl.textSize = 16f
            lbl.setTextColor(Color.WHITE)
            lbl.gravity = Gravity.CENTER
            lbl.setPadding(0,4,0,2)
            pedal.addView(lbl)

            val sub = TextView(this)
            sub.text = desc
            sub.textSize = 10f
            sub.setTextColor(0xAAFFFFFF.toInt())
            sub.gravity = Gravity.CENTER
            pedal.addView(sub)

            cabList.addView(pedal, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0,4,0,4) })
        }
        cabScroll.addView(cabList)
        cabinetPanel.addView(cabScroll)
        cabinetContainer.addView(cabinetPanel)

        // ✅ ILAGAY LAHAT SA SCREEN
        twoPanels.addView(mainPanel)
        twoPanels.addView(cabinetContainer)
        root.addView(twoPanels)

        // ✅ SWIPE PAKALIWA = BUKAS ANG CABINET
        twoPanels.setOnTouchListener { _, e ->
            when(e.action) {
                MotionEvent.ACTION_DOWN -> startX = e.rawX
                MotionEvent.ACTION_MOVE -> {
                    val d = startX - e.rawX
                    if(d > 100 && !cabinetOpen) openCabinet()
                    if(d < -100 && cabinetOpen) closeCabinet()
                }
            }
            true
        }
    }

    // ✅ KUSANG AYUSIN ANG LAHAT NG PIHITAN MULA SA PRESET!
    private fun updateKnobsFromPreset() {
        val vals = floatArrayOf(
            AudioMixer.noiseGate, AudioMixer.tone, AudioMixer.gain,
            AudioMixer.overdrive, AudioMixer.distortion, AudioMixer.fuzz,
            AudioMixer.chorus, AudioMixer.phaser, AudioMixer.tremolo,
            AudioMixer.vibrato, AudioMixer.delay, AudioMixer.reverb,
            AudioMixer.wah, AudioMixer.ampType, AudioMixer.bass,
            AudioMixer.mid, AudioMixer.treble, AudioMixer.masterVolume
        )
        for(i in knobViews.indices) {
            val (knob, pct) = knobViews[i]
            knob.value = vals[i]
            pct.text = "${(vals[i]*100).toInt()}%"
        }
    }

    private fun openCabinet() {
        cabinetOpen = true
        mainPanel.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.5f)
        cabinetContainer.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.5f)
        cabinetContainer.visibility = View.VISIBLE
    }
    private fun closeCabinet() {
        cabinetOpen = false
        mainPanel.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3f)
        cabinetContainer.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f)
        cabinetContainer.visibility = View.GONE
    }

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Handa na! Pihitin o pumili ng Preset!", Toast.LENGTH_LONG).show()
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
