package com.pinkcloud.memento.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.pinkcloud.memento.R
import com.pinkcloud.memento.formatMillisToDatetime

class MemoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val layoutFrontCard: ConstraintLayout
    val layoutBackCard: ConstraintLayout
    val buttonPhoto: ImageButton
    val editFrontCaption: EditText
    val editBackCaption: EditText
    val sliderPriority: PrioritySlider
    val textAlarmState: TextView
    val textAlarmTime: TextView

    var isAlarmEnabled: Boolean

    init {
        inflate(context, R.layout.layout_card, this)
        setBackgroundColor(context.getColor(R.color.gray_100))

        layoutFrontCard = findViewById<ConstraintLayout>(R.id.layout_front_card)
        layoutBackCard = findViewById<ConstraintLayout>(R.id.layout_back_card)
        buttonPhoto = findViewById<ImageButton>(R.id.button_photo)
        editFrontCaption = findViewById<EditText>(R.id.edit_front_caption)
        editBackCaption = findViewById<EditText>(R.id.edit_back_caption)
        sliderPriority = findViewById<PrioritySlider>(R.id.slider_priority)
        textAlarmState = findViewById<TextView>(R.id.text_alarm_state)
        textAlarmTime = findViewById<TextView>(R.id.text_alarm_time)

        isAlarmEnabled = false
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        textAlarmTime.text = formatMillisToDatetime(System.currentTimeMillis())

        textAlarmState.setOnClickListener {
            isAlarmEnabled = !isAlarmEnabled
            setAlarmState(isAlarmEnabled)
        }
        setAlarmState(isAlarmEnabled)
    }

    fun setAlarmState(enable: Boolean) {
        isAlarmEnabled = enable
        if (enable) {
            textAlarmState.text = context.getString(R.string.on)
            textAlarmState.setTextColor(context.getColor(R.color.aqua))
        } else {
            textAlarmState.text = context.getString(R.string.off)
            textAlarmState.setTextColor(context.getColor(R.color.gray))
        }
    }

}