package com.habit.app.ui.item

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible

class FlexibleAdapterWithDrag(
    items: List<IFlexible<*>> = mutableListOf(),
    private val callback: (FlexibleAdapterWithDrag) -> Unit = { }
) : FlexibleAdapter<IFlexible<*>>(items)  {

    var mItemTouchHelper: ItemTouchHelper? = null
        private set

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        // 确保只 attach 一次
        if (mItemTouchHelper == null) {
            val callback = FlexibleItemTouchHelperCallback(this)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper!!.attachToRecyclerView(recyclerView)
        }

        // 启用长按拖拽
        setLongPressDragEnabled(false)

        // FlexibleAdapter 内部拖动事件监听
        addListener(object : OnItemMoveListener {
            override fun onActionStateChanged(vh: RecyclerView.ViewHolder?, state: Int) {}

            override fun shouldMoveItem(fromPosition: Int, toPosition: Int): Boolean {
                if (fromPosition < 3 || toPosition < 3 || toPosition == itemCount - 1) return false
                return true
            }

            // 拖动回调
            override fun onItemMove(fromPosition: Int, toPosition: Int) {}
        })
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return super.onItemMove(fromPosition, toPosition)
    }

    fun onEndDrag() {
        callback.invoke(this)
    }
}