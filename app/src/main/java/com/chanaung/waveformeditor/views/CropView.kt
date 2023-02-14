package com.chanaung.waveformeditor.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.DragEvent
import android.view.MotionEvent

class CropView(context: Context, attrs: AttributeSet): androidx.appcompat.widget.AppCompatImageView(context, attrs) {

    init {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onDragEvent(event: DragEvent?): Boolean {
        return super.onDragEvent(event)
    }

    override fun setOnDragListener(l: OnDragListener?) {
        super.setOnDragListener(l)
    }
}