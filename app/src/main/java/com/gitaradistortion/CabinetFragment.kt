package com.gitaradistortion

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        root.setBackgroundColor(0xFF1E1E1E.toInt())
        root.setPadding(12, 12, 12, 12)

        val title = TextView(ctx)
        title.text = "📦  PEDAL CABINET — MGA HINDI GINAGAMIT"
        title.textSize = 18f
        title.setTextColor(0xFF66DDFF.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 4, 0, 12)
        root.addView(title)

        val hint = TextView(ctx)
        hint.text = "👉 Ilipat sa PEDAL BOARD para makapaglaro"
        hint.textSize = 11f
        hint.setTextColor(0xFF999999.toInt())
        hint.gravity = Gravity.CENTER
        hint.setPadding(0, 0, 0, 10)
        root.addView(hint)

        val inCab = listOf("GAIN", "OVERDRIVE", "DISTORTION", "PHASER", "DELAY", "WAH-WAH")
        val list = LinearLayout(ctx)
        list.orientation = LinearLayout.VERTICAL
        list.setPadding(4, 4, 4, 4)

        inCab.forEach { name ->
            val item = LinearLayout(ctx)
            item.orientation = LinearLayout.HORIZONTAL
            item.gravity = Gravity.CENTER_VERTICAL
            item.setPadding(12, 10, 12, 10)
            item.setBackgroundColor(0xFF2A2A2A.toInt())

            val nameTv = TextView(ctx)
            nameTv.text = "🎛️   $name"
            nameTv.setTextColor(Color.WHITE)
            nameTv.textSize = 14f
            nameTv.gravity = Gravity.START
            nameTv.setPadding(8, 0, 0, 0)
            item.addView(nameTv)
            list.addView(item)
        }
        root.addView(list)
        return root
    }
}
