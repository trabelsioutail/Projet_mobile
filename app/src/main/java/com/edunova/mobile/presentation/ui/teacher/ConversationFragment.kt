package com.edunova.mobile.presentation.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.edunova.mobile.databinding.FragmentConversationBinding
import com.edunova.mobile.presentation.viewmodel.MessageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConversationFragment : Fragment() {
    
    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!
    
    private val args: ConversationFragmentArgs by navArgs()
    private val messageViewModel: MessageViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // TODO: Implémenter la conversation
        messageViewModel.loadMessages(args.conversationId)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}