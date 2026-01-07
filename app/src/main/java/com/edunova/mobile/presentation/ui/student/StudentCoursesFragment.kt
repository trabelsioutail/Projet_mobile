package com.edunova.mobile.presentation.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentStudentCoursesBinding
import com.edunova.mobile.domain.model.Course
import com.edunova.mobile.presentation.adapter.CourseAdapter
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.CourseViewModel
import com.edunova.mobile.utils.Resource
import com.edunova.mobile.utils.collectSafely
import com.edunova.mobile.utils.gone
import com.edunova.mobile.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentCoursesFragment : BaseFragment<FragmentStudentCoursesBinding>() {
    
    private val courseViewModel: CourseViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter
    
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStudentCoursesBinding {
        return FragmentStudentCoursesBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupRecyclerView()
        setupFab()
        // Charger les cours immédiatement
        loadCourses()
    }
    
    private fun setupFab() {
        // Ajouter un bouton pour s'inscrire aux cours
        binding.fabAddCourse?.setOnClickListener {
            showAvailableCoursesDialog()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Recharger les cours quand on revient sur ce fragment
        loadCourses()
    }
    
    override fun observeData() {
        // Observer les cours de l'étudiant
        courseViewModel.coursesState.collectSafely(viewLifecycleOwner) { resource ->
            binding.swipeRefreshLayout.isRefreshing = false
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.recyclerViewCourses.gone()
                    binding.textViewEmpty.gone()
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    val courses = resource.data ?: emptyList()
                    if (courses.isEmpty()) {
                        binding.recyclerViewCourses.gone()
                        binding.textViewEmpty.visible()
                        binding.textViewEmpty.text = "Aucun cours inscrit"
                    } else {
                        binding.recyclerViewCourses.visible()
                        binding.textViewEmpty.gone()
                        courseAdapter.submitList(courses)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.recyclerViewCourses.gone()
                    binding.textViewEmpty.visible()
                    binding.textViewEmpty.text = resource.message ?: "Erreur de chargement"
                }
                null -> {
                    // État initial
                }
            }
        }
        
        // Observer les erreurs
        courseViewModel.errorMessage.collectSafely(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                courseViewModel.clearErrorMessage()
            }
        }
    }
    
    override fun setupListeners() {
        safeWithBinding { binding ->
            binding.swipeRefreshLayout.setOnRefreshListener {
                loadCourses(forceRefresh = true)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(
            onCourseClick = { course ->
                navigateToCourseDetail(course)
            }
        )
        
        binding.recyclerViewCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = courseAdapter
        }
    }
    
    private fun loadCourses(forceRefresh: Boolean = false) {
        courseViewModel.loadStudentCourses(forceRefresh)
    }
    
    private fun navigateToCourseDetail(course: Course) {
        val action = StudentCoursesFragmentDirections
            .actionCoursesToCourseDetail(course.id)
        findNavController().navigate(action)
    }
    
    private fun showAvailableCoursesDialog() {
        val availableCourses = listOf(
            Course(
                id = 1,
                title = "Mathématiques Avancées",
                description = "Cours de mathématiques niveau universitaire avec algèbre et analyse",
                teacherId = 1,
                teacherName = "Prof. Ghofrane Sebteoui",
                isEnrolled = false,
                status = "active",
                isPublic = true,
                enrollmentOpen = true
            ),
            Course(
                id = 2,
                title = "Programmation Web",
                description = "HTML, CSS, JavaScript et frameworks modernes (React, Vue.js)",
                teacherId = 1,
                teacherName = "Prof. Ghofrane Sebteoui",
                isEnrolled = false,
                status = "active",
                isPublic = true,
                enrollmentOpen = true
            ),
            Course(
                id = 3,
                title = "Base de Données",
                description = "SQL, MySQL et conception de bases de données relationnelles",
                teacherId = 1,
                teacherName = "Prof. Ghofrane Sebteoui",
                isEnrolled = false,
                status = "active",
                isPublic = true,
                enrollmentOpen = true
            ),
            Course(
                id = 4,
                title = "Intelligence Artificielle",
                description = "Machine Learning, Deep Learning et applications pratiques",
                teacherId = 1,
                teacherName = "Prof. Ghofrane Sebteoui",
                isEnrolled = false,
                status = "active",
                isPublic = true,
                enrollmentOpen = true
            )
        )
        
        val courseNames = availableCourses.map { "${it.title} - ${it.teacherName}" }.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cours disponibles")
            .setItems(courseNames) { _, which ->
                val selectedCourse = availableCourses[which]
                enrollInCourse(selectedCourse)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun enrollInCourse(course: Course) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Inscription au cours")
            .setMessage("Voulez-vous vous inscrire au cours \"${course.title}\" ?")
            .setPositiveButton("S'inscrire") { _, _ ->
                // Simuler l'inscription
                android.widget.Toast.makeText(
                    requireContext(),
                    "Inscription réussie au cours \"${course.title}\" !",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                
                // Recharger les cours
                loadCourses(true)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showErrorMessage(message: String) {
        safeWithBinding { binding ->
            binding.swipeRefreshLayout.isRefreshing = false
            // TODO: Ajouter un Snackbar ici si nécessaire
        }
    }
}