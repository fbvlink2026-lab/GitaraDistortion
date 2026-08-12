package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ PAALALA — KATULAD NG TONEBRIDGE!
        val hint = findViewById<TextView>(R.id.iRigHint)
        hint?.text = "🔌 Isaksak ang iRig → Pahintulutan ang Mikropono → I-ON ang MASTER"

        // ✅ HUMINGI NG PAHINTULOT — KAILANGAN NG iRIG!
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }
    }
}
