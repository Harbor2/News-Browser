package com.habit.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.habit.app.R
import com.habit.app.databinding.LayoutBrowserMenuItemBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager

class BrowserMenuItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutBrowserMenuItemBinding
    private var mIcon: Int = -1
    var engineTag: String = ""
        private set

    init {
        binding = LayoutBrowserMenuItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.BrowserMenuItem) {
            val title = getString(R.styleable.BrowserMenuItem_bmiTitle) ?: ""
            mIcon = getResourceId(R.styleable.BrowserMenuItem_bmiIcon, -1)
            engineTag = tag as? String ?: ""

            if (mIcon != -1) {
                binding.ivIcon.setImageResource(ThemeManager.getSkinImageResId(mIcon))
            }
            binding.tvTitle.text = title
            updateThemeUI()
        }
    }

    fun updateData(icon: Int, title: String) {
        mIcon = icon
        binding.ivIcon.setImageResource(ThemeManager.getSkinImageResId(icon))
        binding.tvTitle.text = title
    }

    fun updateThemeUI() {
        if (mIcon != -1) {
            binding.ivIcon.setImageResource(ThemeManager.getSkinImageResId(mIcon))
        }
        EMManager.from(binding.tvTitle).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
    }
}