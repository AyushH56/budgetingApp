package com.example.poegroup4.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.poegroup4.SpendingRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.max

class CategoryBarChartView @JvmOverloads constructor(
    context: Context,
    private val data: List<SpendingRecord>,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 22f
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
    private var selectedBarIndex: Int? = null

    private var barData: List<Pair<String, Double>> = listOf()
    private var budgetGoals: Map<String, Pair<Double, Double>> = mapOf()

    private val chartPaddingTop = 100f
    private val chartPaddingBottom = 200f  // Increased to make room for full legend
    private val chartPaddingSides = 100f
    private val barSpacing = 30f
    private val labelMargin = 12f
    private val barOffset = 10f

    init {
        prepareBarData()
        loadBudgetGoals()
    }

    private fun prepareBarData() {
        val grouped = data.groupBy { it.category }.mapValues { it.value.sumOf { r -> r.amount } }
        barData = grouped.toList().sortedByDescending { it.second }
    }

    private fun loadBudgetGoals() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val dbRef = FirebaseDatabase.getInstance().reference
            .child("budgetGoals")
            .child(user.uid)

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goals = mutableMapOf<String, Pair<Double, Double>>()
                for (goalSnap in snapshot.children) {
                    val category = goalSnap.child("category").getValue(String::class.java)
                    val min = goalSnap.child("minBudget").getValue(Double::class.java)
                    val max = goalSnap.child("maxBudget").getValue(Double::class.java)

                    if (category != null && min != null && max != null) {
                        goals[category] = Pair(min, max)
                    }
                }
                budgetGoals = goals
                invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle if needed
            }
        })
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
            canvas.drawText("R${"%.0f".format(yVal)}", 12f, yPos + 8f, textPaint)
        }

        // Bars and X-axis labels
        var x = originX + barOffset
        barData.forEachIndexed { index, (category, amount) ->
            val barHeight = (amount / maxValue * chartHeight).toFloat()
            val barTop = originY - barHeight
            val barRect = RectF(x, barTop, x + barWidth, originY)

            // Color logic
            val goal = budgetGoals[category]
            paint.color = when {
                goal == null -> Color.LTGRAY
                amount < goal.first -> Color.BLUE
                amount > goal.second -> Color.RED
                else -> Color.GREEN
            }

            canvas.drawRect(barRect, paint)
            barRects.add(Pair(barRect, "$category: R${"%.2f".format(amount)}"))

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
                canvas.drawText(label, rect.left, rect.top - 16f, tooltipPaint)
            }
        }

        drawLegend(canvas)
    }

    private fun drawLegend(canvas: Canvas) {
        val legendItems = listOf(
            Color.BLUE to "Below Min",
            Color.GREEN to "Within Range",
            Color.RED to "Above Max",
            Color.LTGRAY to "No Goal Set"
        )

        val boxSize = 30f
        val spacing = 20f
        val textPadding = 10f

        val totalItemWidth = legendItems.fold(0f) { acc, item ->
            acc + boxSize + textPadding + textPaint.measureText(item.second) + spacing
        }

        val startX = (width - totalItemWidth) / 2f
        val startY = height - 60f

        var x = startX
        for ((color, label) in legendItems) {
            paint.color = color
            canvas.drawRect(x, startY, x + boxSize, startY + boxSize, paint)
            canvas.drawText(label, x + boxSize + textPadding, startY + boxSize - 5f, textPaint)
            x += boxSize + textPadding + textPaint.measureText(label) + spacing
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
