package com.edunova.mobile.presentation.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentStudentDashboardBinding
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import com.edunova.mobile.presentation.ui.common.AiChatFragment
import com.edunova.mobile.utils.collectSafely
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentDashboardFragment : BaseFragment<FragmentStudentDashboardBinding>() {
    
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStudentDashboardBinding {
        return FragmentStudentDashboardBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupAnimations()
        loadStudentData()
        setupAiChatButton()
    }
    
    override fun observeData() {
        // Observer l'utilisateur authentifié
        authViewModel.authenticatedUser.collectSafely(viewLifecycleOwner) { user ->
            user?.let { updateStudentInfo(it) }
        }
        
        // Observer les erreurs
        authViewModel.errorMessage.collectSafely(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
            }
        }
    }
    
    override fun setupListeners() {
        safeWithBinding { binding ->
            // Actions rapides
            binding.actionViewCourses.setOnClickListener {
                findNavController().navigate(R.id.nav_courses)
            }
            
            binding.actionTakeQuiz.setOnClickListener {
                findNavController().navigate(R.id.nav_quizzes)
            }
            
            binding.actionMessages.setOnClickListener {
                findNavController().navigate(R.id.nav_messages)
            }
            
            // Navigation vers les détails
            binding.cardCurrentCourses.setOnClickListener {
                findNavController().navigate(R.id.nav_courses)
            }
            
            binding.cardRecentQuizzes.setOnClickListener {
                findNavController().navigate(R.id.nav_quizzes)
            }
            
            // Clic sur l'avatar pour aller au profil
            binding.imageViewStudentAvatar.setOnClickListener {
                findNavController().navigate(R.id.nav_profile)
            }
        }
    }
    
    private fun setupAiChatButton() {
        safeWithBinding { binding ->
            // Check if the FAB exists in the layout (for backward compatibility)
            try {
                val fabAiChat = binding.root.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAiChat)
                fabAiChat?.setOnClickListener {
                    openAiChat()
                }
            } catch (e: Exception) {
                // FAB not found in layout, ignore
            }
        }
    }
    
    private fun openAiChat() {
        try {
            val aiChatFragment = AiChatFragment.newInstance("etudiant")
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, aiChatFragment)
                .addToBackStack("AiChat")
                .commit()
        } catch (e: Exception) {
            try {
                val aiChatFragment = AiChatFragment.newInstance("etudiant")
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, aiChatFragment)
                    .addToBackStack("AiChat")
                    .commit()
            } catch (ex: Exception) {
                showError("🤖 Assistant IA temporairement indisponible")
            }
        }
    }
    
    private fun setupAnimations() {
        safeWithBinding { binding ->
            // Animation d'entrée pour la carte de bienvenue
            binding.cardWelcome.alpha = 0f
            binding.cardWelcome.translationY = -100f
            
            binding.cardWelcome.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .start()
            
            // Animation pour les statistiques
            binding.layoutQuickStats.alpha = 0f
            binding.layoutQuickStats.animate()
                .alpha(1f)
                .setStartDelay(200)
                .setDuration(400)
                .start()
            
            // Animation pour les cartes
            binding.cardCurrentCourses.alpha = 0f
            binding.cardCurrentCourses.translationX = -100f
            binding.cardCurrentCourses.animate()
                .alpha(1f)
                .translationX(0f)
                .setStartDelay(400)
                .setDuration(400)
                .start()
            
            binding.cardRecentQuizzes.alpha = 0f
            binding.cardRecentQuizzes.translationX = 100f
            binding.cardRecentQuizzes.animate()
                .alpha(1f)
                .translationX(0f)
                .setStartDelay(600)
                .setDuration(400)
                .start()
            
            binding.cardQuickActions.alpha = 0f
            binding.cardQuickActions.translationY = 100f
            binding.cardQuickActions.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(800)
                .setDuration(400)
                .start()
        }
    }
    
    private fun loadStudentData() {
        safeWithBinding { binding ->
            // Données statiques pour la démonstration
            binding.textViewCoursesCount.text = "3"
            binding.textViewQuizzesCount.text = "8"
            binding.textViewAverageScore.text = "16.2"
        }
    }
    
    private fun updateStudentInfo(user: com.edunova.mobile.domain.model.User) {
        safeWithBinding { binding ->
            binding.textViewStudentName.text = "${user.firstName} ${user.lastName}"
            
            // Avatar par défaut pour l'instant
            binding.imageViewStudentAvatar.setImageResource(R.drawable.ic_person)
            
            // Salutation personnalisée selon l'heure
            val greeting = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
                in 5..11 -> "Bonjour,"
                in 12..17 -> "Bon après-midi,"
                else -> "Bonsoir,"
            }
            
            // Mise à jour de la salutation (si vous avez un TextView pour cela)
            // binding.textViewGreeting.text = greeting
        }
    }
}