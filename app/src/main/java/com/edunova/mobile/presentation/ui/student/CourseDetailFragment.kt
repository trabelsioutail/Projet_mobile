package com.edunova.mobile.presentation.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.databinding.FragmentCourseDetailStudentBinding
import com.edunova.mobile.domain.model.Course
import com.edunova.mobile.presentation.adapter.CourseContentAdapter
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.CourseViewModel
import com.edunova.mobile.utils.Resource
import com.edunova.mobile.utils.collectSafely
import com.edunova.mobile.utils.gone
import com.edunova.mobile.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CourseDetailFragment : BaseFragment<FragmentCourseDetailStudentBinding>() {
    
    private val courseViewModel: CourseViewModel by viewModels()
    private val args: CourseDetailFragmentArgs by navArgs()
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCourseDetailStudentBinding {
        return FragmentCourseDetailStudentBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupToolbar()
        loadCourseDetails()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun loadCourseDetails() {
        courseViewModel.loadCourse(args.courseId)
    }
    
    override fun observeData() {
        courseViewModel.selectedCourse.collectSafely(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.layoutContent.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.layoutContent.visible()
                    resource.data?.let { displayCourseDetails(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    android.widget.Toast.makeText(
                        requireContext(),
                        resource.message ?: "Erreur de chargement",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                null -> {
                    // État initial
                }
            }
        }
        
        // Observer les messages de succès
        courseViewModel.successMessage.collectSafely(viewLifecycleOwner) { message ->
            message?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_LONG).show()
                courseViewModel.clearMessages()
                // Recharger le cours pour mettre à jour l'état
                courseViewModel.loadCourse(args.courseId)
            }
        }
    }
    
    private fun displayCourseDetails(course: Course) {
        binding.apply {
            progressBar.gone()
            layoutContent.visible()
            
            textViewTitle.text = course.title
            textViewDescription.text = course.description ?: "Aucune description disponible"
            textViewTeacher.text = "Enseignant: ${course.teacherName ?: "Non spécifié"}"
            textViewCreatedAt.text = "Créé le: ${course.createdAt}"
            textViewStudentsCount.text = "${course.studentsCount} étudiants inscrits"
            
            // Simuler du contenu de cours
            val mockContents = createMockContentText(course.id)
            
            // Pour cette version simplifiée, on affiche juste du texte
            // TODO: Implémenter le RecyclerView avec CourseContentAdapter
            
            // Bouton d'inscription/désinscription avec action réelle
            updateEnrollmentButton(course)
        }
    }
    
    private fun updateEnrollmentButton(course: Course) {
        binding.apply {
            if (course.isEnrolled) {
                buttonEnroll.text = "Se désinscrire"
                buttonEnroll.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark, null))
                buttonEnroll.setOnClickListener {
                    showUnenrollConfirmation(course)
                }
            } else {
                buttonEnroll.text = "S'inscrire"
                buttonEnroll.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, null))
                buttonEnroll.setOnClickListener {
                    enrollInCourse(course)
                }
            }
        }
    }
    
    private fun showUnenrollConfirmation(course: Course) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Désinscription")
            .setMessage("Êtes-vous sûr de vouloir vous désinscrire du cours \"${course.title}\" ?")
            .setPositiveButton("Se désinscrire") { _, _ ->
                unenrollFromCourse(course)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun enrollInCourse(course: Course) {
        courseViewModel.enrollInCourse(course.id)
    }
    
    private fun unenrollFromCourse(course: Course) {
        courseViewModel.unenrollFromCourse(course.id)
    }
    
    private fun createMockContentText(courseId: Int): String {
        return when (courseId) {
            1 -> "• Introduction aux Mathématiques\n• Équations du Second Degré\n• Exercices Pratiques"
            2 -> "• HTML et CSS de Base\n• JavaScript Fondamentaux\n• Projet Final"
            3 -> "• Introduction aux Bases de Données\n• SQL Avancé\n• Conception de Schémas"
            else -> "• Contenu du cours\n• Exercices\n• Évaluation"
        }
    }
    
    override fun setupListeners() {
        // Les listeners sont configurés dans displayCourseDetails()
    }
}