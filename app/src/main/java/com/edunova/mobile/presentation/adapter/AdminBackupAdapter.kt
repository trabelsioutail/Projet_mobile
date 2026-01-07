package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemAdminBackupBinding
import com.edunova.mobile.data.repository.AdminBackup

class AdminBackupAdapter(
    private val onRestoreBackup: (AdminBackup) -> Unit,
    private val onDeleteBackup: (AdminBackup) -> Unit
) : ListAdapter<AdminBackup, AdminBackupAdapter.BackupViewHolder>(BackupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        val binding = ItemAdminBackupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BackupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BackupViewHolder(
        private val binding: ItemAdminBackupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(backup: AdminBackup) {
            binding.apply {
                textViewBackupDate.text = backup.createdAt
                textViewBackupSize.text = backup.size
                textViewBackupType.text = backup.status
                
                // Type icon
                val typeIcon = when (backup.status.lowercase()) {
                    "completed" -> com.edunova.mobile.R.drawable.ic_backup
                                    "in_progress" -> com.edunova.mobile.R.drawable.ic_schedule
                    else -> com.edunova.mobile.R.drawable.ic_backup
                }
                imageViewBackupIcon.setImageResource(typeIcon)
                
                // Type color
                val typeColor = when (backup.status.lowercase()) {
                    "completed" -> android.R.color.holo_green_dark
                    "in_progress" -> android.R.color.holo_blue_dark
                    else -> android.R.color.darker_gray
                }
                textViewBackupType.setTextColor(
                    binding.root.context.getColor(typeColor)
                )
                
                // Click listeners
                buttonRestoreBackup.setOnClickListener { onRestoreBackup(backup) }
                buttonDeleteBackup.setOnClickListener { onDeleteBackup(backup) }
            }
        }
    }

    private class BackupDiffCallback : DiffUtil.ItemCallback<AdminBackup>() {
        override fun areItemsTheSame(oldItem: AdminBackup, newItem: AdminBackup): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AdminBackup, newItem: AdminBackup): Boolean {
            return oldItem == newItem
        }
    }
}