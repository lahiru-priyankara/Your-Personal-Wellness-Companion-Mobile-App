package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentAdvancedSettingsBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AdvancedSettingsFragment : Fragment() {

    private var _binding: FragmentAdvancedSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvancedSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        loadSettings()
        updateSettingsDisplay()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Data Management
        binding.exportDataButton.setOnClickListener {
            exportAllData()
        }
        
        binding.importDataButton.setOnClickListener {
            importData()
        }
        
        binding.clearAllDataButton.setOnClickListener {
            showClearDataDialog()
        }
        
        binding.backupSettingsButton.setOnClickListener {
            backupSettings()
        }
        
        binding.restoreSettingsButton.setOnClickListener {
            restoreSettings()
        }
        
        // Analytics & Insights
        binding.analyticsSettingsButton.setOnClickListener {
            showAnalyticsSettings()
        }
        
        binding.insightsSettingsButton.setOnClickListener {
            showInsightsSettings()
        }
        
        binding.privacySettingsButton.setOnClickListener {
            showPrivacySettings()
        }
        
        // App Behavior
        binding.notificationSettingsButton.setOnClickListener {
            showNotificationSettings()
        }
        
        binding.themeSettingsButton.setOnClickListener {
            showThemeSettings()
        }
        
        binding.languageSettingsButton.setOnClickListener {
            showLanguageSettings()
        }
        
        // Advanced Features
        binding.widgetSettingsButton.setOnClickListener {
            showWidgetSettings()
        }
        
        binding.syncSettingsButton.setOnClickListener {
            showSyncSettings()
        }
        
        binding.debugSettingsButton.setOnClickListener {
            showDebugSettings()
        }
    }

    private fun loadSettings() {
        val prefs = prefs()
        
        // Load current settings
        val analyticsEnabled = prefs.getBoolean("analytics_enabled", true)
        val insightsEnabled = prefs.getBoolean("insights_enabled", true)
        val privacyMode = prefs.getBoolean("privacy_mode", false)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        val autoBackup = prefs.getBoolean("auto_backup", false)
        val syncEnabled = prefs.getBoolean("sync_enabled", false)
        
        updateSettingsStatus()
    }

    private fun updateSettingsDisplay() {
        updateSettingsStatus()
        updateDataStats()
    }

    private fun updateSettingsStatus() {
        val prefs = prefs()
        
        // Analytics
        val analyticsEnabled = prefs.getBoolean("analytics_enabled", true)
        binding.analyticsStatusText.text = if (analyticsEnabled) "✅ Enabled" else "❌ Disabled"
        binding.analyticsStatusText.setTextColor(if (analyticsEnabled) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        
        // Insights
        val insightsEnabled = prefs.getBoolean("insights_enabled", true)
        binding.insightsStatusText.text = if (insightsEnabled) "✅ Enabled" else "❌ Disabled"
        binding.insightsStatusText.setTextColor(if (insightsEnabled) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        
        // Privacy
        val privacyMode = prefs.getBoolean("privacy_mode", false)
        binding.privacyStatusText.text = if (privacyMode) "🔒 Privacy Mode" else "🌐 Standard Mode"
        binding.privacyStatusText.setTextColor(if (privacyMode) Color.parseColor("#FF9800") else Color.parseColor("#2196F3"))
        
        // Notifications
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        binding.notificationStatusText.text = if (notificationsEnabled) "🔔 Enabled" else "🔕 Disabled"
        binding.notificationStatusText.setTextColor(if (notificationsEnabled) Color.parseColor("#4CAF50") else Color.parseColor("#757575"))
        
        // Auto Backup
        val autoBackup = prefs.getBoolean("auto_backup", false)
        binding.backupStatusText.text = if (autoBackup) "🔄 Auto Backup" else "📁 Manual Backup"
        binding.backupStatusText.setTextColor(if (autoBackup) Color.parseColor("#4CAF50") else Color.parseColor("#FF9800"))
        
        // Sync
        val syncEnabled = prefs.getBoolean("sync_enabled", false)
        binding.syncStatusText.text = if (syncEnabled) "☁️ Sync Enabled" else "📱 Local Only"
        binding.syncStatusText.setTextColor(if (syncEnabled) Color.parseColor("#4CAF50") else Color.parseColor("#757575"))
    }

    private fun updateDataStats() {
        val habits = getHabits()
        val moods = getMoods()
        val goals = getGoals()
        val challenges = getChallenges()
        
        binding.habitsCountText.text = "${habits.size} habits"
        binding.moodsCountText.text = "${moods.size} mood entries"
        binding.goalsCountText.text = "${goals.size} goals"
        binding.challengesCountText.text = "${challenges.size} challenges"
        
        val totalData = habits.size + moods.size + goals.size + challenges.size
        binding.totalDataText.text = "$totalData total entries"
    }

    private fun exportAllData() {
        val habits = getHabits()
        val moods = getMoods()
        val goals = getGoals()
        val challenges = getChallenges()
        
        val exportData = JSONObject().apply {
            put("export_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put("app_version", "1.0.0")
            put("habits", JSONArray().apply {
                habits.forEach { habit ->
                    put(JSONObject().apply {
                        put("name", habit.name)
                        put("completedDates", JSONArray().apply {
                            habit.completedDates.forEach { date -> put(date) }
                        })
                    })
                }
            })
            put("moods", JSONArray().apply {
                moods.forEach { mood ->
                    put(JSONObject().apply {
                        put("emoji", mood.emoji)
                        put("note", mood.note)
                        put("timestamp", mood.timestamp)
                    })
                }
            })
            put("goals", JSONArray().apply {
                goals.forEach { goal ->
                    put(JSONObject().apply {
                        put("id", goal.id)
                        put("title", goal.title)
                        put("description", goal.description)
                        put("targetDays", goal.targetDays)
                        put("currentProgress", goal.currentProgress)
                        put("isCompleted", goal.isCompleted)
                        put("createdDate", goal.createdDate)
                    })
                }
            })
            put("challenges", JSONArray().apply {
                challenges.forEach { challenge ->
                    put(JSONObject().apply {
                        put("id", challenge.id)
                        put("title", challenge.title)
                        put("description", challenge.description)
                        put("durationDays", challenge.durationDays)
                        put("emoji", challenge.emoji)
                    })
                }
            })
        }
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(android.content.Intent.EXTRA_TEXT, exportData.toString())
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Wellness App Data Export")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Export Data"))
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "📤 Data exported successfully!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun importData() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📥 Import Data")
            .setMessage("To import data, you need to have a JSON file exported from this app. This will replace all current data.")
            .setPositiveButton("Import") { _, _ ->
                // In a real app, you would implement file picker here
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "📥 Import feature requires file picker implementation",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearDataDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Clear All Data")
            .setMessage("This will permanently delete all your habits, moods, goals, and challenges. This action cannot be undone.")
            .setPositiveButton("Clear All Data") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllData() {
        val prefs = prefs()
        prefs.edit()
            .remove("habits_json")
            .remove("moods_json")
            .remove("goals_json")
            .remove("challenges_json")
            .apply()
        
        updateDataStats()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "🗑️ All data cleared successfully!",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    private fun backupSettings() {
        val prefs = prefs()
        val settings = JSONObject().apply {
            put("backup_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put("theme", prefs.getString("selected_theme", "0"))
            put("language", prefs.getString("selected_language", "0"))
            put("notifications_enabled", prefs.getBoolean("notifications_enabled", true))
            put("analytics_enabled", prefs.getBoolean("analytics_enabled", true))
            put("insights_enabled", prefs.getBoolean("insights_enabled", true))
            put("privacy_mode", prefs.getBoolean("privacy_mode", false))
            put("auto_backup", prefs.getBoolean("auto_backup", false))
            put("sync_enabled", prefs.getBoolean("sync_enabled", false))
        }
        
        prefs.edit().putString("settings_backup", settings.toString()).apply()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "💾 Settings backed up successfully!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun restoreSettings() {
        val prefs = prefs()
        val backupJson = prefs.getString("settings_backup", null)
        
        if (backupJson != null) {
            try {
                val settings = JSONObject(backupJson)
                prefs.edit()
                    .putString("selected_theme", settings.optString("theme", "0"))
                    .putString("selected_language", settings.optString("language", "0"))
                    .putBoolean("notifications_enabled", settings.optBoolean("notifications_enabled", true))
                    .putBoolean("analytics_enabled", settings.optBoolean("analytics_enabled", true))
                    .putBoolean("insights_enabled", settings.optBoolean("insights_enabled", true))
                    .putBoolean("privacy_mode", settings.optBoolean("privacy_mode", false))
                    .putBoolean("auto_backup", settings.optBoolean("auto_backup", false))
                    .putBoolean("sync_enabled", settings.optBoolean("sync_enabled", false))
                    .apply()
                
                updateSettingsDisplay()
                
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "🔄 Settings restored successfully!",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "❌ Failed to restore settings",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "❌ No backup found",
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun showAnalyticsSettings() {
        val options = arrayOf("Enable Analytics", "Disable Analytics", "View Analytics Data")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Analytics Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> setAnalyticsEnabled(true)
                    1 -> setAnalyticsEnabled(false)
                    2 -> showAnalyticsData()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setAnalyticsEnabled(enabled: Boolean) {
        prefs().edit().putBoolean("analytics_enabled", enabled).apply()
        updateSettingsDisplay()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            if (enabled) "📊 Analytics enabled" else "📊 Analytics disabled",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showAnalyticsData() {
        val habits = getHabits()
        val moods = getMoods()
        val totalHabits = habits.size
        val totalMoods = moods.size
        val completedToday = habits.count { it.completedDates.contains(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
        
        val analyticsText = """
            📊 Analytics Data
            
            Total Habits: $totalHabits
            Total Mood Entries: $totalMoods
            Completed Today: $completedToday
            
            Most Active Habit: ${if (habits.isNotEmpty()) habits.maxByOrNull { it.completedDates.size }?.name ?: "None" else "None"}
            Average Mood: ${if (moods.isNotEmpty()) "Positive" else "No data"}
            
            Data Collection: ${if (prefs().getBoolean("analytics_enabled", true)) "Enabled" else "Disabled"}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Analytics Overview")
            .setMessage(analyticsText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showInsightsSettings() {
        val options = arrayOf("Enable Insights", "Disable Insights", "Reset Insights")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💡 Insights Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> setInsightsEnabled(true)
                    1 -> setInsightsEnabled(false)
                    2 -> resetInsights()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setInsightsEnabled(enabled: Boolean) {
        prefs().edit().putBoolean("insights_enabled", enabled).apply()
        updateSettingsDisplay()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            if (enabled) "💡 Insights enabled" else "💡 Insights disabled",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun resetInsights() {
        prefs().edit().remove("insights_data").apply()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "🔄 Insights reset successfully!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showPrivacySettings() {
        val options = arrayOf("Enable Privacy Mode", "Disable Privacy Mode", "Clear Personal Data")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔒 Privacy Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> setPrivacyMode(true)
                    1 -> setPrivacyMode(false)
                    2 -> clearPersonalData()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setPrivacyMode(enabled: Boolean) {
        prefs().edit().putBoolean("privacy_mode", enabled).apply()
        updateSettingsDisplay()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            if (enabled) "🔒 Privacy mode enabled" else "🌐 Privacy mode disabled",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun clearPersonalData() {
        prefs().edit()
            .remove("user_name")
            .remove("user_email")
            .remove("personal_notes")
            .apply()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "🗑️ Personal data cleared!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showNotificationSettings() {
        val options = arrayOf("Enable All Notifications", "Disable All Notifications", "Customize Notifications")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔔 Notification Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> setNotificationsEnabled(true)
                    1 -> setNotificationsEnabled(false)
                    2 -> showCustomizeNotifications()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setNotificationsEnabled(enabled: Boolean) {
        prefs().edit().putBoolean("notifications_enabled", enabled).apply()
        updateSettingsDisplay()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            if (enabled) "🔔 Notifications enabled" else "🔕 Notifications disabled",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showCustomizeNotifications() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔔 Customize Notifications")
            .setMessage("Notification customization features:\n\n• Habit reminders\n• Mood check-ins\n• Goal progress\n• Water reminders\n• Meditation sessions")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showThemeSettings() {
        findNavController().navigate(R.id.SettingsFragment)
    }

    private fun showLanguageSettings() {
        findNavController().navigate(R.id.SettingsFragment)
    }

    private fun showWidgetSettings() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📱 Widget Settings")
            .setMessage("Widget customization options:\n\n• Show today's progress\n• Display habit streaks\n• Quick action buttons\n• Customize appearance")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSyncSettings() {
        val options = arrayOf("Enable Sync", "Disable Sync", "Sync Now", "View Sync Status")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("☁️ Sync Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> setSyncEnabled(true)
                    1 -> setSyncEnabled(false)
                    2 -> syncNow()
                    3 -> showSyncStatus()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setSyncEnabled(enabled: Boolean) {
        prefs().edit().putBoolean("sync_enabled", enabled).apply()
        updateSettingsDisplay()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            if (enabled) "☁️ Sync enabled" else "📱 Local only mode",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun syncNow() {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "🔄 Syncing data... (Simulated)",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showSyncStatus() {
        val syncEnabled = prefs().getBoolean("sync_enabled", false)
        val lastSync = prefs().getString("last_sync", "Never")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("☁️ Sync Status")
            .setMessage("Sync Status: ${if (syncEnabled) "Enabled" else "Disabled"}\nLast Sync: $lastSync\n\nNote: This is a demo app. Real sync would require cloud services.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDebugSettings() {
        val debugInfo = """
            🐛 Debug Information
            
            App Version: 1.0.0
            Build Type: Debug
            Data Storage: SharedPreferences
            Total Habits: ${getHabits().size}
            Total Moods: ${getMoods().size}
            Total Goals: ${getGoals().size}
            Total Challenges: ${getChallenges().size}
            
            Settings:
            • Analytics: ${prefs().getBoolean("analytics_enabled", true)}
            • Insights: ${prefs().getBoolean("insights_enabled", true)}
            • Privacy: ${prefs().getBoolean("privacy_mode", false)}
            • Notifications: ${prefs().getBoolean("notifications_enabled", true)}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🐛 Debug Information")
            .setMessage(debugInfo)
            .setPositiveButton("OK", null)
            .show()
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

    private fun getGoals(): List<Goal> {
        val json = prefs().getString("goals_json", "[]")
        val arr = JSONArray(json)
        val list = mutableListOf<Goal>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.optString("id", System.currentTimeMillis().toString())
            val title = o.optString("title")
            val description = o.optString("description")
            val targetDays = o.optInt("targetDays", 30)
            val currentProgress = o.optInt("currentProgress", 0)
            val isCompleted = o.optBoolean("isCompleted", false)
            val createdDate = o.optString("createdDate", java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()))
            list.add(Goal(id, title, description, targetDays, currentProgress, isCompleted, createdDate))
        }
        return list
    }

    private fun getChallenges(): List<Challenge> {
        val json = prefs().getString("challenges_json", "[]")
        val arr = JSONArray(json)
        val list = mutableListOf<Challenge>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.optString("id", System.currentTimeMillis().toString())
            val title = o.optString("title")
            val description = o.optString("description")
            val durationDays = o.optInt("durationDays", 7)
            val emoji = o.optString("emoji", "🎯")
            list.add(Challenge(id, title, description, durationDays, emoji))
        }
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
