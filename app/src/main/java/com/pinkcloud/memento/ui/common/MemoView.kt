package com.pinkcloud.memento.ui.common

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.pinkcloud.memento.R
import com.pinkcloud.memento.utils.GlideApp
import com.pinkcloud.memento.utils.formatMillisToDatetime
import com.pinkcloud.memento.utils.getMeasuredFontSize
import com.pinkcloud.memento.utils.getTypeface

class MemoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), PickerDialogFragment.PickDatetimeListener {

    private val layoutFrontCard: ConstraintLayout
    private val layoutBackCard: ConstraintLayout
    private val imagePhoto: ImageFilterView
    private val editFrontCaption: EditText
    private val editBackCaption: EditText
    private val sliderPriority: PrioritySlider
    private val checkboxAlarmState: CheckBox
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
            checkboxAlarmState.isChecked = value
            if (value) {
                textAlarmTime.paintFlags = 0
                textAlarmTime.setTextColor(context.getColor(R.color.background_dark))
            } else {
                textAlarmTime.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                textAlarmTime.setTextColor(context.getColor(R.color.gray_600))
            }
        }

    private var childClickable: Boolean
    private var isCacheEnable: Boolean
    private var isHintEnable: Boolean

    init {
        inflate(context, R.layout.layout_card, this)
        setBackgroundColor(context.getColor(R.color.gray_100))

        layoutFrontCard = findViewById(R.id.layout_front_card)
        layoutBackCard = findViewById(R.id.layout_back_card)
        imagePhoto = findViewById(R.id.image_photo)
        editFrontCaption = findViewById(R.id.edit_front_caption)
        editBackCaption = findViewById(R.id.edit_back_caption)
        sliderPriority = findViewById(R.id.slider_priority)
        checkboxAlarmState = findViewById(R.id.checkbox_alarm_state)
        textAlarmTime = findViewById(R.id.text_alarm_time)

        isAlarmEnabled = false

        context.theme.obtainStyledAttributes(attrs, R.styleable.MemoView, 0, 0).apply {
            try {
                childClickable = getBoolean(R.styleable.MemoView_childClickable, true)
                isCacheEnable = getBoolean(R.styleable.MemoView_isCacheEnable, true)
                isHintEnable = getBoolean(R.styleable.MemoView_isHintEnable, true)
            } finally {
                recycle()
            }
        }
        cameraDistance = 8000 * resources.displayMetrics.density
    }

    /**
     * limit max line of front and back caption EditText view
     * */
    private var maxFrontLines = 4
    private var maxBackLines = 10

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        maxFrontLines = editFrontCaption.measuredHeight/editFrontCaption.lineHeight
        maxBackLines = editBackCaption.measuredHeight/editBackCaption.lineHeight
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setCaptionTextStyle() {
        editFrontCaption.textSize = getMeasuredFontSize()
        editBackCaption.textSize = getMeasuredFontSize()
        editFrontCaption.typeface = getTypeface(context)
        editBackCaption.typeface = getTypeface(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        textAlarmTime.text = formatMillisToDatetime(System.currentTimeMillis())
        checkboxAlarmState.setOnClickListener {
            isAlarmEnabled = !isAlarmEnabled
            if (isAlarmEnabled) showTimePicker()
        }
        textAlarmTime.setOnClickListener {
            showTimePicker()
        }
        setCaptionTextStyle()

        if (!isHintEnable) {
            editFrontCaption.hint = ""
            editBackCaption.hint = ""
        }

        if (!childClickable) {
            editFrontCaption.movementMethod = null // To prevent intercepting key event
            editFrontCaption.keyListener = null // To prevent intercepting key event
            editBackCaption.movementMethod = null
            editBackCaption.keyListener = null
            sliderPriority.isEnabled = false
            checkboxAlarmState.isClickable = false
            textAlarmTime.isClickable = false
        }
        if (childClickable) {
            enableFront()
            editFrontCaption.doAfterTextChanged {
                val lineCount = editFrontCaption.lineCount
                if (lineCount > maxFrontLines) {
                    editFrontCaption.setText(it?.substring(0, it.length-1))
                    Toast.makeText(context, R.string.front_caption_is_full, Toast.LENGTH_SHORT).show()
                }
            }
            editBackCaption.doAfterTextChanged {
                val lineCount = editBackCaption.lineCount
                if (lineCount > maxBackLines) {
                    editBackCaption.setText(it?.substring(0, it.length-1))
                    Toast.makeText(context, R.string.back_caption_is_full, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showTimePicker() {
        PickerDialogFragment(this).apply {
            isCancelable = false
            show(
                this@MemoView.findFragment<Fragment>().requireActivity().supportFragmentManager,
                "PickerDialogFragment"
            )
        }
    }

    override fun onPickDatetime(millis: Long) {
        alarmTime = millis
//        invalidate()
    }

    fun resetVisibility() {
        if (layoutFrontCard.visibility == View.INVISIBLE) {
            flip()
        }
    }

    fun flip() {
        if (layoutFrontCard.visibility == View.VISIBLE) {
            layoutFrontCard.visibility = View.INVISIBLE
            layoutBackCard.visibility = View.VISIBLE
            if (childClickable) disableFront()
        } else {
            layoutFrontCard.visibility = View.VISIBLE
            layoutBackCard.visibility = View.INVISIBLE
            if (childClickable) enableFront()
        }
    }

    private fun enableFront() {
        editFrontCaption.isEnabled = true
        editBackCaption.isEnabled = false
        sliderPriority.isEnabled = false
        checkboxAlarmState.isClickable = false
        textAlarmTime.isClickable = false
    }

    private fun disableFront() {
        editFrontCaption.isEnabled = false
        editBackCaption.isEnabled = true
        sliderPriority.isEnabled = true
        checkboxAlarmState.isClickable = true
        textAlarmTime.isClickable = true
    }
}