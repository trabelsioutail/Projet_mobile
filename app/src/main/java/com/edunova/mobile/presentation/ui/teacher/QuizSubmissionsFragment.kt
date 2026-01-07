package com.edunova.mobile.presentation.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.edunova.mobile.databinding.FragmentQuizSubmissionsBinding
import com.edunova.mobile.presentation.viewmodel.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizSubmissionsFragment : Fragment() {
    
    private var _binding: FragmentQuizSubmissionsBinding? = null
    private val binding get() = _binding!!
    
    private val args: QuizSubmissionsFragmentArgs by navArgs()
    private val quizViewModel: QuizViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizSubmissionsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // TODO: Implémenter les soumissions du quiz
        // Méthode temporairement désactivée car non implémentée dans le repository
        // quizViewModel.loadMySubmissions()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}