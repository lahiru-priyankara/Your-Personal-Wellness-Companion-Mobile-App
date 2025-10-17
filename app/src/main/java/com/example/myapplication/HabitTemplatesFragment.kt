package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentHabitTemplatesBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class HabitTemplatesFragment : Fragment() {

    private var _binding: FragmentHabitTemplatesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var templatesAdapter: HabitTemplatesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitTemplatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadHabitTemplates()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        templatesAdapter = HabitTemplatesAdapter { template -> 
            showTemplateDetails(template)
        }
        binding.templatesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.templatesRecyclerView.adapter = templatesAdapter
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.categoryFilter.setOnClickListener {
            showCategoryFilter()
        }
        
        binding.searchTemplatesButton.setOnClickListener {
            showSearchDialog()
        }
    }

    private fun loadHabitTemplates() {
        val templates = getHabitTemplates()
        templatesAdapter.submitList(templates)
    }

    private fun getHabitTemplates(): List<HabitTemplate> {
        return listOf(
            // Health & Fitness
            HabitTemplate(
                id = "drink_water",
                name = "💧 Drink Water",
                category = "Health & Fitness",
                description = "Stay hydrated throughout the day",
                difficulty = "Easy",
                duration = "Daily",
                benefits = listOf("Better hydration", "Improved energy", "Clearer skin"),
                tips = listOf("Start with a glass in the morning", "Set hourly reminders", "Carry a water bottle"),
                icon = "💧",
                color = "#2196F3"
            ),
            HabitTemplate(
                id = "exercise",
                name = "🏃‍♀️ Daily Exercise",
                category = "Health & Fitness",
                description = "Get your body moving every day",
                difficulty = "Medium",
                duration = "Daily",
                benefits = listOf("Better fitness", "Improved mood", "Stronger body"),
                tips = listOf("Start with 10 minutes", "Choose activities you enjoy", "Track your progress"),
                icon = "🏃‍♀️",
                color = "#4CAF50"
            ),
            HabitTemplate(
                id = "meditation",
                name = "🧘‍♀️ Meditation",
                category = "Health & Fitness",
                description = "Practice mindfulness and inner peace",
                difficulty = "Easy",
                duration = "Daily",
                benefits = listOf("Reduced stress", "Better focus", "Inner peace"),
                tips = listOf("Start with 5 minutes", "Find a quiet space", "Focus on breathing"),
                icon = "🧘‍♀️",
                color = "#9C27B0"
            ),
            
            // Productivity
            HabitTemplate(
                id = "morning_routine",
                name = "🌅 Morning Routine",
                category = "Productivity",
                description = "Start your day with intention",
                difficulty = "Medium",
                duration = "Daily",
                benefits = listOf("Better focus", "Reduced stress", "More energy"),
                tips = listOf("Wake up at the same time", "Avoid phone first hour", "Include exercise"),
                icon = "🌅",
                color = "#FF9800"
            ),
            HabitTemplate(
                id = "reading",
                name = "📚 Daily Reading",
                category = "Productivity",
                description = "Expand your knowledge through reading",
                difficulty = "Easy",
                duration = "Daily",
                benefits = listOf("Better knowledge", "Improved focus", "Enhanced vocabulary"),
                tips = listOf("Read for 20 minutes", "Choose interesting books", "Take notes"),
                icon = "📚",
                color = "#607D8B"
            ),
            HabitTemplate(
                id = "journaling",
                name = "📝 Journaling",
                category = "Productivity",
                description = "Reflect on your day and thoughts",
                difficulty = "Easy",
                duration = "Daily",
                benefits = listOf("Better self-awareness", "Reduced stress", "Clearer thinking"),
                tips = listOf("Write for 10 minutes", "Be honest with yourself", "Review weekly"),
                icon = "📝",
                color = "#795548"
            ),
            
            // Wellness
            HabitTemplate(
                id = "gratitude",
                name = "🙏 Gratitude Practice",
                category = "Wellness",
                description = "Cultivate appreciation and positivity",
                difficulty = "Easy",
                duration = "Daily",
                benefits = listOf("Better mood", "Reduced anxiety", "Improved relationships"),
                tips = listOf("Write 3 things daily", "Be specific", "Share with others"),
                icon = "🙏",
                color = "#FFC107"
            ),
            HabitTemplate(
                id = "sleep_hygiene",
                name = "😴 Sleep Hygiene",
                category = "Wellness",
                description = "Improve your sleep quality",
                difficulty = "Medium",
                duration = "Daily",
                benefits = listOf("Better sleep", "More energy", "Improved health"),
                tips = listOf("Sleep at same time", "No screens 1 hour before", "Cool bedroom"),
                icon = "😴",
                color = "#3F51B5"
            ),
            HabitTemplate(
                id = "digital_detox",
                name = "📱 Digital Detox",
                category = "Wellness",
                description = "Reduce screen time and reconnect",
                difficulty = "Hard",
                duration = "Daily",
                benefits = listOf("Better focus", "Reduced anxiety", "Real connections"),
                tips = listOf("Set phone-free hours", "Use apps to track usage", "Find offline activities"),
                icon = "📱",
                color = "#E91E63"
            ),
            
            // Learning
            HabitTemplate(
                id = "learn_language",
                name = "🗣️ Language Learning",
                category = "Learning",
                description = "Master a new language",
                difficulty = "Medium",
                duration = "Daily",
                benefits = listOf("New skills", "Better memory", "Cultural understanding"),
                tips = listOf("Practice 15 minutes daily", "Use apps like Duolingo", "Practice speaking"),
                icon = "🗣️",
                color = "#00BCD4"
            ),
            HabitTemplate(
                id = "coding_practice",
                name = "💻 Coding Practice",
                category = "Learning",
                description = "Improve your programming skills",
                difficulty = "Medium",
                duration = "Daily",
                benefits = listOf("Better skills", "Career growth", "Problem solving"),
                tips = listOf("Code for 30 minutes", "Work on projects", "Learn new concepts"),
                icon = "💻",
                color = "#673AB7"
            ),
            HabitTemplate(
                id = "skill_development",
                name = "🎯 Skill Development",
                category = "Learning",
                description = "Continuously improve your abilities",
                difficulty = "Medium",
                duration = "Daily",
                benefits = listOf("Career growth", "Personal satisfaction", "New opportunities"),
                tips = listOf("Choose one skill", "Practice daily", "Track progress"),
                icon = "🎯",
                color = "#FF5722"
            )
        )
    }

    private fun showTemplateDetails(template: HabitTemplate) {
        val message = """
            ${template.description}
            
            📊 Difficulty: ${template.difficulty}
            ⏰ Duration: ${template.duration}
            
            🌟 Benefits:
            ${template.benefits.joinToString("\n• ", "• ")}
            
            💡 Tips:
            ${template.tips.joinToString("\n• ", "• ")}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("${template.icon} ${template.name}")
            .setMessage(message)
            .setPositiveButton("Add This Habit") { _, _ ->
                addHabitFromTemplate(template)
            }
            .setNeutralButton("Save Template") { _, _ ->
                saveTemplateToFavorites(template)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun addHabitFromTemplate(template: HabitTemplate) {
        val habits = getHabits()
        val newHabit = Habit(template.name, mutableSetOf())
        
        val updatedHabits = habits + newHabit
        saveHabits(updatedHabits)
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "✅ Habit '${template.name}' added successfully!",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
        
        findNavController().navigateUp()
    }

    private fun saveTemplateToFavorites(template: HabitTemplate) {
        val prefs = prefs()
        val favorites = prefs.getStringSet("favorite_templates", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        favorites.add(template.id)
        prefs.edit().putStringSet("favorite_templates", favorites).apply()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "💾 Template saved to favorites!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showCategoryFilter() {
        val categories = listOf("All", "Health & Fitness", "Productivity", "Wellness", "Learning")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📂 Filter by Category")
            .setItems(categories.toTypedArray()) { _, which ->
                val selectedCategory = categories[which]
                filterTemplatesByCategory(selectedCategory)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun filterTemplatesByCategory(category: String) {
        val allTemplates = getHabitTemplates()
        val filteredTemplates = if (category == "All") {
            allTemplates
        } else {
            allTemplates.filter { it.category == category }
        }
        templatesAdapter.submitList(filteredTemplates)
    }

    private fun showSearchDialog() {
        val input = android.widget.EditText(requireContext())
        input.hint = "Search habit templates..."
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔍 Search Templates")
            .setView(input)
            .setPositiveButton("Search") { _, _ ->
                val query = input.text.toString().lowercase()
                searchTemplates(query)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun searchTemplates(query: String) {
        val allTemplates = getHabitTemplates()
        val filteredTemplates = allTemplates.filter { template ->
            template.name.lowercase().contains(query) ||
            template.description.lowercase().contains(query) ||
            template.category.lowercase().contains(query) ||
            template.benefits.any { it.lowercase().contains(query) }
        }
        templatesAdapter.submitList(filteredTemplates)
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

    private fun saveHabits(habits: List<Habit>) {
        val arr = JSONArray()
        for (habit in habits) {
            val o = JSONObject()
            o.put("name", habit.name)
            val datesArr = JSONArray()
            for (date in habit.completedDates) datesArr.put(date)
            o.put("completedDates", datesArr)
            arr.put(o)
        }
        prefs().edit().putString("habits_json", arr.toString()).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class HabitTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val difficulty: String,
    val duration: String,
    val benefits: List<String>,
    val tips: List<String>,
    val icon: String,
    val color: String
)
