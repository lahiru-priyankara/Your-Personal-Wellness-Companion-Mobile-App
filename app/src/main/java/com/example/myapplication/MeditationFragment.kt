package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentMeditationBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MeditationFragment : Fragment() {

    private var _binding: FragmentMeditationBinding? = null
    private val binding get() = _binding!!
    
    private var isMeditating = false
    private var currentSession: MeditationSession? = null
    private var countDownTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeditationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        loadMeditationHistory()
        updateMeditationStats()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.startMeditationButton.setOnClickListener {
            showMeditationOptions()
        }
        
        binding.stopMeditationButton.setOnClickListener {
            stopMeditation()
        }
        
        binding.meditationHistoryButton.setOnClickListener {
            showMeditationHistory()
        }
        
        binding.shareMeditationButton.setOnClickListener {
            shareMeditationProgress()
        }
        
        // Quick start buttons
        binding.quickStart2min.setOnClickListener {
            startMeditationSession(2)
        }
        
        binding.quickStart5min.setOnClickListener {
            startMeditationSession(5)
        }
        
        binding.quickStart10min.setOnClickListener {
            startMeditationSession(10)
        }
        
        binding.quickStart15min.setOnClickListener {
            startMeditationSession(15)
        }
    }

    private fun showMeditationOptions() {
        val options = arrayOf(
            "🧘‍♀️ Guided Meditation (5 min)",
            "🌊 Breathing Exercise (3 min)",
            "🧠 Mindfulness (10 min)",
            "💤 Sleep Meditation (15 min)",
            "⚡ Quick Focus (2 min)",
            "🎯 Custom Duration"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🧘‍♀️ Choose Meditation Type")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startGuidedMeditation(5)
                    1 -> startBreathingExercise(3)
                    2 -> startMindfulness(10)
                    3 -> startSleepMeditation(15)
                    4 -> startQuickFocus(2)
                    5 -> showCustomDurationDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startMeditationSession(duration: Int) {
        if (isMeditating) {
            stopMeditation()
        }
        
        currentSession = MeditationSession(
            id = System.currentTimeMillis().toString(),
            type = "Custom",
            duration = duration,
            startTime = System.currentTimeMillis(),
            completed = false
        )
        
        startTimer(duration)
        updateUIForMeditation()
    }

    private fun startGuidedMeditation(duration: Int) {
        currentSession = MeditationSession(
            id = System.currentTimeMillis().toString(),
            type = "Guided Meditation",
            duration = duration,
            startTime = System.currentTimeMillis(),
            completed = false
        )
        
        startTimer(duration)
        updateUIForMeditation()
        showGuidedInstructions()
    }

    private fun startBreathingExercise(duration: Int) {
        currentSession = MeditationSession(
            id = System.currentTimeMillis().toString(),
            type = "Breathing Exercise",
            duration = duration,
            startTime = System.currentTimeMillis(),
            completed = false
        )
        
        startTimer(duration)
        updateUIForMeditation()
        showBreathingInstructions()
    }

    private fun startMindfulness(duration: Int) {
        currentSession = MeditationSession(
            id = System.currentTimeMillis().toString(),
            type = "Mindfulness",
            duration = duration,
            startTime = System.currentTimeMillis(),
            completed = false
        )
        
        startTimer(duration)
        updateUIForMeditation()
        showMindfulnessInstructions()
    }

    private fun startSleepMeditation(duration: Int) {
        currentSession = MeditationSession(
            id = System.currentTimeMillis().toString(),
            type = "Sleep Meditation",
            duration = duration,
            startTime = System.currentTimeMillis(),
            completed = false
        )
        
        startTimer(duration)
        updateUIForMeditation()
        showSleepInstructions()
    }

    private fun startQuickFocus(duration: Int) {
        currentSession = MeditationSession(
            id = System.currentTimeMillis().toString(),
            type = "Quick Focus",
            duration = duration,
            startTime = System.currentTimeMillis(),
            completed = false
        )
        
        startTimer(duration)
        updateUIForMeditation()
        showFocusInstructions()
    }

    private fun showCustomDurationDialog() {
        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Enter duration in minutes"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🧘‍♀️ Custom Meditation Duration")
            .setView(input)
            .setPositiveButton("Start") { _, _ ->
                val duration = input.text.toString().toIntOrNull() ?: 5
                if (duration in 1..60) {
                    startMeditationSession(duration)
                } else {
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        "Please enter a duration between 1-60 minutes",
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startTimer(durationMinutes: Int) {
        val durationMillis = durationMinutes * 60 * 1000L
        
        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.timerText.text = String.format("%02d:%02d", minutes, seconds)
                
                val progress = ((durationMillis - millisUntilFinished) * 100) / durationMillis
                binding.meditationProgressBar.progress = progress.toInt()
            }
            
            override fun onFinish() {
                completeMeditation()
            }
        }.start()
        
        isMeditating = true
    }

    private fun stopMeditation() {
        countDownTimer?.cancel()
        isMeditating = false
        currentSession = null
        
        binding.timerText.text = "00:00"
        binding.meditationProgressBar.progress = 0
        binding.meditationStatusText.text = "🧘‍♀️ Ready to meditate"
        binding.meditationStatusText.setTextColor(Color.parseColor("#757575"))
        
        binding.startMeditationButton.visibility = View.VISIBLE
        binding.stopMeditationButton.visibility = View.GONE
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "⏹️ Meditation stopped",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun completeMeditation() {
        isMeditating = false
        currentSession?.let { session ->
            val completedSession = session.copy(completed = true)
            saveMeditationSession(completedSession)
        }
        
        binding.timerText.text = "00:00"
        binding.meditationProgressBar.progress = 100
        binding.meditationStatusText.text = "🎉 Meditation completed!"
        binding.meditationStatusText.setTextColor(Color.parseColor("#4CAF50"))
        
        binding.startMeditationButton.visibility = View.VISIBLE
        binding.stopMeditationButton.visibility = View.GONE
        
        showCompletionDialog()
        updateMeditationStats()
    }

    private fun updateUIForMeditation() {
        binding.meditationStatusText.text = "🧘‍♀️ Meditating..."
        binding.meditationStatusText.setTextColor(Color.parseColor("#2196F3"))
        
        binding.startMeditationButton.visibility = View.GONE
        binding.stopMeditationButton.visibility = View.VISIBLE
    }

    private fun showGuidedInstructions() {
        val instructions = """
            🧘‍♀️ Guided Meditation Instructions
            
            1. Find a comfortable seated position
            2. Close your eyes gently
            3. Focus on your breathing
            4. Follow the gentle guidance
            5. Let thoughts come and go naturally
            
            💡 Tip: Don't judge your thoughts, just observe them
        """.trimIndent()
        
        showInstructionsDialog("Guided Meditation", instructions)
    }

    private fun showBreathingInstructions() {
        val instructions = """
            🌊 Breathing Exercise Instructions
            
            1. Sit comfortably with spine straight
            2. Breathe in slowly for 4 counts
            3. Hold your breath for 4 counts
            4. Breathe out slowly for 4 counts
            5. Repeat this cycle
            
            💡 Focus on the rhythm of your breath
        """.trimIndent()
        
        showInstructionsDialog("Breathing Exercise", instructions)
    }

    private fun showMindfulnessInstructions() {
        val instructions = """
            🧠 Mindfulness Practice
            
            1. Notice your current thoughts and feelings
            2. Observe without judgment
            3. Focus on the present moment
            4. Notice sounds, sensations, and thoughts
            5. Return to awareness when mind wanders
            
            💡 Mindfulness is about being present, not perfect
        """.trimIndent()
        
        showInstructionsDialog("Mindfulness", instructions)
    }

    private fun showSleepInstructions() {
        val instructions = """
            💤 Sleep Meditation
            
            1. Lie down comfortably
            2. Focus on relaxing each body part
            3. Breathe deeply and slowly
            4. Let go of the day's thoughts
            5. Drift into peaceful sleep
            
            💡 This meditation helps prepare for restful sleep
        """.trimIndent()
        
        showInstructionsDialog("Sleep Meditation", instructions)
    }

    private fun showFocusInstructions() {
        val instructions = """
            ⚡ Quick Focus Session
            
            1. Take 3 deep breaths
            2. Focus on a single point or object
            3. When mind wanders, gently return focus
            4. Stay present and aware
            5. End with gratitude
            
            💡 Perfect for a quick mental reset
        """.trimIndent()
        
        showInstructionsDialog("Quick Focus", instructions)
    }

    private fun showInstructionsDialog(title: String, instructions: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🧘‍♀️ $title")
            .setMessage(instructions)
            .setPositiveButton("Start Meditation") { _, _ ->
                // Meditation already started
            }
            .setNegativeButton("Cancel") { _, _ ->
                stopMeditation()
            }
            .show()
    }

    private fun showCompletionDialog() {
        val session = currentSession
        if (session != null) {
            val message = """
                🎉 Great job completing your meditation!
                
                Duration: ${session.duration} minutes
                Type: ${session.type}
                
                ${getCompletionMessage(session.duration)}
            """.trimIndent()
            
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("🧘‍♀️ Meditation Complete")
                .setMessage(message)
                .setPositiveButton("Continue") { _, _ ->
                    // Continue with app
                }
                .setNeutralButton("Share") { _, _ ->
                    shareMeditationProgress()
                }
                .show()
        }
    }

    private fun getCompletionMessage(duration: Int): String {
        return when {
            duration >= 15 -> "🌟 Excellent! You've completed a long meditation session. Your mind and body will thank you!"
            duration >= 10 -> "💪 Great work! You've built strong mindfulness skills."
            duration >= 5 -> "👍 Good job! Regular meditation practice brings many benefits."
            else -> "🌱 Great start! Every moment of mindfulness counts."
        }
    }

    private fun saveMeditationSession(session: MeditationSession) {
        val prefs = prefs()
        val sessionsJson = prefs.getString("meditation_sessions", "[]")
        val sessionsArray = JSONArray(sessionsJson)
        
        val sessionJson = JSONObject().apply {
            put("id", session.id)
            put("type", session.type)
            put("duration", session.duration)
            put("startTime", session.startTime)
            put("completed", session.completed)
        }
        
        sessionsArray.put(sessionJson)
        prefs.edit().putString("meditation_sessions", sessionsArray.toString()).apply()
    }

    private fun loadMeditationHistory() {
        val sessions = getMeditationSessions()
        val totalSessions = sessions.size
        val totalMinutes = sessions.sumOf { it.duration }
        val thisWeekSessions = sessions.count { 
            val sessionDate = Date(it.startTime)
            val daysDiff = (Date().time - sessionDate.time) / (1000 * 60 * 60 * 24)
            daysDiff <= 7
        }
        
        binding.totalSessionsText.text = "$totalSessions sessions"
        binding.totalMinutesText.text = "$totalMinutes minutes"
        binding.thisWeekText.text = "$thisWeekSessions this week"
    }

    private fun updateMeditationStats() {
        loadMeditationHistory()
    }

    private fun getMeditationSessions(): List<MeditationSession> {
        val prefs = prefs()
        val sessionsJson = prefs.getString("meditation_sessions", "[]")
        val sessionsArray = JSONArray(sessionsJson)
        val sessions = mutableListOf<MeditationSession>()
        
        for (i in 0 until sessionsArray.length()) {
            val sessionJson = sessionsArray.getJSONObject(i)
            val session = MeditationSession(
                id = sessionJson.optString("id"),
                type = sessionJson.optString("type"),
                duration = sessionJson.optInt("duration"),
                startTime = sessionJson.optLong("startTime"),
                completed = sessionJson.optBoolean("completed")
            )
            sessions.add(session)
        }
        
        return sessions
    }

    private fun showMeditationHistory() {
        val sessions = getMeditationSessions().sortedByDescending { it.startTime }
        val historyText = if (sessions.isNotEmpty()) {
            "📊 Your Meditation History:\n\n" + sessions.take(10).joinToString("\n") { session ->
                val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(session.startTime))
                "${session.type}: ${session.duration} min - $date"
            }
        } else {
            "📊 No meditation history yet. Start your first session to begin tracking!"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🧘‍♀️ Meditation History")
            .setMessage(historyText)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun shareMeditationProgress() {
        val sessions = getMeditationSessions()
        val totalSessions = sessions.size
        val totalMinutes = sessions.sumOf { it.duration }
        val thisWeekSessions = sessions.count { 
            val sessionDate = Date(it.startTime)
            val daysDiff = (Date().time - sessionDate.time) / (1000 * 60 * 60 * 24)
            daysDiff <= 7
        }
        
        val shareText = """
            🧘‍♀️ My Meditation Journey
            
            📊 Total Sessions: $totalSessions
            ⏱️ Total Time: $totalMinutes minutes
            📅 This Week: $thisWeekSessions sessions
            
            💪 Building mindfulness and inner peace, one session at a time!
            
            #Meditation #Mindfulness #WellnessJourney #SLIITApp
        """.trimIndent()
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "My Meditation Progress")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Meditation Progress"))
    }

    private fun prefs() = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        mediaPlayer?.release()
        _binding = null
    }
}

data class MeditationSession(
    val id: String,
    val type: String,
    val duration: Int,
    val startTime: Long,
    val completed: Boolean
)
