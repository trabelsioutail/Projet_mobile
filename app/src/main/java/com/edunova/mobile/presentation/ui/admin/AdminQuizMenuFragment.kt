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
import com.edunova.mobile.databinding.FragmentAdminQuizMenuBinding
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminQuizMenuFragment : Fragment() {
    
    private var _binding: FragmentAdminQuizMenuBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminQuizMenuBinding.inflate(inflater, container, false)
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
        binding.buttonViewAllQuizzes.setOnClickListener {
            navigateToViewAllQuizzes()
        }
        
        binding.buttonCreateQuiz.setOnClickListener {
            navigateToCreateQuiz()
        }
        
        binding.buttonQuizStatistics.setOnClickListener {
            navigateToQuizStatistics()
        }
    }
    
    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        adminViewModel.loadAllQuizzes()
        
        // Observer les données des quiz pour les statistiques de base
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.quizzesState.collect { resource ->
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
    
    private fun navigateToViewAllQuizzes() {
        try {
            // Navigation vers la nouvelle interface simplifiée des quiz
            val fragment = SimpleAdminQuizzesFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminQuizzesList")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = SimpleAdminQuizzesFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminQuizzesList")
                    .commit()
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Erreur de navigation: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun navigateToCreateQuiz() {
        try {
            val fragment = AdminCreateQuizFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminCreateQuiz")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminCreateQuizFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminCreateQuiz")
                    .commit()
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Erreur de navigation: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun navigateToQuizStatistics() {
        try {
            val fragment = SimpleAdminQuizzesFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminQuizStatistics")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = SimpleAdminQuizzesFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminQuizStatistics")
                    .commit()
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Erreur de navigation: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}