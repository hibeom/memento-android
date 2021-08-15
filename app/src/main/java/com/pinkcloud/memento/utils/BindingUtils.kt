package com.pinkcloud.memento.utils

import androidx.databinding.BindingAdapter
import com.pinkcloud.memento.common.MemoView

@BindingAdapter("captionTextSize")
fun MemoView.setCaptionTextSize(textSize: Float) {
    setCaptionTextSize(textSize)
}