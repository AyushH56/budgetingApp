package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.OverviewItem
import com.example.poegroup4.R

class OverviewAdapter(private val overviewItems: List<OverviewItem>) :
    RecyclerView.Adapter<OverviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_overview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val overviewItem = overviewItems[position]
        holder.tvCategory.text = overviewItem.category
        holder.tvMonthBudgeted.text = "Month: ${overviewItem.month}"
        holder.tvBudgetedAmount.text = "Budgeted: R${String.format("%.2f", overviewItem.minBudget)}"
        holder.tvSpentAmount.text = "Spent: R${String.format("%.2f", overviewItem.amountSpent)}"
        holder.tvMaxAmount.text = "Max Budget Goal: R${String.format("%.2f", overviewItem.maxBudget)}"
    }

    override fun getItemCount(): Int {
        return overviewItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvMonthBudgeted: TextView = itemView.findViewById(R.id.tvMonthBudgeted)
        val tvBudgetedAmount: TextView = itemView.findViewById(R.id.tvBudgetedAmount)
        val tvSpentAmount: TextView = itemView.findViewById(R.id.tvSpentAmount)
        val tvMaxAmount: TextView = itemView.findViewById(R.id.tvMaxAmount)
    }
}