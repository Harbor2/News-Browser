package com.habit.app.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.core.view.isVisible
import com.habit.app.R
import com.habit.app.ui.base.BaseActivity
import com.habit.app.databinding.ActivitySplashBinding
import com.habit.app.helper.KeyValueManager
import com.wyz.emlibrary.em.EMManager

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initListener()
    }

    private fun initView() {
        EMManager.from(binding.btnStart)
            .setCorner(12f)
            .setBackGroundColor(R.color.btn_color)

        val isEnteredHome = KeyValueManager.getBooleanValue(KeyValueManager.KEY_ENTERED_HOME, false)
        if (!isEnteredHome) {
            binding.containerGetStarted.isVisible = true
            return
        }
        binding.containerGetStarted.isVisible = false
        ValueAnimator.ofInt(0, 100).apply {
            duration = 3000
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    jumpMainPage()
                }
            })
            start()
        }
    }

    private fun initListener() {
        binding.containerGetStarted.setOnClickListener {
            jumpMainPage()
        }
    }

    private fun jumpMainPage() {
        MainActivity.startActivity(this)
        finish()
    }
}