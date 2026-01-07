package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemCourseBinding
import com.edunova.mobile.domain.model.Course

class CourseAdapter(
    private val onCourseClick: (Course) -> Unit
) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CourseViewHolder(
        private val binding: ItemCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            binding.apply {
                textCourseTitle.text = course.title
                textCourseDescription.text = course.description ?: "Aucune description"
                textStudentsCount.text = "${course.studentsCount} étudiants"
                textContentsCount.text = "${course.contentsCount} contenus"
                
                // Format date
                course.createdAt?.let { dateStr ->
                    try {
                        // Simplifier l'affichage de la date
                        textCourseDate.text = "Créé le ${dateStr.substring(0, 10)}"
                    } catch (e: Exception) {
                        textCourseDate.text = "Date inconnue"
                    }
                } ?: run {
                    textCourseDate.text = "Date inconnue"
                }

                root.setOnClickListener {
                    onCourseClick(course)
                }
            }
        }
    }

    private class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem == newItem
        }
    }
}