package com.example.poegroup4

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.adapters.ExpenseAdapter
import com.example.poegroup4.adapters.SearchCategoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Locale

class SearchExpensesActivity : BaseActivity() {

    private lateinit var periodGroup: RadioGroup
    private lateinit var searchEdit: EditText
    private lateinit var expensesRV: RecyclerView
    private lateinit var totalsRV: RecyclerView
    private lateinit var db: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    private val allTx = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_search_expenses)

        layoutInflater.inflate(R.layout.activity_search_expenses, findViewById(R.id.content_frame))

        supportActionBar?.title = "Search Expenses"

        periodGroup = findViewById(R.id.periodRadioGroup)
        searchEdit = findViewById(R.id.searchEditText)
        expensesRV = findViewById(R.id.expenseRecyclerView)
        totalsRV = findViewById(R.id.categoryTotalsRecyclerView)

        expensesRV.layoutManager = LinearLayoutManager(this)
        totalsRV.layoutManager = LinearLayoutManager(this)

        db = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(auth.currentUser!!.uid)
            .child("transactions")

        periodGroup.setOnCheckedChangeListener { _, _ -> reloadAndDisplay() }

        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                reloadAndDisplay()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun reloadAndDisplay() {
        val checked = periodGroup.checkedRadioButtonId

        // Validation: check if a time period was selected
        if (checked == -1) {
            AlertDialog.Builder(this)
                .setTitle("Validation Error")
                .setMessage("Please select a time period before searching.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val now = System.currentTimeMillis()
        val cutoff = Calendar.getInstance().apply {
            when (checked) {
                R.id.radioLastWeek -> add(Calendar.DAY_OF_YEAR, -7)
                R.id.radioLastMonth -> add(Calendar.MONTH, -1)
                R.id.radioLastQuarter -> add(Calendar.MONTH, -3)
            }
        }.timeInMillis

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                allTx.clear()

                if (!snap.exists()) {
                    AlertDialog.Builder(this@SearchExpensesActivity)
                        .setTitle("No Data")
                        .setMessage("No transactions found for the selected period.")
                        .setPositiveButton("OK", null)
                        .show()
                    displayFiltered()
                    return
                }

                for (c in snap.children) {
                    c.getValue(Transaction::class.java)?.let {
                        if (it.timestamp >= cutoff) allTx.add(it)
                    }
                }

                if (allTx.isEmpty()) {
                    AlertDialog.Builder(this@SearchExpensesActivity)
                        .setTitle("No Results")
                        .setMessage("No transactions match your criteria.")
                        .setPositiveButton("OK", null)
                        .show()
                }

                displayFiltered()
            }

            override fun onCancelled(e: DatabaseError) {
                AlertDialog.Builder(this@SearchExpensesActivity)
                    .setTitle("Database Error")
                    .setMessage("Failed to load transactions: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        })
    }


    private fun displayFiltered() {
        val q = searchEdit.text.toString().lowercase(Locale.getDefault())
        val filtered = allTx.filter {
            it.description.lowercase().contains(q)
        }

        // 1) show expenses
        expensesRV.adapter = ExpenseAdapter(filtered) { tx ->
            // on photo click: decode and show
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

        // 2) show category totals
        val catTotals = filtered.groupBy { it.description.split(" - ").firstOrNull() ?: "Other" }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        totalsRV.adapter = SearchCategoryAdapter(catTotals)
    }
}
