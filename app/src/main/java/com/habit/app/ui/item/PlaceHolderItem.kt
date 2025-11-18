package com.habit.app.ui.item

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.habit.app.R
import com.habit.app.databinding.LayoutItemPlaceHolderBinding
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class PlaceHolderItem(
    private val height: Float
) : AbstractFlexibleItem<PlaceHolderItem.ViewHolder>() {

    var mCallback: (() -> Unit)? = null
    class ViewHolder(val binding: LayoutItemPlaceHolderBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemPlaceHolderBinding.bind(view), adapter)
    }

    override fun onViewAttached(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
        holder: ViewHolder,
        position: Int
    ) {
        super.onViewAttached(adapter, holder, position)
        val layoutParams = holder.itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams
        layoutParams?.isFullSpan = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        val params = holder.binding.holderView.layoutParams as FrameLayout.LayoutParams
        params.topMargin = EMUtil.dp2px(height).toInt()

        holder.binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mCallback?.invoke()
            }
            true
        }
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_place_holder
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}