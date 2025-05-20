package com.example.poegroup4

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
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
    private var photoUri: Uri? = null
    private var currentPhotoPath: String = ""
    private var selectedDate: String? = null

    companion object {
        const val PICK_IMAGE_REQUEST = 101
        const val CAMERA_REQUEST_CODE = 102

        const val PERMISSION_REQUEST_CODE = 200
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
            checkPermissions()
            showImagePickerDialog()
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

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> dispatchTakePictureIntent()
                1 -> openGallery()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    photoUri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    selectedPhotoUri = photoUri
                    Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show()
                }
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        selectedPhotoUri = uri
                        Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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
                        categoryList.add("No categories available")
                        usercategories.isEnabled = false
                    } else {
                        usercategories.isEnabled = true
                    }

                    val adapter = ArrayAdapter(
                        this@TransactionActivity,
                        android.R.layout.simple_spinner_item,
                        categoryList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    usercategories.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TransactionActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveExpense() {
        val selectedCategory = usercategories.selectedItem?.toString()
        val amountText = editAmount.text.toString()
        val description = editDescription.text.toString()

        if (amountText.isBlank() || selectedCategory.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter an amount and select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDouble()
        val roundedAmount = kotlin.math.ceil(amount)
        val emergencyFund = roundedAmount - amount
        val startTime = textStartTime.text.toString()
        val endTime = textEndTime.text.toString()
        val category = selectedCategory
        val date = selectedDate ?: ""

        if (selectedPhotoUri != null) {
            encodePhotoAndSaveTransaction(amount, roundedAmount, emergencyFund, category, description, startTime, endTime, date, selectedPhotoUri!!)
        } else {
            val transaction = Transaction(
                amount = amount,
                roundedAmount = roundedAmount,
                emergencyFund = emergencyFund,
                category = category,
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
        var inputStream = contentResolver.openInputStream(uri)
        return try {
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
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
}