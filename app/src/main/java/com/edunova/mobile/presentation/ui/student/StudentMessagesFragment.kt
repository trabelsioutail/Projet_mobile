package com.edunova.mobile.presentation.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentStudentMessagesBinding
import com.edunova.mobile.domain.model.Conversation
import com.edunova.mobile.presentation.adapter.ConversationAdapter
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.MessageViewModel
import com.edunova.mobile.utils.Resource
import com.edunova.mobile.utils.collectSafely
import com.edunova.mobile.utils.gone
import com.edunova.mobile.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentMessagesFragment : BaseFragment<FragmentStudentMessagesBinding>() {
    
    private val messageViewModel: MessageViewModel by viewModels()
    private lateinit var conversationAdapter: ConversationAdapter
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStudentMessagesBinding {
        return FragmentStudentMessagesBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupRecyclerView()
        loadConversations()
    }
    
    override fun observeData() {
        // Observer les conversations de l'étudiant
        messageViewModel.conversationsState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.recyclerViewConversations.gone()
                    binding.textViewEmpty.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    val conversations = resource.data ?: emptyList()
                    if (conversations.isEmpty()) {
                        binding.recyclerViewConversations.gone()
                        binding.textViewEmpty.visible()
                        binding.textViewEmpty.text = "Aucune conversation"
                    } else {
                        binding.recyclerViewConversations.visible()
                        binding.textViewEmpty.gone()
                        conversationAdapter.submitList(conversations)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.recyclerViewConversations.gone()
                    binding.textViewEmpty.visible()
                    binding.textViewEmpty.text = resource.message ?: "Erreur de chargement"
                }
            }
        }
        
        // Observer les erreurs
        messageViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                // Afficher l'erreur
                messageViewModel.clearErrorMessage()
            }
        }
    }
    
    override fun setupListeners() {
        safeWithBinding { binding ->
            binding.swipeRefreshLayout.setOnRefreshListener {
                loadConversations(forceRefresh = true)
            }
            
            binding.fabNewMessage.setOnClickListener {
                navigateToNewMessage()
            }
        }
    }
    
    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(
            onConversationClick = { conversation ->
                navigateToConversation(conversation)
            }
        )
        
        binding.recyclerViewConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conversationAdapter
        }
    }
    
    private fun loadConversations(forceRefresh: Boolean = false) {
        messageViewModel.loadConversationsWithState()
    }
    
    private fun navigateToConversation(conversation: Conversation) {
        // Afficher un dialog avec les messages de la conversation
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Conversation avec ${conversation.participantName}")
            .setMessage("Messages :\n\n• ${conversation.lastMessage}\n• Vous pouvez maintenant voir les conversations\n• Envoyer des messages\n• Recevoir des réponses")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }
    
    private fun navigateToNewMessage() {
        // Afficher un dialog pour créer un nouveau message
        val teachers = arrayOf(
            "Prof. Ghofrane Sebteoui (ghofrane.sebteoui@edunova.tn)",
            "Prof. Martin Dubois", 
            "Prof. Sophie Laurent", 
            "Prof. Ahmed Khalil"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Nouveau message")
            .setItems(teachers) { _, which ->
                val selectedTeacher = teachers[which]
                showMessageDialog(selectedTeacher)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showMessageDialog(teacherName: String) {
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "Tapez votre message..."
            setPadding(32, 32, 32, 32)
            minLines = 3
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Message à $teacherName")
            .setView(editText)
            .setPositiveButton("Envoyer") { _, _ ->
                val message = editText.text.toString()
                if (message.isNotBlank()) {
                    // Simuler l'envoi avec confirmation
                    android.widget.Toast.makeText(
                        requireContext(), 
                        "Message envoyé à $teacherName !\n\nVotre message: \"$message\"", 
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    
                    // Recharger les conversations
                    loadConversations(true)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showErrorMessage(message: String) {
        safeWithBinding { binding ->
            binding.swipeRefreshLayout.isRefreshing = false
            // TODO: Ajouter un Snackbar ici si nécessaire
        }
    }
}