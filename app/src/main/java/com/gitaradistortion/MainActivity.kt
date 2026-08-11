package com.gitaradistortion

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

// 🎛️ BILOG NA PIHITAN — PARANG TUNAY NA PEDAL!
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
        paint.color = 0xFF222222.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        // Gilid na kulay
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = color
        canvas.drawCircle(cx, cy, r - 2f, paint)

        // Pihitang linya
        val angle = -135f + (270f) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 4f
        paint.color = color
        val endX = cx + (r - 22f) * sin(rad).toFloat()
        val endY = cy - (r - 22f) * cos(rad).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)

        // Gitna
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = color
        canvas.drawCircle(cx, cy, 10f, paint)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🎛️ IKONEKTA ANG MGA BAGAY — WALANG AUDIO MUNA!
        val statusText = findViewById<TextView>(R.id.statusText)
        val volumeText = findViewById<TextView>(R.id.volumeText)
        val toneText = findViewById<TextView>(R.id.toneText)
        val distText = findViewById<TextView>(R.id.distortionText)
        val volKnob = findViewById<KnobView>(R.id.volKnob)
        val toneKnob = findViewById<KnobView>(R.id.toneKnob)
        val distKnob = findViewById<KnobView>(R.id.distKnob)
        val powerBtn = findViewById<Button>(R.id.powerBtn)
        val emergencyBtn = findViewById<Button>(R.id.emergencyBtn)

        // 🔊 VOLUME
        volKnob.color = 0xFFFF8822.toInt()
        volKnob.value = 0.5f
        volKnob.onValueChange = { v ->
            volumeText.text = "🔊 VOLUME: ${(v * 100).toInt()}%"
        }

        // 🎵 TONE
        toneKnob.color = 0xFF22DD88.toInt()
        toneKnob.value = 0.5f
        toneKnob.onValueChange = { v ->
            toneText.text = "🎵 TONE: ${(v * 100).toInt()}%"
        }

        // 💥 DISTORTION
        distKnob.color = 0xFFFF3333.toInt()
        distKnob.value = 0.5f
        distKnob.onValueChange = { v ->
            distText.text = "💥 DISTORT: ${(v * 100).toInt()}%"
        }

        // 🟢🔴 POWER BUTTON — WALANG AUDIO MUNA!
        powerBtn.setOnClickListener {
            statusText.text = "✅ GUMAGANA ANG SCREEN!"
            statusText.setTextColor(0xFF44FF44.toInt())
        }

        // 🚨 EMERGENCY
        emergencyBtn.setOnClickListener {
            statusText.text = "🚨 NAKA-OFF"
            statusText.setTextColor(0xFFFF4444.toInt())
        }
    }
}
