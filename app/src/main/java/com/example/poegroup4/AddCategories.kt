package com.example.poegroup4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddCategories : AppCompatActivity()
{
    private lateinit var categoryName: EditText
    private lateinit var categoryBudget: EditText
    private lateinit var saveButton: Button
    //private lateinit var databaseReference: DatabaseReference
    //private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_categories)

        categoryName = findViewById(R.id.edtCatName)
        categoryBudget = findViewById(R.id.edtCatBudget)
        saveButton = findViewById(R.id.btn_save)

        //auth = FirebaseAuth.getInstance()
        //databaseReference = FirebaseDatabase.getInstance().getReference("categories")

        saveButton.setOnClickListener {
            //saveCategory()
        }
    }

//    private fun saveCategory() {
//        val name = categoryName.text.toString()
//        val budget = categoryBudget.text.toString().toDoubleOrNull()
//
//        if (name.isNotEmpty() && budget != null) {
//            val userId = auth.currentUser?.uid ?: return // Get current user ID (from Login)
//            val categoryId = databaseReference.push().key // Auto-generated ID
//            val category = Category(name, budget, userId)
//
//            categoryId?.let {
//                databaseReference.child(userId ?: "defaultUser") // Store categories under user ID
//                    .child(it) // Store category with auto-generated ID
//                    .setValue(category)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show()
//                            finish() // Go back after adding
//                        } else {
//                            Toast.makeText(this, "Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//            }
//        } else {
//            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
//        }
//    }

}