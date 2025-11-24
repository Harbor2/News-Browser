package com.habit.app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.databinding.FragmentSearchBinding
import com.habit.app.ui.base.BaseFragment
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.SearchViewModel
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.getValue

class SearchFragment() : BaseFragment<FragmentSearchBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)
    private val viewModel: SearchViewModel by activityViewModels()

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (binding.containerNavi.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = EMUtil.getStatusBarHeight(requireContext())
        }

        initView()
        initData()
        setupObserver()
        initListener()
    }

    private fun initView() {
        updateUIConfig()

        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }
    }

    private fun initData() {

    }

    private fun initListener() {
        binding.loadingView.setOnClickListener {}
    }

    private fun setupObserver() {
        viewModel.input.observe(viewLifecycleOwner) { inputStr ->
            binding.editInput.setText(inputStr)
        }

        lifecycleScope.launch {

        }
    }

    private fun updateUIConfig() {
        binding.root.setBackgroundColor(ThemeManager.getSkinColor(R.color.page_main_color))
        binding.ivNaviTabAdd.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_tab_add))
        binding.ivNaviPageRefresh.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_page_refresh))
        binding.ivSearchIcon.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_search_icon))
        binding.editInput.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_80))
        EMManager.from(binding.containerInput)
            .setCorner(18f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }
}