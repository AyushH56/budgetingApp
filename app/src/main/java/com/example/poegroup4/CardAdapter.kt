package com.example.poegroup4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// RecyclerView Adapter for displaying a list of HomeCard items in card format
class CardAdapter(
    private val items: List<HomeCard>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    // ViewHolder class that holds and binds the views for each item
    inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.cardIcon)
        val title: TextView = view.findViewById(R.id.cardTitle)

        // Binds a HomeCard object to the views
        fun bind(card: HomeCard) {
            icon.setImageResource(card.iconRes)
            title.text = card.title
            itemView.setOnClickListener { onClick(card.title) }
        }
    }

    // Inflates the layout for a new ViewHolder when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    // Binds data to an existing ViewHolder based on position
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int = items.size
}
