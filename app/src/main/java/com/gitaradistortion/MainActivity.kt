package com.gitaradistortion

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
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

// 🎛️ MALAKING PIHITAN — MAY PUWANG SA PANGALAN
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

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = 0xFF555555.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFF1A1A1A.toInt()
        canvas.drawCircle(cx, cy, r - 3f, paint)

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 5f + value * 5f
        paint.color = glow
        canvas.drawCircle(cx, cy, r - 6f, paint)

        val capR = r * 0.65f
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFFE8E8E8.toInt()
        canvas.drawCircle(cx, cy, capR, paint)

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = 0xFFBBBBBB.toInt()
        canvas.drawCircle(cx, cy, capR - 1.5f, paint)

        val angle = -135f + (270f) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 6f + value * 3f
        paint.color = glow
        val len = capR * 0.75f
        val endX = cx + len * sin(rad).toFloat()
        val endY = cy - len * cos(rad).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = glow
        canvas.drawCircle(cx, cy, 10f + value * 4f, paint)
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

    // ✅ LAHAT NG EPEKTO
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
        System.loadLibrary("gitaradistortion")

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(8, 12, 8, 8)

        val title = TextView(this)
        title.text = "🎸  GUITAR EFFECTS  🎸"
        title.textSize = 22f
        title.setTextColor(0xFFFF9922.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 4, 0, 12)
        root.addView(title)

        // ✅ BAWAT EPEKTO: BUTTON SA TAAS → PIHITAN → BUONG PANGALAN → HALAGA
        fun makeEffectView(fullName: String, color: Int, defaultValue: Float,
                            onValue: (Float) -> Unit,
                            onSwitch: (Boolean) -> Unit): LinearLayout {
            val col = LinearLayout(this)
            col.orientation = LinearLayout.VERTICAL
            col.gravity = Gravity.CENTER
            col.setPadding(2, 0, 2, 6)

            // ✅ ON/OFF BUTTON SA TAAS
            val btnSwitch = Button(this)
            btnSwitch.text = "⚪ OFF"
            btnSwitch.setTextColor(Color.WHITE)
            btnSwitch.setBackgroundColor(Color.parseColor("#333333"))
            btnSwitch.textSize = 10f
            btnSwitch.setPadding(2, 1, 2, 1)
            btnSwitch.minWidth = 60
            var isEffectOn = false
            btnSwitch.setOnClickListener {
                isEffectOn = !isEffectOn
                btnSwitch.text = if (isEffectOn) "🟢 ON" else "⚪ OFF"
                btnSwitch.setBackgroundColor(if (isEffectOn) color else Color.parseColor("#333333"))
                onSwitch(isEffectOn)
            }
            col.addView(btnSwitch)

            // ✅ PIHITAN
            val knob = KnobView(this)
            knob.baseColor = color
            knob.value = defaultValue
            val txtVal = TextView(this)
            txtVal.text = "${(defaultValue * 100).toInt()}%"
            txtVal.setTextColor(color)
            txtVal.textSize = 11f
            txtVal.gravity = Gravity.CENTER
            knob.onValueChange = { v ->
                txtVal.text = "${(v * 100).toInt()}%"
                onValue(v)
            }
            col.addView(knob, LinearLayout.LayoutParams(100, 100))

            // ✅ BUONG PANGALAN — HINDI LANG ICON
            val txtLabel = TextView(this)
            txtLabel.text = fullName
            txtLabel.setTextColor(Color.WHITE)
            txtLabel.textSize = 11f
            txtLabel.gravity = Gravity.CENTER
            txtLabel.setPadding(0, 2, 0, 1)
            col.addView(txtLabel)
            col.addView(txtVal)

            return col
        }

        // ==========================================
        // ✅ HANAY 1 — ITAAS: VOLUME, TONE, REVERB, NOISE GATE
        // ==========================================
        val row1 = LinearLayout(this)
        row1.orientation = LinearLayout.HORIZONTAL
        row1.gravity = Gravity.CENTER
        row1.setPadding(0, 0, 0, 4)
        row1.addView(makeEffectView("VOLUME", 0xFFFF8822.toInt(), 0.75f,
            { setVolumeLevel(it) }, { setVolumeEnabled(it) }))
        row1.addView(makeEffectView("TONE", 0xFF44DD88.toInt(), 0.50f,
            { setToneLevel(it) }, { setToneEnabled(it) }))
        row1.addView(makeEffectView("REVERB", 0xFFAA66FF.toInt(), 0.25f,
            { setReverbLevel(it) }, { setReverbEnabled(it) }))
        row1.addView(makeEffectView("NOISE GATE", 0xFF66DDDD.toInt(), 0.04f,
            { setNoiseGateLevel(it * 0.15f) }, { setNoiseGateEnabled(it) }))
        root.addView(row1)

        // ==========================================
        // ✅ HANAY 2 — IBABA: GAIN, OVERDRIVE, DISTORTION, PHASER
        // ==========================================
        val row2 = LinearLayout(this)
        row2.orientation = LinearLayout.HORIZONTAL
        row2.gravity = Gravity.CENTER
        row2.setPadding(0, 4, 0, 4)
        row2.addView(makeEffectView("GAIN", 0xFFFFFF00.toInt(), 0.50f,
            { setGainLevel(it * 2f) }, { setGainEnabled(it) }))
        row2.addView(makeEffectView("OVERDRIVE", 0xFFFFAA00.toInt(), 0.00f,
            { setOverdriveLevel(it) }, { setOverdriveEnabled(it) }))
        row2.addView(makeEffectView("DISTORTION", 0xFFFF4444.toInt(), 0.00f,
            { setDistortionLevel(it) }, { setDistortionEnabled(it) }))
        row2.addView(makeEffectView("PHASER", 0xFF44AAFF.toInt(), 0.00f,
            { setPhaserLevel(it) }, { setPhaserEnabled(it) }))
        root.addView(row2)

        val statusText = TextView(this)
        statusText.text = "🔴 NAKA-OFF — Isaksak ang iRig bago mag-ON"
        statusText.textSize = 13f
        statusText.setTextColor(0xFFFF6666.toInt())
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 6, 0, 6)
        root.addView(statusText)

        val btn = Button(this)
        btn.text = "🔘  POWER"
        btn.textSize = 18f
        btn.setBackgroundColor(0xFF228833.toInt())
        btn.setTextColor(Color.WHITE)
        btn.setPadding(60, 14, 60, 14)
        btn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
                return@setOnClickListener
            }

            isOn = !isOn
            if (isOn) {
                startAudioEngine()
                btn.text = "🔴  POWER OFF"
                btn.setBackgroundColor(0xFFFF4444.toInt())
                statusText.text = "🟢 GUMAGANA — Isaksak ang gitara sa iRig!"
                statusText.setTextColor(0xFF44FF44.toInt())
            } else {
                stopAudioEngine()
                btn.text = "🔘  POWER"
                btn.setBackgroundColor(0xFF228833.toInt())
                statusText.text = "🔴 NAKA-OFF — Handa na ang iyong mga ayos"
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
            Toast.makeText(this, "✅ Pahintulot nakuha! Isaksak ang iRig → Pindutin muli ang POWER!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "⚠️ Kailangan ng pahintulot sa Mikropono!", Toast.LENGTH_LONG).show()
        }
    }
}
