package com.habit.app.ui.custom.scan

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.habit.app.databinding.LayoutScanAnimationViewBinding
import com.wyz.emlibrary.util.EMUtil

class CustomScanAnimationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutScanAnimationViewBinding

    private val parentHeight = EMUtil.dp2px(260f)
    private val lineHeight = EMUtil.dp2px(2f)
    init {
        binding = LayoutScanAnimationViewBinding.inflate(LayoutInflater.from(context), this, true)

        binding.ivScanLine.postDelayed({
            startAnimation()
        }, 500)
    }

    private var isPaused = false
    private var currentY = 0f

    private fun startAnimation() {
        binding.ivScanLine.translationY = -lineHeight
        binding.ivScanLine.animate()
            .translationY(parentHeight + lineHeight)
            .setDuration(1500)
            .setInterpolator(LinearInterpolator())
            .withEndAction {
                if (!isPaused) startAnimation() // 无限循环
            }
            .start()
    }

    fun pauseAnimation() {
        if (isPaused) return
        isPaused = true
        binding.ivScanLine.animate().cancel()
        currentY = binding.ivScanLine.translationY
    }

    fun resumeAnimation() {
        if (!isPaused) return
        isPaused = false
        binding.ivScanLine.animate()
            .translationY(parentHeight + lineHeight)
            .setDuration(((parentHeight + lineHeight - currentY) / (parentHeight + lineHeight) * 1500).toLong()) // 计算剩余时间
            .setInterpolator(LinearInterpolator())
            .withEndAction {
                if (!isPaused) startAnimation() // 无限循环
            }
            .start()
    }
}