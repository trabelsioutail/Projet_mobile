package com.edunova.mobile.presentation.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.databinding.FragmentQuizDetailBinding
import com.edunova.mobile.domain.model.Quiz
import com.edunova.mobile.domain.model.QuizQuestion
import com.edunova.mobile.presentation.adapter.QuizQuestionDetailAdapter
import com.edunova.mobile.presentation.viewmodel.QuizViewModel
import com.edunova.mobile.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuizDetailFragment : Fragment() {
    
    private var _binding: FragmentQuizDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: QuizDetailFragmentArgs by navArgs()
    private val quizViewModel: QuizViewModel by viewModels()
    
    private lateinit var questionAdapter: QuizQuestionDetailAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // Charger les détails du quiz
        quizViewModel.loadQuizDetails(args.quizId)
    }
    
    private fun setupRecyclerView() {
        questionAdapter = QuizQuestionDetailAdapter(
            onEditQuestion = { question, position ->
                editQuestion(question, position)
            },
            onDeleteQuestion = { question, position ->
                deleteQuestion(question, position)
            }
        )
        
        binding.rvQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = questionAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            btnEditQuiz.setOnClickListener {
                // TODO: Navigation vers l'édition du quiz
                showMessage("Édition du quiz en développement")
            }
            
            btnPublishQuiz.setOnClickListener {
                publishQuiz()
            }
            
            btnDeleteQuiz.setOnClickListener {
                confirmDeleteQuiz()
            }
            
            btnAddQuestion.setOnClickListener {
                addNewQuestion()
            }
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            quizViewModel.selectedQuiz.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // TODO: Afficher le loading
                    }
                    is Resource.Success -> {
                        resource.data?.let { quiz ->
                            updateUI(quiz)
                        }
                    }
                    is Resource.Error -> {
                        showMessage("Erreur: ${resource.message}")
                    }
                    null -> {}
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            quizViewModel.successMessage.collect { message ->
                if (message != null) {
                    showMessage(message)
                    quizViewModel.clearMessages()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            quizViewModel.errorMessage.collect { message ->
                if (message != null) {
                    showMessage(message)
                    quizViewModel.clearMessages()
                }
            }
        }
    }
    
    private fun updateUI(quiz: Quiz) {
        binding.apply {
            tvQuizTitle.text = quiz.title
            tvQuizDescription.text = quiz.description ?: "Aucune description"
            tvQuestionCount.text = quiz.questions.size.toString()
            tvTotalPoints.text = quiz.totalPoints.toString()
            tvTimeLimit.text = if (quiz.timeLimit != null) "${quiz.timeLimit}" else "∞"
            tvStatus.text = quiz.status.name
            tvMaxAttempts.text = quiz.maxAttempts.toString()
            tvPassingScore.text = "${quiz.passingScore.toInt()}%"
            tvCreatedAt.text = quiz.createdAt ?: "Non défini"
            
            // Mettre à jour la liste des questions
            if (quiz.questions.isNotEmpty()) {
                questionAdapter.submitList(quiz.questions)
                rvQuestions.visibility = View.VISIBLE
                tvNoQuestions.visibility = View.GONE
            } else {
                rvQuestions.visibility = View.GONE
                tvNoQuestions.visibility = View.VISIBLE
            }
            
            // Mettre à jour le bouton de publication
            btnPublishQuiz.text = when (quiz.status) {
                com.edunova.mobile.domain.model.AssignmentStatus.DRAFT -> "Publier"
                com.edunova.mobile.domain.model.AssignmentStatus.PUBLISHED -> "Dépublier"
                com.edunova.mobile.domain.model.AssignmentStatus.ARCHIVED -> "Restaurer"
            }
        }
    }
    
    private fun editQuestion(question: QuizQuestion, position: Int) {
        // TODO: Ouvrir un dialog d'édition ou naviguer vers un écran d'édition
        showMessage("Édition de question en développement")
    }
    
    private fun deleteQuestion(question: QuizQuestion, position: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer la question")
            .setMessage("Êtes-vous sûr de vouloir supprimer cette question ?")
            .setPositiveButton("Supprimer") { _, _ ->
                // TODO: Implémenter la suppression de question
                showMessage("Suppression de question en développement")
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun addNewQuestion() {
        // TODO: Navigation vers l'ajout de question ou dialog
        showMessage("Ajout de question en développement")
    }
    
    private fun publishQuiz() {
        quizViewModel.publishQuiz(args.quizId)
    }
    
    private fun confirmDeleteQuiz() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer le quiz")
            .setMessage("Êtes-vous sûr de vouloir supprimer ce quiz ? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                quizViewModel.deleteQuiz(args.quizId)
                findNavController().navigateUp()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}