package com.gitaradistortion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class KnobView(context: Context) : View(context) {
    var value = 0.5f
        set(v) { field = v.coerceIn(0f, 1f); invalidate() }

    // ✅ TUKUYIN ANG TAMANG URI — DAPAT MAY: (Float) -> Unit
    var onValueChange: ((Float) -> Unit)? = null

    var baseColor = 0xFFFF6622.toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var angle = 0.0

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2
        val cy = h / 2
        val r = minOf(cx, cy) - 4f

        angle = (-135 + value * 270) * PI / 180

        // Bilog na gilid
        paint.color = Color.DKGRAY
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, r + 4f, paint)

        // Katawan ng pihitan
        paint.color = baseColor
        canvas.drawCircle(cx, cy, r, paint)

        // Guhit ng turo
        paint.color = Color.WHITE
        paint.strokeWidth = 4f
        paint.strokeCap = Paint.Cap.ROUND
        val len = r * 0.75f
        canvas.drawLine(
            cx, cy,
            cx + (cos(angle) * len).toFloat(),
            cy + (sin(angle) * len).toFloat(),
            paint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val cx = width / 2f
            val cy = height / 2f
            val dx = event.x - cx
            val dy = event.y - cy
            var a = atan2(dy, dx) * 180 / PI
            if (a < 0) a += 360.0
            var v = ((a - 225) / 270).toFloat()
            v = v.coerceIn(0f, 1f)
            if (v != value) {
                value = v
                // ✅ I-PASA ANG HALAGA — DAPAT MAY PARAMETER!
                onValueChange?.invoke(v)
            }
            return true
        }
        return super.onTouchEvent(event)
    }
}
