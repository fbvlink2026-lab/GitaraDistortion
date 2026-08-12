package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    private var isAudioOn = false

    // ✅ Tawag sa AudioEngine — TUGMA SA d1f6612
    private external fun startAudioEngine(): Unit
    private external fun stopAudioEngine(): Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ PANGUNAHING LAYOUT
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())

        // ✅ DALAWANG PANEL — PEDAL BOARD ↔ CABINET
        val pager = ViewPager2(this)
        val adapter = PanelAdapter(this)
        pager.adapter = adapter

        // ✅ IPASOK SA SCREEN
        root.addView(pager, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
        ))
        setContentView(root)

        // ✅ HINGI NG PAHINTULOT SA MIKROPONO
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }
}
