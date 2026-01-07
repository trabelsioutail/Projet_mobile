package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.R
import com.edunova.mobile.domain.model.AiChatMessage
import java.text.SimpleDateFormat
import java.util.*

class AiChatAdapter(
    private val onSuggestionClick: (String) -> Unit
) : ListAdapter<AiChatMessage, AiChatAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutUserMessage: View = itemView.findViewById(R.id.layoutUserMessage)
        private val layoutAiMessage: View = itemView.findViewById(R.id.layoutAiMessage)
        private val textViewUserMessage: TextView = itemView.findViewById(R.id.textViewUserMessage)
        private val textViewAiMessage: TextView = itemView.findViewById(R.id.textViewAiMessage)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        private val recyclerViewMessageSuggestions: RecyclerView = itemView.findViewById(R.id.recyclerViewMessageSuggestions)

        fun bind(message: AiChatMessage) {
            if (message.isFromUser) {
                layoutUserMessage.visibility = View.VISIBLE
                layoutAiMessage.visibility = View.GONE
                textViewUserMessage.text = message.content
            } else {
                layoutUserMessage.visibility = View.GONE
                layoutAiMessage.visibility = View.VISIBLE
                textViewAiMessage.text = message.content
                
                // Setup suggestions if available
                if (message.suggestions.isNotEmpty()) {
                    recyclerViewMessageSuggestions.visibility = View.VISIBLE
                    val suggestionsAdapter = AiSuggestionSmallAdapter(onSuggestionClick)
                    recyclerViewMessageSuggestions.layoutManager = LinearLayoutManager(
                        itemView.context, 
                        LinearLayoutManager.HORIZONTAL, 
                        false
                    )
                    recyclerViewMessageSuggestions.adapter = suggestionsAdapter
                    suggestionsAdapter.submitList(message.suggestions)
                } else {
                    recyclerViewMessageSuggestions.visibility = View.GONE
                }
            }

            // Format timestamp
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            textViewTimestamp.text = timeFormat.format(Date(message.timestamp))
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<AiChatMessage>() {
        override fun areItemsTheSame(oldItem: AiChatMessage, newItem: AiChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AiChatMessage, newItem: AiChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}

class AiSuggestionSmallAdapter(
    private val onSuggestionClick: (String) -> Unit
) : ListAdapter<String, AiSuggestionSmallAdapter.SuggestionViewHolder>(StringDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_suggestion_small, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipSuggestion: com.google.android.material.chip.Chip = 
            itemView.findViewById(R.id.chipSuggestion)

        fun bind(suggestion: String) {
            chipSuggestion.text = suggestion
            chipSuggestion.setOnClickListener {
                onSuggestionClick(suggestion)
            }
        }
    }

    private class StringDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}