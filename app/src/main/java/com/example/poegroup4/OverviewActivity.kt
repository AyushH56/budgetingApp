package com.example.poegroup4

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.CategoryAdapter
import com.example.poegroup4.adapters.ExpenseAdapter
import com.example.poegroup4.adapters.SearchCategoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class OverviewActivity : BaseActivity() {

    private lateinit var db: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private lateinit var categoryRV: RecyclerView
    private lateinit var categoryBreakdownRV: RecyclerView
    private lateinit var expenseRV: RecyclerView
    private lateinit var totalsRV: RecyclerView
    private lateinit var totalSpentTextView: TextView
    private lateinit var categoryPeriodGroup: RadioGroup
    private lateinit var searchPeriodGroup: RadioGroup
    private lateinit var searchEdit: EditText

    private val allTransactions = mutableListOf<Transaction>()
    private var cutoffDate: Date = Date()
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var justReloaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_overview, findViewById(R.id.content_frame))

        supportActionBar?.title = "Overview"

        db = FirebaseDatabase.getInstance().reference.child("users").child(auth.currentUser!!.uid)

        categoryRV = findViewById(R.id.categoryRecyclerView)
        categoryBreakdownRV = findViewById(R.id.categoryBreakdownRecyclerView)
        expenseRV = findViewById(R.id.expenseRecyclerView)
        totalsRV = findViewById(R.id.categoryTotalsRecyclerView)
        totalSpentTextView = findViewById(R.id.totalSpentTextView)
        categoryPeriodGroup = findViewById(R.id.categoryPeriodRadioGroup)
        searchPeriodGroup = findViewById(R.id.searchPeriodRadioGroup)
        searchEdit = findViewById(R.id.searchEditText)

        categoryRV.layoutManager = LinearLayoutManager(this)
        categoryBreakdownRV.layoutManager = LinearLayoutManager(this)
        expenseRV.layoutManager = LinearLayoutManager(this)
        totalsRV.layoutManager = LinearLayoutManager(this)

        loadCategories()

        categoryPeriodGroup.setOnCheckedChangeListener { _, checkedId ->
            val period = when (checkedId) {
                R.id.categoryRadioLastWeek -> Calendar.DAY_OF_YEAR to -7
                R.id.categoryRadioLastMonth -> Calendar.MONTH to -1
                R.id.categoryRadioLastQuarter -> Calendar.MONTH to -3
                else -> null
            }

            period?.let {
                val cal = Calendar.getInstance().apply { add(it.first, it.second) }
                loadCategorySpending(cal.time)
            } ?: run {
                totalSpentTextView.text = "Please select a time period"
            }
        }

        searchPeriodGroup.setOnCheckedChangeListener { _, _ -> reloadAndDisplay() }

        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { handler.removeCallbacks(it) }
                searchRunnable = Runnable { displayFiltered() }
                handler.postDelayed(searchRunnable!!, 500)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadCategories() {
        db.child("categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<Categories>()
                for (child in snapshot.children) {
                    child.getValue(Categories::class.java)?.let {
                        categories.add(it)
                    }
                }
                categoryRV.adapter = CategoryAdapter(categories)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadCategorySpending(cutoff: Date) {
        val transactionsRef = db.child("transactions")
        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalSpent = 0.0
                val categoryMap = mutableMapOf<String, Double>()

                for (snap in snapshot.children) {
                    val tx = snap.getValue(Transaction::class.java)
                    if (tx != null) {
                        try {
                            val txDate = dateFormat.parse(tx.date)
                            if (txDate != null && txDate >= cutoff) {
                                val cat = tx.category.ifBlank { "Other" }
                                totalSpent += tx.amount
                                categoryMap[cat] = categoryMap.getOrDefault(cat, 0.0) + tx.amount
                            }
                        } catch (_: Exception) {}
                    }
                }

                totalSpentTextView.text =
                    if (categoryMap.isEmpty()) "No transactions found" else "Total Spent: R$totalSpent"
                categoryBreakdownRV.adapter = SearchCategoryAdapter(categoryMap)
            }

            override fun onCancelled(error: DatabaseError) {
                totalSpentTextView.text = "Failed to load data"
            }
        })
    }

    private fun reloadAndDisplay() {
        val checked = searchPeriodGroup.checkedRadioButtonId
        if (checked == -1) {
            AlertDialog.Builder(this)
                .setTitle("Validation Error")
                .setMessage("Please select a time period before searching.")
                .setPositiveButton("OK", null)
                .show()
            allTransactions.clear()
            displayFiltered()
            return
        }

        val calendar = Calendar.getInstance()
        when (checked) {
            R.id.searchRadioLastWeek -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            R.id.searchRadioLastMonth -> calendar.add(Calendar.MONTH, -1)
            R.id.searchRadioLastQuarter -> calendar.add(Calendar.MONTH, -3)
        }
        cutoffDate = calendar.time

        db.child("transactions").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allTransactions.clear()
                for (child in snapshot.children) {
                    child.getValue(Transaction::class.java)?.let {
                        allTransactions.add(it)
                    }
                }
                justReloaded = true
                displayFiltered()
            }

            override fun onCancelled(error: DatabaseError) {
                AlertDialog.Builder(this@OverviewActivity)
                    .setTitle("Database Error")
                    .setMessage("Failed to load transactions: ${error.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }

    private fun displayFiltered() {
        val query = searchEdit.text.toString().lowercase(Locale.getDefault())
        val filtered = if (searchPeriodGroup.checkedRadioButtonId == -1) {
            emptyList()
        } else {
            allTransactions.filter {
                try {
                    val date = dateFormat.parse(it.date) ?: Date(0)
                    date >= cutoffDate && (query.isEmpty() || it.description.lowercase().contains(query))
                } catch (e: Exception) {
                    false
                }
            }
        }

        if (filtered.isEmpty() && justReloaded) {
            AlertDialog.Builder(this)
                .setTitle("No Results")
                .setMessage("No transactions match your search.")
                .setPositiveButton("OK", null)
                .show()
            justReloaded = false
        }

        expenseRV.adapter = ExpenseAdapter(filtered) { tx ->
            tx.photoBase64?.let {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                AlertDialog.Builder(this)
                    .setTitle("Receipt")
                    .setView(ImageView(this).apply { setImageBitmap(bitmap) })
                    .setPositiveButton("Close", null)
                    .show()
            }
        }

        val totals = filtered.groupBy { it.category.ifBlank { "Other" } }
            .mapValues { it.value.sumOf { tx -> tx.amount } }

        totalsRV.adapter = SearchCategoryAdapter(totals)
    }
}
