package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object {
        fun setMasterEnabledGlobal(enabled: Boolean) { AudioMixer.setMasterEnabled(enabled) }
        fun setNoiseGateEnabledGlobal(enabled: Boolean) { AudioMixer.updateNoiseGateEnabled(enabled) }
        fun setNoiseGateLevelGlobal(level: Float) { AudioMixer.updateNoiseGateThreshold(level) }
        fun setVolumeEnabledGlobal(enabled: Boolean) { AudioMixer.updateVolumeEnabled(enabled) }
        fun setVolumeLevelGlobal(level: Float) { AudioMixer.updateVolumeLevel(level) }
        fun setGainEnabledGlobal(enabled: Boolean) { AudioMixer.updateGainEnabled(enabled) }
        fun setGainLevelGlobal(level: Float) { AudioMixer.updateGainLevel(level) }
        fun setOverdriveEnabledGlobal(enabled: Boolean) { AudioMixer.updateOverdriveEnabled(enabled) }
        fun setOverdriveLevelGlobal(level: Float) { AudioMixer.updateOverdriveLevel(level) }
        fun setDistortionEnabledGlobal(enabled: Boolean) { AudioMixer.updateDistortionEnabled(enabled) }
        fun setDistortionLevelGlobal(level: Float) { AudioMixer.updateDistortionLevel(level) }
        fun setToneEnabledGlobal(enabled: Boolean) { AudioMixer.updateToneEnabled(enabled) }
        fun setToneLevelGlobal(level: Float) { AudioMixer.updateToneLevel(level) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        } else {
            AudioEngine.start(this)
        }

        if (savedInstanceState == null) {
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentContainer, PedalBoardFragment())
            ft.commit()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.firstOrNull() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Pinahintulutan na!", Toast.LENGTH_SHORT).show()
            AudioEngine.start(this)
        } else {
            Toast.makeText(this, "❌ Kailangan ng pahintulot sa Mikropono!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioEngine.stop()
    }
}
