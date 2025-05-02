package com.example.poegroup4

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class BudgetGoalsActivity : BaseActivity() {

    // UI components
    private lateinit var usercategories: Spinner
    private lateinit var minBudgetEditText: EditText
    private lateinit var maxBudgetEditText: EditText
    private lateinit var saveGoalButton: Button

    // Firebase references
    private lateinit var database: DatabaseReference
    private lateinit var user: FirebaseUser

    // Holds the user's category names
    private val categoryList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout into the base activity's content frame
        layoutInflater.inflate(R.layout.activity_budget_goals, findViewById(R.id.content_frame))

        // Link UI components to their XML counterparts
        usercategories = findViewById(R.id.categorySpinner)
        minBudgetEditText = findViewById(R.id.edit_min_goal)
        maxBudgetEditText = findViewById(R.id.edit_max_goal)
        saveGoalButton = findViewById(R.id.btn_save_goals)

        // Initialize Firebase references
        user = FirebaseAuth.getInstance().currentUser!!
        database = FirebaseDatabase.getInstance().reference

        // Load the list of categories for the user
        loadCategories()

        // Set up click listener to save budget goal
        saveGoalButton.setOnClickListener {
            saveGoal()
        }
    }

    // Load categories from the database and populate the Spinner
    private fun loadCategories() {
        val userId = user.uid
        database.child("categories")
            .child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryList.clear()
                    for (categorySnap in snapshot.children) {
                        val name = categorySnap.child("catName").getValue(String::class.java)?.trim()
                        name?.let { categoryList.add(it) }
                    }

                    // If no categories are found, redirect to AddCategories activity
                    if (categoryList.isEmpty()) {
                        Toast.makeText(
                            this@BudgetGoalsActivity,
                            "Please add a category first",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this@BudgetGoalsActivity, AddCategories::class.java)
                        startActivity(intent)
                        finish()
                        return
                    }

                    // Enable Spinner and populate it with categories
                    usercategories.isEnabled = true

                    // Adapter to show categories in the spinner with custom colors
                    val adapter = object : ArrayAdapter<String>(
                        this@BudgetGoalsActivity,
                        android.R.layout.simple_spinner_item,
                        categoryList
                    ) {
                        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                            val view = super.getView(position, convertView, parent) as TextView
                            view.setTextColor(Color.BLACK) // Spinner text color
                            return view
                        }

                        override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                            val view = super.getDropDownView(position, convertView, parent) as TextView
                            view.setTextColor(Color.WHITE) // Dropdown text color
                            return view
                        }
                    }

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    usercategories.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@BudgetGoalsActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Save budget goal to Firebase for the selected category
    private fun saveGoal() {

        // Check if categories are loaded and one is selected
        if (!usercategories.isEnabled || categoryList.isEmpty()) {
            Toast.makeText(this, "No valid category selected. Please add one first.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user input
        val selectedCategory = usercategories.selectedItem?.toString()
        val minBudgetText = minBudgetEditText.text.toString().trim()
        val maxBudgetText = maxBudgetEditText.text.toString().trim()

        // Validate fields
        if (selectedCategory.isNullOrEmpty() || minBudgetText.isEmpty() || maxBudgetText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert input to double
        val minBudget = minBudgetText.toDoubleOrNull()
        val maxBudget = maxBudgetText.toDoubleOrNull()

        // Validate budget values
        if (minBudget == null || maxBudget == null || minBudget < 0 || maxBudget < 0 || minBudget > maxBudget) {
            Toast.makeText(this, "Enter valid budget values (min ≤ max)", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate unique ID for goal
        val goalId = database.child("budgetGoals").child(user.uid).push().key!!

        // Create goal object
        val goal = BudgetGoals(
            category = selectedCategory,
            minBudget = minBudget,
            maxBudget = maxBudget
        )

        // Save to Firebase
        database.child("budgetGoals").child(user.uid).child(goalId).setValue(goal)
            .addOnSuccessListener {
                Toast.makeText(this, "Goal saved successfully", Toast.LENGTH_SHORT).show()
                finish() // Close activity after successful save
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show()
            }
    }
}
