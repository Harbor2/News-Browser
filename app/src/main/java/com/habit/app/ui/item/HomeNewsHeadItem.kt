package com.habit.app.ui.item

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.databinding.LayoutItemHomeNewsHeadBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class HomeNewsHeadItem() : AbstractFlexibleItem<HomeNewsHeadItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemHomeNewsHeadBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemHomeNewsHeadBinding.bind(view), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        EMManager.from(holder.binding.tvNews).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(holder.binding.tvNewsMore).setTextRealColor(ThemeManager.getSkinColor(R.color.save_text_color_60))
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_home_news_head
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}