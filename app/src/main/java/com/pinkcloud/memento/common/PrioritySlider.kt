package com.pinkcloud.memento.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.slider.Slider
import com.pinkcloud.memento.R

class PrioritySlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Slider(context, attrs, defStyleAttr) {



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

    }
}