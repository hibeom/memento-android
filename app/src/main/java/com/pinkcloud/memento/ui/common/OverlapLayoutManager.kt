package com.pinkcloud.memento.ui.common

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Integer.max
import kotlin.math.abs
import kotlin.math.min

class OverlapLayoutManager : RecyclerView.LayoutManager() {

    var currentPosition: Int = 0
    var recycler: RecyclerView.Recycler? = null
    var state: RecyclerView.State? = null

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount <= 0) return
        if (childCount > 0) return
        currentPosition = min(currentPosition, state.itemCount - 1)
        this.recycler = recycler
        this.state = state

        for (i in currentPosition until state.itemCount) {
            if (i > currentPosition + 1) break
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

    // There was a problem adding topChild above current shown view
    // If you encounter the same problem, check the code below. You can address this by changing children only when scrolling down if topDiff > 0
    //    val topDiff = topChild.top
    //    var dyRemain = dy
    //
    //    if (topDiff <= -dy && topDiff >= 0) {
    //        dyRemain = dy + topDiff
    //        topChild.offsetTopAndBottom(topDiff)
    //        Timber.d("dy:$dy, dyRemain:$dyRemain, topDiff:$topDiff")
    //        if (currentPosition == 0) {
    //            return -topDiff
    //        }
    //
    //        if (currentPosition < state.itemCount - 1) {
    //            val bottomChild = getChildAt(0)
    //            bottomChild?.let {
    //                removeView(bottomChild)
    //                recycler.recycleView(bottomChild)
    //            }
    //        }
    //        currentPosition -= 1
    //        ...
    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (childCount <= 0) return 0
        var topChild = getChildAt(childCount - 1)!!
//        var topChildPosition = getPosition(topChild)
//        Timber.d("top child position: $topChildPosition")

        if (dy >= 0) {
            /** scroll up*/
            if (currentPosition == state.itemCount - 1) return 0
            /** fix the last card*/

            val bottomDiff = topChild.bottom
            var dyRemain = dy
            if (bottomDiff <= dy) {
                dyRemain = dy - bottomDiff
                topChild.offsetTopAndBottom(-bottomDiff)

                removeView(topChild)
                recycler.recycleView(topChild)

                topChild = getChildAt(childCount - 1)!!
                currentPosition += 1

                if (currentPosition == state.itemCount - 1) {
                    return bottomDiff
                }
                val view = recycler.getViewForPosition(currentPosition + 1)
                addView(view, 0) // stack reverse
                layoutView(view)
            }
            topChild.offsetTopAndBottom(-dyRemain)
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

    /**
     * @param positionStart removed item position
     * @param itemCount removed items count
     * */
    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        // when search text changed or complete memo
        Timber.d("onItemsRemoved")
        Timber.d("positionStart:$positionStart")
        currentPosition = min(positionStart, this.itemCount - 1)
        if (this.itemCount <= 0) currentPosition = 0
        recycler?.let { removeAndRecycleAllViews(it) }
    }

    /**
     * @param positionStart added item position
     * @param itemCount added items count
     * */
    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        // when search text changed or add memo
        Timber.d("onItemsAdded")
        Timber.d("positionStart:$positionStart")
        currentPosition = positionStart
        recycler?.let { removeAndRecycleAllViews(it) }
    }

    override fun onScrollStateChanged(state: Int) {
        when (state) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                if (recycler == null || this.state == null) return
                val topChild = getChildAt(childCount - 1)
                topChild?.let {
                    if (abs(topChild.top) > abs(topChild.bottom)) {
                        monotoneScrollToPosition(recycler!!, this.state!!, currentPosition + 1)
                    } else {
                        monotoneScrollToPosition(recycler!!, this.state!!, currentPosition)
                    }
                }
            }
        }
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        // My silly smooth scroll. Need to improved smooth scoll
        if (childCount == 0) return
        recycler?.let { monotoneScrollToPosition(it, state, position) }
    }

    private fun monotoneScrollToPosition(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        position: Int
    ) {
        val topChild = getChildAt(childCount - 1)!!
        val topChildPosition = getPosition(topChild)
        val childHeight = topChild.measuredHeight
        var dy: Int
        if (topChildPosition < position) {
            dy = (position - topChildPosition - 1) * childHeight + topChild.bottom
        } else {
            dy = (topChildPosition - position) * childHeight + -topChild.top
            dy *= -1
        }

        val step = if (dy >= 0) 20 else -20
        GlobalScope.launch(Dispatchers.Main) {
            while (abs(dy) > 0) {
                delay(1)
                val targetDy = if (step > 0) min(dy, step) else max(dy, step)
                val actualDy = scrollVerticallyBy(targetDy, recycler, state)
                if (actualDy == 0) break
                dy += -actualDy
            }
        }
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    // when font changed
    override fun onItemsChanged(recyclerView: RecyclerView) {
        Timber.d("onItemsChanged")
        recycler?.let { detachAndScrapAttachedViews(it) }
    }

    override fun onItemsUpdated(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        Timber.d("onItemsUpdated")
        recycler?.let { removeAndRecycleAllViews(it) }
    }

    // when changing order
    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        Timber.d("onItemsMoved")
        currentPosition = 0
        recycler?.let { removeAndRecycleAllViews(it) }
    }

    fun getCurrentView(): View? {
        return getChildAt(childCount - 1)
    }
}