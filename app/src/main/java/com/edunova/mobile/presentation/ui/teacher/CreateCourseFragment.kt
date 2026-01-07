package com.edunova.mobile.presentation.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.edunova.mobile.databinding.FragmentCreateCourseBinding
import com.edunova.mobile.presentation.viewmodel.CourseViewModel
import com.edunova.mobile.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateCourseFragment : Fragment() {
    
    private var _binding: FragmentCreateCourseBinding? = null
    private val binding get() = _binding!!
    
    private val args: CreateCourseFragmentArgs by navArgs()
    private val courseViewModel: CourseViewModel by viewModels()
    
    private var isEditMode = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateCourseBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        isEditMode = args.courseId != -1
        
        setupUI()
        setupValidation()
        setupClickListeners()
        observeViewModel()
        
        if (isEditMode) {
            loadCourseForEdit()
        }
    }
    
    private fun setupUI() {
        binding.apply {
            if (isEditMode) {
                tvTitle.text = "Modifier le Cours"
                btnSave.text = "Mettre à Jour"
            } else {
                tvTitle.text = "Nouveau Cours"
                btnSave.text = "Créer le Cours"
            }
        }
    }
    
    private fun setupValidation() {
        binding.apply {
            etTitle.addTextChangedListener {
                tilTitle.error = null
                validateForm()
            }
            
            etDescription.addTextChangedListener {
                tilDescription.error = null
                validateForm()
            }
        }
        
        setupDropdowns()
    }
    
    private fun setupDropdowns() {
        // Configuration des catégories
        val categories = arrayOf(
            "Développement Web",
            "Développement Mobile",
            "Intelligence Artificielle",
            "Cybersécurité",
            "Base de Données",
            "Réseaux",
            "Design UI/UX",
            "Marketing Digital",
            "Gestion de Projet",
            "Langues",
            "Sciences",
            "Mathématiques",
            "Autre"
        )
        
        val categoryAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        binding.etCategory.setAdapter(categoryAdapter)
        
        // Configuration des niveaux de difficulté
        val levels = arrayOf(
            "Débutant",
            "Intermédiaire", 
            "Avancé",
            "Expert"
        )
        
        val levelAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            levels
        )
        binding.etLevel.setAdapter(levelAdapter)
        
        // Valeurs par défaut
        binding.etCategory.setText(categories[0], false)
        binding.etLevel.setText(levels[0], false)
    }
    
    private fun setupClickListeners() {
        binding.apply {
            btnSave.setOnClickListener {
                if (validateForm()) {
                    saveCourse()
                }
            }
            
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
            
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.selectedCourse.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                        
                        if (isEditMode) {
                            resource.data?.let { course ->
                                populateFields(course)
                            }
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                        Snackbar.make(binding.root, resource.message ?: "Erreur", Snackbar.LENGTH_LONG).show()
                    }
                    null -> {}
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.successMessage.collect { message ->
                message?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.errorMessage.collect { message ->
                message?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.isLoading.collect { isLoading ->
                binding.btnSave.isEnabled = !isLoading
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun loadCourseForEdit() {
        courseViewModel.loadCourseDetails(args.courseId)
    }
    
    private fun populateFields(course: com.edunova.mobile.domain.model.Course) {
        binding.apply {
            etTitle.setText(course.title)
            etDescription.setText(course.description)
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        binding.apply {
            // Validation du titre
            if (etTitle.text.toString().trim().isEmpty()) {
                tilTitle.error = "Le titre est requis"
                isValid = false
            } else if (etTitle.text.toString().trim().length < 3) {
                tilTitle.error = "Le titre doit contenir au moins 3 caractères"
                isValid = false
            }
            
            // Validation de la description
            if (etDescription.text.toString().trim().isEmpty()) {
                tilDescription.error = "La description est requise"
                isValid = false
            } else if (etDescription.text.toString().trim().length < 10) {
                tilDescription.error = "La description doit contenir au moins 10 caractères"
                isValid = false
            }
            
            btnSave.isEnabled = isValid
        }
        
        return isValid
    }
    
    private fun saveCourse() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        
        if (isEditMode) {
            courseViewModel.updateCourse(args.courseId, title, description)
        } else {
            courseViewModel.createCourse(title, description)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}