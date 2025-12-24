package com.habit.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.recyclerview.widget.LinearLayoutManager
import com.habit.app.R
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.databinding.ActivityBookmarkFolderSelectBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.dialog.NewFolderDialog
import com.habit.app.ui.item.BookmarkFolderSelectItem
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import com.wyz.emlibrary.util.immersiveWindow
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem


class BookmarkFolderSelectActivity : BaseActivity() {
    private lateinit var binding: ActivityBookmarkFolderSelectBinding
    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
    private var newFolderDialog: NewFolderDialog? = null
    private var mInputFolderIds: List<Int> = emptyList()
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

    private val folderItemCallback = object : BookmarkFolderSelectItem.FolderCallback {
        override fun onFolderClick(item: BookmarkFolderSelectItem) {
            if (mInputFolderIds.contains(item.folderData.folderId)) {
                UtilHelper.showToast(this@BookmarkFolderSelectActivity, getString(R.string.toast_cannot_move_here))
                return
            }
            mCurrentFolder = item.folderData.folderId
            if (mCurrentFolder != -1) {
                binding.tvFolderName.text = item.folderData.folderName
            } else {
                binding.tvFolderName.text = ""
            }
            updateBookmarkItems(mCurrentFolder)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkFolderSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUiConfig()

        with(binding.recList) {
            itemAnimator = null
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@BookmarkFolderSelectActivity)
        }
    }

    private fun initData() {
        val inputIds = intent.getStringExtra(INPUT_FOLDER_IDS)
        if (!inputIds.isNullOrEmpty()) {
            mInputFolderIds = inputIds.split(",").map { it.toInt() }
        }
        updateBookmarkItems(mCurrentFolder)
    }

    private fun initListener() {
        binding.ivNaviBack.setOnClickListener {
            checkPageFinish()
        }
        binding.ivNaviCreate.setOnClickListener {
            processAddFolder()
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnMove.setOnClickListener {
            setResult(RESULT_OK, Intent().putExtra(SELECT_FOLDER_ID, mCurrentFolder))
            finish()
        }
    }

    /**
     * 刷新bookmark列表
     */
    private fun updateBookmarkItems(parentFolder: Int) {
        // folder
        val folderList = DBManager.getDao().getSubFolder(parentFolder)
        val items = ArrayList<AbstractFlexibleItem<*>>()
        folderList.forEach { data ->
            items.add(BookmarkFolderSelectItem(this, data, folderItemCallback))
        }
        mAdapter.updateDataSet(items)
    }

    private fun processAddFolder() {
        Log.d(TAG, "processAddFolder")
        newFolderDialog = NewFolderDialog.tryShowDialog(this)?.apply {
            setData(mCurrentFolder)
            this.mCallback = folderCreateCallback
            setOnDismissListener {
                newFolderDialog = null
            }
        }
    }

    private fun updateUiConfig() {
        EMManager.from(binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.btnCancel)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.create_folder_cancel_color))
        EMManager.from(binding.btnMove)
            .setCorner(12f)
            .setBackGroundRealColor(EMUtil.getColor(R.color.btn_color))
        binding.btnCancel.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_60))
        binding.btnMove.setTextColor(EMUtil.getColor(R.color.white))
        binding.ivNaviBack.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_back))
        binding.ivNaviCreate.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_bh_navi_create_folder))
        binding.tvFolderName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        mAdapter.currentItems.forEach { item ->
            mAdapter.updateItem(item, "update")
        }
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUiConfig()
    }

    private fun checkPageFinish() {
        if (mCurrentFolder == -1) {
            finish()
        } else {
            // 返回上一级
            val parentFolder = DBManager.getDao().getFolderById(mCurrentFolder)
            parentFolder?.let {
                mCurrentFolder = it.parentId
                if (mCurrentFolder != -1) {
                    binding.tvFolderName.text = it.folderName
                } else {
                    binding.tvFolderName.text = ""
                }
                updateBookmarkItems(mCurrentFolder)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            checkPageFinish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val INPUT_FOLDER_IDS = "input_folder_id"
        const val SELECT_FOLDER_ID = "select_folder_id"
    }
}