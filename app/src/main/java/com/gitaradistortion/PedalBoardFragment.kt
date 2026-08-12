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
        root.setPadding(8, 16, 8, 8)

        val title = TextView(ctx)
        title.text = "🎛️ PEDAL BOARD"
        title.textSize = 20f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 4, 0, 12)
        root.addView(title)

        // ✅ UNANG HANAY — LAHAT NAKALABAS NA!
        val row = LinearLayout(ctx)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER
        row.setPadding(2, 2, 2, 2)

        val noiseGate = NoiseGatePedal()
        noiseGate.onEnabledChanged = { MainActivity.setNoiseGateEnabledGlobal(it) }
        noiseGate.onThresholdChanged = { v -> MainActivity.setNoiseGateLevelGlobal(v) }
        row.addView(noiseGate.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val volume = VolumePedal()
        volume.onEnabledChanged = { MainActivity.setVolumeEnabledGlobal(it) }
        volume.onLevelChanged = { v -> MainActivity.setVolumeLevelGlobal(v) }
        row.addView(volume.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        // ✅ ILABAS NA ANG GAIN! MULA SA CABINET!
        val gain = GainPedal()
        gain.onEnabledChanged = { MainActivity.setGainEnabledGlobal(it) }
        gain.onLevelChanged = { v -> MainActivity.setGainLevelGlobal(v) }
        row.addView(gain.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        root.addView(row)
        return root
    }
}
