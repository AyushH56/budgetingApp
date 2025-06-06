package com.example.poegroup4

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProgressDashboard : BaseActivity() {

    private lateinit var pieChart: PieChart
    private val categoryColorMap = mutableMapOf<String, Int>()
    private lateinit var container: LinearLayout
    private lateinit var database: FirebaseDatabase
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(
            R.layout.activity_progress_dashboard,
            findViewById(R.id.content_frame)
        )

        supportActionBar?.title = "Progress Dashboard"

        pieChart = findViewById(R.id.pieChart)
        container = findViewById(R.id.progressContainer)

        database = FirebaseDatabase.getInstance()

        fetchBudgetAndSpending()
    }

    private fun fetchBudgetAndSpending() {
        val budgetRef = database.getReference("budgetGoals").child(userId)
        val transactionRef = database.getReference("users").child(userId).child("transactions")

        budgetRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(budgetSnapshot: DataSnapshot) {
                val budgetMap =
                    mutableMapOf<String, Pair<Double, Double>>() // category -> (min, max)

                for (budget in budgetSnapshot.children) {
                    val category = budget.child("category").getValue(String::class.java) ?: continue
                    val min = budget.child("minBudget").getValue(Double::class.java) ?: 0.0
                    val max = budget.child("maxBudget").getValue(Double::class.java) ?: 0.0
                    budgetMap[category.lowercase()] = Pair(min, max)
                }

                transactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(transSnapshot: DataSnapshot) {
                        val spendMap = mutableMapOf<String, Double>()

                        for (transaction in transSnapshot.children) {
                            val category =
                                transaction.child("category").getValue(String::class.java)
                                    ?.lowercase() ?: continue
                            val amount =
                                transaction.child("amount").getValue(Double::class.java) ?: 0.0
                            spendMap[category] = spendMap.getOrDefault(category, 0.0) + amount
                        }

                        drawProgressBars(budgetMap, spendMap)
                        drawPieChart(spendMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@ProgressDashboard,
                            "Failed to load transactions",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProgressDashboard, "Failed to load budgets", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun drawProgressBars(
        budgetMap: Map<String, Pair<Double, Double>>,
        spendMap: Map<String, Double>
    ) {
        container.removeAllViews()

        for ((category, spent) in spendMap) {
            val budget = budgetMap[category] ?: continue
            val maxBudget = budget.second

            val progressText = TextView(this)
            progressText.text = "$category: R${spent} / R${maxBudget}"
            progressText.setTextColor(Color.BLACK)
            progressText.textSize = 16f

            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            progressBar.max = maxBudget.toInt()
            progressBar.progress = 0
            progressBar.progressDrawable = getDrawable(R.drawable.progress_drawable)

            progressBar.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                40
            )

            if (spent > maxBudget) {
                progressBar.progressTintList = getColorStateList(android.R.color.holo_red_dark)
            }

            progressBar.animate().setDuration(1000).withEndAction {
                progressBar.progress = spent.toInt()
            }.start()

            container.addView(progressText)
            container.addView(progressBar)
        }
    }

    private fun drawPieChart(spendMap: Map<String, Double>) {
        if (spendMap.isEmpty()) {
            Toast.makeText(this, "No spending data to display in chart", Toast.LENGTH_SHORT).show()
            return
        }

        val entries = mutableListOf<PieEntry>()
        val categoryColors = mutableListOf<Int>()

        for ((category, amount) in spendMap) {
            entries.add(PieEntry(amount.toFloat(), ""))
            categoryColors.add(generateColorFromCategory(category))
        }

        val dataSet = PieDataSet(entries, "")
        val colors = spendMap.keys.map { generateColorFromCategory(it) }
        dataSet.colors = colors
        dataSet.setDrawValues(false)

        // Pie chart styling
        pieChart.setUsePercentValues(false)
        pieChart.setDrawHoleEnabled(true)
        pieChart.holeRadius = 55f
        pieChart.transparentCircleRadius = 60f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "Your Spending"
        pieChart.setCenterTextSize(18f)
        pieChart.setCenterTextColor(Color.DKGRAY)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()

        // Custom vertical legend
        val legendContainer = findViewById<LinearLayout>(R.id.legendContainer)
        legendContainer.removeAllViews()
        legendContainer.orientation = LinearLayout.VERTICAL

        val totalSpending = spendMap.values.sum()

        spendMap.entries.forEach { (category, amount) ->
            val percent = if (totalSpending > 0) (amount / totalSpending) * 100 else 0.0
            val labelText =
                "${category.replaceFirstChar { it.uppercaseChar() }}: R%.2f (%.1f%%)".format(
                    amount,
                    percent
                )

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 8, 0, 8)
            }

            val colorBox = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(30, 30).apply {
                    rightMargin = 20
                }
                setBackgroundColor(generateColorFromCategory(category))

            }

            val label = TextView(this).apply {
                text = labelText
                setTextColor(Color.BLACK)
                textSize = 14f
            }

            row.addView(colorBox)
            row.addView(label)
            legendContainer.addView(row)
        }
    }

    private fun generateColorFromCategory(category: String): Int {
        // If color already assigned, return it
        categoryColorMap[category]?.let { return it }

     
        val index = categoryColorMap.size
        val hue = (index * 137.508f) % 360f
        val saturation = 0.9f
        val brightness = 1.0f

        val hsv = floatArrayOf(hue, saturation, brightness)
        val color = Color.HSVToColor(hsv)
        categoryColorMap[category] = color
        return color
    }



}




