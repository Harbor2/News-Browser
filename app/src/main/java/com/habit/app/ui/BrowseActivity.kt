package com.habit.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.habit.app.ui.base.BaseActivity
import com.habit.app.R
import com.habit.app.databinding.ActivityWebBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.util.immersiveWindow

class BrowseActivity : BaseActivity() {
    private lateinit var binding: ActivityWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, ThemeManager.isNightTheme(), binding.containerNavi)

        binding.webView.setBackgroundColor(getColor(R.color.page_main_color))
        val title = intent?.getStringExtra(EXTRA_KEY_TITLE) ?: ""
        val url = intent?.getStringExtra(EXTRA_KEY_URL) ?: ""

        binding.webView.loadUrl(url)
        binding.titleLabel.text = title

        binding.backView.setOnClickListener {
            finish()
        }
    }

    companion object {
        private const val EXTRA_KEY_TITLE = "EXTRA_KEY_TITLE"
        private const val EXTRA_KEY_URL = "EXTRA_KEY_URL"

        fun startPrivacyPolicy(context: Context) {
            val intent = Intent(context, BrowseActivity::class.java)
            intent.putExtra(EXTRA_KEY_TITLE, context.getString(R.string.privacy_policy))
            intent.putExtra(EXTRA_KEY_URL, "https://sites.google.com/view/habitize/")
            context.startActivity(intent)
        }

        fun startTermOfService(context: Context) {
            val intent = Intent(context, BrowseActivity::class.java)
            intent.putExtra(EXTRA_KEY_TITLE, context.getString(R.string.terms_of_service))
            intent.putExtra(EXTRA_KEY_URL, "https://sites.google.com/view/habitizeterms/")
            context.startActivity(intent)
        }
    }
}