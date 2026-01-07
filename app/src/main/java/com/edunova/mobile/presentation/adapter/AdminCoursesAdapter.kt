package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemAdminCourseBinding
import com.edunova.mobile.data.repository.AdminCourse

class AdminCoursesAdapter(
    private val onViewCourse: (AdminCourse) -> Unit,
    private val onEditCourse: (AdminCourse) -> Unit,
    private val onDeleteCourse: (AdminCourse) -> Unit,
    private val onToggleCourseStatus: (AdminCourse) -> Unit,
    private val onManageEnrollments: (AdminCourse) -> Unit,
    private val onViewStatistics: (AdminCourse) -> Unit
) : ListAdapter<AdminCourse, AdminCoursesAdapter.CourseViewHolder>(CourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemAdminCourseBinding.inflate(
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
        private val binding: ItemAdminCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: AdminCourse) {
            binding.apply {
                // Course basic info
                textViewCourseTitle.text = "📚 ${course.title}"
                textViewCourseDescription.text = course.description
                textViewTeacherName.text = "👨‍🏫 ${course.teacherName}"
                textViewCourseLevel.text = "📅 Créé: ${course.createdAt}"
                textViewCourseDuration.text = "🆔 ID: ${course.id}"
                
                // Enrollment info with enhanced display
                val enrollmentText = when {
                    course.enrollmentCount == 0 -> "👥 Aucune inscription"
                    course.enrollmentCount == 1 -> "👤 1 étudiant inscrit"
                    else -> "👥 ${course.enrollmentCount} étudiants inscrits"
                }
                textViewEnrollmentInfo.text = enrollmentText
                
                // Calculate enrollment percentage (assuming max 50 students per course)
                val maxEnrollments = 50
                val enrollmentPercentage = if (course.enrollmentCount > 0) {
                    minOf(100, (course.enrollmentCount * 100) / maxEnrollments)
                } else 0
                progressBarEnrollment.progress = enrollmentPercentage
                textViewEnrollmentPercentage.text = "$enrollmentPercentage%"
                
                // Enhanced status display
                val (statusText, statusColor) = when (course.status) {
                    "active" -> "✅ Actif" to android.R.color.holo_green_dark
                    "inactive" -> "❌ Inactif" to android.R.color.holo_red_dark
                    "pending" -> "⏳ En attente" to android.R.color.holo_orange_dark
                    "draft" -> "📝 Brouillon" to android.R.color.darker_gray
                    else -> "❓ Inconnu" to android.R.color.darker_gray
                }
                    
                textViewCourseStatus.text = statusText
                textViewCourseStatus.setTextColor(
                    binding.root.context.getColor(statusColor)
                )
                
                // Status switch with proper handling
                val isActive = course.status == "active"
                switchCourseStatus.setOnCheckedChangeListener(null)
                switchCourseStatus.isChecked = isActive
                
                switchCourseStatus.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != isActive) {
                        onToggleCourseStatus(course)
                    }
                }
                
                // Enhanced click listeners
                buttonViewCourse.setOnClickListener { 
                    android.util.Log.d("AdminCourse", "View button clicked for course: ${course.title}")
                    onViewCourse(course)
                }
                
                buttonEditCourse.setOnClickListener { 
                    android.util.Log.d("AdminCourse", "Edit button clicked for course: ${course.title}")
                    onEditCourse(course)
                }
                
                buttonDeleteCourse.setOnClickListener { 
                    android.util.Log.d("AdminCourse", "Delete button clicked for course: ${course.title}")
                    onDeleteCourse(course)
                }
                
                // Additional functionality buttons (if they exist in layout)
                try {
                    val buttonManageEnrollments = binding.root.findViewById<android.widget.Button>(
                        binding.root.context.resources.getIdentifier("buttonManageEnrollments", "id", binding.root.context.packageName)
                    )
                    buttonManageEnrollments?.setOnClickListener {
                        onManageEnrollments(course)
                    }
                } catch (e: Exception) {
                    // Button doesn't exist in layout
                }
                
                try {
                    val buttonViewStats = binding.root.findViewById<android.widget.Button>(
                        binding.root.context.resources.getIdentifier("buttonViewStats", "id", binding.root.context.packageName)
                    )
                    buttonViewStats?.setOnClickListener {
                        onViewStatistics(course)
                    }
                } catch (e: Exception) {
                    // Button doesn't exist in layout
                }
                
                // Card click for quick view
                root.setOnClickListener { 
                    onViewCourse(course)
                }
            }
        }
    }

    private class CourseDiffCallback : DiffUtil.ItemCallback<AdminCourse>() {
        override fun areItemsTheSame(oldItem: AdminCourse, newItem: AdminCourse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AdminCourse, newItem: AdminCourse): Boolean {
            return oldItem == newItem
        }
    }
}