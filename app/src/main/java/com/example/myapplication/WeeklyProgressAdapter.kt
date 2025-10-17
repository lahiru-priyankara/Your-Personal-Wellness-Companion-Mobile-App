package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WeeklyProgressAdapter(
    private val onDayClick: (WeeklyProgressFragment.WeeklyDayData) -> Unit
) : RecyclerView.Adapter<WeeklyProgressAdapter.WeeklyDayViewHolder>() {

    private var weeklyData = listOf<WeeklyProgressFragment.WeeklyDayData>()

    fun updateData(data: List<WeeklyProgressFragment.WeeklyDayData>) {
        weeklyData = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyDayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weekly_day, parent, false)
        return WeeklyDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeeklyDayViewHolder, position: Int) {
        holder.bind(weeklyData[position])
    }

    override fun getItemCount() = weeklyData.size

    inner class WeeklyDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayCard: CardView = itemView.findViewById(R.id.dayCard)
        private val dayName: TextView = itemView.findViewById(R.id.dayName)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val habitsText: TextView = itemView.findViewById(R.id.habitsText)
        private val moodText: TextView = itemView.findViewById(R.id.moodText)
        private val waterText: TextView = itemView.findViewById(R.id.waterText)
        private val meditationText: TextView = itemView.findViewById(R.id.meditationText)
        private val completionText: TextView = itemView.findViewById(R.id.completionText)
        private val todayIndicator: TextView = itemView.findViewById(R.id.todayIndicator)

        fun bind(dayData: WeeklyProgressFragment.WeeklyDayData) {
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            
            dayName.text = dayFormat.format(dayData.date)
            dateText.text = dateFormat.format(dayData.date)
            
            habitsText.text = "🎯 ${dayData.habitsCompleted}/${dayData.totalHabits}"
            moodText.text = "😊 ${String.format("%.1f", dayData.moodScore)}"
            waterText.text = "💧 ${dayData.waterIntake}ml"
            meditationText.text = "🧘 ${dayData.meditationMinutes}min"
            
            val completionPercentage = if (dayData.totalHabits > 0) {
                (dayData.habitsCompleted * 100) / dayData.totalHabits
            } else 0
            
            completionText.text = "📊 $completionPercentage%"
            
            // Show today indicator
            todayIndicator.visibility = if (dayData.isToday) View.VISIBLE else View.GONE
            
            // Set card colors based on completion
            when {
                completionPercentage >= 80 -> {
                    dayCard.setCardBackgroundColor(Color.parseColor("#E8F5E8"))
                    completionText.setTextColor(Color.parseColor("#2E7D32"))
                }
                completionPercentage >= 50 -> {
                    dayCard.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                    completionText.setTextColor(Color.parseColor("#F57C00"))
                }
                else -> {
                    dayCard.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                    completionText.setTextColor(Color.parseColor("#D32F2F"))
                }
            }
            
            // Set mood color
            when {
                dayData.moodScore >= 7 -> moodText.setTextColor(Color.parseColor("#2E7D32"))
                dayData.moodScore >= 5 -> moodText.setTextColor(Color.parseColor("#F57C00"))
                else -> moodText.setTextColor(Color.parseColor("#D32F2F"))
            }
            
            itemView.setOnClickListener {
                onDayClick(dayData)
            }
        }
    }
}
