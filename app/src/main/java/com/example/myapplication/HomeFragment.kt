package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentHomeBinding
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchResultsAdapter
    private var selectedDate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedDate = todayKey()
        updateSelectedDateInfo()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            updateSelectedDateInfo()
        }

        searchAdapter = SearchResultsAdapter()
        binding.searchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.searchResults.adapter = searchAdapter

        binding.searchEdit.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                performSearch(query ?: "")
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.searchResults.visibility = View.GONE
                    binding.selectedDateCard.visibility = View.VISIBLE
                } else {
                    performSearch(newText)
                }
                return true
            }
        })

        binding.addQuickHabit.setOnClickListener {
            findNavController().navigate(R.id.FirstFragment)
        }

        binding.addQuickMood.setOnClickListener {
            findNavController().navigate(R.id.MoodFragment)
        }

        // Top Navigation Bar
        binding.root.findViewById<androidx.cardview.widget.CardView>(R.id.navHabits)?.setOnClickListener {
            findNavController().navigate(R.id.FirstFragment)
        }

        binding.root.findViewById<androidx.cardview.widget.CardView>(R.id.navMood)?.setOnClickListener {
            findNavController().navigate(R.id.MoodFragment)
        }

        binding.root.findViewById<androidx.cardview.widget.CardView>(R.id.navTimer)?.setOnClickListener {
            findNavController().navigate(R.id.TimerFragment)
        }

        binding.root.findViewById<androidx.cardview.widget.CardView>(R.id.settingsIcon)?.setOnClickListener {
            findNavController().navigate(R.id.SettingsFragment)
        }

        // Add analytics and goals navigation
        binding.habitsCompletedText.setOnClickListener {
            findNavController().navigate(R.id.AnalyticsFragment)
        }

        binding.moodsLoggedText.setOnClickListener {
            findNavController().navigate(R.id.AnalyticsFragment)
        }

        // Add access to new features
        binding.calendarView.setOnClickListener {
            findNavController().navigate(R.id.StreakCalendarFragment)
        }

        // Quick Action Cards
        binding.root.findViewById<androidx.cardview.widget.CardView>(R.id.quickHydration)?.setOnClickListener {
            findNavController().navigate(R.id.WaterTrackingFragment)
        }

        binding.root.findViewById<androidx.cardview.widget.CardView>(R.id.quickMeditation)?.setOnClickListener {
            findNavController().navigate(R.id.MeditationFragment)
        }

        // Note: Floating Action Buttons were removed from layout
        // Quick add and share functionality is available through other UI elements
    }

    private fun updateSelectedDateInfo() {
        val habits = getHabits()
        val moods = getMoods()
        
        val completedHabits = habits.filter { it.completedDates.contains(selectedDate) }
        val dayMoods = moods.filter { 
            val moodDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
            moodDate == selectedDate
        }

        binding.selectedDateText.text = "📍 Selected: $selectedDate"
        binding.habitsCompletedText.text = "${completedHabits.size}/${habits.size}"
        binding.moodsLoggedText.text = "${dayMoods.size}"
        
        if (dayMoods.isNotEmpty()) {
            binding.moodSummary.text = "Moods: ${dayMoods.joinToString(" ") { it.emoji }}"
            binding.moodSummary.visibility = View.VISIBLE
        } else {
            binding.moodSummary.visibility = View.GONE
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            binding.searchResults.visibility = View.GONE
            binding.selectedDateCard.visibility = View.VISIBLE
            return
        }

        binding.searchResults.visibility = View.VISIBLE
        binding.selectedDateCard.visibility = View.GONE

        val results = mutableListOf<SearchResult>()
        
        // Search habits
        getHabits().forEach { habit ->
            if (habit.name.contains(query, ignoreCase = true)) {
                results.add(SearchResult("Habit", habit.name, "Streak: ${HabitsFragmentHelper.calculateStreak(habit)} days"))
            }
        }

        // Search moods
        getMoods().forEach { mood ->
            if (mood.note.contains(query, ignoreCase = true)) {
                val date = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(mood.timestamp))
                results.add(SearchResult("Mood", "${mood.emoji} ${mood.note}", date))
            }
        }

        searchAdapter.submitList(results)
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

    private fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun trackHydration() {
        // Add hydration tracking
        val prefs = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
        val today = todayKey()
        val currentCount = prefs.getInt("hydration_$today", 0)
        prefs.edit().putInt("hydration_$today", currentCount + 1).apply()
        
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            binding.root, 
            "💧 Water tracked! Total today: ${currentCount + 1} glasses", 
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Settings") {
            findNavController().navigate(R.id.SettingsFragment)
        }
        snackbar.show()
    }

    private fun startMeditation() {
        // Quick meditation timer
        val options = arrayOf("2 minutes", "5 minutes", "10 minutes", "Custom")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🧘‍♀️ Start Meditation")
            .setItems(options) { _, which ->
                val minutes = when (which) {
                    0 -> 2
                    1 -> 5
                    2 -> 10
                    else -> 5 // Default
                }
                startMeditationTimer(minutes)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startMeditationTimer(minutes: Int) {
        // Navigate to timer with preset time
        findNavController().navigate(R.id.TimerFragment)
        
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            binding.root, 
            "🧘‍♀️ Starting $minutes minute meditation", 
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
        snackbar.show()
    }

    private fun showQuickAddDialog() {
        val options = arrayOf(
            "🎯 Professional Add Habit", 
            "😊 Professional Add Mood", 
            "💧 Water Tracking", 
            "🧘‍♀️ Meditation",
            "⏰ Start Timer",
            "📊 Dashboard",
            "📈 Analytics",
            "🏆 Set Goal",
            "📅 Streak Calendar",
            "📈 Weekly Progress",
            "📚 Wellness Tips"
        )
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚡ Quick Add")
            .setMessage(WellnessQuotes.getRandomQuote())
            .setItems(options) { _, which ->
                when (which) {
                    0 -> findNavController().navigate(R.id.AddHabitFragment)
                    1 -> findNavController().navigate(R.id.AddMoodFragment)
                    2 -> findNavController().navigate(R.id.WaterTrackingFragment)
                    3 -> findNavController().navigate(R.id.MeditationFragment)
                    4 -> findNavController().navigate(R.id.TimerFragment)
                    5 -> findNavController().navigate(R.id.DashboardFragment)
                    6 -> findNavController().navigate(R.id.AnalyticsFragment)
                    7 -> findNavController().navigate(R.id.GoalsFragment)
                    8 -> findNavController().navigate(R.id.StreakCalendarFragment)
                    9 -> findNavController().navigate(R.id.WeeklyProgressFragment)
                    10 -> findNavController().navigate(R.id.WellnessTipsFragment)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareProgress() {
        val habits = getHabits()
        val moods = getMoods()
        val today = todayKey()
        val completedHabits = habits.count { it.completedDates.contains(today) }
        val totalHabits = habits.size
        val todayMoods = moods.filter { 
            val moodDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
            moodDate == today
        }
        
        val progressText = """
            📊 Today's Wellness Progress
            
            🎯 Habits: $completedHabits/$totalHabits completed
            😊 Moods logged: ${todayMoods.size}
            ${if (todayMoods.isNotEmpty()) "Today's mood: ${todayMoods.joinToString(" ") { it.emoji }}" else ""}
            
            📅 Date: $today
            
            💪 Keep up the great work!
            
            #WellnessJourney #HealthyHabits #SLIITApp
        """.trimIndent()
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, progressText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Wellness Progress")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Progress"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class MoodEntry(
    val emoji: String,
    val note: String,
    val timestamp: Long
)

data class SearchResult(
    val type: String,
    val title: String,
    val subtitle: String
)
