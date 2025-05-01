package com.example.poegroup4

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
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

        usercategories = findViewById(R.id.categorySpinner)
        editAmount = findViewById(R.id.edit_amount)
        editDescription = findViewById(R.id.edit_description)
        textStartTime = findViewById(R.id.text_start_time)
        textEndTime = findViewById(R.id.text_end_time)
        textDate = findViewById(R.id.text_date)
        btnUploadPhoto = findViewById(R.id.btn_upload_photo)
        btnSaveExpense = findViewById(R.id.btn_save_expense)

        user = FirebaseAuth.getInstance().currentUser!!
        database = FirebaseDatabase.getInstance().reference

        loadCategories()

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
                textStartTime.text = String.format("%02d:%02d", hour, minute)
            }
        }

        textEndTime.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                textEndTime.text = String.format("%02d:%02d", hour, minute)
            }
        }

        textDate.setOnClickListener {
            showDatePickerDialog()
        }

        textStartTime.text = "09:00"
        textEndTime.text = "10:00"
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
                textDate.text = selectedDate
            },
            year, month, day
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
            hour, minute, false
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
                        val name = categorySnap.child("catName").getValue(String::class.java)?.trim()
                        name?.let { categoryList.add(it) }
                    }

                    if (categoryList.isEmpty()) {
                        Toast.makeText(this@TransactionActivity, "Please add a category before adding transactions.", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@TransactionActivity, AddCategories::class.java))
                        finish() // Prevents return to transaction screen without categories
                        return
                    }

                    val adapter = ArrayAdapter(
                        this@TransactionActivity,
                        android.R.layout.simple_spinner_item,
                        categoryList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    usercategories.adapter = adapter
                    usercategories.isEnabled = true
                    btnSaveExpense.isEnabled = true
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TransactionActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun saveExpense() {
        val selectedCategory = usercategories.selectedItem?.toString()
        val amountText = editAmount.text.toString().trim()
        val description = editDescription.text.toString().trim()
        val startTime = textStartTime.text.toString().trim()
        val endTime = textEndTime.text.toString().trim()
        val date = selectedDate

        if (selectedCategory.isNullOrBlank()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        if (amountText.isBlank()) {
            editAmount.error = "Enter amount"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            editAmount.error = "Amount must be a positive number"
            return
        }

        if (date.isNullOrBlank()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        if (startTime.isBlank() || endTime.isBlank()) {
            Toast.makeText(this, "Please select a start and end time", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isBlank()) {
            editDescription.error = "Enter a description"
            return
        }

        val roundedAmount = kotlin.math.ceil(amount)
        val emergencyFund = roundedAmount - amount

        if (selectedPhotoUri != null) {
            encodePhotoAndSaveTransaction(amount, roundedAmount, emergencyFund, selectedCategory, description, startTime, endTime, date, selectedPhotoUri!!)
        } else {
            val transaction = Transaction(
                amount = amount,
                roundedAmount = roundedAmount,
                emergencyFund = emergencyFund,
                category = selectedCategory,
                description = description,
                startTime = startTime,
                endTime = endTime,
                date = date,
                photoBase64 = null,
                timestamp = System.currentTimeMillis()
            )
            saveTransactionToDatabase(transaction)
        }
    }


    private fun encodePhotoAndSaveTransaction(
        amount: Double,
        roundedAmount: Double,
        emergencyFund: Double,
        category: String,
        description: String,
        startTime: String,
        endTime: String,
        date: String,
        photoUri: Uri
    ) {
        val photoBase64 = encodeImageToBase64(photoUri)

        val transaction = Transaction(
            amount = amount,
            roundedAmount = roundedAmount,
            emergencyFund = emergencyFund,
            category = category,
            description = description,
            startTime = startTime,
            endTime = endTime,
            date = date,
            photoBase64 = photoBase64,
            timestamp = System.currentTimeMillis()
        )

        saveTransactionToDatabase(transaction)
    }

    private fun encodeImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveTransactionToDatabase(transaction: Transaction) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("transactions")

        val transactionId = dbRef.push().key ?: return

        dbRef.child(transactionId)
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
