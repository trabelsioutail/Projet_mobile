package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.databinding.ItemTakeQuizQuestionBinding
import com.edunova.mobile.domain.model.QuizQuestion

class TakeQuizQuestionAdapter(
    private val onAnswerSelected: (questionId: Int, selectedOptionId: String) -> Unit
) : ListAdapter<QuizQuestion, TakeQuizQuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemTakeQuizQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuestionViewHolder(
        private val binding: ItemTakeQuizQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: QuizQuestion) {
            binding.apply {
                textViewQuestionNumber.text = "Question ${adapterPosition + 1}"
                textViewPoints.text = "${question.points} points"
                textViewQuestionText.text = question.questionText

                // Effacer les options précédentes
                radioGroupOptions.removeAllViews()

                // Ajouter les nouvelles options
                question.options.forEach { option ->
                    val radioButton = RadioButton(binding.root.context).apply {
                        id = option.id.hashCode()
                        text = option.text
                        textSize = 14f
                        setPadding(16, 12, 16, 12)
                        
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                onAnswerSelected(question.id, option.id)
                            }
                        }
                    }
                    radioGroupOptions.addView(radioButton)
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