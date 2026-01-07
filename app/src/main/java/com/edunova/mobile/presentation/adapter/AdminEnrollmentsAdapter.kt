package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.R
import com.edunova.mobile.data.repository.AdminEnrollment

class AdminEnrollmentsAdapter(
    private val onApproveEnrollment: (AdminEnrollment) -> Unit,
    private val onRejectEnrollment: (AdminEnrollment) -> Unit,
    private val onPendingEnrollment: (AdminEnrollment) -> Unit,
    private val onRemoveEnrollment: (AdminEnrollment) -> Unit,
    private val onViewDetails: (AdminEnrollment) -> Unit,
    private val onSendMessage: (AdminEnrollment) -> Unit
) : ListAdapter<AdminEnrollment, AdminEnrollmentsAdapter.EnrollmentViewHolder>(EnrollmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrollmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_enrollment, parent, false)
        return EnrollmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: EnrollmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EnrollmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val studentNameText: TextView = itemView.findViewById(R.id.textViewStudentName)
        private val courseNameText: TextView = itemView.findViewById(R.id.textViewCourseName)
        private val statusText: TextView = itemView.findViewById(R.id.textViewStatus)
        private val enrollmentDateText: TextView = itemView.findViewById(R.id.textViewEnrollmentDate)
        private val approveButton: View = itemView.findViewById(R.id.buttonApprove)
        private val rejectButton: View = itemView.findViewById(R.id.buttonReject)
        private val pendingButton: View = itemView.findViewById(R.id.buttonPending)

        fun bind(enrollment: AdminEnrollment) {
            // Vérifications de sécurité pour éviter les paramètres null
            val studentName = enrollment.studentName?.takeIf { it.isNotBlank() } ?: "Étudiant inconnu"
            val courseTitle = enrollment.courseTitle?.takeIf { it.isNotBlank() } ?: "Cours inconnu"
            val status = enrollment.status?.takeIf { it.isNotBlank() } ?: "pending"
            val enrolledAt = enrollment.enrolledAt?.takeIf { it.isNotBlank() } ?: "Date inconnue"
            
            // Affichage des informations avec emojis
            studentNameText.text = "👤 $studentName"
            courseNameText.text = "📚 $courseTitle"
            enrollmentDateText.text = "📅 Inscrit le: $enrolledAt"
            
            // Affichage du statut avec couleurs et emojis
            val (statusDisplayText, statusColor) = when(status.lowercase()) {
                "active", "approved", "enrolled" -> "✅ Actif" to android.R.color.holo_green_dark
                "inactive", "rejected" -> "❌ Rejeté" to android.R.color.holo_red_dark
                "pending" -> "⏳ En attente" to android.R.color.holo_orange_dark
                "completed" -> "🎓 Terminé" to android.R.color.holo_blue_dark
                "suspended" -> "⏸️ Suspendu" to android.R.color.darker_gray
                else -> "📊 $status" to android.R.color.black
            }
            
            statusText.text = statusDisplayText
            statusText.setTextColor(itemView.context.getColor(statusColor))

            // Configuration des boutons selon le statut
            setupButtonsForStatus(enrollment, status)
            
            // Click listeners pour les actions
            approveButton.setOnClickListener {
                onApproveEnrollment(enrollment)
            }
            
            rejectButton.setOnClickListener {
                onRejectEnrollment(enrollment)
            }
            
            pendingButton.setOnClickListener {
                onPendingEnrollment(enrollment)
            }
            
            // Click sur la carte pour voir les détails
            itemView.setOnClickListener {
                onViewDetails(enrollment)
            }
            
            // Long click pour envoyer un message
            itemView.setOnLongClickListener {
                onSendMessage(enrollment)
                true
            }
        }
        
        private fun setupButtonsForStatus(enrollment: AdminEnrollment, status: String) {
            when(status.lowercase()) {
                "pending" -> {
                    // En attente : montrer Approuver et Rejeter
                    approveButton.visibility = View.VISIBLE
                    rejectButton.visibility = View.VISIBLE
                    pendingButton.visibility = View.GONE
                    
                    // Changer les icônes/textes des boutons
                    (approveButton as? TextView)?.text = "✅"
                    (rejectButton as? TextView)?.text = "❌"
                }
                "active", "approved", "enrolled" -> {
                    // Actif : montrer Suspendre et Mettre en attente
                    approveButton.visibility = View.GONE
                    rejectButton.visibility = View.VISIBLE
                    pendingButton.visibility = View.VISIBLE
                    
                    (rejectButton as? TextView)?.text = "❌"
                    (pendingButton as? TextView)?.text = "📋"
                }
                "inactive", "rejected" -> {
                    // Rejeté : montrer Réactiver
                    approveButton.visibility = View.VISIBLE
                    rejectButton.visibility = View.GONE
                    pendingButton.visibility = View.VISIBLE
                    
                    (approveButton as? TextView)?.text = "✅"
                    (pendingButton as? TextView)?.text = "📋"
                }
                else -> {
                    // Autres statuts : montrer toutes les options
                    approveButton.visibility = View.VISIBLE
                    rejectButton.visibility = View.VISIBLE
                    pendingButton.visibility = View.VISIBLE
                    
                    (approveButton as? TextView)?.text = "✅"
                    (rejectButton as? TextView)?.text = "❌"
                    (pendingButton as? TextView)?.text = "📋"
                }
            }
        }
    }

    class EnrollmentDiffCallback : DiffUtil.ItemCallback<AdminEnrollment>() {
        override fun areItemsTheSame(oldItem: AdminEnrollment, newItem: AdminEnrollment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AdminEnrollment, newItem: AdminEnrollment): Boolean {
            return oldItem == newItem
        }
    }
}