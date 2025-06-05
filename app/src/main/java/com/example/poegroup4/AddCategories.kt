package com.example.poegroup4

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.CategoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddCategories : BaseActivity() {

    private lateinit var categoryName: EditText
    private lateinit var categoryBudget: EditText
    private lateinit var saveButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryList: ArrayList<Categories>

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var selectedCategoryId: String? = null // For editing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_add_categories, findViewById(R.id.content_frame))
        supportActionBar?.title = "Add Categories"

        auth = FirebaseAuth.getInstance()
        categoryName = findViewById(R.id.edit_category_name)
        categoryBudget = findViewById(R.id.edtCatBudget)
        saveButton = findViewById(R.id.btn_save)
        recyclerView = findViewById(R.id.categoryRecyclerView)

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("categories").child(userId)

        categoryList = ArrayList()
        categoryAdapter = CategoryAdapter(
            categoryList,
            onEditClick = { category ->
                // Log to confirm edit categoryId
                Log.d("AddCategories", "Editing category ID: ${category.categoryId}")

                selectedCategoryId = category.categoryId
                categoryName.setText(category.catName)
                categoryBudget.setText(category.catBudget.toString())
                saveButton.text = "Update"
            },
            onDeleteClick = { category ->
                category.categoryId?.let {
                    databaseReference.child(it).removeValue()
                    Toast.makeText(this, "Category deleted!", Toast.LENGTH_SHORT).show()
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = categoryAdapter

        loadCategories()

        saveButton.setOnClickListener {
            saveOrUpdateCategory()
        }
    }

    private fun saveOrUpdateCategory() {
        val name = categoryName.text.toString().trim()
        val budgetInput = categoryBudget.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Category name is required", Toast.LENGTH_SHORT).show()
            return
        }

        val budget = budgetInput.toDoubleOrNull()
        if (budget == null || budget <= 0) {
            Toast.makeText(this, "Please enter a valid positive budget", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        // Use selectedCategoryId if editing, else generate new ID
        val categoryId = selectedCategoryId ?: databaseReference.push().key

        if (categoryId == null) {
            Toast.makeText(this, "Error generating ID", Toast.LENGTH_SHORT).show()
            return
        }

        val category = Categories(
            categoryId = categoryId,
            catName = name,
            catBudget = budget,
            userId = userId
        )

        databaseReference.child(categoryId).setValue(category).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (selectedCategoryId != null) {
                    Toast.makeText(this, "Category updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show()
                }
                categoryName.text.clear()
                categoryBudget.text.clear()

                // Reset after successful save/update
                selectedCategoryId = null
                saveButton.text = "Save"
            } else {
                Toast.makeText(this, "Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategories() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                for (categorySnap in snapshot.children) {
                    val category = categorySnap.getValue(Categories::class.java)
                    category?.let {
                        it.categoryId = categorySnap.key  // SET the categoryId here explicitly
                        categoryList.add(it)
                    }
                }
                categoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddCategories, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
