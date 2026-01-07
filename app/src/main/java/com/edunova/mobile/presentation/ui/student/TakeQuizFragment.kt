package com.edunova.mobile.presentation.ui.student

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.databinding.FragmentTakeQuizBinding
import com.edunova.mobile.domain.model.Quiz
import com.edunova.mobile.presentation.adapter.TakeQuizQuestionAdapter
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.QuizViewModel
import com.edunova.mobile.utils.Resource
import com.edunova.mobile.utils.collectSafely
import com.edunova.mobile.utils.gone
import com.edunova.mobile.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TakeQuizFragment : BaseFragment<FragmentTakeQuizBinding>() {
    
    private val quizViewModel: QuizViewModel by viewModels()
    private val args: TakeQuizFragmentArgs by navArgs()
    private lateinit var questionAdapter: TakeQuizQuestionAdapter
    
    private var currentQuiz: Quiz? = null
    private var countDownTimer: CountDownTimer? = null
    private val userAnswers = mutableMapOf<Int, String>()
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTakeQuizBinding {
        return FragmentTakeQuizBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupToolbar()
        setupRecyclerView()
        loadQuiz()
    }
    
    private fun setupToolbar() {
        binding.buttonBack.setOnClickListener {
            showExitConfirmation()
        }
    }
    
    private fun setupRecyclerView() {
        questionAdapter = TakeQuizQuestionAdapter { questionId, selectedOptionId ->
            onAnswerSelected(questionId, selectedOptionId)
        }
        
        binding.recyclerViewQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = questionAdapter
        }
    }
    
    private fun loadQuiz() {
        quizViewModel.loadQuizDetails(args.quizId)
    }
    
    override fun observeData() {
        quizViewModel.selectedQuiz.collectSafely(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.layoutQuizContent.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.layoutQuizContent.visible()
                    currentQuiz = resource.data
                    displayQuiz(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    android.widget.Toast.makeText(
                        requireContext(),
                        resource.message ?: "Erreur de chargement",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
                null -> {
                    // État initial
                }
            }
        }
        
        quizViewModel.submitQuizResult.collectSafely(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.buttonSubmit.isEnabled = false
                    binding.buttonSubmit.text = "Soumission en cours..."
                }
                is Resource.Success -> {
                    val submission = resource.data
                    if (submission != null) {
                        // Naviguer vers les résultats
                        val action = TakeQuizFragmentDirections
                            .actionTakeQuizToResult(submission.id)
                        findNavController().navigate(action)
                    }
                }
                is Resource.Error -> {
                    binding.buttonSubmit.isEnabled = true
                    binding.buttonSubmit.text = "Soumettre le quiz"
                    android.widget.Toast.makeText(
                        requireContext(),
                        resource.message ?: "Erreur lors de la soumission",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                null -> {
                    // État initial
                }
            }
        }
    }
    
    private fun displayQuiz(quiz: Quiz?) {
        quiz?.let {
            binding.apply {
                textViewQuizTitle.text = it.title
                textViewQuizDescription.text = it.description ?: "Aucune description"
                textViewQuestionCount.text = "${it.questions.size} questions"
                textViewTimeLimit.text = if (it.timeLimit != null) {
                    "${it.timeLimit} minutes"
                } else {
                    "Pas de limite"
                }
                
                updateProgress()
                questionAdapter.submitList(it.questions)
                
                // Démarrer le timer si nécessaire
                it.timeLimit?.let { timeLimit ->
                    startTimer(timeLimit * 60 * 1000L) // Convertir en millisecondes
                }
            }
        }
    }
    
    private fun startTimer(durationMs: Long) {
        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.textViewTimer.text = String.format("%02d:%02d", minutes, seconds)
            }
            
            override fun onFinish() {
                binding.textViewTimer.text = "00:00"
                android.widget.Toast.makeText(
                    requireContext(),
                    "Temps écoulé ! Soumission automatique...",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                submitQuiz()
            }
        }.start()
    }
    
    private fun onAnswerSelected(questionId: Int, selectedOptionId: String) {
        userAnswers[questionId] = selectedOptionId
        updateProgress()
        updateSubmitButton()
    }
    
    private fun updateProgress() {
        val totalQuestions = currentQuiz?.questions?.size ?: 0
        val answeredQuestions = userAnswers.size
        binding.textViewProgress.text = "$answeredQuestions / $totalQuestions réponses"
    }
    
    private fun updateSubmitButton() {
        val totalQuestions = currentQuiz?.questions?.size ?: 0
        val answeredQuestions = userAnswers.size
        
        binding.buttonSubmit.isEnabled = answeredQuestions == totalQuestions
        
        if (answeredQuestions == totalQuestions) {
            binding.buttonSubmit.text = "Soumettre le quiz"
        } else {
            binding.buttonSubmit.text = "Répondez à toutes les questions (${totalQuestions - answeredQuestions} restantes)"
        }
    }
    
    override fun setupListeners() {
        binding.buttonSubmit.setOnClickListener {
            showSubmitConfirmation()
        }
    }
    
    private fun showSubmitConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Soumettre le quiz")
            .setMessage("Êtes-vous sûr de vouloir soumettre vos réponses ? Cette action est irréversible.")
            .setPositiveButton("Soumettre") { _, _ ->
                submitQuiz()
            }
            .setNegativeButton("Continuer", null)
            .show()
    }
    
    private fun submitQuiz() {
        countDownTimer?.cancel()
        currentQuiz?.let { quiz ->
            quizViewModel.submitQuiz(quiz.id, userAnswers)
        }
    }
    
    private fun showExitConfirmation() {
        if (userAnswers.isNotEmpty()) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Quitter le quiz")
                .setMessage("Vos réponses seront perdues. Êtes-vous sûr de vouloir quitter ?")
                .setPositiveButton("Quitter") { _, _ ->
                    countDownTimer?.cancel()
                    findNavController().navigateUp()
                }
                .setNegativeButton("Continuer", null)
                .show()
        } else {
            countDownTimer?.cancel()
            findNavController().navigateUp()
        }
    }
    
    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}