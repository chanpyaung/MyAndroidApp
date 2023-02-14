package com.chanaung.waveformeditor.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class WaveFormView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paint = Paint().apply {
        color = Color.argb( 255, 76, 175, 80)
        strokeWidth = 2f
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
    }

    private var inactivePaint = Paint().apply {
        color = Color.argb(255, 62, 69, 77)
        strokeWidth = 2f
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
    }

    private var startTrimX = 0F
    private var endTrimX = resources.displayMetrics.widthPixels.toFloat() - 10f
    private val minGap = 50f

    private val path = Path()
    private val clipPath = Path()
    private var mWidth = 1f
    private var mHeight = 1f
    private var waveForms = mutableListOf<Pair<Float, Float>>()

    private var trimLinePaint = Paint().apply {
        color = Color.argb(255, 126, 137, 153)
        strokeWidth = 5f
    }
    init {
        mWidth = resources.displayMetrics.widthPixels.toFloat()
        mHeight = resources.displayMetrics.heightPixels.toFloat()
        path.reset()
        clipPath.reset()
        waveForms.clear()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.let {
            if (waveForms.isNotEmpty()) {
                drawWaveForm(it)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        endTrimX = mWidth
    }

    private fun drawWaveForm(canvas: Canvas) {
        path.reset()
        val defaultAmplitudeHeight = 1.0f
        val centerY = (mHeight/2).toFloat() // this should give me the middle point, so assuming top = 0 to be 1, centerY = 0 && height/bottom = -1
        val stepX = mWidth/(waveForms.size-1)
        for (i in 0 until  waveForms.size-1) {
            val start = i * stepX
            val right = (i + 1) * stepX
            val top = abs(waveForms[i].second) * centerY
            val nexTop = abs(waveForms[i+1].second) * centerY
            val amplitudeTop = (centerY - top) * defaultAmplitudeHeight
            val nextAmplitudeTop = (centerY - nexTop) * defaultAmplitudeHeight
            val bottom = abs(waveForms[i].first) * centerY
            val nextBottom = abs(waveForms[i+1].first) * centerY
            val amplitudeBottom = (centerY + bottom ) * defaultAmplitudeHeight
            val nextAmplitudeBottom = (centerY + nextBottom ) * defaultAmplitudeHeight

            path.apply {
                moveTo(start, amplitudeTop)
                lineTo(start, amplitudeBottom)
                lineTo(right, nextAmplitudeBottom)
                lineTo(right, nextAmplitudeTop)
            }
        }
        path.lineTo(mWidth, centerY)
        path.close()

        canvas.drawPath(path, inactivePaint)
        canvas.save()
        clipPath.reset()
        clipPath.addRect(startTrimX, 0f, endTrimX, mHeight, Path.Direction.CCW)
        canvas.clipPath(clipPath)
        canvas.drawPath(path, paint)
        canvas.restore()
        drawTrimLines(canvas)
    }

    fun addWaveForms(mWaveforms: List<Pair<Float, Float>>) {
        invalidate()
        waveForms.clear()
        waveForms.addAll(mWaveforms)
    }

    fun clear() {
        waveForms.clear()
        path.reset()
        startTrimX = 0f
        endTrimX = mWidth
        invalidate()
    }

    fun getTrimmedWaveForm(): List<Pair<Float, Float>>? {
        if (waveForms.isEmpty()) {
            return null
        }
        val startIndex = (startTrimX / width * waveForms.size).toInt()
        var endIndex = (endTrimX / width * waveForms.size).toInt()
        if (endIndex == waveForms.size) {
            endIndex -= 1
        }
        return waveForms.toMutableList().slice(startIndex..endIndex)
    }

    private fun drawTrimLines(canvas: Canvas) {
        canvas.drawLine(startTrimX, 0f, startTrimX, mHeight, trimLinePaint)
        canvas.drawLine(endTrimX, 0f, endTrimX, mHeight, trimLinePaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                handleActionDown(event)
            }
            MotionEvent.ACTION_UP -> {
                handleActionUp(event)
            }
            MotionEvent.ACTION_MOVE -> {
                handleActionMove(event)
            }
            else -> return false
        }
        return true
    }

    private fun handleActionDown(event: MotionEvent) {
        calculateX(event.x)
    }

    private fun handleActionMove(event: MotionEvent) {
        calculateX(event.x)
        invalidate()
    }

    private fun handleActionUp(event: MotionEvent) {
        calculateX(event.x)
    }

    private fun calculateX(x: Float) {
        val leftDistance = abs(x - startTrimX)
        val rightDistance = abs(x - endTrimX)
        if (leftDistance < rightDistance && endTrimX - x > minGap) {
            if(endTrimX - x >= minGap && x > 0) {
                startTrimX = x
            }
        } else {
            if (x - startTrimX >= minGap && x - startTrimX > minGap && x < mWidth) {
                endTrimX = x
            }
        }
    }

}