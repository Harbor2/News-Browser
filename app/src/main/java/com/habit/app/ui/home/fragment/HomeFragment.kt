package com.habit.app.ui.home.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.habit.app.ui.base.BaseFragment
import com.habit.app.R
import com.habit.app.data.NEWS_CATEGORY_WORLD
import com.habit.app.databinding.FragmentHomeBinding
import com.habit.app.event.EngineChangedEvent
import com.habit.app.helper.GsonUtil
import com.habit.app.helper.KeyValueManager
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.data.model.AccessSingleData
import com.habit.app.data.model.RealTimeNewsData
import com.habit.app.data.repority.PullNewsRepository
import com.habit.app.event.HomeAccessUpdateEvent
import com.habit.app.ui.home.AccessSelectActivity
import com.habit.app.ui.home.SearchActivity
import com.habit.app.ui.dialog.SearchEngineDialog
import com.habit.app.ui.home.BookmarkHistoryActivity
import com.habit.app.ui.home.CameraScanActivity
import com.habit.app.ui.home.FileDownloadActivity
import com.habit.app.ui.item.HomeAccessItem
import com.habit.app.ui.item.HomeNewsCardItem
import com.habit.app.ui.item.HomeNewsHeadItem
import com.habit.app.ui.item.HomeSearchItem
import com.habit.app.ui.item.PlaceHolderItem
import com.habit.app.viewmodel.MainActivityModel
import com.habit.app.viewmodel.news.PullNewsModelFactory
import com.habit.app.viewmodel.news.PullNewsViewModel
import com.wyz.emlibrary.em.Direction
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import kotlin.getValue

class HomeFragment() : BaseFragment<FragmentHomeBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)
    private val viewModel: MainActivityModel by activityViewModels()
    private val pullNewsModel: PullNewsViewModel by viewModels {
        PullNewsModelFactory(PullNewsRepository())
    }
    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)

    private var accessList = ArrayList<AccessSingleData>()
    private var newsList = ArrayList<RealTimeNewsData>()

    /**
     * 新闻列表是否正在加载中
     */
    private var isListLoading = false
    private var curPage = 1
    private var hasMore = false

    /**
     * 新闻item点击回调
     */
    private val newsItemCallback: (String) -> Unit = { newsUrl ->
        viewModel.setSearchUrl(newsUrl)
    }

    /**
     * 相机权限
     */
    private val cameraPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if (result) {
            checkAndJumpScanActivity()
        }
    }

    /**
     * 麦克风权限
     */
    private val micPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if (result) {
            checkAndJumpMicSearch()
        }
    }

    private val searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val searchStr = result.data?.getStringExtra("searchStr")
            if (!searchStr.isNullOrEmpty()) {
                viewModel.setKeyWorkSearch(searchStr)
            }
        }
    }

    /**
     * 添加access item回调
     */
    private val accessAddLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            updateAccessList()
        }
    }

    /**
     * 搜索item回调
     */
    private val searchItemCallback = object : HomeSearchItem.HomeSearchItemCallback {
        override fun onSearch() {
            jumpSearchActivity(hasFocus = true, hasMic = false)
        }

        override fun onEngineSelect() {
            showEngineSelectDialog()
        }

        override fun onMicrophoneSelect() {
            checkAndJumpMicSearch()
        }

        override fun onScanSelect() {
            checkAndJumpScanActivity()
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
        setupObserver()
        initData()
        initListener()
    }

    private fun initView() {
        updateUIConfig()
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
            ThemeManager.getSkinImageResId(if (viewModel.privacyObserver.value!!) R.drawable.iv_search_trace else R.drawable.iv_search_untrace)
        )
        binding.root.setBackgroundColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.bgTop)
            .setGradientRealColor(
                intArrayOf(
                    ThemeManager.getSkinColor(R.color.home_top_bg_start),
                    ThemeManager.getSkinColor(R.color.home_top_bg_end)
                ), Direction.TOP
            )
        mAdapter.updateDataSet(mAdapter.currentItems)
    }

    private fun initData() {
        accessList = getAccessList()
        loadingObserver.value = true
        isListLoading = true
        // 拉取最新闻
        pullNewsModel.pullNews(NEWS_CATEGORY_WORLD)
        newsList = getNewsList(curPage)
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
        binding.btnNaviTrace.setOnClickListener {
            viewModel.setPrivacyObserver(!viewModel.privacyObserver.value!!)
        }
    }

    private fun setupObserver() {
        lifecycleScope.launch {
            viewModel.privacyObserver.observe(requireActivity()) { value ->
                binding.btnNaviTrace.setImageResource(
                    ThemeManager.getSkinImageResId(if (value) R.drawable.iv_search_trace else R.drawable.iv_search_untrace)
                )
            }
        }
        lifecycleScope.launch {
            pullNewsModel.pullNewObserver.collect { list ->
                Log.d(TAG, "首页新闻列表返回：$list")
                if (list.isEmpty()) {
                    return@collect
                }
                // 数据库更新
                DBManager.getDao().insertNewsToTable(list)
                // page重置
                curPage = 1
                loadingObserver.value = false
                isListLoading = false
                binding.swipeRefresh.isRefreshing = false
                // 拉取新闻
                newsList = getNewsList(curPage)
                updateList()
            }
        }
    }

    private fun checkAndJumpMicSearch() {
        if (!UtilHelper.hasMicPerm(requireContext())) {
            micPermLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            return
        }
        jumpSearchActivity(hasFocus = false, hasMic = true)
    }

    private fun checkAndJumpScanActivity() {
        if (!UtilHelper.hasCameraPermission(requireContext())) {
            cameraPermLauncher.launch(android.Manifest.permission.CAMERA)
            return
        }
        CameraScanActivity.startActivity(requireActivity())
    }

    private fun jumpSearchActivity(hasFocus: Boolean = false, hasMic: Boolean = false) {
        searchLauncher.launch(Intent(requireContext(), SearchActivity::class.java).apply {
            putExtra("hasFocus", hasFocus)
            putExtra("hasMic", hasMic)
        })
    }

    private fun updateList() {
        val items = ArrayList<AbstractFlexibleItem<*>>()

        items.add(getHomeSearchItem())
        items.add(getHomeAccessItem())

        items.add(HomeNewsHeadItem())
        items.add(PlaceHolderItem(12f))
        newsList.forEach { newsData ->
            items.add(HomeNewsCardItem(requireContext(), newsData, newsItemCallback))
        }

        mAdapter.updateDataSet(items)
    }

    /**
     * 更新access列表
     */
    private fun updateAccessList() {
        accessList = getAccessList()
        mAdapter.currentItems.filterIsInstance<HomeAccessItem>().firstOrNull()?.let {
            it.accessList = accessList
            mAdapter.updateItem(it)
        }
    }

    private fun getAccessList(): ArrayList<AccessSingleData> {
        val cacheAccessStr = KeyValueManager.getValueByKey(KeyValueManager.KEY_HOME_ACCESS_INFO) ?: ""
        return if (cacheAccessStr.isEmpty()) {
            UtilHelper.getDefaultHomeAccessList(requireContext())
        } else {
            GsonUtil.gson.fromJson(cacheAccessStr, object : TypeToken<ArrayList<AccessSingleData>>() {}.type)
        }
    }

    /**
     * 数据库拉取新闻
     */
    private fun getNewsList(page: Int): ArrayList<RealTimeNewsData> {
        val result = DBManager.getDao().getNewsFromTable(NEWS_CATEGORY_WORLD, page, 10)
        hasMore = result.second
        return result.first
    }

    private fun refreshData() {
        curPage = 1
        isListLoading = true
        pullNewsModel.pullNews(NEWS_CATEGORY_WORLD)
    }

    private fun loadMoreData() {
        if (!hasMore) {
            UtilHelper.showToast(requireContext(), getString(R.string.toast_no_more_news))
            return
        }
        lifecycleScope.launch {
            curPage++
            isListLoading = true
            loadingObserver.value = true
            newsList.addAll(getNewsList(curPage))
            delay(800)
            isListLoading = false
            loadingObserver.value = false
            updateList()
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
                "iv_access_single_file" -> {
                    // download
                    FileDownloadActivity.startActivity(requireContext())
                }
                "iv_access_single_game" -> {
                    // history
                    BookmarkHistoryActivity.startActivity(requireContext(), viewModel.privacyObserver.value!!, false)
                }
                "iv_access_single_bookmark" -> {
                    BookmarkHistoryActivity.startActivity(requireContext(), viewModel.privacyObserver.value!!, true)
                }
                "iv_access_single_add" -> {
                    accessAddLauncher.launch(Intent(requireContext(), AccessSelectActivity::class.java))
                }
            }
        } else {
            // 打开webview
            viewModel.setSearchUrl(data.linkUrl)
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
    fun onAccessChangedEvent(event: HomeAccessUpdateEvent) {
        updateAccessList()
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

    interface HomeFragmentCallback {
        fun onSearch(searchStr: String)
    }
}