package com.gitaradistortion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sqrt

class KnobView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var value = 0.5f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }

    var onValueChange: ((Float) -> Unit)? = null
    var baseColor = 0xFFFF6622.toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var lastAngle = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = minOf(w, h) / 2f - 8f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // ✅ Bilog na gilid
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = 0xFF333333.toInt()
        canvas.drawCircle(centerX, centerY, radius, paint)

        // ✅ Napihit na bahagi
        val sweep = value * 300f
        paint.color = baseColor
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius),
            120f, sweep, false, paint)

        // ✅ Loob ng bilog
        paint.style = Paint.Style.FILL
        paint.color = 0xFF222222.toInt()
        canvas.drawCircle(centerX, centerY, radius - 6f, paint)

        // ✅ Tuldok na palaso
        val angleRad = Math.toRadians((120f + sweep).toDouble())
        val indicatorRadius = radius * 0.7f
        val x = centerX + Math.cos(angleRad).toFloat() * indicatorRadius
        val y = centerY + Math.sin(angleRad).toFloat() * indicatorRadius
        paint.color = Color.WHITE
        canvas.drawCircle(x, y, 8f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x - centerX
        val y = event.y - centerY
        val distance = sqrt(x*x + y*y)

        if (distance < radius * 0.4f) return true

        val angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble())).toFloat()
        var normalized = if (angle < -90f) angle + 450f else angle + 90f
        normalized = normalized.coerceIn(0f, 360f)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> lastAngle = normalized
            MotionEvent.ACTION_MOVE -> {
                var delta = normalized - lastAngle
                if (abs(delta) > 180f) delta = if (delta > 0) delta - 360f else delta + 360f
                value += delta / 300f
                value = value.coerceIn(0f, 1f)
                lastAngle = normalized
                onValueChange?.invoke(value)
            }
        }
        return true
    }
}
