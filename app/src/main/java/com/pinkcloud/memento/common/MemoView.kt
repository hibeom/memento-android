package com.pinkcloud.memento.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.pinkcloud.memento.R
import com.pinkcloud.memento.utils.formatMillisToDatetime
import timber.log.Timber

class MemoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), PickerDialogFragment.PickDatetimeListener {

    val layoutFrontCard: ConstraintLayout
    val layoutBackCard: ConstraintLayout
    val buttonPhoto: ImageButton
    val editFrontCaption: EditText
    val editBackCaption: EditText
    val sliderPriority: PrioritySlider
    val textAlarmState: TextView
    val textAlarmTime: TextView

    var isAlarmEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                textAlarmState.text = context.getString(R.string.on)
                textAlarmState.setTextColor(context.getColor(R.color.aqua))
            } else {
                textAlarmState.text = context.getString(R.string.off)
                textAlarmState.setTextColor(context.getColor(R.color.gray))
            }
        }

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

    override fun onFinishInflate() {
        super.onFinishInflate()
        textAlarmTime.text = formatMillisToDatetime(System.currentTimeMillis())
        textAlarmState.setOnClickListener {
            isAlarmEnabled = !isAlarmEnabled
        }
        textAlarmTime.setOnClickListener {
            PickerDialogFragment(this).apply {
                isCancelable = false
                show(
                    this@MemoView.findFragment<Fragment>().requireActivity().supportFragmentManager,
                    "PickerDialogFragment"
                )
            }
        }
    }

    override fun onPickDatetime(millis: Long) {
        textAlarmTime.text = formatMillisToDatetime(millis)
        invalidate()
    }
}