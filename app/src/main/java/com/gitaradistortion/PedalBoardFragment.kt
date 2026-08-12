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
        root.setBackgroundColor(0xFF1E1E1E.toInt())
        root.setPadding(16, 24, 16, 16)

        val title = TextView(ctx)
        title.text = "🎛️  PEDAL BOARD"
        title.textSize = 22f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 8, 0, 16)
        root.addView(title)

        val hint = TextView(ctx)
        hint.text = "📦 Pahilis pakanan → pumili mula sa Cabinet"
        hint.textSize = 13f
        hint.setTextColor(0xFFAAAAAA.toInt())
        hint.gravity = Gravity.CENTER
        hint.setPadding(0, 0, 0, 20)
        root.addView(hint)

        val row = LinearLayout(ctx)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER
        row.setPadding(8, 8, 8, 8)

        val noiseGate = NoiseGatePedal()
        noiseGate.onEnabledChanged = { MainActivity.setNoiseGateEnabledGlobal(it) }
        noiseGate.onLevelChanged = { MainActivity.setNoiseGateLevelGlobal(it) }
        row.addView(noiseGate.makeView(ctx))

        val volume = VolumePedal()
        volume.onEnabledChanged = { MainActivity.setVolumeEnabledGlobal(it) }
        volume.onLevelChanged = { MainActivity.setVolumeLevelGlobal(it) }
        row.addView(volume.makeView(ctx))

        root.addView(row)
        return root
    }
}
