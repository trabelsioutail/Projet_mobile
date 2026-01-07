package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemQuizQuestionEditBinding
import com.edunova.mobile.domain.model.QuizQuestion

class QuizQuestionAdapter(
    private val onEditQuestion: (QuizQuestion, Int) -> Unit,
    private val onDeleteQuestion: (Int) -> Unit
) : ListAdapter<QuizQuestion, QuizQuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuizQuestionEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class QuestionViewHolder(
        private val binding: ItemQuizQuestionEditBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: QuizQuestion, position: Int) {
            binding.apply {
                tvQuestionNumber.text = "Question ${position + 1}"
                tvQuestionText.text = question.questionText
                tvQuestionType.text = question.questionType.displayName
                tvPoints.text = "${question.points} point(s)"
                
                // Afficher les options si c'est un choix multiple
                if (question.options.isNotEmpty()) {
                    tvOptions.text = question.options.joinToString("\n") { "• $it" }
                    tvOptions.visibility = android.view.View.VISIBLE
                } else {
                    tvOptions.visibility = android.view.View.GONE
                }
                
                // Afficher la bonne réponse
                tvCorrectAnswer.text = "Réponse: ${question.correctAnswer ?: "Non définie"}"
                
                btnEditQuestion.setOnClickListener {
                    onEditQuestion(question, position)
                }
                
                btnDeleteQuestion.setOnClickListener {
                    onDeleteQuestion(position)
                }
            }
        }
    }

    private class QuestionDiffCallback : DiffUtil.ItemCallback<QuizQuestion>() {
        override fun areItemsTheSame(oldItem: QuizQuestion, newItem: QuizQuestion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: QuizQuestion, newItem: QuizQuestion): Boolean {
            return oldItem == newItem
        }
    }
}