package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView

class StreakCalendarAdapter(
    private val context: Context,
    private val streakDays: List<StreakDay>
) : BaseAdapter() {

    override fun getCount(): Int = streakDays.size

    override fun getItem(position: Int): StreakDay = streakDays[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_streak_day, parent, false)
        
        val streakDay = getItem(position)
        val dayText = view.findViewById<TextView>(R.id.dayText)
        val dayCard = view.findViewById<CardView>(R.id.dayCard)
        
        dayText.text = streakDay.day.toString()
        
        // Set colors based on streak level
        when (streakDay.streakLevel) {
            StreakLevel.EXCELLENT -> {
                dayCard.setCardBackgroundColor(Color.parseColor("#4CAF50"))
                dayText.setTextColor(Color.WHITE)
            }
            StreakLevel.GOOD -> {
                dayCard.setCardBackgroundColor(Color.parseColor("#8BC34A"))
                dayText.setTextColor(Color.WHITE)
            }
            StreakLevel.OK -> {
                dayCard.setCardBackgroundColor(Color.parseColor("#FFC107"))
                dayText.setTextColor(Color.BLACK)
            }
            StreakLevel.NONE -> {
                dayCard.setCardBackgroundColor(Color.parseColor("#E0E0E0"))
                dayText.setTextColor(Color.GRAY)
            }
        }
        
        // Highlight today
        if (streakDay.isToday) {
            dayCard.setCardBackgroundColor(Color.parseColor("#2196F3"))
            dayText.setTextColor(Color.WHITE)
        }
        
        return view
    }
}
