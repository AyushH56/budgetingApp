package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.R
import com.example.poegroup4.Transaction

// Adapter for displaying a list of expense transactions in a RecyclerView
class ExpenseAdapter(
    private var expenses: List<Transaction>, // List of transactions (expenses)
    private val onPhotoClick: (Transaction) -> Unit // Lambda for handling photo icon clicks
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    // ViewHolder holds the views for a single expense item
    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryText: TextView = itemView.findViewById(R.id.categoryText)         // Text for category name
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)   // Text for description
        val amountText: TextView = itemView.findViewById(R.id.amountText)             // Text for amount spent
        val photoIcon: ImageView = itemView.findViewById(R.id.photoIcon)              // Icon shown if photo is attached
    }

    // Inflates the item layout and creates a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    // Binds the data to the views in each ViewHolder
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = expenses[position] // Get the transaction at this position

        // Set the text views with transaction data
        holder.categoryText.text = item.category
        holder.descriptionText.text = item.description
        holder.amountText.text = "R${item.amount}"

        // Show photo icon if a photo is available, and set a click listener
        if (!item.photoBase64.isNullOrEmpty()) {
            holder.photoIcon.visibility = View.VISIBLE
            holder.photoIcon.setOnClickListener {
                onPhotoClick(item) // Trigger the lambda to handle the click
            }
        } else {
            // Hide the photo icon if no photo is attached
            holder.photoIcon.visibility = View.GONE
        }
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int = expenses.size

    // Updates the adapter's data list and refreshes the view
    fun updateList(newList: List<Transaction>) {
        expenses = newList
        notifyDataSetChanged()
    }
}
