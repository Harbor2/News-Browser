package com.habit.app.ui.custom.scan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.habit.app.R
import com.wyz.emlibrary.util.EMUtil

/**
 * 自定义扫描浮层view
 */
class CustomMaskView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private val maskPath = Path()
    private val rectPath = Path()

    /**
     * 透明区域圆角
     */
    private val cornerRadius = EMUtil.dp2px(8f)

    /**
     * 延长线的长度
     */
    private val lineLength = EMUtil.dp2px(30f)

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = EMUtil.getColor(R.color.white)
        strokeWidth = EMUtil.dp2px(6f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        maskPath.reset()
        rectPath.reset()

        maskPath.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
        // 计算矩形区域的位置
        val rectWidth = EMUtil.dp2px(260f)
        val rectHeight = EMUtil.dp2px(260f)

        val left = (width - rectWidth) / 2f
        val top = EMUtil.dp2px(230f)
        val right = left + rectWidth
        val bottom = top + rectHeight

        // 创建透明的圆角矩形区域
        rectPath.addRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, Path.Direction.CW)
        // 裁剪出圆形透明区域
        maskPath.op(rectPath, Path.Op.DIFFERENCE)
        canvas.clipPath(maskPath)

        // 绘制遮罩
        canvas.drawColor(Color.parseColor("#80000000"))
        drawCornerArcs(canvas, left, top, right, bottom)
    }

    /**
     * 绘制圆角线条
     */
    private fun drawCornerArcs(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val arcRect = RectF()
        // 左上角弧线和直线
        arcRect.set(left, top, left + cornerRadius * 2, top + cornerRadius * 2)
        canvas.drawArc(arcRect, 180f, 90f, false, borderPaint)
        canvas.drawLine(left + cornerRadius, top, left + cornerRadius + lineLength, top, borderPaint)
        canvas.drawLine(left, top + cornerRadius, left, top + cornerRadius + lineLength, borderPaint)
        // 右上角弧线和直线
        arcRect.set(right - cornerRadius * 2, top, right, top + cornerRadius * 2)
        canvas.drawArc(arcRect, 270f, 90f, false, borderPaint)
        canvas.drawLine(right - cornerRadius, top, right - cornerRadius - lineLength, top, borderPaint)
        canvas.drawLine(right, top + cornerRadius, right, top + cornerRadius + lineLength, borderPaint)
        // 左下角弧线和直线
        arcRect.set(left, bottom - cornerRadius * 2, left + cornerRadius * 2, bottom)
        canvas.drawArc(arcRect, 90f, 90f, false, borderPaint)
        canvas.drawLine(left + cornerRadius, bottom, left + cornerRadius + lineLength, bottom, borderPaint)
        canvas.drawLine(left, bottom - cornerRadius, left, bottom - cornerRadius - lineLength, borderPaint)
        // 右下角弧线和直线
        arcRect.set(right - cornerRadius * 2, bottom - cornerRadius * 2, right, bottom)
        canvas.drawArc(arcRect, 0f, 90f, false, borderPaint)
        canvas.drawLine(right - cornerRadius, bottom, right - cornerRadius - lineLength, bottom, borderPaint)
        canvas.drawLine(right, bottom - cornerRadius, right, bottom - cornerRadius - lineLength, borderPaint)
    }
}