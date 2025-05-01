package com.example.poegroup4

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.SearchCategoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SearchCategoryActivity : BaseActivity() {
    private lateinit var periodRadioGroup: RadioGroup
    private lateinit var totalSpentTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutInflater.inflate(R.layout.activity_category_spending, findViewById(R.id.content_frame))

        supportActionBar?.title = "Search Category Spending"

        periodRadioGroup = findViewById(R.id.periodRadioGroup)
        totalSpentTextView = findViewById(R.id.totalSpentTextView)
        recyclerView = findViewById(R.id.categoryBreakdownRecyclerView)
        database = FirebaseDatabase.getInstance().reference

        recyclerView.layoutManager = LinearLayoutManager(this)

        periodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            recyclerView.adapter = null
            totalSpentTextView.text = "Loading..."

            val selectedPeriod = when (checkedId) {
                R.id.radioLastWeek -> Calendar.DAY_OF_YEAR to -7
                R.id.radioLastMonth -> Calendar.MONTH to -1
                R.id.radioLastQuarter -> Calendar.MONTH to -3
                else -> null
            }

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

    private fun fetchTransactions(cutoffDate: Date) {
        val userId = auth.currentUser?.uid ?: return
        val transactionsRef = database.child("users").child(userId).child("transactions")

        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalSpent = 0.0
                val categoryMap = mutableMapOf<String, Double>()

                for (transactionSnap in snapshot.children) {
                    val transaction = transactionSnap.getValue(Transaction::class.java)
                    if (transaction != null) {
                        try {
                            val transactionDate = dateFormat.parse(transaction.date)
                            if (transactionDate != null && transactionDate >= cutoffDate) {
                                val category = transaction.category.ifBlank { "Other" }
                                totalSpent += transaction.amount
                                categoryMap[category] = categoryMap.getOrDefault(category, 0.0) + transaction.amount
                            }
                        } catch (e: Exception) {
                            // Skip transactions with invalid date format
                        }
                    }
                }

                if (categoryMap.isEmpty()) {
                    totalSpentTextView.text = "No transactions found for this period"
                    recyclerView.adapter = null
                } else {
                    totalSpentTextView.text = "Total Spent: R$totalSpent"
                    recyclerView.adapter = SearchCategoryAdapter(categoryMap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                totalSpentTextView.text = "Failed to load data"
                recyclerView.adapter = null
            }
        })
    }
}