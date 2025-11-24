package com.habit.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.base.BaseActivity
import com.habit.app.R
import com.habit.app.databinding.ActivitySearch2Binding
import com.habit.app.model.TAG
import com.habit.app.ui.fragment.HistoryFragment
import com.habit.app.ui.fragment.SearchFragment
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class SearchActivity2 : BaseActivity() {
    private lateinit var binding: ActivitySearch2Binding
    private val mScope = MainScope()

    private val historyFragmentTag = "HistoryFragment"
    private val searchFragmentTag = "SearchFragment"
    private var currentFragmentTag: String = historyFragmentTag

    private val historyPageCallback = object : HistoryFragment.HistoryFragmentCallback {
        override fun onSearch() {
            switchFragment(searchFragmentTag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        binding = ActivitySearch2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
    }

    private fun initData() {
        switchFragment(currentFragmentTag)
    }

    private fun initListener() {

    }

    private fun switchFragment(tag: String) {
        currentFragmentTag = tag
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentByTag(tag)
        val transaction = fragmentManager.beginTransaction()
        if (currentFragment == null) {
            // Fragment 不存在，创建新的实例
            val newFragment = when (tag) {
                historyFragmentTag -> HistoryFragment().apply {
                    this.mCallback = historyPageCallback
                    setFocus(hasFocus)
                }
                searchFragmentTag -> SearchFragment()
                else -> return
            }
            transaction.add(R.id.fragment_container, newFragment, tag)
            (newFragment as? BaseFragment<*>)?.updateFragmentSelect(true)
        } else {
            // Fragment 已存在，直接显示
            transaction.show(currentFragment)
            (currentFragment as? BaseFragment<*>)?.updateFragmentSelect(true)
        }
        // 隐藏其他 Fragment
        for (fragment in fragmentManager.fragments) {
            if (fragment.tag != tag) {
                transaction.hide(fragment)
                (fragment as? BaseFragment<*>)?.updateFragmentSelect(false)
            }
        }
        transaction.commit()
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }

    companion object {
        private var hasFocus: Boolean = false
        fun startActivity(context: Context, focus: Boolean = false) {
            this.hasFocus = focus
            context.startActivity(Intent(context, SearchActivity2::class.java))
        }
    }
}