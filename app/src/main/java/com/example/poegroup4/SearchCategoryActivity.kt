package com.example.poegroup4

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.SearchCategoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class SearchCategoryActivity : BaseActivity() {

    // UI components
    private lateinit var periodRadioGroup: RadioGroup
    private lateinit var totalSpentTextView: TextView
    private lateinit var recyclerView: RecyclerView

    // Firebase references
    private lateinit var database: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    // Date formatter to match transaction date format
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout into the BaseActivity's content frame
        layoutInflater.inflate(R.layout.activity_category_spending, findViewById(R.id.content_frame))

        // Set the ActionBar title
        supportActionBar?.title = "Search Category Spending"

        // Initialize views
        periodRadioGroup = findViewById(R.id.periodRadioGroup)
        totalSpentTextView = findViewById(R.id.totalSpentTextView)
        recyclerView = findViewById(R.id.categoryBreakdownRecyclerView)
        database = FirebaseDatabase.getInstance().reference

        // Set layout manager for the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Listener for selecting a time period
        periodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            recyclerView.adapter = null
            totalSpentTextView.text = "Loading..."

            // Determine the cutoff period based on selected radio button
            val selectedPeriod = when (checkedId) {
                R.id.radioLastWeek -> Calendar.DAY_OF_YEAR to -7
                R.id.radioLastMonth -> Calendar.MONTH to -1
                R.id.radioLastQuarter -> Calendar.MONTH to -3
                else -> null
            }

            // Calculate the cutoff date and fetch transactions
            selectedPeriod?.let {
                val cutoffDate = Calendar.getInstance().apply {
                    add(it.first, it.second)
                }
                fetchTransactions(cutoffDate.time)
            } ?: run {
                totalSpentTextView.text = "Please select a time period"
            }
        }
    }

    // Fetch and filter transactions from Firebase based on cutoffDate
    private fun fetchTransactions(cutoffDate: Date) {
        val userId = auth.currentUser?.uid ?: return
        val transactionsRef = database.child("users").child(userId).child("transactions")

        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalSpent = 0.0
                val categoryMap = mutableMapOf<String, Double>() // Map to store amount per category

                for (transactionSnap in snapshot.children) {
                    val transaction = transactionSnap.getValue(Transaction::class.java)
                    if (transaction != null) {
                        try {
                            val transactionDate = dateFormat.parse(transaction.date)
                            // Include transaction if it falls within the selected time frame
                            if (transactionDate != null && transactionDate >= cutoffDate) {
                                val category = transaction.category.ifBlank { "Other" }
                                totalSpent += transaction.amount
                                // Sum amounts by category
                                categoryMap[category] =
                                    categoryMap.getOrDefault(category, 0.0) + transaction.amount
                            }
                        } catch (e: Exception) {
                            // Ignore transactions with invalid dates
                        }
                    }
                }

                // Display results
                if (categoryMap.isEmpty()) {
                    totalSpentTextView.text = "No transactions found for this period"

                    recyclerView.adapter = null
                } else {
                    totalSpentTextView.text = "Total Spent: R${totalSpent}"
                    recyclerView.adapter = SearchCategoryAdapter(categoryMap)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle database read error
                totalSpentTextView.text = "Failed to load data"
                recyclerView.adapter = null
            }
        })
    }
    }

