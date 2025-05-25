package com.example.poegroup4

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.poegroup4.views.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsActivity : BaseActivity() {

    private lateinit var periodGroup: RadioGroup
    private lateinit var graphContainer: LinearLayout
    private lateinit var selectedDateText: TextView
    private lateinit var db: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private val allRecords = mutableListOf<SpendingRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_analytics, findViewById(R.id.content_frame))
        supportActionBar?.title = "Analytics"

        periodGroup = findViewById(R.id.periodRadioGroup)
        graphContainer = findViewById(R.id.graphContainer)
        selectedDateText = findViewById(R.id.selectedDateText)

        db = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(auth.currentUser!!.uid)
            .child("transactions")

        setupRadioGroup()
        fetchDataAndLoadGraphs("Last Week")
    }

    private fun setupRadioGroup() {
        periodGroup.setOnCheckedChangeListener { _, checkedId ->
            val period = when (checkedId) {
                R.id.radioLastWeek -> "Last Week"
                R.id.radioLastMonth -> "Last Month"
                R.id.radioLastQuarter -> "Last Quarter"
                else -> "Last Week"
            }
            fetchDataAndLoadGraphs(period)
        }
    }

    private fun fetchDataAndLoadGraphs(period: String) {
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allRecords.clear()
                for (transaction in snapshot.children) {
                    val category = transaction.child("category").getValue(String::class.java) ?: continue
                    val amount = transaction.child("amount").getValue(Double::class.java) ?: continue
                    val dateString = transaction.child("date").getValue(String::class.java) ?: continue

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = try {
                        dateFormat.parse(dateString)
                    } catch (e: Exception) {
                        null
                    } ?: continue

                    allRecords.add(SpendingRecord(category, amount, date))
                }

                loadGraphs(period)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AnalyticsActivity, "Failed to load transactions", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadGraphs(period: String) {
        graphContainer.removeAllViews()
        val records = filterRecords(allRecords, period)

        if (records.isEmpty()) {
            selectedDateText.text = "No data available for selected period"
            return
        }

        val dailySpendingGraph = DailyTrendLineView(this, records)
        val categoryGraph = CategoryBarChartView(this, records)
        val pieChart = CategoryPieChartView(this, records)

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        selectedDateText.text = "Data since ${dateFormat.format(records.first().date)}"

        listOf(
            createGraphBox("Spending Trend", dailySpendingGraph),
            createGraphBox("Category Spending", categoryGraph),
            createGraphBox("Category Distribution", pieChart)
        ).forEach { graphContainer.addView(it) }
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
}

data class SpendingRecord(val category: String, val amount: Double, val date: Date)
