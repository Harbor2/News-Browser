package com.habit.app.ui.item

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.habit.app.R
import com.habit.app.databinding.LayoutItemHomeNewsCardBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.data.model.RealTimeNewsData
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass
import androidx.core.graphics.drawable.toDrawable

class HomeNewsCardItem(
    private val context: Context,
    private val newsData: RealTimeNewsData,
    private val newsItemCallback: (String) -> Unit
) : AbstractFlexibleItem<HomeNewsCardItem.ViewHolder>() {

    class ViewHolder(val binding: LayoutItemHomeNewsCardBinding, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(binding.root, adapter)

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): ViewHolder {
        return ViewHolder(LayoutItemHomeNewsCardBinding.bind(view), adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
                                holder: ViewHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        EMManager.from(holder.binding.tvTitle)
            .setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
            .setTextStr(newsData.title)
        EMManager.from(holder.binding.tvTime)
            .setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color_80))
            .setTextStr(EMUtil.formatDateFromTimestamp("dd-MM-yyyy HH:mm", newsData.pubTime))
        Glide.with(context)
            .load(newsData.thumbUrl)
            .placeholder(ThemeManager.getSkinColor(R.color.dialog_top_line_color).toDrawable())
            .error(ThemeManager.getSkinImageResId(R.drawable.iv_news_default_cover))
            .into(holder.binding.ivIcon)
        holder.binding.lineView.setBackgroundColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
        holder.binding.root.setOnClickListener {
            newsItemCallback.invoke(newsData.newsUrl)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_item_home_news_card
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}