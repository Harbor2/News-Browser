package com.habit.app.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.habit.app.R
import com.habit.app.databinding.FragmentBookmarkBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.item.OverFlyingLayoutManager
import com.wyz.emlibrary.em.EMManager
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem

class BookmarkFragment() : BaseFragment<FragmentBookmarkBinding>() {

    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
    private val loadingObserver = MutableLiveData(false)
    private val emptyObserver = MutableLiveData(false)
    private lateinit var overlayLayoutManager: OverFlyingLayoutManager

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentBookmarkBinding {
        return FragmentBookmarkBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        overlayLayoutManager = OverFlyingLayoutManager(requireContext())

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUIConfig()
        with(binding.recList) {
            itemAnimator = null
            adapter = mAdapter
            layoutManager = overlayLayoutManager
        }
        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }
        emptyObserver.observe(requireActivity()) { value ->
            binding.tvEmpty.visibility = if (value) View.VISIBLE else View.GONE
        }
    }

    private fun initData() {
    }

    private fun initListener() {
    }

    private fun updateUIConfig() {
        EMManager.from(binding.containerSearch)
            .setCorner(21f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        binding.ivSearch.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_bh_search_icon))
        binding.editInput.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.editInput.setHintTextColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}