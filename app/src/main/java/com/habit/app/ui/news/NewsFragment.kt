package com.habit.app.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.habit.app.databinding.FragmentNewsBinding
import com.habit.app.ui.base.BaseFragment
import com.wyz.emlibrary.util.EMUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class NewsFragment() : BaseFragment<FragmentNewsBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)

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
        initData()
        initListener()
    }

    private fun initView() {
        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }
    }

    private fun initData() {

    }

    private fun initListener() {
        binding.loadingView.setOnClickListener {}
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }
}