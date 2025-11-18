package com.habit.app.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

abstract class BaseFragment<T : ViewBinding> : Fragment() {
    // 子类使用该方法来使用binding
    private var _binding: T? = null
    protected val binding: T get() = _binding!!

    // 标志位，记录 Fragment 是否被选中
    protected var isFragmentSelect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 调用onCreateViewBinding方法获取binding
        _binding = onCreateViewBinding(inflater, container)
        return binding.root
    }

    /**
     * Fragment 选中
     */
    open fun onFragmentSelect() {
    }

    /**
     * Fragment 未选中
     */
    open fun onFragmentUnSelect() {
    }

    fun updateFragmentSelect(status: Boolean) {
        if (status) {
            // 选中
            if (!isFragmentSelect) {
                isFragmentSelect = true
                onFragmentSelect()
            }
        } else {
            // 未选中
            if (isFragmentSelect) {
                isFragmentSelect = false
                onFragmentUnSelect()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 引用置空处理
        _binding = null
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun onBaseEvent(event: BaseActivity) {
    }

    // 由子类去重写
    protected abstract fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup?): T
}