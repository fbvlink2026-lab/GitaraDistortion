package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    companion object {
        // ✅ GLOBAL NA TAWAG — PARA SA PEDAL BOARD!
        fun setNoiseGateEnabledGlobal(enabled: Boolean) { AudioMixer.noiseGateOn = enabled }
        fun setNoiseGateLevelGlobal(level: Float) { AudioMixer.noiseGateThreshold = level }
        fun setVolumeEnabledGlobal(enabled: Boolean) { AudioMixer.volumeOn = enabled }
        fun setVolumeLevelGlobal(level: Float) { AudioMixer.volumeLevel = level }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.iRigHint)?.text = "🔌 Isaksak ang iRig → Pahintulutan ang Mikropono → I-ON"

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }
}
