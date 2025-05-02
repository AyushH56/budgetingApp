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

        val budgetRef = database.getReference("budgetGoals").child(userId)
        val transactionRef = database.getReference("users").child(userId).child("transactions")

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "Unknown"

        budgetRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(budgetSnapshot: DataSnapshot) {
                val budgets = budgetSnapshot.children.mapNotNull { budgetSnap ->
                    val category = budgetSnap.child("category").getValue(String::class.java) ?: return@mapNotNull null
                    val minBudget = budgetSnap.child("minBudget").getValue(Double::class.java) ?: 0.0
                    val maxBudget = budgetSnap.child("maxBudget").getValue(Double::class.java) ?: 0.0
                    Triple(category, minBudget, maxBudget)
                }

                transactionRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(transSnapshot: DataSnapshot) {
                        overviewItems.clear()

                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        for ((category, minBudget, maxBudget) in budgets) {
                            var totalSpent = 0.0

                            for (transSnap in transSnapshot.children) {
                                val transCategory = transSnap.child("category").getValue(String::class.java)?.trim()?.lowercase()
                                val transAmount = transSnap.child("amount").getValue(Double::class.java) ?: 0.0
                                val transDate = transSnap.child("date").getValue(String::class.java)

                                if (transDate != null) {
                                    try {
                                        val dateObj = sdf.parse(transDate)
                                        val cal = Calendar.getInstance()
                                        cal.time = dateObj!!

                                        val transMonth = cal.get(Calendar.MONTH) + 1
                                        val transYear = cal.get(Calendar.YEAR)

                                        if (transCategory == category.trim().lowercase() &&
                                            transMonth == currentMonth && transYear == currentYear) {
                                            totalSpent += transAmount
                                        }
                                    } catch (e: Exception) {
                                        // Log error or show a message if needed
                                        e.printStackTrace()
                                    }
                                }
                            }

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
                        Toast.makeText(this@OverviewActivity, "Failed to load transactions", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OverviewActivity, "Failed to load budget goals", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
