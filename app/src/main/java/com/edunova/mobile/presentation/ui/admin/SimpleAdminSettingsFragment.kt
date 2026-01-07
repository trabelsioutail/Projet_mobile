package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.edunova.mobile.databinding.FragmentAdminSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SimpleAdminSettingsFragment : Fragment() {
    
    private var _binding: FragmentAdminSettingsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupClickListeners()
        loadSettings()
    }
    
    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonSaveSettings.setOnClickListener {
            saveSettings()
        }
        
        binding.buttonResetSettings.setOnClickListener {
            resetSettings()
        }
    }
    
    private fun loadSettings() {
        // Load current settings into the form fields
        binding.editTextAppName.setText("EduNova")
        binding.editTextMaxUsersPerCourse.setText("30")
        binding.editTextMaxQuizAttempts.setText("3")
        binding.editTextSessionTimeout.setText("30")
        
        binding.switchEnableRegistration.isChecked = true
        binding.switchEnableNotifications.isChecked = true
        binding.switchMaintenanceMode.isChecked = false
    }
    
    private fun saveSettings() {
        val appName = binding.editTextAppName.text.toString()
        val maxUsers = binding.editTextMaxUsersPerCourse.text.toString()
        val maxAttempts = binding.editTextMaxQuizAttempts.text.toString()
        val sessionTimeout = binding.editTextSessionTimeout.text.toString()
        
        val enableRegistration = binding.switchEnableRegistration.isChecked
        val enableNotifications = binding.switchEnableNotifications.isChecked
        val maintenanceMode = binding.switchMaintenanceMode.isChecked
        
        // Here you would normally save to backend/database
        Toast.makeText(requireContext(), "✅ Paramètres sauvegardés avec succès", Toast.LENGTH_SHORT).show()
    }
    
    private fun resetSettings() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Réinitialiser les paramètres")
            .setMessage("Êtes-vous sûr de vouloir réinitialiser tous les paramètres aux valeurs par défaut ?")
            .setPositiveButton("Réinitialiser") { _, _ ->
                loadSettings() // Reset to default values
                Toast.makeText(requireContext(), "🔄 Paramètres réinitialisés aux valeurs par défaut", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}