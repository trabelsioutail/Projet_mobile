package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.data.repository.AdminUser
import com.edunova.mobile.databinding.ItemAdminUserBinding

class AdminUsersAdapter(
    private val onEditUser: (AdminUser) -> Unit,
    private val onDeleteUser: (AdminUser) -> Unit
) : ListAdapter<AdminUser, AdminUsersAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemAdminUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemAdminUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: AdminUser) {
            binding.apply {
                textViewUserName.text = "${user.firstName} ${user.lastName}"
                textViewUserEmail.text = user.email
                textViewUserRole.text = when (user.role) {
                    "student" -> "Étudiant"
                    "teacher" -> "Enseignant"
                    "admin" -> "Administrateur"
                    else -> user.role
                }
                
                // Status indicator - assume active for now
                textViewUserStatus.text = "Actif"
                textViewUserStatus.setTextColor(
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                )
                
                // Verification status - assume verified for now
                textViewUserVerification.text = "Vérifié"
                
                // Role icon
                val roleIcon = when (user.role) {
                    "student" -> com.edunova.mobile.R.drawable.ic_student
                    "teacher" -> com.edunova.mobile.R.drawable.ic_teacher
                    "admin" -> com.edunova.mobile.R.drawable.ic_admin
                    else -> com.edunova.mobile.R.drawable.ic_people
                }
                imageViewRoleIcon.setImageResource(roleIcon)
                
                // Click listeners
                buttonEditUser.setOnClickListener { onEditUser(user) }
                buttonDeleteUser.setOnClickListener { onDeleteUser(user) }
                switchUserStatus.isChecked = true // Assume active for now
                
                // Date info
                textViewLastLogin.text = "Récemment"
                textViewCreatedAt.text = "Créé le: ${user.createdAt}"
            }
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<AdminUser>() {
        override fun areItemsTheSame(oldItem: AdminUser, newItem: AdminUser): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AdminUser, newItem: AdminUser): Boolean {
            return oldItem == newItem
        }
    }
}