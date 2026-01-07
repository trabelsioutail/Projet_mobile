package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentAdminCourseMenuBinding
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminCourseMenuFragment : Fragment() {
    
    private var _binding: FragmentAdminCourseMenuBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCourseMenuBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupClickListeners()
        loadData()
    }
    
    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonViewAllCourses.setOnClickListener {
            navigateToViewAllCourses()
        }
        
        binding.buttonManageEnrollments.setOnClickListener {
            navigateToManageEnrollments()
        }
        
        binding.buttonCourseSettings.setOnClickListener {
            navigateToCourseSettings()
        }
    }
    
    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        adminViewModel.loadAllCourses()
        
        // Observer les données des cours pour les statistiques de base
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.coursesState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        // Les données sont chargées avec succès
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Erreur de chargement: ${resource.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    private fun navigateToViewAllCourses() {
        try {
            // Navigation vers la nouvelle interface dynamique des cours
            val fragment = AdminCoursesListFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminCoursesList")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminCoursesListFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminCoursesList")
                    .commit()
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Erreur de navigation", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToManageEnrollments() {
        try {
            val fragment = AdminManageEnrollmentsFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminManageEnrollments")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminManageEnrollmentsFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminManageEnrollments")
                    .commit()
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Erreur de navigation", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToCourseSettings() {
        try {
            val fragment = AdminCourseSettingsFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminCourseSettings")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminCourseSettingsFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminCourseSettings")
                    .commit()
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Erreur de navigation", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}