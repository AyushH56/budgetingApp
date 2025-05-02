package com.example.poegroup4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.CategoriesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddCategories : BaseActivity() {

    // Declare UI elements and data variables
    private lateinit var categoryName: EditText
    private lateinit var categoryBudget: EditText
    private lateinit var saveButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoriesAdapter
    private lateinit var categoryList: ArrayList<Categories>

    // Firebase references
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout inside base activity's content frame
        layoutInflater.inflate(R.layout.activity_add_categories, findViewById(R.id.content_frame))

        // Set the title of the action bar
        supportActionBar?.title = "Add Categories"

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        categoryName = findViewById(R.id.edit_category_name)
        categoryBudget = findViewById(R.id.edtCatBudget)
        saveButton = findViewById(R.id.btn_save)
        recyclerView = findViewById(R.id.categoryRecyclerView)

        // Get current user ID
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // If user is not authenticated, show message and exit
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Create database reference under the "categories" node for this user
        databaseReference = FirebaseDatabase.getInstance().getReference("categories").child(userId)

        // Initialize category list and adapter
        categoryList = ArrayList()
        categoryAdapter = CategoriesAdapter(categoryList)

        // Set up RecyclerView with a linear layout
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = categoryAdapter

        // Load existing categories from database
        loadCategories()

        // Set save button click listener to save new category
        saveButton.setOnClickListener {
            saveCategory()
        }
    }

    // Save a new category to the Firebase Realtime Database
    private fun saveCategory() {
        val name = categoryName.text.toString().trim()
        val budgetInput = categoryBudget.text.toString().trim()

        // Validate category name
        if (name.isEmpty()) {
            Toast.makeText(this, "Category name is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate and convert budget input to a Double
        val budget = budgetInput.toDoubleOrNull()
        if (budget == null || budget <= 0) {
            Toast.makeText(this, "Please enter a valid positive budget", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val categoryId = databaseReference.push().key // Generate a unique key for the category

        // Create a new Categories object
        val category = Categories(
            catName = name,
            catBudget = budget,
            userId = userId
        )

        // Save the category to the database under the generated key
        categoryId?.let {
            databaseReference.child(it).setValue(category).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show()
                    categoryName.text.clear()
                    categoryBudget.text.clear()
                } else {
                    Toast.makeText(this, "Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Load categories from Firebase in real-time and update the RecyclerView
    private fun loadCategories() {
        val userId = auth.currentUser?.uid ?: return

        // Listen for data changes in the "categories" node
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear() // Clear existing list
                for (categorySnap in snapshot.children) {
                    val category = categorySnap.getValue(Categories::class.java)
                    category?.let { categoryList.add(it) } // Add non-null categories to the list
                }
                categoryAdapter.notifyDataSetChanged() // Refresh RecyclerView
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddCategories, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
