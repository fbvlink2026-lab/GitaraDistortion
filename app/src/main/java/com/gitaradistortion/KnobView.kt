package com.gitaradistortion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.*

class KnobView(ctx:Context):View(ctx){
    var value=0.5f
        set(v){field=v.coerceIn(0f,1f);invalidate();onChange?.invoke(field)}
    var onChange:((Float)->Unit)?=null
    var baseColor=0xFFFF8822.toInt()
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var downY=0f;private var downV=0f
    private val touchSlop=ViewConfiguration.get(ctx).scaledTouchSlop
    private var isKnobTouch=false

    override fun onDraw(c:Canvas){
        val cx=width/2f;val cy=height/2f;val r=minOf(cx,cy)-3f
        p.style=Paint.Style.FILL;p.color=0xFF2A2A2A.toInt();c.drawCircle(cx,cy,r,p)
        p.style=Paint.Style.STROKE;p.color=baseColor;p.strokeWidth=2.5f;c.drawCircle(cx,cy,r,p)
        val ang=(value*270-225)*PI/180
        val x1=cx+(r*0.65*cos(ang)).toFloat()
        val y1=cy+(r*0.65*sin(ang)).toFloat()
        val x2=cx+(r*0.85*cos(ang)).toFloat()
        val y2=cy+(r*0.85*sin(ang)).toFloat()
        p.strokeWidth=3f;p.color=0xFFFFFFFF.toInt();c.drawLine(x1,y1,x2,y2,p)
    }
    override fun onTouchEvent(e:MotionEvent):Boolean{
        when(e.action){
            MotionEvent.ACTION_DOWN->{downY=e.rawY;downV=value;isKnobTouch=false;parent.requestDisallowInterceptTouchEvent(true);return true}
            MotionEvent.ACTION_MOVE->{
                val dy=abs(e.rawY-downY)
                if(dy>touchSlop*0.5f)isKnobTouch=true
                if(isKnobTouch)value=(downY-e.rawY)/height*1.2f+downV
            }
            MotionEvent.ACTION_UP->parent.requestDisallowInterceptTouchEvent(false)
        }
        return true
    }
}
