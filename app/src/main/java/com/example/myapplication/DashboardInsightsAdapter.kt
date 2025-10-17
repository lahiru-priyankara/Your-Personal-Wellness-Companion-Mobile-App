package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class DashboardInsightsAdapter : RecyclerView.Adapter<DashboardInsightsAdapter.InsightViewHolder>() {

    private var insights = listOf<DashboardFragment.DashboardInsight>()

    fun updateData(data: List<DashboardFragment.DashboardInsight>) {
        insights = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_insight, parent, false)
        return InsightViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsightViewHolder, position: Int) {
        holder.bind(insights[position])
    }

    override fun getItemCount() = insights.size

    inner class InsightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val insightCard: CardView = itemView.findViewById(R.id.insightCard)
        private val iconText: TextView = itemView.findViewById(R.id.iconText)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val valueText: TextView = itemView.findViewById(R.id.valueText)
        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)

        fun bind(insight: DashboardFragment.DashboardInsight) {
            iconText.text = insight.icon
            titleText.text = insight.title
            valueText.text = insight.value
            descriptionText.text = insight.description
            
            // Set value color based on insight color
            valueText.setTextColor(insight.color)
            
            // Set card background based on insight color (subtle)
            val backgroundColor = Color.argb(20, Color.red(insight.color), Color.green(insight.color), Color.blue(insight.color))
            insightCard.setCardBackgroundColor(backgroundColor)
        }
    }
}
