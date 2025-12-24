package com.habit.app.ui.item

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.data.model.FolderData
import com.habit.app.databinding.LayoutItemBookmarkFolderSelectBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class BookmarkFolderSelectItem(
    private val context: Context,
    var folderData: FolderData,
    val mCallback: FolderCallback
) : AbstractFlexibleItem<BookmarkFolderSelectItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemBookmarkFolderSelectBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemBookmarkFolderSelectBinding.bind(view), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        EMManager.from(holder.binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(if (folderData.mSelect == true) R.color.view_bg_color else R.color.transparent))
        holder.binding.ivMenu.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_b_folder_arrow))
        holder.binding.tvFolderName.text = folderData.folderName
        holder.binding.tvFolderName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))

        holder.binding.root.setOnClickListener {
            mCallback.onFolderClick(this)
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
        fun onFolderClick(item: BookmarkFolderSelectItem)
    }
}