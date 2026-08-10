package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.math.*

// 🎛️ BILOG NA PIHITAN — PARANG TUNAY NA AMPLIFIER!
class KnobView(context: android.content.Context) : View(context) {
    var value = 0.5f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }
    
    var onValueChange: ((Float) -> Unit)? = null
    var color = 0xFFFF6622.toInt()

    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: android.graphics.Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2
        val cy = h / 2
        val r = minOf(w, h) / 2 - 12f

        // Bilog na katawan
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFF2A2A30.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        // Gilid na kulay
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = color
        canvas.drawCircle(cx, cy, r - 3f, paint)

        // Pihitang linya
        val angle = -135f + (270f) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 5f
        paint.color = color
        val endX = cx + (r - 25f) * sin(rad).toFloat()
        val endY = cy - (r - 25f) * cos(rad).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)

        // Gitna ng pihitan
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = color
        canvas.drawCircle(cx, cy, 12f, paint)
    }

    private var startAngle = 0.0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cx = width / 2f
        val cy = height / 2f
        val angle = Math.toDegrees(atan2(event.x - cx, cy - event.y).toDouble())

        when (event.action) {
            MotionEvent.ACTION_DOWN -> startAngle = angle
            MotionEvent.ACTION_MOVE -> {
                var delta = angle - startAngle
                if (delta > 180) delta -= 360.0
                if (delta < -180) delta += 360.0
                value += (delta / 270.0).toFloat()
                onValueChange?.invoke(value)
                startAngle = angle
            }
        }
        return true
    }
}

class MainActivity : AppCompatActivity() {
    companion object {
        init {
            System.loadLibrary("gitaradistortion")
        }
    }

    // ============== MGA UTOS PATUNGO SA C++ ==============
    external fun startAudioEngine()
    external fun stopAudioEngine()
    external fun setVolume(v: Float)
    external fun setTone(v: Float)
    external fun setDistortion(v: Float)

    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Humingi ng pahintulot na gamitin ang mikropono/iRig
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }

        // ============== IKONEKTA ANG MGA BAGAY SA SCREEN ==============
        val statusText = findViewById<TextView>(R.id.statusText)
        val volumeText = findViewById<TextView>(R.id.volumeText)
        val toneText = findViewById<TextView>(R.id.toneText)
        val distText = findViewById<TextView>(R.id.distortionText)
        val volKnob = findViewById<KnobView>(R.id.volKnob)
        val toneKnob = findViewById<KnobView>(R.id.toneKnob)
        val distKnob = findViewById<KnobView>(R.id.distKnob)
        val powerBtn = findViewById<Button>(R.id.powerBtn)
        val emergencyBtn = findViewById<Button>(R.id.emergencyBtn)

        // 🔊 VOLUME PIHITAN
        volKnob.color = 0xFFFF8822.toInt()
        volKnob.value = 0.5f
        volKnob.onValueChange = { v ->
            setVolume(v)
            volumeText.text = "🔊 VOLUME: ${(v * 100).toInt()}%"
        }

        // 🎵 TONE PIHITAN
        toneKnob.color = 0xFF22DD88.toInt()
        toneKnob.value = 0.5f
        toneKnob.onValueChange = { v ->
            val toneValue = 0.05f + v * 2.95f
            setTone(toneValue)
            toneText.text = "🎵 TONE: ${(v * 100).toInt()}%"
        }

        // 💥 DISTORTION PIHITAN
        distKnob.color = 0xFFFF3333.toInt()
        distKnob.value = 0.5f
        distKnob.onValueChange = { v ->
            val distValue = 0.5f + v * 9.5f
            setDistortion(distValue)
            distText.text = "💥 DIST: %.1fx".format(distValue)
        }

        // 🟢🔴 POWER BUTTON
        powerBtn.setOnClickListener {
            if (!isRunning) {
                startAudioEngine()
                isRunning = true
                powerBtn.text = "🟢 TURN OFF"
                powerBtn.setBackgroundColor(0xFFFF4444.toInt())
                statusText.text = "🟢 NAKA-ON! Tumugtog ka na!"
                statusText.setTextColor(0xFF44FF44.toInt())
            } else {
                stopAudioEngine()
                isRunning = false
                powerBtn.text = "🔴 TURN ON"
                powerBtn.setBackgroundColor(0xFF228833.toInt())
                statusText.text = "⚪ NAKA-OFF — Isaksak iRig"
                statusText.setTextColor(0xFF888888.toInt())
            }
        }

        // 🚨 EMERGENCY BUTTON
        emergencyBtn.setOnClickListener {
            stopAudioEngine()
            isRunning = false
            volKnob.value = 0f; setVolume(0f)
            toneKnob.value = 0.5f
            distKnob.value = 0.5f
            volumeText.text = "🔊 VOLUME: 0%"
            toneText.text = "🎵 TONE: 50%"
            distText.text = "💥 DIST: 5.3x"
            powerBtn.text = "🔴 TURN ON"
            powerBtn.setBackgroundColor(0xFF228833.toInt())
            statusText.text = "🚨 EMERGENCY — LAHAT TUMIGIL"
            statusText.setTextColor(0xFFFF4444.toInt())
        }
    }
}
