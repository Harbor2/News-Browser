package com.habit.app.ui.item

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.databinding.LayoutItemHomeSearchBinding
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class HomeSearchItem(
    private val context: Context
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
        EMManager.from(holder.binding.containerArea)
            .setCorner(21f)
            .setBorderWidth(1f)
            .setBorderColor(R.color.text_main_color_30)
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
}