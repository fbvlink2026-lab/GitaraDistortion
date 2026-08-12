package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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

    // ✅ AUDIO FUNCTIONS — MAY ANTAS + ON/OFF
    private external fun startAudioEngine(): Unit
    private external fun stopAudioEngine(): Unit

    private external fun setVolumeLevel(v: Float): Unit
    private external fun setVolumeEnabled(e: Boolean): Unit
    private external fun setToneLevel(v: Float): Unit
    private external fun setToneEnabled(e: Boolean): Unit
    private external fun setReverbLevel(v: Float): Unit
    private external fun setReverbEnabled(e: Boolean): Unit
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
        System.loadLibrary("gitaradistortion")

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(16, 20, 16, 16)

        val title = TextView(this)
        title.text = "🎸 GITARA DISTORTION"
        title.textSize = 24f
        title.setTextColor(0xFFFF8822.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 8, 0, 12)
        root.addView(title)

        // ✅ PIHITAN + ON/OFF BUTTON
        fun makeKnobRow(label: String, color: Int, defaultValue: Float,
                         onValue: (Float) -> Unit,
                         onSwitch: (Boolean) -> Unit): LinearLayout {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER
            row.setPadding(0, 4, 0, 4)

            // ✅ ON/OFF BUTTON
            val btnSwitch = Button(this)
            btnSwitch.text = "⚪"
            btnSwitch.setTextColor(android.graphics.Color.WHITE)
            btnSwitch.setBackgroundColor(android.graphics.Color.parseColor("#444444"))
            btnSwitch.textSize = 10f
            btnSwitch.setPadding(4, 2, 4, 2)
            var isEffectOn = false
            btnSwitch.setOnClickListener {
                isEffectOn = !isEffectOn
                btnSwitch.text = if (isEffectOn) "🟢" else "⚪"
                btnSwitch.setBackgroundColor(if (isEffectOn) color else android.graphics.Color.parseColor("#444444"))
                onSwitch(isEffectOn)
            }
            row.addView(btnSwitch)

            // ✅ PIHITAN
            val knob = KnobView(this)
            knob.baseColor = color
            knob.value = defaultValue
            val txtVal = TextView(this)
            txtVal.text = "${(defaultValue * 100).toInt()}%"
            txtVal.setTextColor(android.graphics.Color.WHITE)
            txtVal.textSize = 11f
            knob.onValueChange = { v ->
                txtVal.text = "${(v * 100).toInt()}%"
                onValue(v)
            }
            val knobLayout = LinearLayout.LayoutParams(90, 90)
            knobLayout.setMargins(6, 0, 6, 0)
            row.addView(knob, knobLayout)

            // ✅ PAMAGAT + HALAGA
            val txtWrap = LinearLayout(this)
            txtWrap.orientation = LinearLayout.VERTICAL
            txtWrap.gravity = Gravity.CENTER_VERTICAL
            val txtLabel = TextView(this)
            txtLabel.text = label
            txtLabel.setTextColor(color)
            txtLabel.textSize = 12f
            txtWrap.addView(txtLabel)
            txtWrap.addView(txtVal)
            val txtLayout = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
            txtLayout.weight = 1f
            row.addView(txtWrap, txtLayout)

            return row
        }

        // ========== LAHAT MAY ON/OFF + PIHITAN ==========
        root.addView(makeKnobRow("🔊 VOLUME", 0xFFFF8822.toInt(), 0.75f,
            { setVolumeLevel(it) }, { setVolumeEnabled(it) }))

        root.addView(makeKnobRow("🎵 TONE", 0xFF44DD88.toInt(), 0.50f,
            { setToneLevel(it) }, { setToneEnabled(it) }))

        root.addView(makeKnobRow("🌊 REVERB", 0xFFAA66FF.toInt(), 0.25f,
            { setReverbLevel(it) }, { setReverbEnabled(it) }))

        root.addView(makeKnobRow("⚡ GAIN", 0xFFFFFF00.toInt(), 0.50f,
            { setGainLevel(it * 2f) }, { setGainEnabled(it) }))

        root.addView(makeKnobRow("🔥 OVERDRIVE", 0xFFFFAA00.toInt(), 0.00f,
            { setOverdriveLevel(it) }, { setOverdriveEnabled(it) }))

        root.addView(makeKnobRow("💥 DISTORTION", 0xFFFF4444.toInt(), 0.00f,
            { setDistortionLevel(it) }, { setDistortionEnabled(it) }))

        root.addView(makeKnobRow("🫧 PHASER", 0xFF44AAFF.toInt(), 0.00f,
            { setPhaserLevel(it) }, { setPhaserEnabled(it) }))

        // ========== STATUS TEXT ==========
        val statusText = TextView(this)
        statusText.text = "🔴 NAKA-OFF"
        statusText.textSize = 15f
        statusText.setTextColor(0xFFFF6666.toInt())
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 16, 0, 8)
        root.addView(statusText)

        // ========== MAIN ON/OFF BUTTON ==========
        val btn = Button(this)
        btn.text = "🔘 TURN ON"
        btn.textSize = 16f
        btn.setBackgroundColor(0xFF228833.toInt())
        btn.setTextColor(android.graphics.Color.WHITE)
        btn.setPadding(48, 14, 48, 14)
        btn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
                return@setOnClickListener
            }

            isOn = !isOn
            if (isOn) {
                startAudioEngine()
                btn.text = "🟢 TURN OFF"
                btn.setBackgroundColor(0xFFFF4444.toInt())
                statusText.text = "🟢 GUMAGAMIT NG MIKROFONO — Isaksak ang gitara!"
                statusText.setTextColor(0xFF44FF44.toInt())
            } else {
                stopAudioEngine()
                btn.text = "🔘 TURN ON"
                btn.setBackgroundColor(0xFF228833.toInt())
                statusText.text = "🔴 NAKA-OFF"
                statusText.setTextColor(0xFFFF6666.toInt())
            }
        }
        root.addView(btn)

        setContentView(root)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "✅ Pahintulot nakuha! Pindutin muli ang ON!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "⚠️ Kailangan ng pahintulot sa Mikropono!", Toast.LENGTH_LONG).show()
        }
    }
}
