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
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlin.jvm.javaClass

class HomeNewsCardItem(
    private val context: Context,
    private val newsData: RealTimeNewsData
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
        Glide.with(context)
            .load(newsData.thumbUrl)
            .into(holder.binding.ivIcon)
        holder.binding.tvGuide.text = newsData.guid
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