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
import com.edunova.mobile.databinding.FragmentLoginBinding
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import com.edunova.mobile.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by activityViewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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
        }
        
        binding.etPassword.addTextChangedListener {
            binding.tilPassword.error = null
        }
        
        // Bouton de connexion
        binding.btnLogin.setOnClickListener {
            if (validateInput()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString()
                authViewModel.login(email, password)
            }
        }
        
        // Lien vers l'inscription
        binding.tvRegisterLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        
        // Lien mot de passe oublié
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot_password)
        }
        
        // Connexion Google (à implémenter)
        binding.btnGoogleLogin.setOnClickListener {
            // TODO: Implémenter Google Sign-In
            showMessage("Connexion Google bientôt disponible")
        }
    }
    
    private fun observeViewModel() {
        // Observer l'état d'authentification
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        setLoadingState(true)
                    }
                    is Resource.Success -> {
                        setLoadingState(false)
                        // La navigation est gérée dans AuthActivity
                    }
                    is Resource.Error -> {
                        setLoadingState(false)
                        showError(resource.message ?: "Erreur de connexion")
                    }
                    null -> {
                        setLoadingState(false)
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
        
        // Observer les messages de succès
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.successMessage.collect { message ->
                message?.let {
                    showMessage(it)
                    authViewModel.clearMessages()
                }
            }
        }
    }
    
    private fun validateInput(): Boolean {
        var isValid = true
        
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        // Validation email
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.validation_required)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.validation_email_invalid)
            isValid = false
        }
        
        // Validation mot de passe
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.validation_required)
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.validation_password_min_length)
            isValid = false
        }
        
        return isValid
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnGoogleLogin.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        if (isLoading) {
            binding.btnLogin.text = getString(R.string.loading)
        } else {
            binding.btnLogin.text = getString(R.string.login)
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.error, null))
            .show()
    }
    
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success_color, null))
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}