package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class WellnessTipsAdapter(
    private val onTipClick: (WellnessTip) -> Unit
) : ListAdapter<WellnessTip, WellnessTipsAdapter.TipViewHolder>(TipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wellness_tip, parent, false)
        return TipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        holder.bind(getItem(position), onTipClick)
    }

    class TipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tipCard: CardView = itemView.findViewById(R.id.tipCard)
        private val tipIcon: TextView = itemView.findViewById(R.id.tipIcon)
        private val tipTitle: TextView = itemView.findViewById(R.id.tipTitle)
        private val tipCategory: TextView = itemView.findViewById(R.id.tipCategory)
        private val tipDescription: TextView = itemView.findViewById(R.id.tipDescription)

        fun bind(tip: WellnessTip, onTipClick: (WellnessTip) -> Unit) {
            tipIcon.text = tip.icon
            tipTitle.text = tip.title
            tipCategory.text = tip.category
            tipDescription.text = tip.description
            
            tipCard.setOnClickListener {
                onTipClick(tip)
            }
        }
    }

    private class TipDiffCallback : DiffUtil.ItemCallback<WellnessTip>() {
        override fun areItemsTheSame(oldItem: WellnessTip, newItem: WellnessTip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WellnessTip, newItem: WellnessTip): Boolean {
            return oldItem == newItem
        }
    }
}
