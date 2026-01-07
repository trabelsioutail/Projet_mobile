package com.edunova.mobile.presentation.ui.teacher

import android.animation.ObjectAnimator
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.viewModels
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentTeacherProfileEnhancedBinding
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import com.edunova.mobile.utils.collectSafely
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeacherProfileEnhancedFragment : BaseFragment<FragmentTeacherProfileEnhancedBinding>() {
    
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun createBinding(
        inflater: LayoutInflater, 
        container: ViewGroup?
    ): FragmentTeacherProfileEnhancedBinding {
        return FragmentTeacherProfileEnhancedBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupAnimations()
        loadUserData()
        setupStaticData()
    }
    
    override fun observeData() {
        // Observer l'utilisateur authentifié
        authViewModel.authenticatedUser.collectSafely(viewLifecycleOwner) { user ->
            user?.let { updateUserInfo(it) }
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
            // Bouton pour changer la photo de profil
            binding.fabChangePhoto.setOnClickListener {
                showChangePhotoDialog()
            }
            
            // Actions rapides
            binding.actionEditProfile.setOnClickListener {
                showEditProfileDialog()
            }
            
            binding.actionSettings.setOnClickListener {
                showSettingsDialog()
            }
            
            binding.actionShare.setOnClickListener {
                shareProfile()
            }
            
            // Statistiques cliquables
            binding.layoutStats.setOnClickListener {
                showDetailedStatistics()
            }
            
            // Bouton de déconnexion
            binding.buttonLogout.setOnClickListener {
                showLogoutConfirmation()
            }
            
            // Indicateur de statut
            binding.statusIndicator.setOnClickListener {
                toggleOnlineStatus()
            }
        }
    }
    
    private fun setupAnimations() {
        safeWithBinding { binding ->
            // Animation d'entrée pour la carte de profil
            binding.cardProfile.alpha = 0f
            binding.cardProfile.translationY = 100f
            
            binding.cardProfile.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
            
            // Animation pour les statistiques
            binding.layoutStats.alpha = 0f
            binding.layoutStats.animate()
                .alpha(1f)
                .setStartDelay(200)
                .setDuration(400)
                .start()
            
            // Animation pour les cartes d'informations
            binding.cardDetails.alpha = 0f
            binding.cardDetails.translationX = -100f
            binding.cardDetails.animate()
                .alpha(1f)
                .translationX(0f)
                .setStartDelay(400)
                .setDuration(400)
                .start()
            
            binding.cardQuickActions.alpha = 0f
            binding.cardQuickActions.translationX = 100f
            binding.cardQuickActions.animate()
                .alpha(1f)
                .translationX(0f)
                .setStartDelay(600)
                .setDuration(400)
                .start()
        }
    }
    
    private fun loadUserData() {
        // Les données utilisateur seront chargées via l'observer
    }
    
    private fun setupStaticData() {
        safeWithBinding { binding ->
            // Données statiques pour la démonstration
            binding.textViewCoursesCount.text = "8"
            binding.textViewQuizzesCount.text = "15"
            binding.textViewStudentsCount.text = "156"
            binding.textViewRating.text = "4.7"
            binding.textViewSpecialty.text = "Développement Web & Mobile"
            binding.textViewExperience.text = "5+ années d'enseignement"
            binding.textViewMemberSince.text = "Janvier 2023"
        }
    }
    
    private fun updateUserInfo(user: com.edunova.mobile.domain.model.User) {
        safeWithBinding { binding ->
            binding.textViewName.text = "${user.firstName} ${user.lastName}"
            binding.textViewEmail.text = user.email
            
            // Avatar par défaut pour l'instant
            binding.imageViewAvatar.setImageResource(R.drawable.ic_person)
        }
    }
    
    private fun animateStatistic(textView: android.widget.TextView, newValue: Int) {
        val currentValue = textView.text.toString().toIntOrNull() ?: 0
        
        ObjectAnimator.ofInt(currentValue, newValue).apply {
            duration = 1000
            addUpdateListener { animation ->
                textView.text = animation.animatedValue.toString()
            }
            start()
        }
    }
    
    private fun showChangePhotoDialog() {
        val options = arrayOf(
            "Prendre une photo",
            "Choisir depuis la galerie",
            "Supprimer la photo"
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Photo de profil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSuccess("Prise de photo en développement")
                    1 -> showSuccess("Galerie en développement")
                    2 -> {
                        safeWithBinding { binding ->
                            binding.imageViewAvatar.setImageResource(R.drawable.ic_person)
                            showSuccess("Photo de profil supprimée")
                        }
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showEditProfileDialog() {
        val editOptions = arrayOf(
            "Modifier les informations personnelles",
            "Changer le mot de passe",
            "Mettre à jour la spécialité",
            "Modifier la biographie"
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modifier le profil")
            .setItems(editOptions) { _, which ->
                when (which) {
                    0 -> showSuccess("Édition des informations en développement")
                    1 -> showSuccess("Changement de mot de passe en développement")
                    2 -> updateSpecialty()
                    3 -> showSuccess("Édition de biographie en développement")
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun updateSpecialty() {
        val specialties = arrayOf(
            "Développement Web & Mobile",
            "Intelligence Artificielle",
            "Cybersécurité",
            "Data Science",
            "DevOps & Cloud",
            "UI/UX Design"
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choisir une spécialité")
            .setItems(specialties) { _, which ->
                safeWithBinding { binding ->
                    binding.textViewSpecialty.text = specialties[which]
                    showSuccess("Spécialité mise à jour")
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showSettingsDialog() {
        val settings = arrayOf(
            "Notifications",
            "Confidentialité",
            "Langue",
            "Thème",
            "Sauvegarde et synchronisation"
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Paramètres")
            .setItems(settings) { _, which ->
                when (which) {
                    0 -> showNotificationSettings()
                    1 -> showSuccess("Paramètres de confidentialité")
                    2 -> showLanguageSettings()
                    3 -> showThemeSettings()
                    4 -> showSuccess("Paramètres de sauvegarde")
                }
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showNotificationSettings() {
        val notifications = booleanArrayOf(true, true, false, true)
        val notificationTypes = arrayOf(
            "Nouveaux messages",
            "Soumissions de quiz",
            "Rappels de cours",
            "Mises à jour système"
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Notifications")
            .setMultiChoiceItems(notificationTypes, notifications) { _, which, isChecked ->
                notifications[which] = isChecked
            }
            .setPositiveButton("Sauvegarder") { _, _ ->
                showSuccess("Paramètres de notification sauvegardés")
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showLanguageSettings() {
        val languages = arrayOf("Français", "English", "العربية", "Español")
        var selectedLanguage = 0
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Langue")
            .setSingleChoiceItems(languages, selectedLanguage) { _, which ->
                selectedLanguage = which
            }
            .setPositiveButton("Appliquer") { _, _ ->
                showSuccess("Langue changée: ${languages[selectedLanguage]}")
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showThemeSettings() {
        val themes = arrayOf("Clair", "Sombre", "Automatique")
        var selectedTheme = 0
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Thème")
            .setSingleChoiceItems(themes, selectedTheme) { _, which ->
                selectedTheme = which
            }
            .setPositiveButton("Appliquer") { _, _ ->
                showSuccess("Thème changé: ${themes[selectedTheme]}")
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun shareProfile() {
        val shareText = """
            🎓 Profil EduNova
            
            👨‍🏫 Ghofrane Sebteoui
            📧 ghofrane.sebteoui@edunova.tn
            🏆 Enseignant Expert
            ⭐ Note: 4.7/5
            
            📚 Spécialité: Développement Web & Mobile
            👥 156 étudiants
            📝 15 quiz créés
            📖 8 cours actifs
            
            Rejoignez EduNova pour apprendre avec les meilleurs !
        """.trimIndent()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Profil EduNova - Ghofrane Sebteoui")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Partager le profil"))
    }
    
    private fun showDetailedStatistics() {
        val stats = """
            📊 STATISTIQUES DÉTAILLÉES
            
            📚 COURS
            • 8 cours actifs
            • 156 étudiants inscrits
            • 4.7/5 évaluation moyenne
            • 89% taux de satisfaction
            
            📝 QUIZ
            • 15 quiz créés
            • 234 soumissions totales
            • 78% taux de réussite moyen
            • 14 min temps moyen
            
            👥 ENGAGEMENT
            • 92% taux de participation
            • 67 heures d'enseignement
            • 45 messages reçus ce mois
            • 23 évaluations positives
            
            🏆 ACHIEVEMENTS
            • Top Teacher du mois
            • 100+ heures de contenu
            • 50+ avis 5 étoiles
        """.trimIndent()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📊 Statistiques Complètes")
            .setMessage(stats)
            .setPositiveButton("Exporter PDF") { _, _ ->
                showSuccess("Export PDF en développement")
            }
            .setNeutralButton("Partager") { _, _ ->
                shareStatistics(stats)
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun shareStatistics(stats: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, stats)
            putExtra(Intent.EXTRA_SUBJECT, "Mes statistiques EduNova")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Partager les statistiques"))
    }
    
    private fun toggleOnlineStatus() {
        safeWithBinding { binding ->
            val isOnline = binding.statusIndicator.background.constantState == 
                resources.getDrawable(R.drawable.circle_success, null).constantState
            
            if (isOnline) {
                binding.statusIndicator.setBackgroundResource(R.drawable.circle_light)
                showSuccess("Statut: Hors ligne")
            } else {
                binding.statusIndicator.setBackgroundResource(R.drawable.circle_success)
                showSuccess("Statut: En ligne")
            }
        }
    }
    
    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setIcon(R.drawable.ic_logout)
            .setPositiveButton("Déconnexion") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun performLogout() {
        // Animation de sortie
        safeWithBinding { binding ->
            binding.cardProfile.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(300)
                .withEndAction {
                    authViewModel.logout()
                }
                .start()
        }
    }
}