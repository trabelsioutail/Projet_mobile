package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemQuizBinding
import com.edunova.mobile.domain.model.Quiz
import com.edunova.mobile.domain.model.QuizSubmission
import com.edunova.mobile.utils.gone
import com.edunova.mobile.utils.visible

class QuizAdapter(
    private val onQuizClick: (Quiz) -> Unit,
    private val onEditClick: ((Quiz) -> Unit)? = null,
    private val onDeleteClick: ((Quiz) -> Unit)? = null,
    private val onStartQuizClick: ((Quiz) -> Unit)? = null,
    private val isStudentView: Boolean = false
) : ListAdapter<Quiz, QuizAdapter.QuizViewHolder>(QuizDiffCallback()) {

    private var submissions: List<QuizSubmission> = emptyList()

    fun updateSubmissions(newSubmissions: List<QuizSubmission>) {
        submissions = newSubmissions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizBinding.inflate(
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
        private val binding: ItemQuizBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(quiz: Quiz) {
            binding.apply {
                textViewTitle.text = quiz.title
                textViewDescription.text = quiz.description ?: "Aucune description"
                textViewQuestionCount.text = "${quiz.questions.size} questions"
                textViewDuration.text = if (quiz.timeLimit != null) "${quiz.timeLimit} min" else "Pas de limite"
                
                if (isStudentView) {
                    setupStudentView(quiz)
                } else {
                    setupTeacherView(quiz)
                }
                
                root.setOnClickListener {
                    onQuizClick(quiz)
                }
            }
        }

        private fun setupStudentView(quiz: Quiz) {
            binding.apply {
                // Masquer les boutons d'édition/suppression
                buttonEdit?.gone()
                buttonDelete?.gone()
                
                // Vérifier si l'étudiant a déjà soumis ce quiz
                val submission = submissions.find { it.quizId == quiz.id }
                
                if (submission != null) {
                    // Quiz déjà complété
                    textViewStatus?.visible()
                    textViewStatus?.text = "Complété - Score: ${submission.score.toInt()}/${quiz.totalPoints}"
                    buttonStartQuiz?.text = "Voir les résultats"
                } else {
                    // Quiz pas encore fait
                    textViewStatus?.visible()
                    textViewStatus?.text = "Non complété"
                    buttonStartQuiz?.text = "Commencer le quiz"
                }
                
                buttonStartQuiz?.visible()
                buttonStartQuiz?.setOnClickListener {
                    onStartQuizClick?.invoke(quiz)
                }
            }
        }

        private fun setupTeacherView(quiz: Quiz) {
            binding.apply {
                // Masquer les éléments spécifiques aux étudiants
                textViewStatus?.gone()
                buttonStartQuiz?.gone()
                
                // Afficher les boutons d'action pour les enseignants
                onEditClick?.let { editCallback ->
                    buttonEdit?.visible()
                    buttonEdit?.setOnClickListener {
                        editCallback(quiz)
                    }
                }
                
                onDeleteClick?.let { deleteCallback ->
                    buttonDelete?.visible()
                    buttonDelete?.setOnClickListener {
                        deleteCallback(quiz)
                    }
                }
            }
        }
    }

    private class QuizDiffCallback : DiffUtil.ItemCallback<Quiz>() {
        override fun areItemsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
            return oldItem == newItem
        }
    }
}