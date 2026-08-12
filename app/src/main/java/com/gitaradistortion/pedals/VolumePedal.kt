package com.gitaradistortion.pedals

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.gitaradistortion.KnobView

class VolumePedal {
    var isEnabled = true
    var level = 0.75f
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onLevelChanged: ((Float) -> Unit)? = null

    fun makeView(ctx: Context): LinearLayout {
        val card = LinearLayout(ctx)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(16, 12, 16, 12)
        card.setBackgroundColor(0xFF3A2E1A.toInt())
        card.minimumWidth = 160

        val title = TextView(ctx)
        title.text = "🔊 VOLUME"
        title.setTextColor(0xFFFF8822.toInt())
        title.textSize = 14f
        title.setPadding(0, 0, 0, 8)
        card.addView(title)

        val btn = Button(ctx)
        btn.text = "🟢 ON"
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF228833.toInt())
        btn.textSize = 11f
        btn.setPadding(8, 2, 8, 2)
        btn.minWidth = 80
        btn.setOnClickListener {
            isEnabled = !isEnabled
            btn.text = if (isEnabled) "🟢 ON" else "⚪ OFF"
            btn.setBackgroundColor(if (isEnabled) 0xFF228833.toInt() else 0xFF444444.toInt())
            onEnabledChanged?.invoke(isEnabled)
        }
        card.addView(btn)

        val knob = KnobView(ctx)
        knob.baseColor = 0xFFFF8822.toInt()
        knob.value = level
        knob.onValueChange = { level = it; onLevelChanged?.invoke(it) }
        val knobLayout = LinearLayout.LayoutParams(70, 70)
        knobLayout.setMargins(0, 10, 0, 4)
        card.addView(knob, knobLayout)

        val lbl = TextView(ctx)
        lbl.text = "LEVEL"
        lbl.setTextColor(0xFFDDAA66.toInt())
        lbl.textSize = 10f
        card.addView(lbl)

        return card
    }
}
