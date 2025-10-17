package com.example.myapplication

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private var sensorManager: SensorManager? = null
    private var lastShakeTs: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Configure bottom navigation for icon-only display
        binding.bottomNavigation.labelVisibilityMode = com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }


    override fun onResume() {
        super.onResume()
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val now = System.currentTimeMillis()
        if (magnitude > 25 && now - lastShakeTs > 3000) {
            lastShakeTs = now
            addQuickMood()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    private fun addQuickMood() {
        val prefs = getSharedPreferences("wellness_prefs", MODE_PRIVATE)
        val arr = org.json.JSONArray(prefs.getString("moods_json", "[]"))
        val obj = org.json.JSONObject()
        obj.put("emoji", "⚡")
        obj.put("note", "Quick mood via shake")
        obj.put("timestamp", System.currentTimeMillis())
        arr.put(obj)
        prefs.edit().putString("moods_json", arr.toString()).apply()
        Snackbar.make(binding.root, "Quick mood added", Snackbar.LENGTH_SHORT).show()
    }
}