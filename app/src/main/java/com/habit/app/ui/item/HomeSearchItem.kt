package com.habit.app.ui.item

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.databinding.LayoutItemHomeSearchBinding
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.model.ENGINE_BAIDU
import com.habit.app.model.ENGINE_BING
import com.habit.app.model.ENGINE_DUCKDUCK
import com.habit.app.model.ENGINE_GOOGLE
import com.habit.app.model.ENGINE_YAHOO
import com.habit.app.model.ENGINE_YANDEX
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class HomeSearchItem(
    private val context: Context,
    private val callback: HomeSearchItemCallback
) : AbstractFlexibleItem<HomeSearchItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemHomeSearchBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemHomeSearchBinding.bind(view), adapter)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        holder.binding.ivSearchSound.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_search_sound))
        holder.binding.ivSearchScan.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_search_scan))
        holder.binding.ivEngineIconArrow.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_engine_select_arrow))
        updateEngineIcon(holder.binding.ivEngineIcon)
        EMManager.from(holder.binding.containerArea)
            .setCorner(21f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_30))

        holder.binding.ivEngineIcon.setOnClickListener {
            callback.onEngineSelect()
        }
        holder.binding.ivSearchSound.setOnClickListener {
            callback.onMicrophoneSelect()
        }
        holder.binding.ivSearchScan.setOnClickListener {
            callback.onScanSelect()
        }
    }

    fun updateEngineIcon(ivEngineIcon: AppCompatImageView) {
        val iconRes = when (KeyValueManager.getValueByKey(KeyValueManager.KEY_ENGINE_SELECT) ?: ENGINE_GOOGLE) {
            ENGINE_GOOGLE -> R.drawable.iv_engine_icon_google
            ENGINE_BING -> R.drawable.iv_engine_icon_bing
            ENGINE_YAHOO -> R.drawable.iv_engine_icon_yahoo
            ENGINE_DUCKDUCK -> R.drawable.iv_engine_icon_duckduck
            ENGINE_YANDEX -> R.drawable.iv_engine_icon_yandex
            ENGINE_BAIDU -> R.drawable.iv_engine_icon_baidu
            else -> R.drawable.iv_engine_icon_google
        }
        ivEngineIcon.setImageResource(iconRes)
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_home_search
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    interface HomeSearchItemCallback {
        fun onEngineSelect()
        fun onMicrophoneSelect()
        fun onScanSelect()
    }
}