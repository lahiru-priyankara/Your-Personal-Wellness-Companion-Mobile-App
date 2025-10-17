package com.example.myapplication

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class HabitTemplatesAdapter(
    private val onTemplateClick: (HabitTemplate) -> Unit
) : ListAdapter<HabitTemplate, HabitTemplatesAdapter.TemplateViewHolder>(TemplateDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(getItem(position), onTemplateClick)
    }

    class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val templateCard: CardView = itemView.findViewById(R.id.templateCard)
        private val templateIcon: TextView = itemView.findViewById(R.id.templateIcon)
        private val templateName: TextView = itemView.findViewById(R.id.templateName)
        private val templateCategory: TextView = itemView.findViewById(R.id.templateCategory)
        private val templateDescription: TextView = itemView.findViewById(R.id.templateDescription)
        private val templateDifficulty: TextView = itemView.findViewById(R.id.templateDifficulty)
        private val templateDuration: TextView = itemView.findViewById(R.id.templateDuration)

        fun bind(template: HabitTemplate, onTemplateClick: (HabitTemplate) -> Unit) {
            templateIcon.text = template.icon
            templateName.text = template.name
            templateCategory.text = template.category
            templateDescription.text = template.description
            templateDifficulty.text = template.difficulty
            templateDuration.text = template.duration
            
            // Set card color based on template color
            try {
                val color = Color.parseColor(template.color)
                templateCard.setCardBackgroundColor(color)
                
                // Set text colors based on background
                val textColor = if (isColorDark(color)) Color.WHITE else Color.BLACK
                templateName.setTextColor(textColor)
                templateCategory.setTextColor(textColor)
                templateDescription.setTextColor(textColor)
                templateDifficulty.setTextColor(textColor)
                templateDuration.setTextColor(textColor)
            } catch (e: Exception) {
                // Use default colors if parsing fails
                templateCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            
            templateCard.setOnClickListener {
                onTemplateClick(template)
            }
        }
        
        private fun isColorDark(color: Int): Boolean {
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            return darkness >= 0.5
        }
    }

    private class TemplateDiffCallback : DiffUtil.ItemCallback<HabitTemplate>() {
        override fun areItemsTheSame(oldItem: HabitTemplate, newItem: HabitTemplate): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HabitTemplate, newItem: HabitTemplate): Boolean {
            return oldItem == newItem
        }
    }
}
