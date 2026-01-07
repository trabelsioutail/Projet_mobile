package com.edunova.mobile.presentation.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentResetPasswordBinding
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {
    
    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by activityViewModels()
    private var userEmail: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Récupérer l'email depuis les arguments
        userEmail = arguments?.getString("email") ?: ""
        
        if (userEmail.isEmpty()) {
            showError("Email manquant. Retour à l'écran précédent.")
            findNavController().navigateUp()
            return
        }
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Pré-remplir l'email
        binding.etEmail.setText(userEmail)
        
        // Validation en temps réel
        binding.etResetCode.addTextChangedListener {
            binding.tilResetCode.error = null
        }
        
        binding.etNewPassword.addTextChangedListener {
            binding.tilNewPassword.error = null
        }
        
        binding.etConfirmPassword.addTextChangedListener {
            binding.tilConfirmPassword.error = null
        }
        
        // Bouton de réinitialisation
        binding.btnResetPassword.setOnClickListener {
            if (validateInput()) {
                val email = userEmail
                val code = binding.etResetCode.text.toString().trim()
                val newPassword = binding.etNewPassword.text.toString()
                resetPassword(email, code, newPassword)
            }
        }
        
        // Renvoyer le code
        binding.tvResendCode.setOnClickListener {
            resendResetCode()
        }
        
        // Retour
        binding.tvBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModel() {
        // Observer les messages de succès
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.successMessage.collect { message ->
                message?.let {
                    showSuccessMessage(it)
                    authViewModel.clearMessages()
                    
                    // Rediriger vers la connexion après succès
                    if (it.contains("réinitialisé")) {
                        findNavController().navigateUp()
                    }
                }
            }
        }
        
        // Observer les messages d'erreur
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.errorMessage.collect { message ->
                message?.let {
                    showError(it)
                    authViewModel.clearMessages()
                }
            }
        }
        
        // Observer l'état de chargement
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.isLoading.collect { isLoading ->
                setLoadingState(isLoading)
            }
        }
    }
    
    private fun resetPassword(email: String, code: String, newPassword: String) {
        setLoadingState(true)
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Appel API réel pour réinitialiser le mot de passe
                val result = authViewModel.resetPassword(email, code, newPassword)
                
                setLoadingState(false)
                
                if (result.isSuccess) {
                    showSuccessMessage("Mot de passe réinitialisé avec succès ! Vous pouvez maintenant vous connecter.")
                    
                    // Rediriger vers la connexion après un délai
                    kotlinx.coroutines.delay(1500)
                    findNavController().navigate(R.id.action_reset_password_to_login)
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Erreur lors de la réinitialisation")
                }
            } catch (e: Exception) {
                setLoadingState(false)
                showError("Erreur de connexion: ${e.message}")
            }
        }
    }
    
    private fun resendResetCode() {
        setLoadingState(true)
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Appel API réel pour renvoyer le code
                val result = authViewModel.sendPasswordResetEmail(userEmail)
                
                setLoadingState(false)
                
                if (result.isSuccess) {
                    showSuccessMessage("Nouveau code envoyé à outailtrabelsi79@gmail.com")
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Erreur lors de l'envoi du code")
                }
            } catch (e: Exception) {
                setLoadingState(false)
                showError("Erreur de connexion: ${e.message}")
            }
        }
    }
    
    private fun validateInput(): Boolean {
        var isValid = true
        
        val code = binding.etResetCode.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        // Validation du code
        if (code.isEmpty()) {
            binding.tilResetCode.error = "Code requis"
            isValid = false
        } else if (code.length != 6) {
            binding.tilResetCode.error = "Le code doit contenir 6 caractères"
            isValid = false
        }
        
        // Validation du nouveau mot de passe
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = "Nouveau mot de passe requis"
            isValid = false
        } else if (newPassword.length < 6) {
            binding.tilNewPassword.error = "Le mot de passe doit contenir au moins 6 caractères"
            isValid = false
        }
        
        // Validation de la confirmation
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Confirmation requise"
            isValid = false
        } else if (newPassword != confirmPassword) {
            binding.tilConfirmPassword.error = "Les mots de passe ne correspondent pas"
            isValid = false
        }
        
        return isValid
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        binding.btnResetPassword.isEnabled = !isLoading
        binding.tvResendCode.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        if (isLoading) {
            binding.btnResetPassword.text = "Réinitialisation..."
        } else {
            binding.btnResetPassword.text = "Réinitialiser le mot de passe"
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .show()
    }
    
    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.success_color, null))
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}