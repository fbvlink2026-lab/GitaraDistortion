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
    var release = 0.3f
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onThresholdChanged: ((Float) -> Unit)? = null
    var onReleaseChanged: ((Float) -> Unit)? = null

    fun makeView(ctx: Context): LinearLayout {
        val card = LinearLayout(ctx)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(8, 8, 8, 8)
        card.setBackgroundColor(0xFF002A20.toInt())
        card.minimumWidth = 170

        val title = TextView(ctx)
        title.text = "🚧 NOISE GATE"
        title.setTextColor(0xFF44DD88.toInt())
        title.textSize = 13f
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
            btn.setBackgroundColor(if (isEnabled) 0xFF00AA55.toInt() else 0xFF444444.toInt())
            onEnabledChanged?.invoke(isEnabled)
        }
        card.addView(btn)

        val knobsRow = LinearLayout(ctx)
        knobsRow.orientation = LinearLayout.HORIZONTAL
        knobsRow.gravity = Gravity.CENTER
        knobsRow.setPadding(4, 6, 4, 4)

        val k1 = makeKnob(ctx, "TOLERANCE", threshold, 0xFF44DD88.toInt()) {
            threshold = it
            onThresholdChanged?.invoke(it)
        }
        val k2 = makeKnob(ctx, "RELEASE", release, 0xFF88BB88.toInt()) {
            release = it
            onReleaseChanged?.invoke(it)
        }
        knobsRow.addView(k1.view, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        knobsRow.addView(k2.view, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        card.addView(knobsRow)
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
        pct.text = "${(initVal * 100).toInt()}%"
        pct.setTextColor(color)
        pct.textSize = 9f
        knob.onValueChange = {
            pct.text = "${(it * 100).toInt()}%"
            onChange(it)
        }
        col.addView(knob, LinearLayout.LayoutParams(70, 70))
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
