package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemAdminQuizEnhancedBinding
import com.edunova.mobile.data.repository.AdminQuiz

class AdminQuizzesEnhancedAdapter(
    private val onViewQuiz: (AdminQuiz) -> Unit,
    private val onEditQuiz: (AdminQuiz) -> Unit,
    private val onDeleteQuiz: (AdminQuiz) -> Unit,
    private val onToggleQuizStatus: (AdminQuiz) -> Unit,
    private val onActivateQuiz: (AdminQuiz) -> Unit,
    private val onDeactivateQuiz: (AdminQuiz) -> Unit
) : ListAdapter<AdminQuiz, AdminQuizzesEnhancedAdapter.QuizViewHolder>(QuizDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemAdminQuizEnhancedBinding.inflate(
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
        private val binding: ItemAdminQuizEnhancedBinding
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
                
                // Informations supplémentaires dans la description
                val additionalInfo = """
                    ${quiz.description}
                    
                    📊 STATISTIQUES:
                    • ${quiz.totalSubmissions} soumissions totales
                    • ${quiz.uniqueStudents} étudiants uniques
                    • Score moyen: ${quiz.averageScore}%
                    • ${quiz.questionCount} questions
                    
                    ℹ️ INFORMATIONS:
                    • 👨‍🏫 Enseignant: ${quiz.teacherName}
                    • 📅 Créé le: ${quiz.createdAt}
                    • Temps limite: ${quiz.timeLimit} minutes
                    • Tentatives autorisées: ${quiz.maxAttempts}
                    • Score de passage: ${quiz.passingScore}%
                """.trimIndent()
                
                textViewQuizDescription.text = additionalInfo
                
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
                
                // Configurer les contrôles de statut
                val isActive = quiz.status.lowercase() == "active"
                
                // Switch principal
                switchQuizStatus.setOnCheckedChangeListener(null)
                switchQuizStatus.isChecked = isActive
                
                // Checkboxes avec logique exclusive
                checkBoxActive.setOnCheckedChangeListener(null)
                checkBoxInactive.setOnCheckedChangeListener(null)
                
                checkBoxActive.isChecked = isActive
                checkBoxInactive.isChecked = !isActive
                
                // Boutons rapides
                buttonQuickActivate.isEnabled = !isActive
                buttonQuickDeactivate.isEnabled = isActive
                
                // Listeners pour les checkboxes (logique exclusive)
                checkBoxActive.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && !isActive) {
                        // Activer le quiz
                        checkBoxInactive.setOnCheckedChangeListener(null)
                        checkBoxInactive.isChecked = false
                        checkBoxInactive.setOnCheckedChangeListener { _, checked ->
                            if (checked && isActive) {
                                checkBoxActive.setOnCheckedChangeListener(null)
                                checkBoxActive.isChecked = false
                                checkBoxActive.setOnCheckedChangeListener { _, c -> 
                                    if (c && !isActive) {
                                        checkBoxInactive.isChecked = false
                                        onActivateQuiz(quiz)
                                    }
                                }
                                onDeactivateQuiz(quiz)
                            }
                        }
                        onActivateQuiz(quiz)
                    } else if (!isChecked && isActive) {
                        // Si on décoche "Actif" alors que le quiz est actif, cocher "Inactif"
                        checkBoxInactive.setOnCheckedChangeListener(null)
                        checkBoxInactive.isChecked = true
                        checkBoxInactive.setOnCheckedChangeListener { _, checked ->
                            if (checked && isActive) {
                                checkBoxActive.setOnCheckedChangeListener(null)
                                checkBoxActive.isChecked = false
                                checkBoxActive.setOnCheckedChangeListener { _, c -> 
                                    if (c && !isActive) {
                                        checkBoxInactive.isChecked = false
                                        onActivateQuiz(quiz)
                                    }
                                }
                                onDeactivateQuiz(quiz)
                            }
                        }
                        onDeactivateQuiz(quiz)
                    }
                }
                
                checkBoxInactive.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && isActive) {
                        // Désactiver le quiz
                        checkBoxActive.setOnCheckedChangeListener(null)
                        checkBoxActive.isChecked = false
                        checkBoxActive.setOnCheckedChangeListener { _, checked ->
                            if (checked && !isActive) {
                                checkBoxInactive.setOnCheckedChangeListener(null)
                                checkBoxInactive.isChecked = false
                                checkBoxInactive.setOnCheckedChangeListener { _, c -> 
                                    if (c && isActive) {
                                        checkBoxActive.isChecked = false
                                        onDeactivateQuiz(quiz)
                                    }
                                }
                                onActivateQuiz(quiz)
                            }
                        }
                        onDeactivateQuiz(quiz)
                    } else if (!isChecked && !isActive) {
                        // Si on décoche "Inactif" alors que le quiz est inactif, cocher "Actif"
                        checkBoxActive.setOnCheckedChangeListener(null)
                        checkBoxActive.isChecked = true
                        checkBoxActive.setOnCheckedChangeListener { _, checked ->
                            if (checked && !isActive) {
                                checkBoxInactive.setOnCheckedChangeListener(null)
                                checkBoxInactive.isChecked = false
                                checkBoxInactive.setOnCheckedChangeListener { _, c -> 
                                    if (c && isActive) {
                                        checkBoxActive.isChecked = false
                                        onDeactivateQuiz(quiz)
                                    }
                                }
                                onActivateQuiz(quiz)
                            }
                        }
                        onActivateQuiz(quiz)
                    }
                }
                
                // Switch toggle
                switchQuizStatus.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != isActive) {
                        // Mettre à jour les checkboxes
                        checkBoxActive.setOnCheckedChangeListener(null)
                        checkBoxInactive.setOnCheckedChangeListener(null)
                        
                        checkBoxActive.isChecked = isChecked
                        checkBoxInactive.isChecked = !isChecked
                        
                        // Remettre les listeners
                        setupCheckboxListeners(quiz, isActive)
                        
                        onToggleQuizStatus(quiz)
                    }
                }
                
                // Boutons rapides
                buttonQuickActivate.setOnClickListener {
                    if (!isActive) {
                        onActivateQuiz(quiz)
                    }
                }
                
                buttonQuickDeactivate.setOnClickListener {
                    if (isActive) {
                        onDeactivateQuiz(quiz)
                    }
                }
                
                // Click listeners pour les actions principales
                buttonViewQuiz.setOnClickListener { onViewQuiz(quiz) }
                buttonEditQuiz.setOnClickListener { onEditQuiz(quiz) }
                buttonDeleteQuiz.setOnClickListener { onDeleteQuiz(quiz) }
                
                // Card click for details
                root.setOnClickListener { onViewQuiz(quiz) }
            }
        }
        
        private fun ItemAdminQuizEnhancedBinding.setupCheckboxListeners(quiz: AdminQuiz, isActive: Boolean) {
            checkBoxActive.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !isActive) {
                    checkBoxInactive.isChecked = false
                    onActivateQuiz(quiz)
                } else if (!isChecked && isActive) {
                    checkBoxInactive.isChecked = true
                    onDeactivateQuiz(quiz)
                }
            }
            
            checkBoxInactive.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && isActive) {
                    checkBoxActive.isChecked = false
                    onDeactivateQuiz(quiz)
                } else if (!isChecked && !isActive) {
                    checkBoxActive.isChecked = true
                    onActivateQuiz(quiz)
                }
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