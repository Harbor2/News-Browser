package com.habit.app.ui.item

import android.R.attr.animation
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.databinding.LayoutItemHomeAccessBinding
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class HomeAccessItem(
    private val context: Context
) : AbstractFlexibleItem<HomeAccessItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemHomeAccessBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemHomeAccessBinding.bind(view), adapter)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        EMManager.from(holder.binding.btnSave)
            .setCorner(18f)
            .setBorderWidth(1f)
            .setBorderColor(R.color.btn_color_30)

        updateRecList(holder.binding.recList)
    }

    private fun updateRecList(recList: RecyclerView) {
        val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
        with(recList) {
            setHasFixedSize(true)
            this.adapter = mAdapter
            animation = null
            layoutManager = GridLayoutManager(context, 5)
        }
        val items = ArrayList<AbstractFlexibleItem<*>>()
        mAdapter.updateDataSet(items)
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_home_access
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}