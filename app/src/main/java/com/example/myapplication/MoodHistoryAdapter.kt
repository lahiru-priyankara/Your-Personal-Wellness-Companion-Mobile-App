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

class MoodHistoryAdapter : RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

    private var moods = listOf<MoodEntry>()

    fun updateData(moodList: List<MoodEntry>) {
        moods = moodList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position])
    }

    override fun getItemCount() = moods.size

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodCard: CardView = itemView.findViewById(R.id.moodCard)
        private val emojiText: TextView = itemView.findViewById(R.id.emojiText)
        private val moodText: TextView = itemView.findViewById(R.id.moodText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val noteText: TextView = itemView.findViewById(R.id.noteText)

        fun bind(mood: MoodEntry) {
            emojiText.text = mood.emoji
            moodText.text = mood.description
            timeText.text = formatTime(mood.timestamp)
            noteText.text = mood.note

            // Set card color based on mood
            val backgroundColor = getMoodColor(mood.emoji)
            moodCard.setCardBackgroundColor(backgroundColor)

            // Show/hide note based on content
            noteText.visibility = if (mood.note.isNotEmpty()) View.VISIBLE else View.GONE
        }

        private fun formatTime(timestamp: Long): String {
            val date = Date(timestamp)
            val now = Date()
            val diff = now.time - timestamp

            return when {
                diff < 60000 -> "Just now" // Less than 1 minute
                diff < 3600000 -> "${diff / 60000}m ago" // Less than 1 hour
                diff < 86400000 -> "${diff / 3600000}h ago" // Less than 1 day
                else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(date)
            }
        }

        private fun getMoodColor(emoji: String): Int {
            return when (emoji) {
                "😄", "🤩", "😊" -> Color.parseColor("#E8F5E8") // Happy - Light green
                "😐", "😌", "🤔" -> Color.parseColor("#FFF3E0") // Neutral - Light orange
                "😕", "😢", "😤" -> Color.parseColor("#FFEBEE") // Sad - Light red
                "😴", "😰" -> Color.parseColor("#F3E5F5") // Tired/Anxious - Light purple
                "😍", "😎" -> Color.parseColor("#E3F2FD") // Loved/Confident - Light blue
                else -> Color.parseColor("#F5F5F5") // Default - Light gray
            }
        }
    }

    data class MoodEntry(
        val emoji: String,
        val description: String,
        val note: String,
        val timestamp: Long
    )
}
