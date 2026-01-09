package com.habit.app.ui.item;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OverFlyingLayoutManager extends LinearLayoutManager {

    /**
     * 推进的减速倍数（越大越慢）
     */
    private float slowTimes = 3f;

    /**
     * 单个 item 高度
     */
    private int viewHeight = 0;

    /**
     * 当前推进的总偏移（核心状态）
     */
    private int verticalScrollOffset = 0;

    /**
     * 滚动回弹动画
     */
    private ValueAnimator reboundAnimator;

    public OverFlyingLayoutManager(Context context) {
        super(context);
        setAutoMeasureEnabled(true);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    // ========================
    // Layout
    // ========================

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            verticalScrollOffset = 0;
            return;
        }

        // 只测量一次 item 高度
        if (viewHeight == 0) {
            View view = recycler.getViewForPosition(0);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            viewHeight = getDecoratedMeasuredHeight(view);
            detachAndScrapView(view, recycler);
        }

        detachAndScrapAttachedViews(recycler);
        layoutItems(recycler);
    }

    /**
     * 核心布局逻辑
     * position 0 在最前面（顶部）
     */
    private void layoutItems(RecyclerView.Recycler recycler) {
        int itemCount = getItemCount();

        // 正向遍历，保证 position 0 在顶部
        for (int i = 0; i < itemCount; i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);

            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            // 推进公式（核心）：
            // verticalScrollOffset：整体推进量
            // i * viewHeight：该 item 理论推进点
            int baseOffset = i * viewHeight - verticalScrollOffset;

            // 所有 item 初始都在顶部（>= 0）
            int topOffset = Math.max(0, (int) (baseOffset / slowTimes));

            layoutDecoratedWithMargins(
                    view,
                    0,
                    topOffset,
                    width,
                    topOffset + height
            );
        }
    }

    // ========================
    // Scroll
    // ========================

    @Override
    public int scrollVerticallyBy(int dy,
                                  RecyclerView.Recycler recycler,
                                  RecyclerView.State state) {

        if (getChildCount() == 0) return 0;

        int maxOffset = (getItemCount() - 1) * viewHeight;

        // 正向消费 dy
        int newOffset = verticalScrollOffset + dy;

        if (newOffset < 0) newOffset = 0;
        if (newOffset > maxOffset) newOffset = maxOffset;

        int consumed = newOffset - verticalScrollOffset;
        verticalScrollOffset = newOffset;

        detachAndScrapAttachedViews(recycler);
        layoutItems(recycler);

        return consumed;
    }

    /**
     * 滚动回弹
     */
    public void checkSoftReboundIfNeeded() {
        int softMax = Math.max(0, (getItemCount() - 1) * viewHeight - 500);

        if (verticalScrollOffset <= softMax) return;

        if (reboundAnimator != null && reboundAnimator.isRunning()) {
            reboundAnimator.cancel();
        }

        int start = verticalScrollOffset;
        int end = softMax;

        reboundAnimator = ValueAnimator.ofInt(start, end);
        reboundAnimator.setDuration(400); // 慢慢回
        reboundAnimator.setInterpolator(new DecelerateInterpolator());

        reboundAnimator.addUpdateListener(animation -> {
            verticalScrollOffset = (int) animation.getAnimatedValue();
            requestLayout();
        });

        reboundAnimator.start();
    }

    public void scrollToPositionWithOffsetInternal(int position, int offset) {
        if (viewHeight == 0) return;
        if (position < 0) position = 0;
        verticalScrollOffset = position * viewHeight + offset;
        if (verticalScrollOffset < 0) verticalScrollOffset = 0;
        requestLayout();
    }

    @Override
    public View findViewByPosition(int position) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (getPosition(child) == position) {
                return child;
            }
        }
        return null;
    }
}