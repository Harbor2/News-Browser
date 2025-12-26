package com.habit.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.habit.app.R
import com.habit.app.databinding.LayoutDeleteDataItemBinding
import com.habit.app.helper.ThemeManager

class DeleteDataItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    val binding: LayoutDeleteDataItemBinding
    private var mTitle: String = ""
    private var mSelect: Boolean? = null

    init {
        binding = LayoutDeleteDataItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.DeleteDataItem) {
            mTitle = getString(R.styleable.DeleteDataItem_ddiTitle) ?: ""
            mSelect = getBoolean(R.styleable.DeleteDataItem_ddiSelect, true)
            binding.tvTitle.text = mTitle
            updateThemeUi()
        }

        binding.root.setOnClickListener {
            mSelect = !mSelect!!
            updateThemeUi()
        }
    }

    fun updateThemeUi() {
        binding.tvTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.ivCheckbox.setImageResource(ThemeManager.getSkinImageResId(
            if (mSelect == true) R.drawable.iv_checkbox_select else R.drawable.iv_checkbox_unselect))
    }

    fun getSelectStatus() : Boolean {
        return mSelect!!
    }
}