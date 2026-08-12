package com.gitaradistortion

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(0xFF121212.toInt())

        // ✅ GUITAR FX — GUMAGALAW
        val title = TextView(this)
        title.text = "GUITAR FX"
        title.textSize = 48f
        title.setTextColor(0xFFFFCC00.toInt())
        title.setShadowLayer(8f, 0f, 4f, 0xAA000000.toInt())
        title.gravity = Gravity.CENTER
        title.visibility = View.INVISIBLE
        root.addView(title)

        // ✅ PAGKAGALAW — LUMALABAS AT LUMALAKI
        val anim = AnimationSet(true).apply {
            addAnimation(ScaleAnimation(0.5f, 1f, 0.5f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f).apply {
                duration = 1200
            })
            addAnimation(AlphaAnimation(0f, 1f).apply { duration = 1200 })
        }

        // ✅ SA IBABA — NAGLALOAD
        val sub = TextView(this)
        sub.text = "Inihahanda ang tunog..."
        sub.textSize = 14f
        sub.setTextColor(0xFF999999.toInt())
        sub.setPadding(0, 24, 0, 0)
        sub.gravity = Gravity.CENTER
        root.addView(sub)

        setContentView(root)

        // ✅ SIMULAN ANG GALAW PAGKABUKAS
        Handler(Looper.getMainLooper()).postDelayed({
            title.visibility = View.VISIBLE
            title.startAnimation(anim)
        }, 200)

        // ✅ AWTOMATIK LIPAT PAGKATAPOS NG 2.5 SEGUNDO
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2500)
    }
}
