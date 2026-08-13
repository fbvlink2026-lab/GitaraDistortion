package com.gitaradistortion

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class PedalBoardFragment : Fragment() {
    private var cabinetVisible = false
    private lateinit var mainRow: LinearLayout
    private lateinit var activeArea: LinearLayout
    private lateinit var cabinetArea: LinearLayout
    private var startX = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val ctx = inflater.context
        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.setPadding(6, 8, 6, 6)

        // ✅ TAAS: MASTER ON/OFF
        val masterBar = LinearLayout(ctx)
        masterBar.orientation = LinearLayout.HORIZONTAL
        masterBar.gravity = Gravity.CENTER
        masterBar.setBackgroundColor(0xFF220000.toInt())
        masterBar.setPadding(12, 8, 12, 8)

        val masterLabel = TextView(ctx)
        masterLabel.text = "🔴 MASTER"
        masterLabel.textSize = 16f
        masterLabel.setTextColor(0xFFFF4444.toInt())
        masterLabel.setPadding(0, 0, 16, 0)
        masterBar.addView(masterLabel)

        val masterBtn = Button(ctx)
        masterBtn.text = "🟢 ON"
        masterBtn.setTextColor(Color.WHITE)
        masterBtn.setBackgroundColor(0xFF228833.toInt())
        masterBtn.textSize = 13f
        masterBtn.setPadding(20, 4, 20, 4)
        masterBtn.minWidth = 120
        masterBtn.setOnClickListener {
            val isOn = masterBtn.text == "🟢 ON"
            MainActivity.setMasterEnabledGlobal(!isOn)
            if (isOn) {
                masterBtn.text = "⚪ OFF"
                masterBtn.setBackgroundColor(0xFF882222.toInt())
                masterLabel.setTextColor(0xFF888888.toInt())
            } else {
                masterBtn.text = "🟢 ON"
                masterBtn.setBackgroundColor(0xFF228833.toInt())
                masterLabel.setTextColor(0xFFFF4444.toInt())
            }
        }
        masterBar.addView(masterBtn)
        root.addView(masterBar)

        // ✅ TITLE
        val title = TextView(ctx)
        title.text = "🎛️ PEDAL BOARD ← Hilahin pakaliwa para sa Cabinet"
        title.textSize = 14f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 8, 0, 8)
        root.addView(title)

        // ✅ GITNA: HILAHAN PAKALIWA/PABANAN
        mainRow = LinearLayout(ctx)
        mainRow.orientation = LinearLayout.HORIZONTAL
        mainRow.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startX = event.rawX
                MotionEvent.ACTION_MOVE -> {
                    val delta = startX - event.rawX
                    if (delta > 80 && !cabinetVisible) showCabinet()
                    if (delta < -80 && cabinetVisible) hideCabinet()
                }
            }
            true
        }

        // ✅ KALIWA — AKTIBONG PEDAL
        activeArea = LinearLayout(ctx)
        activeArea.orientation = LinearLayout.VERTICAL
        val activeLP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3f)
        activeArea.layoutParams = activeLP
        activeArea.setPadding(2, 2, 6, 2)
        activeArea.setBackgroundColor(0xFF1A1A1A.toInt())

        val activeTitle = TextView(ctx)
        activeTitle.text = "✅ AKTIBONG PEDAL"
        activeTitle.textSize = 13f
        activeTitle.setTextColor(0xFF88FF88.toInt())
        activeTitle.gravity = Gravity.CENTER
        activeTitle.setPadding(0, 4, 0, 6)
        activeArea.addView(activeTitle)

        val pedalsRow = LinearLayout(ctx)
        pedalsRow.orientation = LinearLayout.HORIZONTAL
        pedalsRow.gravity = Gravity.CENTER

        val noiseGate = NoiseGatePedal()
        noiseGate.onEnabledChanged = { MainActivity.setNoiseGateEnabledGlobal(it) }
        noiseGate.onThresholdChanged = { v -> MainActivity.setNoiseGateLevelGlobal(v) }
        pedalsRow.addView(noiseGate.makeView(ctx), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val volume = VolumePedal()
        volume.onEnabledChanged = { MainActivity.setVolumeEnabledGlobal(it) }
        volume.onLevelChanged = { v -> MainActivity.setVolumeLevelGlobal(v) }
        pedalsRow.addView(volume.makeView(ctx), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        val gain = GainPedal()
        gain.onEnabledChanged = { MainActivity.setGainEnabledGlobal(it) }
        gain.onLevelChanged = { v -> MainActivity.setGainLevelGlobal(v) }
        pedalsRow.addView(gain.makeView(ctx), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        activeArea.addView(pedalsRow)
        mainRow.addView(activeArea)

        // ✅ KANAN — CABINET (NAKATAGO SA SIMULA)
        cabinetArea = LinearLayout(ctx)
        cabinetArea.orientation = LinearLayout.VERTICAL
        val cabLP = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0f)
        cabinetArea.layoutParams = cabLP
        cabinetArea.setBackgroundColor(0xFF2A2A2A.toInt())
        cabinetArea.setPadding(6, 6, 6, 6)
        cabinetArea.visibility = View.GONE

        val cabTitle = TextView(ctx)
        cabTitle.text = "📦 CABINET — Pindutin → Ilipat sa Aktibong Pedal"
        cabTitle.textSize = 11f
        cabTitle.setTextColor(0xFFCCCC66.toInt())
        cabTitle.gravity = Gravity.CENTER
        cabTitle.setPadding(0, 4, 0, 8)
        cabinetArea.addView(cabTitle)

        val cabScroll = ScrollView(ctx)
        val cabInner = LinearLayout(ctx)
        cabInner.orientation = LinearLayout.VERTICAL

        // ✅ OVERDRIVE — PINDUTIN → LIPAT SA AKTIBONG PEDAL!
        val btnOverdrive = makeCabinetItem(ctx, "🟠 OVERDRIVE", 0xFFFFAA22.toInt())
        btnOverdrive.setOnClickListener {
            addPedalToActive("overdrive")
        }
        cabInner.addView(btnOverdrive)

        // ✅ DISTORTION — PINDUTIN → LIPAT SA AKTIBONG PEDAL!
        val btnDistortion = makeCabinetItem(ctx, "🔴 DISTORTION", 0xFFFF4422.toInt())
        btnDistortion.setOnClickListener {
            addPedalToActive("distortion")
        }
        cabInner.addView(btnDistortion)

        // ✅ TONE — PINDUTIN → LIPAT SA AKTIBONG PEDAL!
        val btnTone = makeCabinetItem(ctx, "🎨 TONE", 0xFF44DDAA.toInt())
        btnTone.setOnClickListener {
            addPedalToActive("tone")
        }
        cabInner.addView(btnTone)

        cabScroll.addView(cabInner)
        cabinetArea.addView(cabScroll)
        mainRow.addView(cabinetArea)
        root.addView(mainRow)
        return root
    }

    private fun makeCabinetItem(ctx: android.content.Context, text: String, color: Int): TextView {
        val tv = TextView(ctx)
        tv.text = text
        tv.setTextColor(color)
        tv.setBackgroundColor(0xFF333333.toInt())
        tv.setPadding(12, 14, 12, 14)
        tv.textSize = 13f
        tv.gravity = Gravity.CENTER
        return tv
    }

    private fun showCabinet() {
        cabinetVisible = true
        activeArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f)
        cabinetArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f)
        cabinetArea.visibility = View.VISIBLE
    }

    private fun hideCabinet() {
        cabinetVisible = false
        activeArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3f)
        cabinetArea.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0f)
        cabinetArea.visibility = View.GONE
    }

    private fun addPedalToActive(pedalType: String) {
        val ctx = view?.context ?: return
        val pedalsRow = activeArea.getChildAt(1) as LinearLayout

        when (pedalType) {
            "overdrive" -> {
                val pedal = OverdrivePedal()
                pedal.onEnabledChanged = { MainActivity.setOverdriveEnabledGlobal(it) }
                pedal.onLevelChanged = { v -> MainActivity.setOverdriveLevelGlobal(v) }
                pedalsRow.addView(pedal.makeView(ctx), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
            "distortion" -> {
                val pedal = DistortionPedal()
                pedal.onEnabledChanged = { MainActivity.setDistortionEnabledGlobal(it) }
                pedal.onLevelChanged = { v -> MainActivity.setDistortionLevelGlobal(v) }
                pedalsRow.addView(pedal.makeView(ctx), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
            "tone" -> {
                val pedal = TonePedal()
                pedal.onEnabledChanged = { MainActivity.setToneEnabledGlobal(it) }
                pedal.onLevelChanged = { v -> MainActivity.setToneLevelGlobal(v) }
                pedalsRow.addView(pedal.makeView(ctx), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            }
        }
    }
}
