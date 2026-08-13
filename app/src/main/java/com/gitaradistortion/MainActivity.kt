package com.gitaradistortion

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
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
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private val knobBindings = mutableListOf<Pair<KnobView, TextView>>()
    private val presetNames = listOf("Clean","Blues","Rock","Metal")
    private val presetColors = listOf(0xFF226644,0xFF664422,0xFF992222,0xFF222222)
    private val presetDesc = listOf("Malinaw","Mainit","Matigas","Mabigat")

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
        buildViewPager()
    }

    private fun buildViewPager() {
        viewPager = findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = true

        val adapter = ScreenAdapter(this)
        viewPager.adapter = adapter
    }

    private inner class ScreenAdapter(ctx:Context): androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {
        private val SCREEN_MIXER = 0
        private val SCREEN_CABINET = 1
        override fun getItemCount()=2

        override fun onCreateViewHolder(p:RecyclerView.ViewHolder, t:Int):ViewHolder {
            return when(t){
                SCREEN_MIXER -> MixerViewHolder(buildMixerScreen())
                SCREEN_CABINET -> CabinetViewHolder(buildCabinetScreen())
                else -> MixerViewHolder(LinearLayout(this@MainActivity))
            }
        }

        override fun onBindViewHolder(h:ViewHolder, pos:Int){}
        override fun getItemViewType(pos:Int)=pos

        inner class MixerViewHolder(v:LinearLayout):ViewHolder(v)
        inner class CabinetViewHolder(v:LinearLayout):ViewHolder(v)
    }

    // ==========================================
    // 🎛️ SCREEN 1 — MAIN MIXER PANEL
    // ==========================================
    private fun buildMixerScreen():LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        val title = TextView(this)
        title.text = "🎛️ MAIN MIXER — SWIPE PAKALIWA → PRESETS"
        title.textSize = 14f; title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER; title.setPadding(0,8,0,4)
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
                col.setPadding(4,4,4,4)
                val knob = KnobView(this)
                knob.baseColor = color
                knob.value = 0.5f
                val pct = TextView(this)
                pct.text = "50%"
                pct.setTextColor(color); pct.textSize = 10f
                knob.onChange = { v ->
                    pct.text = "${(v*100).toInt()}%"
                    setter(v)
                }
                col.addView(knob, LinearLayout.LayoutParams(72,72))
                col.addView(pct)
                val lbl = TextView(this)
                lbl.text = label; lbl.setTextColor(color); lbl.textSize = 8f
                lbl.gravity = Gravity.CENTER
                col.addView(lbl)
                rowLay.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1f))
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
        bar.setPadding(6,8,6,8)
        val btn = Button(this)
        btn.text = "🟢 ON"
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF228833.toInt())
        btn.textSize = 12f
        btn.setOnClickListener {
            val on = AudioMixer.isAllOn()
            AudioMixer.setAllOn(!on)
            if(on){btn.text="🔴 OFF";btn.setBackgroundColor(0xFF882222.toInt())}
            else{btn.text="🟢 ON";btn.setBackgroundColor(0xFF228833.toInt());if(!AudioEngine.isRunning())AudioEngine.start(this)}
        }
        bar.addView(btn)
        val nameIn = EditText(this)
        nameIn.hint="Pangalan"
        nameIn.setTextColor(Color.WHITE)
        nameIn.setHintTextColor(0xFF888888.toInt())
        nameIn.textSize=11f
        nameIn.setBackgroundColor(0xFF333333.toInt())
        nameIn.setPadding(8,2,8,2)
        bar.addView(nameIn, LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f).apply { setMargins(8,0,8,0) })
        val saveBtn = Button(this)
        saveBtn.text="💾 SAVE"
        saveBtn.setTextColor(Color.WHITE)
        saveBtn.setBackgroundColor(0xFF226644.toInt())
        saveBtn.textSize=11f
        saveBtn.setOnClickListener {
            val n = nameIn.text.toString().trim()
            if(n.isEmpty()){Toast.makeText(this,"❌ Ilagay ang pangalan!",Toast.LENGTH_SHORT).show();return@setOnClickListener}
            AudioMixer.savePreset(n)
            Toast.makeText(this,"✅ NAISAVE: $n",Toast.LENGTH_SHORT).show()
            nameIn.text.clear()
        }
        bar.addView(saveBtn)
        root.addView(bar)
        return root
    }

    // ==========================================
    // 📦 SCREEN 2 — CABINET — MGA PEDAL NAKAHILIRA!
    // ==========================================
    private fun buildCabinetScreen():LinearLayout {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF1A1A1A.toInt())
        val title = TextView(this)
        title.text = "📦 PRESETS — PUMILI NG PEDAL • SWIPE PAKANAN → BALIK MIXER"
        title.textSize = 13f; title.setTextColor(0xFFCCCC66.toInt())
        title.gravity = Gravity.CENTER; title.setPadding(0,12,0,8)
        root.addView(title)

        // ✅ MGA PEDAL — NAKAHILIRA HORIZONTAL! KATULAD NG TONEBRIDGE!
        val scroll = ScrollView(this)
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(12,12,12,12)
        presetNames.forEachIndexed { i, name ->
            val pedal = LinearLayout(this)
            pedal.orientation = LinearLayout.VERTICAL
            pedal.setBackgroundColor(presetColors[i].toInt())
            pedal.setPadding(16,20,16,20)
            pedal.gravity = Gravity.CENTER
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply { setMargins(8,4,8,4) }
            pedal.layoutParams = params
            pedal.setOnClickListener {
                AudioMixer.applyPreset(name)
                updateAllKnobs()
                viewPager.currentItem = 0 // ✅ AWTOMATIK BALIK SA MIXER!
                Toast.makeText(this,"✅ PRESET: $name — AWTOMATIK NAAYOS LAHAT!",Toast.LENGTH_SHORT).show()
                if(!AudioEngine.isRunning()) AudioEngine.start(this)
            }
            val lbl = TextView(this)
            lbl.text = name; lbl.textSize = 18f; lbl.setTextColor(Color.WHITE)
            lbl.gravity = Gravity.CENTER; lbl.setPadding(0,0,0,4)
            pedal.addView(lbl)
            val sub = TextView(this)
            sub.text = presetDesc[i]; sub.textSize = 11f; sub.setTextColor(0xAAFFFFFF.toInt())
            sub.gravity = Gravity.CENTER
            pedal.addView(sub)
            row.addView(pedal)
        }
        scroll.addView(row)
        root.addView(scroll)
        return root
    }

    // ✅ AWTOMATIK AYUSIN LAHAT NG PIHITAN MULA SA PRESET!
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
            Toast.makeText(this,"✅ Handa na! Swipe pakaliwa para sa Presets!",Toast.LENGTH_LONG).show()
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
