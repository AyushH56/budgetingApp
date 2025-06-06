package com.example.poegroup4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmergencyFundHistoryAdapter(private val list: List<Transaction>) :
    RecyclerView.Adapter<EmergencyFundHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTransaction: TextView = view.findViewById(R.id.tvTransaction)
        val tvAmountSaved: TextView = view.findViewById(R.id.tvAmountSaved)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_fund_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvDate.text = item.date
        holder.tvTransaction.text = item.description
        holder.tvAmountSaved.text = "R${"%.2f".format(item.emergencyFund)}"
    }
}
