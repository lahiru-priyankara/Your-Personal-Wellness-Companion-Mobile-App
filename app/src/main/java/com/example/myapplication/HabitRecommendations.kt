package com.example.myapplication

import kotlin.random.Random

object HabitRecommendations {
    
    private val beginnerHabits = listOf(
        "💧 Drink a glass of water when you wake up",
        "🧘‍♀️ Take 5 deep breaths before bed",
        "📱 Put phone away 30 minutes before sleep",
        "🚶‍♀️ Take a 10-minute walk outside",
        "📝 Write down 3 things you're grateful for",
        "🌅 Watch the sunrise or sunset",
        "🍎 Eat one piece of fruit daily",
        "📚 Read for 15 minutes",
        "🛏️ Make your bed every morning",
        "😊 Smile at yourself in the mirror"
    )
    
    private val intermediateHabits = listOf(
        "🏋️‍♀️ Do 20 pushups or squats",
        "🥗 Eat a healthy salad daily",
        "📖 Journal for 10 minutes",
        "🧘‍♂️ Meditate for 10-15 minutes",
        "💧 Drink 8 glasses of water",
        "🚫 No social media for 2 hours",
        "🎨 Practice a creative hobby for 30 minutes",
        "📞 Call a friend or family member",
        "🌱 Learn something new for 20 minutes",
        "🧹 Organize one area of your living space"
    )
    
    private val advancedHabits = listOf(
        "🏃‍♀️ Exercise for 30+ minutes",
        "📚 Read for 45+ minutes daily",
        "🧘‍♀️ Meditate for 20+ minutes",
        "🍽️ Practice mindful eating all meals",
        "💪 Complete a full workout routine",
        "📝 Write 500+ words in a journal",
        "🌿 Spend 1+ hour in nature",
        "🎯 Work on a personal goal for 1+ hour",
        "🤝 Perform a random act of kindness",
        "📱 Have a complete digital detox day"
    )
    
    private val weeklyChallenge = listOf(
        "🎨 Try a new creative activity each day this week",
        "🌍 Learn about a different culture each day",
        "🍳 Cook a new healthy recipe each day",
        "📞 Connect with a different friend/family member daily",
        "🌱 Try a new form of exercise each day",
        "📚 Read about a new topic each day",
        "🧘‍♀️ Try different meditation techniques daily",
        "🎵 Listen to a new genre of music each day"
    )
    
    private val moodBasedHabits = mapOf(
        "😢" to listOf(
            "🫂 Reach out to a supportive friend",
            "🛁 Take a relaxing bath or shower", 
            "🎵 Listen to uplifting music",
            "🌿 Spend time in nature",
            "📝 Write about your feelings"
        ),
        "😊" to listOf(
            "🎉 Celebrate this good mood with others",
            "📸 Take photos of beautiful moments",
            "🎁 Do something kind for someone",
            "🌟 Set a positive intention for tomorrow",
            "📝 Write down what made you happy"
        ),
        "😰" to listOf(
            "🫁 Practice deep breathing exercises",
            "🧘‍♀️ Try a 5-minute meditation",
            "🚶‍♀️ Take a gentle walk",
            "☕ Have a calming tea",
            "📱 Limit news/social media today"
        ),
        "😴" to listOf(
            "☕ Drink some water and have caffeine mindfully",
            "🌅 Get some natural light exposure",
            "🏃‍♀️ Do light stretching or movement",
            "🍎 Eat something energizing and healthy",
            "💤 Plan for better sleep tonight"
        )
    )
    
    fun getRecommendationsForUser(
        currentHabits: List<Habit>,
        userLevel: String = "beginner",
        recentMood: String? = null
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Add mood-based recommendations first
        recentMood?.let { mood ->
            moodBasedHabits[mood]?.let { moodHabits ->
                recommendations.addAll(moodHabits.shuffled().take(2))
            }
        }
        
        // Get level-appropriate habits
        val levelHabits = when (userLevel) {
            "advanced" -> advancedHabits
            "intermediate" -> intermediateHabits
            else -> beginnerHabits
        }
        
        // Filter out habits similar to existing ones
        val filteredHabits = levelHabits.filter { suggestion ->
            !currentHabits.any { existingHabit ->
                suggestion.lowercase().contains(existingHabit.name.lowercase().take(10)) ||
                existingHabit.name.lowercase().contains(suggestion.lowercase().take(10))
            }
        }
        
        // Add level-appropriate recommendations
        recommendations.addAll(filteredHabits.shuffled().take(3))
        
        // Add a weekly challenge
        recommendations.add(weeklyChallenge.random())
        
        return recommendations.distinct().take(5)
    }
    
    fun getUserLevel(habits: List<Habit>): String {
        val averageStreak = if (habits.isNotEmpty()) {
            habits.sumOf { HabitsFragmentHelper.calculateStreak(it) } / habits.size
        } else 0
        
        return when {
            habits.size >= 8 || averageStreak >= 21 -> "advanced"
            habits.size >= 4 || averageStreak >= 7 -> "intermediate"
            else -> "beginner"
        }
    }
    
    fun getMotivationalContext(userLevel: String): String {
        return when (userLevel) {
            "advanced" -> "🌟 You're a wellness champion! Here are some challenging habits to keep growing:"
            "intermediate" -> "💪 You're building great momentum! Ready for the next level?"
            else -> "🌱 Great start on your wellness journey! Here are some gentle habits to begin with:"
        }
    }
    
    fun getPersonalizedTip(habits: List<Habit>, recentMood: String?): String {
        val streaks = habits.map { HabitsFragmentHelper.calculateStreak(it) }
        val maxStreak = streaks.maxOrNull() ?: 0
        val avgStreak = if (streaks.isNotEmpty()) streaks.average().toInt() else 0
        
        return when {
            maxStreak >= 30 -> "🏆 Incredible! You have a 30+ day streak. You're proving that consistency creates lasting change!"
            avgStreak >= 14 -> "🔥 Your average streak is 2+ weeks! You're developing powerful life-changing habits."
            habits.size >= 5 -> "🎯 Managing ${habits.size} habits shows great commitment! Focus on consistency over perfection."
            habits.isEmpty() -> "🌱 Starting is the hardest part, and you're here! Every expert was once a beginner."
            recentMood == "😢" -> "💙 Remember, tough days don't last, but tough people do. Your habits are your anchor."
            recentMood == "😊" -> "🌈 Your positive energy is contagious! Use this good mood to strengthen your habits."
            else -> "💫 Small daily improvements lead to stunning yearly results. Keep going!"
        }
    }
}
