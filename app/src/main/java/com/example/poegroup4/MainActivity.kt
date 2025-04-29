package com.example.poegroup4

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : BaseActivity() { // Extends BaseActivity
    private lateinit var btnOpenDatePicker: Button
    private lateinit var tvSelectedMonth: TextView
    private lateinit var recyclerViewBudget: RecyclerView
    private lateinit var etBudgetGoal: EditText
    private lateinit var etEmergencyFund: EditText
    private lateinit var btnSaveBudget: Button

    private var selectedMonth = Calendar.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate content into BaseActivity's content_frame
        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.content_frame))

        // Set Toolbar Title
        supportActionBar?.title = "Home Page"

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("BudgetData", MODE_PRIVATE)

        // Initialize UI Components
        btnOpenDatePicker = findViewById(R.id.btnOpenDatePicker)
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        recyclerViewBudget = findViewById(R.id.recyclerViewBudget)
        etBudgetGoal = findViewById(R.id.etBudgetGoal)
        etEmergencyFund = findViewById(R.id.etEmergencyFund)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)

        // Set RecyclerView
        recyclerViewBudget.layoutManager = LinearLayoutManager(this)
        recyclerViewBudget.adapter = BudgetAdapter(getDummyBudgetData())

        // Load previously saved data
        loadSavedBudget()

        // DatePicker button logic
        btnOpenDatePicker.setOnClickListener {
            openMonthPicker()
        }

        // Save Budget button logic
        btnSaveBudget.setOnClickListener {
            saveBudgetData()
        }
    }

    private fun openMonthPicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, _ ->
                selectedMonth.set(Calendar.YEAR, year)
                selectedMonth.set(Calendar.MONTH, month)

                val sdf = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
                tvSelectedMonth.text = sdf.format(selectedMonth.time) // Update the TextView dynamically
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun saveBudgetData() {
        val budgetGoal = etBudgetGoal.text.toString().trim()
        val emergencyFund = etEmergencyFund.text.toString().trim()

        if (budgetGoal.isEmpty() || emergencyFund.isEmpty()) {
            etBudgetGoal.error = "Enter a valid budget goal"
            etEmergencyFund.error = "Enter a valid emergency fund"
            return
        }

        // Save data locally using SharedPreferences
        with(sharedPreferences.edit()) {
            putString("BudgetGoal", budgetGoal)
            putString("EmergencyFund", emergencyFund)
            apply()
        }

        // Confirmation message
        Toast.makeText(this, "Budget saved successfully!", Toast.LENGTH_SHORT).show()

        // Update UI for testing purposes
        loadSavedBudget()
    }

    private fun loadSavedBudget() {
        // Retrieve data from SharedPreferences
        val budgetGoal = sharedPreferences.getString("BudgetGoal", "")
        val emergencyFund = sharedPreferences.getString("EmergencyFund", "")

        // Update the input fields with the saved data (optional, for testing)
        etBudgetGoal.setText(budgetGoal)
        etEmergencyFund.setText(emergencyFund)
    }

    private fun getDummyBudgetData(): List<Budget> {
        return listOf(
            Budget("Groceries", 500.0, 300.0, "200.0"),
            Budget("Rent", 1000.0, 0.0, "1000.0"),
            Budget("Utilities", 200.0, 50.0, "150.0")
        )
    }
}