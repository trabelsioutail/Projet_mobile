package com.edunova.mobile.presentation.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.edunova.mobile.databinding.FragmentQuizResultBinding
import com.edunova.mobile.domain.model.QuizSubmission
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.QuizViewModel
import com.edunova.mobile.utils.Resource
import com.edunova.mobile.utils.collectSafely
import com.edunova.mobile.utils.gone
import com.edunova.mobile.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizResultFragment : BaseFragment<FragmentQuizResultBinding>() {
    
    private val quizViewModel: QuizViewModel by viewModels()
    private val args: QuizResultFragmentArgs by navArgs()
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentQuizResultBinding {
        return FragmentQuizResultBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupToolbar()
        loadResults()
    }
    
    private fun setupToolbar() {
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun loadResults() {
        // Pour cette démo, on va simuler les résultats basés sur la soumission récente
        displayMockResults()
    }
    
    override fun observeData() {
        quizViewModel.submitQuizResult.collectSafely(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { submission ->
                        displayResults(submission)
                    }
                }
                else -> {
                    // Utiliser les résultats mockés si pas de données
                }
            }
        }
    }
    
    private fun displayMockResults() {
        // Simuler des résultats basés sur l'ID de soumission
        val mockSubmission = QuizSubmission(
            id = args.submissionId,
            quizId = 1,
            studentId = 27,
            answers = mapOf(1 to "a", 2 to "b"),
            score = 85,
            earnedPoints = 17,
            totalPoints = 20,
            passed = true,
            submittedAt = System.currentTimeMillis(),
            attemptNumber = 1
        )
        displayResults(mockSubmission)
    }
    
    private fun displayResults(submission: QuizSubmission) {
        binding.apply {
            progressBar.gone()
            layoutContent.visible()
            
            // Score principal
            textViewScore.text = "${submission.score}%"
            progressBarScore.progress = submission.score
            
            // Statut
            if (submission.passed) {
                textViewStatus.text = "Réussi ✅"
                textViewStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                textViewCongratulations.text = "Félicitations ! Vous avez réussi ce quiz."
                textViewCongratulations.visible()
            } else {
                textViewStatus.text = "Échoué ❌"
                textViewStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                textViewCongratulations.text = "Continuez vos efforts ! Vous pouvez réessayer."
                textViewCongratulations.visible()
            }
            
            // Détails
            textViewEarnedPoints.text = "${submission.earnedPoints} / ${submission.totalPoints} points"
            textViewAttemptNumber.text = "Tentative ${submission.attemptNumber}"
            
            // Date de soumission
            val date = java.text.SimpleDateFormat("dd/MM/yyyy à HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(submission.submittedAt))
            textViewSubmittedAt.text = "Soumis le $date"
            
            // Recommandations
            val recommendations = if (submission.passed) {
                listOf(
                    "Excellent travail ! Continuez sur cette lancée.",
                    "Vous maîtrisez bien les concepts abordés.",
                    "N'hésitez pas à passer aux quiz suivants."
                )
            } else {
                listOf(
                    "Révisez les concepts non maîtrisés.",
                    "Consultez les ressources du cours.",
                    "Demandez de l'aide à votre enseignant si nécessaire.",
                    "Réessayez le quiz après révision."
                )
            }
            
            textViewRecommendations.text = recommendations.joinToString("\n• ", "• ")
        }
    }
    
    override fun setupListeners() {
        binding.buttonReturnToQuizzes.setOnClickListener {
            // Retourner à la liste des quiz
            findNavController().popBackStack(
                com.edunova.mobile.R.id.nav_quizzes,
                false
            )
        }
        
        binding.buttonRetryQuiz.setOnClickListener {
            // Recommencer le quiz (si autorisé)
            showRetryConfirmation()
        }
    }
    
    private fun showRetryConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Recommencer le quiz")
            .setMessage("Voulez-vous recommencer ce quiz ? Votre score actuel sera remplacé.")
            .setPositiveButton("Recommencer") { _, _ ->
                // Naviguer vers le quiz
                findNavController().popBackStack(
                    com.edunova.mobile.R.id.nav_quizzes,
                    false
                )
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}