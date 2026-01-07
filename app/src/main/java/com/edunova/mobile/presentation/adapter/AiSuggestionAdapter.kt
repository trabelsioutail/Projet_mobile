package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.R
import com.edunova.mobile.domain.model.AiSuggestion

class AiSuggestionAdapter(
    private val onSuggestionClick: (AiSuggestion) -> Unit
) : ListAdapter<AiSuggestion, AiSuggestionAdapter.SuggestionViewHolder>(SuggestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewSuggestionIcon: TextView = itemView.findViewById(R.id.textViewSuggestionIcon)
        private val textViewSuggestionText: TextView = itemView.findViewById(R.id.textViewSuggestionText)

        fun bind(suggestion: AiSuggestion) {
            textViewSuggestionIcon.text = suggestion.icon
            textViewSuggestionText.text = suggestion.text
            
            itemView.setOnClickListener {
                onSuggestionClick(suggestion)
            }
        }
    }

    private class SuggestionDiffCallback : DiffUtil.ItemCallback<AiSuggestion>() {
        override fun areItemsTheSame(oldItem: AiSuggestion, newItem: AiSuggestion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AiSuggestion, newItem: AiSuggestion): Boolean {
            return oldItem == newItem
        }
    }
}