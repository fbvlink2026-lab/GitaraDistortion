package com.gitaradistortion

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class PedalBoardFragment : Fragment() {
    var parentPager: ViewPager2? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val ctx = inflater.context
        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF2C2C2C.toInt())
        root.setPadding(12, 12, 12, 4)

        val title = TextView(ctx)
        title.text = "🎛️  PEDAL BOARD — MGA IKINAKABIT"
        title.textSize = 18f
        title.setTextColor(0xFFFFCC00.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 4, 0, 12)
        root.addView(title)

        val hint = TextView(ctx)
        hint.text = "📦 Pumunta sa CABINET para magdagdag/pag-alis ng pedal"
        hint.textSize = 11f
        hint.setTextColor(0xFFBBBBBB.toInt())
        hint.gravity = Gravity.CENTER
        hint.setPadding(0, 0, 0, 8)
        root.addView(hint)

        val onBoard = PedalDef.onBoard()

        val grid = LinearLayout(ctx)
        grid.orientation = LinearLayout.VERTICAL
        grid.setPadding(4, 4, 4, 4)

        onBoard.chunked(2).forEach { rowPair ->
            val row = LinearLayout(ctx)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER
            rowPair.forEach { pedal ->
                row.addView(makePedalCard(ctx, pedal))
            }
            grid.addView(row)
        }

        root.addView(grid)
        return root
    }

    private fun makePedalCard(ctx: Context, pedal: PedalDef): LinearLayout {
        val card = LinearLayout(ctx)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER
        card.setPadding(10, 10, 10, 10)
        card.setBackgroundColor(0xFF3A3A3A.toInt())
        card.minimumWidth = 165

        val btnPower = Button(ctx)
        btnPower.text = "⚪ OFF"
        btnPower.setTextColor(Color.WHITE)
        btnPower.setBackgroundColor(0xFF555555.toInt())
        btnPower.textSize = 10f
        btnPower.setPadding(6, 2, 6, 2)
        btnPower.minWidth = 70
        var isOn = false
        btnPower.setOnClickListener {
            isOn = !isOn
            btnPower.text = if (isOn) "🟢 ON" else "⚪ OFF"
            btnPower.setBackgroundColor(if (isOn) pedal.themeColor or 0xFF000000.toInt() else 0xFF555555.toInt())
        }
        card.addView(btnPower)

        val name = TextView(ctx)
        name.text = "${pedal.icon} ${pedal.displayName}"
        name.setTextColor(Color.WHITE)
        name.textSize = 13f
        name.gravity = Gravity.CENTER
        name.setPadding(0, 6, 0, 4)
        card.addView(name)

        // ✅ MARAMING PIHITAN — AYON SA BAWAT PEDAL!
        val knobsRow = LinearLayout(ctx)
        knobsRow.orientation = LinearLayout.HORIZONTAL
        knobsRow.gravity = Gravity.CENTER

        pedal.knobLabels.forEach { label ->
            val col = LinearLayout(ctx)
            col.orientation = LinearLayout.VERTICAL
            col.gravity = Gravity.CENTER

            val knob = KnobView(ctx)
            knob.baseColor = pedal.themeColor
            knob.value = 0.5f
            knob.pager = parentPager
            knob.layoutParams = LinearLayout.LayoutParams(62, 62)
            knobsRow.addView(knob)

            val lbl = TextView(ctx)
            lbl.text = label
            lbl.setTextColor(0xFFCCCCCC.toInt())
            lbl.textSize = 9f
            lbl.gravity = Gravity.CENTER
            lbl.setPadding(0, 2, 4, 0)
            col.addView(knob)
            col.addView(lbl)
            knobsRow.addView(col)
        }
        card.addView(knobsRow)
        return card
    }
}
