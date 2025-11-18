package com.habit.app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.habit.app.ui.base.BaseFragment
import com.habit.app.R
import com.habit.app.databinding.FragmentHomeBinding
import com.habit.app.ui.item.HomeAccessItem
import com.habit.app.ui.item.HomeSearchItem
import com.wyz.emlibrary.em.Direction
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class HomeFragment() : BaseFragment<FragmentHomeBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)
    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, parent, false)
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
        updateUIConfig()

        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }

        with(binding.recList) {
            setHasFixedSize(true)
            adapter = mAdapter
            animation = null
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun updateUIConfig() {
        EMManager.from(binding.bgTop)
            .setGradientColor(intArrayOf(R.color.home_top_bg_start, R.color.home_top_bg_end), Direction.TOP)
    }

    private fun initData() {
        updateList()
    }

    private fun initListener() {
        binding.loadingView.setOnClickListener {}
    }

    private fun updateList() {
        val items = ArrayList<AbstractFlexibleItem<*>>()

        items.add(HomeSearchItem(requireContext()))
        items.add(HomeAccessItem(requireContext()))
        mAdapter.updateDataSet(items)
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }
}