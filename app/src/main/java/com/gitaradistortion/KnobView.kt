package com.gitaradistortion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class KnobView(context: Context) : View(context) {
    var value = 0.5f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }

    var onValueChange: ((Float) -> Unit)? = null
    var baseColor = 0xFFFF6622.toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        try {
            val w = width.toFloat()
            val h = height.toFloat()
            val cx = w / 2
            val cy = h / 2
            val r = minOf(cx, cy) - 4f
            val angleRad = Math.toRadians((-135 + value * 270).toDouble())

            paint.color = Color.parseColor("#2A2A2A")
            canvas.drawCircle(cx, cy, r + 5f, paint)

            val brightness = 0.5f + value * 0.5f
            val red = (Color.red(baseColor) * brightness).toInt()
            val green = (Color.green(baseColor) * brightness).toInt()
            val blue = (Color.blue(baseColor) * brightness).toInt()
            paint.color = Color.rgb(red, green, blue)
            canvas.drawCircle(cx, cy, r, paint)

            paint.color = Color.WHITE
            paint.strokeWidth = 4f
            paint.strokeCap = Paint.Cap.ROUND
            val len = r * 0.7f
            canvas.drawLine(
                cx, cy,
                cx + (cos(angleRad) * len).toFloat(),
                cy + (sin(angleRad) * len).toFloat(),
                paint
            )
        } catch (_: Exception) {}
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val cx = width / 2f
                val cy = height / 2f
                val dx = event.x - cx
                val dy = event.y - cy
                var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                if (angleDeg < 0) angleDeg += 360f
                var newValue = ((angleDeg - 225f) / 270f).coerceIn(0f, 1f)

                if (abs(newValue - value) > 0.001f) {
                    value = newValue
                    onValueChange?.invoke(newValue)
                }
                return true
            }
        } catch (_: Exception) {}
        return super.onTouchEvent(event)
    }
}
