package com.gitaradistortion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class KnobView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var value = 0.5f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }

    var onValueChange: ((Float) -> Unit)? = null
    var baseColor = 0xFFFF8822.toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bgRect = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private val startAngle = -135f
    private val endAngle = 135f
    private val angleRange = endAngle - startAngle

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = minOf(w, h) / 2f - 4f
        bgRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = Color.parseColor("#2A2A2A")
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, radius, paint)

        paint.color = Color.parseColor("#444444")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawArc(bgRect, startAngle, angleRange, false, paint)

        paint.color = baseColor
        paint.strokeWidth = 4f
        val sweep = value * angleRange
        canvas.drawArc(bgRect, startAngle, sweep, false, paint)

        paint.color = Color.parseColor("#C89C3C")
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, radius * 0.65f, paint)

        paint.color = Color.WHITE
        paint.strokeWidth = 3f
        val ptrAngle = Math.toRadians((startAngle + sweep).toDouble())
        val ptrLen = radius * 0.55f
        val ptrX = centerX + sin(ptrAngle).toFloat() * ptrLen
        val ptrY = centerY - cos(ptrAngle).toFloat() * ptrLen
        canvas.drawLine(centerX, centerY, ptrX, ptrY, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val dx = event.x - centerX
                val dy = event.y - centerY
                var angle = Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble())).toFloat()
                if (angle < startAngle) angle += 360f
                val newValue = ((angle - startAngle) / angleRange).coerceIn(0f, 1f)
                if (newValue != value) {
                    value = newValue
                    onValueChange?.invoke(value)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
