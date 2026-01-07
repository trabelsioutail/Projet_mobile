package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.edunova.mobile.databinding.FragmentQuizDetailBinding
import com.edunova.mobile.presentation.viewmodel.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizDetailFragment : Fragment() {
    
    private var _binding: FragmentQuizDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: QuizDetailFragmentArgs by navArgs()
    private val quizViewModel: QuizViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            setupUI()
            loadQuizDetails()
            
            Toast.makeText(requireContext(), "Détails du quiz ${args.quizId} chargés", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupUI() {
        // Configuration de base de l'interface
        binding.apply {
            // Titre temporaire
            // textViewQuizTitle.text = "Quiz ID: ${args.quizId}"
        }
    }
    
    private fun loadQuizDetails() {
        // Charger les détails du quiz
        // quizViewModel.loadQuizDetails(args.quizId)
        
        // Simuler le chargement
        Toast.makeText(requireContext(), "Chargement des détails du quiz...", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}