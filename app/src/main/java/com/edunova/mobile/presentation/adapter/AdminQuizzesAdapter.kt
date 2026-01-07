package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemAdminQuizBinding
import com.edunova.mobile.data.repository.AdminQuiz

class AdminQuizzesAdapter(
    private val onViewQuiz: (AdminQuiz) -> Unit,
    private val onEditQuiz: (AdminQuiz) -> Unit,
    private val onDeleteQuiz: (AdminQuiz) -> Unit,
    private val onToggleQuizStatus: (AdminQuiz) -> Unit
) : ListAdapter<AdminQuiz, AdminQuizzesAdapter.QuizViewHolder>(QuizDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemAdminQuizBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuizViewHolder(
        private val binding: ItemAdminQuizBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(quiz: AdminQuiz) {
            binding.apply {
                // Informations principales avec emojis
                textViewQuizTitle.text = "📝 ${quiz.title}"
                textViewQuizDescription.text = quiz.description.ifEmpty { "Aucune description disponible" }
                textViewCourseName.text = "📚 Cours: ${quiz.courseTitle}"
                
                // Statistiques détaillées
                textViewQuestionCount.text = "📊 ${quiz.totalSubmissions} soumissions • ${quiz.uniqueStudents} étudiants"
                textViewTimeLimit.text = "⏱️ ${quiz.timeLimit} minutes"
                textViewMaxAttempts.text = "🔄 ${quiz.maxAttempts} tentatives max"
                textViewPassingScore.text = "🎯 ${quiz.passingScore}% requis pour réussir"
                
                // Informations supplémentaires
                val additionalInfo = "👥 ${quiz.uniqueStudents} étudiants uniques • 📈 Score moyen: ${quiz.averageScore}% • ❓ ${quiz.questionCount} questions"
                textViewQuizDescription.text = "${quiz.description}\n\n$additionalInfo"
                
                // Status avec couleurs et emojis basé sur le champ status
                val (statusText, statusColor, statusEmoji) = when (quiz.status.lowercase()) {
                    "active" -> Triple("Actif", android.R.color.holo_green_dark, "✅")
                    "inactive" -> Triple("Inactif", android.R.color.holo_red_dark, "❌")
                    "draft" -> Triple("Brouillon", android.R.color.holo_orange_dark, "📝")
                    else -> Triple("Inconnu", android.R.color.darker_gray, "❓")
                }
                    
                textViewQuizStatus.text = "$statusEmoji $statusText"
                textViewQuizStatus.setTextColor(
                    binding.root.context.getColor(statusColor)
                )
                
                // Enseignant et date de création
                val teacherInfo = "👨‍🏫 Enseignant: ${quiz.teacherName}"
                val createdInfo = "📅 Créé le: ${quiz.createdAt}"
                
                // Ajouter ces informations à la description
                textViewQuizDescription.text = """
                    ${quiz.description}
                    
                    📊 STATISTIQUES:
                    • ${quiz.totalSubmissions} soumissions totales
                    • ${quiz.uniqueStudents} étudiants uniques
                    • Score moyen: ${quiz.averageScore}%
                    • ${quiz.questionCount} questions
                    
                    ℹ️ INFORMATIONS:
                    • $teacherInfo
                    • $createdInfo
                    • Temps limite: ${quiz.timeLimit} minutes
                    • Tentatives autorisées: ${quiz.maxAttempts}
                    • Score de passage: ${quiz.passingScore}%
                """.trimIndent()
                
                // Status switch basé sur le champ status
                val isActive = quiz.status.lowercase() == "active"
                
                // Configurer le switch principal
                switchQuizStatus.setOnCheckedChangeListener(null)
                switchQuizStatus.isChecked = isActive
                
                // Configurer les checkboxes
                checkBoxActive.setOnCheckedChangeListener(null)
                checkBoxInactive.setOnCheckedChangeListener(null)
                
                checkBoxActive.isChecked = isActive
                checkBoxInactive.isChecked = !isActive
                
                // Listener simple pour le switch
                switchQuizStatus.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != isActive) {
                        onToggleQuizStatus(quiz)
                    }
                }
                
                // Listeners simples pour les checkboxes
                checkBoxActive.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && !isActive) {
                        // Activer le quiz
                        onToggleQuizStatus(quiz)
                    } else if (!isChecked && isActive) {
                        // Désactiver le quiz (décocher actif = cocher inactif)
                        onToggleQuizStatus(quiz)
                    }
                }
                
                checkBoxInactive.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && isActive) {
                        // Désactiver le quiz
                        onToggleQuizStatus(quiz)
                    } else if (!isChecked && !isActive) {
                        // Activer le quiz (décocher inactif = cocher actif)
                        onToggleQuizStatus(quiz)
                    }
                }
                
                // Click listeners
                buttonViewQuiz.setOnClickListener { onViewQuiz(quiz) }
                buttonEditQuiz.setOnClickListener { onEditQuiz(quiz) }
                buttonDeleteQuiz.setOnClickListener { onDeleteQuiz(quiz) }
                
                // Card click for details
                root.setOnClickListener { onViewQuiz(quiz) }
            }
        }
    }

    private class QuizDiffCallback : DiffUtil.ItemCallback<AdminQuiz>() {
        override fun areItemsTheSame(oldItem: AdminQuiz, newItem: AdminQuiz): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AdminQuiz, newItem: AdminQuiz): Boolean {
            return oldItem == newItem
        }
    }
}