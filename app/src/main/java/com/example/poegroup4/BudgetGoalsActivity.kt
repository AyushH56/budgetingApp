package com.example.poegroup4

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BudgetGoalsActivity : BaseActivity()
{

    private lateinit var usercategories: Spinner
    private lateinit var minBudgetEditText: EditText
    private lateinit var maxBudgetEditText: EditText
    private lateinit var saveGoalButton: Button

    private lateinit var database: DatabaseReference
    private lateinit var user: FirebaseUser

    private val categoryList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_budget_goals, findViewById(R.id.content_frame))

        usercategories = findViewById(R.id.categorySpinner)
        minBudgetEditText = findViewById(R.id.edit_min_goal)
        maxBudgetEditText = findViewById(R.id.edit_max_goal)
        saveGoalButton = findViewById(R.id.btn_save_goals)

        user = FirebaseAuth.getInstance().currentUser!!
        database = FirebaseDatabase.getInstance().reference

        loadCategories()

        saveGoalButton.setOnClickListener {
            saveGoal()
        }
    }


    private fun loadCategories() {
        val userId = user.uid
        database.child("categories")
            .child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryList.clear()
                    for (categorySnap in snapshot.children) {
                        // Trim whitespace in catName
                        val name = categorySnap.child("catName").getValue(String::class.java)?.trim()
                        name?.let { categoryList.add(it) }
                    }

                    if (categoryList.isEmpty()) {
                        categoryList.add("No categories available")
                        usercategories.isEnabled = false
                    } else {
                        usercategories.isEnabled = true
                    }


                    val adapter = object : ArrayAdapter<String>(
                        this@BudgetGoalsActivity,
                        android.R.layout.simple_spinner_item,
                        categoryList
                    ) {
                        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                            val view = super.getView(position, convertView, parent) as TextView
                            view.setTextColor(Color.BLACK)  // Set text color to black
                            return view
                        }

                        override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                            val view = super.getDropDownView(position, convertView, parent) as TextView
                            view.setTextColor(Color.WHITE)  // Set text color to white
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



    private fun saveGoal() {
        val selectedCategory = usercategories.selectedItem?.toString()
        val minBudgetText = minBudgetEditText.text.toString().trim()
        val maxBudgetText = maxBudgetEditText.text.toString().trim()

        if (selectedCategory.isNullOrEmpty() || minBudgetText.isEmpty() || maxBudgetText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val minBudget = minBudgetText.toDoubleOrNull()
        val maxBudget = maxBudgetText.toDoubleOrNull()

        if (minBudget == null || maxBudget == null || minBudget < 0 || maxBudget < 0 || minBudget > maxBudget) {
            Toast.makeText(this, "Enter valid budget values (min ≤ max)", Toast.LENGTH_SHORT).show()
            return
        }

        val goalId = database.child("budgetGoals").child(user.uid).push().key!!

        val goal = BudgetGoals(
            category = selectedCategory,
            minBudget = minBudget,
            maxBudget = maxBudget
        )

        database.child("budgetGoals").child(user.uid).child(goalId).setValue(goal)
            .addOnSuccessListener {
                Toast.makeText(this, "Goal saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show()
            }
    }
}