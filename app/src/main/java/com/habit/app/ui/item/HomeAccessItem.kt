package com.habit.app.ui.item

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.databinding.LayoutItemHomeAccessBinding
import com.habit.app.helper.GsonUtil
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.model.AccessSingleData
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class HomeAccessItem(
    private val context: Context,
    var accessList: ArrayList<AccessSingleData>,
    private val callback: HomeAccessItemCallback
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
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.save_border_color))
            .setTextRealColor(ThemeManager.getSkinColor(R.color.save_text_color))
        holder.binding.btnSave.isVisible = accessList.firstOrNull()?.isEdit ?: false

        updateRecList(holder.binding.recList, holder.binding.btnSave)

        holder.binding.btnSave.setOnClickListener {
            accessList.map {
                it.isEdit = false
                it.sortIndex = accessList.indexOf(it)
            }
            val adapter = holder.binding.recList.adapter
            if (adapter is FlexibleAdapterWithDrag) {
                holder.binding.btnSave.isVisible = false
                adapter.updateDataSet(adapter.currentItems)
            }
            // 保存排序结果
            KeyValueManager.saveValueWithKey(KeyValueManager.KEY_HOME_ACCESS_INFO, GsonUtil.gson.toJson(accessList))
        }
    }

    private fun updateRecList(recList: RecyclerView, btnSave: View) {
        val items = ArrayList<IFlexible<*>>()
        val mAdapter = FlexibleAdapterWithDrag(items) { adapter->
            adapter.currentItems.map {
                val item = it as AccessSingleItem
                item.data.sortIndex = adapter.currentItems.indexOf(item)
            }
            accessList.sortBy { it.sortIndex }
        }

        val singleItemCallback = object : AccessSingleItem.SingleItemCallback {
            override fun onEnterEdit() {
                btnSave.isVisible = true
                accessList.map { it.isEdit = true }
                mAdapter.updateDataSet(items)
            }

            override fun onItemDelete(item: AccessSingleItem) {
                val index = accessList.indexOf(item.data)
                if (index != -1) {
                    mAdapter.removeItem(index)
                    accessList.removeAt(index)
                }
            }

            override fun onItemClick(item: AccessSingleItem) {
                callback.onAccessOpen(item.data)
            }
        }

        with(recList) {
            setHasFixedSize(true)
            this.adapter = mAdapter
            animation = null
            layoutManager = GridLayoutManager(context, 5)
        }
        accessList.forEach {
            items.add(AccessSingleItem(context, it).apply {
                this.mCallback = singleItemCallback
            })
        }
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

    interface HomeAccessItemCallback {
        fun onAccessOpen(data: AccessSingleData)
    }
}