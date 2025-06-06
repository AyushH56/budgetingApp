package com.example.poegroup4

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class EmergencyFundHistoryActivity : BaseActivity() {

    private lateinit var tvEmergencyFundTotal: TextView
    private lateinit var periodGroup: RadioGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmergencyFundHistoryAdapter
    private val emergencyFundList = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate into BaseActivity’s content_frame
        layoutInflater.inflate(R.layout.activity_emergency_fund_history, findViewById(R.id.content_frame))

        supportActionBar?.title = "Emergency Fund History"

        tvEmergencyFundTotal = findViewById(R.id.tvEmergencyFundTotal)
        periodGroup = findViewById(R.id.periodRadioGroup)
        recyclerView = findViewById(R.id.recyclerEmergencyFundHistory)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EmergencyFundHistoryAdapter(emergencyFundList)
        recyclerView.adapter = adapter

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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("transactions")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                emergencyFundList.clear()

                val now = Calendar.getInstance()
                val startCalendar = Calendar.getInstance()

                when (period) {
                    "Last Week" -> startCalendar.add(Calendar.DAY_OF_YEAR, -7)
                    "Last Month" -> startCalendar.add(Calendar.MONTH, -1)
                    "Last Quarter" -> startCalendar.add(Calendar.MONTH, -3)
                }

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                var totalEmergencyFund = 0.0

                for (transSnap in snapshot.children) {
                    val emergencyFund = transSnap.child("emergencyFund").getValue(Double::class.java) ?: 0.0
                    val dateStr = transSnap.child("date").getValue(String::class.java) ?: continue
                    val description = transSnap.child("description").getValue(String::class.java) ?: "Unknown"
                    val amount = transSnap.child("amount").getValue(Double::class.java) ?: 0.0

                    val dateObj = try {
                        sdf.parse(dateStr)
                    } catch (e: Exception) {
                        null
                    } ?: continue

                    if (dateObj.before(startCalendar.time)) continue

                    if (emergencyFund > 0) {
                        emergencyFundList.add(
                            Transaction(
                                amount = amount,
                                emergencyFund = emergencyFund,
                                date = dateStr,
                                description = description
                            )
                        )
                        totalEmergencyFund += emergencyFund
                    }
                }

                tvEmergencyFundTotal.text = "Emergency Fund Total: R${"%.2f".format(totalEmergencyFund)}"
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EmergencyFundHistoryActivity, "Failed to load emergency fund data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
