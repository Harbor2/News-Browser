package com.habit.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.habit.app.R
import com.habit.app.databinding.LayoutAccessSelectItemBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.data.model.AccessSingleData
import com.wyz.emlibrary.em.EMManager

class AccessSelectItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutAccessSelectItemBinding
    private var isSelect: Boolean = false
    var accessTag: String = ""
        private set

    init {
        binding = LayoutAccessSelectItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.AccessSelectItem) {
            val title = getString(R.styleable.AccessSelectItem_asiTitle) ?: ""
            val icon = getResourceId(R.styleable.AccessSelectItem_asiIcon, -1)
            isSelect = getBoolean(R.styleable.AccessSelectItem_asiSelect, false)
            accessTag = tag as? String ?: ""

            if (icon != -1) {
                binding.ivIcon.setImageResource(icon)
            }
            binding.tvTitle.text = title
            updateThemeUI()
        }
    }

    fun updateThemeUI() {
        EMManager.from(binding.ivSelectBg)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        binding.ivSelect.setImageResource(ThemeManager.getSkinImageResId(if (isSelect) R.drawable.iv_subtract else R.drawable.iv_add))
        EMManager.from(binding.tvTitle).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
    }

    fun setSelect(select: Boolean) {
        isSelect = select
        binding.ivSelect.setImageResource(ThemeManager.getSkinImageResId(if (select) R.drawable.iv_subtract else R.drawable.iv_add))
        EMManager.from(binding.ivSelectBg)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
    }

    fun getBindData(): AccessSingleData? {
        return getTag(R.id.tag_access_data) as? AccessSingleData
    }
}