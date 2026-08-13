package com.gitaradistortion

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class DistortionPedal {
    var isEnabled = false
    var level = 0.5f
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onLevelChanged: ((Float) -> Unit)? = null

    fun makeView(ctx: Context): LinearLayout {
        val card = LinearLayout(ctx)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(12, 12, 12, 12)
        card.setBackgroundColor(0xFF3A1010.toInt())
        card.minimumWidth = 180

        val title = TextView(ctx)
        title.text = "🔴 DISTORTION"
        title.setTextColor(0xFFFF4422.toInt())
        title.textSize = 14f
        title.setPadding(0, 0, 0, 6)
        card.addView(title)

        val btn = Button(ctx)
        btn.text = "⚪ OFF"
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF444444.toInt())
        btn.textSize = 11f
        btn.setPadding(8, 2, 8, 2)
        btn.minWidth = 90
        btn.setOnClickListener {
            isEnabled = !isEnabled
            btn.text = if (isEnabled) "🟢 ON" else "⚪ OFF"
            btn.setBackgroundColor(if (isEnabled) 0xFFCC3322.toInt() else 0xFF444444.toInt())
            onEnabledChanged?.invoke(isEnabled)
        }
        card.addView(btn)

        val knobsRow = LinearLayout(ctx)
        knobsRow.orientation = LinearLayout.HORIZONTAL
        knobsRow.gravity = Gravity.CENTER
        knobsRow.setPadding(4, 8, 4, 4)

        val knobCol = LinearLayout(ctx)
        knobCol.orientation = LinearLayout.VERTICAL
        knobCol.gravity = Gravity.CENTER
        val levelKnob = KnobView(ctx)
        levelKnob.baseColor = 0xFFFF4422.toInt()
        levelKnob.value = level
        val levelPct = TextView(ctx)
        levelPct.text = "50%"
        levelPct.setTextColor(0xFFDD7755.toInt())
        levelPct.textSize = 10f
        levelKnob.onValueChange = { v ->
            level = v
            levelPct.text = "${(v * 100).toInt()}%"
            onLevelChanged?.invoke(v)
        }
        knobCol.addView(levelKnob, LinearLayout.LayoutParams(90, 90))
        knobCol.addView(levelPct)
        val lbl = TextView(ctx)
        lbl.text = "AMOUNT"
        lbl.setTextColor(0xFFDD7755.toInt())
        lbl.textSize = 9f
        knobCol.addView(lbl)
        knobsRow.addView(knobCol)

        card.addView(knobsRow)
        return card
    }
}
