package com.gitaradistortion

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.*

// 🎛️ PIHITAN NA KATULAD NG SA GITARA — MAY TUNAY NA KAP!
class KnobView(context: android.content.Context) : View(context) {
    var value = 0.5f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }
    
    var onValueChange: ((Float) -> Unit)? = null
    var baseColor = 0xFFFF8822.toInt()

    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    // ✅ KULAY NA UMIILAW HABANG PINIPIHIT
    private fun getGlowColor(): Int {
        val f = 0.35f + value * 0.65f
        val r = ((baseColor shr 16 and 0xFF) * f).toInt().coerceAtMost(255)
        val g = ((baseColor shr 8 and 0xFF) * f).toInt().coerceAtMost(255)
        val b = ((baseColor and 0xFF) * f).toInt().coerceAtMost(255)
        return Color.rgb(r, g, b)
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2
        val cy = h / 2
        val r = minOf(w, h) / 2 - 6f
        val glow = getGlowColor()

        // ✅ 1. ILALIM — MAITIM NA SANGGA NG PIHITAN
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFF202020.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        // ✅ 2. GILID — UMIILAW NA GUHIT
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 3.5f + value * 4f
        paint.color = glow
        canvas.drawCircle(cx, cy, r - 3f, paint)

        // ✅ 3. KAP — PARANG TUNAY NA PLASTIK O METAL NA KAP NG GITARA
        val capR = r * 0.72f
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFFF0F0F0.toInt()  // ✅ PUTING KAP KATULAD NG SA GITARA
        canvas.drawCircle(cx, cy, capR, paint)

        // ✅ 4. GILID NG KAP — ANINO PARA MAY LALIM
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = 0xFFCCCCCC.toInt()
        canvas.drawCircle(cx, cy, capR - 1.5f, paint)

        // ✅ 5. TANDA — MAKAPAL NA GUHIT SA KAP
        val angle = -135f + (270f) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 5f + value * 3f
        paint.color = glow
        val len = capR * 0.8f
        val endX = cx + len * sin(rad).toFloat()
        val endY = cy - len * cos(rad).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)

        // ✅ 6. GITNA NG KAP — MUNTING BILOG
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
    private lateinit var statusText: TextView
    private lateinit var powerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🎸 PANGUNAHING SCREEN
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF121212.toInt())
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(24, 40, 24, 24)

        // 📌 PAMAGAT
        val title = TextView(this)
        title.text = "🎸 GITARA DISTORTION"
        title.textSize = 28f
        title.setTextColor(0xFFFF8822.toInt())
        title.gravity = Gravity.CENTER
        title.setPadding(0, 10, 0, 25)
        root.addView(title)

        // ========== HANAY 1: VOLUME — TONE — DISTORTION ==========
        val row1 = LinearLayout(this)
        row1.orientation = LinearLayout.HORIZONTAL
        row1.gravity = Gravity.CENTER

        // 🔊 VOLUME
        val volCol = LinearLayout(this)
        volCol.orientation = LinearLayout.VERTICAL
        volCol.gravity = Gravity.CENTER
        val volText = TextView(this)
        volText.text = "🔊 VOLUME\n50%"
        volText.textSize = 13f
        volText.setTextColor(0xFFFF8822.toInt())
        volText.gravity = Gravity.CENTER
        val volKnob = KnobView(this)
        volKnob.layoutParams = LinearLayout.LayoutParams(170, 170)
        volKnob.baseColor = 0xFFFF8822.toInt()
        volKnob.value = 0.5f
        volKnob.onValueChange = { v ->
            volText.text = "🔊 VOLUME\n${(v * 100).toInt()}%"
        }
        volCol.addView(volKnob)
        volCol.addView(volText)
        row1.addView(volCol)

        // 🎵 TONE
        val toneCol = LinearLayout(this)
        toneCol.orientation = LinearLayout.VERTICAL
        toneCol.gravity = Gravity.CENTER
        val toneText = TextView(this)
        toneText.text = "🎵 TONE\n50%"
        toneText.textSize = 13f
        toneText.setTextColor(0xFF44DD88.toInt())
        toneText.gravity = Gravity.CENTER
        val toneKnob = KnobView(this)
        toneKnob.layoutParams = LinearLayout.LayoutParams(170, 170)
        toneKnob.baseColor = 0xFF44DD88.toInt()
        toneKnob.value = 0.5f
        toneKnob.onValueChange = { v ->
            toneText.text = "🎵 TONE\n${(v * 100).toInt()}%"
        }
        toneCol.addView(toneKnob)
        toneCol.addView(toneText)
        row1.addView(toneCol)

        // 💥 DISTORTION
        val distCol = LinearLayout(this)
        distCol.orientation = LinearLayout.VERTICAL
        distCol.gravity = Gravity.CENTER
        val distText = TextView(this)
        distText.text = "💥 DIST\n50%"
        distText.textSize = 13f
        distText.setTextColor(0xFFFF4444.toInt())
        distText.gravity = Gravity.CENTER
        val distKnob = KnobView(this)
        distKnob.layoutParams = LinearLayout.LayoutParams(170, 170)
        distKnob.baseColor = 0xFFFF4444.toInt()
        distKnob.value = 0.5f
        distKnob.onValueChange = { v ->
            distText.text = "💥 DIST\n${(v * 100).toInt()}%"
        }
        distCol.addView(distKnob)
        distCol.addView(distText)
        row1.addView(distCol)

        root.addView(row1)

        // ========== HANAY 2: GAIN — CHORUS — REVERB ==========
        val row2 = LinearLayout(this)
        row2.orientation = LinearLayout.HORIZONTAL
        row2.gravity = Gravity.CENTER
        row2.setPadding(0, 15, 0, 0)

        // ⚡ GAIN
        val gainCol = LinearLayout(this)
        gainCol.orientation = LinearLayout.VERTICAL
        gainCol.gravity = Gravity.CENTER
        val gainText = TextView(this)
        gainText.text = "⚡ GAIN\n50%"
        gainText.textSize = 13f
        gainText.setTextColor(0xFFFFCC00.toInt())
        gainText.gravity = Gravity.CENTER
        val gainKnob = KnobView(this)
        gainKnob.layoutParams = LinearLayout.LayoutParams(170, 170)
        gainKnob.baseColor = 0xFFFFCC00.toInt()
        gainKnob.value = 0.5f
        gainKnob.onValueChange = { v ->
            gainText.text = "⚡ GAIN\n${(v * 100).toInt()}%"
        }
        gainCol.addView(gainKnob)
        gainCol.addView(gainText)
        row2.addView(gainCol)

        // 🎶 CHORUS
        val chorusCol = LinearLayout(this)
        chorusCol.orientation = LinearLayout.VERTICAL
        chorusCol.gravity = Gravity.CENTER
        val chorusText = TextView(this)
        chorusText.text = "🎶 CHORUS\n50%"
        chorusText.textSize = 13f
        chorusText.setTextColor(0xFF44AAFF.toInt())
        chorusText.gravity = Gravity.CENTER
        val chorusKnob = KnobView(this)
        chorusKnob.layoutParams = LinearLayout.LayoutParams(170, 170)
        chorusKnob.baseColor = 0xFF44AAFF.toInt()
        chorusKnob.value = 0.5f
        chorusKnob.onValueChange = { v ->
            chorusText.text = "🎶 CHORUS\n${(v * 100).toInt()}%"
        }
        chorusCol.addView(chorusKnob)
        chorusCol.addView(chorusText)
        row2.addView(chorusCol)

        // 🌊 REVERB
        val reverbCol = LinearLayout(this)
        reverbCol.orientation = LinearLayout.VERTICAL
        reverbCol.gravity = Gravity.CENTER
        val reverbText = TextView(this)
        reverbText.text = "🌊 REVERB\n50%"
        reverbText.textSize = 13f
        reverbText.setTextColor(0xFFAA66FF.toInt())
        reverbText.gravity = Gravity.CENTER
        val reverbKnob = KnobView(this)
        reverbKnob.layoutParams = LinearLayout.LayoutParams(170, 170)
        reverbKnob.baseColor = 0xFFAA66FF.toInt()
        reverbKnob.value = 0.5f
        reverbKnob.onValueChange = { v ->
            reverbText.text = "🌊 REVERB\n${(v * 100).toInt()}%"
        }
        reverbCol.addView(reverbKnob)
        reverbCol.addView(reverbText)
        row2.addView(reverbCol)

        root.addView(row2)

        // ========== 🟢🔘 ON/OFF BUTTON ==========
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 0)

            statusText = TextView(this@MainActivity).apply {
                text = "🔴 NAKA-OFF"
                textSize = 16f
                setTextColor(0xFFFF6666.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 15)
            }
            addView(statusText)

            powerBtn = Button(this@MainActivity).apply {
                text = "🔘 TURN ON"
                textSize = 17f
                setBackgroundColor(0xFF228833.toInt())
                setTextColor(Color.WHITE)
                setPadding(60, 18, 60, 18)

                setOnClickListener {
                    isOn = !isOn
                    if (isOn) {
                        text = "🟢 TURN OFF"
                        setBackgroundColor(0xFFFF4444.toInt())
                        statusText.text = "🟢 NAKA-ON — Handa na!"
                        statusText.setTextColor(0xFF44FF44.toInt())
                    } else {
                        text = "🔘 TURN ON"
                        setBackgroundColor(0xFF228833.toInt())
                        statusText.text = "🔴 NAKA-OFF"
                        statusText.setTextColor(0xFFFF6666.toInt())
                    }
                }
            }
            addView(powerBtn)
            root.addView(this)
        }

        setContentView(root)
    }
}
