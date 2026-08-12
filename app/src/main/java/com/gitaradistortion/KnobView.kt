package com.gitaradistortion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.*

class KnobView(context: Context) : View(context) {
    var value = 0.5f
        set(v) { field = v.coerceIn(0f, 1f); invalidate(); onValueChange?.invoke(field) }
    var onValueChange: ((Float) -> Unit)? = null
    var baseColor = 0xFFFF8822.toInt()
    var pager: ViewPager2? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var startAngle = 0.0
    private var isDragging = false

    private fun glowColor(): Int {
        val f = 0.35f + value * 0.65f
        val r = ((baseColor shr 16 and 0xFF) * f).toInt().coerceAtMost(255)
        val g = ((baseColor shr 8 and 0xFF) * f).toInt().coerceAtMost(255)
        val b = ((baseColor and 0xFF) * f).toInt().coerceAtMost(255)
        return Color.rgb(r, g, b)
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = minOf(width, height) / 2 - 3f
        val glow = glowColor()

        // Rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = 0xFF444444.toInt()
        canvas.drawCircle(cx, cy, r, paint)

        // Face
        paint.style = Paint.Style.FILL
        paint.color = 0xFF222222.toInt()
        canvas.drawCircle(cx, cy, r - 2f, paint)

        // Glow ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f + value * 4f
        paint.color = glow
        canvas.drawCircle(cx, cy, r - 4f, paint)

        // Knob cap
        val capR = r * 0.65f
        paint.style = Paint.Style.FILL
        paint.color = 0xFFE8E8E8.toInt()
        canvas.drawCircle(cx, cy, capR, paint)

        // Indicator line
        val angle = -135f + 270f * value
        val rad = Math.toRadians(angle.toDouble())
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f + value * 3f
        paint.color = glow
        val len = capR * 0.75f
        canvas.drawLine(cx, cy,
            cx + len * sin(rad).toFloat(),
            cy - len * cos(rad).toFloat(), paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cx = width / 2f
        val cy = height / 2f
        val angle = Math.toDegrees(atan2(event.x - cx, cy - event.y).toDouble())

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startAngle = angle; isDragging = true; pager?.isUserInputEnabled = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDragging) return true
                var delta = angle - startAngle
                if (delta > 180) delta -= 360.0
                if (delta < -180) delta += 360.0
                value += (delta / 270.0).toFloat()
                startAngle = angle
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false; pager?.isUserInputEnabled = true
            }
        }
        return true
    }
}
