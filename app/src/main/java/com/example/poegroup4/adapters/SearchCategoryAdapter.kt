package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.R

// Adapter for displaying a map of categories and their respective amounts in a RecyclerView
class SearchCategoryAdapter(
    private val categoryMap: Map<String, Double> // Map of category names to their amounts
) : RecyclerView.Adapter<SearchCategoryAdapter.ViewHolder>() {

    // ViewHolder holds the views for a single category item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)   // Text for the category name
        val categoryAmount: TextView = itemView.findViewById(R.id.categoryAmount) // Text for the amount associated with the category
    }

    // Inflates the item layout and creates a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_category_total, parent, false) // Inflate the layout for category items
        return ViewHolder(itemView) // Return the created ViewHolder
    }

    // Binds the data (category and amount) to the views in the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryMap.keys.elementAt(position) // Get the category at the current position
        val amount = categoryMap[category] ?: 0.0 // Get the amount for the category, default to 0 if null
        holder.categoryName.text = category // Set the category name text
        holder.categoryAmount.text = "R${amount}" // Set the amount text, formatted as currency
    }

    // Returns the number of items in the map (category-count pairs)
    override fun getItemCount(): Int {
        return categoryMap.size
    }
}
