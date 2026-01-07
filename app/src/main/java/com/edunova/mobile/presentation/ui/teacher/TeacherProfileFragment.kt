package com.edunova.mobile.presentation.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.edunova.mobile.databinding.FragmentTeacherProfileBinding
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TeacherProfileFragment : Fragment() {
    
    private var _binding: FragmentTeacherProfileBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        observeUser()
        setupClickListeners()
    }
    
    private fun observeUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authenticatedUser.collect { user ->
                user?.let {
                    binding.textViewName.text = "${it.firstName} ${it.lastName}"
                    binding.textViewEmail.text = it.email
                    binding.textViewRole.text = "Enseignant"
                    
                    // TODO: Charger l'avatar avec Glide
                    // Glide.with(this@TeacherProfileFragment)
                    //     .load(it.avatarUrl)
                    //     .placeholder(R.drawable.ic_person)
                    //     .into(binding.imageViewAvatar)
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonEditProfile.setOnClickListener {
            // TODO: Navigation vers édition du profil
        }
        
        binding.buttonSettings.setOnClickListener {
            // TODO: Navigation vers paramètres
        }
        
        binding.buttonLogout.setOnClickListener {
            authViewModel.logout()
        }
        
        binding.cardViewStats.setOnClickListener {
            // TODO: Navigation vers statistiques détaillées
        }
        
        binding.cardViewCourses.setOnClickListener {
            // TODO: Navigation vers mes cours
        }
        
        binding.cardViewStudents.setOnClickListener {
            // TODO: Navigation vers mes étudiants
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}