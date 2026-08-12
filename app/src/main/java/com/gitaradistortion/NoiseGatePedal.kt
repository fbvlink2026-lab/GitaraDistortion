package com.gitaradistortion

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class NoiseGatePedal {
    var isEnabled = true
    var threshold = 0.04f
    var decay = 0.3f
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onThresholdChanged: ((Float) -> Unit)? = null
    var onDecayChanged: ((Float) -> Unit)? = null

    fun makeView(ctx: Context): LinearLayout {
        val card = LinearLayout(ctx)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(12, 12, 12, 12)
        card.setBackgroundColor(0xFF1E3A3A.toInt())
        card.minimumWidth = 180

        val title = TextView(ctx)
        title.text = "🚧 NOISE GATE"
        title.setTextColor(0xFF44DDDD.toInt())
        title.textSize = 14f
        title.setPadding(0, 0, 0, 6)
        card.addView(title)

        val btn = Button(ctx)
        btn.text = "🟢 ON"
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF228866.toInt())
        btn.textSize = 11f
        btn.setPadding(8, 2, 8, 2)
        btn.minWidth = 90
        btn.setOnClickListener {
            isEnabled = !isEnabled
            btn.text = if (isEnabled) "🟢 ON" else "⚪ OFF"
            btn.setBackgroundColor(if (isEnabled) 0xFF228866.toInt() else 0xFF444444.toInt())
            onEnabledChanged?.invoke(isEnabled)
        }
        card.addView(btn)

        val knobsRow = LinearLayout(ctx)
        knobsRow.orientation = LinearLayout.HORIZONTAL
        knobsRow.gravity = Gravity.CENTER
        knobsRow.setPadding(4, 8, 4, 4)

        // ✅ THRESHOLD — MALAKI ANG PIHITAN + MAY %!
        val knob1Col = LinearLayout(ctx)
        knob1Col.orientation = LinearLayout.VERTICAL
        knob1Col.gravity = Gravity.CENTER
        val thresholdKnob = KnobView(ctx)
        thresholdKnob.baseColor = 0xFF44DDDD.toInt()
        thresholdKnob.value = threshold
        val thresholdPct = TextView(ctx)
        thresholdPct.text = "4%"
        thresholdPct.setTextColor(0xFF88AAAA.toInt())
        thresholdPct.textSize = 10f
        thresholdKnob.onValueChange = { v ->
            threshold = v
            thresholdPct.text = "${(v * 100).toInt()}%"
            onThresholdChanged?.invoke(v)
        }
        knob1Col.addView(thresholdKnob, LinearLayout.LayoutParams(90, 90)) // ✅ MALAKI!
        knob1Col.addView(thresholdPct)
        val lbl1 = TextView(ctx)
        lbl1.text = "THRESHOLD"
        lbl1.setTextColor(0xFF88AAAA.toInt())
        lbl1.textSize = 9f
        knob1Col.addView(lbl1)
        knobsRow.addView(knob1Col)

        // ✅ DECAY — MALAKI ANG PIHITAN + MAY %!
        val knob2Col = LinearLayout(ctx)
        knob2Col.orientation = LinearLayout.VERTICAL
        knob2Col.gravity = Gravity.CENTER
        val decayKnob = KnobView(ctx)
        decayKnob.baseColor = 0xFF44DDDD.toInt()
        decayKnob.value = decay
        val decayPct = TextView(ctx)
        decayPct.text = "30%"
        decayPct.setTextColor(0xFF88AAAA.toInt())
        decayPct.textSize = 10f
        decayKnob.onValueChange = { v ->
            decay = v
            decayPct.text = "${(v * 100).toInt()}%"
            onDecayChanged?.invoke(v)
        }
        knob2Col.addView(decayKnob, LinearLayout.LayoutParams(90, 90)) // ✅ MALAKI!
        knob2Col.addView(decayPct)
        val lbl2 = TextView(ctx)
        lbl2.text = "DECAY"
        lbl2.setTextColor(0xFF88AAAA.toInt())
        lbl2.textSize = 9f
        knob2Col.addView(lbl2)
        knobsRow.addView(knob2Col)

        card.addView(knobsRow)
        return card
    }
}
