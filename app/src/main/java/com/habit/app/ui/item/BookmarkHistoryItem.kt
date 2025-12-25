package com.habit.app.ui.item

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.habit.app.R
import com.habit.app.data.model.HistoryData
import com.habit.app.databinding.LayoutItemBookmarkHistoryBinding
import com.habit.app.helper.ThemeManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class BookmarkHistoryItem(
    private val context: Context,
    var data: HistoryData,
    val mCallback: HistoryCallback
) : AbstractFlexibleItem<BookmarkHistoryItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemBookmarkHistoryBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemBookmarkHistoryBinding.bind(view), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        Glide.with(context)
            .load(data.getIconBitmap(context))
            .error(ThemeManager.getSkinImageResId(R.drawable.iv_web_icon_default))
            .into(holder.binding.ivIcon)
        holder.binding.tvWebTitle.text = data.name
        holder.binding.tvWebUrl.text = data.url
        holder.binding.ivMenu.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_b_folder_menu))
        holder.binding.tvWebTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        holder.binding.tvWebUrl.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
        holder.binding.root.setOnClickListener {
            mCallback.onHistoryClick(this)
        }
        holder.binding.ivMenu.setOnClickListener {
            mCallback.onHistoryMenu(holder.binding.root, this)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_bookmark_history
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    interface HistoryCallback {
        fun onHistoryClick(item: BookmarkHistoryItem)
        fun onHistoryMenu(anchorView: View, item: BookmarkHistoryItem)
    }
}