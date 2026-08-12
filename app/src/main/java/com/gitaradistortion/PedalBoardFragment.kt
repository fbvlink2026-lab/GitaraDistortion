package com.gitaradistortion

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.gitaradistortion.pedals.NoiseGatePedal
import com.gitaradistortion.pedals.VolumePedal

class PedalBoardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val ctx = inflater.context
        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.setPadding(12, 20, 12, 12)

        val title = TextView(ctx)
        title.text = "🎛️ PEDAL BOARD"
        title.textSize = 22f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 8, 0, 16)
        root.addView(title)

        val row = LinearLayout(ctx)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER
        row.setPadding(4, 4, 4, 4)

        val noiseGate = NoiseGatePedal()
        noiseGate.onEnabledChanged = { MainActivity.setNoiseGateEnabledGlobal(it) }
        noiseGate.onThresholdChanged = { MainActivity.setNoiseGateLevelGlobal(it) }
        row.addView(noiseGate.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val volume = VolumePedal()
        volume.onEnabledChanged = { MainActivity.setVolumeEnabledGlobal(it) }
        volume.onLevelChanged = { MainActivity.setVolumeLevelGlobal(it) }
        row.addView(volume.makeView(ctx), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        root.addView(row)
        return root
    }
}
