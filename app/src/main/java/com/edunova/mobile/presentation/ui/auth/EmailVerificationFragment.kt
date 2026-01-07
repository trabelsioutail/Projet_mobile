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
import com.edunova.mobile.databinding.FragmentEmailVerificationBinding
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmailVerificationFragment : Fragment() {
    
    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by activityViewModels()
    private var userEmail: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Récupérer l'email depuis les arguments ou utiliser un email par défaut
        userEmail = arguments?.getString("email") ?: "user@example.com"
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Pré-remplir l'email
        binding.etEmail.setText(userEmail)
        
        // Validation en temps réel
        binding.etVerificationCode.addTextChangedListener {
            binding.tilVerificationCode.error = null
        }
        
        // Bouton de vérification
        binding.btnVerifyEmail.setOnClickListener {
            if (validateInput()) {
                val email = userEmail
                val code = binding.etVerificationCode.text.toString().trim()
                verifyEmail(email, code)
            }
        }
        
        // Renvoyer le code
        binding.tvResendCode.setOnClickListener {
            resendVerificationCode()
        }
        
        // Changer d'email
        binding.tvChangeEmail.setOnClickListener {
            // Retourner à l'inscription pour changer l'email
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
                    if (it.contains("vérifié") || it.contains("actif")) {
                        kotlinx.coroutines.delay(2000)
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
    
    private fun verifyEmail(email: String, code: String) {
        // Simuler l'appel API pour le moment
        setLoadingState(true)
        
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(2000)
            setLoadingState(false)
            
            // Simuler le succès
            showSuccessMessage("🎉 Email vérifié avec succès ! Votre compte est maintenant actif. Redirection vers la connexion...")
            
            // Rediriger vers la connexion
            kotlinx.coroutines.delay(2500)
            findNavController().navigateUp()
        }
    }
    
    private fun resendVerificationCode() {
        setLoadingState(true)
        
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(1500)
            setLoadingState(false)
            
            showSuccessMessage("Nouveau code de vérification envoyé à outailtrabelsi79@gmail.com")
        }
    }
    
    private fun validateInput(): Boolean {
        val code = binding.etVerificationCode.text.toString().trim()
        
        if (code.isEmpty()) {
            binding.tilVerificationCode.error = "Code de vérification requis"
            return false
        }
        
        if (code.length != 6) {
            binding.tilVerificationCode.error = "Le code doit contenir 6 caractères"
            return false
        }
        
        return true
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        binding.btnVerifyEmail.isEnabled = !isLoading
        binding.tvResendCode.isEnabled = !isLoading
        binding.tvChangeEmail.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        if (isLoading) {
            binding.btnVerifyEmail.text = "Vérification..."
        } else {
            binding.btnVerifyEmail.text = "Vérifier mon compte"
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