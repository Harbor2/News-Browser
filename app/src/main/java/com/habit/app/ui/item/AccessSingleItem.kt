package com.habit.app.ui.item

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.habit.app.R
import com.habit.app.databinding.LayoutItemAccessSingleBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.data.model.AccessSingleData
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class AccessSingleItem(
    private val context: Context,
    val data: AccessSingleData
) : AbstractFlexibleItem<AccessSingleItem.ViewHolder>() {

    var mCallback: SingleItemCallback? = null

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
        // 兼容本地资源icon
        if (data.iconResName.startsWith("iv_")) {
            holder.binding.ivIcon.setImageResource(ThemeManager.getSkinImageResId(UtilHelper.getResIdByName(context, data.iconResName)))
        } else {
            // 本地资源
            Glide.with(context)
                .load(data.iconResName)
                .error(ThemeManager.getSkinImageResId(R.drawable.iv_web_icon_default))
                .into(holder.binding.ivIcon)
        }
        holder.binding.tvName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_80))
        holder.binding.tvName.text = data.name
        holder.binding.ivDelete.isVisible = !data.isSpecial && data.isEdit

        holder.itemView.setOnLongClickListener {
            if (!data.isEdit) {
                mCallback?.onEnterEdit()
            }
            val pos = holder.bindingAdapterPosition
            if (pos < 3 || pos == adapter.itemCount - 1) return@setOnLongClickListener false
            (adapter as? FlexibleAdapterWithDrag)?.mItemTouchHelper?.startDrag(holder)
            true
        }

        holder.itemView.setOnClickListener {
            if (data.isEdit) {
                UtilHelper.showToast(context, context.getString(R.string.toast_exit_edit_mode_first))
                return@setOnClickListener
            }
            mCallback?.onItemClick(this)
        }
        // 删除
        holder.binding.ivDelete.setOnClickListener {
            mCallback?.onItemDelete(this)
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

    interface SingleItemCallback {
        fun onEnterEdit()
        fun onItemClick(item: AccessSingleItem)

        fun onItemDelete(item: AccessSingleItem)
    }
}