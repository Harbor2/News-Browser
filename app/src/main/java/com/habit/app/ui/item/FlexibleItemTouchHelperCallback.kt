package com.habit.app.ui.item

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class FlexibleItemTouchHelperCallback(
    private val adapter: FlexibleAdapterWithDrag
): ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, 0)
    }

    override fun isLongPressDragEnabled(): Boolean = false

    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition
        if (from < 3 || to < 3 || to == adapter.itemCount - 1) return false
        return adapter.onItemMove(from, to)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}