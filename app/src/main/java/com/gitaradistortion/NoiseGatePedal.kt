package com.gitaradistortion

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class NoiseGatePedal {
    var isEnabled = true // ✅ I-ON NA AGAD!
    var threshold = 0.04f
    var decay = 0.3f
    var onEnabledChanged: ((Boolean) -> Unit)? = null
    var onThresholdChanged: ((Float) -> Unit)? = null
    var onDecayChanged: ((Float) -> Unit)? = null

    fun makeView(ctx: Context): LinearLayout {
        val card = LinearLayout(ctx)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(20, 16, 20, 16)
        card.setBackgroundColor(0xFF1E3A3A.toInt())
        card.minimumWidth = 200

        val title = TextView(ctx)
        title.text = "🚧 NOISE GATE"
        title.setTextColor(0xFF44DDDD.toInt())
        title.textSize = 15f
        title.setPadding(0, 0, 0, 10)
        card.addView(title)

        val btn = Button(ctx)
        btn.text = "🟢 ON" // ✅ I-ON NA AGAD!
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundColor(0xFF228866.toInt()) // ✅ BERDE = I-ON
        btn.textSize = 12f
        btn.setPadding(12, 4, 12, 4)
        btn.minWidth = 100
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
        knobsRow.setPadding(0, 12, 0, 4)

        val knob1 = LinearLayout(ctx)
        knob1.orientation = LinearLayout.VERTICAL
        knob1.gravity = Gravity.CENTER
        val thresholdKnob = KnobView(ctx)
        thresholdKnob.baseColor = 0xFF44DDDD.toInt()
        thresholdKnob.value = threshold
        thresholdKnob.onValueChange = { v ->
            threshold = v
            onThresholdChanged?.invoke(v)
        }
        knob1.addView(thresholdKnob, LinearLayout.LayoutParams(60, 60))
        val lbl1 = TextView(ctx)
        lbl1.text = "THRESHOLD"
        lbl1.setTextColor(0xFF88AAAA.toInt())
        lbl1.textSize = 9f
        knob1.addView(lbl1)
        knobsRow.addView(knob1)

        val knob2 = LinearLayout(ctx)
        knob2.orientation = LinearLayout.VERTICAL
        knob2.gravity = Gravity.CENTER
        val decayKnob = KnobView(ctx)
        decayKnob.baseColor = 0xFF44DDDD.toInt()
        decayKnob.value = decay
        decayKnob.onValueChange = { v ->
            decay = v
            onDecayChanged?.invoke(v)
        }
        knob2.addView(decayKnob, LinearLayout.LayoutParams(60, 60))
        val lbl2 = TextView(ctx)
        lbl2.text = "DECAY"
        lbl2.setTextColor(0xFF88AAAA.toInt())
        lbl2.textSize = 9f
        knob2.addView(lbl2)
        knobsRow.addView(knob2)

        card.addView(knobsRow)
        return card
    }
}
