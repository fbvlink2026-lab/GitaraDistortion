package com.gitaradistortion

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
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

    // ✅ LAHAT NG FX — 18 PIHITAN
    private val fxList = listOf(
        Triple("🚧 NOISE GATE",0xFF44DD88.toInt(),{v:Float->AudioMixer.noiseGate=v}),
        Triple("🎵 TONE",0xFFFFCC44.toInt(),{v->AudioMixer.tone=v}),
        Triple("⚡ GAIN",0xFFFFDD22.toInt(),{v->AudioMixer.gain=v}),
        Triple("🟠 OVERDRIVE",0xFFFF9922.toInt(),{v->AudioMixer.overdrive=v}),
        Triple("🔴 DISTORTION",0xFFFF4422.toInt(),{v->AudioMixer.distortion=v}),
        Triple("⚫ FUZZ",0xFF664422.toInt(),{v->AudioMixer.fuzz=v}),
        Triple("🫧 CHORUS",0xFF66AAFF.toInt(),{v->AudioMixer.chorus=v}),
        Triple("🌀 PHASER",0xFFAA66FF.toInt(),{v->AudioMixer.phaser=v}),
        Triple("📳 TREMOLO",0xFFFF6688.toInt(),{v->AudioMixer.tremolo=v}),
        Triple("🎶 VIBRATO",0xFF88CCFF.toInt(),{v->AudioMixer.vibrato=v}),
        Triple("⏱️ DELAY",0xFF8888FF.toInt(),{v->AudioMixer.delay=v}),
        Triple("🌊 REVERB",0xFF44CCDD.toInt(),{v->AudioMixer.reverb=v}),
        Triple("🎵 WAH",0xFFFF66AA.toInt(),{v->AudioMixer.wah=v}),
        Triple("🔊 AMP",0xFFFFBB44.toInt(),{v->AudioMixer.ampType=v}),
        Triple("🔵 BASS",0xFF4488FF.toInt(),{v->AudioMixer.bass=v}),
        Triple("🟡 MID",0xFFFFCC00.toInt(),{v->AudioMixer.mid=v}),
        Triple("🟢 TREBLE",0xFF44DD88.toInt(),{v->AudioMixer.treble=v}),
        Triple("🎚️ MASTER",0xFFFFFFFF.toInt(),{v->AudioMixer.masterVolume=v})
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AudioMixer.init(this)
        checkPermission()
        setupViewPager()
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        val screens = listOf(buildMixerScreen(), buildCabinetScreen())
        viewPager.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun getItemCount() = 2
            override fun onCreateViewHolder(p:ViewGroup, t:Int) =
                object : androidx.recyclerview.widget.RecyclerView.ViewHolder(screens[t]) {}
            override fun onBindViewHolder(h:androidx.recyclerview.widget.RecyclerView.ViewHolder, p:Int) {}
        }
    }

    // ==========================================
    // 🎛️ SCREEN 1 — MAIN MIXER
    // ✅ DEFAULT: LAHAT NG PIHITAN = 0%
    // ==========================================
    private fun buildMixerScreen():LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())

        val title = TextView(this)
        title.text = "🎛️ MAIN MIXER — SWIPE PAKALIWA → PRESETS CABINET"
        title.textSize = 13f; title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER; title.setPadding(0,12,0,6)
        root.addView(title)

        val scroll = ScrollView(this)
        val grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL
        val perRow = 3
        knobBindings.clear()
        for(row in fxList.indices step perRow) {
            val rowLay = LinearLayout(this)
            rowLay.orientation = LinearLayout.HORIZONTAL
            rowLay.gravity = Gravity.CENTER
            for(k in row until minOf(row+perRow, fxList.size)) {
                val (label, color, setter) = fxList[k]
                val col = LinearLayout(this)
                col.orientation = LinearLayout.VERTICAL
                col.gravity = Gravity.CENTER
                col.setPadding(4,6,4,6)
                val knob = KnobView(this)
                knob.baseColor = color
                knob.value = 0.0f // ✅ DEFAULT 0%
                val pct = TextView(this)
                pct.text = "0%"
                pct.setTextColor(color); pct.textSize = 10f
                knob.onChange = { v ->
                    pct.text = "${(v*100).toInt()}%"
                    setter(v)
                }
                col.addView(knob, LinearLayout.LayoutParams(76,76))
                col.addView(pct)
                val lbl = TextView(this)
                lbl.text = label; lbl.setTextColor(color); lbl.textSize = 8f
                lbl.gravity = Gravity.CENTER
                col.addView(lbl)
                rowLay.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1f))
                knobBindings.add(knob to pct)
            }
            grid.addView(rowLay)
        }
        scroll.addView(grid)
        root.addView(scroll)

        // ✅ MASTER BAR — ON/OFF + SAVE
        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.setBackgroundColor(0xFF220000.toInt())
        bar.setPadding(8,10,8,10)
        val btn = Button(this)
        btn.text = "🔴 OFF" // ✅ DEFAULT OFF
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF882222.toInt())
        btn.textSize = 12f
        btn.setOnClickListener {
            val on = AudioMixer.isAllOn()
            AudioMixer.setAllOn(!on)
            if(on){btn.text="🔴 OFF";btn.setBackgroundColor(0xFF882222.toInt());AudioEngine.stop()}
            else{btn.text="🟢 ON";btn.setBackgroundColor(0xFF228833.toInt());if(!AudioEngine.isRunning())AudioEngine.start(this)}
        }
        bar.addView(btn)
        val nameIn = EditText(this)
        nameIn.hint="Pangalan ng Preset"
        nameIn.setTextColor(Color.WHITE)
        nameIn.setHintTextColor(0xFF888888.toInt())
        nameIn.textSize=11f
        nameIn.setBackgroundColor(0xFF333333.toInt())
        nameIn.setPadding(10,4,10,4)
        bar.addView(nameIn, LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f).apply { setMargins(10,0,10,0) })
        val saveBtn = Button(this)
        saveBtn.text="💾 I-SAVE"
        saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(0xFF226644.toInt())
        saveBtn.textSize=11f
        saveBtn.setOnClickListener {
            val n = nameIn.text.toString().trim()
            if(n.isEmpty()){Toast.makeText(this,"❌ Ilagay ang pangalan ng Preset!",Toast.LENGTH_SHORT).show();return@setOnClickListener}
            AudioMixer.savePreset(n)
            Toast.makeText(this,"✅ NAISAVE: \"$n\" — Makikita na sa Cabinet!",Toast.LENGTH_SHORT).show()
            nameIn.text.clear()
        }
        bar.addView(saveBtn)
        root.addView(bar)
        return root
    }

    // ==========================================
    // 📦 CABINET — PRESET PEDAL = PIPIHITIN LANG!
    // ✅ WALANG TUNOG DITO — HALAGA LANG ANG BABAGUHIN!
    // ==========================================
    private fun buildCabinetScreen():LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF1A1A1A.toInt())

        // ✅ TAAS — ARROW BACK
        val topBar = LinearLayout(this)
        topBar.orientation = LinearLayout.HORIZONTAL
        topBar.setBackgroundColor(0xFF222222.toInt())
        topBar.setPadding(8,8,8,8)

        val backBtn = Button(this)
        backBtn.text = "⬅️"
        backBtn.textSize = 18f
        backBtn.setTextColor(Color.WHITE)
        backBtn.setBackgroundColor(0xFF444444.toInt())
        backBtn.setPadding(12,4,12,4)
        backBtn.setOnClickListener { viewPager.currentItem = 0 }
        topBar.addView(backBtn)

        val title = TextView(this)
        title.text = "📦 PRESETS — PUMILI → AWTOMATIK PIPIHITIN LAHAT"
        title.textSize = 13f; title.setTextColor(0xFFCCCC66.toInt())
        title.gravity = Gravity.CENTER; title.setPadding(12,4,0,4)
        topBar.addView(title, LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f))
        root.addView(topBar)

        // ✅ HILIRANG PEDAL — KAMUKHA NG TONEBRIDGE!
        val scroll = ScrollView(this)
        val horiz = LinearLayout(this)
        horiz.orientation = LinearLayout.HORIZONTAL
        horiz.setPadding(12,20,12,20)
        horiz.gravity = Gravity.CENTER_VERTICAL

        val builtIn = listOf(
            Triple("Clean",0xFF226644.toInt(),"Malinaw"),
            Triple("Blues",0xFF664422.toInt(),"Mainit"),
            Triple("Rock",0xFF992222.toInt(),"Matigas"),
            Triple("Metal",0xFF222222.toInt(),"Mabigat")
        )
        val allPresets = AudioMixer.getPresetNames().toList()

        allPresets.forEach { name ->
            val (color, desc) = builtIn.find{it.first==name}?.let{Pair(it.second,it.third)} ?: Pair(0xFF445566.toInt(),"Sariling Preset")
            val pedal = LinearLayout(this)
            pedal.orientation = LinearLayout.VERTICAL
            pedal.setBackgroundColor(color)
            pedal.setPadding(18,24,18,24)
            pedal.gravity = Gravity.CENTER
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            p.setMargins(10,4,10,4)
            pedal.layoutParams = p

            // ✅ PINDUTIN PEDAL → PIPIHITIN LANG ANG MGA HALAGA! WALANG TUNOG DITO!
            pedal.setOnClickListener {
                AudioMixer.applyPreset(name) // ✅ PALIT HALAGA LANG
                updateAllKnobs() // ✅ AYUSIN ANG MGA PIHITAN SA SCREEN
                viewPager.currentItem = 0 // ✅ BALIK SA MIXER
                Toast.makeText(this,"✅ PRESET: \"$name\" — LAHAT PIHIT NA! PINDUTIN 🟢 ON PARA MAY TUNOG!",Toast.LENGTH_SHORT).show()
            }
            val lbl = TextView(this)
            lbl.text = name; lbl.textSize = 18f; lbl.setTextColor(Color.WHITE)
            lbl.setTypeface(null, Typeface.BOLD)
            lbl.gravity = Gravity.CENTER; lbl.setPadding(0,0,0,4)
            pedal.addView(lbl)
            val sub = TextView(this)
            sub.text = desc; sub.textSize = 11f; sub.setTextColor(0xAAFFFFFF.toInt())
            sub.gravity = Gravity.CENTER
            pedal.addView(sub)
            horiz.addView(pedal)
        }
        scroll.addView(horiz)
        root.addView(scroll)
        return root
    }

    // ✅ AYUSIN ANG MGA PIHITAN SA SCREEN AYON SA PRESET
    private fun updateAllKnobs() {
        val vals = AudioMixer.getAllValues()
        for(i in knobBindings.indices) {
            val (knob, pct) = knobBindings[i]
            knob.value = vals[i]
            pct.text = "${(vals[i]*100).toInt()}%"
        }
    }

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"✅ Handa na! Pumili muna ng Preset bago i-ON!",Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),123)
        }
    }
    override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray) {
        super.onRequestPermissionsResult(r,p,g)
        if(r==123 && g.firstOrNull()==PackageManager.PERMISSION_GRANTED) Toast.makeText(this,"✅ Pahintulot natanggap!",Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() { super.onDestroy(); AudioEngine.stop() }
}
