package com.chanaung.waveformeditor.views

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toRect
import kotlin.math.abs

class WaveFormView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        const val TRIM_LINE_TOUCH_AREA = 50f
    }
    private enum class TrimLine {
        START, END, NONE
    }

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
    private val minGap = 100f

    private val path = Path()
    private val pathArea = RectF()
    private val clipPath = Path()
    private var mWidth = 1f
    private var mHeight = 1f
    private var waveForms: List<Pair<Float, Float>>? = null
    private var activeTrimLine = TrimLine.NONE
    private val gestureExclusionRects = mutableListOf<Rect>()

    fun getWaveForms(): List<Pair<Float, Float>>? = waveForms

    private var trimLinePaint = Paint().apply {
        color = Color.argb(255, 126, 137, 153)
        strokeWidth = 10f
    }

    private var activeTrimLinePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 10f
    }
    init {
        mWidth = resources.displayMetrics.widthPixels.toFloat()
        mHeight = resources.displayMetrics.heightPixels.toFloat()
        path.reset()
        clipPath.reset()
        waveForms = null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        mHeight= MeasureSpec.getSize(heightMeasureSpec).toFloat()
        setMeasuredDimension(mWidth.toInt(), mHeight.toInt())
    }
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.let {
            if (waveForms?.isNotEmpty() == true) {
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
        if (waveForms.isNullOrEmpty()) {
            return
        }
        path.reset()
        val defaultAmplitudeHeight = 1.0f
        val centerY = (mHeight/2) // this should give me the middle point, so assuming top = 0 to be 1, centerY = 0 && height/bottom = -1
        waveForms?.let { waveForms ->
            val stepX = mWidth/(waveForms.size)
            if (waveForms.size > 1) {
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
            } else {
                for (i in waveForms.indices) {
                    val start = i * stepX
                    val amplitudeTop = (centerY - abs(waveForms[i].second) * centerY) * defaultAmplitudeHeight
                    val amplitudeBottom = (centerY + abs(waveForms[i].first) * centerY) * defaultAmplitudeHeight
                    path.apply {
                        moveTo(start, amplitudeTop)
                        lineTo(start, amplitudeBottom)
                        if (i == waveForms.size - 1) {
                            lineTo(mWidth, centerY)
                        }
                    }
                }
            }
        }
        path.close()
        canvas.drawPath(path, inactivePaint)
        canvas.save()
        clipPath.reset()
        pathArea.apply {
            left = startTrimX
            top = 0f
            right = endTrimX
            bottom = mHeight
        }
        clipPath.addRect(pathArea, Path.Direction.CCW)
        canvas.clipPath(clipPath)
        canvas.drawPath(path, paint)
        canvas.restore()
        drawTrimLines(canvas)
        updateGestureExclusion()
    }

    fun addWaveForms(mWaveforms: List<Pair<Float, Float>>) {
        invalidate()
        waveForms = null
        waveForms = mWaveforms
    }

    fun clear() {
        waveForms = null
        path.reset()
        startTrimX = 0f
        endTrimX = mWidth
        invalidate()
    }

    fun trimAt(start: Int, end: Int) {
        waveForms?.let { wforms ->
            val stepX = mWidth/(wforms.size-1)
            startTrimX = (start) * stepX
            endTrimX = (end) * stepX
        }
    }
    fun getTrimmedWaveForm(): List<Pair<Float, Float>>? {
        if (waveForms?.isEmpty() == true) {
            return null
        }
        waveForms?.let { wforms ->
            val startIndex = (startTrimX / mWidth * wforms.size).toInt()
            var endIndex = (endTrimX / mWidth * wforms.size).toInt()
            if (endIndex == wforms.size) {
                endIndex -= 1
            }
            return wforms.slice(startIndex..endIndex)
        }
        return null
    }

    private fun drawTrimLines(canvas: Canvas) {
        canvas.drawLine(startTrimX, 0f, startTrimX, mHeight, if (activeTrimLine == TrimLine.START) activeTrimLinePaint else trimLinePaint)
        canvas.drawLine(endTrimX, 0f, endTrimX, mHeight, if (activeTrimLine == TrimLine.END) activeTrimLinePaint else trimLinePaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val x = event.x
            when(event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (x >= startTrimX - TRIM_LINE_TOUCH_AREA && x <= startTrimX + TRIM_LINE_TOUCH_AREA) {
                        activeTrimLine = TrimLine.START
                    } else if (x >= endTrimX - TRIM_LINE_TOUCH_AREA && x <= endTrimX + TRIM_LINE_TOUCH_AREA) {
                        activeTrimLine = TrimLine.END
                    } else {
                        activeTrimLine = TrimLine.NONE
                    }
                }
                MotionEvent.ACTION_UP -> {
                    activeTrimLine = TrimLine.NONE
                }
                MotionEvent.ACTION_MOVE -> {
                    if (activeTrimLine != TrimLine.NONE) {
                        when(activeTrimLine) {
                            TrimLine.START -> {
                                if(endTrimX - x >= minGap && x > 0) {
                                    startTrimX = x
                                }
                            }
                            TrimLine.END -> {
                                if (x - startTrimX >= minGap && x - startTrimX > minGap && x < mWidth) {
                                    endTrimX = x
                                }
                            }
                            else -> {}
                        }
                        invalidate()
                    }

                }
                else -> return false
            }
        }
        return true
    }

    private fun updateGestureExclusion() {
        // Skip this call if we're not running on Android 10+
        if (Build.VERSION.SDK_INT < 29) return

        // First, lets clear out any existing rectangles
        gestureExclusionRects.clear()
        gestureExclusionRects += pathArea.toRect()
        systemGestureExclusionRects = gestureExclusionRects
    }

}