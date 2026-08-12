package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    private var isAudioOn = false

    // ✅ TAWAG SA AUDIO ENGINE — DITO LANG!
    private external fun startAudioEngine(): Unit
    private external fun stopAudioEngine(): Unit

    private external fun setVolumeEnabled(e: Boolean): Unit
    private external fun setVolumeLevel(v: Float): Unit
    private external fun setNoiseGateEnabled(e: Boolean): Unit
    private external fun setNoiseGateLevel(v: Float): Unit

    companion object {
        private lateinit var instance: MainActivity
        fun setNoiseGateEnabledGlobal(e: Boolean) = instance.setNoiseGateEnabled(e)
        fun setNoiseGateLevelGlobal(v: Float) = instance.setNoiseGateLevel(v)
        fun setVolumeEnabledGlobal(e: Boolean) = instance.setVolumeEnabled(e)
        fun setVolumeLevelGlobal(v: Float) = instance.setVolumeLevel(v)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())

        val pager = ViewPager2(this)
        val adapter = PanelAdapter(this)
        pager.adapter = adapter
        root.addView(pager, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
        ))

        setContentView(root)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Pahintulot nakuha! I-ON ang audio!", Toast.LENGTH_LONG).show()
        }
    }
}
