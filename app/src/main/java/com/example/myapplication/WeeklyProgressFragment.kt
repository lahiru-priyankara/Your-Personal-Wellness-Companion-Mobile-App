package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentWeeklyProgressBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class WeeklyProgressFragment : Fragment() {

    private var _binding: FragmentWeeklyProgressBinding? = null
    private val binding get() = _binding!!
    private lateinit var weeklyAdapter: WeeklyProgressAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeeklyProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadWeeklyData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        weeklyAdapter = WeeklyProgressAdapter { dayData ->
            // Handle day click - show detailed progress
            showDayDetails(dayData)
        }
        
        binding.weeklyRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = weeklyAdapter
        }
    }

    private fun loadWeeklyData() {
        val weeklyData = generateWeeklyData()
        weeklyAdapter.updateData(weeklyData)
        
        // Update summary
        updateWeeklySummary(weeklyData)
    }

    private fun generateWeeklyData(): List<WeeklyDayData> {
        val data = mutableListOf<WeeklyDayData>()
        val calendar = Calendar.getInstance()
        
        // Get last 7 days
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = calendar.time
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            
            val habitsCompleted = getHabitsCompletedForDate(dateKey)
            val totalHabits = getTotalHabitsForDate(dateKey)
            val moodScore = getMoodScoreForDate(dateKey)
            val waterIntake = getWaterIntakeForDate(dateKey)
            val meditationMinutes = getMeditationMinutesForDate(dateKey)
            
            data.add(WeeklyDayData(
                date = date,
                dateString = dateKey,
                habitsCompleted = habitsCompleted,
                totalHabits = totalHabits,
                moodScore = moodScore,
                waterIntake = waterIntake,
                meditationMinutes = meditationMinutes,
                isToday = i == 0
            ))
        }
        
        return data
    }

    private fun getHabitsCompletedForDate(date: String): Int {
        val prefs = requireContext().getSharedPreferences("habits", 0)
        val habitsJson = prefs.getString("habits_json", "[]")
        val habitsArray = JSONArray(habitsJson)
        
        var completed = 0
        for (i in 0 until habitsArray.length()) {
            val habit = habitsArray.getJSONObject(i)
            val completedDates = habit.optString("completedDates", "")
            if (completedDates.contains(date)) {
                completed++
            }
        }
        return completed
    }

    private fun getTotalHabitsForDate(date: String): Int {
        val prefs = requireContext().getSharedPreferences("habits", 0)
        val habitsJson = prefs.getString("habits_json", "[]")
        val habitsArray = JSONArray(habitsJson)
        return habitsArray.length()
    }

    private fun getMoodScoreForDate(date: String): Float {
        val prefs = requireContext().getSharedPreferences("moods", 0)
        val moodsJson = prefs.getString("moods_json", "[]")
        val moodsArray = JSONArray(moodsJson)
        
        for (i in 0 until moodsArray.length()) {
            val mood = moodsArray.getJSONObject(i)
            if (mood.optString("date") == date) {
                return mood.optDouble("score", 5.0).toFloat()
            }
        }
        return 5.0f // Default neutral mood
    }

    private fun getWaterIntakeForDate(date: String): Int {
        val prefs = requireContext().getSharedPreferences("water_tracking", 0)
        return prefs.getInt("water_$date", 0)
    }

    private fun getMeditationMinutesForDate(date: String): Int {
        val prefs = requireContext().getSharedPreferences("meditation", 0)
        return prefs.getInt("meditation_$date", 0)
    }

    private fun updateWeeklySummary(data: List<WeeklyDayData>) {
        val totalHabits = data.sumOf { it.totalHabits }
        val completedHabits = data.sumOf { it.habitsCompleted }
        val averageMood = data.map { it.moodScore }.average()
        val totalWater = data.sumOf { it.waterIntake }
        val totalMeditation = data.sumOf { it.meditationMinutes }
        
        binding.weeklySummaryCard.visibility = View.VISIBLE
        binding.habitsCompletedText.text = "$completedHabits/$totalHabits"
        binding.moodAverageText.text = String.format("%.1f", averageMood)
        binding.waterTotalText.text = "${totalWater}ml"
        binding.meditationTotalText.text = "${totalMeditation}min"
        
        // Calculate completion percentage
        val completionPercentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
        binding.completionPercentageText.text = "${completionPercentage}%"
        
        // Set progress bar
        binding.weeklyProgressBar.progress = completionPercentage
    }

    private fun setupClickListeners() {
        binding.exportButton.setOnClickListener {
            exportWeeklyData()
        }
        
        binding.refreshButton.setOnClickListener {
            loadWeeklyData()
        }
    }

    private fun showDayDetails(dayData: WeeklyDayData) {
        val details = """
            📅 ${SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(dayData.date)}
            
            🎯 Habits: ${dayData.habitsCompleted}/${dayData.totalHabits}
            😊 Mood Score: ${String.format("%.1f", dayData.moodScore)}/10
            💧 Water: ${dayData.waterIntake}ml
            🧘 Meditation: ${dayData.meditationMinutes}min
            
            Completion: ${if (dayData.totalHabits > 0) (dayData.habitsCompleted * 100) / dayData.totalHabits else 0}%
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Day Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportWeeklyData() {
        val data = generateWeeklyData()
        val exportText = buildString {
            appendLine("📊 Weekly Wellness Report")
            appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
            appendLine()
            
            data.forEach { day ->
                appendLine("📅 ${SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(day.date)}")
                appendLine("   🎯 Habits: ${day.habitsCompleted}/${day.totalHabits}")
                appendLine("   😊 Mood: ${String.format("%.1f", day.moodScore)}/10")
                appendLine("   💧 Water: ${day.waterIntake}ml")
                appendLine("   🧘 Meditation: ${day.meditationMinutes}min")
                appendLine()
            }
        }
        
        val intent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, exportText)
        }
        startActivity(android.content.Intent.createChooser(intent, "Share Weekly Report"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class WeeklyDayData(
        val date: Date,
        val dateString: String,
        val habitsCompleted: Int,
        val totalHabits: Int,
        val moodScore: Float,
        val waterIntake: Int,
        val meditationMinutes: Int,
        val isToday: Boolean = false
    )
}
