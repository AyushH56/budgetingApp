package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.R


class SearchCategoryAdapter(
    private val categoryMap: Map<String, Double>
) : RecyclerView.Adapter<SearchCategoryAdapter.ViewHolder>() {

    // Define ViewHolder
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val categoryAmount: TextView = itemView.findViewById(R.id.categoryAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.activity_category_total, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryMap.keys.elementAt(position)
        val amount = categoryMap[category] ?: 0.0
        holder.categoryName.text = category
        holder.categoryAmount.text = "R$amount"
    }

    override fun getItemCount(): Int {
        return categoryMap.size
    }
}

