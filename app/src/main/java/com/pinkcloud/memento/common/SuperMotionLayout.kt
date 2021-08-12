package com.pinkcloud.memento.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.motion.widget.MotionLayout
import timber.log.Timber

class SuperMotionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    private var startX = 0f
    private var isSwipeTriggered = false
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if(ev?.action == MotionEvent.ACTION_DOWN) {
            startX = ev.x
            isSwipeTriggered = false
        }

        return when (ev?.actionMasked) {
            // Always handle the case of the touch gesture being complete.
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                // Release the scroll.
                false // Do not intercept touch event, let the child handle it
            }
            MotionEvent.ACTION_MOVE -> {
                if (isSwipeTriggered) {
                    // We're currently scrolling, so yes, intercept the
                    // touch event!
                    true
                } else {

                    // If the user has dragged their finger horizontally more than
                    // the touch slop, start the scroll

                    // left as an exercise for the reader
                    val xDiff: Int = Math.abs(ev.x - startX).toInt()

                    // Touch slop should be calculated using ViewConfiguration
                    // constants.
                    if (xDiff > 50) {
                        // Start scrolling!
                        isSwipeTriggered = true
                        true
                    } else {
                        false
                    }
                }
            }
            else -> {
                // In general, we don't want to intercept touch events. They should be
                // handled by the child view.
                false
            }
        }
    }
}