package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.edunova.mobile.databinding.FragmentCourseDetailBinding
import com.edunova.mobile.presentation.viewmodel.CourseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourseDetailFragment : Fragment() {
    
    private var _binding: FragmentCourseDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: CourseDetailFragmentArgs by navArgs()
    private val courseViewModel: CourseViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            setupUI()
            loadCourseDetails()
            
            Toast.makeText(requireContext(), "Détails du cours ${args.courseId} chargés", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupUI() {
        // Configuration de base de l'interface
        binding.apply {
            // Titre temporaire
            // textViewCourseTitle.text = "Cours ID: ${args.courseId}"
        }
    }
    
    private fun loadCourseDetails() {
        // Charger les détails du cours
        // courseViewModel.loadCourseDetails(args.courseId)
        
        // Simuler le chargement
        Toast.makeText(requireContext(), "Chargement des détails du cours...", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}