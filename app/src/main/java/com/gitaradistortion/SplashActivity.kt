package com.gitaradistortion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        val root = LinearLayout(this)
        root.setBackgroundColor(0xFF0A0A0A.toInt())
        root.gravity = Gravity.CENTER
        root.orientation = LinearLayout.VERTICAL

        // ✅ GUITAR FX — ANIMATED!
        val title = TextView(this)
        title.text = "🎸 GUITAR FX"
        title.textSize = 38f
        title.setTextColor(0xFFFFD700.toInt()) // GINTO
        title.setShadowLayer(15f, 0f, 0f, 0xFFFFAA00.toInt())
        title.gravity = Gravity.CENTER
        root.addView(title)

        // ✅ ANIMATION: LUMALAKI + KUMIKITAKIT
        val scale = ScaleAnimation(
            0.6f, 1f, 0.6f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scale.duration = 1800
        scale.repeatMode = Animation.REVERSE
        scale.repeatCount = Animation.INFINITE
        title.startAnimation(scale)

        // ✅ SUBTITLE
        val sub = TextView(this)
        sub.text = "Handa na para sa iRig / USB Audio Interface"
        sub.textSize = 13f
        sub.setTextColor(0xFFAAAAAA.toInt())
        sub.gravity = Gravity.CENTER
        sub.setPadding(0, 20, 0, 0)
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 2000
        fadeIn.startOffset = 800
        sub.startAnimation(fadeIn)
        root.addView(sub)

        // ✅ COPYRIGHT
        val copy = TextView(this)
        copy.text = "Created by: MartoDosko\n© 2026"
        copy.textSize = 12f
        copy.setTextColor(0xFF777777.toInt())
        copy.gravity = Gravity.CENTER
        copy.setPadding(0, 40, 0, 0)
        copy.startAnimation(fadeIn)
        root.addView(copy)

        setContentView(root)

        // ✅ AWTOMATIK PUMUNTA SA MAIN AFTER 2.5 SEGUNDOS
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2500)
    }
}
