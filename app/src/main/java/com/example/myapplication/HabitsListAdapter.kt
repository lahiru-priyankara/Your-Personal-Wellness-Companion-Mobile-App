package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemHabitBinding

class HabitsListAdapter(
    private val onToggleComplete: (Int) -> Unit,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : ListAdapter<Habit, HabitsListAdapter.HabitVH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitVH {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitVH(binding)
    }

    override fun onBindViewHolder(holder: HabitVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitVH(private val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onToggleComplete(bindingAdapterPosition) }
            binding.more.setOnClickListener { v ->
                val menu = PopupMenu(v.context, v)
                menu.menu.add("Edit").setOnMenuItemClickListener {
                    onEdit(bindingAdapterPosition); true
                }
                menu.menu.add("Delete").setOnMenuItemClickListener {
                    onDelete(bindingAdapterPosition); true
                }
                menu.show()
            }
        }

        fun bind(item: Habit) {
            binding.title.text = item.name
            val todayCompleted = item.completedDates.contains(HabitsFragmentHelper.todayKey())
            val streak = HabitsFragmentHelper.calculateStreak(item)
            
            binding.status.text = if (todayCompleted) {
                when {
                    streak >= 30 -> "🏆 30+ days! Amazing!"
                    streak >= 14 -> "🔥 2+ weeks strong!"
                    streak >= 7 -> "⭐ 1+ week streak!"
                    streak >= 3 -> "💪 Building momentum!"
                    else -> "✅ Done today!"
                }
            } else {
                "⏳ Pending today"
            }
            
            binding.streak.text = "Streak: $streak days"
        }
    }
}

object HabitsFragmentHelper {
    fun todayKey(): String = HabitsFragment().let { // not ideal, but provides date key
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }

    fun calculateStreak(habit: Habit): Int {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        var streak = 0
        while (true) {
            val key = sdf.format(cal.time)
            if (habit.completedDates.contains(key)) {
                streak++
                cal.add(java.util.Calendar.DATE, -1)
            } else {
                break
            }
        }
        return streak
    }
}


