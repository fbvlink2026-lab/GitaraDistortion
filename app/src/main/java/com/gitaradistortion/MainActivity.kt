package com.gitaradistortion

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// ✅ MGA PEDAL — BAWAT ISA MAY SARILING URI!
enum class PedalDef(
    val id: String,
    val displayName: String,
    val icon: String,
    val themeColor: Int,
    val knobLabels: List<String>,
    val defaultPosOnBoard: Int? = null  // null = nasa Cabinet
) {
    VOLUME("VOL", "VOLUME", "🔊", 0xFFFF8822.toInt(), listOf("LEVEL"), 1),
    TONE("TON", "TONE", "🎵", 0xFF44DD88.toInt(), listOf("SHAPE"), 2),
    REVERB("REV", "REVERB", "🌊", 0xFFAA66FF.toInt(), listOf("MIX", "DECAY"), 3),
    NOISE_GATE("NOI", "NOISE GATE", "🚧", 0xFF66DDDD.toInt(), listOf("THRESH", "RELEASE"), 4),
    GAIN("GAI", "GAIN", "⚡", 0xFFFFFF00.toInt(), listOf("DRIVE", "LEVEL"), 5),
    OVERDRIVE("OVE", "OVERDRIVE", "🔥", 0xFFFFAA00.toInt(), listOf("GAIN", "TONE", "LEVEL"), 6),
    DISTORTION("DIS", "DISTORTION", "💥", 0xFFFF4444.toInt(), listOf("GAIN", "TONE", "LEVEL"), null),
    PHASER("PHA", "PHASER", "🫧", 0xFF44AAFF.toInt(), listOf("RATE", "DEPTH", "MIX"), null),
    DELAY("DLY", "DELAY/ECHO", "⏱️", 0xFF00CCFF.toInt(), listOf("TIME", "FEEDBACK", "MIX"), null),
    WAH_WAH("WAH", "WAH-WAH", "🎺", 0xFFFF00AA.toInt(), listOf("FREQ", "RANGE"), null);

    companion object {
        fun all() = values().toList()
        fun onBoard() = values().filter { it.defaultPosOnBoard != null }.sortedBy { it.defaultPosOnBoard }
        fun inCabinet() = values().filter { it.defaultPosOnBoard == null }
    }
}

// ✅ PANGUNAHING SCREEN — MAY PEDAL BOARD + CABINET + MASTER!
class MainActivity : FragmentActivity() {
    private var isMasterOn = false

    // === AUDIO NATIVE CALLS ===
    private external fun startAudioEngine(): Unit
    private external fun stopAudioEngine(): Unit
    private external fun setMasterVolume(v: Float): Unit
    private external fun setVolumeLevel(v: Float): Unit
    private external fun setVolumeEnabled(e: Boolean): Unit
    private external fun setToneLevel(v: Float): Unit
    private external fun setToneEnabled(e: Boolean): Unit
    private external fun setReverbMix(v: Float): Unit
    private external fun setReverbDecay(v: Float): Unit
    private external fun setReverbEnabled(e: Boolean): Unit
    private external fun setNoiseGateThresh(v: Float): Unit
    private external fun setNoiseGateRelease(v: Float): Unit
    private external fun setNoiseGateEnabled(e: Boolean): Unit
    private external fun setGainDrive(v: Float): Unit
    private external fun setGainLevel(v: Float): Unit
    private external fun setGainEnabled(e: Boolean): Unit
    private external fun setOverdriveGain(v: Float): Unit
    private external fun setOverdriveTone(v: Float): Unit
    private external fun setOverdriveLevel(v: Float): Unit
    private external fun setOverdriveEnabled(e: Boolean): Unit
    private external fun setDistortionGain(v: Float): Unit
    private external fun setDistortionTone(v: Float): Unit
    private external fun setDistortionLevel(v: Float): Unit
    private external fun setDistortionEnabled(e: Boolean): Unit
    private external fun setPhaserRate(v: Float): Unit
    private external fun setPhaserDepth(v: Float): Unit
    private external fun setPhaserMix(v: Float): Unit
    private external fun setPhaserEnabled(e: Boolean): Unit
    private external fun setDelayTime(v: Float): Unit
    private external fun setDelayFeedback(v: Float): Unit
    private external fun setDelayMix(v: Float): Unit
    private external fun setDelayEnabled(e: Boolean): Unit
    private external fun setWahFreq(v: Float): Unit
    private external fun setWahRange(v: Float): Unit
    private external fun setWahEnabled(e: Boolean): Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("gitaradistortion")

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF1A1A1A.toInt())

        // ✅ VIEWPAGER — PEDAL BOARD ↔ CABINET
        val viewPager = ViewPager2(this)
        viewPager.offscreenPageLimit = 1
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) PedalBoardFragment()
                       else CabinetFragment()
            }
        }
        root.addView(viewPager, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        // ✅ PANEL INDICATOR
        val panelIndicator = TextView(this)
        panelIndicator.text = "◀  🎛️ PEDAL BOARD  |  📦 CABINET ▶"
        panelIndicator.textSize = 13f
        panelIndicator.setTextColor(0xFFAAAAAA.toInt())
        panelIndicator.gravity = Gravity.CENTER
        panelIndicator.setPadding(0, 4, 0, 4)
        root.addView(panelIndicator)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                panelIndicator.text = if (position == 0)
                    "◀  🎛️ PEDAL BOARD  |  📦 CABINET ▶"
                else "◀  📦 CABINET  |  🎛️ PEDAL BOARD ▶"
            }
        })

        // ✅ MASTER PANEL — NAKABABA! HINDI NA LILIPAT!
        val masterPanel = LinearLayout(this)
        masterPanel.orientation = LinearLayout.HORIZONTAL
        masterPanel.setBackgroundColor(0xFF2B2B2B.toInt())
        masterPanel.setPadding(16, 12, 16, 12)
        masterPanel.gravity = Gravity.CENTER_VERTICAL

        // MASTER VOLUME — MALAKING PIHITAN
        val masterKnob = KnobView(this)
        masterKnob.baseColor = 0xFFFFCC00.toInt()
        masterKnob.value = 0.75f
        masterKnob.pager = viewPager
        masterKnob.layoutParams = LinearLayout.LayoutParams(70, 70)
        masterKnob.onValueChange = { setMasterVolume(it) }

        val masterLabel = TextView(this)
        masterLabel.text = "🎚️ MASTER\nVOLUME"
        masterLabel.setTextColor(Color.WHITE)
        masterLabel.textSize = 11f
        masterLabel.gravity = Gravity.CENTER
        masterLabel.setPadding(8, 0, 12, 0)

        // MASTER ON/OFF — MALAKING BUTTON
        val masterBtn = Button(this)
        masterBtn.text = "🔘 MASTER\nOFF"
        masterBtn.textSize = 14f
        masterBtn.setBackgroundColor(0xFF444444.toInt())
        masterBtn.setTextColor(Color.WHITE)
        masterBtn.setPadding(20, 8, 20, 8)
        masterBtn.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
                return@setOnClickListener
            }
            isMasterOn = !isMasterOn
            if (isMasterOn) {
                startAudioEngine()
                masterBtn.text = "🔴 MASTER\nON"
                masterBtn.setBackgroundColor(0xFF22AA33.toInt())
                Toast.makeText(this, "✅ MASTER ON — Isaksak ang iRig!", Toast.LENGTH_SHORT).show()
            } else {
                stopAudioEngine()
                masterBtn.text = "🔘 MASTER\nOFF"
                masterBtn.setBackgroundColor(0xFF444444.toInt())
            }
        }

        masterPanel.addView(masterKnob)
        masterPanel.addView(masterLabel)
        masterPanel.addView(masterBtn)
        root.addView(masterPanel)

        setContentView(root)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Pahintulot nakuha! Isaksak ang iRig!", Toast.LENGTH_LONG).show()
        }
    }
}
