package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentAddHabitBinding
import org.json.JSONArray
import org.json.JSONObject

class AddHabitFragment : Fragment() {

    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!

    private val habitCategories = listOf(
        "🏃‍♀️ Fitness & Health",
        "🧠 Learning & Development", 
        "💼 Productivity & Work",
        "😊 Wellness & Mindfulness",
        "🏠 Home & Lifestyle",
        "👥 Social & Relationships",
        "💰 Finance & Goals",
        "🎨 Creative & Hobbies"
    )

    private val habitFrequencies = listOf(
        "Daily",
        "Weekly", 
        "Monthly",
        "Custom"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupClickListeners()
        loadSuggestedHabits()
    }

    private fun setupUI() {
        // Setup category spinner
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, habitCategories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter

        // Setup frequency spinner
        val frequencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, habitFrequencies)
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.frequencySpinner.adapter = frequencyAdapter

        // Set default values
        binding.habitNameEdit.hint = "Enter your habit name..."
        binding.habitDescriptionEdit.hint = "Add a description (optional)"
        binding.reminderTimeEdit.hint = "Set reminder time (optional)"
    }

    private fun setupClickListeners() {
        binding.saveHabitBtn.setOnClickListener {
            saveHabit()
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.suggestedHabitsBtn.setOnClickListener {
            showSuggestedHabits()
        }

        binding.templateHabitsBtn.setOnClickListener {
            findNavController().navigate(R.id.HabitTemplatesFragment)
        }
    }

    private fun loadSuggestedHabits() {
        val suggestedHabits = listOf(
            "💧 Drink 8 glasses of water",
            "🧘‍♀️ Meditate for 10 minutes",
            "📚 Read for 30 minutes",
            "🏃‍♀️ Exercise for 20 minutes",
            "😴 Sleep 8 hours",
            "🙏 Practice gratitude",
            "🍎 Eat 5 servings of fruits/vegetables",
            "📱 Limit social media to 1 hour",
            "🧹 Clean for 15 minutes",
            "💡 Learn something new"
        )

        binding.suggestedHabitsList.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            suggestedHabits
        )

        binding.suggestedHabitsList.setOnItemClickListener { _, _, position, _ ->
            val selectedHabit = suggestedHabits[position]
            binding.habitNameEdit.setText(selectedHabit)
            binding.habitNameEdit.setSelection(selectedHabit.length)
        }
    }

    private fun showSuggestedHabits() {
        val suggestions = arrayOf(
            "💧 Drink 8 glasses of water daily",
            "🧘‍♀️ Meditate for 10 minutes",
            "📚 Read for 30 minutes",
            "🏃‍♀️ Exercise for 20 minutes",
            "😴 Sleep 8 hours nightly",
            "🙏 Practice gratitude daily",
            "🍎 Eat 5 servings of fruits/vegetables",
            "📱 Limit social media to 1 hour",
            "🧹 Clean for 15 minutes",
            "💡 Learn something new daily"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💡 Suggested Habits")
            .setMessage("Choose a habit to get started:")
            .setItems(suggestions) { _, which ->
                val selectedHabit = suggestions[which]
                binding.habitNameEdit.setText(selectedHabit)
                binding.habitNameEdit.setSelection(selectedHabit.length)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveHabit() {
        val habitName = binding.habitNameEdit.text.toString().trim()
        val habitDescription = binding.habitDescriptionEdit.text.toString().trim()
        val category = binding.categorySpinner.selectedItem.toString()
        val frequency = binding.frequencySpinner.selectedItem.toString()
        val reminderTime = binding.reminderTimeEdit.text.toString().trim()

        if (habitName.isEmpty()) {
            binding.habitNameEdit.error = "Please enter a habit name"
            binding.habitNameEdit.requestFocus()
            return
        }

        // Save habit to SharedPreferences
        val prefs = requireContext().getSharedPreferences("habits", 0)
        val habitsJson = prefs.getString("habits_json", "[]")
        val habitsArray = JSONArray(habitsJson)

        val newHabit = JSONObject().apply {
            put("id", System.currentTimeMillis().toString())
            put("name", habitName)
            put("description", habitDescription)
            put("category", category)
            put("frequency", frequency)
            put("reminderTime", reminderTime)
            put("completedDates", "")
            put("createdDate", java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()))
        }

        habitsArray.put(newHabit)
        prefs.edit().putString("habits_json", habitsArray.toString()).apply()

        // Show success message
        showSuccessDialog(habitName)
    }

    private fun showSuccessDialog(habitName: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎉 Habit Added Successfully!")
            .setMessage("Your habit '$habitName' has been added to your daily routine. Keep up the great work!")
            .setPositiveButton("View Habits") { _, _ ->
                findNavController().navigate(R.id.FirstFragment)
            }
            .setNeutralButton("Add Another") { _, _ ->
                // Clear form for another habit
                binding.habitNameEdit.setText("")
                binding.habitDescriptionEdit.setText("")
                binding.reminderTimeEdit.setText("")
                binding.categorySpinner.setSelection(0)
                binding.frequencySpinner.setSelection(0)
            }
            .setNegativeButton("Done") { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
