package com.habit.app.ui.item

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.habit.app.R
import com.habit.app.data.model.BookmarkData
import com.habit.app.data.model.FolderData
import com.habit.app.databinding.LayoutItemBookmarkFolderBinding
import com.habit.app.databinding.LayoutItemBookmarkUrlBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.item.BookmarkFolderItem.FolderCallback
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class BookmarkUrlItem(
    private val context: Context,
    var bookmarkData: BookmarkData,
    val mCallback: BookmarkCallback
) : AbstractFlexibleItem<BookmarkUrlItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemBookmarkUrlBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemBookmarkUrlBinding.bind(view), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        Glide.with(context)
            .load(bookmarkData.getIconBitmap(context))
            .error(ThemeManager.getSkinImageResId(R.drawable.iv_web_icon_default))
            .into(holder.binding.ivIcon)
        holder.binding.tvWebTitle.text = bookmarkData.name
        holder.binding.tvWebUrl.text = bookmarkData.url
        EMManager.from(holder.binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(if (bookmarkData.mSelect == true) R.color.view_bg_color else R.color.transparent))
        holder.binding.tvWebTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        holder.binding.tvWebUrl.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
        holder.binding.ivMenu.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_b_folder_menu))
        if (bookmarkData.mSelect == null) {
            holder.binding.ivMenu.visibility = View.VISIBLE
            holder.binding.ivCheckbox.visibility = View.GONE
        } else {
            holder.binding.ivMenu.visibility = View.GONE
            holder.binding.ivCheckbox.setImageResource(
                ThemeManager.getSkinImageResId(
                    if (bookmarkData.mSelect == true) R.drawable.iv_checkbox_select
                    else R.drawable.iv_checkbox_unselect
                )
            )
            holder.binding.ivCheckbox.visibility = View.VISIBLE
        }

        holder.binding.root.setOnClickListener {
            if (bookmarkData.mSelect == null) {
                mCallback.onBookmarkClick(this)
            } else {
                bookmarkData.mSelect = !bookmarkData.mSelect!!
                holder.binding.ivCheckbox.setImageResource(
                    ThemeManager.getSkinImageResId(
                        if (bookmarkData.mSelect == true) R.drawable.iv_checkbox_select
                        else R.drawable.iv_checkbox_unselect
                    )
                )
                EMManager.from(holder.binding.root)
                    .setBackGroundRealColor(
                        ThemeManager.getSkinColor(
                            if (bookmarkData.mSelect == true) R.color.view_bg_color
                            else R.color.transparent)
                    )
                mCallback.onBookmarkSelect(this)
            }
        }
        holder.binding.ivMenu.setOnClickListener {
            mCallback.onBookmarkMenu(holder.binding.root, this)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_bookmark_url
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    interface BookmarkCallback {
        fun onBookmarkSelect(item: BookmarkUrlItem)
        fun onBookmarkClick(item: BookmarkUrlItem)
        fun onBookmarkMenu(anchorView: View, item: BookmarkUrlItem)
    }
}