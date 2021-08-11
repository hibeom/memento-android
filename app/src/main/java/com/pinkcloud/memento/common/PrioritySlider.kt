package com.pinkcloud.memento.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.slider.Slider
import com.pinkcloud.memento.R
import timber.log.Timber

class PrioritySlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Slider(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        trackActiveTintList = if (value < 50) {
            var red = 225/50f*(value+1) + 30
            var green = 92/50f*(value+1) + 150
            ColorStateList.valueOf(Color.rgb(red.toInt(), green.toInt(), 0))
        } else {
            var green = 242 - 242/50f*(value-50)
            ColorStateList.valueOf(Color.rgb(255, green.toInt(), 0))
        }

    }
}