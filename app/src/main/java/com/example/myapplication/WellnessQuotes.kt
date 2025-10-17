package com.example.myapplication

import kotlin.random.Random

object WellnessQuotes {
    
    private val motivationalQuotes = listOf(
        "🌅 Every day is a new beginning. Take a deep breath and start again.",
        "💪 You are stronger than you think and more capable than you imagine.",
        "🌱 Small steps every day lead to big changes every year.",
        "✨ Your potential is endless. Go do what you were created to do.",
        "🎯 Success is the sum of small efforts repeated day in and day out.",
        "🌟 Believe in yourself and all that you are. You are amazing!",
        "🔥 The only impossible journey is the one you never begin.",
        "💎 You are a work in progress, and that's perfectly okay.",
        "🌈 Every accomplishment starts with the decision to try.",
        "🦋 Progress, not perfection. Every step counts!",
        "🌸 Be gentle with yourself. You're doing better than you think.",
        "⭐ Your wellness journey is unique to you. Embrace it!",
        "🌿 Healthy habits are like compound interest - they grow over time.",
        "💫 Today's choices become tomorrow's opportunities.",
        "🌺 Self-care isn't selfish. It's essential.",
        "🎨 You have the power to create the life you want.",
        "🌻 Consistency beats perfection every time.",
        "🚀 Your future self will thank you for starting today.",
        "🎪 Life is too important to be taken too seriously. Enjoy the journey!",
        "🌙 Rest when you're tired, but never give up on your dreams."
    )
    
    private val habitEncouragement = listOf(
        "🎯 Great job building this habit! Keep the momentum going!",
        "💪 Your consistency is impressive! This habit is becoming second nature.",
        "🔥 Amazing streak! You're proving that small actions create big changes.",
        "⭐ This habit is shaping a better you, one day at a time!",
        "🌟 Your dedication to this habit is inspiring!",
        "🎊 Celebration time! You're crushing this habit!",
        "💎 This habit is becoming a valuable part of your daily routine.",
        "🌈 Your persistence with this habit is paying off beautifully!",
        "🚀 You're on fire with this habit! Keep reaching for the stars!",
        "🌱 Watch this habit grow into something amazing in your life!"
    )
    
    private val moodBoosts = listOf(
        "😊 Remember: You're allowed to feel your emotions. They're all valid.",
        "🌈 This feeling is temporary. You've overcome challenges before.",
        "💝 You deserve love, kindness, and all the good things in life.",
        "🌟 Your mental health matters. Take time to nurture it today.",
        "🦋 It's okay to have bad days. Tomorrow is a fresh start.",
        "💙 You are not alone. Your feelings matter and so do you.",
        "🌸 Be kind to yourself today. You're doing the best you can.",
        "⭐ Every emotion teaches us something. What is today teaching you?",
        "🎨 You have the strength to turn this day around.",
        "🌺 Your feelings are valid, but they don't define your worth."
    )
    
    private val wellnessTips = listOf(
        "💧 Hydration tip: Start your day with a glass of water!",
        "🧘‍♀️ Try 5 minutes of deep breathing when you feel stressed.",
        "🌅 Morning sunlight helps regulate your sleep cycle naturally.",
        "🚶‍♀️ A 10-minute walk can boost your mood significantly!",
        "📱 Consider a digital detox for 30 minutes before bed.",
        "🥗 Add one extra serving of vegetables to your day.",
        "😴 Quality sleep is the foundation of good health.",
        "🎵 Music can be a powerful mood enhancer. What's your feel-good song?",
        "📝 Journaling for 5 minutes can help process your thoughts.",
        "🤝 Connect with a friend or loved one today. Social connections boost wellness!",
        "🌳 Spend time in nature, even if it's just looking at trees outside.",
        "🧘‍♂️ Practice gratitude - name 3 things you're thankful for right now.",
        "💪 Stretch for 5 minutes to release physical tension.",
        "🎯 Set small, achievable goals to build momentum.",
        "🌟 Celebrate small wins - they lead to big transformations!"
    )
    
    private val achievementCelebrations = listOf(
        "🎉 Outstanding! You've reached a major milestone!",
        "🏆 Champion level unlocked! Your dedication is incredible!",
        "⭐ Superstar achievement! You're setting an amazing example!",
        "🔥 On fire! This achievement shows your true commitment!",
        "💎 Brilliant work! You've earned this achievement through consistency!",
        "🌟 Stellar performance! This achievement is well-deserved!",
        "🚀 Incredible achievement! You're reaching new heights!",
        "👑 Achievement royalty! Your persistence has paid off magnificently!",
        "💪 Powerhouse achievement! You're unstoppable!",
        "🎊 Celebration worthy! This achievement is a testament to your growth!"
    )
    
    fun getRandomQuote(): String {
        return motivationalQuotes.random()
    }
    
    fun getHabitEncouragement(): String {
        return habitEncouragement.random()
    }
    
    fun getMoodBoost(): String {
        return moodBoosts.random()
    }
    
    fun getWellnessTip(): String {
        return wellnessTips.random()
    }
    
    fun getAchievementCelebration(): String {
        return achievementCelebrations.random()
    }
    
    fun getQuoteByMood(mood: String): String {
        return when (mood) {
            "😢", "😰", "😕" -> getMoodBoost()
            "😊", "😄", "🤩", "😍" -> getRandomQuote()
            else -> getWellnessTip()
        }
    }
    
    fun getQuoteForStreak(streak: Int): String {
        return when {
            streak >= 30 -> "🏆 30 days! You're a habit master! ${achievementCelebrations.random()}"
            streak >= 14 -> "🔥 Two weeks strong! ${habitEncouragement.random()}"
            streak >= 7 -> "⭐ One week streak! ${habitEncouragement.random()}"
            streak >= 3 -> "💪 Building momentum! ${habitEncouragement.random()}"
            else -> getRandomQuote()
        }
    }
    
    fun getDailyWisdom(): String {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        val allQuotes = motivationalQuotes + wellnessTips
        return allQuotes[dayOfYear % allQuotes.size]
    }
}
