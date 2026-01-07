package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.databinding.FragmentAdminMessagesBinding
import com.edunova.mobile.presentation.viewmodel.MessageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminMessagesFragment : Fragment() {
    
    private var _binding: FragmentAdminMessagesBinding? = null
    private val binding get() = _binding!!
    
    private val messageViewModel: MessageViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            setupUI()
            setupClickListeners()
            loadMessages()
            
            Toast.makeText(requireContext(), "Messages admin chargés", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupUI() {
        binding.apply {
            textViewTitle.text = "💬 Messages et Communications"
            textViewSubtitle.text = "Gérer les conversations et notifications"
            
            // Configuration du RecyclerView (si nécessaire)
            recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            buttonNewMessage.setOnClickListener {
                showNewMessageDialog()
            }
            
            buttonBroadcast.setOnClickListener {
                showBroadcastDialog()
            }
            
            buttonNotifications.setOnClickListener {
                showNotificationsDialog()
            }
        }
    }
    
    private fun loadMessages() {
        // Simuler le chargement des messages
        binding.progressBar.visibility = View.VISIBLE
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Simuler un délai de chargement
                kotlinx.coroutines.delay(1000)
                
                binding.progressBar.visibility = View.GONE
                binding.textViewEmptyState.visibility = View.VISIBLE
                binding.textViewEmptyState.text = "📭 Aucun message pour le moment"
                
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Erreur de chargement: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showNewMessageDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("✉️ Nouveau message")
            .setMessage("Fonctionnalité de création de message à implémenter.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showBroadcastDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📢 Message de diffusion")
            .setMessage("Envoyer un message à tous les utilisateurs.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showNotificationsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔔 Notifications")
            .setMessage("Gérer les notifications système.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}