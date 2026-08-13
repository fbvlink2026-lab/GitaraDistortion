package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private val knobBindings = mutableListOf<Pair<KnobView, TextView>>()

    private val fxList = listOf(
        Triple("🚧 NOISE GATE", 0xFF44DD88.toInt(), { v: Float -> AudioMixer.noiseGate = v }),
        Triple("🎵 TONE", 0xFFFFCC44.toInt(), { v -> AudioMixer.tone = v }),
        Triple("⚡ GAIN", 0xFFFFDD22.toInt(), { v -> AudioMixer.gain = v }),
        Triple("🟠 OVERDRIVE", 0xFFFF9922.toInt(), { v -> AudioMixer.overdrive = v }),
        Triple("🔴 DISTORTION", 0xFFFF4422.toInt(), { v -> AudioMixer.distortion = v }),
        Triple("⚫ FUZZ", 0xFF664422.toInt(), { v -> AudioMixer.fuzz = v }),
        Triple("🫧 CHORUS", 0xFF66AAFF.toInt(), { v -> AudioMixer.chorus = v }),
        Triple("🌀 PHASER", 0xFFAA66FF.toInt(), { v -> AudioMixer.phaser = v }),
        Triple("📳 TREMOLO", 0xFFFF6688.toInt(), { v -> AudioMixer.tremolo = v }),
        Triple("🎶 VIBRATO", 0xFF88CCFF.toInt(), { v -> AudioMixer.vibrato = v }),
        Triple("⏱️ DELAY", 0xFF8888FF.toInt(), { v -> AudioMixer.delay = v }),
        Triple("🌊 REVERB", 0xFF44CCDD.toInt(), { v -> AudioMixer.reverb = v }),
        Triple("🎵 WAH", 0xFFFF66AA.toInt(), { v -> AudioMixer.wah = v }),
        Triple("🔊 AMP", 0xFFFFBB44.toInt(), { v -> AudioMixer.ampType = v }),
        Triple("🔵 BASS", 0xFF4488FF.toInt(), { v -> AudioMixer.bass = v }),
        Triple("🟡 MID", 0xFFFFCC00.toInt(), { v -> AudioMixer.mid = v }),
        Triple("🟢 TREBLE", 0xFF44DD88.toInt(), { v -> AudioMixer.treble = v }),
        Triple("🎚️ MASTER", 0xFFFFFFFF.toInt(), { v -> AudioMixer.masterVolume = v })
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AudioMixer.init(this)
        setupViewPager()
        
        // ✅ HUWAG HINGIN ANG PAHINTULOT DITO! MAGSISARA ANG APP!
        // ✅ HINGIN LANG KAPAG PININDOT NA ANG PINDUTAN!
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        val screens = listOf(buildMixerScreen(), buildCabinetScreen())
        viewPager.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun getItemCount() = 2
            override fun onCreateViewHolder(p: ViewGroup, t: Int) =
                object : androidx.recyclerview.widget.RecyclerView.ViewHolder(screens[t]) {}
            override fun onBindViewHolder(h: androidx.recyclerview.widget.RecyclerView.ViewHolder, p: Int) {}
        }
    }

    private fun buildMixerScreen(): LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())

        val title = TextView(this)
        title.text = "🎛️ GITARA FX — SWIPE PAKALIWA → PRESETS"
        title.textSize = 14f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 16, 0, 8)
        root.addView(title)

        val scroll = ScrollView(this)
        val grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL
        val perRow = 3
        knobBindings.clear()
        for (row in fxList.indices step perRow) {
            val rowLay = LinearLayout(this)
            rowLay.orientation = LinearLayout.HORIZONTAL
            rowLay.gravity = Gravity.CENTER
            for (k in row until minOf(row + perRow, fxList.size)) {
                val (label, color, setter) = fxList[k]
                val col = LinearLayout(this)
                col.orientation = LinearLayout.VERTICAL
                col.gravity = Gravity.CENTER
                col.setPadding(4, 6, 4, 6)
                val knob = KnobView(this)
                knob.baseColor = color
                knob.value = 0.5f
                val pct = TextView(this)
                pct.text = "50%"
                pct.setTextColor(color)
                pct.textSize = 10f
                knob.onChange = { v ->
                    pct.text = "${(v * 100).toInt()}%"
                    setter(v)
                }
                col.addView(knob, LinearLayout.LayoutParams(72, 72))
                col.addView(pct)
                val lbl = TextView(this)
                lbl.text = label
                lbl.setTextColor(color)
                lbl.textSize = 8f
                lbl.gravity = Gravity.CENTER
                col.addView(lbl)
                rowLay.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                knobBindings.add(knob to pct)
            }
            grid.addView(rowLay)
        }
        scroll.addView(grid)
        root.addView(scroll)

        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.setBackgroundColor(0xFF220000.toInt())
        bar.setPadding(8, 12, 8, 12)
        val btn = Button(this)
        btn.text = "🟢 BUKSAN ANG TUNOG"
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF228833.toInt())
        btn.textSize = 12f
        btn.setOnClickListener { toggleAudio(btn) }
        bar.addView(btn)
        val nameIn = EditText(this)
        nameIn.hint = "Pangalan ng Preset"
        nameIn.setTextColor(Color.WHITE)
        nameIn.setHintTextColor(0xFF888888.toInt())
        nameIn.textSize = 11f
        nameIn.setBackgroundColor(0xFF333333.toInt())
        nameIn.setPadding(10, 4, 10, 4)
        bar.addView(nameIn, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(10, 0, 10, 0) })
        val saveBtn = Button(this)
        saveBtn.text = "💾 I-SAVE"
        saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(0xFF226644.toInt())
        saveBtn.textSize = 11f
        saveBtn.setOnClickListener {
            val n = nameIn.text.toString().trim()
            if (n.isEmpty()) {
                Toast.makeText(this, "❌ Ilagay ang pangalan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AudioMixer.savePreset(n)
            Toast.makeText(this, "✅ NAISAVE: \"$n\"", Toast.LENGTH_SHORT).show()
            nameIn.text.clear()
        }
        bar.addView(saveBtn)
        root.addView(bar)
        return root
    }

    private fun toggleAudio(btn: Button) {
        if (AudioMixer.isAllOn()) {
            AudioMixer.setAllOn(false)
            AudioEngine.stop()
            btn.text = "🟢 BUKSAN ANG TUNOG"
            btn.setBackgroundColor(0xFF228833.toInt())
            Toast.makeText(this, "🔴 NAPATAY ANG TUNOG", Toast.LENGTH_SHORT).show()
        } else {
            // ✅ DITO LANG HINGIN ANG PAHINTULOT — HINDI SA SIMULA!
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
                Toast.makeText(this, "⚠️ PILIIN: PAYAG ✅", Toast.LENGTH_LONG).show()
                return
            }
            val ok = AudioEngine.start(this)
            if (ok) {
                AudioMixer.setAllOn(true)
                btn.text = "🔴 PATAYIN ANG TUNOG"
                btn.setBackgroundColor(0xFF882222.toInt())
            }
        }
    }

    override fun onRequestPermissionsResult(r: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(r, p, g)
        if (r == 123) {
            if (g.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ SALAMAT! PINDUTIN ULIT: 🟢 BUKSAN ANG TUNOG", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "❌ KAILANGAN NG MIKROPONO PARA SA TUNOG!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun buildCabinetScreen(): LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF1A1A1A.toInt())

        val topBar = LinearLayout(this)
        topBar.orientation = LinearLayout.HORIZONTAL
        topBar.setBackgroundColor(0xFF222222.toInt())
        topBar.setPadding(8, 8, 8, 8)

        val backBtn = Button(this)
        backBtn.text = "⬅️"
        backBtn.textSize = 18f
        backBtn.setTextColor(Color.WHITE)
        backBtn.setBackgroundColor(0xFF444444.toInt())
        backBtn.setPadding(12, 4, 12, 4)
        backBtn.setOnClickListener { viewPager.currentItem = 0 }
        topBar.addView(backBtn)

        val title = TextView(this)
        title.text = "📦 PRESETS — PUMILI NG PEDAL"
        title.textSize = 14f
        title.setTextColor(0xFFCCCC66.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(12, 4, 0, 4)
        topBar.addView(title, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(topBar)

        val scroll = ScrollView(this)
        val horiz = LinearLayout(this)
        horiz.orientation = LinearLayout.HORIZONTAL
        horiz.setPadding(12, 20, 12, 20)

        val builtIn = listOf(
            Triple("Clean", 0xFF226644.toInt(), "Malinaw"),
            Triple("Blues", 0xFF664422.toInt(), "Mainit"),
            Triple("Rock", 0xFF992222.toInt(), "Matigas"),
            Triple("Metal", 0xFF222222.toInt(), "Mabigat")
        )
        val allPresets = AudioMixer.getPresetNames().toList()

        allPresets.forEach { name ->
            val (color, desc) = builtIn.find { it.first == name }?.let { Pair(it.second, it.third) } ?: Pair(0xFF445566.toInt(), "Sariling Preset")
            val pedal = LinearLayout(this)
            pedal.orientation = LinearLayout.VERTICAL
            pedal.setBackgroundColor(color)
            pedal.setPadding(18, 24, 18, 24)
            pedal.gravity = Gravity.CENTER
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            p.setMargins(10, 4, 10, 4)
            pedal.layoutParams = p
            pedal.setOnClickListener {
                AudioMixer.applyPreset(name)
                updateAllKnobs()
                viewPager.currentItem = 0
                Toast.makeText(this, "✅ PRESET: \"$name\" — NAAYOS NA!", Toast.LENGTH_SHORT).show()
            }
            val lbl = TextView(this)
            lbl.text = name
            lbl.textSize = 18f
            lbl.setTextColor(Color.WHITE)
            lbl.setTypeface(null, Typeface.BOLD)
            lbl.gravity = Gravity.CENTER
            lbl.setPadding(0, 0, 0, 4)
            pedal.addView(lbl)
            val sub = TextView(this)
            sub.text = desc
            sub.textSize = 11f
            sub.setTextColor(0xAAFFFFFF.toInt())
            sub.gravity = Gravity.CENTER
            pedal.addView(sub)
            horiz.addView(pedal)
        }
        scroll.addView(horiz)
        root.addView(scroll)
        return root
    }

    private fun updateAllKnobs() {
        val vals = AudioMixer.getAllValues()
        for (i in knobBindings.indices) {
            val (knob, pct) = knobBindings[i]
            knob.value = vals[i]
            pct.text = "${(vals[i] * 100).toInt()}%"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioEngine.stop()
    }
}
