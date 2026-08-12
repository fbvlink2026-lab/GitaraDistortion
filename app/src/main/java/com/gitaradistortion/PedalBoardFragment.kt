package com.gitaradistortion

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class PedalBoardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val ctx = inflater.context
        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.setPadding(12, 8, 12, 8)

        // ==========================================
        // ✅ AKTIBONG PEDAL — NAKALABAS SA BOARD
        // ==========================================
        val activeTitle = TextView(ctx)
        activeTitle.text = "🎛️ AKTIBONG PEDAL"
        activeTitle.textSize = 18f
        activeTitle.setTextColor(0xFFFFCC00.toInt())
        activeTitle.gravity = Gravity.CENTER
        activeTitle.setPadding(0, 4, 0, 6)
        root.addView(activeTitle)

        val activeRow = LinearLayout(ctx)
        activeRow.orientation = LinearLayout.HORIZONTAL
        activeRow.gravity = Gravity.CENTER
        activeRow.setPadding(4, 4, 4, 8)

        // ✅ NOISE GATE — NAKALABAS NA
        val noiseGate = NoiseGatePedal()
        noiseGate.onEnabledChanged = { MainActivity.setNoiseGateEnabledGlobal(it) }
        noiseGate.onThresholdChanged = { v -> MainActivity.setNoiseGateLevelGlobal(v) }
        activeRow.addView(noiseGate.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        // ✅ VOLUME — NAKALABAS NA
        val volume = VolumePedal()
        volume.onEnabledChanged = { MainActivity.setVolumeEnabledGlobal(it) }
        volume.onLevelChanged = { v -> MainActivity.setVolumeLevelGlobal(v) }
        activeRow.addView(volume.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        root.addView(activeRow)

        // ==========================================
        // ✅ 📦 CABINET — LAGAYAN NG MGA PEDAL!
        // ==========================================
        val cabinetTitle = TextView(ctx)
        cabinetTitle.text = "📦 CABINET — Pindutin para ilabas"
        cabinetTitle.textSize = 14f
        cabinetTitle.setTextColor(0xFF888888.toInt())
        cabinetTitle.gravity = Gravity.CENTER
        cabinetTitle.setPadding(0, 12, 0, 6)
        root.addView(cabinetTitle)

        val cabinet = LinearLayout(ctx)
        cabinet.orientation = LinearLayout.HORIZONTAL
        cabinet.gravity = Gravity.CENTER
        cabinet.setBackgroundColor(0xFF222222.toInt())
        cabinet.setPadding(8, 12, 8, 12)

        // ✅ GAIN — NASA CABINET PA! HUGUTIN MO LATER!
        val gainLabel = TextView(ctx)
        gainLabel.text = "⚡ GAIN"
        gainLabel.setTextColor(0xFFAA66FF.toInt())
        gainLabel.setBackgroundColor(0xFF333333.toInt())
        gainLabel.setPadding(16, 10, 16, 10)
        gainLabel.textSize = 13f
        gainLabel.gravity = Gravity.CENTER
        cabinet.addView(gainLabel)

        root.addView(cabinet)
        return root
    }
}
