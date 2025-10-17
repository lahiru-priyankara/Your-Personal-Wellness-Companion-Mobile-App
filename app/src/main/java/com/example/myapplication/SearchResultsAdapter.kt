package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemSearchResultBinding

class SearchResultsAdapter : ListAdapter<SearchResult, SearchResultsAdapter.SearchVH>(Diff) {

    object Diff : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
            oldItem.title == newItem.title && oldItem.type == newItem.type

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchVH {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchVH(binding)
    }

    override fun onBindViewHolder(holder: SearchVH, position: Int) {
        holder.bind(getItem(position))
    }

    class SearchVH(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchResult) {
            binding.typeLabel.text = item.type
            binding.titleText.text = item.title
            binding.subtitleText.text = item.subtitle
        }
    }
}
