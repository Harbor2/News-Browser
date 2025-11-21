package com.habit.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.habit.app.R
import com.habit.app.databinding.LayoutEngineSelectItemBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager

class EngineSelectItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutEngineSelectItemBinding
    private var isSelect: Boolean = false
    var engineTag: String = ""
        private set

    init {
        binding = LayoutEngineSelectItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.EngineSelectItem) {
            val title = getString(R.styleable.EngineSelectItem_egsTitle) ?: ""
            val icon = getResourceId(R.styleable.EngineSelectItem_egsIcon, -1)
            isSelect = getBoolean(R.styleable.EngineSelectItem_egsSelect, false)
            engineTag = tag as? String ?: ""

            if (icon != -1) {
                binding.ivIcon.setImageResource(icon)
            }
            binding.tvTitle.text = title
            updateThemeUI()
        }
    }

    fun updateThemeUI() {
        EMManager.from(binding.root)
            .setCorner(12f)
            .setBorderWidth(1f)
            .setBorderColor(if (isSelect) R.color.btn_color else R.color.transparent)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.tvTitle).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
    }

    fun setSelect(select: Boolean) {
        isSelect = select
        EMManager.from(binding.root)
            .setCorner(12f)
            .setBorderWidth(1f)
            .setBorderColor(if (isSelect) R.color.btn_color else R.color.transparent)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
    }
}