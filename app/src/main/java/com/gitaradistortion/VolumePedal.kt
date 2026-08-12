package com.gitaradistortion

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class VolumePedal {
    var isEnabled = true
    var level = 0.75f
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onLevelChanged: ((Float) -> Unit)? = null

    fun makeView(ctx: Context): LinearLayout {
        val card = LinearLayout(ctx)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(20, 16, 20, 16)
        card.setBackgroundColor(0xFF3A2A1A.toInt())
        card.minimumWidth = 200

        val title = TextView(ctx)
        title.text = "🔊 VOLUME"
        title.setTextColor(0xFFFF8822.toInt())
        title.textSize = 15f
        title.setPadding(0, 0, 0, 10)
        card.addView(title)

        val btn = Button(ctx)
        btn.text = "🟢 ON"
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF228833.toInt())
        btn.textSize = 12f
        btn.setPadding(12, 4, 12, 4)
        btn.minWidth = 100
        btn.setOnClickListener {
            isEnabled = !isEnabled
            btn.text = if (isEnabled) "🟢 ON" else "⚪ OFF"
            btn.setBackgroundColor(if (isEnabled) 0xFF228833.toInt() else 0xFF444444.toInt())
            onEnabledChanged?.invoke(isEnabled)
        }
        card.addView(btn)

        val knobsRow = LinearLayout(ctx)
        knobsRow.orientation = LinearLayout.HORIZONTAL
        knobsRow.gravity = Gravity.CENTER
        knobsRow.setPadding(0, 12, 0, 4)

        val knob1 = LinearLayout(ctx)
        knob1.orientation = LinearLayout.VERTICAL
        knob1.gravity = Gravity.CENTER
        val levelKnob = KnobView(ctx)
        levelKnob.baseColor = 0xFFFF8822.toInt()
        levelKnob.value = level
        levelKnob.onValueChange = { v ->
            level = v
            onLevelChanged?.invoke(v)
        }
        knob1.addView(levelKnob, LinearLayout.LayoutParams(60, 60))
        val lbl1 = TextView(ctx)
        lbl1.text = "LEVEL"
        lbl1.setTextColor(0xFFCC9955.toInt())
        lbl1.textSize = 9f
        knob1.addView(lbl1)
        knobsRow.addView(knob1)

        card.addView(knobsRow)
        return card
    }
}
