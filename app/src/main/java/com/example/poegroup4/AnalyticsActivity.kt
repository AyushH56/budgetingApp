package com.example.poegroup4

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.poegroup4.views.*
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AnalyticsActivity : BaseActivity() {

    private lateinit var periodGroup: RadioGroup
    private lateinit var graphContainer: LinearLayout
    private lateinit var selectedDateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_analytics, findViewById(R.id.content_frame))
        supportActionBar?.title = "Analytics"

        periodGroup = findViewById(R.id.periodRadioGroup)
        graphContainer = findViewById(R.id.graphContainer)
        selectedDateText = findViewById(R.id.selectedDateText)

        setupRadioGroup()
        loadGraphs("Last Week")
    }

    private fun setupRadioGroup() {
        periodGroup.setOnCheckedChangeListener { _, checkedId ->
            val period = when (checkedId) {
                R.id.radioLastWeek -> "Last Week"
                R.id.radioLastMonth -> "Last Month"
                R.id.radioLastQuarter -> "Last Quarter"
                else -> "Last Week"
            }
            loadGraphs(period)
        }
    }

    private fun loadGraphs(period: String) {
        graphContainer.removeAllViews()
        val records = filterRecords(generateDummyRecords(), period)

        // Create graph views with data
        val dailySpendingGraph = DailyTrendLineView(this, records)
        val categoryGraph = CategoryBarChartView(this, records)
        val pieChart = CategoryPieChartView(this, records)

        // Update date range info
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        selectedDateText.text = "Data since ${dateFormat.format(records.firstOrNull()?.date ?: Date())}"

        // Add graphs wrapped in labeled boxes with proper height and margin
        listOf(
            createGraphBox("Spending Trend", dailySpendingGraph),
            createGraphBox("Category Spending", categoryGraph),
            createGraphBox("Category Distribution", pieChart)
        ).forEach { graphContainer.addView(it) }
    }

    private fun createGraphBox(title: String, graph: View): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.ic_analytics_drawing_background)

            val titleText = TextView(context).apply {
                text = title
                textSize = 16f
                setTextColor(Color.BLACK)
                setPadding(0, 0, 0, 16)
            }

            addView(titleText)

            // Set layout params on graph for proper sizing
            graph.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                900
            )
            addView(graph)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(-16, 0, -16, 32)
        }

        box.layoutParams = params
        return box
    }

    private fun filterRecords(records: List<SpendingRecord>, period: String): List<SpendingRecord> {
        val calendar = Calendar.getInstance()
        when (period) {
            "Last Week" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "Last Month" -> calendar.add(Calendar.MONTH, -1)
            "Last Quarter" -> calendar.add(Calendar.MONTH, -3)
        }
        val startDate = calendar.time
        return records.filter { it.date.after(startDate) }
    }

    // Dummy Data, need to change to pull from Firebase database
    private fun generateDummyRecords(): List<SpendingRecord> {
        val categories = listOf("Food", "Transport", "Entertainment", "Rent", "Utilities")
        val now = Calendar.getInstance()
        val list = mutableListOf<SpendingRecord>()
        repeat(90) {
            now.add(Calendar.DAY_OF_YEAR, -1)
            val amount = (50..500).random().toDouble()
            val category = categories.random()
            list.add(SpendingRecord(category, amount, now.time))
        }
        return list
    }
}

data class SpendingRecord(val category: String, val amount: Double, val date: Date)
