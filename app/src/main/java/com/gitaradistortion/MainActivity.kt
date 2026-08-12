package com.gitaradistortion

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
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
        paint.strokeWidth = 4f + value * 4f
        paint.color = glow
        canvas.drawCircle(cx, cy, r - 4f, paint)

        val capR = r * 0.65f
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFFE8E8E8.toInt()
        canvas.drawCircle(cx, cy, capR, paint)

        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = 0xFFBBBBBB.toInt()
        canvas.drawCircle(cx, cy, capR - 1f, paint)

        val angle = -135f + (270f) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 5f + value * 2f
        paint.color = glow
        val len = capR * 0.75f
        val endX = cx + len * sin(rad).toFloat()
        val endY = cy - len * cos(rad).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)

        paint.style = android.graphics.Paint.Style.FILL
        paint.color = glow
        canvas.drawCircle(cx, cy, 8f + value * 3f, paint)
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
    private lateinit var prefs: SharedPreferences

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

    data class FxDef(
        val id: String,
        val icon: String,
        val name: String,
        val color: Int,
        val defValue: Float,
        val defaultPos: Int,
        val defaultSizePercent: Int,
        val setLevel: (Float) -> Unit,
        val setEnabled: (Boolean) -> Unit
    )

    private val allFx = listOf(
        FxDef("VOL", "🔊", "VOLUME", 0xFFFF8822.toInt(), 0.75f, 1, 100, { setVolumeLevel(it) }, { setVolumeEnabled(it) }),
        FxDef("TON", "🎵", "TONE", 0xFF44DD88.toInt(), 0.50f, 2, 100, { setToneLevel(it) }, { setToneEnabled(it) }),
        FxDef("REV", "🌊", "REVERB", 0xFFAA66FF.toInt(), 0.25f, 3, 90, { setReverbLevel(it) }, { setReverbEnabled(it) }),
        FxDef("NOI", "🚧", "NOISE GATE", 0xFF66DDDD.toInt(), 0.04f, 4, 90, { setNoiseGateLevel(it * 0.15f) }, { setNoiseGateEnabled(it) }),
        FxDef("GAI", "⚡", "GAIN", 0xFFFFFF00.toInt(), 0.50f, 5, 100, { setGainLevel(it * 2f) }, { setGainEnabled(it) }),
        FxDef("OVE", "🔥", "OVERDRIVE", 0xFFFFAA00.toInt(), 0.00f, 6, 100, { setOverdriveLevel(it) }, { setOverdriveEnabled(it) }),
        FxDef("DIS", "💥", "DISTORTION", 0xFFFF4444.toInt(), 0.00f, 7, 110, { setDistortionLevel(it) }, { setDistortionEnabled(it) }),
        FxDef("PHA", "🫧", "PHASER", 0xFF44AAFF.toInt(), 0.00f, 8, 90, { setPhaserLevel(it) }, { setPhaserEnabled(it) })
    )

    private fun getPos(fx: FxDef): Int = prefs.getInt("POS_${fx.id}", fx.defaultPos)
    private fun getSizePercent(fx: FxDef): Int = prefs.getInt("SZ_${fx.id}", fx.defaultSizePercent)
    private fun savePos(fxId: String, pos: Int) = prefs.edit().putInt("POS_$fxId", pos).apply()
    private fun saveSize(fxId: String, percent: Int) = prefs.edit().putInt("SZ_$fxId", percent).apply()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("gitaradistortion")
        prefs = getSharedPreferences("FxSettings", Context.MODE_PRIVATE)
        rebuildUI()
    }

    private fun rebuildUI() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(6, 4, 6, 2)

        val title = TextView(this)
        title.text = "🎸  GUITAR EFFECTS  🎸"
        title.textSize = 18f
        title.setTextColor(0xFFFF9922.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 2, 0, 2)
        root.addView(title)

        val hint = TextView(this)
        hint.text = "Puwesto:1-8 | Laki:80-120 → I-SAVE → I-RESTART"
        hint.textSize = 9f
        hint.setTextColor(0xFFAAAAAA.toInt())
        hint.gravity = Gravity.CENTER
        hint.setPadding(0, 0, 0, 4)
        root.addView(hint)

        val sortedFx = allFx.sortedBy { getPos(it) }

        fun makeEffectCard(fx: FxDef): LinearLayout {
            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.gravity = Gravity.CENTER
            card.setPadding(6, 6, 6, 6)
            card.setBackgroundColor(0xFF1E1E1E.toInt())
            card.minimumWidth = 150

            val szPercent = getSizePercent(fx)
            val knobSize = (75 * szPercent / 100).coerceIn(65, 95)

            val btnSwitch = Button(this)
            btnSwitch.text = "⚪ OFF"
            btnSwitch.setTextColor(Color.WHITE)
            btnSwitch.setBackgroundColor(Color.parseColor("#444444"))
            btnSwitch.textSize = 9f
            btnSwitch.setPadding(4, 1, 4, 1)
            btnSwitch.minWidth = 60
            var isEffectOn = false
            btnSwitch.setOnClickListener {
                isEffectOn = !isEffectOn
                btnSwitch.text = if (isEffectOn) "🟢 ON" else "⚪ OFF"
                btnSwitch.setBackgroundColor(if (isEffectOn) fx.color else Color.parseColor("#444444"))
                fx.setEnabled(isEffectOn)
            }
            card.addView(btnSwitch)

            val knob = KnobView(this)
            knob.baseColor = fx.color
            knob.value = fx.defValue
            val txtVal = TextView(this)
            txtVal.text = "${(fx.defValue * 100).toInt()}%"
            txtVal.setTextColor(fx.color)
            txtVal.textSize = 10f
            txtVal.gravity = Gravity.CENTER
            knob.onValueChange = { v ->
                txtVal.text = "${(v * 100).toInt()}%"
                fx.setLevel(v)
            }
            card.addView(knob, LinearLayout.LayoutParams(knobSize, knobSize))

            val txtLabel = TextView(this)
            txtLabel.text = "${fx.icon} ${fx.name}"
            txtLabel.setTextColor(Color.WHITE)
            txtLabel.textSize = 10f
            txtLabel.gravity = Gravity.CENTER
            txtLabel.setPadding(0, 2, 0, 1)
            card.addView(txtLabel)

            val txtSize = TextView(this)
            txtSize.text = "$szPercent%"
            txtSize.setTextColor(0xFF888888.toInt())
            txtSize.textSize = 9f
            txtSize.gravity = Gravity.CENTER
            txtSize.setPadding(0, 0, 0, 3)
            card.addView(txtSize)

            val rowInput = LinearLayout(this)
            rowInput.orientation = LinearLayout.HORIZONTAL
            rowInput.gravity = Gravity.CENTER
            rowInput.setPadding(2, 2, 2, 2)
            rowInput.setBackgroundColor(0xFF2A2A2A.toInt())

            val etPos = EditText(this)
            etPos.hint = "#"
            etPos.setText("${getPos(fx)}")
            etPos.textSize = 10f
            etPos.setTextColor(Color.WHITE)
            etPos.setHintTextColor(0xFF666666.toInt())
            etPos.setBackgroundColor(0xFF383838.toInt())
            etPos.setPadding(2, 0, 2, 0)
            etPos.minWidth = 28
            etPos.maxWidth = 28
            etPos.gravity = Gravity.CENTER
            rowInput.addView(etPos)

            val space = TextView(this)
            space.minWidth = 3
            rowInput.addView(space)

            val etSz = EditText(this)
            etSz.hint = "%"
            etSz.setText("$szPercent")
            etSz.textSize = 10f
            etSz.setTextColor(Color.WHITE)
            etSz.setHintTextColor(0xFF666666.toInt())
            etSz.setBackgroundColor(0xFF383838.toInt())
            etSz.setPadding(2, 0, 2, 0)
            etSz.minWidth = 28
            etSz.maxWidth = 28
            etSz.gravity = Gravity.CENTER
            rowInput.addView(etSz)

            val space2 = TextView(this)
            space2.minWidth = 3
            rowInput.addView(space2)

            val btnSave = Button(this)
            btnSave.text = "✓"
            btnSave.textSize = 10f
            btnSave.setTextColor(Color.WHITE)
            btnSave.setBackgroundColor(0xFF227733.toInt())
            btnSave.setPadding(4, 0, 4, 0)
            btnSave.minWidth = 26
            btnSave.setOnClickListener {
                val newPos = etPos.text.toString().toIntOrNull()
                val newSz = etSz.text.toString().toIntOrNull()
                if (newPos != null && newPos in 1..8 && newSz != null && newSz in 80..120) {
                    savePos(fx.id, newPos)
                    saveSize(fx.id, newSz)
                    Toast.makeText(this, "✅ NAI-SAVE! I-RESTART ANG APP!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "⚠️ Puwesto:1-8 | Laki:80-120", Toast.LENGTH_SHORT).show()
                }
            }
            rowInput.addView(btnSave)

            card.addView(rowInput)
            return card
        }

        // ✅ 2 HANAY: 4 SA KALIWA, 4 SA KANAN
        val colLeft = LinearLayout(this)
        colLeft.orientation = LinearLayout.VERTICAL
        colLeft.gravity = Gravity.CENTER
        colLeft.setPadding(4, 0, 4, 0)
        sortedFx.take(4).forEach { colLeft.addView(makeEffectCard(it)) }

        val colRight = LinearLayout(this)
        colRight.orientation = LinearLayout.VERTICAL
        colRight.gravity = Gravity.CENTER
        colRight.setPadding(4, 0, 4, 0)
        sortedFx.drop(4).forEach { colRight.addView(makeEffectCard(it)) }

        val twoCols = LinearLayout(this)
        twoCols.orientation = LinearLayout.HORIZONTAL
        twoCols.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        twoCols.addView(colLeft)
        twoCols.addView(colRight)
        root.addView(twoCols)

        val statusText = TextView(this)
        statusText.text = "🔴 NAKA-OFF — Isaksak ang iRig bago mag-ON"
        statusText.textSize = 10f
        statusText.setTextColor(0xFFFF6666.toInt())
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 4, 0, 4)
        root.addView(statusText)

        val btn = Button(this)
        btn.text = "🔘  POWER"
        btn.textSize = 15f
        btn.setBackgroundColor(0xFF228833.toInt())
        btn.setTextColor(Color.WHITE)
        btn.setPadding(40, 6, 40, 6)
        btn.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
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
