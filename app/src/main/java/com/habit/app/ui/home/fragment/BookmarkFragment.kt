package com.habit.app.ui.home.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.habit.app.R
import com.habit.app.data.OPTION_ADD_TO_HOME
import com.habit.app.data.OPTION_ADD_TO_NAVI
import com.habit.app.data.OPTION_DELETE
import com.habit.app.data.OPTION_EDIT
import com.habit.app.data.OPTION_OPEN_IN_NEW_TAB
import com.habit.app.data.OPTION_REMOVE
import com.habit.app.data.OPTION_SELECT
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.BookmarkData
import com.habit.app.data.model.FolderData
import com.habit.app.databinding.FragmentBookmarkBinding
import com.habit.app.event.HomeAccessUpdateEvent
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.dialog.BookmarkEditDialog
import com.habit.app.ui.dialog.DeleteConfirmDialog
import com.habit.app.ui.dialog.MenuPopupFloat
import com.habit.app.ui.dialog.NavigationEditDialog
import com.habit.app.ui.dialog.NewFolderDialog
import com.habit.app.ui.home.BookmarkFolderSelectActivity
import com.habit.app.ui.item.BookmarkFolderItem
import com.habit.app.ui.item.BookmarkUrlItem
import com.habit.app.viewmodel.home.BHActivityModel
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import org.greenrobot.eventbus.EventBus

class BookmarkFragment() : BaseFragment<FragmentBookmarkBinding>() {

    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)

    private val bhActivityModel: BHActivityModel by activityViewModels<BHActivityModel>()
    private val loadingObserver = MutableLiveData(false)
    private val emptyObserver = MutableLiveData(false)
    private var newFolderDialog: NewFolderDialog? = null
    private var bookmarkEditDialog: BookmarkEditDialog? = null
    private var naviAddEditDialog: NavigationEditDialog? = null
    private var mDeleteDialog: DeleteConfirmDialog? = null

    /**
     * 所有的书签
     */
    private var mAllBookmarks: ArrayList<BookmarkData> = arrayListOf()

    /**
     * 当前层级
     */
    private var mCurrentFolder: Int = -1

    /**
     * folder 选择回调
     */
    private val folderSelectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val folderId = result.data?.getIntExtra(BookmarkFolderSelectActivity.SELECT_FOLDER_ID, -1) ?: -1
            bookmarkEditDialog?.updateDirectory(folderId)
        }
    }

    /**
     * folder 创建
     */
    private val folderCreateCallback: () -> Unit = {
        updateBookmarkItems(mCurrentFolder)
    }

    /**
     * 书签编辑回调
     */
    private val bookmarkEditCallback = object : BookmarkEditDialog.EditDialogCallback {
        override fun onFolderSaved() {
            UtilHelper.showToast(requireContext(), getString(R.string.toast_succeed))
            bhActivityModel.setEditObserver(false)
            binding.containerBottomOption.isVisible = false
            updateBookmarkItems(mCurrentFolder)
        }

        override fun onLaunchFolderSelect(folderIds: List<Int>) {
            folderSelectLauncher.launch(Intent(context, BookmarkFolderSelectActivity::class.java).apply {
                putExtra(BookmarkFolderSelectActivity.INPUT_FOLDER_IDS, folderIds.joinToString(","))
            })
        }
    }

    private val folderItemCallback = object : BookmarkFolderItem.FolderCallback {
        override fun onFolderClick(item: BookmarkFolderItem) {
            mCurrentFolder = item.folderData.folderId
            updateBookmarkItems(mCurrentFolder)
        }

        override fun onFolderMenu(anchorView: View, item: BookmarkFolderItem) {
            showMenu(anchorView, item.folderData)
        }

        override fun onFolderSelect(item: BookmarkFolderItem) {
            // 判断是否全选
            bhActivityModel.setBookmarkSelectAll(checkSelectAll())
        }
    }

    private val urlItemCallback = object : BookmarkUrlItem.BookmarkCallback {
        override fun onBookmarkClick(item: BookmarkUrlItem) {
            // 打开webview
        }

        override fun onBookmarkMenu(anchorView: View, item: BookmarkUrlItem) {
            showMenu(anchorView, item.bookmarkData)
        }

        override fun onBookmarkSelect(item: BookmarkUrlItem) {
            // 判断是否全选
            bhActivityModel.setBookmarkSelectAll(checkSelectAll())
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
    ): FragmentBookmarkBinding {
        return FragmentBookmarkBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUpObservers()
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
        // 查询所有书签
        mAllBookmarks = DBManager.getDao().getBookMarks()
    }

    private fun initListener() {
        binding.btnMove.setOnClickListener {
            val selectedDataList = getSelectItemData()
            Log.d(TAG, "move ${selectedDataList.size} 个元素")
            if (selectedDataList.isEmpty()) return@setOnClickListener
            showBookmarkEditDialog(selectedDataList)
        }

        binding.btnRemove.setOnClickListener {
            showDeleteConfirmDialog()
        }

        binding.editInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 内容为空则刷新当前目录
                if (s.toString().trim().isEmpty()) {
                    updateBookmarkItems(mCurrentFolder)
                }
            }
        })

        binding.editInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.editInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    updateSearchBookmark(query)
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
            OPTION_DELETE, OPTION_REMOVE -> {
                deleteSelectItems(data)
            }
            OPTION_OPEN_IN_NEW_TAB -> {

            }
            OPTION_EDIT -> {
                showBookmarkEditDialog(listOf(data))
            }
            OPTION_ADD_TO_NAVI -> {
                showNaviAddEditDialog(data)
            }
            OPTION_ADD_TO_HOME -> {

            }
            OPTION_SELECT -> {
                enterSelectMode(data)
                // 判断是否全选
                bhActivityModel.setBookmarkSelectAll(checkSelectAll())
            }
        }
    }

    private fun setUpObservers() {
    }

    /**
     * 获取选中item 的data list
     */
    private fun getSelectItemData(): ArrayList<Any> {
        val selectedDataList: ArrayList<Any> = arrayListOf()
        mAdapter.currentItems.map { item ->
            when (item) {
                is BookmarkFolderItem -> {
                    if (item.folderData.mSelect == true) {
                        selectedDataList.add(item.folderData)
                    }
                }

                is BookmarkUrlItem -> {
                    if (item.bookmarkData.mSelect == true) {
                        selectedDataList.add(item.bookmarkData)
                    }
                }
            }
        }
        return selectedDataList
    }

    /**
     * @param exitEdit 是否X按钮 退出编辑模式
     */
    fun selectAllOrNot(exitEdit: Boolean = false) {
        if (exitEdit) {
            bhActivityModel.setBookmarkSelectAll(false)
            exitSelectMode()
        } else {
            bhActivityModel.setBookmarkSelectAll(!bhActivityModel.bookmarkSelectAllObserver.value!!)
            processSelectAllOrNot(bhActivityModel.bookmarkSelectAllObserver.value!!)
        }
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

    fun checkPageFinish() {
        // 退出选择模式
        if (bhActivityModel.editObserver.value!!) {
            selectAllOrNot(true)
            return
        }
        if (mCurrentFolder == -1) {
            activity?.finish()
        } else {
            // 返回上一级
            mCurrentFolder = DBManager.getDao().getFolderById(mCurrentFolder)?.parentId ?: -1
            updateBookmarkItems(mCurrentFolder)
        }
    }

    /**
     * 删除选中item
     */
    private fun deleteSelectItems(data: Any? = null) {
        val selectedDataList = if (data == null) getSelectItemData() else listOf(data)
        Log.d(TAG, "remove ${selectedDataList.size} 个元素")
        if (selectedDataList.isEmpty()) return

        // 删除folder
        DBManager.getDao().deleteFolders(selectedDataList.filterIsInstance<FolderData>().map { it.folderId })
        // 删除url
        DBManager.getDao().deleteBookmarkByUrls(selectedDataList.filterIsInstance<BookmarkData>().map { it.url })
        // 刷新当前目录
        updateBookmarkItems(mCurrentFolder)
        bhActivityModel.setEditObserver(false)
        binding.containerBottomOption.isVisible = false
    }

    /**
     * 进入选择模式
     */
    private fun enterSelectMode(selectData: Any? = null) {
        mAdapter.currentItems.forEach { item ->
            when (item) {
                is BookmarkFolderItem -> {
                    // folder
                    item.folderData.mSelect = (selectData as? FolderData)?.folderId == item.folderData.folderId
                }

                is BookmarkUrlItem -> {
                    // url
                    item.bookmarkData.mSelect = (selectData as? BookmarkData)?.sign == item.bookmarkData.sign
                }
            }
        }
        mAdapter.updateDataSet(mAdapter.currentItems)

        if (!binding.containerBottomOption.isVisible) {
            binding.containerBottomOption.isVisible = true
            bhActivityModel.setEditObserver(true)
            binding.containerSearch.isVisible = false
        }
    }

    /**
     * 处理全选 非全选
     */
    private fun processSelectAllOrNot(selectAll: Boolean) {
        mAdapter.currentItems.forEach { item ->
            when (item) {
                is BookmarkFolderItem -> {
                    // folder
                    item.folderData.mSelect = selectAll
                }

                is BookmarkUrlItem -> {
                    // url
                    item.bookmarkData.mSelect = selectAll
                }
            }
        }
        mAdapter.updateDataSet(mAdapter.currentItems)
    }

    /**
     * 退出选择模式
     */
    private fun exitSelectMode() {
        mAdapter.currentItems.forEach { item ->
            when (item) {
                is BookmarkFolderItem -> {
                    // folder
                    item.folderData.mSelect = null
                }

                is BookmarkUrlItem -> {
                    // url
                    item.bookmarkData.mSelect = null
                }
            }
        }
        mAdapter.updateDataSet(mAdapter.currentItems)
        if (binding.containerBottomOption.isVisible) {
            binding.containerBottomOption.isVisible = false
            bhActivityModel.setEditObserver(false)
            binding.containerSearch.isVisible = true
        }
    }

    /**
     * 判断item是否全选
     */
    private fun checkSelectAll(): Boolean {
        mAdapter.currentItems.forEach { item ->
            when (item) {
                is BookmarkFolderItem -> {
                    if (item.folderData.mSelect == false) return false
                }

                is BookmarkUrlItem -> {
                    if (item.bookmarkData.mSelect == false) return false
                }
            }
        }
        return true
    }

    /**
     * 刷新bookmark列表
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
        emptyObserver.value = items.isEmpty()
        mAdapter.updateDataSet(items)
    }

    /**
     * 更新搜索书签列表
     */
    private fun updateSearchBookmark(query: String) {
        // 过滤数据
        val filterList = mAllBookmarks.filter { bookmark ->
            bookmark.name.contains(query, true) || bookmark.url.contains(query, true)
        }
        val items = ArrayList<AbstractFlexibleItem<*>>()
        filterList.forEach { data ->
            items.add(BookmarkUrlItem(requireActivity(), data, urlItemCallback))
        }
        emptyObserver.value = items.isEmpty()
        mAdapter.updateDataSet(items)
    }

    private fun showMenu(anchorView: View, payload: Any) {
        val menuList = if (payload is FolderData) {
            arrayListOf(
                OPTION_DELETE,
                OPTION_EDIT,
                OPTION_SELECT
            )
        } else {
            arrayListOf(
                OPTION_OPEN_IN_NEW_TAB,
                OPTION_REMOVE,
                OPTION_EDIT,
                OPTION_ADD_TO_NAVI,
                OPTION_ADD_TO_HOME,
                OPTION_SELECT
            )
        }
        MenuPopupFloat(requireActivity()).setData(payload).setCallback(popMenuCallback).show(anchorView, menuList)
    }

    /**
     * 显示编辑书签 dialog
     */
    private fun showBookmarkEditDialog(list: List<Any>) {
        bookmarkEditDialog = BookmarkEditDialog.tryShowDialog(requireActivity())?.apply {
            this.mCallback = bookmarkEditCallback
            setData(list, mCurrentFolder)

            setOnDismissListener {
                bookmarkEditDialog = null
            }
        }
    }

    /**
     * 添加编辑导航 dialog
     */
    private fun showNaviAddEditDialog(data: Any) {
        if (data !is BookmarkData) return

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

    fun showDeleteConfirmDialog() {
        // 二次确认
        mDeleteDialog = DeleteConfirmDialog.tryShowDialog(requireActivity())?.apply {
            this.initData(
                R.drawable.iv_dialog_delete_icon,
                getString(R.string.text_delete_all_selected),
                getString(R.string.text_cancel),
                getString(R.string.text_delete))
            setOnDismissListener {
                mDeleteDialog = null
            }
            this.mCallback = { result ->
                if (result) {
                    deleteSelectItems()
                }
            }
        }
    }

    private fun updateUIConfig() {
        EMManager.from(binding.containerSearch)
            .setCorner(21f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        EMManager.from(binding.btnMove)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.create_folder_cancel_color))
        EMManager.from(binding.btnRemove)
            .setCorner(12f)
            .setBackGroundRealColor(EMUtil.getColor("#FF1B0B"))
        binding.btnMove.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_60))
        binding.ivSearch.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_bh_search_icon))
        binding.editInput.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.editInput.setHintTextColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        binding.tvEmpty.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        newFolderDialog?.updateThemeUI()
        bookmarkEditDialog?.updateThemeUI()
        naviAddEditDialog?.updateThemeUI()
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