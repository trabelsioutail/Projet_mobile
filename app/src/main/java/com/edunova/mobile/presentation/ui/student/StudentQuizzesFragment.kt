package com.edunova.mobile.presentation.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentStudentQuizzesBinding
import com.edunova.mobile.domain.model.Quiz
import com.edunova.mobile.presentation.adapter.QuizAdapter
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.QuizViewModel
import com.edunova.mobile.utils.Resource
import com.edunova.mobile.utils.collectSafely
import com.edunova.mobile.utils.gone
import com.edunova.mobile.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentQuizzesFragment : BaseFragment<FragmentStudentQuizzesBinding>() {
    
    private val quizViewModel: QuizViewModel by viewModels()
    private lateinit var quizAdapter: QuizAdapter
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStudentQuizzesBinding {
        return FragmentStudentQuizzesBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupRecyclerView()
        loadQuizzes()
    }
    
    override fun observeData() {
        // Observer les quiz disponibles pour l'étudiant
        quizViewModel.teacherQuizzesState.collectSafely(viewLifecycleOwner) { resource ->
            binding.swipeRefreshLayout.isRefreshing = false
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.recyclerViewQuizzes.gone()
                    binding.textViewEmpty.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    val quizzes = resource.data ?: emptyList()
                    if (quizzes.isEmpty()) {
                        binding.recyclerViewQuizzes.gone()
                        binding.textViewEmpty.visible()
                        binding.textViewEmpty.text = "Aucun quiz disponible"
                    } else {
                        binding.recyclerViewQuizzes.visible()
                        binding.textViewEmpty.gone()
                        quizAdapter.submitList(quizzes)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.recyclerViewQuizzes.gone()
                    binding.textViewEmpty.visible()
                    binding.textViewEmpty.text = resource.message ?: "Erreur de chargement"
                }
                null -> {
                    // État initial
                }
            }
        }
        
        // Observer les soumissions de l'étudiant
        quizViewModel.mySubmissions.collectSafely(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val submissions = resource.data ?: emptyList()
                    // Mettre à jour l'adapter avec les informations de soumission
                    quizAdapter.updateSubmissions(submissions)
                }
                else -> {
                    // Gérer les autres états si nécessaire
                }
            }
        }
        
        // Observer les erreurs
        quizViewModel.errorMessage.collectSafely(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                quizViewModel.clearErrorMessage()
            }
        }
    }
    
    override fun setupListeners() {
        safeWithBinding { binding ->
            binding.swipeRefreshLayout.setOnRefreshListener {
                loadQuizzes(forceRefresh = true)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun setupRecyclerView() {
        quizAdapter = QuizAdapter(
            onQuizClick = { quiz ->
                navigateToQuizDetail(quiz)
            },
            onStartQuizClick = { quiz ->
                startQuiz(quiz)
            },
            isStudentView = true
        )
        
        binding.recyclerViewQuizzes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quizAdapter
        }
    }
    
    private fun loadQuizzes(forceRefresh: Boolean = false) {
        // Charger les quiz disponibles pour les étudiants
        quizViewModel.loadStudentQuizzes()
        // Charger les soumissions de l'étudiant
        quizViewModel.loadMySubmissions()
    }
    
    private fun navigateToQuizDetail(quiz: Quiz) {
        // Navigation vers les détails du quiz (pour les enseignants)
        // TODO: Implémenter la navigation
    }
    
    private fun startQuiz(quiz: Quiz) {
        val action = StudentQuizzesFragmentDirections
            .actionQuizzesToTakeQuiz(quiz.id)
        findNavController().navigate(action)
    }
    
    private fun showQuizInterface(quiz: Quiz) {
        val questions = quiz.questions
        if (questions.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "Ce quiz n'a pas de questions", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        var currentQuestionIndex = 0
        val userAnswers = mutableMapOf<String, String>()
        
        fun showQuestion(questionIndex: Int) {
            if (questionIndex >= questions.size) {
                // Quiz terminé, calculer le score
                calculateAndShowResult(quiz, userAnswers)
                return
            }
            
            val question = questions[questionIndex]
            val options = question.options
            
            val optionTexts = options.map { it.text }.toTypedArray()
            
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Question ${questionIndex + 1}/${questions.size}")
                .setMessage(question.questionText)
                .setSingleChoiceItems(optionTexts, -1) { dialog, which ->
                    val selectedOption = options[which]
                    userAnswers[question.id.toString()] = selectedOption.id
                    
                    dialog.dismiss()
                    
                    // Passer à la question suivante
                    showQuestion(questionIndex + 1)
                }
                .setCancelable(false)
                .show()
        }
        
        showQuestion(0)
    }
    
    private fun calculateAndShowResult(quiz: Quiz, userAnswers: Map<String, String>) {
        var correctAnswers = 0
        val totalQuestions = quiz.questions.size
        
        quiz.questions.forEach { question ->
            val userAnswer = userAnswers[question.id.toString()]
            if (userAnswer == question.correctAnswer) {
                correctAnswers++
            }
        }
        
        val score = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
        val passed = score >= (quiz.passingScore?.toInt() ?: 70)
        
        val message = """
            Quiz terminé !
            
            Score: $score%
            Bonnes réponses: $correctAnswers/$totalQuestions
            Résultat: ${if (passed) "Réussi ✅" else "Échoué ❌"}
            
            ${if (passed) "Félicitations !" else "Continuez vos efforts !"}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Résultat du Quiz")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                // Recharger les quiz pour mettre à jour le statut
                loadQuizzes(true)
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showErrorMessage(message: String) {
        safeWithBinding { binding ->
            binding.swipeRefreshLayout.isRefreshing = false
            // TODO: Ajouter un Snackbar ici si nécessaire
        }
    }
}