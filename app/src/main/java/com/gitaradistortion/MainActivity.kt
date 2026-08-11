package com.gitaradistortion

import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

// 🎛️ BILOG NA PIHITAN — MAY KAP AT UMIILAW HABANG PINIPIHIT!
class KnobView(context: android.content.Context) : View(context) {
    var value = 0.5f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }
    
    var onValueChange: ((Float) -> Unit)? = null
    var baseColor = 0xFFFF6622.toInt()

    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    // ✅ KULAY NA UMIILAW — HABANG LUMALAKI ANG VALUE, MAS MABRIGHT!
    private fun getGlowingColor(): Int {
        val baseR = (baseColor shr 16) and 0xFF
        val baseG = (baseColor shr 8) and 0xFF
        val baseB = baseColor and 0xFF
        
        // ✅ MAS MALAKI ANG VALUE → MAS MABRIGHT ANG KULAY!
        val brightness = 0.4f + value * 0.6f
        val r = minOf(255, (baseR * brightness / 0.7f).toInt())
        val g = minOf(255, (baseG * brightness / 0.7f).toInt())
        val b = minOf(255, (baseB * brightness / 0.7f).toInt())
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2
        val cy = h / 2
        val r = minOf(w, h) / 2 - 8f
        val glowColor = getGlowingColor()

        // ✅ ILALIM — MAITIM NA BILOG
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFF1A1A1A.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        // ✅ GILID — UMIILAW NA GUHIT! MAS MAKAPAL KAPAG MATAAS ANG VALUE!
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 4f + value * 5f  // ✅ LUMALAKI ANG KAPAL!
        paint.color = glowColor
        canvas.drawCircle(cx, cy, r - 4f, paint)

        // ✅ KAP — PILAK NA BILOG SA TAAS
        val capR = r * 0.75f
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = 0xFFE8E8E8.toInt()
        canvas.drawCircle(cx, cy, capR, paint)

        // ✅ ANINO SA KAP
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = 0xFFBBBBBB.toInt()
        canvas.drawCircle(cx, cy, capR - 2f, paint)

        // ✅ TANDA NG DIREKSYON — UMIILAW! MAS MAKAPAL!
        val angle = -135f + (270f) * value
        val rad = Math.toRadians(angle.toDouble())
        paint.strokeWidth = 5f + value * 4f  // ✅ LUMALAKI ANG GUHIT!
        paint.color = glowColor
        val indicatorLen = capR * 0.85f
        val endX = cx + indicatorLen * sin(rad).toFloat()
        val endY = cy - indicatorLen * cos(rad).toFloat()
        canvas.drawLine(cx, cy, endX, endY, paint)

        // ✅ GITNA NG KAP — UMIILAW NA BILOG!
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = glowColor
        canvas.drawCircle(cx, cy, 9f + value * 5f, paint)  // ✅ LUMALAKI RIN!
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
        title.setPadding(0, 10, 0, 30)
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
        volKnob.layoutParams = LinearLayout.LayoutParams(180, 180)
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
        toneKnob.layoutParams = LinearLayout.LayoutParams(180, 180)
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
        distKnob.layoutParams = LinearLayout.LayoutParams(180, 180)
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
        row2.setPadding(0, 20, 0, 0)

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
        gainKnob.layoutParams = LinearLayout.LayoutParams(180, 180)
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
        chorusKnob.layoutParams = LinearLayout.LayoutParams(180, 180)
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
        reverbKnob.layoutParams = LinearLayout.LayoutParams(180, 180)
        reverbKnob.baseColor = 0xFFAA66FF.toInt()
        reverbKnob.value = 0.5f
        reverbKnob.onValueChange = { v ->
            reverbText.text = "🌊 REVERB\n${(v * 100).toInt()}%"
        }
        reverbCol.addView(reverbKnob)
        reverbCol.addView(reverbText)
        row2.addView(reverbCol)

        root.addView(row2)
        setContentView(root)
    }
}
