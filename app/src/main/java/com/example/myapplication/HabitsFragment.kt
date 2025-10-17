package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentHabitsBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HabitsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HabitsListAdapter(
            onToggleComplete = { index -> toggleCompletionToday(index) },
            onEdit = { index -> promptAddOrEditHabit(index) },
            onDelete = { index -> deleteHabit(index) }
        )

        binding.habitsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.habitsRecycler.adapter = adapter
        attachReorder()

        binding.quickAddBtn.setOnClickListener { promptAddOrEditHabit(null) }
        binding.headerAddBtn.setOnClickListener { promptAddOrEditHabit(null) }
        binding.headerShareBtn.setOnClickListener { shareSummary() }

        loadHabitsIntoAdapter()
        updateProgress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun promptAddOrEditHabit(index: Int?) {
        val ctx = requireContext()
        val input = EditText(ctx)
        input.hint = "Enter habit name..."
        input.setPadding(32, 32, 32, 32)
        input.textSize = 16f
        
        if (index != null) {
            input.setText(getHabits()[index].name)
        }
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(ctx)
            .setTitle(if (index == null) "➕ Add New Habit" else "✏️ Edit Habit")
            .setMessage(if (index == null) "What habit would you like to add to your daily routine?" else "Edit your habit name:")
            .setView(input)
            .setPositiveButton("💝 Add Habit") { d, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (index == null) {
                        addHabit(name)
                        showHabitAddedConfirmation(name)
                    } else {
                        editHabit(index, name)
                        showHabitEditedConfirmation(name)
                    }
                } else {
                    android.widget.Toast.makeText(ctx, "Please enter a habit name", android.widget.Toast.LENGTH_SHORT).show()
                }
                d.dismiss()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .setNeutralButton("🎯 Professional Add") { d, _ ->
                d.dismiss()
                if (index == null) {
                    findNavController().navigate(R.id.AddHabitFragment)
                }
            }
            .create()
        
        dialog.show()
        
        // Auto-focus the input field
        input.requestFocus()
    }

    private fun showHabitAddedConfirmation(habitName: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎉 Habit Added!")
            .setMessage("Your habit '$habitName' has been added successfully!")
            .setPositiveButton("Great!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showHabitEditedConfirmation(habitName: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("✅ Habit Updated!")
            .setMessage("Your habit has been updated to '$habitName'!")
            .setPositiveButton("Great!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun toggleCompletionToday(index: Int) {
        val habits = getHabits().toMutableList()
        val habit = habits[index]
        val today = todayKey()
        val toggled = habit.completedDates.toMutableSet()
        if (toggled.contains(today)) toggled.remove(today) else toggled.add(today)
        habits[index] = habit.copy(completedDates = toggled)
        saveHabits(habits)
        loadHabitsIntoAdapter()
        updateProgress()
    }

    private fun addHabit(name: String) {
        val habits = getHabits().toMutableList()
        habits.add(Habit(name, emptySet()))
        saveHabits(habits)
        loadHabitsIntoAdapter()
        updateProgress()
    }

    private fun editHabit(index: Int, name: String) {
        val habits = getHabits().toMutableList()
        val existing = habits[index]
        habits[index] = existing.copy(name = name)
        saveHabits(habits)
        loadHabitsIntoAdapter()
    }

    private fun deleteHabit(index: Int) {
        val habits = getHabits().toMutableList()
        habits.removeAt(index)
        saveHabits(habits)
        loadHabitsIntoAdapter()
        updateProgress()
    }

    private fun loadHabitsIntoAdapter() {
        val habits = getHabits()
        adapter.submitList(habits)
        binding.emptyText.visibility = if (habits.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateProgress() {
        val habits = getHabits()
        val today = todayKey()
        val total = habits.size
        val completed = habits.count { it.completedDates.contains(today) }
        val percent = if (total == 0) 0 else (completed * 100 / total)
        binding.progressText.text = "Today's completion: $completed/$total ($percent%)"
        binding.progressBar.max = if (total == 0) 1 else total
        binding.progressBar.progress = completed
    }

    private fun prefs() = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)

    private fun saveHabits(habits: List<Habit>) {
        val arr = JSONArray()
        habits.forEach { h ->
            val o = JSONObject()
            o.put("name", h.name)
            val dates = JSONArray()
            h.completedDates.forEach { dates.put(it) }
            o.put("completedDates", dates)
            arr.put(o)
        }
        prefs().edit().putString("habits_json", arr.toString()).apply()
        // update widget
        val am = android.appwidget.AppWidgetManager.getInstance(requireContext())
        val cn = android.content.ComponentName(requireContext(), HabitsWidgetProvider::class.java)
        val ids = am.getAppWidgetIds(cn)
        if (ids.isNotEmpty()) {
            HabitsWidgetProvider.updateAppWidget(requireContext(), am, ids[0])
        }
    }

    private fun getHabits(): List<Habit> {
        val json = prefs().getString("habits_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<Habit>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val name = o.optString("name")
                val datesArr = o.optJSONArray("completedDates") ?: JSONArray()
                val set = mutableSetOf<String>()
                for (j in 0 until datesArr.length()) set.add(datesArr.getString(j))
                list.add(Habit(name, set))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }


    private fun showSmartSuggestions() {
        val suggestions = listOf(
            "💧 Drink 8 glasses of water daily",
            "🧘‍♀️ Meditate for 10 minutes",
            "📚 Read for 30 minutes",
            "🏃‍♀️ Exercise for 20 minutes",
            "😴 Sleep 8 hours nightly",
            "🙏 Practice gratitude daily"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💡 Smart Habit Suggestions")
            .setItems(suggestions.toTypedArray()) { _, which ->
                val suggestion = suggestions[which]
                addHabitFromSuggestion(suggestion)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addHabitFromSuggestion(suggestion: String) {
        val habitName = suggestion.substringAfter(" ")
        val habits = getHabits()
        val newHabit = Habit(habitName, mutableSetOf())
        val updatedHabits = habits + newHabit
        saveHabits(updatedHabits)
        loadHabitsIntoAdapter()
        updateProgress()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "✅ Habit '$habitName' added successfully!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun shareSummary() {
        val habits = getHabits()
        val today = todayKey()
        val total = habits.size
        val completed = habits.count { it.completedDates.contains(today) }
        val percentage = if (total > 0) (completed * 100) / total else 0
        
        val shareText = """
            📊 My Daily Habits Progress
            
            ✅ Completed: $completed/$total habits
            📈 Progress: $percentage%
            
            💪 Building healthy habits one day at a time!
            
            #WellnessJourney #HealthyHabits #SLIITApp
        """.trimIndent()
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Daily Habits Progress")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Progress"))
    }

    private fun attachReorder() {
        val callback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
                val list = getHabits().toMutableList()
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                java.util.Collections.swap(list, from, to)
                saveHabits(list)
                adapter.submitList(list)
                return true
            }
            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) { }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.habitsRecycler)
    }
}

data class Habit(
    val name: String,
    val completedDates: Set<String>
)


