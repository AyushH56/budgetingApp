package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.Categories
import com.example.poegroup4.R

class CategoryAdapter(
    private val categories: List<Categories>,
    private val onEditClick: (Categories) -> Unit,
    private val onDeleteClick: (Categories) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.category_name)
        val categoryBudgetTextView: TextView = itemView.findViewById(R.id.category_budget)
        val editButton: Button = itemView.findViewById(R.id.btn_edit)
        val deleteButton: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item_layout, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryNameTextView.text = category.catName
        holder.categoryBudgetTextView.text = "Budget: R${category.catBudget}"

        holder.editButton.setOnClickListener {
            onEditClick(category)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(category)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}
