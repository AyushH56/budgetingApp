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

    // UI components and data adapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OverviewAdapter

    // List that holds items to display in the overview
    private val overviewItems = mutableListOf<OverviewItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate this activity into the BaseActivity layout
        layoutInflater.inflate(R.layout.activity_overview, findViewById(R.id.content_frame))

        // Set toolbar title
        supportActionBar?.title = "Overview"

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerOverview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set adapter to the RecyclerView
        adapter = OverviewAdapter(overviewItems)
        recyclerView.adapter = adapter

        // Fetch data from Firebase to display
        fetchOverviewData()
    }

    /**
     * Retrieves the user's budget goals and transaction data for the current month
     * from Firebase, then calculates total spending per category and updates the RecyclerView.
     */
    private fun fetchOverviewData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()

        // References to Firebase nodes
        val budgetRef = database.getReference("budgetGoals").child(userId)
        val transactionRef = database.getReference("users").child(userId).child("transactions")

        // Get current month and year for filtering
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // January = 0
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "Unknown"

        // Read budget data for the user
        budgetRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(budgetSnapshot: DataSnapshot) {
                val budgets = budgetSnapshot.children.mapNotNull { budgetSnap ->
                    val category = budgetSnap.child("category").getValue(String::class.java) ?: return@mapNotNull null
                    val minBudget = budgetSnap.child("minBudget").getValue(Double::class.java) ?: 0.0
                    val maxBudget = budgetSnap.child("maxBudget").getValue(Double::class.java) ?: 0.0
                    Triple(category, minBudget, maxBudget)
                }

                // Now retrieve transaction data for matching categories
                transactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(transSnapshot: DataSnapshot) {
                        overviewItems.clear() // Clear existing data

                        for ((category, minBudget, maxBudget) in budgets) {
                            var totalSpent = 0.0

                            // Sum transaction amounts in the same category for current month/year
                            for (transSnap in transSnapshot.children) {
                                val transCategory = transSnap.child("category").getValue(String::class.java)
                                val transAmount = transSnap.child("amount").getValue(Double::class.java) ?: 0.0
                                val transDate = transSnap.child("date").getValue(String::class.java)

                                // Check if category and date match
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

                            // Add the result to the list
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

                        // Notify adapter to refresh RecyclerView
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Show error if transaction retrieval fails
                        Toast.makeText(this@OverviewActivity, "Failed to load transactions", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // Show error if budget retrieval fails
                Toast.makeText(this@OverviewActivity, "Failed to load budget goals", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
