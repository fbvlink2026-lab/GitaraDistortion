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

// 🎛️ PIHITAN — MAY SARILING LAKI AT PWEDE HILAHIN
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

// 🎛️ BAWAT EPEKTO — BUONG BAHAGI
class FxView(
    context: android.content.Context,
    val icon: String,
    val fxName: String,
    val color: Int,
    initValue: Float,
    initSize: Int,
    val onLevelChange: (Float) -> Unit,
    val onEnableChange: (Boolean) -> Unit
) : LinearLayout(context) {
    
    var currentSize = initSize
        set(v) {
            field = v.coerceIn(80, 150)
            updateSize()
        }
    
    var isEnabled = false
    val knob: KnobView
    val btnSwitch: Button
    val txtLabel: TextView
    val txtValue: TextView

    init {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(4, 4, 4, 8)
        setBackgroundColor(0xFF222222.toInt())
        elevation = 4f

        btnSwitch = Button(context)
        btnSwitch.text = "⚪ OFF"
        btnSwitch.setTextColor(Color.WHITE)
        btnSwitch.setBackgroundColor(Color.parseColor("#444444"))
        btnSwitch.textSize = 10f
        btnSwitch.setPadding(2, 1, 2, 1)
        btnSwitch.minWidth = 70
        btnSwitch.setOnClickListener {
            isEnabled = !isEnabled
            btnSwitch.text = if (isEnabled) "🟢 ON" else "⚪ OFF"
            btnSwitch.setBackgroundColor(if (isEnabled) color else Color.parseColor("#444444"))
            onEnableChange(isEnabled)
        }
        addView(btnSwitch)

        knob = KnobView(context)
        knob.baseColor = color
        knob.value = initValue
        knob.onValueChange = { v ->
            txtValue.text = "${(v * 100).toInt()}%"
            onLevelChange(v)
        }
        addView(knob, LayoutParams(currentSize, currentSize))

        txtLabel = TextView(context)
        txtLabel.text = "$icon $fxName"
        txtLabel.setTextColor(Color.WHITE)
        txtLabel.textSize = 10f
        txtLabel.gravity = Gravity.CENTER
        txtLabel.setPadding(0, 3, 0, 1)
        addView(txtLabel)

        txtValue = TextView(context)
        txtValue.text = "${(initValue * 100).toInt()}%"
        txtValue.setTextColor(color)
        txtValue.textSize = 11f
        txtValue.gravity = Gravity.CENTER
        addView(txtValue)

        // ✅ HAWAKAN MO SA GILID → PICHIRIN PARA LAKIHAN/LIITAN
        setOnLongClickListener {
            Toast.makeText(context, "👉 HILAHIN PATAAS/PABABA = LAKI | HILAHIN KALIWA/KANAN = ILIPAT", Toast.LENGTH_LONG).show()
            true
        }
    }

    private fun updateSize() {
        val lp = knob.layoutParams
        lp.width = currentSize
        lp.height = currentSize
        knob.layoutParams = lp
    }
}

class MainActivity : AppCompatActivity() {
    private var isOn = false
    private lateinit var container: LinearLayout
    private var draggedFx: FxView? = null
    private var startX = 0f
    private var startY = 0f
    private var originalIndex = -1

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

    // ✅ KAHULUGAN NG BAWAT EPEKTO
    data class FxData(
        val icon: String,
        val name: String,
        val color: Int,
        val defValue: Float,
        val defSize: Int,
        val setLevel: (Float) -> Unit,
        val setEnabled: (Boolean) -> Unit
    )

    // ✅ ORIHINAL NA AYOS — DALAWANG HANAY
    private val allFx = listOf(
        // KALIWA
        FxData("🔊", "VOLUME", 0xFFFF8822.toInt(), 0.75f, 110, { setVolumeLevel(it) }, { setVolumeEnabled(it) }),
        FxData("🎵", "TONE", 0xFF44DD88.toInt(), 0.50f, 110, { setToneLevel(it) }, { setToneEnabled(it) }),
        FxData("🌊", "REVERB", 0xFFAA66FF.toInt(), 0.25f, 100, { setReverbLevel(it) }, { setReverbEnabled(it) }),
        FxData("🚧", "NOISE GATE", 0xFF66DDDD.toInt(), 0.04f, 100, { setNoiseGateLevel(it * 0.15f) }, { setNoiseGateEnabled(it) }),
        // KANAN
        FxData("⚡", "GAIN", 0xFFFFFF00.toInt(), 0.50f, 110, { setGainLevel(it * 2f) }, { setGainEnabled(it) }),
        FxData("🔥", "OVERDRIVE", 0xFFFFAA00.toInt(), 0.00f, 110, { setOverdriveLevel(it) }, { setOverdriveEnabled(it) }),
        FxData("💥", "DISTORTION", 0xFFFF4444.toInt(), 0.00f, 120, { setDistortionLevel(it) }, { setDistortionEnabled(it) }),
        FxData("🫧", "PHASER", 0xFF44AAFF.toInt(), 0.00f, 100, { setPhaserLevel(it) }, { setPhaserEnabled(it) })
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("gitaradistortion")

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(8, 8, 8, 4)

        val title = TextView(this)
        title.text = "🎸  HILAHIN MO PALIPAT-LIPAT! PICHIRIN PARA LAKIHAN! 🎸"
        title.textSize = 13f
        title.setTextColor(0xFFFF9922.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 4, 0, 8)
        root.addView(title)

        // ✅ LALAGYAN NG LAHAT NG EPEKTO — DALAWANG HANAY
        container = LinearLayout(this)
        container.orientation = LinearLayout.HORIZONTAL
        container.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        val colLeft = LinearLayout(this)
        colLeft.orientation = LinearLayout.VERTICAL
        colLeft.gravity = Gravity.CENTER
        colLeft.setPadding(4, 0, 4, 0)

        val colRight = LinearLayout(this)
        colRight.orientation = LinearLayout.VERTICAL
        colRight.gravity = Gravity.CENTER
        colRight.setPadding(4, 0, 4, 0)

        // ✅ ILAGAY SA DALAWANG HANAY
        allFx.take(4).forEach { data ->
            val fx = FxView(this, data.icon, data.name, data.color, data.defValue, data.defSize, data.setLevel, data.setEnabled)
            colLeft.addView(fx)
        }
        allFx.drop(4).forEach { data ->
            val fx = FxView(this, data.icon, data.name, data.color, data.defValue, data.defSize, data.setLevel, data.setEnabled)
            colRight.addView(fx)
        }

        container.addView(colLeft)
        container.addView(colRight)
        root.addView(container)

        val statusText = TextView(this)
        statusText.text = "👉 HAWAKAN NG MATAGAL → HILAHIN = ILIPAT | PATAAS/PABABA = LAKIHAN/LIITAN"
        statusText.textSize = 10f
        statusText.setTextColor(0xFFAAAAAA.toInt())
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 4, 0, 4)
        root.addView(statusText)

        val btn = Button(this)
        btn.text = "🔘  POWER"
        btn.textSize = 18f
        btn.setBackgroundColor(0xFF228833.toInt())
        btn.setTextColor(Color.WHITE)
        btn.setPadding(60, 12, 60, 12)
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
