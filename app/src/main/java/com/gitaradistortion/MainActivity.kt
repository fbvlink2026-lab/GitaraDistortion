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

        // ✅ GILID NG PIHITAN — PARANG METAL
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = 0xFF555555.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        // ✅ LOOB NG PIHITAN — MADILIM
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFF1A1A1A.toInt()
        canvas.drawCircle(cx, cy, r - 2f, paint)

        // ✅ ILALIM NG PIHITAN — MAY KULAY AYON SA ANTAS
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 4.5f + value * 5f
        paint.color = glow
        canvas.drawCircle(cx, cy, r - 5f, paint)

        // ✅ TAKIP NG PIHITAN — PARANG GOMA
        val capR = r * 0.68f
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFFE0E0E0.toInt()
        canvas.drawCircle(cx, cy, capR, paint)

        // ✅ GUHIT SA GILID NG TAKIP
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0xFFBBBBBB.toInt()
        canvas.drawCircle(cx, cy, capR - 1f, paint)

        // ✅ GUHIT NA TUMUTURO — KUNG SAAN NAKATURO ANG PIHITAN
        val angle = -135f + (270f) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 5f + value * 3f
        paint.color = glow
        val len = capR * 0.75f
        val endX = cx + len * sin(rad).toFloat()
        val endY = cy - len * cos(rad).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)

        // ✅ GITNA NG PIHITAN
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

    // ✅ LAHAT NG 8 NA EPEKTO — MAY ANTAS + ON/OFF
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

        // ✅ PANGUNAHING LIKOD — PARANG KAHON NG AMP
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF181818.toInt())
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(12, 16, 12, 12)

        // ✅ PAMAGAT — PARANG TUNAY NA PEDAL
        val title = TextView(this)
        title.text = "🎸  GUITAR EFFECTS  🎸"
        title.textSize = 22f
        title.setTextColor(0xFFFF9922.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 6, 0, 16)
        root.addView(title)

        // ✅ GUHIT SA ILALIM NG PAMAGAT
        val line = View(this)
        line.setBackgroundColor(0xFF333333.toInt())
        val lineParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2
        )
        lineParams.setMargins(8, 0, 8, 12)
        root.addView(line, lineParams)

        // ✅ GUMAWA NG HANAY NG PIHITAN — MAY ON/OFF + PIHITAN + PAMAGAT + HALAGA
        fun makeKnobRow(label: String, color: Int, defaultValue: Float,
                         onValue: (Float) -> Unit,
                         onSwitch: (Boolean) -> Unit): LinearLayout {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(4, 2, 4, 2)
            row.setBackgroundColor(0xFF202020.toInt())
            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowParams.setMargins(2, 2, 2, 2)
            row.setPadding(8, 6, 8, 6)

            // ✅ ON/OFF BUTTON — BILOG NA BILOG
            val btnSwitch = Button(this)
            btnSwitch.text = "⚪"
            btnSwitch.setTextColor(Color.WHITE)
            btnSwitch.setBackgroundColor(Color.parseColor("#444444"))
            btnSwitch.textSize = 14f
            btnSwitch.setPadding(2, 0, 2, 0)
            btnSwitch.width = 42
            btnSwitch.height = 42
            var isEffectOn = false
            btnSwitch.setOnClickListener {
                isEffectOn = !isEffectOn
                btnSwitch.text = if (isEffectOn) "🟢" else "⚪"
                btnSwitch.setBackgroundColor(if (isEffectOn) color else Color.parseColor("#444444"))
                onSwitch(isEffectOn)
            }
            row.addView(btnSwitch)

            // ✅ PIHITAN
            val knob = KnobView(this)
            knob.baseColor = color
            knob.value = defaultValue
            val txtVal = TextView(this)
            txtVal.text = "${(defaultValue * 100).toInt()}%"
            txtVal.setTextColor(Color.WHITE)
            txtVal.textSize = 10f
            txtVal.gravity = Gravity.CENTER
            knob.onValueChange = { v ->
                txtVal.text = "${(v * 100).toInt()}%"
                onValue(v)
            }
            val knobParams = LinearLayout.LayoutParams(80, 80)
            knobParams.setMargins(8, 0, 8, 0)
            row.addView(knob, knobParams)

            // ✅ PAMAGAT + HALAGA
            val txtWrap = LinearLayout(this)
            txtWrap.orientation = LinearLayout.VERTICAL
            txtWrap.gravity = Gravity.CENTER_VERTICAL
            val txtLabel = TextView(this)
            txtLabel.text = label
            txtLabel.setTextColor(color)
            txtLabel.textSize = 13f
            txtLabel.setPadding(0, 0, 0, 2)
            txtWrap.addView(txtLabel)
            txtWrap.addView(txtVal)
            val txtParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
            txtParams.weight = 1f
            row.addView(txtWrap, txtParams)

            return row
        }

        // ========== HANAY 1: VOLUME — TONE — REVERB ==========
        val row1 = LinearLayout(this)
        row1.orientation = LinearLayout.HORIZONTAL
        row1.gravity = Gravity.CENTER
        row1.setPadding(0, 0, 0, 4)
        row1.addView(makeKnobRow("🔊 VOLUME", 0xFFFF8822.toInt(), 0.75f,
            { setVolumeLevel(it) }, { setVolumeEnabled(it) }))
        root.addView(row1)

        root.addView(makeKnobRow("🎵 TONE", 0xFF44DD88.toInt(), 0.50f,
            { setToneLevel(it) }, { setToneEnabled(it) }))

        root.addView(makeKnobRow("🌊 REVERB", 0xFFAA66FF.toInt(), 0.25f,
            { setReverbLevel(it) }, { setReverbEnabled(it) }))

        root.addView(makeKnobRow("🚧 NOISE GATE", 0xFF66DDDD.toInt(), 0.04f,
            { setNoiseGateLevel(it * 0.15f) }, { setNoiseGateEnabled(it) }))

        // ========== HANAY 2: GAIN — OVERDRIVE — DIST — PHASER ==========
        val row2Title = TextView(this)
        row2Title.text = "⚡ EFFECTS ⚡"
        row2Title.textSize = 14f
        row2Title.setTextColor(0xFF777777.toInt())
        row2Title.gravity = Gravity.CENTER
        row2Title.setPadding(0, 10, 0, 4)
        root.addView(row2Title)

        root.addView(makeKnobRow("⚡ GAIN", 0xFFFFFF00.toInt(), 0.50f,
            { setGainLevel(it * 2f) }, { setGainEnabled(it) }))

        root.addView(makeKnobRow("🔥 OVERDRIVE", 0xFFFFAA00.toInt(), 0.00f,
            { setOverdriveLevel(it) }, { setOverdriveEnabled(it) }))

        root.addView(makeKnobRow("💥 DISTORTION", 0xFFFF4444.toInt(), 0.00f,
            { setDistortionLevel(it) }, { setDistortionEnabled(it) }))

        root.addView(makeKnobRow("🫧 PHASER", 0xFF44AAFF.toInt(), 0.00f,
            { setPhaserLevel(it) }, { setPhaserEnabled(it) }))

        // ========== STATUS ==========
        val statusText = TextView(this)
        statusText.text = "🔴 READY — Pindutin ON para magsimula"
        statusText.textSize = 13f
        statusText.setTextColor(0xFFFF6666.toInt())
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 12, 0, 6)
        root.addView(statusText)

        // ========== PANGUNAHING BUTTON ==========
        val btn = Button(this)
        btn.text = "🔘 POWER"
        btn.textSize = 17f
        btn.setBackgroundColor(0xFF227733.toInt())
        btn.setTextColor(Color.WHITE)
        btn.setPadding(40, 14, 40, 14)
        btn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
                return@setOnClickListener
            }

            isOn = !isOn
            if (isOn) {
                startAudioEngine()
                btn.text = "🔴 POWER OFF"
                btn.setBackgroundColor(0xFFCC3333.toInt())
                statusText.text = "🟢 GUMAGAMIT NG MIKROFONO — Isaksak ang gitara!"
                statusText.setTextColor(0xFF44EE44.toInt())
            } else {
                stopAudioEngine()
                btn.text = "🔘 POWER"
                btn.setBackgroundColor(0xFF227733.toInt())
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
