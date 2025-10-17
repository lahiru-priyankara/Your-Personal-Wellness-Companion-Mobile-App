package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentAddMoodBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AddMoodFragment : Fragment() {

    private var _binding: FragmentAddMoodBinding? = null
    private val binding get() = _binding!!

    private val moodCategories = mapOf(
        "😄" to "Very Happy",
        "😊" to "Happy", 
        "😌" to "Content",
        "😐" to "Neutral",
        "😕" to "Worried",
        "😔" to "Sad",
        "😢" to "Crying",
        "😭" to "Devastated",
        "😤" to "Angry",
        "🤩" to "Excited",
        "😴" to "Tired",
        "🤔" to "Thoughtful",
        "😍" to "Loved",
        "😰" to "Anxious",
        "😎" to "Confident"
    )

    private var selectedMood: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupClickListeners()
        setupMoodGrid()
    }

    private fun setupUI() {
        binding.moodNoteEdit.hint = "How are you feeling? What's on your mind?"
        binding.moodNoteEdit.textSize = 16f
    }

    private fun setupClickListeners() {
        binding.saveMoodBtn.setOnClickListener {
            saveMood()
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.quickMoodBtn.setOnClickListener {
            showQuickMoodOptions()
        }
    }

    private fun setupMoodGrid() {
        val moodButtons = listOf(
            binding.mood1, binding.mood2, binding.mood3, binding.mood4,
            binding.mood5, binding.mood6, binding.mood7, binding.mood8,
            binding.mood9, binding.mood10, binding.mood11, binding.mood12,
            binding.mood13, binding.mood14, binding.mood15, binding.mood16
        )

        val moodEmojis = listOf(
            "😄", "😊", "😌", "😐",
            "😕", "😔", "😢", "😭",
            "😤", "🤩", "😴", "🤔",
            "😍", "😰", "😎", "😋"
        )

        moodButtons.forEachIndexed { index, button ->
            if (index < moodEmojis.size) {
                val emoji = moodEmojis[index]
                button.text = emoji
                button.setOnClickListener {
                    selectMood(emoji, button)
                }
            }
        }
    }

    private fun selectMood(emoji: String, button: View) {
        selectedMood = emoji
        binding.selectedMoodEmoji.text = emoji
        binding.selectedMoodText.text = moodCategories[emoji] ?: "Mood"
        binding.selectedMoodContainer.visibility = View.VISIBLE
        
        // Update button states
        val moodButtons = listOf(
            binding.mood1, binding.mood2, binding.mood3, binding.mood4,
            binding.mood5, binding.mood6, binding.mood7, binding.mood8,
            binding.mood9, binding.mood10, binding.mood11, binding.mood12,
            binding.mood13, binding.mood14, binding.mood15, binding.mood16
        )
        
        moodButtons.forEach { btn ->
            btn.background = resources.getDrawable(android.R.drawable.btn_default, null)
        }
        button.background = resources.getDrawable(android.R.drawable.btn_default_small, null)
    }

    private fun showQuickMoodOptions() {
        val quickMoods = arrayOf(
            "😄 Great! - Feeling amazing today!",
            "😊 Good - Having a nice day",
            "😐 Okay - Just a regular day",
            "😕 Not great - Having some challenges",
            "😢 Sad - Feeling down today"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚡ Quick Mood")
            .setMessage("Select your current mood:")
            .setItems(quickMoods) { _, which ->
                val quickMoodEmojis = listOf("😄", "😊", "😐", "😕", "😢")
                val quickMoodNotes = listOf(
                    "Feeling amazing today!",
                    "Having a nice day",
                    "Just a regular day",
                    "Having some challenges", 
                    "Feeling down today"
                )
                
                val emoji = quickMoodEmojis[which]
                val note = quickMoodNotes[which]
                
                selectedMood = emoji
                binding.selectedMoodEmoji.text = emoji
                binding.selectedMoodText.text = moodCategories[emoji] ?: "Mood"
                binding.selectedMoodContainer.visibility = View.VISIBLE
                binding.moodNoteEdit.setText(note)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveMood() {
        if (selectedMood == null) {
            Toast.makeText(requireContext(), "Please select a mood first", Toast.LENGTH_SHORT).show()
            return
        }

        val note = binding.moodNoteEdit.text.toString().trim()
        val emoji = selectedMood!!
        
        // Save mood to SharedPreferences
        val prefs = requireContext().getSharedPreferences("moods", 0)
        val moodsJson = prefs.getString("moods_json", "[]")
        val moodsArray = JSONArray(moodsJson)

        val newMood = JSONObject().apply {
            put("emoji", emoji)
            put("note", note)
            put("timestamp", System.currentTimeMillis())
            put("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }

        moodsArray.put(newMood)
        prefs.edit().putString("moods_json", moodsArray.toString()).apply()

        // Show success message
        showSuccessDialog(emoji, note)
    }

    private fun showSuccessDialog(emoji: String, note: String) {
        val moodDescriptions = mapOf(
            "😄" to "Very Happy", "😊" to "Happy", "😌" to "Content",
            "😐" to "Neutral", "😕" to "Worried", "😔" to "Sad",
            "😢" to "Crying", "😭" to "Devastated", "😤" to "Angry",
            "🤩" to "Excited", "😌" to "Peaceful", "😴" to "Tired",
            "🤔" to "Thoughtful", "😤" to "Frustrated", "😍" to "Loved",
            "😰" to "Anxious", "😎" to "Confident"
        )
        
        val description = moodDescriptions[emoji] ?: "Mood"
        
        val motivationalMessages = mapOf(
            "😄" to "🌟 Amazing! Keep that positive energy flowing!",
            "😊" to "✨ Great mood! You're doing well today!",
            "😌" to "💚 Peaceful and content - that's wonderful!",
            "😐" to "💪 That's okay! Every day is a new opportunity!",
            "😕" to "🤗 It's okay to have tough days. You've got this!",
            "😔" to "💙 Take care of yourself. Tomorrow will be better!",
            "😢" to "💙 It's okay to feel sad. You're not alone!",
            "😭" to "💙 Take time to heal. You're stronger than you know!",
            "😤" to "💪 Channel that energy into something positive!",
            "🤩" to "🎉 Your excitement is contagious! Keep it up!",
            "😴" to "😴 Rest is important. Take care of yourself!",
            "🤔" to "🧠 Deep thinking leads to great insights!",
            "😍" to "💕 Love makes everything better!",
            "😰" to "🤗 Take deep breaths. You can handle this!",
            "😎" to "🔥 Confidence looks great on you!"
        )
        
        val message = motivationalMessages[emoji] ?: "Thank you for logging your mood!"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💝 Mood Logged Successfully!")
            .setMessage("$emoji $description\n\n$message")
            .setPositiveButton("Great!") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .setNeutralButton("View Analytics") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigate(R.id.AnalyticsFragment)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
