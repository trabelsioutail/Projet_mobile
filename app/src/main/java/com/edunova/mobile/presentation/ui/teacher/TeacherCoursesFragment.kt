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
import com.edunova.mobile.databinding.FragmentTeacherCoursesBinding
import com.edunova.mobile.presentation.adapter.CourseAdapter
import com.edunova.mobile.presentation.viewmodel.CourseViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TeacherCoursesFragment : Fragment() {
    
    private var _binding: FragmentTeacherCoursesBinding? = null
    private val binding get() = _binding!!
    
    private val courseViewModel: CourseViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        observeCourses()
        loadCourses()
    }
    
    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter { course ->
            // Navigation vers les détails du cours
            val action = TeacherCoursesFragmentDirections
                .actionCoursesToCourseDetail(course.id)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = courseAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonAddCourse.setOnClickListener {
            val action = TeacherCoursesFragmentDirections
                .actionCoursesToCreateCourse()
            findNavController().navigate(action)
        }
        
        binding.buttonRetry.setOnClickListener {
            loadCourses()
        }
    }
        
    private fun observeCourses() {
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.coursesFlow.collect { courses ->
                // Vérifier que le binding existe encore
                if (_binding != null) {
                    courseAdapter.submitList(courses)
                    
                    if (courses.isEmpty()) {
                        showEmptyState()
                    } else {
                        showContent()
                    }
                }
            }
        }
        
        // Observer aussi l'état de chargement
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.coursesState.collect { resource ->
                // Vérifier que le binding existe encore
                if (_binding != null) {
                    when (resource) {
                        is Resource.Loading -> {
                            showLoading()
                        }
                        is Resource.Error -> {
                            showError(resource.message ?: "Erreur inconnue")
                        }
                        is Resource.Success -> {
                            // Success géré par coursesFlow
                        }
                        null -> {
                            // État initial
                        }
                    }
                }
            }
        }
    }
    
    private fun loadCourses() {
        courseViewModel.loadCourses(forceRefresh = true)
    }
    
    private fun showLoading() {
        _binding?.let { binding ->
            binding.recyclerViewCourses.visibility = View.GONE
            binding.layoutStatus.visibility = View.VISIBLE
            binding.textStatus.text = "Chargement des cours..."
            binding.buttonRetry.visibility = View.GONE
        }
    }
    
    private fun showContent() {
        _binding?.let { binding ->
            binding.recyclerViewCourses.visibility = View.VISIBLE
            binding.layoutStatus.visibility = View.GONE
        }
    }
    
    private fun showEmptyState() {
        _binding?.let { binding ->
            binding.recyclerViewCourses.visibility = View.GONE
            binding.layoutStatus.visibility = View.VISIBLE
            binding.textStatus.text = "Aucun cours trouvé\n\nCommencez par créer votre premier cours !"
            binding.buttonRetry.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        _binding?.let { binding ->
            binding.recyclerViewCourses.visibility = View.GONE
            binding.layoutStatus.visibility = View.VISIBLE
            binding.textStatus.text = "Erreur: $message"
            binding.buttonRetry.visibility = View.VISIBLE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}