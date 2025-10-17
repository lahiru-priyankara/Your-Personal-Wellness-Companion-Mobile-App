package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentWellnessTipsBinding
import java.util.*

class WellnessTipsFragment : Fragment() {

    private var _binding: FragmentWellnessTipsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tipsAdapter: WellnessTipsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWellnessTipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadWellnessTips()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        tipsAdapter = WellnessTipsAdapter { tip -> showTipDetails(tip) }
        binding.tipsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tipsRecyclerView.adapter = tipsAdapter
    }

    private fun loadWellnessTips() {
        val tips = getWellnessTips()
        tipsAdapter.submitList(tips)
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.refreshTipsButton.setOnClickListener {
            loadWellnessTips()
        }
        
        binding.categoryFilter.setOnClickListener {
            showCategoryFilter()
        }
    }

    private fun showCategoryFilter() {
        val categories = arrayOf("All", "Mental Health", "Physical Health", "Nutrition", "Sleep", "Stress Management", "Mindfulness")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📚 Filter by Category")
            .setItems(categories) { _, which ->
                val selectedCategory = categories[which]
                filterTipsByCategory(selectedCategory)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun filterTipsByCategory(category: String) {
        val allTips = getWellnessTips()
        val filteredTips = if (category == "All") {
            allTips
        } else {
            allTips.filter { it.category == category }
        }
        tipsAdapter.submitList(filteredTips)
    }

    private fun showTipDetails(tip: WellnessTip) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💡 ${tip.title}")
            .setMessage("${tip.description}\n\n${tip.detailedInfo}")
            .setPositiveButton("Save Tip") { _, _ ->
                saveTipToFavorites(tip)
            }
            .setNeutralButton("Share") { _, _ ->
                shareTip(tip)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun saveTipToFavorites(tip: WellnessTip) {
        val prefs = requireContext().getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet("favorite_tips", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        favorites.add(tip.id)
        prefs.edit().putStringSet("favorite_tips", favorites).apply()
        
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "💾 Tip saved to favorites!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun shareTip(tip: WellnessTip) {
        val shareText = """
            💡 Wellness Tip: ${tip.title}
            
            ${tip.description}
            
            ${tip.detailedInfo}
            
            #WellnessTips #HealthyLiving #SLIITApp
        """.trimIndent()
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Wellness Tip: ${tip.title}")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Wellness Tip"))
    }

    private fun getWellnessTips(): List<WellnessTip> {
        return listOf(
            WellnessTip(
                id = "hydration_tip",
                title = "Stay Hydrated",
                category = "Physical Health",
                description = "Drink water throughout the day for optimal health",
                detailedInfo = "Aim for 8 glasses of water daily. Start your morning with a glass of water to kickstart your metabolism. Carry a water bottle with you and set reminders to drink water every hour.",
                icon = "💧"
            ),
            WellnessTip(
                id = "sleep_tip",
                title = "Quality Sleep",
                category = "Sleep",
                description = "Establish a consistent sleep routine for better rest",
                detailedInfo = "Go to bed and wake up at the same time every day, even on weekends. Create a relaxing bedtime routine, avoid screens 1 hour before bed, and keep your bedroom cool and dark.",
                icon = "😴"
            ),
            WellnessTip(
                id = "mindfulness_tip",
                title = "Daily Mindfulness",
                category = "Mindfulness",
                description = "Practice mindfulness to reduce stress and improve focus",
                detailedInfo = "Start with just 5 minutes of meditation daily. Focus on your breathing, observe your thoughts without judgment, and gradually increase your practice time.",
                icon = "🧘‍♀️"
            ),
            WellnessTip(
                id = "exercise_tip",
                title = "Regular Movement",
                category = "Physical Health",
                description = "Incorporate physical activity into your daily routine",
                detailedInfo = "Aim for at least 30 minutes of moderate exercise daily. This can include walking, dancing, yoga, or any activity you enjoy. Start small and build consistency.",
                icon = "🏃‍♀️"
            ),
            WellnessTip(
                id = "nutrition_tip",
                title = "Balanced Nutrition",
                category = "Nutrition",
                description = "Eat a variety of colorful fruits and vegetables",
                detailedInfo = "Include all food groups in your meals. Eat the rainbow - different colored fruits and vegetables provide different nutrients. Limit processed foods and added sugars.",
                icon = "🥗"
            ),
            WellnessTip(
                id = "stress_tip",
                title = "Stress Management",
                category = "Stress Management",
                description = "Develop healthy coping strategies for stress",
                detailedInfo = "Practice deep breathing, take regular breaks, maintain social connections, and engage in activities you enjoy. Remember that it's okay to ask for help when needed.",
                icon = "🌿"
            ),
            WellnessTip(
                id = "mental_health_tip",
                title = "Mental Health Awareness",
                category = "Mental Health",
                description = "Prioritize your mental wellbeing",
                detailedInfo = "Check in with yourself regularly, practice self-compassion, maintain healthy boundaries, and don't hesitate to seek professional help when needed. Your mental health matters.",
                icon = "🧠"
            ),
            WellnessTip(
                id = "gratitude_tip",
                title = "Gratitude Practice",
                category = "Mental Health",
                description = "Cultivate gratitude for improved wellbeing",
                detailedInfo = "Write down 3 things you're grateful for each day. This simple practice can improve mood, reduce stress, and help you focus on the positive aspects of life.",
                icon = "🙏"
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class WellnessTip(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val detailedInfo: String,
    val icon: String
)
