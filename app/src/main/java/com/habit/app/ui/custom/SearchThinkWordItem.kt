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
    private var mKeyWord: String = ""
    private var mWord: String = ""
    init {
        binding = LayoutSearchThinkWordItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.SearchThinkWordItem) {
            updateThemeUI()
        }
    }

    fun updateKeyWord(key: String, word: String) {
        this.mKeyWord = key
        this.mWord = word

        updateThemeUI()
    }

    fun updateThemeUI() {
        binding.ivSearchIcon.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_think_word_search_icon))
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