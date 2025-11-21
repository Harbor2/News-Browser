package com.habit.app.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.habit.app.ui.base.BaseFragment
import com.habit.app.R
import com.habit.app.databinding.FragmentHomeBinding
import com.habit.app.event.EngineChangedEvent
import com.habit.app.helper.GsonUtil
import com.habit.app.helper.KeyValueManager
import com.habit.app.model.TAG
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.model.AccessSingleData
import com.habit.app.model.HomeNewsData
import com.habit.app.ui.AccessSelectActivity
import com.habit.app.ui.SearchActivity
import com.habit.app.ui.dialog.SearchEngineDialog
import com.habit.app.ui.item.HomeAccessItem
import com.habit.app.ui.item.HomeNewsCardItem
import com.habit.app.ui.item.HomeNewsHeadItem
import com.habit.app.ui.item.HomeSearchItem
import com.wyz.emlibrary.em.Direction
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe

class HomeFragment() : BaseFragment<FragmentHomeBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)
    private val traceObserver = MutableLiveData(false)
    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)

    private var accessList = ArrayList<AccessSingleData>()
    private var newsList = ArrayList<HomeNewsData>()

    /**
     * 新闻列表是否正在加载中
     */
    private var isListLoading = false
    private var curPage = 1

    /**
     * 添加access item回调
     */
    private val accessAddLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            accessList = getAccessList()
            mAdapter.currentItems.filterIsInstance<HomeAccessItem>().firstOrNull()?.let {
                it.accessList = accessList
                mAdapter.updateItem(it)
            }
        }
    }

    /**
     * 搜索item回调
     */
    private val searchItemCallback = object : HomeSearchItem.HomeSearchItemCallback {
        override fun onSearch() {
            SearchActivity.startActivity(requireContext())
        }

        override fun onEngineSelect() {
            showEngineSelectDialog()
        }

        override fun onMicrophoneSelect() {

        }

        override fun onScanSelect() {

        }
    }

    /**
     * access item 回调
     */
    private val accessItemCallback = object : HomeAccessItem.HomeAccessItemCallback {
        override fun onAccessOpen(data: AccessSingleData) {
            processAccessClick(data)
        }
    }

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
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
        binding.recList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && !isListLoading) {
                    // 划到最底部且不是加载状态
                    loadMoreData()
                }
            }
        })
    }

    private fun updateList() {
        val items = ArrayList<AbstractFlexibleItem<*>>()

        items.add(getHomeSearchItem())
        items.add(getHomeAccessItem())

        items.add(HomeNewsHeadItem())
        newsList.forEach { newsData ->
            items.add(HomeNewsCardItem(requireContext(), newsData))
        }

        mAdapter.updateDataSet(items)
    }

    private fun getAccessList(): ArrayList<AccessSingleData> {
        val cacheAccessStr = KeyValueManager.getValueByKey(KeyValueManager.KEY_HOME_ACCESS_INFO) ?: ""
        return if (cacheAccessStr.isEmpty()) {
            UtilHelper.getDefaultHomeAccessList(requireContext())
        } else {
            GsonUtil.gson.fromJson(cacheAccessStr, object : TypeToken<ArrayList<AccessSingleData>>() {}.type)
        }
    }

    private fun getNewsList(): ArrayList<HomeNewsData> {
        return arrayListOf(
            HomeNewsData(newsTitle = "第1项数据"),
            HomeNewsData(newsTitle = "第2项数据"),
            HomeNewsData(newsTitle = "第3项数据"),
            HomeNewsData(newsTitle = "第4项数据"),
            HomeNewsData(newsTitle = "第5项数据"),
            HomeNewsData(newsTitle = "第6项数据"),
            HomeNewsData(newsTitle = "第7项数据"),
            HomeNewsData(newsTitle = "第8项数据"),
            HomeNewsData(newsTitle = "第9项数据"),
            HomeNewsData(newsTitle = "第10项数据")
        )
    }

    private fun refreshData() {
        curPage = 1
        isListLoading = true
        mScope.launch(Dispatchers.IO) {
            delay(2000)
            newsList = getNewsList()
            withContext(Dispatchers.Main) {
                isListLoading = false
                binding.swipeRefresh.isRefreshing = false
                updateList()
            }
        }
    }

    private fun loadMoreData() {
        curPage++
        isListLoading = true
        loadingObserver.value = true
        mScope.launch(Dispatchers.IO) {
            delay(1500)
            val moreList = getNewsList()
            newsList.addAll(moreList)
            withContext(Dispatchers.Main) {
                loadingObserver.value = false
                isListLoading = false
                updateList()
            }
        }
    }

    private fun showEngineSelectDialog() {
        SearchEngineDialog.tryShowDialog(requireActivity())
    }

    /**
     * 处理 access item点击
     */
    private fun processAccessClick(data: AccessSingleData) {
        if (data.isSpecial) {
            when (data.iconResName) {
                "iv_access_single_file" -> {}
                "iv_access_single_game" -> {}
                "iv_access_single_bookmark" -> {}
                "iv_access_single_add" -> {
                    accessAddLauncher.launch(Intent(requireContext(), AccessSelectActivity::class.java))
                }
            }
        } else {
            // 打开webview
        }
    }

    private fun getHomeSearchItem(): HomeSearchItem {
        val existItem = mAdapter.currentItems.filterIsInstance<HomeSearchItem>().firstOrNull()
        return existItem ?: HomeSearchItem(requireContext(), searchItemCallback)
    }

    private fun getHomeAccessItem(): HomeAccessItem {
        val existItem = mAdapter.currentItems.filterIsInstance<HomeAccessItem>().firstOrNull()
        return existItem ?: HomeAccessItem(requireContext(), ArrayList(accessList.sortedBy { it.sortIndex }), accessItemCallback)
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        Log.d(TAG, "fragment中 onThemeChanged")
        updateUIConfig()
    }

    @Subscribe
    fun onEngineChangedEvent(event: EngineChangedEvent) {
        mAdapter.currentItems.filterIsInstance<HomeSearchItem>().firstOrNull()?.let {
            mAdapter.updateItem(it)
        }
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }
}