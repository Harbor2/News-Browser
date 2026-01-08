package com.habit.app.ui.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.data.db.DBManager
import com.habit.app.databinding.FragmentPrivacyBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.WebViewManager
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.item.OverFlyingLayoutManager
import com.habit.app.ui.item.TagSnapItem
import com.habit.app.viewmodel.tag.TagsViewModel
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class PrivacyFragment() : BaseFragment<FragmentPrivacyBinding>() {

    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
    private val loadingObserver = MutableLiveData(false)
    private val emptyObserver = MutableLiveData(false)

    private lateinit var overlayLayoutManager: OverFlyingLayoutManager
    private val tagsModel: TagsViewModel by activityViewModels()

    private val snapItemCallback = object : TagSnapItem.TagSnapItemCallback {
        override fun onItemClick(item: TagSnapItem) {
            tagsModel.setSnapSelect(item.snapData)
        }

        override fun onItemClose(item: TagSnapItem) {
            deleteSnapItem(item)
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
        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }
        emptyObserver.observe(requireActivity()) { value ->
            binding.viewEmpty.visibility = if (value) View.VISIBLE else View.GONE
        }
    }

    private fun initData() {
        updateSnapList()
    }

    private fun initListener() {
    }

    private fun deleteSnapItem(item: TagSnapItem) {
        mAdapter.removeItem(mAdapter.currentItems.indexOf(item))
        DBManager.getDao().deleteWebSnapFromTable(item.snapData)
        WebViewManager.releaseWebView(item.snapData.sign)
        tagsModel.setPrivacyTagCount(mAdapter.currentItems.size)

        if (mAdapter.currentItems.filterIsInstance<TagSnapItem>().isEmpty()) {
            emptyObserver.value = true
        }
    }

    /**
     * 删除所有的snap
     */
    fun deleteSnapDataAndCheckEmpty() {
        loadingObserver.value = true
        lifecycleScope.launch(Dispatchers.IO) {
            val destroySignData = mAdapter.currentItems.filterIsInstance<TagSnapItem>().map { it.snapData }
            DBManager.getDao().deleteWebSnapsFromTable(destroySignData)
            withContext(Dispatchers.Main) {
                WebViewManager.releaseWebView(destroySignData.map { it.sign })
                mAdapter.clear()
                loadingObserver.value = false
                emptyObserver.value = true
                tagsModel.setPrivacyTagCount(0)
            }
        }
    }

    private fun updateSnapList() {
        val webSnaps = DBManager.getDao().getWebSnapsFromTable().filter { it.isPrivacyMode == true }
        val items = ArrayList<AbstractFlexibleItem<*>>()

        webSnaps.reversed().forEach {
            items.add(TagSnapItem(requireContext(), it, snapItemCallback))
        }
        if (items.isEmpty()) {
            emptyObserver.value = true
        } else {
            emptyObserver.value = false
            mAdapter.updateDataSet(items)
            binding.recList.post {
                overlayLayoutManager.scrollToPositionWithOffsetInternal((items.size - 2).coerceAtLeast(0), -400)
            }
        }
        tagsModel.setPrivacyTagCount(items.size)
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        mAdapter.updateDataSet(mAdapter.currentItems)
        binding.cardView.setCardBackgroundColor(ThemeManager.getSkinColor(R.color.view_bg_color))
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}