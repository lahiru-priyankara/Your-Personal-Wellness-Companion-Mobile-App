package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentDashboardBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var insightsAdapter: DashboardInsightsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadDashboardData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        insightsAdapter = DashboardInsightsAdapter()
        binding.insightsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = insightsAdapter
        }
    }

    private fun loadDashboardData() {
        val insights = generateDashboardInsights()
        insightsAdapter.updateData(insights)
        
        // Update key metrics
        updateKeyMetrics()
    }

    private fun generateDashboardInsights(): List<DashboardInsight> {
        val insights = mutableListOf<DashboardInsight>()
        
        // Habit completion insight
        val habitCompletion = calculateHabitCompletion()
        insights.add(DashboardInsight(
            title = "Habit Completion",
            value = "${habitCompletion}%",
            description = "Your daily habit completion rate",
            icon = "🎯",
            color = when {
                habitCompletion >= 80 -> Color.parseColor("#10B981")
                habitCompletion >= 60 -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#EF4444")
            }
        ))
        
        // Mood trend insight
        val moodTrend = calculateMoodTrend()
        insights.add(DashboardInsight(
            title = "Mood Trend",
            value = moodTrend,
            description = "Your emotional wellness trend",
            icon = "😊",
            color = when (moodTrend) {
                "Improving" -> Color.parseColor("#10B981")
                "Stable" -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#EF4444")
            }
        ))
        
        // Water intake insight
        val waterIntake = getTodayWaterIntake()
        insights.add(DashboardInsight(
            title = "Hydration",
            value = "${waterIntake}ml",
            description = "Today's water intake",
            icon = "💧",
            color = if (waterIntake >= 2000) Color.parseColor("#10B981") else Color.parseColor("#F59E0B")
        ))
        
        // Meditation insight
        val meditationMinutes = getTodayMeditationMinutes()
        insights.add(DashboardInsight(
            title = "Meditation",
            value = "${meditationMinutes}min",
            description = "Today's mindfulness practice",
            icon = "🧘",
            color = if (meditationMinutes >= 10) Color.parseColor("#10B981") else Color.parseColor("#F59E0B")
        ))
        
        // Streak insight
        val currentStreak = calculateCurrentStreak()
        insights.add(DashboardInsight(
            title = "Current Streak",
            value = "${currentStreak} days",
            description = "Your wellness streak",
            icon = "🔥",
            color = if (currentStreak >= 7) Color.parseColor("#10B981") else Color.parseColor("#F59E0B")
        ))
        
        // Weekly goal progress
        val weeklyProgress = calculateWeeklyProgress()
        insights.add(DashboardInsight(
            title = "Weekly Progress",
            value = "${weeklyProgress}%",
            description = "This week's wellness goals",
            icon = "📊",
            color = when {
                weeklyProgress >= 80 -> Color.parseColor("#10B981")
                weeklyProgress >= 60 -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#EF4444")
            }
        ))
        
        return insights
    }

    private fun calculateHabitCompletion(): Int {
        val prefs = requireContext().getSharedPreferences("habits", 0)
        val habitsJson = prefs.getString("habits_json", "[]")
        val habitsArray = JSONArray(habitsJson)
        
        if (habitsArray.length() == 0) return 0
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var completed = 0
        
        for (i in 0 until habitsArray.length()) {
            val habit = habitsArray.getJSONObject(i)
            val completedDates = habit.optString("completedDates", "")
            if (completedDates.contains(today)) {
                completed++
            }
        }
        
        return (completed * 100) / habitsArray.length()
    }

    private fun calculateMoodTrend(): String {
        val prefs = requireContext().getSharedPreferences("moods", 0)
        val moodsJson = prefs.getString("moods_json", "[]")
        val moodsArray = JSONArray(moodsJson)
        
        if (moodsArray.length() < 2) return "Stable"
        
        val recentMoods = mutableListOf<Float>()
        for (i in maxOf(0, moodsArray.length() - 7) until moodsArray.length()) {
            val mood = moodsArray.getJSONObject(i)
            recentMoods.add(mood.optDouble("score", 5.0).toFloat())
        }
        
        return when {
            recentMoods.size >= 2 && recentMoods.last() > recentMoods.first() -> "Improving"
            recentMoods.size >= 2 && recentMoods.last() < recentMoods.first() -> "Declining"
            else -> "Stable"
        }
    }

    private fun getTodayWaterIntake(): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = requireContext().getSharedPreferences("water_tracking", 0)
        return prefs.getInt("water_$today", 0)
    }

    private fun getTodayMeditationMinutes(): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = requireContext().getSharedPreferences("meditation", 0)
        return prefs.getInt("meditation_$today", 0)
    }

    private fun calculateCurrentStreak(): Int {
        val prefs = requireContext().getSharedPreferences("habits", 0)
        val habitsJson = prefs.getString("habits_json", "[]")
        val habitsArray = JSONArray(habitsJson)
        
        if (habitsArray.length() == 0) return 0
        
        val calendar = Calendar.getInstance()
        var streak = 0
        
        while (true) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            var dayCompleted = true
            
            for (i in 0 until habitsArray.length()) {
                val habit = habitsArray.getJSONObject(i)
                val completedDates = habit.optString("completedDates", "")
                if (!completedDates.contains(date)) {
                    dayCompleted = false
                    break
                }
            }
            
            if (dayCompleted) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        return streak
    }

    private fun calculateWeeklyProgress(): Int {
        val prefs = requireContext().getSharedPreferences("habits", 0)
        val habitsJson = prefs.getString("habits_json", "[]")
        val habitsArray = JSONArray(habitsJson)
        
        if (habitsArray.length() == 0) return 0
        
        val calendar = Calendar.getInstance()
        var weeklyCompleted = 0
        val totalPossible = habitsArray.length() * 7
        
        for (day in 0..6) {
            calendar.add(Calendar.DAY_OF_YEAR, -day)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            for (i in 0 until habitsArray.length()) {
                val habit = habitsArray.getJSONObject(i)
                val completedDates = habit.optString("completedDates", "")
                if (completedDates.contains(date)) {
                    weeklyCompleted++
                }
            }
        }
        
        return if (totalPossible > 0) (weeklyCompleted * 100) / totalPossible else 0
    }

    private fun updateKeyMetrics() {
        val habitCompletion = calculateHabitCompletion()
        val waterIntake = getTodayWaterIntake()
        val meditationMinutes = getTodayMeditationMinutes()
        val currentStreak = calculateCurrentStreak()
        
        binding.habitCompletionText.text = "${habitCompletion}%"
        binding.waterIntakeText.text = "${waterIntake}ml"
        binding.meditationText.text = "${meditationMinutes}min"
        binding.streakText.text = "${currentStreak} days"
        
        // Update progress bars
        binding.habitProgressBar.progress = habitCompletion
        binding.waterProgressBar.progress = minOf(100, (waterIntake * 100) / 2000)
        binding.meditationProgressBar.progress = minOf(100, (meditationMinutes * 100) / 30)
    }

    private fun setupClickListeners() {
        binding.refreshButton.setOnClickListener {
            loadDashboardData()
        }
        
        binding.exportButton.setOnClickListener {
            exportDashboardData()
        }
    }

    private fun exportDashboardData() {
        val habitCompletion = calculateHabitCompletion()
        val waterIntake = getTodayWaterIntake()
        val meditationMinutes = getTodayMeditationMinutes()
        val currentStreak = calculateCurrentStreak()
        val weeklyProgress = calculateWeeklyProgress()
        
        val exportText = """
            📊 WellnessPro Dashboard Report
            Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}
            
            🎯 Habit Completion: ${habitCompletion}%
            💧 Water Intake: ${waterIntake}ml
            🧘 Meditation: ${meditationMinutes}min
            🔥 Current Streak: ${currentStreak} days
            📊 Weekly Progress: ${weeklyProgress}%
            
            Keep up the great work! 🌟
        """.trimIndent()
        
        val intent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, exportText)
        }
        startActivity(android.content.Intent.createChooser(intent, "Share Dashboard Report"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class DashboardInsight(
        val title: String,
        val value: String,
        val description: String,
        val icon: String,
        val color: Int
    )
}
