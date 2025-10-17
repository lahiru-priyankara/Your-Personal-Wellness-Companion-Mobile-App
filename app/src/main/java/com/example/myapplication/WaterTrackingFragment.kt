package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentWaterTrackingBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class WaterTrackingFragment : Fragment() {

    private var _binding: FragmentWaterTrackingBinding? = null
    private val binding get() = _binding!!
    
    private var dailyGoal = 8 // glasses
    private var currentIntake = 0
    private var today = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadWaterData()
        setupClickListeners()
        updateWaterDisplay()
        setupWaterReminders()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.addWaterButton.setOnClickListener {
            addWaterIntake()
        }
        
        binding.minusWaterButton.setOnClickListener {
            removeWaterIntake()
        }
        
        binding.setGoalButton.setOnClickListener {
            showGoalDialog()
        }
        
        binding.waterHistoryButton.setOnClickListener {
            showWaterHistory()
        }
        
        binding.shareWaterButton.setOnClickListener {
            shareWaterProgress()
        }
        
        // Quick add buttons
        binding.quickAdd50ml.setOnClickListener {
            addWaterAmount(50)
        }
        
        binding.quickAdd100ml.setOnClickListener {
            addWaterAmount(100)
        }
        
        binding.quickAdd200ml.setOnClickListener {
            addWaterAmount(200)
        }
        
        binding.quickAdd250ml.setOnClickListener {
            addWaterAmount(250)
        }
    }

    private fun loadWaterData() {
        val prefs = prefs()
        dailyGoal = prefs.getInt("water_daily_goal", 8)
        currentIntake = prefs.getInt("water_$today", 0)
        
        binding.dailyGoalText.text = "$dailyGoal glasses"
        binding.currentIntakeText.text = "$currentIntake glasses"
    }

    private fun addWaterIntake() {
        currentIntake++
        saveWaterData()
        updateWaterDisplay()
        showWaterFeedback()
    }

    private fun removeWaterIntake() {
        if (currentIntake > 0) {
            currentIntake--
            saveWaterData()
            updateWaterDisplay()
        }
    }

    private fun addWaterAmount(ml: Int) {
        val glasses = ml / 250 // Assuming 250ml per glass
        currentIntake += glasses
        saveWaterData()
        updateWaterDisplay()
        showWaterFeedback()
    }

    private fun saveWaterData() {
        prefs().edit().putInt("water_$today", currentIntake).apply()
    }

    private fun updateWaterDisplay() {
        binding.currentIntakeText.text = "$currentIntake glasses"
        binding.progressText.text = "$currentIntake / $dailyGoal"
        
        val progressPercentage = if (dailyGoal > 0) (currentIntake * 100) / dailyGoal else 0
        binding.waterProgressBar.progress = progressPercentage
        
        // Update progress color based on completion
        when {
            progressPercentage >= 100 -> {
                binding.waterProgressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                binding.progressStatusText.text = "🎉 Goal achieved! Great job!"
                binding.progressStatusText.setTextColor(Color.parseColor("#4CAF50"))
            }
            progressPercentage >= 75 -> {
                binding.waterProgressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#8BC34A"))
                binding.progressStatusText.text = "💪 Almost there! Keep going!"
                binding.progressStatusText.setTextColor(Color.parseColor("#8BC34A"))
            }
            progressPercentage >= 50 -> {
                binding.waterProgressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFC107"))
                binding.progressStatusText.text = "🌊 Good progress! Stay hydrated!"
                binding.progressStatusText.setTextColor(Color.parseColor("#FFC107"))
            }
            else -> {
                binding.waterProgressBar.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
                binding.progressStatusText.text = "💧 Let's start hydrating!"
                binding.progressStatusText.setTextColor(Color.parseColor("#FF9800"))
            }
        }
        
        // Update hydration tips
        updateHydrationTips(progressPercentage)
    }

    private fun updateHydrationTips(progress: Int) {
        val tips = when {
            progress >= 100 -> listOf(
                "🎉 Excellent! You've met your hydration goal!",
                "💪 Keep up this great habit!",
                "🌟 Your body will thank you for staying hydrated!"
            )
            progress >= 75 -> listOf(
                "💧 You're almost at your goal!",
                "🌊 Just a bit more water to go!",
                "💪 You're doing great with hydration!"
            )
            progress >= 50 -> listOf(
                "🌊 You're halfway to your hydration goal!",
                "💧 Keep drinking water throughout the day!",
                "💪 Good progress on your hydration journey!"
            )
            else -> listOf(
                "💧 Start your day with a glass of water!",
                "🌊 Hydration is key to feeling your best!",
                "💪 Every glass of water counts towards your health!"
            )
        }
        
        binding.hydrationTipText.text = tips.random()
    }

    private fun showWaterFeedback() {
        val feedback = when {
            currentIntake >= dailyGoal -> "🎉 Goal achieved! You're a hydration champion!"
            currentIntake >= dailyGoal * 0.75 -> "💪 Almost there! Just ${dailyGoal - currentIntake} more glasses!"
            currentIntake >= dailyGoal * 0.5 -> "🌊 Good progress! Keep hydrating!"
            else -> "💧 Great start! Every glass counts!"
        }
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            feedback,
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showGoalDialog() {
        val goalOptions = arrayOf("6 glasses", "8 glasses", "10 glasses", "12 glasses", "Custom")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💧 Set Daily Water Goal")
            .setItems(goalOptions) { _, which ->
                when (which) {
                    0 -> setDailyGoal(6)
                    1 -> setDailyGoal(8)
                    2 -> setDailyGoal(10)
                    3 -> setDailyGoal(12)
                    4 -> showCustomGoalDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setDailyGoal(goal: Int) {
        dailyGoal = goal
        prefs().edit().putInt("water_daily_goal", goal).apply()
        updateWaterDisplay()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "💧 Daily goal set to $goal glasses!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showCustomGoalDialog() {
        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Enter glasses per day"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💧 Custom Water Goal")
            .setView(input)
            .setPositiveButton("Set Goal") { _, _ ->
                val goal = input.text.toString().toIntOrNull() ?: 8
                if (goal in 1..20) {
                    setDailyGoal(goal)
                } else {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        "Please enter a goal between 1-20 glasses",
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWaterHistory() {
        val history = getWaterHistory()
        val historyText = if (history.isNotEmpty()) {
            "📊 Your Water Intake History:\n\n" + history.joinToString("\n")
        } else {
            "📊 No water intake history yet. Start tracking to see your progress!"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💧 Water History")
            .setMessage(historyText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getWaterHistory(): List<String> {
        val prefs = prefs()
        val history = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        
        for (i in 0..6) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val intake = prefs.getInt("water_$date", 0)
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
            history.add("$dayName: $intake glasses")
        }
        
        return history
    }

    private fun shareWaterProgress() {
        val progressPercentage = if (dailyGoal > 0) (currentIntake * 100) / dailyGoal else 0
        
        val shareText = """
            💧 My Hydration Progress Today
            
            🥤 Water Intake: $currentIntake / $dailyGoal glasses
            📊 Progress: $progressPercentage%
            
            ${if (progressPercentage >= 100) "🎉 Goal achieved! I'm staying hydrated!" else "💪 Working towards my hydration goal!"}
            
            #Hydration #HealthyLiving #WellnessJourney #SLIITApp
        """.trimIndent()
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Hydration Progress")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Hydration Progress"))
    }

    private fun setupWaterReminders() {
        val prefs = prefs()
        val remindersEnabled = prefs.getBoolean("water_reminders_enabled", true)
        val reminderInterval = prefs.getInt("water_reminder_interval", 2) // hours
        
        if (remindersEnabled) {
            binding.reminderStatusText.text = "🔔 Reminders every $reminderInterval hours"
            binding.reminderStatusText.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            binding.reminderStatusText.text = "🔕 Reminders disabled"
            binding.reminderStatusText.setTextColor(Color.parseColor("#757575"))
        }
        
        binding.reminderSettingsButton.setOnClickListener {
            showReminderSettings()
        }
    }

    private fun showReminderSettings() {
        val options = arrayOf("Every 1 hour", "Every 2 hours", "Every 3 hours", "Disable reminders")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔔 Water Reminder Settings")
            .setItems(options) { _, which ->
                val prefs = prefs()
                when (which) {
                    0 -> {
                        prefs.edit().putBoolean("water_reminders_enabled", true).putInt("water_reminder_interval", 1).apply()
                        setupWaterReminders()
                    }
                    1 -> {
                        prefs.edit().putBoolean("water_reminders_enabled", true).putInt("water_reminder_interval", 2).apply()
                        setupWaterReminders()
                    }
                    2 -> {
                        prefs.edit().putBoolean("water_reminders_enabled", true).putInt("water_reminder_interval", 3).apply()
                        setupWaterReminders()
                    }
                    3 -> {
                        prefs.edit().putBoolean("water_reminders_enabled", false).apply()
                        setupWaterReminders()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun prefs() = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
