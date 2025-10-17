package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentStreakCalendarBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class StreakCalendarFragment : Fragment() {

    private var _binding: FragmentStreakCalendarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var calendarAdapter: StreakCalendarAdapter
    private var currentMonth = 0
    private var currentYear = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreakCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = Calendar.getInstance()
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)
        
        setupCalendar()
        setupClickListeners()
        updateStreakStats()
    }

    private fun setupCalendar() {
        calendarAdapter = StreakCalendarAdapter(requireContext(), getStreakData())
        binding.streakCalendarGrid.adapter = calendarAdapter
        
        updateMonthHeader()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.prevMonthButton.setOnClickListener {
            if (currentMonth > 0) {
                currentMonth--
            } else {
                currentMonth = 11
                currentYear--
            }
            updateCalendar()
        }
        
        binding.nextMonthButton.setOnClickListener {
            if (currentMonth < 11) {
                currentMonth++
            } else {
                currentMonth = 0
                currentYear++
            }
            updateCalendar()
        }
        
        binding.shareStreakButton.setOnClickListener {
            shareStreakProgress()
        }
    }

    private fun updateCalendar() {
        calendarAdapter = StreakCalendarAdapter(requireContext(), getStreakData())
        binding.streakCalendarGrid.adapter = calendarAdapter
        updateMonthHeader()
        updateStreakStats()
    }

    private fun updateMonthHeader() {
        val monthNames = arrayOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
        binding.monthYearText.text = "${monthNames[currentMonth]} $currentYear"
    }

    private fun getStreakData(): List<StreakDay> {
        val habits = getHabits()
        val moods = getMoods()
        val streakDays = mutableListOf<StreakDay>()
        
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        for (day in 1..daysInMonth) {
            calendar.set(currentYear, currentMonth, day)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val completedHabits = habits.count { it.completedDates.contains(dateStr) }
            val dayMoods = moods.filter { 
                val moodDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
                moodDate == dateStr
            }
            
            val isToday = isToday(calendar.time)
            val streakLevel = when {
                completedHabits >= 3 && dayMoods.isNotEmpty() -> StreakLevel.EXCELLENT
                completedHabits >= 2 -> StreakLevel.GOOD
                completedHabits >= 1 -> StreakLevel.OK
                else -> StreakLevel.NONE
            }
            
            streakDays.add(StreakDay(
                day = day,
                date = dateStr,
                completedHabits = completedHabits,
                totalHabits = habits.size,
                moodCount = dayMoods.size,
                streakLevel = streakLevel,
                isToday = isToday
            ))
        }
        
        return streakDays
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val checkDate = Calendar.getInstance()
        checkDate.time = date
        
        return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) &&
               today.get(Calendar.MONTH) == checkDate.get(Calendar.MONTH) &&
               today.get(Calendar.DAY_OF_MONTH) == checkDate.get(Calendar.DAY_OF_MONTH)
    }

    private fun updateStreakStats() {
        val habits = getHabits()
        val currentStreak = calculateCurrentStreak(habits)
        val bestStreak = calculateBestStreak(habits)
        val monthlyCompletion = calculateMonthlyCompletion()
        
        binding.currentStreakText.text = "$currentStreak days"
        binding.bestStreakText.text = "$bestStreak days"
        binding.monthlyCompletionText.text = "${monthlyCompletion}%"
        
        // Update streak motivation
        val motivation = when {
            currentStreak >= 30 -> "🏆 You're a wellness champion!"
            currentStreak >= 14 -> "🔥 Two weeks strong! Keep it up!"
            currentStreak >= 7 -> "⭐ One week streak! Amazing!"
            currentStreak >= 3 -> "💪 Building momentum!"
            else -> "🌱 Start your wellness journey today!"
        }
        
        binding.streakMotivationText.text = motivation
    }

    private fun calculateCurrentStreak(habits: List<Habit>): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calendar = Calendar.getInstance()
        var streak = 0
        
        while (true) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val hasActivity = habits.any { it.completedDates.contains(dateStr) }
            
            if (hasActivity) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        return streak
    }

    private fun calculateBestStreak(habits: List<Habit>): Int {
        return habits.maxOfOrNull { HabitsFragmentHelper.calculateStreak(it) } ?: 0
    }

    private fun calculateMonthlyCompletion(): Int {
        val habits = getHabits()
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        var totalPossible = 0
        var totalCompleted = 0
        
        for (day in 1..daysInMonth) {
            calendar.set(currentYear, currentMonth, day)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            totalPossible += habits.size
            totalCompleted += habits.count { it.completedDates.contains(dateStr) }
        }
        
        return if (totalPossible > 0) ((totalCompleted.toDouble() / totalPossible) * 100).toInt() else 0
    }

    private fun shareStreakProgress() {
        val currentStreak = calculateCurrentStreak(getHabits())
        val bestStreak = calculateBestStreak(getHabits())
        val monthlyCompletion = calculateMonthlyCompletion()
        
        val shareText = """
            📅 My Wellness Streak Calendar
            
            🔥 Current Streak: $currentStreak days
            🏆 Best Streak: $bestStreak days
            📊 Monthly Completion: $monthlyCompletion%
            
            💪 Building healthy habits one day at a time!
            
            #WellnessJourney #HealthyHabits #StreakCalendar #SLIITApp
        """.trimIndent()
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Wellness Streak Progress")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Streak Progress"))
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

data class StreakDay(
    val day: Int,
    val date: String,
    val completedHabits: Int,
    val totalHabits: Int,
    val moodCount: Int,
    val streakLevel: StreakLevel,
    val isToday: Boolean
)

enum class StreakLevel {
    NONE, OK, GOOD, EXCELLENT
}
