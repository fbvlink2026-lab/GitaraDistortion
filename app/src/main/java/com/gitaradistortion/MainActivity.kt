package com.gitaradistortion

import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

// 🎛️ BILOG NA PIHITAN
class KnobView(context: android.content.Context) : View(context) {
    var value = 0.5f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }
    
    var onValueChange: ((Float) -> Unit)? = null
    var baseColor = 0xFFFF8822.toInt()

    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    private fun getGlowColor(): Int {
        val f = 0.35f + value * 0.65f
        val r = ((baseColor shr 16 and 0xFF) * f).toInt().coerceAtMost(255)
        val g = ((baseColor shr 8 and 0xFF) * f).toInt().coerceAtMost(255)
        val b = ((baseColor and 0xFF) * f).toInt().coerceAtMost(255)
        return android.graphics.Color.rgb(r, g, b)
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2
        val cy = h / 2
        val r = minOf(w, h) / 2 - 6f
        val glow = getGlowColor()

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFF202020.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 3.5f + value * 4f
        paint.color = glow
        canvas.drawCircle(cx, cy, r - 3f, paint)

        val capR = r * 0.72f
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFFF0F0F0.toInt()
        canvas.drawCircle(cx, cy, capR, paint)

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = 0xFFCCCCCC.toInt()
        canvas.drawCircle(cx, cy, capR - 1.5f, paint)

        val angle = -135f + (270f) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 5f + value * 3f
        paint.color = glow
        val len = capR * 0.8f
        val endX = cx + len * sin(rad).toFloat()
        val endY = cy - len * cos(rad).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = glow
        canvas.drawCircle(cx, cy, 8f + value * 4f, paint)
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
    private var isOn = false

    // ✅ SUSULITAN LANG — WALANG C++ MUNA!
    private external fun startAudioEngine(): Boolean
    private external fun stopAudioEngine(): Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(24, 30, 24, 24)

        val title = TextView(this)
        title.text = "🎸 GITARA DISTORTION"
        title.textSize = 28f
        title.setTextColor(0xFFFF8822.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 10, 0, 20)
        root.addView(title)

        // ========== HANAY 1 ==========
        val row1 = LinearLayout(this)
        row1.orientation = LinearLayout.HORIZONTAL
        row1.gravity = Gravity.CENTER

        fun makeKnob(label: String, color: Int): LinearLayout {
            val col = LinearLayout(this)
            col.orientation = LinearLayout.VERTICAL
            col.gravity = Gravity.CENTER
            val txt = TextView(this)
            txt.text = "$label\n50%"
            txt.textSize = 13f
            txt.setTextColor(color)
            txt.gravity = Gravity.CENTER
            val knob = KnobView(this)
            knob.layoutParams = LinearLayout.LayoutParams(160, 160)
            knob.baseColor = color
            knob.value = 0.5f
            knob.onValueChange = { v ->
                txt.text = "$label\n${(v * 100).toInt()}%"
            }
            col.addView(knob)
            col.addView(txt)
            return col
        }

        row1.addView(makeKnob("🔊 VOLUME", 0xFFFF8822.toInt()))
        row1.addView(makeKnob("🎵 TONE", 0xFF44DD88.toInt()))
        row1.addView(makeKnob("💥 DIST", 0xFFFF4444.toInt()))
        root.addView(row1)

        // ========== HANAY 2 ==========
        val row2 = LinearLayout(this)
        row2.orientation = LinearLayout.HORIZONTAL
        row2.gravity = Gravity.CENTER
        row2.setPadding(0, 10, 0, 0)

        row2.addView(makeKnob("⚡ GAIN", 0xFFFFCC00.toInt()))
        row2.addView(makeKnob("🎶 CHORUS", 0xFF44AAFF.toInt()))
        row2.addView(makeKnob("🌊 REVERB", 0xFFAA66FF.toInt()))
        root.addView(row2)

        // ========== ON/OFF BUTTON ==========
        val statusText = TextView(this)
        statusText.text = "🔴 NAKA-OFF"
        statusText.textSize = 16f
        statusText.setTextColor(0xFFFF6666.toInt())
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 30, 0, 12)
        root.addView(statusText)

        val btn = android.widget.Button(this)
        btn.text = "🔘 TURN ON"
        btn.textSize = 17f
        btn.setBackgroundColor(0xFF228833.toInt())
        btn.setTextColor(android.graphics.Color.WHITE)
        btn.setPadding(55, 16, 55, 16)
        btn.setOnClickListener {
            isOn = !isOn
            if (isOn) {
                statusText.text = "🟢 AUDIO — SUSULITAN LANG"
                statusText.setTextColor(0xFF44FF44.toInt())
                Toast.makeText(this, "✅ Kung BERDE — ibabalik na natin ang C++!", Toast.LENGTH_LONG).show()
            } else {
                statusText.text = "🔴 NAKA-OFF"
                statusText.setTextColor(0xFFFF6666.toInt())
            }
        }
        root.addView(btn)

        setContentView(root)
    }
}
