package com.example.myapplication

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object WeatherHabits {
    
    private val weatherBasedHabits = mapOf(
        "sunny" to listOf(
            "☀️ Take a 15-minute walk in the sunshine",
            "🌻 Spend time in your garden or balcony",
            "🏃‍♀️ Go for a morning jog outside",
            "🧘‍♀️ Do outdoor yoga or meditation",
            "📚 Read a book in the park",
            "🚴‍♀️ Go for a bike ride",
            "🏖️ Have a picnic lunch outside"
        ),
        "rainy" to listOf(
            "☔ Practice indoor yoga or stretching",
            "📖 Read a book with a cup of tea",
            "🎨 Try a creative hobby like drawing",
            "🧘‍♀️ Meditate while listening to rain sounds",
            "🍲 Cook a healthy meal from scratch",
            "📝 Journal about your thoughts and feelings",
            "🎵 Listen to calming music"
        ),
        "cloudy" to listOf(
            "🚶‍♀️ Take a gentle walk in the cool air",
            "📚 Visit a library or bookstore",
            "☕ Enjoy a warm beverage mindfully",
            "🧘‍♂️ Practice breathing exercises",
            "📱 Organize your digital photos",
            "🌱 Tend to indoor plants",
            "✍️ Write gratitude notes"
        ),
        "hot" to listOf(
            "🏊‍♀️ Go swimming or water activities",
            "🧊 Stay hydrated with infused water",
            "🌅 Wake up early for cooler activities",
            "🧘‍♀️ Practice cooling breathing techniques",
            "🍉 Eat hydrating fruits and vegetables",
            "🌿 Stay in shaded areas for outdoor time",
            "💧 Take refreshing breaks throughout the day"
        ),
        "cold" to listOf(
            "🔥 Warm up with gentle exercises",
            "🍵 Enjoy hot herbal teas",
            "🧥 Layer up and take a brisk walk",
            "🧘‍♀️ Practice warming meditation",
            "🍲 Prepare hearty, nutritious meals",
            "📚 Cozy up with a good book",
            "🔥 Light candles for ambiance"
        )
    )
    
    private val timeBasedHabits = mapOf(
        "morning" to listOf(
            "🌅 Watch the sunrise",
            "🧘‍♀️ Morning meditation (5-10 minutes)",
            "💧 Drink a glass of water",
            "📝 Write morning intentions",
            "🏃‍♀️ Light stretching or yoga",
            "📚 Read inspirational content",
            "☀️ Get natural sunlight exposure"
        ),
        "afternoon" to listOf(
            "🚶‍♀️ Take a walking break",
            "🍎 Have a healthy snack",
            "💧 Hydrate with water",
            "🧘‍♀️ Quick breathing exercise",
            "📱 Digital detox for 15 minutes",
            "🌿 Step outside for fresh air",
            "📝 Check in with your goals"
        ),
        "evening" to listOf(
            "🌅 Watch the sunset",
            "📝 Reflect on your day",
            "🧘‍♀️ Evening meditation",
            "📚 Read before bed",
            "🛁 Relaxing bath or shower",
            "📱 Put away devices 1 hour before bed",
            "🌙 Prepare for tomorrow"
        )
    )
    
    fun getWeatherBasedHabits(weatherCondition: String, timeOfDay: String): List<String> {
        val weatherHabits = weatherBasedHabits[weatherCondition] ?: weatherBasedHabits["sunny"]!!
        val timeHabits = timeBasedHabits[timeOfDay] ?: timeBasedHabits["morning"]!!
        
        return (weatherHabits + timeHabits).distinct().shuffled().take(5)
    }
    
    fun getSmartHabitRecommendation(
        context: Context,
        currentHabits: List<Habit>,
        recentMood: String? = null
    ): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeOfDay = when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            else -> "evening"
        }
        
        // Simulate weather based on season and time
        val weatherCondition = getSimulatedWeather()
        
        val recommendations = getWeatherBasedHabits(weatherCondition, timeOfDay)
        val filteredRecommendations = recommendations.filter { suggestion ->
            !currentHabits.any { existingHabit ->
                suggestion.lowercase().contains(existingHabit.name.lowercase().take(10)) ||
                existingHabit.name.lowercase().contains(suggestion.lowercase().take(10))
            }
        }
        
        val selectedRecommendation = filteredRecommendations.firstOrNull() ?: recommendations.first()
        
        return buildSmartRecommendation(selectedRecommendation, weatherCondition, timeOfDay, recentMood)
    }
    
    private fun getSimulatedWeather(): String {
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        return when {
            month in 5..8 && hour in 10..16 -> "hot" // Summer afternoon
            month in 11..2 -> "cold" // Winter
            hour in 6..10 -> "sunny" // Morning
            hour in 14..18 -> "cloudy" // Afternoon
            else -> "rainy" // Evening/other times
        }
    }
    
    private fun buildSmartRecommendation(
        habit: String,
        weather: String,
        timeOfDay: String,
        recentMood: String?
    ): String {
        val context = when {
            weather == "sunny" && timeOfDay == "morning" -> "Perfect morning for outdoor activities!"
            weather == "rainy" && timeOfDay == "evening" -> "Cozy evening for indoor wellness!"
            recentMood in listOf("😢", "😰") -> "This gentle activity might help lift your spirits"
            recentMood in listOf("😊", "😄") -> "Great time to build on your positive energy!"
            else -> "Based on current conditions, this would be perfect for you"
        }
        
        return """
            💡 Smart Habit Suggestion
            
            $habit
            
            $context
            
            🌤️ Weather: ${weather.capitalize()}
            ⏰ Time: ${timeOfDay.capitalize()}
            
            ${getMotivationalTip(weather, timeOfDay)}
        """.trimIndent()
    }
    
    private fun getMotivationalTip(weather: String, timeOfDay: String): String {
        return when {
            weather == "sunny" && timeOfDay == "morning" -> "☀️ Sunshine boosts vitamin D and mood!"
            weather == "rainy" && timeOfDay == "evening" -> "🌧️ Rain creates the perfect cozy atmosphere for self-care"
            timeOfDay == "morning" -> "🌅 Starting your day with intention sets a positive tone"
            timeOfDay == "evening" -> "🌙 Evening routines help you wind down and prepare for rest"
            else -> "💪 Every small step towards wellness counts!"
        }
    }
    
    fun getSeasonalHabits(): List<String> {
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val season = when (month) {
            in 2..4 -> "spring"
            in 5..7 -> "summer"
            in 8..10 -> "autumn"
            else -> "winter"
        }
        
        return when (season) {
            "spring" -> listOf(
                "🌸 Take a nature walk and notice new growth",
                "🌱 Start a small garden or plant herbs",
                "🧹 Spring cleaning for mental clarity",
                "🚴‍♀️ Go for bike rides in the fresh air",
                "🌺 Practice outdoor yoga"
            )
            "summer" -> listOf(
                "🏊‍♀️ Swimming or water activities",
                "🌅 Early morning outdoor activities",
                "🍉 Eat seasonal fruits and vegetables",
                "🌞 Get vitamin D from safe sun exposure",
                "🏖️ Beach or park visits"
            )
            "autumn" -> listOf(
                "🍂 Collect and press autumn leaves",
                "🎃 Try seasonal cooking with pumpkins",
                "🚶‍♀️ Take walks to enjoy fall colors",
                "📚 Cozy up with books and tea",
                "🧘‍♀️ Practice gratitude for the harvest season"
            )
            "winter" -> listOf(
                "🔥 Warm up with hot beverages",
                "🧥 Layer up for outdoor winter activities",
                "🍲 Cook hearty, warming meals",
                "📚 Read by the fireplace or heater",
                "🧘‍♀️ Practice warming meditation"
            )
            else -> emptyList()
        }
    }
}
