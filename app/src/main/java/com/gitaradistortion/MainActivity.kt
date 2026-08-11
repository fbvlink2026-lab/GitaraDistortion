package com.gitaradistortion

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ✅ PINAKA-SIMPLE NA SCREEN — WALANG KAHIT ANONG KULANG!
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(0xFF111111.toInt())
        layout.setPadding(32, 32, 32, 32)

        val title = TextView(this)
        title.text = "🎸 GITARA DISTORTION"
        title.textSize = 28f
        title.setTextColor(0xFFFF8822.toInt())
        title.setPadding(0, 50, 0, 30)

        val msg = TextView(this)
        msg.text = "✅ GUMAGANA ANG APP!\n\nLumabas na ang screen!\nHindi na nagsasara!"
        msg.textSize = 20f
        msg.setTextColor(0xFF44FF44.toInt())
        msg.setPadding(0, 30, 0, 30)

        layout.addView(title)
        layout.addView(msg)

        setContentView(layout)
    }
}
