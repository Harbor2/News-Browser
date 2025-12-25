package com.habit.app.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.habit.app.databinding.FragmentSettingBinding
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.home.BookmarkHistoryActivity
import com.habit.app.viewmodel.MainActivityModel
import com.wyz.emlibrary.util.EMUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlin.getValue

class SettingFragment() : BaseFragment<FragmentSettingBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)
    private val viewModel: MainActivityModel by activityViewModels()

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, parent, false)
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
        binding.containerNavi.setOnClickListener {
            BookmarkHistoryActivity.startActivity(requireActivity(), viewModel.privacyObserver.value!!, false)
        }
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }
}