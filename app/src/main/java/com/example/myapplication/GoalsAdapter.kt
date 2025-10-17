package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class GoalsAdapter(
    private val onGoalClick: (Goal) -> Unit
) : ListAdapter<Goal, GoalsAdapter.GoalViewHolder>(GoalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position), onGoalClick)
    }

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val goalCard: CardView = itemView.findViewById(R.id.goalCard)
        private val goalTitle: TextView = itemView.findViewById(R.id.goalTitle)
        private val goalDescription: TextView = itemView.findViewById(R.id.goalDescription)
        private val goalProgress: ProgressBar = itemView.findViewById(R.id.goalProgress)
        private val goalProgressText: TextView = itemView.findViewById(R.id.goalProgressText)
        private val goalStatus: ImageView = itemView.findViewById(R.id.goalStatus)
        private val goalAction: CardView = itemView.findViewById(R.id.goalAction)

        fun bind(goal: Goal, onGoalClick: (Goal) -> Unit) {
            goalTitle.text = goal.title
            goalDescription.text = goal.description
            
            goalProgress.max = goal.targetDays
            goalProgress.progress = goal.currentProgress
            goalProgressText.text = "${goal.currentProgress}/${goal.targetDays} days"
            
            if (goal.isCompleted) {
                goalStatus.setImageResource(android.R.drawable.ic_menu_upload)
                goalStatus.setColorFilter(android.graphics.Color.parseColor("#4CAF50"))
                goalAction.visibility = View.GONE
                goalCard.setCardBackgroundColor(android.graphics.Color.parseColor("#E8F5E8"))
            } else {
                goalStatus.setImageResource(android.R.drawable.ic_menu_compass)
                goalStatus.setColorFilter(android.graphics.Color.parseColor("#FF9800"))
                goalAction.visibility = View.VISIBLE
                goalCard.setCardBackgroundColor(android.graphics.Color.WHITE)
                
                goalAction.setOnClickListener {
                    onGoalClick(goal)
                }
            }
            
            // Add click animation
            goalCard.setOnClickListener {
                if (!goal.isCompleted) {
                    onGoalClick(goal)
                }
            }
        }
    }

    private class GoalDiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem == newItem
        }
    }
}
