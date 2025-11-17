package com.habit.app.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.example.dogtok.ui.base.BaseFragment
import com.habit.app.MyApplication
import com.habit.app.databinding.FragmentHomeBinding
import com.habit.app.model.TAG
import com.wyz.emlibrary.util.EMUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class HomeFragment() : BaseFragment<FragmentHomeBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerNavi.post {
            (binding.containerNavi.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                topMargin = EMUtil.getStatusBarHeight(requireContext())
                binding.containerNavi.layoutParams = this
            }
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