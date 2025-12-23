package com.habit.app.ui.home.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.habit.app.R
import com.habit.app.data.OPTION_ADD_TO_HOME
import com.habit.app.data.OPTION_ADD_TO_NAVI
import com.habit.app.data.OPTION_EDIT
import com.habit.app.data.OPTION_OPEN_IN_NEW_TAB
import com.habit.app.data.OPTION_REMOVE
import com.habit.app.data.OPTION_SELECT
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.BookmarkData
import com.habit.app.data.model.FolderData
import com.habit.app.databinding.FragmentBookmarkBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.dialog.MenuPopupFloat
import com.habit.app.ui.dialog.NewFolderDialog
import com.habit.app.ui.item.BookmarkFolderItem
import com.habit.app.ui.item.BookmarkUrlItem
import com.habit.app.viewmodel.home.BHActivityModel
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem

class BookmarkFragment() : BaseFragment<FragmentBookmarkBinding>() {

    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
    private val bhActivityModel: BHActivityModel by activityViewModels<BHActivityModel>()
    private val loadingObserver = MutableLiveData(false)
    private val emptyObserver = MutableLiveData(false)
    private var newFolderDialog: NewFolderDialog? = null

    /**
     * 当前层级
     */
    private var mCurrentFolder: Int = -1

    /**
     * folder 创建
     */
    private val folderCreateCallback: () -> Unit = {
        updateBookmarkItems(mCurrentFolder)
    }

    private val folderItemCallback = object : BookmarkFolderItem.FolderCallback {
        override fun onFolderClick(item: BookmarkFolderItem) {
        }

        override fun onFolderMenu(anchorView: View, item: BookmarkFolderItem) {
            showMenu(anchorView, item.folderData)
        }

        override fun onFolderSelect(item: BookmarkFolderItem) {
        }
    }

    private val urlItemCallback = object : BookmarkUrlItem.BookmarkCallback {
        override fun onBookmarkSelect(item: BookmarkUrlItem) {
        }

        override fun onBookmarkClick(item: BookmarkUrlItem) {
        }

        override fun onBookmarkMenu(anchorView: View, item: BookmarkUrlItem) {
            showMenu(anchorView, item.bookmarkData)
        }
    }

    private val popMenuCallback = object :MenuPopupFloat.PopupCallback {
        override fun onOptionSelect(option: String, data: Any?) {

        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentBookmarkBinding {
        return FragmentBookmarkBinding.inflate(inflater, parent, false)
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
        // 获取当前目录
        mCurrentFolder = bhActivityModel.initBookmarkUrl?.let {
            DBManager.getDao().getBookMarkByUrl(url = it)?.folderId ?: -1
        } ?: -1

        updateBookmarkItems(mCurrentFolder)
    }

    private fun initListener() {
    }

    fun processActivityBack() {
        activity?.finish()
    }

    fun processAddFolder() {
        Log.d(TAG, "processAddFolder")
        newFolderDialog = NewFolderDialog.tryShowDialog(requireActivity())?.apply {
            setData(mCurrentFolder)
            this.mCallback = folderCreateCallback
            setOnDismissListener {
                newFolderDialog = null
            }
        }
    }

    /**
     * 刷新bookmark
     */
    private fun updateBookmarkItems(parentFolder: Int) {
        // folder
        val folderList = DBManager.getDao().getSubFolder(parentFolder)
        // mark
        val bookmarkList = DBManager.getDao().getBookmarksByFolderId(parentFolder)
        // 合并数据
        val dataList = folderList + bookmarkList

        val items = ArrayList<AbstractFlexibleItem<*>>()
        dataList.forEach { data ->
            when (data) {
                is FolderData -> items.add(BookmarkFolderItem(requireActivity(), data, folderItemCallback))
                is BookmarkData -> items.add(BookmarkUrlItem(requireActivity(), data, urlItemCallback))
            }
        }

        mAdapter.updateDataSet(items)
    }

    private fun showMenu(anchorView: View, payload: Any) {
        val menuList = arrayListOf(
            OPTION_OPEN_IN_NEW_TAB,
            OPTION_REMOVE,
            OPTION_EDIT,
            OPTION_ADD_TO_NAVI,
            OPTION_ADD_TO_HOME,
            OPTION_SELECT
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
        newFolderDialog?.updateThemeUI()
        mAdapter.currentItems.forEach { item ->
            mAdapter.updateItem(item, "update")
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