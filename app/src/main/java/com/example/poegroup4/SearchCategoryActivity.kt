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
import java.util.Calendar

class SearchCategoryActivity : BaseActivity()
{
    private lateinit var periodRadioGroup: RadioGroup
    private lateinit var totalSpentTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflating the layout properly
        layoutInflater.inflate(R.layout.activity_category_spending, findViewById(R.id.content_frame))

        periodRadioGroup = findViewById(R.id.periodRadioGroup)
        totalSpentTextView = findViewById(R.id.totalSpentTextView)
        recyclerView = findViewById(R.id.categoryBreakdownRecyclerView)
        database = FirebaseDatabase.getInstance().reference

        recyclerView.layoutManager = LinearLayoutManager(this)

        periodRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedPeriod = when (checkedId) {
                R.id.radioLastWeek -> Calendar.DAY_OF_YEAR to -7
                R.id.radioLastMonth -> Calendar.MONTH to -1
                R.id.radioLastQuarter -> Calendar.MONTH to -3
                else -> null
            }

            selectedPeriod?.let {
                val cutoff = Calendar.getInstance()
                cutoff.add(it.first, it.second)
                fetchTransactionsSince(cutoff.timeInMillis)
            }
        }
    }

    private fun fetchTransactionsSince(cutoffTime: Long) {
        val userId = auth.currentUser?.uid ?: return
        val transactionsRef = database.child("users").child(userId).child("transactions")

        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalSpent = 0.0
                val categoryMap = mutableMapOf<String, Double>()

                for (transactionSnap in snapshot.children) {
                    val transaction = transactionSnap.getValue(Transaction::class.java)
                    if (transaction != null && transaction.timestamp >= cutoffTime) {
                        totalSpent += transaction.amount

                        // Extract category from description
                        val descriptionParts = transaction.description.split(" - ")
                        val category = descriptionParts.getOrNull(0)?.trim() ?: "Other"

                        categoryMap[category] = categoryMap.getOrDefault(category, 0.0) + transaction.amount
                    }
                }

                totalSpentTextView.text = "Total Spent: R$totalSpent"

                // Set the RecyclerView adapter with the category breakdown
                recyclerView.adapter = SearchCategoryAdapter(categoryMap)
            }

            override fun onCancelled(error: DatabaseError) {
                totalSpentTextView.text = "Failed to load data"
            }
        })
    }

}