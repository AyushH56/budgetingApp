package com.example.poegroup4.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.poegroup4.SpendingRecord
import kotlin.math.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CategoryPieChartView @JvmOverloads constructor(
    context: Context,
    private val data: List<SpendingRecord>,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 26f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 24f
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.BLACK
    }

    private var selectedCategory: String? = null

    data class Slice(
        val category: String,
        val sweepAngle: Float,
        val startAngle: Float,
        val color: Int,
        val percentage: Float
    )

    private val slices = mutableListOf<Slice>()

    init {
        calculateSlices()
    }

    private fun calculateSlices() {
        val totalAmount = data.sumOf { it.amount }
        if (totalAmount == 0.0) return

        val grouped = data.groupBy { it.category }.mapValues { it.value.sumOf { v -> v.amount } }

        var startAngle = 0f
        for ((category, amount) in grouped) {
            val percentage = (amount / totalAmount).toFloat()
            val sweep = percentage * 360f
            val color = Color.rgb((50..200).random(), (50..200).random(), (50..200).random())
            slices.add(Slice(category, sweep, startAngle, color, percentage * 100))
            startAngle += sweep
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width * 0.4f
        val centerY = height / 2f
        val radius = min(width, height) * 0.28f

        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // Draw slices and percentage labels
        for (slice in slices) {
            paint.color = slice.color
            canvas.drawArc(rect, slice.startAngle, slice.sweepAngle, true, paint)

            if (slice.category == selectedCategory) {
                canvas.drawArc(rect, slice.startAngle, slice.sweepAngle, true, highlightPaint)
            }

            // Draw percentage text inside the slice
            val midAngle = Math.toRadians((slice.startAngle + slice.sweepAngle / 2).toDouble())
            val textRadius = radius * 0.6f
            val textX = (centerX + cos(midAngle) * textRadius).toFloat()
            val textY = (centerY + sin(midAngle) * textRadius).toFloat()
            canvas.drawText("${"%.1f".format(slice.percentage)}%", textX, textY, textPaint)
        }

        // Draw legend
        val legendStartX = centerX + radius + 40f
        var legendY = centerY - radius + 10f

        for (slice in slices) {
            paint.color = slice.color
            canvas.drawRect(legendStartX, legendY, legendStartX + 30f, legendY + 30f, paint)
            canvas.drawText(
                "${slice.category}: ${"%.1f".format(slice.percentage)}%",
                legendStartX + 40f,
                legendY + 25f,
                legendPaint
            )
            legendY += 45f
        }

        // Draw selected category info at bottom
        selectedCategory?.let { category ->
            val selected = slices.find { it.category == category }
            selected?.let {
                val text = "Selected: ${it.category} (${String.format("%.1f", it.percentage)}%)"
                val textWidth = textPaint.measureText(text)
                canvas.drawText(text, (width - textWidth) / 2f, height - 30f, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val centerX = width * 0.4f
            val centerY = height / 2f
            val dx = event.x - centerX
            val dy = event.y - centerY
            val distance = sqrt(dx * dx + dy * dy)

            val radius = min(width, height) * 0.28f
            if (distance <= radius) {
                val angle = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 360) % 360
                for (slice in slices) {
                    val endAngle = slice.startAngle + slice.sweepAngle
                    if (angle >= slice.startAngle && angle <= endAngle) {
                        selectedCategory = slice.category
                        invalidate()
                        break
                    }
                }
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
