package com.habit.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.webkit.WebView

class CustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): WebView(context, attrs, defStyleAttr) {

    private val gestureDetector: GestureDetector
    var mScrollBack: ((Boolean) -> Unit)? = null
    private var mLastUpScroll: Boolean? = null

    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                val isUpScroll = distanceY > 0
                if (mLastUpScroll!= isUpScroll) {
                    mLastUpScroll = isUpScroll
                    mLastUpScroll?.let {
                        mScrollBack?.invoke(it)
                    }
                }
                return false
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
}