package com.gitaradistortion

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "✅ GUMAGANA!\n\nLumabas na ang screen!"
        tv.textSize = 24f
        tv.setTextColor(0xFF44FF44.toInt())
        tv.setPadding(50, 100, 50, 50)
        setContentView(tv)
    }
}
