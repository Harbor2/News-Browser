package com.habit.app.ui.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habit.app.data.db.DBManager
import com.habit.app.databinding.FragmentPrivacyBinding
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.item.OverFlyingLayoutManager
import com.habit.app.ui.item.TagSnapItem
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem

class PrivacyFragment() : BaseFragment<FragmentPrivacyBinding>() {

    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
    private lateinit var overlayLayoutManager: OverFlyingLayoutManager

    private val snapItemCallback = object : TagSnapItem.TagSnapItemCallback {
        override fun onItemClick(item: TagSnapItem) {

        }

        override fun onItemClose(item: TagSnapItem) {
            mAdapter.removeItem(mAdapter.currentItems.indexOf(item))
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentPrivacyBinding {
        return FragmentPrivacyBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        overlayLayoutManager = OverFlyingLayoutManager(requireContext())

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        with(binding.recList) {
            itemAnimator = null
            adapter = mAdapter
            layoutManager = overlayLayoutManager
        }
    }

    private fun initData() {
        updateSnapList()
    }

    private fun initListener() {
    }

    private fun updateSnapList() {
        val webSnaps = DBManager.getDao().getWebSnapsFromTable()
        val items = ArrayList<AbstractFlexibleItem<*>>()

        webSnaps.forEach {
            items.add(TagSnapItem(requireContext(), it, snapItemCallback))
        }
        mAdapter.updateDataSet(items)
        binding.recList.post {
            overlayLayoutManager.scrollToPositionWithOffsetInternal((items.size - 2).coerceAtLeast(0), -400)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}