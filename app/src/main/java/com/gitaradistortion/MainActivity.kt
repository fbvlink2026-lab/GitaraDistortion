package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.commit

class MainActivity : AppCompatActivity() {
    companion object {
        fun setNoiseGateEnabledGlobal(enabled: Boolean) { AudioMixer.updateNoiseGateEnabled(enabled) }
        fun setNoiseGateLevelGlobal(level: Float) { AudioMixer.updateNoiseGateThreshold(level) }
        fun setVolumeEnabledGlobal(enabled: Boolean) { AudioMixer.updateVolumeEnabled(enabled) }
        fun setVolumeLevelGlobal(level: Float) { AudioMixer.updateVolumeLevel(level) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ PAALALA SA ITAAS
        findViewById<TextView>(R.id.iRigHint)?.text = "🔌 Isaksak ang iRig → Pahintulutan ang Mikropono → I-ON"

        // ✅ ILABAS ANG PEDAL BOARD SA SCREEN!
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, PedalBoardFragment())
            }
        }

        // ✅ HUMINGI NG PAHINTULOT SA MIKROPOno
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }
}
