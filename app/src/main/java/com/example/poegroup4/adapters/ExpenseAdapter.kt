package com.example.poegroup4.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poegroup4.R
import com.example.poegroup4.Transaction

class ExpenseAdapter(
    private var expenses: List<Transaction>,
    private val onPhotoClick: (Transaction) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val amountText: TextView = itemView.findViewById(R.id.amountText)
        val photoIcon: ImageView = itemView.findViewById(R.id.photoIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = expenses[position]
        holder.categoryText.text = item.category
        holder.descriptionText.text = item.description
        holder.amountText.text = "R${item.amount}"

        if (!item.photoBase64.isNullOrEmpty()) {
            holder.photoIcon.visibility = View.VISIBLE
            holder.photoIcon.setOnClickListener {
                onPhotoClick(item)
            }
        } else {
            holder.photoIcon.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateList(newList: List<Transaction>) {
        expenses = newList
        notifyDataSetChanged()
    }
}
