package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentAnalyticsBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAnalytics()
        setupClickListeners()
    }

    private fun setupAnalytics() {
        val habits = getHabits()
        val moods = getMoods()
        
        // Weekly completion rate
        val weeklyRate = calculateWeeklyCompletionRate(habits)
        binding.weeklyProgressText.text = "${weeklyRate}%"
        binding.weeklyProgressBar.progress = weeklyRate
        
        // Mood trend
        val moodTrend = calculateMoodTrend(moods)
        binding.moodTrendText.text = when {
            moodTrend > 0 -> "📈 Improving (${moodTrend}%)"
            moodTrend < 0 -> "📉 Needs attention (${moodTrend}%)"
            else -> "📊 Stable"
        }
        
        // Best streak
        val bestStreak = habits.maxOfOrNull { HabitsFragmentHelper.calculateStreak(it) } ?: 0
        binding.bestStreakText.text = "$bestStreak days"
        
        // Current streak average
        val avgStreak = if (habits.isNotEmpty()) {
            habits.sumOf { HabitsFragmentHelper.calculateStreak(it) } / habits.size
        } else 0
        binding.avgStreakText.text = "$avgStreak days"
        
        // Weekly mood distribution
        setupMoodDistribution(moods)
        
        // Achievements
        setupAchievements(habits, moods)
        
        // Insights
        generateInsights(habits, moods)
    }

    private fun calculateWeeklyCompletionRate(habits: List<Habit>): Int {
        if (habits.isEmpty()) return 0
        
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
        
        var totalPossible = 0
        var totalCompleted = 0
        
        for (i in 0..6) {
            val date = Calendar.getInstance().apply { 
                time = today
                add(Calendar.DAY_OF_YEAR, -i) 
            }
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
            
            totalPossible += habits.size
            totalCompleted += habits.count { it.completedDates.contains(dateStr) }
        }
        
        return if (totalPossible > 0) ((totalCompleted.toDouble() / totalPossible) * 100).roundToInt() else 0
    }

    private fun calculateMoodTrend(moods: List<MoodEntry>): Int {
        if (moods.size < 2) return 0
        
        val recentMoods = moods.filter { 
            val moodDate = Date(it.timestamp)
            val daysDiff = (Date().time - moodDate.time) / (1000 * 60 * 60 * 24)
            daysDiff <= 7
        }.sortedBy { it.timestamp }
        
        if (recentMoods.size < 2) return 0
        
        val moodValues = recentMoods.map { moodToValue(it.emoji) }
        val firstHalf = moodValues.take(moodValues.size / 2).average()
        val secondHalf = moodValues.drop(moodValues.size / 2).average()
        
        return ((secondHalf - firstHalf) * 20).roundToInt()
    }

    private fun moodToValue(emoji: String): Double {
        return when (emoji) {
            "😢", "😰", "😡" -> 1.0
            "😐", "😕" -> 2.0
            "🙂", "😊" -> 3.0
            "😄", "🤩" -> 4.0
            "😍", "🥰" -> 5.0
            else -> 3.0
        }
    }

    private fun setupMoodDistribution(moods: List<MoodEntry>) {
        val weekMoods = moods.filter { 
            val daysDiff = (Date().time - it.timestamp) / (1000 * 60 * 60 * 24)
            daysDiff <= 7
        }
        
        val distribution = weekMoods.groupBy { it.emoji }.mapValues { it.value.size }
        val total = weekMoods.size.toFloat()
        
        val chartData = StringBuilder("Weekly Mood Distribution:\n")
        distribution.forEach { (emoji, count) ->
            val percentage = if (total > 0) ((count / total) * 100).roundToInt() else 0
            chartData.append("$emoji $percentage% ")
        }
        
        binding.moodChartText.text = if (total > 0) chartData.toString() else "No mood data this week"
    }

    private fun setupAchievements(habits: List<Habit>, moods: List<MoodEntry>) {
        val achievements = mutableListOf<String>()
        
        // Streak achievements
        val maxStreak = habits.maxOfOrNull { HabitsFragmentHelper.calculateStreak(it) } ?: 0
        when {
            maxStreak >= 30 -> achievements.add("🏆 Month Master - 30+ day streak!")
            maxStreak >= 14 -> achievements.add("🔥 Two Week Warrior - 14+ day streak!")
            maxStreak >= 7 -> achievements.add("⭐ Week Champion - 7+ day streak!")
            maxStreak >= 3 -> achievements.add("💪 Getting Started - 3+ day streak!")
        }
        
        // Consistency achievements
        val consistentHabits = habits.count { HabitsFragmentHelper.calculateStreak(it) >= 7 }
        if (consistentHabits >= 3) {
            achievements.add("🎯 Multi-tasker - 3+ consistent habits!")
        }
        
        // Mood achievements
        val weekMoods = moods.filter { 
            val daysDiff = (Date().time - it.timestamp) / (1000 * 60 * 60 * 24)
            daysDiff <= 7
        }
        if (weekMoods.size >= 7) {
            achievements.add("📝 Mindful Logger - Daily mood tracking!")
        }
        
        // Wellness achievements
        val totalHabits = habits.size
        when {
            totalHabits >= 10 -> achievements.add("🌟 Wellness Guru - 10+ habits!")
            totalHabits >= 5 -> achievements.add("🌱 Growing Garden - 5+ habits!")
        }
        
        binding.achievementsText.text = if (achievements.isNotEmpty()) {
            achievements.joinToString("\n")
        } else {
            "🎯 Start building habits to earn achievements!"
        }
    }

    private fun generateInsights(habits: List<Habit>, moods: List<MoodEntry>) {
        val insights = mutableListOf<String>()
        
        // Best performing day
        val dayPerformance = mutableMapOf<String, Int>()
        habits.forEach { habit ->
            habit.completedDates.forEach { date ->
                try {
                    val calendar = Calendar.getInstance()
                    calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) ?: Date()
                    val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
                    dayPerformance[dayName] = dayPerformance.getOrDefault(dayName, 0) + 1
                } catch (e: Exception) { }
            }
        }
        
        val bestDay = dayPerformance.maxByOrNull { it.value }?.key
        if (bestDay != null) {
            insights.add("📅 Your best day: $bestDay")
        }
        
        // Mood patterns
        val positiveEmojis = setOf("😊", "😄", "🤩", "😍", "🥰", "🙂")
        val recentPositiveMoods = moods.filter { 
            val daysDiff = (Date().time - it.timestamp) / (1000 * 60 * 60 * 24)
            daysDiff <= 7 && positiveEmojis.contains(it.emoji)
        }
        
        if (recentPositiveMoods.size >= 5) {
            insights.add("🌈 Great week! Mostly positive moods")
        }
        
        // Streak insights
        val streaks = habits.map { HabitsFragmentHelper.calculateStreak(it) }.sorted()
        if (streaks.isNotEmpty()) {
            val avgStreak = streaks.average().roundToInt()
            if (avgStreak >= 7) {
                insights.add("🔥 Excellent consistency across habits!")
            } else if (avgStreak >= 3) {
                insights.add("💪 Building good momentum!")
            }
        }
        
        // Recommendations
        if (habits.size < 3) {
            insights.add("💡 Try adding 2-3 more habits for better wellness")
        }
        
        if (moods.none { (Date().time - it.timestamp) / (1000 * 60 * 60 * 24) <= 1 }) {
            insights.add("📝 Don't forget to log your mood today!")
        }
        
        binding.insightsText.text = if (insights.isNotEmpty()) {
            insights.joinToString("\n\n")
        } else {
            "📊 Keep tracking to see personalized insights!"
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.shareAnalyticsButton.setOnClickListener {
            shareAnalytics()
        }
    }

    private fun shareAnalytics() {
        val habits = getHabits()
        val moods = getMoods()
        val weeklyRate = calculateWeeklyCompletionRate(habits)
        val bestStreak = habits.maxOfOrNull { HabitsFragmentHelper.calculateStreak(it) } ?: 0
        
        val analyticsText = """
            📊 My Wellness Analytics
            
            📈 Weekly Completion: ${weeklyRate}%
            🔥 Best Streak: $bestStreak days
            🎯 Active Habits: ${habits.size}
            😊 Moods This Week: ${moods.filter { (Date().time - it.timestamp) / (1000 * 60 * 60 * 24) <= 7 }.size}
            
            💪 Keep up the great work!
            
            #WellnessJourney #Analytics #HealthyHabits
        """.trimIndent()
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, analyticsText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Wellness Analytics")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Analytics"))
    }

    private fun prefs() = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)

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

    private fun getMoods(): List<MoodEntry> {
        val json = prefs().getString("moods_json", "[]")
        val arr = JSONArray(json)
        val list = mutableListOf<MoodEntry>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val emoji = o.optString("emoji")
            val note = o.optString("note")
            val ts = o.optLong("timestamp")
            list.add(MoodEntry(emoji, note, ts))
        }
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
