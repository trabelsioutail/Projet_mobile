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
import com.edunova.mobile.databinding.FragmentForgotPasswordBinding
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import com.edunova.mobile.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {
    
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Validation en temps réel
        binding.etEmail.addTextChangedListener {
            binding.tilEmail.error = null
            binding.cardInfo.visibility = View.GONE
        }
        
        // Bouton d'envoi
        binding.btnSendReset.setOnClickListener {
            if (validateInput()) {
                val email = binding.etEmail.text.toString().trim()
                sendResetEmail(email)
            }
        }
        
        // Retour à la connexion
        binding.tvBackToLogin.setOnClickListener {
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
    
    private fun sendResetEmail(email: String) {
        setLoadingState(true)
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Appel API réel pour envoyer le code de réinitialisation
                val result = authViewModel.sendPasswordResetEmail(email)
                
                setLoadingState(false)
                
                if (result.isSuccess) {
                    showInfoMessage(
                        "Code de réinitialisation envoyé à outailtrabelsi79@gmail.com\n" +
                        "Vérifiez votre boîte mail et utilisez le code reçu."
                    )
                    
                    // Naviguer vers l'écran de vérification du token avec l'email
                    val bundle = Bundle().apply {
                        putString("email", email)
                    }
                    findNavController().navigate(
                        R.id.action_forgot_password_to_reset_password,
                        bundle
                    )
                } else {
                    showError(result.exceptionOrNull()?.message ?: "Erreur lors de l'envoi de l'email")
                }
            } catch (e: Exception) {
                setLoadingState(false)
                showError("Erreur de connexion: ${e.message}")
            }
        }
    }
    
    private fun validateInput(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email requis"
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format d'email invalide"
            return false
        }
        
        return true
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        binding.btnSendReset.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        if (isLoading) {
            binding.btnSendReset.text = "Envoi en cours..."
        } else {
            binding.btnSendReset.text = "Envoyer le code"
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
    
    private fun showInfoMessage(message: String) {
        binding.tvInfoMessage.text = message
        binding.cardInfo.visibility = View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}