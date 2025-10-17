package com.example.myapplication

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentTimerBinding
import java.util.concurrent.TimeUnit

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!

    private var countDownTimer: CountDownTimer? = null
    private var stopwatchTimer: CountDownTimer? = null
    private var isRunning = false
    private var timeInMillis = 0L
    private var totalTimeInMillis = 0L
    private var currentMode = TimerMode.STOPWATCH
    
    // Sound and vibration settings
    private var soundEnabled = true
    private var vibrationEnabled = true
    private var soundVolume = 50 // 0-100
    private var vibrationIntensity = 50 // 0-100
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    enum class TimerMode {
        STOPWATCH, COUNTDOWN, INTERVALS
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSettings()
        initializeAudioAndVibration()
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        updateTimerDisplay()
        binding.progressRing.max = 100
        binding.progressRing.progress = 0
        
        // Set initial mode
        selectMode(TimerMode.STOPWATCH)
    }

    private fun setupClickListeners() {
        binding.startPauseBtn.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.resetBtn.setOnClickListener {
            resetTimer()
        }

        binding.stopwatchTab.setOnClickListener {
            selectMode(TimerMode.STOPWATCH)
        }

        binding.countdownTab.setOnClickListener {
            selectMode(TimerMode.COUNTDOWN)
        }

        binding.intervalsTab.setOnClickListener {
            selectMode(TimerMode.INTERVALS)
        }

        binding.timerDisplay.setOnClickListener {
            if (currentMode == TimerMode.COUNTDOWN && !isRunning) {
                showTimePickerDialog()
            }
        }
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences("timer_settings", Context.MODE_PRIVATE)
        soundEnabled = prefs.getBoolean("sound_enabled", true)
        vibrationEnabled = prefs.getBoolean("vibration_enabled", true)
        soundVolume = prefs.getInt("sound_volume", 50)
        vibrationIntensity = prefs.getInt("vibration_intensity", 50)
    }

    private fun initializeAudioAndVibration() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, soundVolume)
            vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        } catch (e: Exception) {
            // Handle audio initialization errors
        }
    }

    private fun showTimerSettings() {
        val options = arrayOf(
            "🔊 Sound Settings",
            "📳 Vibration Settings", 
            "⚙️ General Settings"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⏰ Timer Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSoundSettings()
                    1 -> showVibrationSettings()
                    2 -> showGeneralSettings()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSoundSettings() {
        val soundOptions = arrayOf(
            "🔊 Enable Sound",
            "🔇 Disable Sound",
            "📊 Adjust Volume"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔊 Sound Settings")
            .setItems(soundOptions) { _, which ->
                when (which) {
                    0 -> {
                        soundEnabled = true
                        saveSettings()
                        showToast("Sound enabled")
                    }
                    1 -> {
                        soundEnabled = false
                        saveSettings()
                        showToast("Sound disabled")
                    }
                    2 -> showVolumeDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVibrationSettings() {
        val vibrationOptions = arrayOf(
            "📳 Enable Vibration",
            "🔇 Disable Vibration", 
            "📊 Adjust Intensity"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📳 Vibration Settings")
            .setItems(vibrationOptions) { _, which ->
                when (which) {
                    0 -> {
                        vibrationEnabled = true
                        saveSettings()
                        showToast("Vibration enabled")
                    }
                    1 -> {
                        vibrationEnabled = false
                        saveSettings()
                        showToast("Vibration disabled")
                    }
                    2 -> showVibrationIntensityDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGeneralSettings() {
        val generalOptions = arrayOf(
            "🔄 Reset to Defaults",
            "ℹ️ About Timer"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚙️ General Settings")
            .setItems(generalOptions) { _, which ->
                when (which) {
                    0 -> resetToDefaults()
                    1 -> showAboutDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVolumeDialog() {
        val volumeOptions = arrayOf("Quiet (25%)", "Medium (50%)", "Loud (75%)", "Maximum (100%)")
        val volumeValues = arrayOf(25, 50, 75, 100)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Adjust Volume")
            .setItems(volumeOptions) { _, which ->
                soundVolume = volumeValues[which]
                saveSettings()
                initializeAudioAndVibration()
                showToast("Volume set to ${volumeOptions[which]}")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVibrationIntensityDialog() {
        val intensityOptions = arrayOf("Light (25%)", "Medium (50%)", "Strong (75%)", "Maximum (100%)")
        val intensityValues = arrayOf(25, 50, 75, 100)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Adjust Vibration Intensity")
            .setItems(intensityOptions) { _, which ->
                vibrationIntensity = intensityValues[which]
                saveSettings()
                showToast("Vibration intensity set to ${intensityOptions[which]}")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetToDefaults() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔄 Reset to Defaults")
            .setMessage("This will reset all timer settings to default values. Continue?")
            .setPositiveButton("Reset") { _, _ ->
                soundEnabled = true
                vibrationEnabled = true
                soundVolume = 50
                vibrationIntensity = 50
                saveSettings()
                initializeAudioAndVibration()
                showToast("Settings reset to defaults")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("ℹ️ About Timer")
            .setMessage("""
                🎯 Professional Timer Features:
                
                ⏱️ Stopwatch Mode
                ⏰ Countdown Timer
                🔄 Interval Training
                
                🔊 Sound Alerts
                📳 Vibration Feedback
                ⚙️ Customizable Settings
                
                Perfect for workouts, meditation, and productivity!
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun saveSettings() {
        val prefs = requireContext().getSharedPreferences("timer_settings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("sound_enabled", soundEnabled)
            putBoolean("vibration_enabled", vibrationEnabled)
            putInt("sound_volume", soundVolume)
            putInt("vibration_intensity", vibrationIntensity)
            apply()
        }
    }

    private fun playSound() {
        if (soundEnabled && toneGenerator != null) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
            } catch (e: Exception) {
                // Handle sound errors
            }
        }
    }

    private fun playVibration() {
        if (vibrationEnabled && vibrator != null) {
            try {
                val intensity = (vibrationIntensity / 100f) * 255
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(500, intensity.toInt())
                    vibrator?.vibrate(effect)
                } else {
                    vibrator?.vibrate(500)
                }
            } catch (e: Exception) {
                // Handle vibration errors
            }
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun selectMode(mode: TimerMode) {
        currentMode = mode
        resetTimer()

        // Update tab appearance
        binding.stopwatchTab.setBackgroundColor(if (mode == TimerMode.STOPWATCH) 
            resources.getColor(android.R.color.holo_red_dark, null) else 
            resources.getColor(android.R.color.transparent, null))
        
        binding.countdownTab.setBackgroundColor(if (mode == TimerMode.COUNTDOWN) 
            resources.getColor(android.R.color.holo_red_dark, null) else 
            resources.getColor(android.R.color.transparent, null))
        
        binding.intervalsTab.setBackgroundColor(if (mode == TimerMode.INTERVALS) 
            resources.getColor(android.R.color.holo_red_dark, null) else 
            resources.getColor(android.R.color.transparent, null))

        // Update UI based on mode
        when (mode) {
            TimerMode.STOPWATCH -> {
                binding.modeTitle.text = "Stopwatch"
                binding.resetBtn.visibility = View.VISIBLE
            }
            TimerMode.COUNTDOWN -> {
                binding.modeTitle.text = "Countdown"
                binding.resetBtn.visibility = View.VISIBLE
                if (totalTimeInMillis == 0L) {
                    totalTimeInMillis = 5 * 60 * 1000L // Default 5 minutes
                    timeInMillis = totalTimeInMillis
                }
            }
            TimerMode.INTERVALS -> {
                binding.modeTitle.text = "Intervals"
                binding.resetBtn.visibility = View.VISIBLE
            }
        }
        updateTimerDisplay()
    }

    private fun startTimer() {
        isRunning = true
        binding.startPauseBtn.text = "PAUSE"

        when (currentMode) {
            TimerMode.STOPWATCH -> startStopwatch()
            TimerMode.COUNTDOWN -> startCountdown()
            TimerMode.INTERVALS -> startIntervals()
        }
    }

    private fun pauseTimer() {
        isRunning = false
        binding.startPauseBtn.text = "START"
        countDownTimer?.cancel()
        stopwatchTimer?.cancel()
    }

    private fun resetTimer() {
        isRunning = false
        binding.startPauseBtn.text = "START"
        countDownTimer?.cancel()
        stopwatchTimer?.cancel()

        when (currentMode) {
            TimerMode.STOPWATCH -> {
                timeInMillis = 0L
                binding.progressRing.progress = 0
            }
            TimerMode.COUNTDOWN -> {
                timeInMillis = totalTimeInMillis
                binding.progressRing.progress = 100
            }
            TimerMode.INTERVALS -> {
                timeInMillis = 0L
                binding.progressRing.progress = 0
            }
        }
        updateTimerDisplay()
    }

    private fun startStopwatch() {
        stopwatchTimer = object : CountDownTimer(Long.MAX_VALUE, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeInMillis += 100
                updateTimerDisplay()
                // For stopwatch, progress increases
                val progress = ((timeInMillis % 60000) / 600).toInt()
                binding.progressRing.progress = progress
            }
            override fun onFinish() {}
        }.start()
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(timeInMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timeInMillis = millisUntilFinished
                updateTimerDisplay()
                val progress = ((timeInMillis * 100) / totalTimeInMillis).toInt()
                binding.progressRing.progress = progress
            }
            override fun onFinish() {
                isRunning = false
                binding.startPauseBtn.text = "START"
                timeInMillis = 0L
                updateTimerDisplay()
                binding.progressRing.progress = 0
            }
        }.start()
    }

    private fun startIntervals() {
        // Simple interval timer - 30 seconds work, 10 seconds rest
        startStopwatch() // For now, use stopwatch logic
    }

    private fun updateTimerDisplay() {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - TimeUnit.MINUTES.toSeconds(minutes)
        binding.timerDisplay.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun showTimePickerDialog() {
        // Simple time picker - for demo, just set to 5 minutes
        totalTimeInMillis = 5 * 60 * 1000L
        timeInMillis = totalTimeInMillis
        updateTimerDisplay()
        binding.progressRing.progress = 100
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        stopwatchTimer?.cancel()
        _binding = null
    }
}
