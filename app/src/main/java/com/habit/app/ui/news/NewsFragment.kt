package com.habit.app.ui.news

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habit.app.R
import com.habit.app.data.NEWS_CATEGORY_BUSINESS
import com.habit.app.data.NEWS_CATEGORY_HEALTH
import com.habit.app.data.NEWS_CATEGORY_POLITICS
import com.habit.app.data.NEWS_CATEGORY_SCIENCE
import com.habit.app.data.NEWS_CATEGORY_SPORTS
import com.habit.app.data.NEWS_CATEGORY_TECHNOLOGY
import com.habit.app.data.NEWS_CATEGORY_WORLD
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.RealTimeNewsData
import com.habit.app.data.repority.PullNewsRepository
import com.habit.app.databinding.FragmentNewsBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.custom.NewsTabItem
import com.habit.app.ui.item.HomeNewsCardItem
import com.habit.app.ui.item.PlaceHolderItem
import com.habit.app.viewmodel.MainActivityModel
import com.habit.app.viewmodel.news.PullNewsModelFactory
import com.habit.app.viewmodel.news.PullNewsViewModel
import com.wyz.emlibrary.em.Direction
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class NewsFragment() : BaseFragment<FragmentNewsBinding>() {

    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)

    private val pullNewsModel: PullNewsViewModel by viewModels {
        PullNewsModelFactory(PullNewsRepository())
    }
    private val viewModel: MainActivityModel by activityViewModels()
    private val loadingObserver = MutableLiveData(false)

    /**
     * 当前选中的tab
     */
    private var curSelectTab: String = NEWS_CATEGORY_WORLD

    /**
     * 新闻列表是否正在加载中
     */
    private var isListLoading = false
    private var curPage = 1
    private var hasMore = false
    private var newsList = ArrayList<RealTimeNewsData>()

    private var newsTabMap: LinkedHashMap<String, Boolean> = linkedMapOf(
        NEWS_CATEGORY_WORLD to true,
        NEWS_CATEGORY_POLITICS to true,
        NEWS_CATEGORY_SCIENCE to true,
        NEWS_CATEGORY_HEALTH to true,
        NEWS_CATEGORY_SPORTS to true,
        NEWS_CATEGORY_TECHNOLOGY to true,
        NEWS_CATEGORY_BUSINESS to true,
    )
    /**
     * 新闻item点击回调
     */
    private val newsItemCallback: (String) -> Unit = { newsUrl ->
        viewModel.setSearchUrl(newsUrl)
    }

    /**
     * tab 选中回调
     */
    private val tabSelectListener: (String) -> Unit = { category ->
        curSelectTab = category
        val shouldRefreshNews = newsTabMap[category] ?: true
        binding.containerTabs.forEach { child ->
            if (child is NewsTabItem) {
                child.setSelect(category == child.mTabCategory)
            }
        }
        // 更新list
        curPage = 1
        if (shouldRefreshNews) {
            isListLoading = true
            loadingObserver.value = true
            // 拉取最新闻
            pullNewsModel.pullNews(curSelectTab)
        }
        newsList = getNewsList(curPage)
        updateList()
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentNewsBinding {
        return FragmentNewsBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (binding.containerNavi.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = EMUtil.getStatusBarHeight(requireContext())
        }

        initView()
        setUpObservers()
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

    private fun initData() {
        curSelectTab = newsTabMap.keys.first()

        newsTabMap.keys.forEach { category ->
            NewsTabItem(requireContext()).apply {
                setData(category, category == newsTabMap.keys.first())
                setOnClickListener {
                    if (!isListLoading) {
                        tabSelectListener.invoke(category)
                    }
                }
                if (this.parent != null) {
                    (this.parent as? ViewGroup)?.removeView(this)
                }
                binding.containerTabs.addView(this)
            }
        }

        loadingObserver.value = true
        isListLoading = true
        // 拉取最新闻
        pullNewsModel.pullNews(curSelectTab)
        newsList = getNewsList(curPage)
        updateList()
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            pullNewsModel.pullNewObserver.collect { list ->
                Log.d(TAG, "新闻列表返回：$list")
                newsTabMap[curSelectTab] = false
                loadingObserver.value = false
                isListLoading = false
                binding.swipeRefresh.isRefreshing = false

                if (list.isEmpty()) {
                    return@collect
                }
                // 数据库更新
                DBManager.getDao().insertNewsToTable(list)
                // page重置
                curPage = 1
                // 拉取新闻
                newsList = getNewsList(curPage)
                updateList()
            }
        }
    }

    private fun initListener() {
        binding.loadingView.setOnClickListener {}
        binding.swipeRefresh.setOnRefreshListener {
            if (!isListLoading) {
                refreshData()
            }
        }
        binding.recList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (newsList.isNotEmpty() && !recyclerView.canScrollVertically(1) && !isListLoading) {
                    // 划到最底部且不是加载状态
                    loadMoreData()
                }
            }
        })
    }

    /**
     * 数据库拉取新闻
     */
    private fun getNewsList(page: Int): ArrayList<RealTimeNewsData> {
        val result = DBManager.getDao().getNewsFromTable(curSelectTab, page, 10)
        hasMore = result.second
        return result.first
    }

    private fun refreshData() {
        curPage = 1
        isListLoading = true
        pullNewsModel.pullNews(curSelectTab)
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

    private fun updateList() {
        val items = ArrayList<AbstractFlexibleItem<*>>()
        items.add(PlaceHolderItem(12f))
        if (newsList.isEmpty()) {
            mAdapter.clear()
            binding.recList.post {
                binding.recList.isVisible = false
                binding.tvEmpty.isVisible = true
            }
            return
        }
        binding.recList.isVisible = true
        binding.tvEmpty.isVisible = false
        newsList.forEach { newsData ->
            items.add(HomeNewsCardItem(requireContext(), newsData, newsItemCallback))
        }

        mAdapter.updateDataSet(items)
    }

    private fun updateUIConfig() {
        EMManager.from(binding.bgTop)
            .setGradientRealColor(
                intArrayOf(
                    ThemeManager.getSkinColor(R.color.home_top_bg_start),
                    ThemeManager.getSkinColor(R.color.home_top_bg_end)
                ), Direction.TOP
            )
        binding.tvEmpty.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_40))
        binding.cardView.setCardBackgroundColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        binding.containerTabs.forEach { child ->
            if (child is NewsTabItem) {
                child.updateThemeUI()
            }
        }
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}