package com.example.poegroup4

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // Correct Toolbar import
import androidx.drawerlayout.widget.DrawerLayout

open class BaseActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_drawer)

        // Initialize Toolbar (Correctly using androidx.appcompat.widget.Toolbar)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar) // Correctly sets the Toolbar as the ActionBar

        // Initialize Drawer Layout
        drawerLayout = findViewById(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set Navigation Menu Items
        navList = findViewById(R.id.navList)
        val navItems = arrayOf(
            "Home Page",
            "Overview",
            "Categories",
            "Transactions",
            "Budget Goals",
            "Category Spending",
            "Search Expenses",
            "My Tree",
            "Analytics",
            "Progress Dashboard",
            "Logout"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, navItems)
        navList.adapter = adapter

        // Handle Navigation Item Clicks
        navList.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> startActivity(Intent(this, MainActivity::class.java))
                1 -> startActivity(Intent(this, OverviewActivity::class.java))
                2 -> startActivity(Intent(this, AddCategories::class.java))
                3 -> startActivity(Intent(this, TransactionActivity::class.java))
                4 -> startActivity(Intent(this, BudgetGoalsActivity::class.java))
                5 -> startActivity(Intent(this, SearchCategoryActivity::class.java))
                6 -> startActivity(Intent(this, SearchExpensesActivity::class.java))
                7 -> startActivity(Intent(this, MyTreeActivity::class.java))
                8 -> startActivity(Intent(this, AnalyticsActivity::class.java))
                9 -> startActivity(Intent(this, ProgressDashboardActivity::class.java))

                // POE Part 3 features (Coming Soon)
//                7 -> Toast.makeText(this, "My Tree (Coming Soon).", Toast.LENGTH_SHORT).show()
//                8 -> Toast.makeText(this, "Analytics (Coming Soon).", Toast.LENGTH_SHORT).show()
//                9 -> Toast.makeText(this, "Progress Dashboard (Coming Soon).", Toast.LENGTH_SHORT).show()

                // Done and Implemented Logout feature
                10 -> logoutUser()
            }
            drawerLayout.closeDrawers()
        }
    }

    private fun logoutUser() {
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)
    }
}