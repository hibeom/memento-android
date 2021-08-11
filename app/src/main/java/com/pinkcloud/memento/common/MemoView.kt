package com.pinkcloud.memento.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.pinkcloud.memento.R
import com.pinkcloud.memento.utils.GlideApp
import com.pinkcloud.memento.utils.formatMillisToDatetime

class MemoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), PickerDialogFragment.PickDatetimeListener {

    private val layoutFrontCard: ConstraintLayout
    private val layoutBackCard: ConstraintLayout
    private val imagePhoto: ImageFilterView
    private val editFrontCaption: EditText
    private val editBackCaption: EditText
    private val sliderPriority: PrioritySlider
    private val textAlarmState: TextView
    private val textAlarmTime: TextView

    var imagePath: String? = null
        set(value) {
            field = value
            val request = GlideApp.with(this).load(field).centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .skipMemoryCache(true)
            if (!isCacheEnable) request.diskCacheStrategy(DiskCacheStrategy.NONE)
            request.into(imagePhoto)
        }
    var frontCaption: String? = null
        get() = editFrontCaption.text.toString()
        set(value) {
            field = value
            editFrontCaption.setText(field)
        }
    var backCaption: String? = null
        get() = editBackCaption.text.toString()
        set(value) {
            field = value
            editBackCaption.setText(field)
        }
    var priority: Int = 50
        get() = sliderPriority.value.toInt()
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
                textAlarmState.setTextColor(context.getColor(R.color.conflowerblue))
            } else {
                textAlarmState.text = context.getString(R.string.off)
                textAlarmState.setTextColor(context.getColor(R.color.gray))
            }
        }

    private var childClickable: Boolean
    private var isCacheEnable: Boolean

    init {
        inflate(context, R.layout.layout_card, this)
        setBackgroundColor(context.getColor(R.color.gray_100))

        layoutFrontCard = findViewById(R.id.layout_front_card)
        layoutBackCard = findViewById(R.id.layout_back_card)
        imagePhoto = findViewById(R.id.image_photo)
        editFrontCaption = findViewById(R.id.edit_front_caption)
        editBackCaption = findViewById(R.id.edit_back_caption)
        sliderPriority = findViewById(R.id.slider_priority)
        textAlarmState = findViewById(R.id.text_alarm_state)
        textAlarmTime = findViewById(R.id.text_alarm_time)

        isAlarmEnabled = false

        context.theme.obtainStyledAttributes(attrs, R.styleable.MemoView, 0, 0).apply {
            try {
                childClickable = getBoolean(R.styleable.MemoView_childClickable, true)
                isCacheEnable = getBoolean(R.styleable.MemoView_isCacheEnable, true)
            } finally {
                recycle()
            }
        }
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
        enableFront()
        if (!childClickable) {
            editFrontCaption.movementMethod = null // To prevent intercepting key event
            editFrontCaption.keyListener = null // To prevent intercepting key event
            editBackCaption.movementMethod = null
            editBackCaption.keyListener = null
            sliderPriority.isClickable = false
            textAlarmState.isClickable = false
            textAlarmTime.isClickable = false
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
            disableFront()
        } else {
            layoutFrontCard.visibility = View.VISIBLE
            layoutBackCard.visibility = View.INVISIBLE
            enableFront()
        }
    }

    private fun enableFront() {
        editFrontCaption.isEnabled = true
        editBackCaption.isEnabled = false
        sliderPriority.isEnabled = false
        textAlarmState.isClickable = false
        textAlarmTime.isClickable = false
    }

    private fun disableFront() {
        editFrontCaption.isEnabled = false
        editBackCaption.isEnabled = true
        sliderPriority.isEnabled = true
        textAlarmState.isClickable = true
        textAlarmTime.isClickable = true
    }
}