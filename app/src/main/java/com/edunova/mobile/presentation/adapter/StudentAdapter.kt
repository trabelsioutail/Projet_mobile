package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemStudentBinding
import com.edunova.mobile.domain.model.User

class StudentAdapter(
    private val onStudentClick: (User) -> Unit
) : ListAdapter<User, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudentViewHolder(
        private val binding: ItemStudentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: User) {
            binding.apply {
                tvStudentName.text = "${student.firstName} ${student.lastName}"
                tvStudentEmail.text = student.email
                
                // Avatar avec initiales
                val initials = "${student.firstName.firstOrNull() ?: 'U'}${student.lastName.firstOrNull() ?: 'S'}"
                tvStudentInitials.text = initials
                
                // Couleur de l'avatar basée sur le nom
                val colors = listOf(
                    com.edunova.mobile.R.color.primary_color,
                    com.edunova.mobile.R.color.success_color,
                    com.edunova.mobile.R.color.warning_color,
                    com.edunova.mobile.R.color.info_color,
                    com.edunova.mobile.R.color.error_color
                )
                val colorIndex = (student.firstName + student.lastName).hashCode() % colors.size
                val colorRes = colors[kotlin.math.abs(colorIndex)]
                avatarBackground.setCardBackgroundColor(itemView.context.getColor(colorRes))
                
                root.setOnClickListener {
                    onStudentClick(student)
                }
                
                btnMessage.setOnClickListener {
                    // TODO: Ouvrir conversation avec l'étudiant
                }
                
                btnViewProgress.setOnClickListener {
                    // TODO: Voir le progrès de l'étudiant
                }
            }
        }
    }

    private class StudentDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}