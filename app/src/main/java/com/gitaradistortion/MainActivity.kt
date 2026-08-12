package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private var isOn = false

    // ✅ LAHAT NG FUNCTION — TUGMA SA C++! WALANG KULANG!
    private external fun startAudioEngine(): Unit
    private external fun stopAudioEngine(): Unit

    private external fun setVolumeLevel(v: Float): Unit
    private external fun setVolumeEnabled(e: Boolean): Unit
    private external fun setToneLevel(v: Float): Unit
    private external fun setToneEnabled(e: Boolean): Unit
    private external fun setReverbLevel(v: Float): Unit
    private external fun setReverbEnabled(e: Boolean): Unit
    private external fun setNoiseGateLevel(v: Float): Unit
    private external fun setNoiseGateEnabled(e: Boolean): Unit
    private external fun setGainLevel(v: Float): Unit
    private external fun setGainEnabled(e: Boolean): Unit
    private external fun setOverdriveLevel(v: Float): Unit
    private external fun setOverdriveEnabled(e: Boolean): Unit
    private external fun setDistortionLevel(v: Float): Unit
    private external fun setDistortionEnabled(e: Boolean): Unit
    private external fun setPhaserLevel(v: Float): Unit
    private external fun setPhaserEnabled(e: Boolean): Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.gravity = Gravity.CENTER
        root.setPadding(20, 40, 20, 20)

        val title = TextView(this)
        title.text = "🎸 GITARA DISTORTION"
        title.textSize = 24f
        title.setTextColor(0xFFFFCC00.toInt())
        title.setPadding(0, 0, 0, 30)
        root.addView(title)

        val status = TextView(this)
        status.text = "🔴 NAKA-OFF — Isaksak ang iRig bago mag-ON"
        status.textSize = 12f
        status.setTextColor(0xFFFF6666.toInt())
        status.setPadding(0, 0, 0, 20)
        root.addView(status)

        val btnPower = Button(this)
        btnPower.text = "🔘  I-ON ANG AUDIO"
        btnPower.textSize = 18f
        btnPower.setBackgroundColor(0xFF228833.toInt())
        btnPower.setTextColor(Color.WHITE)
        btnPower.setPadding(60, 15, 60, 15)
        btnPower.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
                return@setOnClickListener
            }
            isOn = !isOn
            if (isOn) {
                startAudioEngine()
                btnPower.text = "🔴  I-OFF ANG AUDIO"
                btnPower.setBackgroundColor(0xFFFF4444.toInt())
                status.text = "🟢 GUMAGANA — Isaksak ang gitara!"
                status.setTextColor(0xFF44FF44.toInt())
                // ✅ NAKA-ON MUNA ANG VOLUME AT NOISE GATE
                setVolumeEnabled(true)
                setVolumeLevel(0.75f)
                setNoiseGateEnabled(true)
                setNoiseGateLevel(0.04f)
            } else {
                stopAudioEngine()
                btnPower.text = "🔘  I-ON ANG AUDIO"
                btnPower.setBackgroundColor(0xFF228833.toInt())
                status.text = "🔴 NAKA-OFF — Handa na"
                status.setTextColor(0xFFFF6666.toInt())
            }
        }
        root.addView(btnPower)
        setContentView(root)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Pahintulot nakuha! Pindutin muli ang button!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "⚠️ Kailangan ng pahintulot sa Mikropono!", Toast.LENGTH_LONG).show()
        }
    }
}
