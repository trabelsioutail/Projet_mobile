package com.edunova.mobile.presentation.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.edunova.mobile.databinding.FragmentAdminProfileBinding
import com.edunova.mobile.presentation.ui.auth.AuthActivity
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminProfileFragment : Fragment() {
    
    private var _binding: FragmentAdminProfileBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            setupUI()
            setupClickListeners()
            loadAdminProfile()
            
            Toast.makeText(requireContext(), "Profil admin chargé avec succès", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors du chargement: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupUI() {
        // Configuration de l'interface utilisateur
        binding.apply {
            // Informations de base de l'admin
            textViewAdminName.text = "Administrateur EduNova"
            textViewAdminEmail.text = "admin@edunova.tn"
            textViewAdminRole.text = "👑 Super Administrateur"
            
            // Statistiques rapides
            textViewTotalUsers.text = "Chargement..."
            textViewTotalCourses.text = "Chargement..."
            textViewTotalQuizzes.text = "Chargement..."
            textViewTotalEnrollments.text = "Chargement..."
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // Bouton de modification du profil
            buttonEditProfile.setOnClickListener {
                showEditProfileDialog()
            }
            
            // Bouton de changement de mot de passe
            buttonChangePassword.setOnClickListener {
                showChangePasswordDialog()
            }
            
            // Bouton de paramètres système
            buttonSystemSettings.setOnClickListener {
                showSystemSettingsDialog()
            }
            
            // Bouton de sauvegarde
            buttonCreateBackup.setOnClickListener {
                createSystemBackup()
            }
            
            // Bouton de déconnexion
            buttonLogout.setOnClickListener {
                showLogoutDialog()
            }
            
            // Cartes de navigation rapide
            cardUsers.setOnClickListener {
                // Navigation vers la gestion des utilisateurs
                Toast.makeText(requireContext(), "Navigation vers les utilisateurs", Toast.LENGTH_SHORT).show()
            }
            
            cardCourses.setOnClickListener {
                // Navigation vers la gestion des cours
                Toast.makeText(requireContext(), "Navigation vers les cours", Toast.LENGTH_SHORT).show()
            }
            
            cardQuizzes.setOnClickListener {
                // Navigation vers la gestion des quiz
                Toast.makeText(requireContext(), "Navigation vers les quiz", Toast.LENGTH_SHORT).show()
            }
            
            cardReports.setOnClickListener {
                // Navigation vers les rapports
                Toast.makeText(requireContext(), "Navigation vers les rapports", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadAdminProfile() {
        // Charger les statistiques système
        adminViewModel.loadSystemStats()
        
        // Observer les statistiques
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.systemStatsState.collect { resource ->
                resource?.let {
                    when (it) {
                        is com.edunova.mobile.utils.Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is com.edunova.mobile.utils.Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            it.data?.let { stats ->
                                updateStatistics(stats)
                            }
                        }
                        is com.edunova.mobile.utils.Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                            // Afficher des statistiques par défaut
                            setDefaultStatistics()
                        }
                    }
                }
            }
        }
    }
    
    private fun updateStatistics(stats: com.edunova.mobile.data.repository.SystemStats) {
        binding.apply {
            textViewTotalUsers.text = "${stats.totalUsers}"
            textViewTotalCourses.text = "${stats.totalCourses}"
            textViewTotalQuizzes.text = "${stats.totalQuizzes}"
            textViewTotalEnrollments.text = "${stats.totalEnrollments}"
        }
    }
    
    private fun setDefaultStatistics() {
        binding.apply {
            textViewTotalUsers.text = "N/A"
            textViewTotalCourses.text = "N/A"
            textViewTotalQuizzes.text = "N/A"
            textViewTotalEnrollments.text = "N/A"
        }
    }
    
    private fun showEditProfileDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("✏️ Modifier le profil")
            .setMessage("Fonctionnalité de modification du profil admin à implémenter.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showChangePasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔒 Changer le mot de passe")
            .setMessage("Fonctionnalité de changement de mot de passe à implémenter.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showSystemSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚙️ Paramètres système")
            .setMessage("Accès aux paramètres système avancés.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun createSystemBackup() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💾 Créer une sauvegarde")
            .setMessage("Voulez-vous créer une sauvegarde complète du système ?")
            .setPositiveButton("Créer") { _, _ ->
                adminViewModel.createBackup()
                
                // Observer le résultat de la sauvegarde
                viewLifecycleOwner.lifecycleScope.launch {
                    adminViewModel.backupActionState.collect { resource ->
                        resource?.let {
                            when (it) {
                                is com.edunova.mobile.utils.Resource.Success -> {
                                    Toast.makeText(requireContext(), "Sauvegarde créée avec succès", Toast.LENGTH_SHORT).show()
                                }
                                is com.edunova.mobile.utils.Resource.Error -> {
                                    Toast.makeText(requireContext(), "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🚪 Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Déconnexion") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun performLogout() {
        // Afficher un indicateur de chargement
        binding.progressBar.visibility = View.VISIBLE
        
        // Observer l'état de déconnexion
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collect { resource ->
                resource?.let {
                    when (it) {
                        is com.edunova.mobile.utils.Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is com.edunova.mobile.utils.Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            // Déconnexion réussie, rediriger vers l'écran de connexion
                            redirectToLogin()
                        }
                        is com.edunova.mobile.utils.Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Erreur lors de la déconnexion: ${it.message}", Toast.LENGTH_LONG).show()
                            // Même en cas d'erreur, on peut forcer la déconnexion locale
                            redirectToLogin()
                        }
                    }
                }
            }
        }
        
        // Observer les messages de succès
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.successMessage.collect { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    authViewModel.clearMessages()
                }
            }
        }
        
        // Déclencher la déconnexion
        authViewModel.logout()
    }
    
    private fun redirectToLogin() {
        try {
            // Créer l'intent vers AuthActivity
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Démarrer AuthActivity
            startActivity(intent)
            
            // Terminer l'activité actuelle
            requireActivity().finish()
            
            Toast.makeText(requireContext(), "Déconnexion réussie", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors de la redirection: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}