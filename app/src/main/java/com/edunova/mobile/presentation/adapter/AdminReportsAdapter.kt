package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemAdminReportBinding
import com.edunova.mobile.data.repository.DailyActivity

class AdminReportsAdapter(
    private val onViewReport: (DailyActivity) -> Unit
) : ListAdapter<DailyActivity, AdminReportsAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemAdminReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(
        private val binding: ItemAdminReportBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: DailyActivity) {
            binding.apply {
                textViewReportTitle.text = "Activité du ${activity.date}"
                textViewReportType.text = "Rapport d'activité"
                textViewReportDate.text = "Date: ${activity.date}"
                textViewReportDescription.text = "${activity.count} activités enregistrées"
                
                // Type icon
                imageViewReportIcon.setImageResource(com.edunova.mobile.R.drawable.ic_analytics)
                
                // Click listeners
                buttonViewReport.setOnClickListener { onViewReport(activity) }
                buttonDownloadReport.setOnClickListener { onViewReport(activity) }
                
                // Card click for view
                root.setOnClickListener { onViewReport(activity) }
            }
        }
    }

    private class ReportDiffCallback : DiffUtil.ItemCallback<DailyActivity>() {
        override fun areItemsTheSame(oldItem: DailyActivity, newItem: DailyActivity): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DailyActivity, newItem: DailyActivity): Boolean {
            return oldItem == newItem
        }
    }
}