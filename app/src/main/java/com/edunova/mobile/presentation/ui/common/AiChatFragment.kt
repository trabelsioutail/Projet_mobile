package com.edunova.mobile.presentation.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.databinding.FragmentAiChatBinding
import com.edunova.mobile.presentation.adapter.AiChatAdapter
import com.edunova.mobile.presentation.adapter.AiSuggestionAdapter
import com.edunova.mobile.presentation.viewmodel.AiChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiChatFragment : Fragment() {
    
    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!
    
    private val aiChatViewModel: AiChatViewModel by viewModels()
    private lateinit var chatAdapter: AiChatAdapter
    private lateinit var suggestionAdapter: AiSuggestionAdapter
    
    private var userRole: String = "etudiant"
    
    companion object {
        private const val ARG_USER_ROLE = "user_role"
        
        fun newInstance(userRole: String): AiChatFragment {
            val fragment = AiChatFragment()
            val args = Bundle()
            args.putString(ARG_USER_ROLE, userRole)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRole = arguments?.getString(ARG_USER_ROLE) ?: "etudiant"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerViews()
        observeViewModel()
        
        // Initialize chat with user role
        aiChatViewModel.initializeChat(userRole)
    }
    
    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.buttonClearChat.setOnClickListener {
            showClearChatDialog()
        }
        
        binding.buttonSend.setOnClickListener {
            sendMessage()
        }
        
        binding.editTextMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
        
        // Enhanced status based on role and time
        updateAiStatus()
    }
    
    private fun updateAiStatus() {
        val timeOfDay = getTimeOfDay()
        val statusText = when (userRole) {
            "admin" -> "Assistant Administratif • $timeOfDay • En ligne"
            "enseignant" -> "Assistant Pédagogique • $timeOfDay • En ligne"
            "etudiant" -> "Assistant d'Apprentissage • $timeOfDay • En ligne"
            else -> "Assistant IA • $timeOfDay • En ligne"
        }
        binding.textViewAiStatus.text = statusText
    }
    
    private fun getTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Matinée"
            in 12..17 -> "Après-midi"
            in 18..22 -> "Soirée"
            else -> "Nuit"
        }
    }
    
    private fun showClearChatDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Effacer la conversation")
            .setMessage("Êtes-vous sûr de vouloir effacer toute la conversation ? Cette action ne peut pas être annulée.")
            .setPositiveButton("Effacer") { _, _ ->
                aiChatViewModel.clearChat()
                Toast.makeText(requireContext(), "Conversation effacée", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun setupRecyclerViews() {
        // Chat messages
        chatAdapter = AiChatAdapter { suggestion ->
            aiChatViewModel.sendMessage(suggestion)
        }
        
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
        
        // Suggestions
        suggestionAdapter = AiSuggestionAdapter { suggestion ->
            aiChatViewModel.sendSuggestion(suggestion)
        }
        
        binding.recyclerViewSuggestions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = suggestionAdapter
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            aiChatViewModel.messages.collect { messages ->
                chatAdapter.submitList(messages) {
                    if (messages.isNotEmpty()) {
                        binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            aiChatViewModel.suggestions.collect { suggestions ->
                suggestionAdapter.submitList(suggestions)
                binding.recyclerViewSuggestions.visibility = if (suggestions.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            aiChatViewModel.isLoading.collect { isLoading ->
                binding.layoutLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.buttonSend.isEnabled = !isLoading
                binding.editTextMessage.isEnabled = !isLoading
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            aiChatViewModel.error.collect { error ->
                error?.let {
                    showErrorDialog(it)
                    aiChatViewModel.clearError()
                }
            }
        }
    }
    
    private fun showErrorDialog(error: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Erreur de communication")
            .setMessage("Une erreur s'est produite : $error\n\nVoulez-vous réessayer ?")
            .setPositiveButton("Réessayer") { _, _ ->
                // Retry last message if possible
                val lastUserMessage = aiChatViewModel.messages.value.lastOrNull { it.isFromUser }
                lastUserMessage?.let { message ->
                    aiChatViewModel.sendMessage(message.content)
                }
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun sendMessage() {
        val message = binding.editTextMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            aiChatViewModel.sendMessage(message)
            binding.editTextMessage.text?.clear()
            
            // Hide keyboard after sending
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.editTextMessage.windowToken, 0)
            
            // Scroll to bottom to show new message
            binding.recyclerViewMessages.post {
                if (aiChatViewModel.messages.value.isNotEmpty()) {
                    binding.recyclerViewMessages.smoothScrollToPosition(aiChatViewModel.messages.value.size - 1)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}