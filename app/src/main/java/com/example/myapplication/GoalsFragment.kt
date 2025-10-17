package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentGoalsBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var goalsAdapter: GoalsAdapter
    private lateinit var challengesAdapter: ChallengesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        loadGoalsAndChallenges()
        setupClickListeners()
        updateDailyWisdom()
    }

    private fun setupRecyclerViews() {
        goalsAdapter = GoalsAdapter { goal -> toggleGoalProgress(goal) }
        binding.goalsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.goalsRecyclerView.adapter = goalsAdapter
        
        challengesAdapter = ChallengesAdapter { challenge -> joinChallenge(challenge) }
        binding.challengesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.challengesRecyclerView.adapter = challengesAdapter
    }

    private fun loadGoalsAndChallenges() {
        val goals = getGoals()
        val challenges = getAvailableChallenges()
        
        goalsAdapter.submitList(goals)
        challengesAdapter.submitList(challenges)
        
        updateGoalsProgress(goals)
    }

    private fun updateGoalsProgress(goals: List<Goal>) {
        val completedGoals = goals.count { it.isCompleted }
        val totalGoals = goals.size
        
        binding.goalsProgressText.text = if (totalGoals > 0) {
            "Goals Progress: $completedGoals/$totalGoals"
        } else {
            "No goals set yet"
        }
        
        val progressPercentage = if (totalGoals > 0) (completedGoals * 100) / totalGoals else 0
        binding.goalsProgressBar.progress = progressPercentage
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.addGoalButton.setOnClickListener {
            showAddGoalDialog()
        }
        
        binding.refreshWisdomButton.setOnClickListener {
            updateDailyWisdom()
        }
    }

    private fun showAddGoalDialog() {
        val goalTypes = arrayOf(
            "🎯 Complete habits daily for a week",
            "🔥 Achieve 10-day habit streak", 
            "😊 Log mood daily for 5 days",
            "💧 Drink 8 glasses water daily",
            "🧘‍♀️ Meditate 3 times this week",
            "📝 Write gratitude notes for 7 days",
            "🚶‍♀️ Walk 10,000 steps daily",
            "📚 Read for 30 minutes daily",
            "💪 Exercise 4 times this week",
            "🌱 Try 3 new healthy habits"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎯 Add New Goal")
            .setItems(goalTypes) { _, which ->
                val selectedGoal = goalTypes[which]
                addNewGoal(selectedGoal)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNewGoal(goalText: String) {
        val newGoal = Goal(
            id = System.currentTimeMillis().toString(),
            title = goalText,
            description = "Complete this goal to boost your wellness journey!",
            targetDays = when {
                goalText.contains("week") -> 7
                goalText.contains("10-day") -> 10
                goalText.contains("5 days") -> 5
                else -> 7
            },
            currentProgress = 0,
            isCompleted = false,
            createdDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
        
        saveGoal(newGoal)
        loadGoalsAndChallenges()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "🎯 Goal added! ${WellnessQuotes.getRandomQuote()}",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    private fun toggleGoalProgress(goal: Goal) {
        val updatedGoal = goal.copy(
            currentProgress = if (goal.currentProgress < goal.targetDays) goal.currentProgress + 1 else goal.currentProgress,
            isCompleted = goal.currentProgress + 1 >= goal.targetDays
        )
        
        saveGoal(updatedGoal)
        loadGoalsAndChallenges()
        
        if (updatedGoal.isCompleted && !goal.isCompleted) {
            showGoalCompletionCelebration(updatedGoal)
        } else {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "💪 Progress updated! ${WellnessQuotes.getHabitEncouragement()}",
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun showGoalCompletionCelebration(goal: Goal) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎉 Goal Completed!")
            .setMessage("${WellnessQuotes.getAchievementCelebration()}\n\nYou've successfully completed: ${goal.title}")
            .setPositiveButton("Share Achievement") { _, _ ->
                shareGoalAchievement(goal)
            }
            .setNegativeButton("Continue", null)
            .show()
    }

    private fun shareGoalAchievement(goal: Goal) {
        val shareText = """
            🎉 Goal Achievement Unlocked!
            
            ✅ ${goal.title}
            
            💪 Completed in ${goal.targetDays} days!
            
            ${WellnessQuotes.getAchievementCelebration()}
            
            #WellnessGoals #Achievement #HealthyHabits #SLIITApp
        """.trimIndent()
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Wellness Goal Achievement!")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Achievement"))
    }

    private fun joinChallenge(challenge: Challenge) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🏆 Join Challenge")
            .setMessage("Ready to take on the ${challenge.title}?\n\n${challenge.description}")
            .setPositiveButton("Join Challenge") { _, _ ->
                val goalFromChallenge = Goal(
                    id = System.currentTimeMillis().toString(),
                    title = "🏆 ${challenge.title}",
                    description = challenge.description,
                    targetDays = challenge.durationDays,
                    currentProgress = 0,
                    isCompleted = false,
                    createdDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
                
                saveGoal(goalFromChallenge)
                loadGoalsAndChallenges()
                
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "🏆 Challenge accepted! Let's do this!",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Maybe Later", null)
            .show()
    }

    private fun updateDailyWisdom() {
        binding.dailyWisdomText.text = WellnessQuotes.getDailyWisdom()
    }

    private fun getAvailableChallenges(): List<Challenge> {
        return listOf(
            Challenge(
                id = "week_warrior",
                title = "Week Warrior",
                description = "Complete all your habits for 7 consecutive days",
                durationDays = 7,
                emoji = "⚔️"
            ),
            Challenge(
                id = "mood_master",
                title = "Mood Master", 
                description = "Log your mood every day for 14 days",
                durationDays = 14,
                emoji = "😊"
            ),
            Challenge(
                id = "hydration_hero",
                title = "Hydration Hero",
                description = "Drink 8 glasses of water daily for 10 days",
                durationDays = 10,
                emoji = "💧"
            ),
            Challenge(
                id = "meditation_monk",
                title = "Meditation Monk",
                description = "Meditate for at least 10 minutes daily for 21 days",
                durationDays = 21,
                emoji = "🧘‍♀️"
            ),
            Challenge(
                id = "gratitude_guru",
                title = "Gratitude Guru",
                description = "Write 3 things you're grateful for daily for 30 days",
                durationDays = 30,
                emoji = "🙏"
            )
        )
    }

    private fun prefs() = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)

    private fun getGoals(): List<Goal> {
        val json = prefs().getString("goals_json", "[]")
        val arr = JSONArray(json)
        val list = mutableListOf<Goal>()
        
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val goal = Goal(
                id = obj.optString("id"),
                title = obj.optString("title"),
                description = obj.optString("description"),
                targetDays = obj.optInt("targetDays"),
                currentProgress = obj.optInt("currentProgress"),
                isCompleted = obj.optBoolean("isCompleted"),
                createdDate = obj.optString("createdDate")
            )
            list.add(goal)
        }
        
        return list
    }

    private fun saveGoal(goal: Goal) {
        val goals = getGoals().toMutableList()
        val existingIndex = goals.indexOfFirst { it.id == goal.id }
        
        if (existingIndex >= 0) {
            goals[existingIndex] = goal
        } else {
            goals.add(goal)
        }
        
        val jsonArray = JSONArray()
        goals.forEach { g ->
            val obj = JSONObject().apply {
                put("id", g.id)
                put("title", g.title)
                put("description", g.description)
                put("targetDays", g.targetDays)
                put("currentProgress", g.currentProgress)
                put("isCompleted", g.isCompleted)
                put("createdDate", g.createdDate)
            }
            jsonArray.put(obj)
        }
        
        prefs().edit().putString("goals_json", jsonArray.toString()).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Goal(
    val id: String,
    val title: String,
    val description: String,
    val targetDays: Int,
    val currentProgress: Int,
    val isCompleted: Boolean,
    val createdDate: String
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val durationDays: Int,
    val emoji: String
)
