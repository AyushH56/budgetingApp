package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.R

class CategoriesAdapter (
    //private val categories: List<Category>
)//: RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {
//
//    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val nameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
//        val budgetTextView: TextView = itemView.findViewById(R.id.categoryBudgetTextView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.category_item_layout, parent, false)
//        return CategoryViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
//        val category = categories[position]
//        holder.nameTextView.text = category.name
//        holder.budgetTextView.text = "Budget: R${category.budget}"
//    }
//
//    override fun getItemCount(): Int = categories.size
//
//}