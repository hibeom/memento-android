package com.pinkcloud.memento.common

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pinkcloud.memento.R
import com.pinkcloud.memento.utils.GlideApp
import com.pinkcloud.memento.utils.formatMillisToDatetime
import timber.log.Timber

class MemoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), PickerDialogFragment.PickDatetimeListener {

    private val layoutFrontCard: ConstraintLayout
    private val layoutBackCard: ConstraintLayout
    val buttonPhoto: ImageButton
    private val editFrontCaption: EditText
    private val editBackCaption: EditText
    private val sliderPriority: PrioritySlider
    private val textAlarmState: TextView
    private val textAlarmTime: TextView

    var imagePath: String? = null
        set(value) {
            field = value
            GlideApp.with(this).load(field).centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(buttonPhoto)
        }
    var frontCaption: String? = null
        set(value) {
            field = value
            editFrontCaption.setText(field)
        }
    var backCaption: String? = null
        set(value) {
            field = value
            editBackCaption.setText(field)
        }
    var priority: Int = 50
        set(value) {
            field = value
            sliderPriority.value = field.toFloat()
        }
    var alarmTime: Long = System.currentTimeMillis()
        set(value) {
            field = value
            textAlarmTime.text = formatMillisToDatetime(field)
        }
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

        layoutFrontCard = findViewById(R.id.layout_front_card)
        layoutBackCard = findViewById(R.id.layout_back_card)
        buttonPhoto = findViewById(R.id.button_photo)
        editFrontCaption = findViewById(R.id.edit_front_caption)
        editBackCaption = findViewById(R.id.edit_back_caption)
        sliderPriority = findViewById(R.id.slider_priority)
        textAlarmState = findViewById(R.id.text_alarm_state)
        textAlarmTime = findViewById(R.id.text_alarm_time)

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
        alarmTime = millis
//        invalidate()
    }

    fun flip() {
        if (layoutFrontCard.visibility == View.VISIBLE) {
            layoutFrontCard.visibility = View.INVISIBLE
            layoutBackCard.visibility = View.VISIBLE
        } else {
            layoutFrontCard.visibility = View.VISIBLE
            layoutBackCard.visibility = View.INVISIBLE
        }
    }
}