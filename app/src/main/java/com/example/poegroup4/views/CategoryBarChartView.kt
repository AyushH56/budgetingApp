package com.example.poegroup4.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.poegroup4.SpendingRecord
import kotlin.math.max
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CategoryBarChartView @JvmOverloads constructor(
    context: Context,
    private val data: List<SpendingRecord>,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 20f
        typeface = Typeface.DEFAULT
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        strokeWidth = 3f
    }
    private val tooltipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val barRects = mutableListOf<Pair<RectF, String>>()
    private val barColors = mutableMapOf<String, Int>()
    private var selectedBarIndex: Int? = null

    private var barData: List<Pair<String, Double>> = listOf()

    private val chartPaddingTop = 100f
    private val chartPaddingBottom = 140f
    private val chartPaddingSides = 100f
    private val barSpacing = 30f
    private val labelMargin = 12f
    private val barOffset = 10f

    init {
        prepareBarData()
    }

    private fun prepareBarData() {
        val grouped = data.groupBy { it.category }.mapValues { it.value.sumOf { r -> r.amount } }
        barData = grouped.toList().sortedByDescending { it.second }

        for ((category, _) in barData) {
            if (!barColors.containsKey(category)) {
                barColors[category] = Color.rgb((60..200).random(), (60..200).random(), (60..200).random())
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        barRects.clear()

        if (barData.isEmpty()) return

        val chartWidth = width - chartPaddingSides * 2
        val chartHeight = height - chartPaddingTop - chartPaddingBottom
        val barWidth = (chartWidth - (barSpacing * (barData.size - 1))) / barData.size
        val maxValue = (barData.maxOfOrNull { it.second } ?: 1.0) * 1.1

        val originX = chartPaddingSides
        val originY = height - chartPaddingBottom

        // Axes
        canvas.drawLine(originX, originY, originX, chartPaddingTop, axisPaint)
        canvas.drawLine(originX, originY, width - chartPaddingSides / 2, originY, axisPaint)

        // Y-axis ticks and labels
        val tickCount = 5
        val tickStep = maxValue / tickCount
        for (i in 0..tickCount) {
            val yVal = tickStep * i
            val yPos = originY - (yVal / maxValue * chartHeight).toFloat()
            canvas.drawLine(originX - 8f, yPos, originX, yPos, axisPaint)
            canvas.drawText("$${"%.0f".format(yVal)}", 12f, yPos + 8f, textPaint)
        }

        // Bars and X-axis labels
        var x = originX + barOffset
        barData.forEachIndexed { index, (category, amount) ->
            val barHeight = (amount / maxValue * chartHeight).toFloat()
            val barTop = originY - barHeight
            val barRect = RectF(x, barTop, x + barWidth, originY)

            paint.color = barColors[category] ?: Color.BLUE
            canvas.drawRect(barRect, paint)

            barRects.add(Pair(barRect, "$category: \$${"%.2f".format(amount)}"))

            // Adjusted Y for rotated label to avoid clipping
            drawRotatedText(
                canvas,
                category,
                x + barWidth / 2,
                originY + labelMargin + 10f,
                90f
            )

            x += barWidth + barSpacing
        }

        // Tooltip
        selectedBarIndex?.let { index ->
            if (index in barRects.indices) {
                val (rect, label) = barRects[index]
                canvas.drawText(
                    label,
                    rect.left,
                    rect.top - 16f,
                    tooltipPaint
                )
            }
        }
    }

    private fun drawRotatedText(canvas: Canvas, text: String, cx: Float, cy: Float, angle: Float) {
        canvas.save()
        canvas.rotate(angle, cx, cy)
        canvas.drawText(text, cx, cy, textPaint)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                selectedBarIndex = barRects.indexOfFirst { it.first.contains(event.x, event.y) }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                selectedBarIndex = null
                invalidate()
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}
