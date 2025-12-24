package com.habit.app.ui.item

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.data.model.FolderData
import com.habit.app.databinding.LayoutItemBookmarkFolderBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class BookmarkFolderItem(
    private val context: Context,
    var folderData: FolderData,
    val mCallback: FolderCallback
) : AbstractFlexibleItem<BookmarkFolderItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemBookmarkFolderBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemBookmarkFolderBinding.bind(view), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        EMManager.from(holder.binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(if (folderData.mSelect == true) R.color.view_bg_color else R.color.transparent))
        holder.binding.ivMenu.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_b_folder_menu))
        holder.binding.tvFolderName.text = folderData.folderName
        holder.binding.tvFolderName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        if (folderData.mSelect == null) {
            holder.binding.ivMenu.visibility = View.VISIBLE
            holder.binding.ivCheckbox.visibility = View.GONE
        } else {
            holder.binding.ivMenu.visibility = View.GONE
            holder.binding.ivCheckbox.setImageResource(
                ThemeManager.getSkinImageResId(
                    if (folderData.mSelect == true) R.drawable.iv_checkbox_select
                    else R.drawable.iv_checkbox_unselect
                )
            )
            holder.binding.ivCheckbox.visibility = View.VISIBLE
        }

        holder.binding.root.setOnClickListener {
            if (folderData.mSelect == null) {
                mCallback.onFolderClick(this)
            } else {
                folderData.mSelect = !folderData.mSelect!!
                holder.binding.ivCheckbox.setImageResource(
                    ThemeManager.getSkinImageResId(
                        if (folderData.mSelect == true) R.drawable.iv_checkbox_select
                        else R.drawable.iv_checkbox_unselect
                    )
                )
                EMManager.from(holder.binding.root)
                    .setBackGroundRealColor(
                        ThemeManager.getSkinColor(
                            if (folderData.mSelect == true) R.color.view_bg_color
                        else R.color.transparent)
                    )
                mCallback.onFolderSelect(this)
            }
        }
        holder.binding.ivMenu.setOnClickListener {
            mCallback.onFolderMenu(holder.binding.root, this)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_bookmark_folder
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    interface FolderCallback {
        fun onFolderSelect(item: BookmarkFolderItem)
        fun onFolderClick(item: BookmarkFolderItem)
        fun onFolderMenu(anchorView: View, item: BookmarkFolderItem)
    }
}