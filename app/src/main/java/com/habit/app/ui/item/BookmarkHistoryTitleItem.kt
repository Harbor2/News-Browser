package com.habit.app.ui.item

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.databinding.LayoutItemBookmarkHistoryTitleBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class BookmarkHistoryTitleItem(
    val timeStr: String
) : AbstractFlexibleItem<BookmarkHistoryTitleItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemBookmarkHistoryTitleBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemBookmarkHistoryTitleBinding.bind(view), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        holder.binding.tvTitle.text = if (timeStr == UtilHelper.getTodayDate()) {
            "Today"
        } else {
            timeStr
        }
        holder.binding.tvTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_70))
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_bookmark_history_title
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BookmarkHistoryTitleItem

        return timeStr == other.timeStr
    }

    override fun hashCode(): Int {
        return timeStr.hashCode()
    }
}