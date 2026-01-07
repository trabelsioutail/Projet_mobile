package com.edunova.mobile.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edunova.mobile.R
import com.edunova.mobile.databinding.ItemQuizQuestionDetailBinding
import com.edunova.mobile.domain.model.QuizQuestion
import com.edunova.mobile.domain.model.QuestionType

class QuizQuestionDetailAdapter(
    private val onEditQuestion: (QuizQuestion, Int) -> Unit,
    private val onDeleteQuestion: (QuizQuestion, Int) -> Unit
) : ListAdapter<QuizQuestion, QuizQuestionDetailAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuizQuestionDetailBinding.inflate(
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
        private val binding: ItemQuizQuestionDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: QuizQuestion, position: Int) {
            binding.apply {
                tvQuestionNumber.text = (position + 1).toString()
                tvQuestionType.text = question.questionType.displayName
                tvQuestionPoints.text = "${question.points} pts"
                tvQuestionText.text = question.questionText
                
                // Afficher les options selon le type de question
                setupOptions(question)
                
                // Afficher la bonne réponse
                tvCorrectAnswer.text = question.correctAnswer ?: "Non définie"
                
                // Gérer la visibilité de la bonne réponse
                layoutCorrectAnswer.visibility = 
                    if (question.correctAnswer != null) View.VISIBLE else View.GONE
                
                // Click listeners
                btnEditQuestion.setOnClickListener {
                    onEditQuestion(question, position)
                }
                
                btnDeleteQuestion.setOnClickListener {
                    onDeleteQuestion(question, position)
                }
            }
        }
        
        private fun setupOptions(question: QuizQuestion) {
            binding.layoutOptions.removeAllViews()
            
            when (question.questionType) {
                QuestionType.MULTIPLE_CHOICE -> {
                    question.options.forEachIndexed { index, option ->
                        val optionView = createOptionView(option.text, index + 1, question.correctAnswer == option.id)
                        binding.layoutOptions.addView(optionView)
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    val trueOption = createOptionView("Vrai", 1, question.correctAnswer == "Vrai")
                    val falseOption = createOptionView("Faux", 2, question.correctAnswer == "Faux")
                    binding.layoutOptions.addView(trueOption)
                    binding.layoutOptions.addView(falseOption)
                }
                QuestionType.SHORT_ANSWER, QuestionType.ESSAY -> {
                    val answerView = createAnswerView("Réponse libre attendue")
                    binding.layoutOptions.addView(answerView)
                }
            }
        }
        
        private fun createOptionView(text: String, number: Int, isCorrect: Boolean): View {
            val context = binding.root.context
            val optionView = LayoutInflater.from(context).inflate(
                R.layout.item_question_option, 
                binding.layoutOptions, 
                false
            )
            
            val tvOptionNumber = optionView.findViewById<TextView>(R.id.tvOptionNumber)
            val tvOptionText = optionView.findViewById<TextView>(R.id.tvOptionText)
            
            tvOptionNumber.text = number.toString()
            tvOptionText.text = text
            
            // Mettre en évidence la bonne réponse
            if (isCorrect) {
                optionView.setBackgroundResource(R.drawable.rounded_background_success_light)
                tvOptionNumber.setBackgroundResource(R.drawable.circle_success)
                tvOptionText.setTextColor(context.getColor(R.color.success))
            } else {
                optionView.setBackgroundResource(R.drawable.rounded_background_light)
                tvOptionNumber.setBackgroundResource(R.drawable.circle_light)
                tvOptionText.setTextColor(context.getColor(R.color.text_primary))
            }
            
            return optionView
        }
        
        private fun createAnswerView(text: String): View {
            val context = binding.root.context
            val answerView = LayoutInflater.from(context).inflate(
                R.layout.item_question_answer, 
                binding.layoutOptions, 
                false
            )
            
            val tvAnswerText = answerView.findViewById<TextView>(R.id.tvAnswerText)
            tvAnswerText.text = text
            
            return answerView
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