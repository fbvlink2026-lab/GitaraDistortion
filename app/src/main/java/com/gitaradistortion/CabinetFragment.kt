package com.gitaradistortion

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

class CabinetFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val ctx = inflater.context
        val root = LinearLayout(ctx)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF1A1A2E.toInt())
        root.setPadding(16, 24, 16, 16)

        val title = TextView(ctx)
        title.text = "📦  PEDAL CABINET"
        title.textSize = 22f
        title.setTextColor(0xFF66DDFF.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 8, 0, 16)
        root.addView(title)

        val hint = TextView(ctx)
        hint.text = "Pumili ng pedal → I-kabit sa Board"
        hint.textSize = 13f
        hint.setTextColor(0xFFAAAAAA.toInt())
        hint.gravity = Gravity.CENTER
        hint.setPadding(0, 0, 0, 16)
        root.addView(hint)

        // ✅ LISTAHAN NG LAHAT NG PEDAL
        val list = LinearLayout(ctx)
        list.orientation = LinearLayout.VERTICAL

        val allPedals = listOf(
            "🚧 NOISE GATE",
            "🔊 VOLUME",
            "⚡ GAIN",
            "🔥 OVERDRIVE",
            "💥 DISTORTION",
            "🎵 TONE",
            "🌊 REVERB",
            "🫧 PHASER",
            "⏱️ DELAY",
            "🎙️ WAH-WAH"
        )

        allPedals.forEach { name ->
            val row = LinearLayout(ctx)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(14, 12, 14, 12)
            row.setBackgroundColor(0xFF252540.toInt())

            val tv = TextView(ctx)
            tv.text = name
            tv.setTextColor(Color.WHITE)
            tv.textSize = 15f
            tv.gravity = Gravity.START
            tv.width = 0
            tv.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            row.addView(tv)

            val btn = Button(ctx)
            btn.text = "IKABIT"
            btn.setTextColor(Color.WHITE)
            btn.setBackgroundColor(0xFF2288AA.toInt())
            btn.textSize = 11f
            btn.setPadding(12, 4, 12, 4)
            btn.minWidth = 75
            row.addView(btn)

            list.addView(row)
        }
        root.addView(list)
        return root
    }
}
