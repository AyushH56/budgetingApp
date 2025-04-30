package com.example.poegroup4

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class TransactionActivity : BaseActivity() {

    private lateinit var editAmount: EditText
    private lateinit var editDescription: EditText
    private lateinit var textStartTime: TextView
    private lateinit var textEndTime: TextView
    private lateinit var btnUploadPhoto: Button
    private lateinit var btnSaveExpense: Button
    private lateinit var radioGroupCategories: RadioGroup

    private var selectedPhotoUri: Uri? = null

    companion object {
        const val PICK_IMAGE_REQUEST = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_transactions, findViewById(R.id.content_frame))

        supportActionBar?.title = "Transactions"

        // Bind views
        editAmount = findViewById(R.id.edit_amount)
        editDescription = findViewById(R.id.edit_description)
        textStartTime = findViewById(R.id.text_start_time)
        textEndTime = findViewById(R.id.text_end_time)
        btnUploadPhoto = findViewById(R.id.btn_upload_photo)
        btnSaveExpense = findViewById(R.id.btn_save_expense)
        radioGroupCategories = findViewById(R.id.radio_group_categories)

        btnUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnSaveExpense.setOnClickListener {
            saveExpense()
        }

        // Set listeners for the time pickers
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

        // Dummy time
        textStartTime.text = "9:00 AM"
        textEndTime.text = "10:00 AM"
    }

    private fun showTimePickerDialog(onTimeSet: (hourOfDay: Int, minute: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                onTimeSet(selectedHour, selectedMinute)
            },
            hour,
            minute,
            false // false for 12-hour format
        )

        timePickerDialog.show()
    }

    private fun saveExpense() {
        val amountText = editAmount.text.toString()
        val description = editDescription.text.toString()
        val selectedCategoryId = radioGroupCategories.checkedRadioButtonId

        if (amountText.isBlank() || selectedCategoryId == -1) {
            Toast.makeText(this, "Please enter an amount and select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = findViewById<RadioButton>(selectedCategoryId).text.toString()
        val fullDescription = "$selectedCategory - $description"

        val amount = amountText.toDouble()
        val roundedAmount = kotlin.math.ceil(amount)
        val emergencyFund = roundedAmount - amount
        val startTime = textStartTime.text.toString()
        val endTime = textEndTime.text.toString()

        if (selectedPhotoUri != null) {
            uploadPhotoAndSaveTransaction(amount, roundedAmount, emergencyFund, fullDescription, startTime, endTime, selectedPhotoUri!!)
        } else {
            val transaction = Transaction(amount, roundedAmount, emergencyFund, fullDescription, startTime, endTime, null)
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
        val photoRef = FirebaseStorage.getInstance().reference
            .child("transaction_photos/${System.currentTimeMillis()}.jpg")

        photoRef.putFile(photoUri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { uri ->
                    val transaction = Transaction(amount, roundedAmount, emergencyFund, description, startTime, endTime, uri.toString())
                    saveTransactionToDatabase(transaction)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Photo upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveTransactionToDatabase(transaction: Transaction) {
        val dbRef = FirebaseDatabase.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val transactionId = dbRef.push().key ?: return

        dbRef.child("users")
            .child(userId)
            .child("transactions")
            .child(transactionId)
            .setValue(transaction)
            .addOnSuccessListener {
                Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedPhotoUri = data?.data
            Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show()
        }
    }
}
