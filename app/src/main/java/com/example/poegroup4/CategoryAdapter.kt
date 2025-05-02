package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.Categories
import com.example.poegroup4.R

class CategoryAdapter(private val categories: List<Categories>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // ViewHolder class to hold references to the views inside the item layout
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Access the TextViews inside the CardView
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val categoryBudgetTextView: TextView = itemView.findViewById(R.id.categoryBudgetTextView)
    }

    // This function inflates the layout and returns the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    // This function binds data to the views in each item
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        // Set the category name and budget into the respective TextViews
        holder.categoryNameTextView.text = category.catName
        holder.categoryBudgetTextView.text = "Budget: R${category.catBudget}"
    }

    // This function returns the total number of items in the list
    override fun getItemCount(): Int {
        return categories.size
    }
}

