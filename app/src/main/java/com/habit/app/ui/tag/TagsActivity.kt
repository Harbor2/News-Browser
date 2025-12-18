package com.habit.app.ui.tag

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.habit.app.R
import com.habit.app.data.db.DBManager
import com.habit.app.databinding.ActivityTagsBinding
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.item.OverFlyingLayoutManager
import com.habit.app.ui.item.TagSnapItem
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import kotlin.collections.ArrayList
import kotlin.collections.forEach


class TagsActivity : BaseActivity() {
    private lateinit var binding: ActivityTagsBinding
    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
    private val overlayLayoutManager = OverFlyingLayoutManager(this@TagsActivity)

    private val snapItemCallback = object : TagSnapItem.TagSnapItemCallback {
        override fun onItemClick(item: TagSnapItem) {

        }

        override fun onItemClose(item: TagSnapItem) {
            mAdapter.removeItem(mAdapter.currentItems.indexOf(item))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)
        KeyValueManager.saveBooleanValue(KeyValueManager.KEY_ENTERED_HOME, true)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUiConfig()

        with(binding.recList) {
            itemAnimator = null
            adapter = mAdapter
            layoutManager = overlayLayoutManager
        }
    }

    private fun initData() {
        binding.tabPublic.updateCount(1)
        binding.tabPrivacy.updateCount(1)
        updateSnapList()
    }

    private fun initListener() {
        binding.tabPublic.setOnClickListener {
            binding.tabPublic.updateSelect(true)
            binding.tabPrivacy.updateSelect(false)
        }
        binding.tabPrivacy.setOnClickListener {
            binding.tabPrivacy.updateSelect(true)
            binding.tabPublic.updateSelect(false)
        }
    }

    private fun updateSnapList() {
        val webSnaps = DBManager.getDao().getWebSnapsFromTable()
        val items = ArrayList<AbstractFlexibleItem<*>>()

        webSnaps.forEach {
            items.add(TagSnapItem(this, it, snapItemCallback))
        }
        mAdapter.updateDataSet(items)
        binding.recList.post {
            overlayLayoutManager.scrollToPositionWithOffsetInternal((items.size - 2).coerceAtLeast(0), -400)
        }
    }

    private fun updateUiConfig() {
        EMManager.from(binding.containerTopTab)
            .setCorner(22f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUiConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, TagsActivity::class.java))
        }
    }
}