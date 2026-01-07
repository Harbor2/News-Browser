package com.habit.app.ui.item

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.habit.app.R
import com.habit.app.data.model.WebViewData
import com.habit.app.databinding.LayoutItemTagSnapBinding
import com.habit.app.helper.ThemeManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder

class TagSnapItem(
    val context: Context,
    val snapData: WebViewData,
    val callback: TagSnapItemCallback? = null
): AbstractFlexibleItem<TagSnapItem.ViewHolder>() {
    class ViewHolder(val binding: LayoutItemTagSnapBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<*>?>
    ): ViewHolder {
        return ViewHolder(LayoutItemTagSnapBinding.bind(view), adapter)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<*>?>,
        holder: ViewHolder,
        position: Int,
        payloads: List<Any?>?
    ) {
        holder.binding.containerCard.setBackgroundColor(ThemeManager.getSkinColor(R.color.view_bg_color))

        Glide.with(context)
            .load(snapData.getWebIconBitmap(context))
            .error(ThemeManager.getSkinImageResId(R.drawable.iv_web_icon_default))
            .into(holder.binding.ivWebIcon)

        holder.binding.tvWebTitle.text = snapData.name.ifEmpty { snapData.url }
        holder.binding.tvWebTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        holder.binding.btnClose.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_web_snap_close))

        Glide.with(context)
            .load(snapData.getCoverBitmap(context))
            .error(ThemeManager.getSkinImageResId(R.drawable.iv_snap_icon_default))
            .into(holder.binding.ivWebCover)

        holder.binding.root.setOnClickListener {
            callback?.onItemClick(this)
        }
        holder.binding.btnClose.setOnClickListener {
            callback?.onItemClose(this)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_tag_snap
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    interface TagSnapItemCallback {
        fun onItemClose(item: TagSnapItem)
        fun onItemClick(item: TagSnapItem)
    }
}