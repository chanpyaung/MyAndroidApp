package com.chanaung.waveformeditor.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

class WaveFormView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var paint = Paint()
    private val path = Path()
    private var width = 1f
    private var height = 1f
    private var waveForms = mutableListOf<Pair<Float, Float>>()
    init {
        paint.apply {
            color = Color.rgb(76, 175, 80)
            strokeWidth = 0f
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
        }

        width = resources.displayMetrics.widthPixels.toFloat()
        height = resources.displayMetrics.heightPixels.toFloat()
        waveForms.clear()
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
        width = w.toFloat()
        height = h.toFloat()
    }

    private fun drawWaveForm(canvas: Canvas) {
        path.reset()
        val defaultAmplitudeHeight = 0.5f
        val centerY = height/2 // this should give me the middle point, so assuming top = 0 to be 1, centerY = 0 && height/bottom = -1
        val stepX = width/(waveForms.size-1)
        for (i in 0 until  waveForms.size-2) {
            var start = i * stepX
            var right = (i + 1) * stepX
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
        canvas.drawPath(path, paint)
    }

    fun addWaveForms(mWaveforms: List<Pair<Float, Float>>) {
        invalidate()
        waveForms.clear()
        waveForms.addAll(mWaveforms)
    }

}