package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.edunova.mobile.databinding.FragmentAdminCourseSettingsBinding
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminCourseSettingsFragment : Fragment() {
    
    private var _binding: FragmentAdminCourseSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCourseSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        loadSettings()
    }
    
    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        // General Settings
        binding.cardGeneralSettings.setOnClickListener {
            showGeneralSettingsDialog()
        }
        
        binding.cardEnrollmentSettings.setOnClickListener {
            showEnrollmentSettingsDialog()
        }
        
        binding.cardNotificationSettings.setOnClickListener {
            showNotificationSettingsDialog()
        }
        
        binding.cardGradingSettings.setOnClickListener {
            showGradingSettingsDialog()
        }
        
        // Advanced Settings
        binding.cardBackupSettings.setOnClickListener {
            showBackupSettingsDialog()
        }
        
        binding.cardIntegrationSettings.setOnClickListener {
            showIntegrationSettingsDialog()
        }
        
        binding.cardSecuritySettings.setOnClickListener {
            showSecuritySettingsDialog()
        }
        
        binding.cardMaintenanceSettings.setOnClickListener {
            showMaintenanceSettingsDialog()
        }
        
        // Action Buttons
        binding.buttonSaveSettings.setOnClickListener {
            saveAllSettings()
        }
        
        binding.buttonResetSettings.setOnClickListener {
            showResetSettingsDialog()
        }
        
        binding.buttonExportSettings.setOnClickListener {
            exportSettings()
        }
        
        binding.buttonImportSettings.setOnClickListener {
            importSettings()
        }
    }
    
    private fun loadSettings() {
        // Load current settings from backend
        // This would typically call adminViewModel.loadCourseSettings()
        
        // For now, set some default values
        binding.switchAutoEnrollment.isChecked = true
        binding.switchEmailNotifications.isChecked = true
        binding.switchPushNotifications.isChecked = false
        binding.switchMaintenanceMode.isChecked = false
    }
    
    private fun showGeneralSettingsDialog() {
        val message = """
            ⚙️ Paramètres généraux des cours
            
            • Durée par défaut des cours: 12 semaines
            • Nombre maximum d'étudiants par cours: 50
            • Langue par défaut: Français
            • Fuseau horaire: UTC+1 (Tunis)
            • Format de date: DD/MM/YYYY
            
            Voulez-vous modifier ces paramètres ?
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("⚙️ Paramètres généraux")
            .setMessage(message)
            .setPositiveButton("Modifier") { _, _ ->
                Toast.makeText(requireContext(), "Modification des paramètres généraux", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showEnrollmentSettingsDialog() {
        val message = """
            👥 Paramètres d'inscription
            
            • Inscription automatique: ${if (binding.switchAutoEnrollment.isChecked) "Activée" else "Désactivée"}
            • Approbation manuelle requise: Non
            • Limite d'inscriptions par étudiant: 5 cours
            • Période d'inscription: Toute l'année
            • Frais d'inscription: Gratuit
            
            Voulez-vous modifier ces paramètres ?
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("👥 Paramètres d'inscription")
            .setMessage(message)
            .setPositiveButton("Modifier") { _, _ ->
                Toast.makeText(requireContext(), "Modification des paramètres d'inscription", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showNotificationSettingsDialog() {
        val message = """
            🔔 Paramètres de notification
            
            • Notifications email: ${if (binding.switchEmailNotifications.isChecked) "Activées" else "Désactivées"}
            • Notifications push: ${if (binding.switchPushNotifications.isChecked) "Activées" else "Désactivées"}
            • Rappels automatiques: Activés
            • Notifications aux enseignants: Activées
            • Fréquence des résumés: Hebdomadaire
            
            Voulez-vous modifier ces paramètres ?
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("🔔 Paramètres de notification")
            .setMessage(message)
            .setPositiveButton("Modifier") { _, _ ->
                Toast.makeText(requireContext(), "Modification des paramètres de notification", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showGradingSettingsDialog() {
        val message = """
            📊 Paramètres de notation
            
            • Échelle de notation: 0-20
            • Note de passage: 10/20
            • Arrondi automatique: Activé
            • Pondération des quiz: 40%
            • Pondération des devoirs: 60%
            • Affichage des notes: Immédiat
            
            Voulez-vous modifier ces paramètres ?
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📊 Paramètres de notation")
            .setMessage(message)
            .setPositiveButton("Modifier") { _, _ ->
                Toast.makeText(requireContext(), "Modification des paramètres de notation", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showBackupSettingsDialog() {
        val message = """
            💾 Paramètres de sauvegarde
            
            • Sauvegarde automatique: Quotidienne à 2h00
            • Rétention des sauvegardes: 30 jours
            • Sauvegarde cloud: Activée
            • Chiffrement: AES-256
            • Dernière sauvegarde: Aujourd'hui à 2h00
            
            Voulez-vous modifier ces paramètres ?
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("💾 Paramètres de sauvegarde")
            .setMessage(message)
            .setPositiveButton("Modifier") { _, _ ->
                Toast.makeText(requireContext(), "Modification des paramètres de sauvegarde", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showIntegrationSettingsDialog() {
        val message = """
            🔗 Paramètres d'intégration
            
            • API externe: Connectée
            • Synchronisation LMS: Activée
            • Webhook notifications: Configurés
            • Single Sign-On (SSO): Désactivé
            • Export automatique: Hebdomadaire
            
            Voulez-vous modifier ces paramètres ?
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("🔗 Paramètres d'intégration")
            .setMessage(message)
            .setPositiveButton("Modifier") { _, _ ->
                Toast.makeText(requireContext(), "Modification des paramètres d'intégration", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showSecuritySettingsDialog() {
        val message = """
            🔒 Paramètres de sécurité
            
            • Authentification à deux facteurs: Recommandée
            • Complexité des mots de passe: Élevée
            • Session timeout: 2 heures
            • Audit des connexions: Activé
            • Chiffrement des données: AES-256
            
            Voulez-vous modifier ces paramètres ?
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("🔒 Paramètres de sécurité")
            .setMessage(message)
            .setPositiveButton("Modifier") { _, _ ->
                Toast.makeText(requireContext(), "Modification des paramètres de sécurité", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showMaintenanceSettingsDialog() {
        val message = """
            🔧 Paramètres de maintenance
            
            • Mode maintenance: ${if (binding.switchMaintenanceMode.isChecked) "Activé" else "Désactivé"}
            • Maintenance programmée: Dimanche 2h-4h
            • Nettoyage automatique: Activé
            • Optimisation base de données: Mensuelle
            • Monitoring système: Actif
            
            Voulez-vous modifier ces paramètres ?
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("🔧 Paramètres de maintenance")
            .setMessage(message)
            .setPositiveButton("Modifier") { _, _ ->
                Toast.makeText(requireContext(), "Modification des paramètres de maintenance", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun saveAllSettings() {
        AlertDialog.Builder(requireContext())
            .setTitle("💾 Sauvegarder les paramètres")
            .setMessage("Êtes-vous sûr de vouloir sauvegarder tous les paramètres modifiés ?")
            .setPositiveButton("Sauvegarder") { _, _ ->
                // Save settings to backend
                Toast.makeText(requireContext(), "Paramètres sauvegardés avec succès", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showResetSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🔄 Réinitialiser les paramètres")
            .setMessage("⚠️ Cette action va restaurer tous les paramètres par défaut. Cette action est irréversible !")
            .setPositiveButton("Réinitialiser") { _, _ ->
                resetToDefaultSettings()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun resetToDefaultSettings() {
        binding.switchAutoEnrollment.isChecked = true
        binding.switchEmailNotifications.isChecked = true
        binding.switchPushNotifications.isChecked = false
        binding.switchMaintenanceMode.isChecked = false
        
        Toast.makeText(requireContext(), "Paramètres réinitialisés aux valeurs par défaut", Toast.LENGTH_SHORT).show()
    }
    
    private fun exportSettings() {
        Toast.makeText(requireContext(), "Export des paramètres en cours...", Toast.LENGTH_SHORT).show()
        
        // Simulate export process
        binding.root.postDelayed({
            Toast.makeText(requireContext(), "Paramètres exportés vers: /storage/edunova_settings.json", Toast.LENGTH_LONG).show()
        }, 2000)
    }
    
    private fun importSettings() {
        AlertDialog.Builder(requireContext())
            .setTitle("📥 Importer les paramètres")
            .setMessage("Sélectionnez le fichier de paramètres à importer :")
            .setPositiveButton("Parcourir") { _, _ ->
                Toast.makeText(requireContext(), "Ouverture du sélecteur de fichiers...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}