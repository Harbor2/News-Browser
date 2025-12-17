package com.habit.app.ui.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.habit.app.R
import com.habit.app.databinding.LayoutTagTabItemBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil

class TagTabItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    val binding: LayoutTagTabItemBinding
    private var mCount: Int = 0
    private var mSelect: Boolean = false

    init {
        binding = LayoutTagTabItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.TagTabItem) {
            val title = getString(R.styleable.TagTabItem_ttiTitle) ?: ""
            mCount = getResourceId(R.styleable.TagTabItem_ttiCount, -1)
            mSelect = getBoolean(R.styleable.TagTabItem_ttiSelect, false)


            EMManager.from(binding.tvCount)
                .setCorner(4f)
                .setBorderWidth(1.5f)
                .setBorderColor(R.color.btn_color_20)

            binding.tvTitle.text = title

            updateCount(mCount)
            updateSelect(mSelect)
        }
    }

    fun updateCount(count: Int) {
        mCount = count
        binding.tvCount.text = count.toString()
        binding.tvCount.isVisible = mSelect && count > 0
    }

    fun updateSelect(isSelect: Boolean) {
        mSelect = isSelect
        binding.tvCount.isVisible = isSelect && mCount > 0
        if (!isSelect) {
            binding.tvTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_70))
        } else {
            binding.tvTitle.setTextColor(EMUtil.getColor(R.color.text_main_color))
        }

        EMManager.from(binding.root)
            .setCorner(16f)
            .setBackGroundRealColor(if (isSelect) Color.WHITE else Color.TRANSPARENT)
    }
}