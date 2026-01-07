package com.edunova.mobile.presentation.ui.student

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentStudentProfileBinding
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import com.edunova.mobile.utils.collectSafely
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentProfileFragment : BaseFragment<FragmentStudentProfileBinding>() {
    
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStudentProfileBinding {
        return FragmentStudentProfileBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupAnimations()
        loadStudentData()
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
            
            binding.actionViewBadges.setOnClickListener {
                showBadgesDialog()
            }
            
            binding.actionSettings.setOnClickListener {
                showSettingsDialog()
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
                .start()
            
            // Animation pour les statistiques
            binding.layoutStats.alpha = 0f
            binding.layoutStats.animate()
                .alpha(1f)
                .setStartDelay(200)
                .setDuration(400)
                .start()
            
            // Animation pour les cartes d'informations
            binding.cardAcademicInfo.alpha = 0f
            binding.cardAcademicInfo.translationX = -100f
            binding.cardAcademicInfo.animate()
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
    
    private fun loadStudentData() {
        safeWithBinding { binding ->
            // Données statiques pour la démonstration
            binding.textViewCoursesCount.text = "3"
            binding.textViewQuizzesCount.text = "8"
            binding.textViewBadgesCount.text = "5"
            binding.textViewLevel.text = "Niveau 3"
            binding.textViewAverage.text = "16.2/20"
            binding.textViewSpecialty.text = "Informatique"
            binding.textViewStudyYear.text = "3ème année Licence"
            binding.textViewEnrollmentDate.text = "Septembre 2023"
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
            "Modifier l'année d'étude"
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modifier le profil")
            .setItems(editOptions) { _, which ->
                when (which) {
                    0 -> showSuccess("Édition des informations en développement")
                    1 -> showSuccess("Changement de mot de passe en développement")
                    2 -> updateSpecialty()
                    3 -> updateStudyYear()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun updateSpecialty() {
        val specialties = arrayOf(
            "Informatique",
            "Développement Web",
            "Intelligence Artificielle",
            "Cybersécurité",
            "Data Science",
            "Réseaux et Télécommunications"
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
    
    private fun updateStudyYear() {
        val studyYears = arrayOf(
            "1ère année Licence",
            "2ème année Licence", 
            "3ème année Licence",
            "1ère année Master",
            "2ème année Master"
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choisir l'année d'étude")
            .setItems(studyYears) { _, which ->
                safeWithBinding { binding ->
                    binding.textViewStudyYear.text = studyYears[which]
                    showSuccess("Année d'étude mise à jour")
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showBadgesDialog() {
        val badges = """
            🏆 MES BADGES OBTENUS
            
            🥇 EXCELLENCE ACADÉMIQUE
            • Premier Quiz Parfait
            • Moyenne Supérieure à 15
            • Participation Active
            
            📚 APPRENTISSAGE
            • Cours Complété
            • Quiz Master
            • Étudiant Assidu
            
            🎯 PROGRESSION
            • Niveau 3 Atteint
            • 100 Points Gagnés
            • Semaine Parfaite
            
            🌟 SPÉCIAUX
            • Early Adopter
            • Feedback Champion
            
            Total: 5 badges obtenus
            Prochain objectif: Badge "Expert" (Niveau 5)
        """.trimIndent()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🏆 Mes Badges")
            .setMessage(badges)
            .setPositiveButton("Partager") { _, _ ->
                shareBadges()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun shareBadges() {
        val shareText = """
            🎓 Mes Badges EduNova
            
            👨‍🎓 Ahmed Ben Ali
            🏆 5 badges obtenus
            📊 Niveau 3
            ⭐ Moyenne: 16.2/20
            
            🥇 Badges récents:
            • Excellence Académique
            • Quiz Master
            • Étudiant Assidu
            
            Rejoignez EduNova et gagnez vos badges !
        """.trimIndent()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Mes Badges EduNova")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Partager mes badges"))
    }
    
    private fun showSettingsDialog() {
        val settings = arrayOf(
            "Notifications",
            "Confidentialité",
            "Langue",
            "Thème",
            "Aide et Support"
        )
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Paramètres")
            .setItems(settings) { _, which ->
                when (which) {
                    0 -> showNotificationSettings()
                    1 -> showSuccess("Paramètres de confidentialité")
                    2 -> showLanguageSettings()
                    3 -> showThemeSettings()
                    4 -> showHelpDialog()
                }
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showNotificationSettings() {
        val notifications = booleanArrayOf(true, true, false, true)
        val notificationTypes = arrayOf(
            "Nouveaux cours disponibles",
            "Rappels de quiz",
            "Messages des enseignants",
            "Résultats d'évaluations"
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
    
    private fun showHelpDialog() {
        val helpText = """
            📚 AIDE ET SUPPORT
            
            🎯 COMMENT UTILISER L'APP
            • Consultez vos cours dans l'onglet Cours
            • Passez les quiz dans l'onglet Quiz
            • Communiquez via Messages
            • Suivez votre progression ici
            
            🏆 SYSTÈME DE BADGES
            • Complétez des cours pour gagner des badges
            • Obtenez de bonnes notes aux quiz
            • Participez activement aux discussions
            
            📞 BESOIN D'AIDE ?
            • Email: support@edunova.tn
            • FAQ intégrée dans l'app
            • Guides vidéo disponibles
            
            🔄 MISES À JOUR
            • Vérifiez régulièrement les mises à jour
            • Nouvelles fonctionnalités ajoutées
        """.trimIndent()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📚 Aide et Support")
            .setMessage(helpText)
            .setPositiveButton("Contacter le Support") { _, _ ->
                showSuccess("Redirection vers le support")
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showDetailedStatistics() {
        val stats = """
            📊 STATISTIQUES DÉTAILLÉES
            
            📚 COURS
            • 3 cours inscrits
            • 2 cours terminés
            • 80% progression moyenne
            • 16.2/20 note moyenne
            
            📝 QUIZ
            • 8 quiz complétés
            • 75% taux de réussite
            • 12 min temps moyen
            • Meilleur score: 19/20
            
            🏆 ACHIEVEMENTS
            • 5 badges obtenus
            • Niveau 3 atteint
            • 156 points gagnés
            • Rang: Top 15%
            
            📈 PROGRESSION
            • Inscrit depuis 4 mois
            • 45 heures d'apprentissage
            • 23 jours d'activité
            • Objectif: Niveau 5
        """.trimIndent()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📊 Mes Statistiques")
            .setMessage(stats)
            .setPositiveButton("Partager") { _, _ ->
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
        
        startActivity(Intent.createChooser(shareIntent, "Partager mes statistiques"))
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