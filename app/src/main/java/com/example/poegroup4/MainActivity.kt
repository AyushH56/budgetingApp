package com.example.poegroup4

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    // Declare RecyclerView variable
    private lateinit var recyclerView: RecyclerView

    // List of cards to display on the home screen, each with a title and icon
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

        // Initialize RecyclerView and set up layout and adapter
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns in grid
        recyclerView.adapter = CardAdapter(cards) { title ->

            // Handle click on each card based on its title
            when (title) {
                "Overview" -> startActivity(Intent(this, OverviewActivity::class.java))
                "Categories" -> startActivity(Intent(this, AddCategories::class.java))
                "Transactions" -> startActivity(Intent(this, TransactionActivity::class.java))
                "Budget Goals" -> startActivity(Intent(this, BudgetGoalsActivity::class.java))
                "Category Spending" -> startActivity(Intent(this, SearchCategoryActivity::class.java))
                "Search Expenses" -> startActivity(Intent(this, SearchExpensesActivity::class.java))
                "My Tree" -> startActivity(Intent(this, MyTreeActivity::class.java))
                "Analytics" -> startActivity(Intent(this, AnalyticsActivity::class.java))
                "Progress Dashboard" -> startActivity(Intent(this, ProgressDashboard::class.java))

                // For features not yet implemented, show a "Coming Soon" toast
//                "My Tree",
//                "Analytics",
//                "Progress Dashboard" -> {
//                    Toast.makeText(this, "$title (Coming Soon).", Toast.LENGTH_SHORT).show()
//                }

                // If user selects "Logout", call logoutUser() method
                "Logout" -> logoutUser()
            }
        }
    }

    // Logs the user out and navigates back to the login screen
    private fun logoutUser() {
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Close the MainActivity so user can’t navigate back using the back button
    }
}
