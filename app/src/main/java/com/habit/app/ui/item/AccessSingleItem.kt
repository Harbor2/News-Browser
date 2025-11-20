package com.habit.app.ui.item

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.databinding.LayoutItemAccessSingleBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.model.AccessSingleData
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class AccessSingleItem(
    private val context: Context,
    private val data: AccessSingleData
) : AbstractFlexibleItem<AccessSingleItem.ViewHolder>() {
    class ViewHolder(val binding: LayoutItemAccessSingleBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemAccessSingleBinding.bind(view), adapter)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        EMManager.from(holder.binding.ivIconBg)
            .setCorner(24f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        holder.binding.ivIcon.setImageResource(ThemeManager.getSkinImageResId(data.iconRes))
        holder.binding.tvName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_80))
        holder.binding.tvName.text = data.name

        holder.itemView.setOnLongClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos < 3 || pos == adapter.itemCount - 1) return@setOnLongClickListener false
            (adapter as? FlexibleAdapterWithDrag)?.mItemTouchHelper?.startDrag(holder)
            true
        }
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_access_single
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}