package com.pinkcloud.memento.utils

import android.view.View
import androidx.databinding.BindingAdapter
import com.pinkcloud.memento.common.MemoView
import com.pinkcloud.memento.database.Memo

@BindingAdapter("hideIfEmpty", "memos")
fun setVisibilityByMemos(view: View, hideIfEmpty: Boolean, memos: List<Memo>?) {
    memos?.let {
        if (hideIfEmpty) {
            if (it.isEmpty()) view.visibility = View.GONE
            else view.visibility = View.VISIBLE
        } else {
            if (it.isEmpty()) view.visibility = View.VISIBLE
            else view.visibility = View.GONE
        }
    }
}

@BindingAdapter("memo")
fun setMemo(memoView: MemoView, memo: Memo) {
    memoView.run {
        imagePath = memo.imagePath
        frontCaption = memo.frontCaption
        backCaption = memo.backCaption
        priority = memo.priority
        isAlarmEnabled = memo.isAlarmEnabled
        alarmTime = memo.alarmTime
        setCaptionTextStyle()
    }
}