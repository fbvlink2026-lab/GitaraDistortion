package com.gitaradistortion

import android.content.Intent
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
        root.setBackgroundColor(-0xF5F5F6)
        root.gravity = Gravity.CENTER
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(this)
        title.text = "🎸 GUITAR FX"
        title.textSize = 38f
        title.setTextColor(-0x2228F0)
        title.setShadowLayer(15f, 0f, 0f, -0x555601)
        title.gravity = Gravity.CENTER
        root.addView(title)

        val scale = ScaleAnimation(
            0.6f, 1f, 0.6f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scale.duration = 1800L
        scale.repeatMode = Animation.REVERSE
        scale.repeatCount = Animation.INFINITE
        title.startAnimation(scale)

        val sub = TextView(this)
        sub.text = "Handa na para sa iRig / USB Audio Interface"
        sub.textSize = 13f
        sub.setTextColor(-0x555556)
        sub.gravity = Gravity.CENTER
        sub.setPadding(0, 20, 0, 0)
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 2000L
        fadeIn.startOffset = 800L
        sub.startAnimation(fadeIn)
        root.addView(sub)

        val copy = TextView(this)
        copy.text = "Created by: MartoDosko\n© 2026"
        copy.textSize = 12f
        copy.setTextColor(-0x888889)
        copy.gravity = Gravity.CENTER
        copy.setPadding(0, 40, 0, 0)
        copy.startAnimation(fadeIn)
        root.addView(copy)

        setContentView(root)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2500L)
    }
}
