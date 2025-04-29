package com.example.poegroup4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BudgetAdapter(private val budgetList: List<Budget>) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        // Inflate the item layout (item_budget.xml) for each RecyclerView item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        // Bind the budget data to the views in item_budget.xml
        val budget = budgetList[position]
        holder.category.text = budget.category
        holder.budgetedAmount.text = "Budgeted: ${budget.budgetedAmount}"
        holder.spentAmount.text = "Spent: ${budget.spentAmount}"
        holder.availableAmount.text = "Available: ${budget.availableAmount}"
    }

    override fun getItemCount(): Int = budgetList.size

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Connect the fields to the views in item_budget.xml
        val category: TextView = itemView.findViewById(R.id.tvCategory)
        val budgetedAmount: TextView = itemView.findViewById(R.id.tvBudgetedAmount)
        val spentAmount: TextView = itemView.findViewById(R.id.tvSpentAmount)
        val availableAmount: TextView = itemView.findViewById(R.id.tvAvailableAmount)
    }
}