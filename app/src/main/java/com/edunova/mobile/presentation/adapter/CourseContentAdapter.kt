package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemCourseContentBinding
import com.edunova.mobile.domain.model.CourseContent
import com.edunova.mobile.domain.model.ContentType

class CourseContentAdapter(
    private val onContentClick: (CourseContent) -> Unit
) : ListAdapter<CourseContent, CourseContentAdapter.ContentViewHolder>(ContentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val binding = ItemCourseContentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContentViewHolder(
        private val binding: ItemCourseContentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(content: CourseContent) {
            binding.apply {
                tvContentTitle.text = content.title
                tvContentType.text = when (content.contentType) {
                    ContentType.PDF -> "Document PDF"
                    ContentType.VIDEO -> "Vidéo"
                    ContentType.DOCUMENT -> "Document"
                    ContentType.LINK -> "Lien"
                }
                tvCreatedDate.text = content.createdAt
                
                // Icône selon le type de contenu
                val iconRes = when (content.contentType) {
                    ContentType.PDF -> com.edunova.mobile.R.drawable.ic_pdf
                    ContentType.VIDEO -> com.edunova.mobile.R.drawable.ic_video
                    ContentType.DOCUMENT -> com.edunova.mobile.R.drawable.ic_document
                    ContentType.LINK -> com.edunova.mobile.R.drawable.ic_link
                }
                ivContentIcon.setImageResource(iconRes)
                
                // Couleur selon le type
                val colorRes = when (content.contentType) {
                    ContentType.PDF -> com.edunova.mobile.R.color.error_color
                    ContentType.VIDEO -> com.edunova.mobile.R.color.primary_color
                    ContentType.DOCUMENT -> com.edunova.mobile.R.color.success_color
                    ContentType.LINK -> com.edunova.mobile.R.color.warning_color
                }
                cardContent.setCardBackgroundColor(
                    itemView.context.getColor(colorRes).let { color ->
                        // Rendre la couleur plus transparente
                        (color and 0x00FFFFFF) or 0x20000000
                    }
                )
                
                root.setOnClickListener {
                    onContentClick(content)
                }
                
                btnDownload.setOnClickListener {
                    // TODO: Implémenter le téléchargement
                }
                
                btnShare.setOnClickListener {
                    // TODO: Implémenter le partage
                }
            }
        }
    }

    private class ContentDiffCallback : DiffUtil.ItemCallback<CourseContent>() {
        override fun areItemsTheSame(oldItem: CourseContent, newItem: CourseContent): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CourseContent, newItem: CourseContent): Boolean {
            return oldItem == newItem
        }
    }
}