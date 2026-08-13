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
        fun setMasterEnabledGlobal(enabled: Boolean) { 
            AudioMixer.setMasterEnabled(enabled) 
        }
        private var audioStarted = false
        
        fun startAudioSafe(ctx: android.content.Context) {
            if(!audioStarted) {
                AudioEngine.start(ctx)
                audioStarted = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ LAGAY ANG FRAGMENT — WALANG AUDIO MUNA!
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, PedalBoardFragment())
                .commit()
        }

        // ✅ HINTAYIN ANG PAHINTULOT — HUWAG AGAD SIMULAN ANG AUDIO!
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Handa na! Pumili ng pedal → I-ON!", Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Pahintulot natanggap! Piliin at I-ON ang pedal!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "❌ Kailangan ng pahintulot sa Mikropono!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioEngine.stop()
    }
}
