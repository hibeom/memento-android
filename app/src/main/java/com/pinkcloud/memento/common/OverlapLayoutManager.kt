package com.pinkcloud.memento.common

import android.graphics.PointF
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.min

class OverlapLayoutManager : RecyclerView.LayoutManager(), RecyclerView.SmoothScroller.ScrollVectorProvider {

    var currentPosition: Int = 0

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        Timber.d("onLayoutChildren")
        if (state.itemCount <= 0) return
        var startPosition = 0
        var endPosition = startPosition + 1
        if (childCount > 0) {
            val topChild = getChildAt(childCount - 1)!!
            val topChildPosition = getPosition(topChild)
            currentPosition = topChildPosition
            val bottomChild = getChildAt(0)!! // child at bottom
            val bottomChildPosition = getPosition(bottomChild)
            startPosition = bottomChildPosition + 1
            endPosition = min(topChildPosition + 1, startPosition + 1)
            //        detachAndScrapAttachedViews(recycler)
        }
        for (i in startPosition until state.itemCount) {
            // TODO 정렬 순서가 바뀌거나...삭제, 혹은 notifyDatasetChange 호출 시...detachAndScrap 해주어야?
            // 다른 방법은?
            if (i > endPosition) break

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

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        // when search text changed or complete memo
        Timber.d("onItemsRemoved")
        Timber.d("positionStart:$positionStart") // removed item position
        super.onItemsRemoved(recyclerView, positionStart, itemCount)
    }

    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        // when search text changed or add memo
        super.onItemsAdded(recyclerView, positionStart, itemCount)
    }

    override fun onScrollStateChanged(state: Int) {
        when (state) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                if (childCount == 0) return
//                val topChild = getChildAt(childCount - 1)!!
//                if (abs(topChild.top) > abs(topChild.bottom)) {
//                    topChild.offsetTopAndBottom(-topChild.bottom)
//                } else {
//                    topChild.offsetTopAndBottom(-topChild.top)
//                }
            }
        }
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State?,
        position: Int
    ) {
        val linearSmoothScroller = LinearSmoothScroller(recyclerView.context)
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        Timber.d("currentPosition:$currentPosition")

        if (childCount == 0) return null
        val topChild = getChildAt(childCount - 1)!!
        val height = topChild.measuredHeight
        var y = 0f
        if (targetPosition < currentPosition) {
            Timber.d("1. target < current")
            // 앞(아래)으로 가면 y가 음수
            y = topChild.top.toFloat()
            y += (currentPosition - targetPosition)*height*-1
        } else if (targetPosition > currentPosition) {
            Timber.d("2. target > current")

            // 뒤(위)로 가면 y가 양수
            y = topChild.bottom.toFloat()
            y += (targetPosition - currentPosition - 1)*height
        } else {
            Timber.d("3. target == current")

            // 자기 자신인 경우 위치 교정
            y = topChild.top.toFloat()
        }
        // TODO y 이가 뭘 의미하는지 모르겠다.
        return PointF(0f, y)
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        // when font changed
        Timber.d("onItemsChanged")
        removeAllViews()
        super.onItemsChanged(recyclerView)
    }

    override fun onItemsUpdated(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        Timber.d("onItemsUpdated")
        super.onItemsUpdated(recyclerView, positionStart, itemCount)
    }

    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        // when changing order
        Timber.d("onItemsMoved")
        removeAllViews()
        super.onItemsMoved(recyclerView, from, to, itemCount)
    }
}