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

    private lateinit var categoryName: EditText
    private lateinit var categoryBudget: EditText
    private lateinit var saveButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoriesAdapter
    private lateinit var categoryList: ArrayList<Categories>

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

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
        categoryAdapter = CategoriesAdapter(categoryList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = categoryAdapter

        loadCategories()

        saveButton.setOnClickListener {
            saveCategory()
        }
    }

    private fun saveCategory() {
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
        val categoryId = databaseReference.push().key

        val category = Categories(
            catName = name,
            catBudget = budget,
            userId = userId
        )

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

    private fun loadCategories() {
        val userId = auth.currentUser?.uid ?: return
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                for (categorySnap in snapshot.children) {
                    val category = categorySnap.getValue(Categories::class.java)
                    category?.let { categoryList.add(it) }
                }
                categoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddCategories, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
