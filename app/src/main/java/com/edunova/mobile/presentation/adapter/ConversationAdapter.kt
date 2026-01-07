package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemConversationBinding
import com.edunova.mobile.domain.model.Conversation
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val onConversationClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            binding.apply {
                textViewParticipantName.text = conversation.participantName
                textViewLastMessage.text = conversation.lastMessage ?: "Aucun message"
                textViewTimestamp.text = formatTimestamp(conversation.lastMessageTimestamp)
                
                // Indicateur de messages non lus
                if (conversation.unreadCount > 0) {
                    textViewUnreadCount.text = conversation.unreadCount.toString()
                    textViewUnreadCount.visibility = android.view.View.VISIBLE
                } else {
                    textViewUnreadCount.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onConversationClick(conversation)
                }
            }
        }

        private fun formatTimestamp(timestamp: Long?): String {
            if (timestamp == null) return ""
            
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60_000 -> "À l'instant"
                diff < 3600_000 -> "${diff / 60_000}min"
                diff < 86400_000 -> "${diff / 3600_000}h"
                else -> {
                    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }

    private class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }
}