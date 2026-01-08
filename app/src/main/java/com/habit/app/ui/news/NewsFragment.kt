package com.habit.app.ui.news

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.habit.app.data.repority.PullNewsRepository
import com.habit.app.data.repority.ThinkWordRepository
import com.habit.app.databinding.FragmentNewsBinding
import com.habit.app.ui.base.BaseFragment
import com.habit.app.viewmodel.home.SearchActivityModel
import com.habit.app.viewmodel.home.SearchActivityModelFactory
import com.habit.app.viewmodel.news.PullNewsModelFactory
import com.habit.app.viewmodel.news.PullNewsViewModel
import com.wyz.emlibrary.TAG
import com.wyz.emlibrary.util.EMUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NewsFragment() : BaseFragment<FragmentNewsBinding>() {

    private val mScope = MainScope()
    private val viewModel: PullNewsViewModel by viewModels {
        PullNewsModelFactory(PullNewsRepository())
    }
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
        setUpObservers()
        initData()
        initListener()
    }

    private fun initView() {
        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }
    }

    private fun initData() {
        viewModel.pullFoxNews()
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            viewModel.foxNewsObserver.collect { str ->
                Log.w(TAG, "collect news:${str}")
            }
        }
    }

    private fun initListener() {
        binding.loadingView.setOnClickListener {}
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }
}