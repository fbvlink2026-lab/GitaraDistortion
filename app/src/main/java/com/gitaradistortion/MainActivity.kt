package com.gitaradistortion

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
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

        // 🎸 PANGUNAHING SCREEN — PARANG PEDAL!
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.setPadding(24, 40, 24, 24)

        val title = TextView(this)
        title.text = "🎸 GITARA DISTORTION"
        title.textSize = 26f
        title.setTextColor(0xFFFF8822.toInt())
        title.setPadding(0, 20, 0, 30)
        root.addView(title)

        // 📍 LAYOUT NG TATLONG PIHITAN
        val knobsRow = LinearLayout(this)
        knobsRow.orientation = LinearLayout.HORIZONTAL
        knobsRow.setPadding(0, 10, 0, 20)

        // 🔊 VOLUME
        val volCol = LinearLayout(this)
        volCol.orientation = LinearLayout.VERTICAL
        volCol.gravity = android.view.Gravity.CENTER
        val volText = TextView(this)
        volText.text = "🔊 VOLUME\n50%"
        volText.textSize = 13f
        volText.setTextColor(0xFFFF8822.toInt())
        val volKnob = KnobView(this)
        volKnob.layoutParams = LinearLayout.LayoutParams(180, 180)
        volKnob.color = 0xFFFF8822.toInt()
        volKnob.value = 0.5f
        volKnob.onValueChange = { v ->
            volText.text = "🔊 VOLUME\n${(v * 100).toInt()}%"
        }
        volCol.addView(volKnob)
        volCol.addView(volText)
        knobsRow.addView(volCol)

        // 🎵 TONE
        val toneCol = LinearLayout(this)
        toneCol.orientation = LinearLayout.VERTICAL
        toneCol.gravity = android.view.Gravity.CENTER
        val toneText = TextView(this)
        toneText.text = "🎵 TONE\n50%"
        toneText.textSize = 13f
        toneText.setTextColor(0xFF44DD88.toInt())
        val toneKnob = KnobView(this)
        toneKnob.layoutParams = LinearLayout.LayoutParams(180, 180)
        toneKnob.color = 0xFF44DD88.toInt()
        toneKnob.value = 0.5f
        toneKnob.onValueChange = { v ->
            toneText.text = "🎵 TONE\n${(v * 100).toInt()}%"
        }
        toneCol.addView(toneKnob)
        toneCol.addView(toneText)
        knobsRow.addView(toneCol)

        // 💥 DISTORTION
        val distCol = LinearLayout(this)
        distCol.orientation = LinearLayout.VERTICAL
        distCol.gravity = android.view.Gravity.CENTER
        val distText = TextView(this)
        distText.text = "💥 DIST\n50%"
        distText.textSize = 13f
        distText.setTextColor(0xFFFF4444.toInt())
        val distKnob = KnobView(this)
        distKnob.layoutParams = LinearLayout.LayoutParams(180, 180)
        distKnob.color = 0xFFFF4444.toInt()
        distKnob.value = 0.5f
        distKnob.onValueChange = { v ->
            distText.text = "💥 DIST\n${(v * 100).toInt()}%"
        }
        distCol.addView(distKnob)
        distCol.addView(distText)
        knobsRow.addView(distCol)

        root.addView(knobsRow)
        setContentView(root)
    }
}
