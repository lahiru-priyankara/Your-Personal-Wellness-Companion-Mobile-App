package com.example.myapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val intervals = listOf(30, 60, 120, 180) // minutes
    private val themes = listOf("Light Theme", "Dark Theme", "Green Theme", "System Default")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSettingsPanel()
        setupClickListeners()
        
        // Debug: Verify all views exist
        android.util.Log.d("SettingsFragment", "Views setup complete")
    }

    private fun setupSettingsPanel() {
        binding.reminderSwitch.isChecked = prefs().getBoolean(KEY_ENABLED, false)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, intervals.map { "$it min" })
        binding.intervalSpinner.adapter = adapter
        val saved = prefs().getInt(KEY_INTERVAL_MIN, 60)
        val idx = intervals.indexOf(saved).takeIf { it >= 0 } ?: 1
        binding.intervalSpinner.setSelection(idx)

        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, themes)
        binding.themeSpinner.adapter = themeAdapter
        binding.themeSpinner.setSelection(prefs().getInt(KEY_THEME, 0))
        binding.vibrateSwitch.isChecked = prefs().getBoolean(KEY_VIBRATE, true)

        binding.saveBtn.setOnClickListener {
            val enabled = binding.reminderSwitch.isChecked
            val minutes = intervals[binding.intervalSpinner.selectedItemPosition]
            prefs().edit()
                .putBoolean(KEY_ENABLED, enabled)
                .putInt(KEY_INTERVAL_MIN, minutes)
                .putInt(KEY_THEME, binding.themeSpinner.selectedItemPosition)
                .putBoolean(KEY_VIBRATE, binding.vibrateSwitch.isChecked)
                .apply()
            if (enabled) {
                ensureNotificationPermission {
                    scheduleReminder(minutes)
                }
            } else {
                cancelReminder()
            }
            applyTheme()
        }
    }

    private fun setupClickListeners() {
        // Add logging to debug clicks
        binding.habitsSection.setOnClickListener {
            android.util.Log.d("SettingsFragment", "Habits clicked")
            showSnackbar("Opening Habits Settings...")
            showHabitsSettings()
        }

        binding.notificationsSection.setOnClickListener {
            android.util.Log.d("SettingsFragment", "Notifications clicked")
            showSnackbar("Opening Notification Settings...")
            showNotificationSettings()
        }

        binding.customizeSection.setOnClickListener {
            android.util.Log.d("SettingsFragment", "Customize clicked")
            showSnackbar("Opening Customize Options...")
            showCustomizeSettings()
        }

        binding.lockScreenSection.setOnClickListener {
            android.util.Log.d("SettingsFragment", "Lock screen clicked")
            showSnackbar("Opening Lock Screen Settings...")
            showLockScreenSettings()
        }

        binding.backupsSection.setOnClickListener {
            android.util.Log.d("SettingsFragment", "Backups clicked")
            showSnackbar("Opening Backup Options...")
            showBackupSettings()
        }

        binding.languageSection.setOnClickListener {
            android.util.Log.d("SettingsFragment", "Language clicked")
            showSnackbar("Opening Language Settings...")
            showLanguageSettings()
        }

        binding.licensesSection.setOnClickListener {
            android.util.Log.d("SettingsFragment", "Licenses clicked")
            showSnackbar("Opening App Info...")
            showLicensesInfo()
        }
        
        // Add access to advanced settings
        binding.root.findViewById<View>(R.id.advancedSettingsButton)?.setOnClickListener {
            findNavController().navigate(R.id.AdvancedSettingsFragment)
        }
        
        // Add visual feedback
        addClickFeedback()
    }

    private fun addClickFeedback() {
        val sections = listOf(
            binding.habitsSection,
            binding.notificationsSection,
            binding.customizeSection,
            binding.lockScreenSection,
            binding.backupsSection,
            binding.languageSection,
            binding.licensesSection
        )
        
        sections.forEach { section ->
            section.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        v.alpha = 0.7f
                        v.scaleX = 0.95f
                        v.scaleY = 0.95f
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        v.alpha = 1.0f
                        v.scaleX = 1.0f
                        v.scaleY = 1.0f
                    }
                }
                false // Don't consume the event
            }
        }
    }

    private fun showHabitsSettings() {
        val options = arrayOf("Clear all habits", "Export habits data", "Import habits", "Reset streaks")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎯 Habit Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> confirmClearHabits()
                    1 -> exportHabitsData()
                    2 -> showSnackbar("Import feature - coming soon!")
                    3 -> confirmResetStreaks()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLockScreenSettings() {
        val options = arrayOf("Show widget on lock screen", "Hide sensitive data", "Quick actions")
        val checkedItems = booleanArrayOf(
            prefs().getBoolean("lockscreen_widget", false),
            prefs().getBoolean("hide_sensitive", true),
            prefs().getBoolean("quick_actions", false)
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔒 Lock Screen Settings")
            .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
                when (which) {
                    0 -> prefs().edit().putBoolean("lockscreen_widget", isChecked).apply()
                    1 -> prefs().edit().putBoolean("hide_sensitive", isChecked).apply()
                    2 -> prefs().edit().putBoolean("quick_actions", isChecked).apply()
                }
            }
            .setPositiveButton("Save") { _, _ ->
                showSnackbar("Lock screen settings saved")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBackupSettings() {
        val options = arrayOf("Backup to Google Drive", "Export to file", "Auto backup", "Restore from backup")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💾 Backup & Restore")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSnackbar("Google Drive backup - feature coming soon!")
                    1 -> exportToFile()
                    2 -> toggleAutoBackup()
                    3 -> showSnackbar("Restore - select backup file")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLanguageSettings() {
        val languages = arrayOf("English", "සිංහල (Sinhala)", "தமிழ் (Tamil)", "System Default")
        val currentLang = prefs().getInt("app_language", 3)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🌐 Choose Language")
            .setSingleChoiceItems(languages, currentLang) { dialog, which ->
                prefs().edit().putInt("app_language", which).apply()
                applyLanguageChange(which)
                showSnackbar("Language changed to ${languages[which]}")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyLanguageChange(languageIndex: Int) {
        val locale = when (languageIndex) {
            0 -> java.util.Locale.ENGLISH
            1 -> java.util.Locale("si", "LK") // Sinhala
            2 -> java.util.Locale("ta", "LK") // Tamil
            else -> java.util.Locale.getDefault() // System
        }
        
        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        
        // Restart activity to apply language changes
        requireActivity().recreate()
    }

    private fun showLicensesInfo() {
        val licenses = """
            🏆 Wellness App v1.0
            
            📚 Open Source Licenses:
            
            • Material Design Components
            • AndroidX Libraries  
            • Kotlin Standard Library
            • Navigation Component
            
            💡 Features:
            • Daily Habit Tracking
            • Mood Journal with Emojis
            • Hydration Reminders
            • Timer & Stopwatch
            • Home Screen Widget
            
            👨‍💻 Developed for SLIIT MAD Lab Exam
            
            ❤️ Made with love for wellness tracking
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("About & Licenses")
            .setMessage(licenses)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmClearHabits() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Clear All Habits")
            .setMessage("Are you sure? This will delete all habits and their progress permanently.")
            .setPositiveButton("Clear") { _, _ ->
                prefs().edit().remove("habits_json").apply()
                showSnackbar("All habits cleared")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmResetStreaks() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Reset All Streaks")
            .setMessage("This will reset all habit streaks to 0. Habits will remain but completion history will be cleared.")
            .setPositiveButton("Reset") { _, _ ->
                // Reset completion dates but keep habit names
                val habitsJson = prefs().getString("habits_json", "[]")
                if (habitsJson != "[]") {
                    val arr = org.json.JSONArray(habitsJson)
                    val newArr = org.json.JSONArray()
                    for (i in 0 until arr.length()) {
                        val habit = arr.getJSONObject(i)
                        val newHabit = org.json.JSONObject()
                        newHabit.put("name", habit.getString("name"))
                        newHabit.put("completedDates", org.json.JSONArray())
                        newArr.put(newHabit)
                    }
                    prefs().edit().putString("habits_json", newArr.toString()).apply()
                }
                showSnackbar("All streaks reset")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportHabitsData() {
        val habits = prefs().getString("habits_json", "[]")
        val moods = prefs().getString("moods_json", "[]")
        val exportData = """
            📊 Wellness App Data Export
            Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}
            
            🎯 Habits Data:
            $habits
            
            😊 Moods Data:  
            $moods
        """.trimIndent()
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, exportData)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Wellness App Data Export")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Export Data"))
    }

    private fun exportToFile() {
        exportHabitsData()
        showSnackbar("Data exported - choose app to save")
    }

    private fun toggleAutoBackup() {
        val current = prefs().getBoolean("auto_backup", false)
        prefs().edit().putBoolean("auto_backup", !current).apply()
        showSnackbar(if (!current) "Auto backup enabled" else "Auto backup disabled")
    }

    private fun showNotificationSettings() {
        val notificationTypes = arrayOf("Hydration reminders", "Habit reminders", "Mood reminders", "Achievement notifications")
        val checkedItems = booleanArrayOf(
            prefs().getBoolean("hydration_notifications", true),
            prefs().getBoolean("habit_notifications", false),
            prefs().getBoolean("mood_notifications", false),
            prefs().getBoolean("achievement_notifications", true)
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔔 Notification Settings")
            .setMultiChoiceItems(notificationTypes, checkedItems) { _, which, isChecked ->
                when (which) {
                    0 -> {
                        prefs().edit().putBoolean("hydration_notifications", isChecked).apply()
                        if (isChecked) toggleSettingsPanel() // Show hydration settings
                    }
                    1 -> prefs().edit().putBoolean("habit_notifications", isChecked).apply()
                    2 -> prefs().edit().putBoolean("mood_notifications", isChecked).apply()
                    3 -> prefs().edit().putBoolean("achievement_notifications", isChecked).apply()
                }
            }
            .setPositiveButton("Configure") { _, _ ->
                toggleSettingsPanel() // Show detailed hydration settings
            }
            .setNegativeButton("Done", null)
            .show()
    }

    private fun showCustomizeSettings() {
        val customOptions = arrayOf("Change theme", "App colors", "Font size", "Card style", "Animation speed")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎨 Customize App")
            .setItems(customOptions) { _, which ->
                when (which) {
                    0 -> showThemeSelector()
                    1 -> showColorPicker()
                    2 -> showFontSizeSelector()
                    3 -> showCardStyleSelector()
                    4 -> showAnimationSpeedSelector()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showThemeSelector() {
        val currentTheme = prefs().getInt(KEY_THEME, 0)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🌈 Choose Theme")
            .setSingleChoiceItems(themes.toTypedArray(), currentTheme) { dialog, which ->
                prefs().edit().putInt(KEY_THEME, which).apply()
                applySelectedTheme(which)
                showSnackbar("Theme changed to ${themes[which]}")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showColorPicker() {
        val colors = arrayOf("Blue Ocean", "Green Nature", "Purple Galaxy", "Orange Sunset", "Pink Blossom")
        val currentColor = prefs().getInt("app_color", 0)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎨 App Colors")
            .setSingleChoiceItems(colors, currentColor) { dialog, which ->
                prefs().edit().putInt("app_color", which).apply()
                showSnackbar("Color scheme: ${colors[which]}")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFontSizeSelector() {
        val sizes = arrayOf("Small", "Medium", "Large", "Extra Large")
        val currentSize = prefs().getInt("font_size", 1)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📝 Font Size")
            .setSingleChoiceItems(sizes, currentSize) { dialog, which ->
                prefs().edit().putInt("font_size", which).apply()
                showSnackbar("Font size: ${sizes[which]}")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCardStyleSelector() {
        val styles = arrayOf("Rounded", "Sharp corners", "Extra rounded", "Material style")
        val currentStyle = prefs().getInt("card_style", 0)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🃏 Card Style")
            .setSingleChoiceItems(styles, currentStyle) { dialog, which ->
                prefs().edit().putInt("card_style", which).apply()
                showSnackbar("Card style: ${styles[which]}")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAnimationSpeedSelector() {
        val speeds = arrayOf("Slow", "Normal", "Fast", "No animations")
        val currentSpeed = prefs().getInt("animation_speed", 1)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚡ Animation Speed")
            .setSingleChoiceItems(speeds, currentSpeed) { dialog, which ->
                prefs().edit().putInt("animation_speed", which).apply()
                showSnackbar("Animation speed: ${speeds[which]}")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applySelectedTheme(themeIndex: Int) {
        val themeRes = when (themeIndex) {
            0 -> R.style.Theme_MyApplication // Light
            1 -> R.style.Theme_MyApplication_Dark // Dark  
            2 -> R.style.Theme_MyApplication_Alt // Green
            else -> R.style.Theme_MyApplication // Default
        }
        requireActivity().setTheme(themeRes)
        requireActivity().recreate()
    }

    private fun toggleSettingsPanel() {
        val panel = binding.settingsPanel
        if (panel.visibility == View.GONE) {
            panel.visibility = View.VISIBLE
        } else {
            panel.visibility = View.GONE
        }
    }

    private fun showSnackbar(message: String) {
        com.google.android.material.snackbar.Snackbar.make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
    }

    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // no-op; scheduling is called in the callback chain where needed
    }

    private fun ensureNotificationPermission(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onGranted(); return
        }
        val permission = android.Manifest.permission.POST_NOTIFICATIONS
        val has = requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        if (has) {
            onGranted()
        } else {
            requestPermission.launch(permission)
            // Best-effort: user can tap Save again after granting
        }
    }

    private fun scheduleReminder(minutes: Int) {
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = reminderPendingIntent()
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, minutes * 60_000L, pi)
    }

    private fun cancelReminder() {
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(reminderPendingIntent())
    }

    private fun reminderPendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        intent.action = "com.example.myapplication.ACTION_HYDRATION_REMINDER"
        return PendingIntent.getBroadcast(requireContext(), 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun prefs() = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_ENABLED = "reminder_enabled"
        const val KEY_INTERVAL_MIN = "reminder_interval_min"
        const val KEY_THEME = "app_theme"
        const val KEY_VIBRATE = "reminder_vibrate"
    }

    private fun applyTheme() {
        val idx = prefs().getInt(KEY_THEME, 0)
        requireActivity().setTheme(if (idx == 0) R.style.Theme_MyApplication else R.style.Theme_MyApplication_Alt)
        requireActivity().recreate()
    }
}


