package com.example.poegroup4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.CategoriesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

//import com.google.firebase.database.

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
//        setContentView(R.layout.activity_add_categories)

        // Inflating the layout properly
        layoutInflater.inflate(R.layout.activity_add_categories, findViewById(R.id.content_frame))

        // Set toolbar title
        supportActionBar?.title = "Add Categories"

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Initialize other views
        categoryName = findViewById(R.id.edit_category_name)
        categoryBudget = findViewById(R.id.edtCatBudget)
        saveButton = findViewById(R.id.btn_save)
        recyclerView = findViewById(R.id.categoryRecyclerView)

        // Firebase user validation
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            finish()  // Redirect to login screen or handle accordingly
            return
        }

        // Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("categories").child(userId)

        // Initialize list and adapter
        categoryList = ArrayList()
        categoryAdapter = CategoriesAdapter(categoryList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = categoryAdapter

        saveButton.setOnClickListener {
            saveCategory()
        }
    }

    private fun saveCategory() {
        val name = categoryName.text.toString()
        val budget = categoryBudget.text.toString().toDoubleOrNull()

        if (name.isNotEmpty() && budget != null) {
            val userId = auth.currentUser?.uid ?: return
            val categoryId = databaseReference.push().key
            val category = Categories(name, budget, userId)

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
        } else {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
        }
    }
}


//    private fun loadCategories() {
//        val userId = auth.currentUser?.uid ?: return
//        databaseReference.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                categoryList.clear()
//                for (categorySnap in snapshot.children) {
//                    val category = categorySnap.getValue(Categories::class.java)
//                    category?.let { categoryList.add(it) }
//                }
//                categoryAdapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@AddCategories, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//}
