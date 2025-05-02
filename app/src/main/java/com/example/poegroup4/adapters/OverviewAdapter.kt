package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.OverviewItem
import com.example.poegroup4.R

// Adapter class for displaying a list of overview items in a RecyclerView
class OverviewAdapter(private val overviewItems: List<OverviewItem>) :
    RecyclerView.Adapter<OverviewAdapter.ViewHolder>() {

    // Called when a new ViewHolder is needed (i.e., when there's no existing ViewHolder to reuse)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for a single overview item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_overview, parent, false)
        return ViewHolder(view)
    }

    // Binds the data from the overviewItems list to the views in each ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val overviewItem = overviewItems[position]

        // Set the category name
        holder.tvCategory.text = overviewItem.category
        // Set the month associated with the budget
        holder.tvMonthBudgeted.text = "Month: ${overviewItem.month}"
        // Display the minimum budget for the month
        holder.tvBudgetedAmount.text = "Budgeted: R${String.format("%.2f", overviewItem.minBudget)}"
        // Display the actual amount spent
        holder.tvSpentAmount.text = "Spent: R${String.format("%.2f", overviewItem.amountSpent)}"
        // Display the maximum allowed budget
        holder.tvMaxAmount.text = "Max Budget Goal: R${String.format("%.2f", overviewItem.maxBudget)}"
    }

    // Returns the total number of items in the dataset
    override fun getItemCount(): Int {
        return overviewItems.size
    }

    // ViewHolder class holds references to all views within a single item view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvMonthBudgeted: TextView = itemView.findViewById(R.id.tvMonthBudgeted)
        val tvBudgetedAmount: TextView = itemView.findViewById(R.id.tvBudgetedAmount)
        val tvSpentAmount: TextView = itemView.findViewById(R.id.tvSpentAmount)
        val tvMaxAmount: TextView = itemView.findViewById(R.id.tvMaxAmount)
    }
}
