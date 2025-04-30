package com.example.poegroup4

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import android.content.Intent
import java.util.*

class TransactionActivity : BaseActivity() {

    private lateinit var usercategories: Spinner
    private lateinit var editAmount: EditText
    private lateinit var editDescription: EditText
    private lateinit var textStartTime: TextView
    private lateinit var textEndTime: TextView
    private lateinit var textDate: TextView
    private lateinit var btnUploadPhoto: Button
    private lateinit var btnSaveExpense: Button

    private lateinit var database: DatabaseReference
    private lateinit var user: FirebaseUser

    private val categoryList = mutableListOf<String>()
    private var selectedPhotoUri: Uri? = null
    private var selectedDate: String? = null

    companion object {
        const val PICK_IMAGE_REQUEST = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_transactions, findViewById(R.id.content_frame))

        supportActionBar?.title = "Transactions"

        // Initialize views
        usercategories = findViewById(R.id.categorySpinner)
        editAmount = findViewById(R.id.edit_amount)
        editDescription = findViewById(R.id.edit_description)
        textStartTime = findViewById(R.id.text_start_time)
        textEndTime = findViewById(R.id.text_end_time)
        textDate = findViewById(R.id.text_date)
        btnUploadPhoto = findViewById(R.id.btn_upload_photo)
        btnSaveExpense = findViewById(R.id.btn_save_expense)

        // Initialize Firebase
        user = FirebaseAuth.getInstance().currentUser!!
        database = FirebaseDatabase.getInstance().reference

        loadCategories()

        // Set onClick listeners
        btnUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnSaveExpense.setOnClickListener {
            saveExpense()
        }

        textStartTime.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                textStartTime.text = String.format("%02d:%02d AM", hour, minute)
            }
        }

        textEndTime.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                textEndTime.text = String.format("%02d:%02d AM", hour, minute)
            }
        }

        textDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Set default values
        textStartTime.text = "9:00 AM"
        textEndTime.text = "10:00 AM"
        textDate.text = "Select Date"
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                textDate.text = selectedDate // Display selected date
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(onTimeSet: (hourOfDay: Int, minute: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute -> onTimeSet(selectedHour, selectedMinute) },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
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
                        val name =
                            categorySnap.child("catName").getValue(String::class.java)?.trim()
                        name?.let { categoryList.add(it) }
                    }

                    if (categoryList.isEmpty()) {
                        categoryList.add("No categories available")
                        usercategories.isEnabled = false
                    } else {
                        usercategories.isEnabled = true
                    }

                    val adapter = ArrayAdapter<String>(
                        this@TransactionActivity,
                        android.R.layout.simple_spinner_item,
                        categoryList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    usercategories.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@TransactionActivity,
                        "Failed to load categories",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun saveExpense() {
        val selectedCategory = usercategories.selectedItem?.toString()
        val amountText = editAmount.text.toString()
        val description = editDescription.text.toString()

        // Validation: Ensure amount and category are selected
        if (amountText.isBlank() || selectedCategory.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter an amount and select a category", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Convert amount to double and round it up
        val amount = amountText.toDouble()
        val roundedAmount = kotlin.math.ceil(amount)

        // Calculate emergency fund (difference between rounded and actual amount)
        val emergencyFund = roundedAmount - amount

        // Get start and end time from text views
        val startTime = textStartTime.text.toString()
        val endTime = textEndTime.text.toString()

        // Full description with category and the description entered
        val fullDescription = "$selectedCategory - $description"

        // If there's a photo selected, upload it first, else save transaction without photo
        if (selectedPhotoUri != null) {
            uploadPhotoAndSaveTransaction(
                amount,
                roundedAmount,
                emergencyFund,
                fullDescription,
                startTime,
                endTime,
                selectedPhotoUri!!
            )
        } else {
            // No photo selected, save transaction directly
            val transaction = Transaction(
                amount,
                roundedAmount,
                emergencyFund,
                fullDescription,
                startTime,
                endTime,
                null
            )
            saveTransactionToDatabase(transaction)
        }
    }

    private fun uploadPhotoAndSaveTransaction(
        amount: Double,
        roundedAmount: Double,
        emergencyFund: Double,
        description: String,
        startTime: String,
        endTime: String,
        photoUri: Uri
    ) {
        // Create a reference in Firebase Storage for the photo
        val photoRef = FirebaseStorage.getInstance().reference
            .child("transaction_photos/${System.currentTimeMillis()}.jpg")

        // Upload the photo to Firebase Storage
        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                // After the photo upload is complete, get the download URL
                photoRef.downloadUrl.addOnSuccessListener { uri ->
                    // Create a transaction object with the photo URL and other details
                    val transaction = Transaction(
                        amount,
                        roundedAmount,
                        emergencyFund,
                        description,
                        startTime,
                        endTime,
                        uri.toString()
                    )
                    saveTransactionToDatabase(transaction) // Save the transaction to Firebase Database
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Photo upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveTransactionToDatabase(transaction: Transaction) {
        // Get current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Reference to the 'transactions' node in Firebase
        val dbRef =
            FirebaseDatabase.getInstance().getReference("users").child(userId).child("transactions")

        // Generate a unique transaction ID using push()
        val transactionId = dbRef.push().key ?: return

        // Save the transaction under the user's transactions
        dbRef.child(transactionId)
            .setValue(transaction)
            .addOnSuccessListener {
                // If transaction is saved successfully
                Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after saving
            }
            .addOnFailureListener {
                // If transaction save fails
                Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show()
            }
    }
}
