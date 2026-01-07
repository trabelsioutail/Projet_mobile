package com.edunova.mobile.presentation.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.databinding.FragmentTeacherMessagesBinding
import com.edunova.mobile.presentation.adapter.ConversationAdapter
import com.edunova.mobile.presentation.viewmodel.MessageViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TeacherMessagesFragment : Fragment() {
    
    private var _binding: FragmentTeacherMessagesBinding? = null
    private val binding get() = _binding!!
    
    private val messageViewModel: MessageViewModel by viewModels()
    private lateinit var conversationAdapter: ConversationAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeConversations()
        setupSwipeRefresh()
        
        // Charger les conversations
        messageViewModel.loadConversations()
    }
    
    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter { conversation ->
            val action = TeacherMessagesFragmentDirections
                .actionMessagesToConversation(conversation.id)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewConversations.apply {
            adapter = conversationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun observeConversations() {
        messageViewModel.conversationsState.observe(viewLifecycleOwner) { resource ->
            _binding?.let { binding ->
                when (resource) {
                    is Resource.Loading<*> -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.swipeRefresh.isRefreshing = false
                    }
                    is Resource.Success<*> -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        conversationAdapter.submitList(resource.data as? List<com.edunova.mobile.domain.model.Conversation>)
                        
                        binding.emptyState.visibility = 
                            if (resource.data.isNullOrEmpty()) View.VISIBLE else View.GONE
                    }
                    is Resource.Error<*> -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        // TODO: Afficher l'erreur
                    }
                }
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            messageViewModel.loadConversationsWithState()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}