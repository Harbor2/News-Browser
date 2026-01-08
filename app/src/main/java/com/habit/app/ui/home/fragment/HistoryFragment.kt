package com.habit.app.ui.home.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.habit.app.R
import com.habit.app.data.OPTION_ADD_TO_BOOKMARK
import com.habit.app.data.OPTION_ADD_TO_HOME
import com.habit.app.data.OPTION_ADD_TO_NAVI
import com.habit.app.data.OPTION_DELETE
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.BookmarkData
import com.habit.app.data.model.HistoryData
import com.habit.app.databinding.FragmentBHistoryBinding
import com.habit.app.event.HomeAccessUpdateEvent
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.dialog.DeleteConfirmDialog
import com.habit.app.ui.dialog.MenuPopupFloat
import com.habit.app.ui.dialog.NavigationEditDialog
import com.habit.app.ui.item.BookmarkHistoryItem
import com.habit.app.ui.item.BookmarkHistoryTitleItem
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import org.greenrobot.eventbus.EventBus
import kotlin.text.ifEmpty

class HistoryFragment() : BaseFragment<FragmentBHistoryBinding>() {

    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
    private val loadingObserver = MutableLiveData(false)
    private val emptyObserver = MutableLiveData(false)
    private var mAllHistory: ArrayList<HistoryData> = ArrayList()
    private var mDeleteDialog: DeleteConfirmDialog? = null
    private var naviAddEditDialog: NavigationEditDialog? = null

    private val historyCallback = object : BookmarkHistoryItem.HistoryCallback {
        override fun onHistoryClick(item: BookmarkHistoryItem) {

        }

        override fun onHistoryMenu(anchorView: View, item: BookmarkHistoryItem) {
            showMenu(anchorView, item.data)
        }
    }

    /**
     * menu回调
     */
    private val popMenuCallback = object :MenuPopupFloat.PopupCallback {
        override fun onOptionSelect(option: String, data: Any) {
            processMenuOption(option, data)
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentBHistoryBinding {
        return FragmentBHistoryBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUIConfig()
        with(binding.recList) {
            itemAnimator = null
            adapter = mAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }
        emptyObserver.observe(requireActivity()) { value ->
            binding.tvEmpty.visibility = if (value) View.VISIBLE else View.GONE
        }
    }

    private fun initData() {
        updateHistoryList()
    }

    private fun initListener() {
        binding.editInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 内容为空则刷新当前目录
                if (s.toString().trim().isEmpty()) {
                    updateHistoryList()
                }
            }
        })

        binding.editInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.editInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    updateSearchHistory(query)
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * 处理menu回调
     */
    private fun processMenuOption(option: String, data: Any) {
        when (option) {
            OPTION_DELETE -> {
                deleteSelectHistory(data)
            }
            OPTION_ADD_TO_BOOKMARK -> {
                processAddToBookmark(data)
            }
            OPTION_ADD_TO_NAVI -> {
                showNaviAddEditDialog(data)
            }
            OPTION_ADD_TO_HOME -> {

            }
        }
    }

    fun processClearHistory() {
        // 二次确认
        mDeleteDialog = DeleteConfirmDialog.tryShowDialog(requireActivity())?.apply {
            this.initData(
                R.drawable.iv_dialog_delete_icon,
                getString(R.string.text_delete_all_history_title),
                getString(R.string.text_cancel),
                getString(R.string.text_delete))
            setOnDismissListener {
                mDeleteDialog = null
            }
            this.mCallback = { result ->
                if (result) {
                    DBManager.getDao().clearHistories()
                    this@HistoryFragment.binding.recList.post {
                        this@HistoryFragment.emptyObserver.value = true
                        updateHistoryList()
                    }
                }
            }
        }
    }

    /**
     * 添加编辑导航 dialog
     */
    private fun showNaviAddEditDialog(data: Any) {
        if (data !is HistoryData) return

        naviAddEditDialog = NavigationEditDialog.tryShowDialog(requireActivity())?.apply {
            setData(data.webIconPath, data.name.ifEmpty { data.url })
            setOnDismissListener {
                naviAddEditDialog = null
            }

            this.mCallback = { name ->
                val result = UtilHelper.homeAddAccessItem(requireContext(), name, data.url, data.webIconPath)
                if (result) {
                    UtilHelper.showToast(requireContext(), getString(R.string.toast_succeed))
                    EventBus.getDefault().post(HomeAccessUpdateEvent())
                }
            }
        }
    }

    /**
     * 添加到书签
     */
    private fun processAddToBookmark(data: Any) {
        if (data !is HistoryData) return
        val exitBookmark = DBManager.getDao().getBookMarkByUrl(data.url)
        if (exitBookmark != null) {
            UtilHelper.showToast(requireContext(), getString(R.string.toast_bookmark_exist))
            return
        }
        DBManager.getDao().insertBookmarkToTable(BookmarkData(name = data.name, url = data.url, webIconPath = data.webIconPath))
        UtilHelper.showToast(requireContext(), getString(R.string.toast_succeed))
    }

    private fun updateHistoryList() {
        mAllHistory = DBManager.getDao().getAllHistoryFromTable()
        val items = ArrayList<AbstractFlexibleItem<*>>()
        mAllHistory.forEach { data ->
            val timeStr = EMUtil.formatDateFromTimestamp("dd/MM/yyyy", data.timeStamp)
            val titleItem = BookmarkHistoryTitleItem(timeStr)
            if (!items.contains(titleItem)) {
                items.add(titleItem)
            }
            items.add(BookmarkHistoryItem(requireActivity(), data, historyCallback))
        }
        emptyObserver.value = items.isEmpty()
        mAdapter.updateDataSet(items, true)
    }

    private fun updateSearchHistory(query: String) {
        // 过滤数据
        val filterList = mAllHistory.filter { history ->
            history.name.contains(query, true) || history.url.contains(query, true)
        }
        val items = ArrayList<AbstractFlexibleItem<*>>()
        filterList.forEach { data ->
            items.add(BookmarkHistoryItem(requireActivity(), data, historyCallback))
        }
        emptyObserver.value = items.isEmpty()
        mAdapter.updateDataSet(items)
    }

    private fun deleteSelectHistory(data: Any) {
        if (data !is HistoryData) return
        DBManager.getDao().deleteHistoryFromTable(data)
        updateHistoryList()
    }
    private fun showMenu(anchorView: View, payload: Any) {
        val menuList = arrayListOf(
            OPTION_DELETE,
            OPTION_ADD_TO_BOOKMARK,
            OPTION_ADD_TO_NAVI,
            OPTION_ADD_TO_HOME
        )
        MenuPopupFloat(requireActivity()).setData(payload).setCallback(popMenuCallback).show(anchorView, menuList)
    }

    private fun updateUIConfig() {
        EMManager.from(binding.containerSearch)
            .setCorner(21f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        binding.ivSearch.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_bh_search_icon))
        binding.editInput.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.editInput.setHintTextColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        binding.tvEmpty.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        mDeleteDialog?.updateThemeUI()
        naviAddEditDialog?.updateThemeUI()
        mAdapter.updateDataSet(mAdapter.currentItems)
        binding.cardView.setCardBackgroundColor(ThemeManager.getSkinColor(R.color.view_bg_color))
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}