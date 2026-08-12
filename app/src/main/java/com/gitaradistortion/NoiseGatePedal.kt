package com.gitaradistortion

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class NoiseGatePedal {
    var isEnabled = false
    var threshold = 0.04f
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onLevelChanged: ((Float) -> Unit)? = null

    fun makeView(ctx: Context): LinearLayout {
        val card = LinearLayout(ctx)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(16, 12, 16, 12)
        card.setBackgroundColor(0xFF2A3A3A.toInt())
        card.minimumWidth = 160

        val title = TextView(ctx)
        title.text = "🚧 NOISE GATE"
        title.setTextColor(0xFF66DDDD.toInt())
        title.textSize = 14f
        title.setPadding(0, 0, 0, 8)
        card.addView(title)

        val btn = Button(ctx)
        btn.text = "⚪ OFF"
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF444444.toInt())
        btn.textSize = 11f
        btn.setPadding(8, 2, 8, 2)
        btn.minWidth = 80
        btn.setOnClickListener {
            isEnabled = !isEnabled
            btn.text = if (isEnabled) "🟢 ON" else "⚪ OFF"
            btn.setBackgroundColor(if (isEnabled) 0xFF228866.toInt() else 0xFF444444.toInt())
            onEnabledChanged?.invoke(isEnabled)
        }
        card.addView(btn)

        val knob = KnobView(ctx)
        knob.baseColor = 0xFF66DDDD.toInt()
        knob.value = threshold
        // ✅ LIGTAS NA PAGTANGGAP — WALANG CRASH!
        knob.onValueChange = { v ->
            threshold = v
            try {
                onLevelChanged?.invoke(v)
            } catch (_: Exception) {}
        }
        val knobLayout = LinearLayout.LayoutParams(70, 70)
        knobLayout.setMargins(0, 10, 0, 4)
        card.addView(knob, knobLayout)

        val lbl = TextView(ctx)
        lbl.text = "THRESHOLD"
        lbl.setTextColor(0xFFAAAAAA.toInt())
        lbl.textSize = 10f
        card.addView(lbl)

        return card
    }
}
