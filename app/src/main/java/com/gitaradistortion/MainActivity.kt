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

// 🎛️ BILOG NA PIHITAN — PARANG TUNAY NA AMP
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
        val r = minOf(w, h) / 2 - 4f
        val glow = getGlowColor()

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = 0xFF555555.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFF1A1A1A.toInt()
        canvas.drawCircle(cx, cy, r - 2f, paint)

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 4.5f + value * 5f
        paint.color = glow
        canvas.drawCircle(cx, cy, r - 5f, paint)

        val capR = r * 0.68f
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFFE0E0E0.toInt()
        canvas.drawCircle(cx, cy, capR, paint)

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0xFFBBBBBB.toInt()
        canvas.drawCircle(cx, cy, capR - 1f, paint)

        val angle = -135f + (270f) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 5f + value * 3f
        paint.color = glow
        val len = capR * 0.75f
        val endX = cx + len * sin(rad).toFloat()
        val endY = cy - len * cos(rad).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = glow
        canvas.drawCircle(cx, cy, 7f + value * 3f, paint)
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

    // ✅ LAHAT NG 8 NA EPEKTO
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

        // ✅ PANGUNAHING LIKOD — PARANG TONEBRIDGE
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(16, 20, 16, 16)

        // ✅ PAMAGAT SA TAAS
        val title = TextView(this)
        title.text = "🎸  GUITAR EFFECTS  🎸"
        title.textSize = 24f
        title.setTextColor(0xFFFF9922.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 8, 0, 16)
        root.addView(title)

        // ✅ GUMAWA NG BAWAT EPEKTO: ON/OFF → PIHITAN → PAMAGAT → HALAGA
        fun makeEffectView(label: String, color: Int, defaultValue: Float,
                            onValue: (Float) -> Unit,
                            onSwitch: (Boolean) -> Unit): LinearLayout {
            val col = LinearLayout(this)
            col.orientation = LinearLayout.VERTICAL
            col.gravity = Gravity.CENTER

            // ✅ ON/OFF BUTTON — NASA TAAS NG PIHITAN! KATULAD NG TONEBRIDGE!
            val btnSwitch = Button(this)
            btnSwitch.text = "⚪ OFF"
            btnSwitch.setTextColor(Color.WHITE)
            btnSwitch.setBackgroundColor(Color.parseColor("#333333"))
            btnSwitch.textSize = 10f
            btnSwitch.setPadding(8, 2, 8, 2)
            btnSwitch.minWidth = 70
            var isEffectOn = false
            btnSwitch.setOnClickListener {
                isEffectOn = !isEffectOn
                btnSwitch.text = if (isEffectOn) "🟢 ON" else "⚪ OFF"
                btnSwitch.setBackgroundColor(if (isEffectOn) color else Color.parseColor("#333333"))
                onSwitch(isEffectOn)
            }
            col.addView(btnSwitch)

            // ✅ PIHITAN — NASA ILALIM NG BUTTON
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
            col.addView(knob, LinearLayout.LayoutParams(90, 90))

            // ✅ PAMAGAT
            val txtLabel = TextView(this)
            txtLabel.text = label
            txtLabel.setTextColor(Color.WHITE)
            txtLabel.textSize = 12f
            txtLabel.gravity = Gravity.CENTER
            txtLabel.setPadding(0, 4, 0, 2)
            col.addView(txtLabel)

            // ✅ HALAGA
            col.addView(txtVal)

            return col
        }

        // ==========================================
        // ✅ DALAWANG HANAY LANG — KALIWA AT KANAN!
        // ==========================================

        // ✅ HANAY 1 — KALIWA: VOLUME, TONE, REVERB, NOISE GATE
        val row1 = LinearLayout(this)
        row1.orientation = LinearLayout.HORIZONTAL
        row1.gravity = Gravity.CENTER
        row1.setPadding(0, 0, 0, 16)
        row1.addView(makeEffectView("🔊 VOLUME", 0xFFFF8822.toInt(), 0.75f,
            { setVolumeLevel(it) }, { setVolumeEnabled(it) }))
        row1.addView(makeEffectView("🎵 TONE", 0xFF44DD88.toInt(), 0.50f,
            { setToneLevel(it) }, { setToneEnabled(it) }))
        row1.addView(makeEffectView("🌊 REVERB", 0xFFAA66FF.toInt(), 0.25f,
            { setReverbLevel(it) }, { setReverbEnabled(it) }))
        row1.addView(makeEffectView("🚧 GATE", 0xFF66DDDD.toInt(), 0.04f,
            { setNoiseGateLevel(it * 0.15f) }, { setNoiseGateEnabled(it) }))
        root.addView(row1)

        // ✅ HANAY 2 — KANAN: GAIN, OVERDRIVE, DIST, PHASER
        val row2 = LinearLayout(this)
        row2.orientation = LinearLayout.HORIZONTAL
        row2.gravity = Gravity.CENTER
        row2.setPadding(0, 8, 0, 8)
        row2.addView(makeEffectView("⚡ GAIN", 0xFFFFFF00.toInt(), 0.50f,
            { setGainLevel(it * 2f) }, { setGainEnabled(it) }))
        row2.addView(makeEffectView("🔥 OVERDRIVE", 0xFFFFAA00.toInt(), 0.00f,
            { setOverdriveLevel(it) }, { setOverdriveEnabled(it) }))
        row2.addView(makeEffectView("💥 DIST", 0xFFFF4444.toInt(), 0.00f,
            { setDistortionLevel(it) }, { setDistortionEnabled(it) }))
        row2.addView(makeEffectView("🫧 PHASER", 0xFF44AAFF.toInt(), 0.00f,
            { setPhaserLevel(it) }, { setPhaserEnabled(it) }))
        root.addView(row2)

        // ✅ STATUS TEXT
        val statusText = TextView(this)
        statusText.text = "🔴 NAKA-OFF — Pindutin POWER sa ibaba"
        statusText.textSize = 13f
        statusText.setTextColor(0xFFFF6666.toInt())
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 12, 0, 8)
        root.addView(statusText)

        // ✅ PANGUNAHING POWER BUTTON
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
                statusText.text = "🟢 GUMAGAMIT NG MIKROFONO — Isaksak ang gitara!"
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
            Toast.makeText(this, "✅ Pahintulot nakuha! Pindutin muli ang POWER!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "⚠️ Kailangan ng pahintulot sa Mikropono!", Toast.LENGTH_LONG).show()
        }
    }
}
