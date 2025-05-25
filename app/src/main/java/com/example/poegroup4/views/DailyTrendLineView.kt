package com.example.poegroup4.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.poegroup4.SpendingRecord
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DailyTrendLineView @JvmOverloads constructor(
    context: Context,
    private val data: List<SpendingRecord>,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        strokeWidth = 3f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 24f
        typeface = Typeface.DEFAULT
    }
    private val tooltipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
        typeface = Typeface.DEFAULT_BOLD
    }

    private var pointPositions = mutableListOf<Pair<Float, Float>>()
    private var touchIndex: Int? = null
    private var keys = listOf<String>()
    private var values = listOf<Double>()

    private val chartPadding = 100f
    private val pointRadius = 8f
    private val xLabelOffset = 12f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val grouped = data.groupBy { SimpleDateFormat("MM-dd", Locale.getDefault()).format(it.date) }
        val dailySums = grouped.mapValues { it.value.sumOf { r -> r.amount } }
        keys = dailySums.keys.sorted().takeLast(10)
        values = keys.map { dailySums[it] ?: 0.0 }

        if (values.isEmpty()) return

        val chartWidth = width - chartPadding * 2
        val chartHeight = height - 2 * chartPadding - 40f
        val spacing = chartWidth / (keys.size - 1)
        val maxVal = (values.maxOrNull() ?: 1.0) * 1.1

        val originX = chartPadding
        val originY = height - chartPadding

        pointPositions.clear()
        var prevX = 0f
        var prevY = 0f

        // Y-axis ticks and labels
        val tickCount = 5
        val tickStep = maxVal / tickCount
        textPaint.textAlign = Paint.Align.LEFT

        for (i in 0..tickCount) {
            val yVal = tickStep * i
            val yPos = originY - (yVal / maxVal * chartHeight).toFloat()
            canvas.drawLine(originX, yPos, width - chartPadding / 2, yPos, axisPaint)
            canvas.drawText("R${"%.0f".format(yVal)}", 0f, yPos + 8f, textPaint)
        }

        // Axes
        canvas.drawLine(originX, originY, width - chartPadding / 2, originY, axisPaint)
        canvas.drawLine(originX, originY, originX, chartPadding, axisPaint)

        textPaint.textAlign = Paint.Align.CENTER

        // Points and lines
        keys.forEachIndexed { i, key ->
            val x = originX + xLabelOffset + i * spacing
            val y = originY - (values[i] / maxVal * chartHeight).toFloat()

            paint.color = Color.BLUE
            canvas.drawCircle(x, y, pointRadius, paint)

            if (i > 0) {
                paint.strokeWidth = 3f
                canvas.drawLine(prevX, prevY, x, y, paint)
            }

            pointPositions.add(Pair(x, y))

            canvas.drawText(key, x, originY + 28f, textPaint)

            prevX = x
            prevY = y
        }

        // Tooltip
        touchIndex?.let { i ->
            val (x, y) = pointPositions[i]
            paint.color = Color.RED
            canvas.drawCircle(x, y, pointRadius + 4f, paint)

            val label = "${keys[i]}: R${"%.2f".format(values[i])}"
            tooltipPaint.textAlign = Paint.Align.LEFT
            val tooltipX = if (x + 200f > width) x - 200f else x + 16f
            canvas.drawText(label, tooltipX, y - 16f, tooltipPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val index = pointPositions.indexOfFirst { abs(it.first - event.x) < 40 }
                if (index != -1 && index != touchIndex) {
                    touchIndex = index
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                touchIndex = null
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
