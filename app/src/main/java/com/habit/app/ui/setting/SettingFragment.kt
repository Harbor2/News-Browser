package com.habit.app.ui.setting

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.data.ENGINE_GOOGLE
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.databinding.FragmentSettingBinding
import com.habit.app.event.HomeTabsClearedEvent
import com.habit.app.helper.DayNightUtil
import com.habit.app.helper.DayNightUtil.NIGHT_MODE_DAY
import com.habit.app.helper.DayNightUtil.NIGHT_MODE_FOLLOW_SYSTEM
import com.habit.app.helper.FeedbackUtils
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.BrowseActivity
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.custom.SettingItem
import com.habit.app.ui.dialog.DataDeleteDialog
import com.habit.app.ui.dialog.SearchEngineDialog
import com.habit.app.ui.dialog.ThemeSelectDialog
import com.habit.app.ui.home.BookmarkHistoryActivity
import com.habit.app.ui.home.FileDownloadActivity
import com.habit.app.viewmodel.MainActivityModel
import com.wyz.emlibrary.em.Direction
import com.wyz.emlibrary.em.EMManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File
import kotlin.getValue

class SettingFragment() : BaseFragment<FragmentSettingBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)
    private val viewModel: MainActivityModel by activityViewModels()

    private var searchEngineDialog: SearchEngineDialog? = null
    private var themeSelectDialog: ThemeSelectDialog? = null
    private var dataDeleteDialog: DataDeleteDialog? = null
    private val defaultBrowserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        val isDefaultBrowser = UtilHelper.isDefaultBrowser(requireContext())
        Log.d(TAG, "settingFragment isDefaultBrowser: $isDefaultBrowser")
        binding.itemDefaultBrowser.updateSwitch(isDefaultBrowser)
        binding.itemDefaultBrowser.isVisible = !isDefaultBrowser
    }

    private fun initView() {
        updateUIConfig()
        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initData() {
        val curEngine = KeyValueManager.getValueByKey(KeyValueManager.KEY_ENGINE_SELECT) ?: ENGINE_GOOGLE
        binding.itemSearchEnging.updateDesc(curEngine)
        binding.itemAddComponents.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        binding.tvVersion.text = "v${UtilHelper.getAppVersionName(requireContext())}"
    }

    private fun initListener() {
        binding.loadingView.setOnClickListener {}

        binding.itemSearchEnging.setOnClickListener {
            Log.d(TAG, "initListener: click search engine")
            searchEngineDialog = SearchEngineDialog.tryShowDialog(requireActivity())?.apply {
                this.mCallback = { engine ->
                    this@SettingFragment.binding.itemSearchEnging.updateDesc(engine)
                }
                setOnDismissListener {
                    searchEngineDialog = null
                }
            }
        }
        binding.itemDefaultBrowser.setOnClickListener {
            val isDefault = UtilHelper.isDefaultBrowser(requireContext())
            if (isDefault) {
                UtilHelper.showToast(requireContext(), getString(R.string.toast_default_browser))
                return@setOnClickListener
            }
            chooseDefaultBrowser()
        }
        binding.itemTheme.setOnClickListener {
            processThemeSelect()
        }
        binding.itemAddComponents.setOnClickListener {
            processAddComponents()
        }

        binding.itemDownload.setOnClickListener {
            FileDownloadActivity.startActivity(requireContext())
        }
        binding.itemBookmark.setOnClickListener {
            BookmarkHistoryActivity.startActivity(requireContext(), viewModel.privacyObserver.value!!, true)
        }
        binding.itemHistory.setOnClickListener {
            BookmarkHistoryActivity.startActivity(requireContext(), viewModel.privacyObserver.value!!, false)
        }
        binding.itemDeleteData.setOnClickListener {
            processDataDelete()
        }

        binding.itemFeedback.setOnClickListener {
            FeedbackUtils.feedback(requireContext())
        }
        binding.itemPrivacy.setOnClickListener {
            BrowseActivity.startPrivacyPolicy(requireContext())
        }
        binding.itemTerms.setOnClickListener {
            BrowseActivity.startTermOfService(requireContext())
        }
    }

    private fun processDataDelete() {
        dataDeleteDialog = DataDeleteDialog.tryShowDialog(requireActivity())?.apply {
            setOnDismissListener {
                dataDeleteDialog = null
            }
            this.mCallback = { list ->
                loadingObserver.value = true
                this@SettingFragment.lifecycleScope.launch(Dispatchers.IO) {
                    if (list.contains(DataDeleteDialog.HISTORY)) {
                        DBManager.getDao().clearHistories()
                    }
                    if (list.contains(DataDeleteDialog.CACHE)) {
                        val parentFile = File(context.cacheDir, "webPic")
                        if (parentFile.exists()) {
                            parentFile.deleteRecursively()
                        }
                        val downloadDir = UtilHelper.getExternalFilesDownloadDir(false)
                        if (downloadDir.exists()) {
                            downloadDir.deleteRecursively()
                        }
                    }
                    if (list.contains(DataDeleteDialog.TABS)) {
                        KeyValueManager.saveBooleanValue(KeyValueManager.KEY_REOPEN_LAST_TAB, false)
                        DBManager.getDao().clearWebSnaps()
                    }
                    if (list.contains(DataDeleteDialog.HISTORY_RECORDS)) {
                        DBManager.getDao().clearSearchRecords()
                    }

                    withContext(Dispatchers.Main) {
                        EventBus.getDefault().post(HomeTabsClearedEvent())
                        delay(1000)
                        UtilHelper.showToast(context, context.getString(R.string.toast_succeed))
                        loadingObserver.value = false
                    }
                }
            }
        }
    }

    private fun processThemeSelect() {
        themeSelectDialog = ThemeSelectDialog.tryShowDialog(requireActivity())?.apply {
            this.mCallback = { mode ->
                Log.d(TAG, "用户选择日夜间模式: $mode")
                var realMode = mode
                if (realMode == NIGHT_MODE_FOLLOW_SYSTEM) {
                    realMode = DayNightUtil.getRealCurrentNightMode(context)
                }
                // 执行切换
                if (realMode == NIGHT_MODE_DAY) {
                    if (ThemeManager.getCurTheme() != ThemeManager.THEME_DEFAULT) {
                        Log.d(TAG, "实际切换到日间模式")
                        ThemeManager.switchTheme(ThemeManager.THEME_DEFAULT)
                    }
                } else {
                    if (ThemeManager.getCurTheme() != ThemeManager.THEME_NIGHT) {
                        Log.d(TAG, "实际切换到夜间模式")
                        ThemeManager.switchTheme(ThemeManager.THEME_NIGHT)
                    }
                }
            }
            setOnDismissListener {
                themeSelectDialog = null
            }
        }
    }

    private fun processAddComponents() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val appWidgetManager = AppWidgetManager.getInstance(requireContext())
        val componentName = ComponentName(requireContext(), SearchWidgetProvider::class.java)
        // 判断 Launcher 是否支持
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(componentName, null, null)
        } else {
            UtilHelper.showToast(requireContext(), getString(R.string.toast_add_widget_not_support))
        }
    }

    private fun updateUIConfig() {
        EMManager.from(binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.topView)
            .setGradientRealColor(
                intArrayOf(
                    ThemeManager.getSkinColor(R.color.home_top_bg_start),
                    ThemeManager.getSkinColor(R.color.home_top_bg_end)
                ),
                Direction.TOP
            )
        binding.ivAppName.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_setting_app_name))
        binding.tvVersion.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_40))
        binding.tvBasic.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_40))
        binding.tvBrowser.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_40))
        binding.tvApplication.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_40))
        binding.containerOptions.forEach { child ->
            if (child is SettingItem) {
                child.updateThemeUi()
            }
        }
        searchEngineDialog?.updateThemeUI()
        themeSelectDialog?.updateThemeUI()
        dataDeleteDialog?.updateThemeUI()
        binding.cardView.setCardBackgroundColor(ThemeManager.getSkinColor(R.color.view_bg_color))
    }

    /**
     * 选择默认浏览器
     */
    private fun chooseDefaultBrowser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireActivity().getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER) && !roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)) {
                defaultBrowserLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER))
            }
        } else {
            startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            )
        }
    }

    override fun onThemeChanged(theme: String) {
        updateUIConfig()
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }
}