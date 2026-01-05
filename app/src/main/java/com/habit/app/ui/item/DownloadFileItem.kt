package com.habit.app.ui.item

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.habit.app.R
import com.habit.app.data.model.DownloadFileData
import com.habit.app.data.model.DownloadItemPayload
import com.habit.app.databinding.LayoutItemDownloadFileBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class DownloadFileItem(
    val context: Context,
    var fileData: DownloadFileData,
    val mCallback: FileItemCallback
) : AbstractFlexibleItem<DownloadFileItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemDownloadFileBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemDownloadFileBinding.bind(view), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        payloads?.firstOrNull()?.let {
            if (it is DownloadItemPayload) {
                updateDownloadInfo(holder, it)
            }
        } ?: run {
            if (fileData.isDownloaded) {
                initFileDownloadedItem(holder)
            } else {
                initFileDownloadingItem(holder)
            }
        }
    }

    /**
     * 更新下载进度
     */
    private fun updateDownloadInfo(holder: ViewHolder, data: DownloadItemPayload) {
        holder.binding.progressView.progress = data.percent
        holder.binding.tvProgress.text = "${data.percent}%"
    }

    private fun initFileDownloadedItem(holder: ViewHolder) {
        setIcon(holder)
        holder.binding.tvName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        holder.binding.tvDesc.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
        holder.binding.ivMenu.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_b_folder_menu))
        holder.binding.tvName.text = UtilHelper.decodeUrlCode(fileData.fileName)
        holder.binding.tvDesc.text = EMUtil.formatBytesSize(fileData.fileSize)
        holder.binding.containerProgress.isVisible = false
        if (fileData.isSelect == null) {
            holder.binding.ivClose.isVisible = false
            holder.binding.ivMenu.isVisible = true
            holder.binding.ivCheckbox.isVisible = false
        } else {
            holder.binding.ivClose.isVisible = false
            holder.binding.ivMenu.isVisible = false
            holder.binding.ivCheckbox.isVisible = true
            holder.binding.ivCheckbox.setImageResource(ThemeManager.getSkinImageResId(if (fileData.isSelect!!) R.drawable.iv_checkbox_select else R.drawable.iv_checkbox_unselect))
        }

        holder.binding.ivMenu.setOnClickListener {
            mCallback.onFileMenuClick(fileData, holder.binding.root)
        }
        holder.binding.root.setOnClickListener {
            if (fileData.isSelect == null) {
                mCallback.onFileOpen(fileData)
            } else {
                fileData.isSelect = !fileData.isSelect!!
                holder.binding.ivCheckbox.setImageResource(ThemeManager.getSkinImageResId(if (fileData.isSelect!!) R.drawable.iv_checkbox_select else R.drawable.iv_checkbox_unselect))
                mCallback.onFileSelect(fileData)
            }
        }
    }

    private fun initFileDownloadingItem(holder: ViewHolder) {
        setIcon(holder)
        holder.binding.tvName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        holder.binding.tvDesc.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
        holder.binding.ivClose.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_download_item_cancel))
        EMManager.from(holder.binding.containerProgress)
            .setCorner(14f)
            .setBorderWidth(1f)
            .setBorderColor(R.color.indication_color)
        holder.binding.tvName.text = UtilHelper.decodeUrlCode(fileData.fileName)
        holder.binding.tvDesc.text = EMUtil.formatBytesSize(fileData.fileSize)
        holder.binding.tvProgress.text = "${fileData.downloadProgress}%"
        holder.binding.progressView.progress = fileData.downloadProgress
        holder.binding.tvProgress.isVisible = !fileData.isPause
        holder.binding.ivDownloadResume.isVisible = fileData.isPause
        if (fileData.isSelect == null) {
            holder.binding.containerProgress.isVisible = true
            holder.binding.ivMenu.isVisible = false
            holder.binding.ivClose.isVisible = true
            holder.binding.ivCheckbox.isVisible = false
        } else {
            holder.binding.containerProgress.isVisible = false
            holder.binding.ivMenu.isVisible = false
            holder.binding.ivClose.isVisible = false
            holder.binding.ivCheckbox.isVisible = true
            holder.binding.ivCheckbox.setImageResource(ThemeManager.getSkinImageResId(if (fileData.isSelect!!) R.drawable.iv_checkbox_select else R.drawable.iv_checkbox_unselect))
        }

        holder.binding.containerProgress.setOnClickListener {
            fileData.isPause = !fileData.isPause
            if (fileData.isPause) {
                holder.binding.tvProgress.isVisible = false
                holder.binding.ivDownloadResume.isVisible = true
            } else {
                holder.binding.tvProgress.isVisible = true
                holder.binding.ivDownloadResume.isVisible = false
            }
            mCallback.onDownloadPause(fileData)
        }
        holder.binding.ivClose.setOnClickListener {
            mCallback.onDownloadCancel(fileData)
        }
        holder.binding.root.setOnClickListener {
            if (fileData.isSelect != null) {
                fileData.isSelect = !fileData.isSelect!!
                holder.binding.ivCheckbox.setImageResource(ThemeManager.getSkinImageResId(if (fileData.isSelect!!) R.drawable.iv_checkbox_select else R.drawable.iv_checkbox_unselect))
                mCallback.onFileSelect(fileData)
            }
        }
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_download_file
    }

    private fun setIcon(holder: ViewHolder) {
        when (fileData.fileType) {
            DownloadFileData.TYPE_PIC -> {
                Glide.with(context)
                    .load(fileData.filePath)
                    .error(R.drawable.iv_file_image)
                    .into(holder.binding.ivCover)
            }
            DownloadFileData.TYPE_VIDEO -> {
                Glide.with(context)
                    .load(fileData.filePath)
                    .error(R.drawable.iv_file_video)
                    .into(holder.binding.ivCover)
            }
            DownloadFileData.TYPE_AUDIO -> {
                holder.binding.ivCover.setImageResource(R.drawable.iv_file_music)
            }

            DownloadFileData.TYPE_DOC -> {
                holder.binding.ivCover.setImageResource(R.drawable.iv_file_docs)
            }

            DownloadFileData.TYPE_APK -> {
                holder.binding.ivCover.setImageResource(R.drawable.iv_file_apk)
            }

            DownloadFileData.TYPE_ZIP -> {
                holder.binding.ivCover.setImageResource(R.drawable.iv_file_zip)
            }

            DownloadFileData.TYPE_UNKNOWN -> {
                holder.binding.ivCover.setImageResource(R.drawable.iv_file_unknown)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DownloadFileItem

        return fileData == other.fileData
    }

    override fun hashCode(): Int {
        return fileData.hashCode()
    }

    interface FileItemCallback {
        fun onFileOpen(fileData: DownloadFileData)

        fun onFileSelect(fileData: DownloadFileData)

        fun onDownloadPause(fileData: DownloadFileData)

        fun onDownloadCancel(fileData: DownloadFileData)

        fun onFileMenuClick(fileData: DownloadFileData, targetView: View)
    }
}