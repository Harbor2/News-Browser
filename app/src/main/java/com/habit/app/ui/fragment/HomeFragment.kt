package com.habit.app.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.habit.app.ui.base.BaseFragment
import com.habit.app.R
import com.habit.app.databinding.FragmentHomeBinding
import com.habit.app.model.TAG
import com.habit.app.helper.ThemeManager
import com.habit.app.model.AccessSingleData
import com.habit.app.model.HomeNewsData
import com.habit.app.ui.item.HomeAccessItem
import com.habit.app.ui.item.HomeNewsCardItem
import com.habit.app.ui.item.HomeNewsHeadItem
import com.habit.app.ui.item.HomeSearchItem
import com.wyz.emlibrary.em.Direction
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class HomeFragment() : BaseFragment<FragmentHomeBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)
    private val traceObserver = MutableLiveData(false)
    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)

    private var accessList = listOf<AccessSingleData>()
    private var newsList = listOf<HomeNewsData>()

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (binding.containerNavi.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = EMUtil.getStatusBarHeight(requireContext())
        }

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUIConfig()

        traceObserver.observe(requireActivity()) { value ->
            binding.btnNaviTrace.setImageResource(
                ThemeManager.getSkinImageResId(if (value) R.drawable.iv_search_trace else R.drawable.iv_search_untrace)
            )
        }
        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }

        with(binding.recList) {
            setHasFixedSize(true)
            adapter = mAdapter
            animation = null
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun updateUIConfig() {
        binding.btnNaviTrace.setImageResource(
            ThemeManager.getSkinImageResId(if (traceObserver.value!!) R.drawable.iv_search_trace else R.drawable.iv_search_untrace)
        )
        binding.root.setBackgroundColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.bgTop)
            .setGradientRealColor(
                intArrayOf(
                    ThemeManager.getSkinColor(R.color.home_top_bg_start),
                    ThemeManager.getSkinColor(R.color.home_top_bg_end)
                ), Direction.TOP
            )
        mAdapter.currentItems.forEach { item ->
            mAdapter.updateItem(item, "update")
        }
    }

    private fun initData() {
        accessList = getAccessList()
        newsList = getNewsList()
        updateList()
    }

    private fun initListener() {
        binding.loadingView.setOnClickListener {}
    }

    private fun updateList() {
        val items = ArrayList<AbstractFlexibleItem<*>>()

        items.add(HomeSearchItem(requireContext()))
        items.add(HomeAccessItem(requireContext(), accessList))
        items.add(HomeNewsHeadItem())
        newsList.forEach { newsData ->
            items.add(HomeNewsCardItem(requireContext(), newsData))
        }
        mAdapter.updateDataSet(items)
    }

    private fun getAccessList(): List<AccessSingleData> {
        return listOf(
            AccessSingleData(R.drawable.iv_access_single_file, getString(R.string.text_file)),
            AccessSingleData(R.drawable.iv_access_single_game, getString(R.string.text_game)),
            AccessSingleData(R.drawable.iv_access_single_bookmark, getString(R.string.text_bookmark)),
            AccessSingleData(R.drawable.iv_access_single_instagram, getString(R.string.text_instagram)),
            AccessSingleData(R.drawable.iv_access_single_tiktok, getString(R.string.text_tiktok)),
            AccessSingleData(R.drawable.iv_access_single_youtube, getString(R.string.text_youtube)),
            AccessSingleData(R.drawable.iv_access_single_twitter, getString(R.string.text_twitter)),
            AccessSingleData(R.drawable.iv_access_single_facebook, getString(R.string.text_facebook)),
            AccessSingleData(R.drawable.iv_access_single_amazon, getString(R.string.text_amazon)),
            AccessSingleData(R.drawable.iv_access_single_add, getString(R.string.text_add))
        )
    }

    private fun getNewsList(): List<HomeNewsData> {
        return listOf(
            HomeNewsData(),
            HomeNewsData(),
            HomeNewsData(),
            HomeNewsData(),
            HomeNewsData(),
            HomeNewsData(),
            HomeNewsData(),
            HomeNewsData(),
            HomeNewsData(),
            HomeNewsData(),
            HomeNewsData()
        )
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        Log.d(TAG, "fragment中 onThemeChanged")
        updateUIConfig()
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }
}