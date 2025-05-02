package com.example.poegroup4

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.OverviewAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class OverviewActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OverviewAdapter
    private val overviewItems = mutableListOf<OverviewItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_overview, findViewById(R.id.content_frame))

        supportActionBar?.title = "Overview"

        recyclerView = findViewById(R.id.recyclerOverview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OverviewAdapter(overviewItems)
        recyclerView.adapter = adapter

        fetchOverviewData()
    }

    private fun fetchOverviewData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()

        val budgetRef = database.getReference("budgetGoals").child(userId)
        val transactionRef = database.getReference("users").child(userId).child("transactions")

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "Unknown"

        // Listen for real-time changes
        val categories = mutableSetOf<String>()

        transactionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(transSnapshot: DataSnapshot) {
                overviewItems.clear()

                // Gather all unique categories from transactions
                for (transSnap in transSnapshot.children) {
                    transSnap.child("category").getValue(String::class.java)?.let {
                        categories.add(it)
                    }
                }

                budgetRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(budgetSnapshot: DataSnapshot) {
                        val budgetMap = mutableMapOf<String, Pair<Double, Double>>() // category to min/max

                        for (budgetSnap in budgetSnapshot.children) {
                            val category = budgetSnap.child("category").getValue(String::class.java) ?: continue
                            val minBudget = budgetSnap.child("minBudget").getValue(Double::class.java) ?: 0.0
                            val maxBudget = budgetSnap.child("maxBudget").getValue(Double::class.java) ?: 0.0
                            budgetMap[category] = Pair(minBudget, maxBudget)
                            categories.add(category)
                        }

                        for (category in categories) {
                            var totalSpent = 0.0

                            for (transSnap in transSnapshot.children) {
                                val transCategory = transSnap.child("category").getValue(String::class.java)
                                val transAmount = transSnap.child("amount").getValue(Double::class.java) ?: 0.0
                                val transDate = transSnap.child("date").getValue(String::class.java)

                                if (transCategory == category && transDate != null) {
                                    val parts = transDate.split("/")
                                    if (parts.size == 3) {
                                        val month = parts[1].toIntOrNull()
                                        val year = parts[2].toIntOrNull()
                                        if (month == currentMonth && year == currentYear) {
                                            totalSpent += transAmount
                                        }
                                    }
                                }
                            }

                            val (minBudget, maxBudget) = budgetMap[category] ?: Pair(0.0, 0.0)
                            overviewItems.add(
                                OverviewItem(
                                    category = category,
                                    month = currentMonthName,
                                    minBudget = minBudget,
                                    maxBudget = maxBudget,
                                    amountSpent = totalSpent
                                )
                            )
                        }

                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@OverviewActivity, "Failed to load budget goals", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OverviewActivity, "Failed to load transactions", Toast.LENGTH_SHORT).show()
            }
        })
    }



}
