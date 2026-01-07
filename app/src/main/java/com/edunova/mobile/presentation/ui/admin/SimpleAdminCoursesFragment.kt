package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentSimpleAdminCoursesBinding

class SimpleAdminCoursesFragment : Fragment() {
    
    private var _binding: FragmentSimpleAdminCoursesBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleAdminCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupContent()
    }
    
    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupContent() {
        binding.textViewTitle.text = "Gestion des Cours"
        binding.textViewDescription.text = "Interface de gestion des cours EduNova"
        
        binding.buttonViewCourses.setOnClickListener {
            navigateToViewAllCourses()
        }
        
        binding.buttonManageEnrollments.setOnClickListener {
            navigateToManageEnrollments()
        }
        
        binding.buttonCourseSettings.setOnClickListener {
            navigateToCourseSettings()
        }
    }
    
    private fun navigateToViewAllCourses() {
        val fragment = AdminViewAllCoursesFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_main, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun navigateToManageEnrollments() {
        val fragment = AdminManageEnrollmentsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_main, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun navigateToCourseSettings() {
        val fragment = AdminCourseSettingsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_main, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}