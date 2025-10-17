package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChallengesAdapter(
    private val onChallengeClick: (Challenge) -> Unit
) : ListAdapter<Challenge, ChallengesAdapter.ChallengeViewHolder>(ChallengeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(getItem(position), onChallengeClick)
    }

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val challengeCard: CardView = itemView.findViewById(R.id.challengeCard)
        private val challengeEmoji: TextView = itemView.findViewById(R.id.challengeEmoji)
        private val challengeTitle: TextView = itemView.findViewById(R.id.challengeTitle)
        private val challengeDuration: TextView = itemView.findViewById(R.id.challengeDuration)

        fun bind(challenge: Challenge, onChallengeClick: (Challenge) -> Unit) {
            challengeEmoji.text = challenge.emoji
            challengeTitle.text = challenge.title
            challengeDuration.text = "${challenge.durationDays} days"
            
            challengeCard.setOnClickListener {
                onChallengeClick(challenge)
            }
        }
    }

    private class ChallengeDiffCallback : DiffUtil.ItemCallback<Challenge>() {
        override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
            return oldItem == newItem
        }
    }
}
