package com.pinkcloud.memento.common

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import kotlin.math.abs

class OverlapLayoutManager : RecyclerView.LayoutManager() {

    var currentPosition: Int = 0

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        Timber.d("onLayoutChildren")
//        detachAndScrapAttachedViews(recycler)
        if (state.itemCount <= 0) return
        var startPosition = 0
        var endPosition = startPosition + 1
        if (childCount > 0) {
            val topChild = getChildAt(childCount - 1)!!
            val topChildPosition = getPosition(topChild)
            val bottomChild = getChildAt(0)!! // child at bottom
            val bottomChildPosition = getPosition(bottomChild)
            startPosition = bottomChildPosition + 1
            endPosition = topChildPosition + 1
        }
        for (i in startPosition until endPosition + 1) {
            val view = recycler.getViewForPosition(i)
            addView(view, 0) // stack reverse
            layoutView(view)
        }
    }

    // sofa, carpet, blank, coffee
    // ------------ addView position 0                   (childCount-1)th child
    // ------------ addView position 1
    // ------------ addView position 2                   1st child
    // ------------ addView position 3 (startPosition) , 0th child
    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (childCount <= 0) return 0
        // top 에서 2번째부턴 1, 2번 째 children 을 함께 offset
        // currentPosition 의 뷰의 bottom 이 0 보다 작아지면, currentPosition  = currentPosition + 1

        var topChild = getChildAt(childCount - 1)
        val topChildPosition = getPosition(topChild!!)
        currentPosition = topChildPosition

        if (dy >= 0) {
            /** scroll up*/
            if (topChildPosition == state.itemCount-1) return 0 /** fix the last card*/

            val bottomDiff = topChild.bottom
            var dyRemain = dy
            if (bottomDiff < dy) {
                dyRemain = dy - bottomDiff
                topChild.offsetTopAndBottom(-bottomDiff)

                removeView(topChild)
                recycler.recycleView(topChild)

                topChild = getChildAt(childCount - 1)
                currentPosition += 1

                if (currentPosition == state.itemCount - 1) {
                    return bottomDiff
                }
                val view = recycler.getViewForPosition(currentPosition + 1)
                addView(view, 0) // stack reverse
                layoutView(view)
            }
            topChild?.offsetTopAndBottom(-dyRemain)
        } else {
            /** scroll down*/
            val topDiff = abs(topChild.top)
            var dyRemain = dy
            if (topDiff < abs(dy)) {
                dyRemain = dy + topDiff
                topChild.offsetTopAndBottom(topDiff)
                if (currentPosition == 0) {
                    return topDiff
                }

                // remove and recycle bottom child
                if (currentPosition < state.itemCount - 1) {
                    val bottomChild = getChildAt(0)
                    bottomChild?.let {
                        removeView(bottomChild)
                        recycler.recycleView(bottomChild)
                    }
                }

                currentPosition -= 1
                val view = recycler.getViewForPosition(currentPosition)
                addView(view)
                measureChildWithMargins(view, 0, 0)
                val left = paddingLeft
                val right = left + view.measuredWidth
                val bottom = 0
                val top = bottom - view.measuredHeight
                layoutDecoratedWithMargins(view, left, top, right, bottom)

                topChild = view
            }
            topChild.offsetTopAndBottom(-dyRemain)
        }
        return dy
    }

    private fun layoutView(view: View) {
        measureChildWithMargins(view, 0, 0)
        val left = paddingLeft
        val right = left + view.measuredWidth
        val top = 0
        val bottom = top + view.measuredHeight
        layoutDecoratedWithMargins(view, left, top, right, bottom)
    }

    override fun canScrollVertically(): Boolean {
        return true
    }
}