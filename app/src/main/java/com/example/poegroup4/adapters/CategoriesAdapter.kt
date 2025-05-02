package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.Categories
import com.example.poegroup4.R

// Adapter class for displaying a list of category items in a RecyclerView
class CategoriesAdapter (
    private val categories: List<Categories> // List of category data to be displayed
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    // ViewHolder class holds references to the views for each data item
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val categoryBudgetTextView: TextView = itemView.findViewById(R.id.categoryBudgetTextView)
    }

    // Called when RecyclerView needs a new ViewHolder of the given type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // Inflate the layout for an individual category item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    // Binds the data to the views in each ViewHolder
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position] // Get category at current position
        holder.categoryNameTextView.text = category.catName // Set category name
        holder.categoryBudgetTextView.text = "Budget: R${category.catBudget}" // Set budget text
    }

    // Returns the total number of items in the dataset
    override fun getItemCount(): Int = categories.size
}
