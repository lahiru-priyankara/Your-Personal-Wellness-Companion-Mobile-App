package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentMoodBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodFragment : Fragment() {

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!

    private val emojis = listOf("😄", "😊", "😌", "😐", "😕", "😔", "😢", "😭", "😤")
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter
    private var selectedEmojiForAdding: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, loadMoodStrings())
        binding.moodList.adapter = listAdapter

        // Setup professional mood history
        moodHistoryAdapter = MoodHistoryAdapter()
        binding.moodRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.moodRecyclerView.adapter = moodHistoryAdapter

        setupEmojiSelection()
        setupAddMoodButton()
        setupQuickMoodButton()
        setupClearMoodsButton()
        setupProfessionalAddButton()
        setupShareMoodButton()
        updateMoodHistory()
    }

    private fun setupEmojiSelection() {
        val emojiViews = listOf(
            binding.emoji1, binding.emoji2, binding.emoji3,
            binding.emoji4, binding.emoji5, binding.emoji6,
            binding.emoji7, binding.emoji8, binding.emoji9
        )
        
        val moodDescriptions = listOf(
            "Very Happy", "Happy", "Content",
            "Neutral", "Worried", "Sad",
            "Crying", "Devastated", "Angry"
        )
        
        emojiViews.forEachIndexed { index, emojiView ->
            emojiView.setOnClickListener {
                val selectedEmoji = emojis[index]
                val selectedDescription = moodDescriptions[index]
                
                // Update selected mood display
                binding.selectedMoodContainer.visibility = View.VISIBLE
                binding.selectedMoodEmoji.text = selectedEmoji
                binding.selectedMoodText.text = selectedDescription
                
                // Store selected emoji for adding mood
                selectedEmojiForAdding = selectedEmoji
            }
        }
    }

    private fun setupAddMoodButton() {
        binding.addMoodBtn.setOnClickListener {
            if (selectedEmojiForAdding != null) {
                val note = binding.noteEdit.text.toString().trim()
                val emojiToShow = selectedEmojiForAdding!!
                addMood(emojiToShow, note)
                binding.noteEdit.setText("")
                binding.selectedMoodContainer.visibility = View.GONE
                selectedEmojiForAdding = null
                refreshList()
                showMoodBasedTip(emojiToShow)
                
                // Show professional confirmation
                val moodDescriptions = mapOf(
                    "😄" to "Very Happy", "😊" to "Happy", "😌" to "Content",
                    "😐" to "Neutral", "😕" to "Worried", "😔" to "Sad",
                    "😢" to "Crying", "😭" to "Devastated", "😤" to "Angry"
                )
                val description = moodDescriptions[emojiToShow] ?: "Mood"
                showMoodConfirmation(emojiToShow, description)
            } else {
                android.widget.Toast.makeText(requireContext(), "Please select a mood first", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupQuickMoodButton() {
        binding.quickMoodBtn.setOnClickListener {
            showQuickMoodDialog()
        }
    }

    private fun setupShareMoodButton() {
        binding.shareMoodBtn.setOnClickListener {
            shareMoodSummary()
        }
    }

    private fun shareMoodSummary() {
        val moods = loadMoodEntries()
        if (moods.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "No moods to share yet", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val lines = moods.take(10).map { entry ->
            val time = dateFmt.format(Date(entry.timestamp))
            "$time  ${entry.emoji}  - ${entry.description}${if (entry.note.isNotEmpty()) " · ${entry.note}" else ""}"
        }
        val body = StringBuilder().apply {
            append("My recent moods:\n")
            lines.forEach { append("• ").append(it).append('\n') }
            append("\nShared from My Wellness App")
        }.toString()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Summary")
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun showQuickMoodDialog() {
        val quickMoods = arrayOf(
            "😄 Great! - Feeling amazing today!",
            "😊 Good - Having a nice day",
            "😐 Okay - Just a regular day",
            "😕 Not great - Having some challenges",
            "😢 Sad - Feeling down today"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚡ Quick Mood Log")
            .setMessage("How are you feeling right now? Select your current mood:")
            .setItems(quickMoods) { _, which ->
                val quickMoodEmojis = listOf("😄", "😊", "😐", "😕", "😢")
                val quickMoodDescriptions = listOf("Great!", "Good", "Okay", "Not great", "Sad")
                val quickMoodNotes = listOf(
                    "Feeling amazing today!",
                    "Having a nice day",
                    "Just a regular day", 
                    "Having some challenges",
                    "Feeling down today"
                )
                
                val selectedEmoji = quickMoodEmojis[which]
                val selectedDescription = quickMoodDescriptions[which]
                val selectedNote = quickMoodNotes[which]
                addMood(selectedEmoji, selectedNote)
                refreshList()
                showMoodConfirmation(selectedEmoji, selectedDescription)
            }
            .setNeutralButton("Custom Mood") { _, _ ->
                showCustomMoodDialog()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomMoodDialog() {
        val customMoods = arrayOf(
            "🤩 Excited", "😌 Peaceful", "😴 Tired", "🤔 Thoughtful",
            "😤 Frustrated", "😍 Loved", "😰 Anxious", "😎 Confident"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎨 Custom Mood")
            .setMessage("Choose a more specific mood:")
            .setItems(customMoods) { _, which ->
                val customEmojis = listOf("🤩", "😌", "😴", "🤔", "😤", "😍", "😰", "😎")
                val customDescriptions = listOf("Excited", "Peaceful", "Tired", "Thoughtful", "Frustrated", "Loved", "Anxious", "Confident")
                
                val selectedEmoji = customEmojis[which]
                val selectedDescription = customDescriptions[which]
                
                // Add custom mood
                addMood(selectedEmoji, "Custom mood: $selectedDescription")
                refreshList()
                showMoodBasedTip(selectedEmoji)
                showMoodConfirmation(selectedEmoji, selectedDescription)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMoodConfirmation(emoji: String, description: String) {
        val motivationalMessages = mapOf(
            "😄" to "🌟 Amazing! Keep that positive energy flowing!",
            "😊" to "✨ Great mood! You're doing well today!",
            "😐" to "💪 That's okay! Every day is a new opportunity!",
            "😕" to "🤗 It's okay to have tough days. You've got this!",
            "😢" to "💙 Take care of yourself. Tomorrow will be better!"
        )
        
        val message = motivationalMessages[emoji] ?: "Thank you for logging your mood!"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💝 Mood Logged Successfully!")
            .setMessage("$emoji $description\n\n$message")
            .setPositiveButton("Great!") { dialog, _ ->
                dialog.dismiss()
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

    private fun refreshList() {
        listAdapter.clear()
        listAdapter.addAll(loadMoodStrings())
        listAdapter.notifyDataSetChanged()
        updateMoodHistory()
    }

    private fun setupClearMoodsButton() {
        binding.clearMoodsBtn.setOnClickListener {
            showClearMoodsDialog()
        }
    }

    private fun setupProfessionalAddButton() {
        binding.professionalAddBtn.setOnClickListener {
            findNavController().navigate(R.id.AddMoodFragment)
        }
    }

    private fun showClearMoodsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Clear All Moods")
            .setMessage("Are you sure you want to clear all your mood history? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllMoods()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllMoods() {
        val prefs = requireContext().getSharedPreferences("moods", 0)
        prefs.edit().clear().apply()
        refreshList()
        updateMoodHistory()
        android.widget.Toast.makeText(requireContext(), "All moods cleared", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun updateMoodHistory() {
        val moods = loadMoodEntries()
        moodHistoryAdapter.updateData(moods)
        updateMoodSummary(moods)
    }

    private fun updateMoodSummary(moods: List<MoodHistoryAdapter.MoodEntry>) {
        val count = moods.size
        binding.moodCountText.text = "$count moods logged"
        
        if (moods.isNotEmpty()) {
            val lastMood = moods.first()
            binding.lastMoodEmoji.text = lastMood.emoji
            binding.lastMoodEmoji.visibility = View.VISIBLE
            
            val moodDescriptions = mapOf(
                "😄" to "Very Happy", "😊" to "Happy", "😌" to "Content",
                "😐" to "Neutral", "😕" to "Worried", "😔" to "Sad",
                "😢" to "Crying", "😭" to "Devastated", "😤" to "Angry",
                "🤩" to "Excited", "😌" to "Peaceful", "😴" to "Tired",
                "🤔" to "Thoughtful", "😤" to "Frustrated", "😍" to "Loved",
                "😰" to "Anxious", "😎" to "Confident"
            )
            
            val lastMoodDescription = moodDescriptions[lastMood.emoji] ?: "Mood"
            binding.moodSummaryText.text = "Last mood: $lastMoodDescription"
        } else {
            binding.lastMoodEmoji.visibility = View.GONE
            binding.moodSummaryText.text = "Your mood journey starts here!"
        }
    }

    private fun loadMoodEntries(): List<MoodHistoryAdapter.MoodEntry> {
        val prefs = requireContext().getSharedPreferences("moods", 0)
        val moodsJson = prefs.getString("moods_json", "[]")
        val moodsArray = org.json.JSONArray(moodsJson)
        
        val moodEntries = mutableListOf<MoodHistoryAdapter.MoodEntry>()
        
        for (i in 0 until moodsArray.length()) {
            val moodObj = moodsArray.getJSONObject(i)
            val emoji = moodObj.getString("emoji")
            val note = moodObj.getString("note")
            val timestamp = moodObj.getLong("timestamp")
            
            val moodDescriptions = mapOf(
                "😄" to "Very Happy", "😊" to "Happy", "😌" to "Content",
                "😐" to "Neutral", "😕" to "Worried", "😔" to "Sad",
                "😢" to "Crying", "😭" to "Devastated", "😤" to "Angry",
                "🤩" to "Excited", "😌" to "Peaceful", "😴" to "Tired",
                "🤔" to "Thoughtful", "😤" to "Frustrated", "😍" to "Loved",
                "😰" to "Anxious", "😎" to "Confident"
            )
            
            val description = moodDescriptions[emoji] ?: "Mood"
            
            moodEntries.add(
                MoodHistoryAdapter.MoodEntry(
                    emoji = emoji,
                    description = description,
                    note = note,
                    timestamp = timestamp
                )
            )
        }
        
        // Sort by timestamp (newest first)
        return moodEntries.sortedByDescending { it.timestamp }
    }

    private fun prefs() = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)

    private fun addMood(emoji: String, note: String) {
        val arr = JSONArray(prefs().getString("moods_json", "[]"))
        val obj = JSONObject()
        obj.put("emoji", emoji)
        obj.put("note", note)
        obj.put("timestamp", System.currentTimeMillis())
        arr.put(obj)
        prefs().edit().putString("moods_json", arr.toString()).apply()
    }

    private fun loadMoodStrings(): List<String> {
        val json = prefs().getString("moods_json", "[]")
        val arr = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in arr.length() - 1 downTo 0) {
            val o = arr.getJSONObject(i)
            val emoji = o.optString("emoji")
            val note = o.optString("note")
            val ts = o.optLong("timestamp")
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ts))
            list.add("$time  $emoji  ${if (note.isEmpty()) "" else "- $note"}")
        }
        return list
    }
    
    private fun showMoodBasedTip(emoji: String) {
        val tip = WellnessQuotes.getQuoteByMood(emoji)
        val habits = getHabits()
        val recommendations = HabitRecommendations.getRecommendationsForUser(habits, recentMood = emoji).take(2)
        
        val message = StringBuilder()
        message.append("$tip\n\n")
        
        if (recommendations.isNotEmpty()) {
            message.append("💡 Suggested activities for your current mood:\n")
            recommendations.forEach { rec ->
                message.append("• $rec\n")
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🌈 Mood Insights")
            .setMessage(message.toString())
            .setPositiveButton("Thank you!", null)
            .setNeutralButton("View Goals") { _, _ ->
                findNavController().navigate(R.id.GoalsFragment)
            }
            .show()
    }
    
    private fun getHabits(): List<Habit> {
        val json = prefs().getString("habits_json", null) ?: return emptyList()
        return try {
            val arr = org.json.JSONArray(json)
            val list = mutableListOf<Habit>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val name = o.optString("name")
                val datesArr = o.optJSONArray("completedDates") ?: org.json.JSONArray()
                val set = mutableSetOf<String>()
                for (j in 0 until datesArr.length()) set.add(datesArr.getString(j))
                list.add(Habit(name, set))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}


