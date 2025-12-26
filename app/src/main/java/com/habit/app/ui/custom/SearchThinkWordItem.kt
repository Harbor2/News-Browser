package com.habit.app.ui.custom

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.habit.app.R
import com.habit.app.databinding.LayoutSearchThinkWordItemBinding
import com.habit.app.helper.ThemeManager
import kotlin.math.min

class SearchThinkWordItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    val binding: LayoutSearchThinkWordItemBinding
    private var mIcon: Int = -1
    private var mKeyWord: String = "2025发"
    private var mWord: String = "2025发大财身体健康"
    init {
        binding = LayoutSearchThinkWordItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.SearchThinkWordItem) {
            mIcon = getResourceId(R.styleable.SearchThinkWordItem_stwIcon, -1)

            updateThemeUI()
        }
    }

    private fun updateKeyWord(key: String, word: String) {
        this.mKeyWord = key
        this.mWord = word

        updateThemeUI()
    }

    fun updateThemeUI() {
        if (mIcon != -1) {
            binding.ivSearchIcon.setImageResource(ThemeManager.getSkinImageResId(mIcon))
        }
        if (mWord.isNotEmpty()) {
            val spannable = SpannableString(mWord)
            if (mKeyWord.isEmpty()) {
                binding.tvText.text = spannable
                binding.tvText.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
            } else {
                spannable.setSpan(
                    ForegroundColorSpan(ThemeManager.getSkinColor(R.color.text_main_color)),
                    0,
                    min(mKeyWord.length, mWord.length),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tvText.text = spannable
                binding.tvText.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
            }
        }
    }
}