package com.gitaradistortion

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class WahPedal {
    var isEnabled = false
    var position = 0.5f
    var resonance = 0.5f
    var level = 0.7f
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onPositionChanged: ((Float) -> Unit)? = null
    var onResonanceChanged: ((Float) -> Unit)? = null
    var onLevelChanged: ((Float) -> Unit)? = null

    fun makeView(ctx: Context): LinearLayout {
        val card = LinearLayout(ctx)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(8, 8, 8, 8)
        card.setBackgroundColor(0xFF401030.toInt())
        card.minimumWidth = 180

        val title = TextView(ctx)
        title.text = "🎵 WAH-WAH"
        title.setTextColor(0xFFFF66AA.toInt())
        title.textSize = 14f
        title.setPadding(0, 0, 0, 4)
        card.addView(title)

        val btn = Button(ctx)
        btn.text = "⚪ OFF"
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF444444.toInt())
        btn.textSize = 11f
        btn.setPadding(6, 2, 6, 2)
        btn.minWidth = 80
        btn.setOnClickListener {
            isEnabled = !isEnabled
            btn.text = if (isEnabled) "🟢 ON" else "⚪ OFF"
            btn.setBackgroundColor(if (isEnabled) 0xFFCC2266.toInt() else 0xFF444444.toInt())
            onEnabledChanged?.invoke(isEnabled)
        }
        card.addView(btn)

        val row1 = LinearLayout(ctx)
        row1.orientation = LinearLayout.HORIZONTAL
        row1.gravity = Gravity.CENTER
        row1.setPadding(4, 6, 4, 2)
        val posKnob = makeKnob(ctx, "PEDAL", position, 0xFFFF66AA.toInt()) {
            position = it
            onPositionChanged?.invoke(it)
        }
        val resKnob = makeKnob(ctx, "RESONANCE", resonance, 0xFFFF99CC.toInt()) {
            resonance = it
            onResonanceChanged?.invoke(it)
        }
        row1.addView(posKnob.view, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row1.addView(resKnob.view, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        card.addView(row1)

        val row2 = LinearLayout(ctx)
        row2.orientation = LinearLayout.HORIZONTAL
        row2.gravity = Gravity.CENTER
        row2.setPadding(4, 2, 4, 6)
        val lvlKnob = makeKnob(ctx, "LEVEL", level, 0xFFFFCCDD.toInt()) {
            level = it
            onLevelChanged?.invoke(it)
        }
        row2.addView(lvlKnob.view, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        card.addView(row2)
        return card
    }

    private fun makeKnob(ctx: Context, label: String, initVal: Float, color: Int, onChange: (Float) -> Unit): KnobResult {
        val col = LinearLayout(ctx)
        col.orientation = LinearLayout.VERTICAL
        col.gravity = Gravity.CENTER
        val knob = KnobView(ctx)
        knob.baseColor = color
        knob.value = initVal
        val pct = TextView(ctx)
        pct.text = if (label == "PEDAL") {
            when {
                initVal < 0.33f -> "LOW"
                initVal < 0.66f -> "MID"
                else -> "HIGH"
            }
        } else "${(initVal * 100).toInt()}%"
        pct.setTextColor(color)
        pct.textSize = 9f
        knob.onValueChange = {
            onChange(it)
            pct.text = if (label == "PEDAL") {
                when {
                    it < 0.33f -> "LOW"
                    it < 0.66f -> "MID"
                    else -> "HIGH"
                }
            } else "${(it * 100).toInt()}%"
        }
        col.addView(knob, LinearLayout.LayoutParams(65, 65))
        col.addView(pct)
        val lbl = TextView(ctx)
        lbl.text = label
        lbl.setTextColor(color)
        lbl.textSize = 8f
        col.addView(lbl)
        return KnobResult(col)
    }
    data class KnobResult(val view: LinearLayout)
}
