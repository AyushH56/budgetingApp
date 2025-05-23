package com.example.poegroup4

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.ExpenseAdapter
import com.example.poegroup4.adapters.SearchCategoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class SearchExpensesActivity : BaseActivity() {

    // UI components
    private lateinit var periodGroup: RadioGroup
    private lateinit var searchEdit: EditText
    private lateinit var expensesRV: RecyclerView
    private lateinit var totalsRV: RecyclerView

    // Firebase database reference
    private lateinit var db: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    // Data-related members
    private val allTx = mutableListOf<Transaction>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var cutoffDate: Date

    // Handler for debounce on search input
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var justReloaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_search_expenses, findViewById(R.id.content_frame))

        supportActionBar?.title = "Search Expenses"

        // Initialize UI elements
        periodGroup = findViewById(R.id.periodRadioGroup)
        searchEdit = findViewById(R.id.searchEditText)
        expensesRV = findViewById(R.id.expenseRecyclerView)
        totalsRV = findViewById(R.id.categoryTotalsRecyclerView)

        expensesRV.layoutManager = LinearLayoutManager(this)
        totalsRV.layoutManager = LinearLayoutManager(this)

        // Get reference to the user's transactions in Firebase
        db = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(auth.currentUser!!.uid)
            .child("transactions")

        // When user selects a time period, reload data
        periodGroup.setOnCheckedChangeListener { _, _ -> reloadAndDisplay() }

        // Add listener for search input with debounce
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Cancel any pending search tasks
                searchRunnable?.let { handler.removeCallbacks(it) }
                // Run a new search after delay
                searchRunnable = Runnable { displayFiltered() }
                handler.postDelayed(searchRunnable!!, 500) // Delay of 500ms
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Load transactions based on the selected time period
    private fun reloadAndDisplay() {
        val checked = periodGroup.checkedRadioButtonId

        if (checked == -1) {
            // Show error if no period is selected
            AlertDialog.Builder(this)
                .setTitle("Validation Error")
                .setMessage("Please select a time period before searching.")
                .setPositiveButton("OK", null)
                .show()
            allTx.clear()
            displayFiltered()
            return
        }

        // Determine cutoff date based on selected period
        val calendar = Calendar.getInstance()
        when (checked) {
            R.id.radioLastWeek -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            R.id.radioLastMonth -> calendar.add(Calendar.MONTH, -1)
            R.id.radioLastQuarter -> calendar.add(Calendar.MONTH, -3)
        }
        cutoffDate = calendar.time

        // Fetch all transactions from Firebase
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                allTx.clear()
                for (c in snap.children) {
                    c.getValue(Transaction::class.java)?.let {
                        allTx.add(it)
                    }
                }
                justReloaded = true
                displayFiltered()
            }

            override fun onCancelled(e: DatabaseError) {
                // Show error dialog if Firebase query fails
                AlertDialog.Builder(this@SearchExpensesActivity)
                    .setTitle("Database Error")
                    .setMessage("Failed to load transactions: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    // Filter and display transactions based on search input and selected time period
    private fun displayFiltered() {
        val query = searchEdit.text.toString().lowercase(Locale.getDefault())

        // Apply filters only if a time period is selected
        val filtered = if (periodGroup.checkedRadioButtonId == -1) {
            emptyList()
        } else {
            allTx.filter { transaction ->
                try {
                    val transactionDate = dateFormat.parse(transaction.date) ?: Date(0)
                    val matchesPeriod = transactionDate >= cutoffDate
                    val matchesSearch = query.isEmpty() || transaction.description.lowercase().contains(query)
                    matchesPeriod && matchesSearch
                } catch (e: Exception) {
                    false
                }
            }
        }

        // If no results found, show a dialog
        if (filtered.isEmpty() && periodGroup.checkedRadioButtonId != -1 && justReloaded) {
            AlertDialog.Builder(this)
                .setTitle("No Results")
                .setMessage("No transactions match your search.")
                .setPositiveButton("OK", null)
                .show()
            justReloaded = false
        }

        // Populate expenses list with filtered transactions
        expensesRV.adapter = ExpenseAdapter(filtered) { tx ->
            // If transaction has a photo, display it in a dialog
            tx.photoBase64?.let { b64 ->
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                AlertDialog.Builder(this)
                    .setTitle("Receipt")
                    .setView(ImageView(this).apply { setImageBitmap(bmp) })
                    .setPositiveButton("Close", null)
                    .show()
            }
        }

        // Calculate and display total per category
        val catTotals = filtered.groupBy { it.category.ifBlank { "Other" } }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        totalsRV.adapter = SearchCategoryAdapter(catTotals)
    }
}
