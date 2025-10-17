package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar for full screen experience
        supportActionBar?.hide()

        // Animate logo and text
        animateSplashElements()

        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000) // 3 second splash screen
    }

    private fun animateSplashElements() {
        // Logo animation
        binding.logo.alpha = 0f
        binding.logo.animate()
            .alpha(1f)
            .setDuration(1000)
            .start()

        // App name animation
        binding.appName.alpha = 0f
        binding.appName.translationY = 50f
        binding.appName.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1200)
            .setStartDelay(500)
            .start()

        // Tagline animation
        binding.tagline.alpha = 0f
        binding.tagline.translationY = 30f
        binding.tagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1000)
            .setStartDelay(1000)
            .start()

        // Loading indicator animation
        binding.loadingIndicator.alpha = 0f
        binding.loadingIndicator.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(1500)
            .start()
    }
}
