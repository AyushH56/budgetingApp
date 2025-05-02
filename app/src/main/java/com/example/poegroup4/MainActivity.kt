package com.example.poegroup4

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val cards = listOf(
        HomeCard("Overview", R.drawable.ic_overview),
        HomeCard("Categories", R.drawable.ic_category),
        HomeCard("Transactions", R.drawable.ic_transactions),
        HomeCard("Budget Goals", R.drawable.ic_goals),
        HomeCard("Category Spending", R.drawable.ic_search),
        HomeCard("Search Expenses", R.drawable.ic_expenses),
        HomeCard("My Tree", R.drawable.ic_tree),
        HomeCard("Analytics", R.drawable.ic_analytics),
        HomeCard("Progress Dashboard", R.drawable.ic_dashboard),
        HomeCard("Logout", R.drawable.ic_logout),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = CardAdapter(cards) { title ->
            when (title) {
                "Overview" -> startActivity(Intent(this, OverviewActivity::class.java))
                "Categories" -> startActivity(Intent(this, AddCategories::class.java))
                "Transactions" -> startActivity(Intent(this, TransactionActivity::class.java))
                "Budget Goals" -> startActivity(Intent(this, BudgetGoalsActivity::class.java))
                "Category Spending" -> startActivity(Intent(this, SearchCategoryActivity::class.java))
                "Search Expenses" -> startActivity(Intent(this, SearchExpensesActivity::class.java))

                // POE Part 3 features (Coming Soon)
                "My Tree",
                "Analytics",
                "Progress Dashboard" -> {
                    Toast.makeText(this, "$title (Coming Soon).", Toast.LENGTH_SHORT).show()
                }

                "Logout" -> logoutUser()
            }
        }
    }

    private fun logoutUser() {
        // Show a toast and log the user out
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()

        // Redirect to the LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))

        // Close the current MainActivity
        finish()
    }
}



// Old MainActivity.kt
//package com.example.poegroup4
//
//import android.app.DatePickerDialog
//import android.content.SharedPreferences
//import android.os.Bundle
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import android.widget.Toast
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Locale
//
//class MainActivity : BaseActivity() { // Extends BaseActivity
//    private lateinit var btnOpenDatePicker: Button
//    private lateinit var tvSelectedMonth: TextView
//    private lateinit var recyclerViewBudget: RecyclerView
//    private lateinit var etBudgetGoal: EditText
//    private lateinit var etEmergencyFund: EditText
//    private lateinit var btnSaveBudget: Button
//
//    private var selectedMonth = Calendar.getInstance()
//    private lateinit var sharedPreferences: SharedPreferences
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Inflate content into BaseActivity's content_frame
//        layoutInflater.inflate(R.layout.activity_main, findViewById(R.id.content_frame))
//
//        // Set Toolbar Title
//        supportActionBar?.title = "Home Page"
//
//        // Initialize SharedPreferences
//        sharedPreferences = getSharedPreferences("BudgetData", MODE_PRIVATE)
//
//        // Initialize UI Components
//        btnOpenDatePicker = findViewById(R.id.btnOpenDatePicker)
//        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
//        recyclerViewBudget = findViewById(R.id.recyclerViewBudget)
//        etBudgetGoal = findViewById(R.id.etBudgetGoal)
//        etEmergencyFund = findViewById(R.id.etEmergencyFund)
//        btnSaveBudget = findViewById(R.id.btnSaveBudget)
//
//        // Set RecyclerView
//        recyclerViewBudget.layoutManager = LinearLayoutManager(this)
//        recyclerViewBudget.adapter = BudgetAdapter(getDummyBudgetData())
//
//        // Load previously saved data
//        loadSavedBudget()
//
//        // DatePicker button logic
//        btnOpenDatePicker.setOnClickListener {
//            openMonthPicker()
//        }
//
//        // Save Budget button logic
//        btnSaveBudget.setOnClickListener {
//            saveBudgetData()
//        }
//    }
//
//    private fun openMonthPicker() {
//        val calendar = Calendar.getInstance()
//
//        val datePickerDialog = DatePickerDialog(
//            this,
//            { _, year, month, _ ->
//                selectedMonth.set(Calendar.YEAR, year)
//                selectedMonth.set(Calendar.MONTH, month)
//
//                val sdf = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
//                tvSelectedMonth.text = sdf.format(selectedMonth.time)
//            },
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DAY_OF_MONTH)
//        )
//
//        // Allow only future months
//        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
//
//        datePickerDialog.show()
//    }
//
//    private fun saveBudgetData() {
//        val budgetGoal = etBudgetGoal.text.toString().trim()
//        val emergencyFund = etEmergencyFund.text.toString().trim()
//
//        if (budgetGoal.isEmpty() || emergencyFund.isEmpty()) {
//            etBudgetGoal.error = "Enter a valid budget goal"
//            etEmergencyFund.error = "Enter a valid emergency fund"
//            return
//        }
//
//        // Save data locally using SharedPreferences
//        with(sharedPreferences.edit()) {
//            putString("BudgetGoal", budgetGoal)
//            putString("EmergencyFund", emergencyFund)
//            apply()
//        }
//
//        // Confirmation message
//        Toast.makeText(this, "Budget saved successfully!", Toast.LENGTH_SHORT).show()
//
//        // Update UI for testing purposes
//        loadSavedBudget()
//    }
//
//    private fun loadSavedBudget() {
//        // Retrieve data from SharedPreferences
//        val budgetGoal = sharedPreferences.getString("BudgetGoal", "")
//        val emergencyFund = sharedPreferences.getString("EmergencyFund", "")
//
//        // Update the input fields with the saved data (optional, for testing)
//        etBudgetGoal.setText(budgetGoal)
//        etEmergencyFund.setText(emergencyFund)
//    }
//
//    private fun getDummyBudgetData(): List<Budget> {
//        return listOf(
//            Budget("Groceries", 500.0, 300.0, "200.0"),
//            Budget("Rent", 1000.0, 0.0, "1000.0"),
//            Budget("Utilities", 200.0, 50.0, "150.0")
//        )
//    }
//}