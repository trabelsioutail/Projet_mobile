package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentAdminViewAllCoursesBinding
import com.edunova.mobile.data.repository.AdminCourse
import com.edunova.mobile.data.repository.AdminTeacher
import com.edunova.mobile.presentation.adapter.AdminCoursesAdapter
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminViewAllCoursesFragment : Fragment() {
    
    private var _binding: FragmentAdminViewAllCoursesBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var coursesAdapter: AdminCoursesAdapter
    private var allCourses: List<AdminCourse> = emptyList()
    private var teachers: List<AdminTeacher> = emptyList()
    private var currentFilter = "all"
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminViewAllCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        setupFilters()
        observeData()
        loadCourses()
        loadTeachers()
    }
    
    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.fabAddCourse.setOnClickListener {
            showAddCourseDialog()
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadCourses()
        }
    }
    
    private fun setupRecyclerView() {
        coursesAdapter = AdminCoursesAdapter(
            onViewCourse = { course -> showCourseDetails(course) },
            onEditCourse = { course -> showEditCourseDialog(course) },
            onDeleteCourse = { course -> showDeleteCourseDialog(course) },
            onToggleCourseStatus = { course -> toggleCourseStatus(course) },
            onManageEnrollments = { course -> manageCourseEnrollments(course) },
            onViewStatistics = { course -> viewCourseStatistics(course) }
        )
        
        binding.recyclerViewCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = coursesAdapter
        }
    }
    
    private fun observeData() {
        // Observer courses list
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.coursesState.collect { resource ->
                binding.swipeRefreshLayout.isRefreshing = false
                
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.textViewError.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.textViewError.visibility = View.GONE
                        
                        resource.data?.let { courses ->
                            allCourses = courses
                            coursesAdapter.submitList(courses)
                            updateStats(courses)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.textViewError.visibility = View.VISIBLE
                        binding.textViewError.text = "Erreur: ${resource.message}"
                        
                        Toast.makeText(requireContext(), 
                            "Erreur de chargement: ${resource.message}", 
                            Toast.LENGTH_LONG).show()
                    }
                    null -> {
                        // Initial state
                    }
                }
            }
        }
        
        // Observer course actions
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.courseActionState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT).show()
                        loadCourses() // Refresh list
                        adminViewModel.clearCourseActionState()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), 
                            "Erreur: ${resource.message}", 
                            Toast.LENGTH_LONG).show()
                        adminViewModel.clearCourseActionState()
                    }
                    else -> {
                        // Loading or null state
                    }
                }
            }
        }
        
        // Observer teachers list
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.teachersState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { teachersList ->
                            teachers = teachersList
                        }
                    }
                    else -> {
                        // Handle loading/error states if needed
                    }
                }
            }
        }
    }
    
    private fun loadCourses() {
        adminViewModel.loadAllCourses()
    }
    
    private fun filterActiveCourses() {
        currentFilter = "active"
        val activeCourses = allCourses.filter { it.status == "active" }
        coursesAdapter.submitList(activeCourses)
        updateStats(activeCourses)
    }
    
    private fun filterInactiveCourses() {
        currentFilter = "inactive"
        val inactiveCourses = allCourses.filter { it.status == "inactive" }
        coursesAdapter.submitList(inactiveCourses)
        updateStats(inactiveCourses)
    }
    
    private fun filterPendingCourses() {
        currentFilter = "pending"
        val pendingCourses = allCourses.filter { it.status == "pending" }
        coursesAdapter.submitList(pendingCourses)
        updateStats(pendingCourses)
    }
    
    private fun showAllCourses() {
        currentFilter = "all"
        coursesAdapter.submitList(allCourses)
        updateStats(allCourses)
    }
    
    private fun setupFilters() {
        // Clear any existing listeners to prevent conflicts
        binding.chipAllCourses.setOnClickListener(null)
        binding.chipActiveCourses.setOnClickListener(null)
        binding.chipInactiveCourses.setOnClickListener(null)
        binding.chipPendingCourses.setOnClickListener(null)
        
        // Set up clean listeners
        binding.chipAllCourses.setOnClickListener { 
            if (currentFilter != "all") {
                showAllCourses()
                updateChipSelection("all")
            }
        }
        binding.chipActiveCourses.setOnClickListener { 
            if (currentFilter != "active") {
                filterActiveCourses()
                updateChipSelection("active")
            }
        }
        binding.chipInactiveCourses.setOnClickListener { 
            if (currentFilter != "inactive") {
                filterInactiveCourses()
                updateChipSelection("inactive")
            }
        }
        binding.chipPendingCourses.setOnClickListener { 
            if (currentFilter != "pending") {
                filterPendingCourses()
                updateChipSelection("pending")
            }
        }
        
        // Set initial selection
        updateChipSelection("all")
    }
    
    private fun updateChipSelection(selectedFilter: String) {
        // Reset all chips
        binding.chipAllCourses.isChecked = false
        binding.chipActiveCourses.isChecked = false
        binding.chipInactiveCourses.isChecked = false
        binding.chipPendingCourses.isChecked = false
        
        // Set selected chip
        when (selectedFilter) {
            "all" -> binding.chipAllCourses.isChecked = true
            "active" -> binding.chipActiveCourses.isChecked = true
            "inactive" -> binding.chipInactiveCourses.isChecked = true
            "pending" -> binding.chipPendingCourses.isChecked = true
        }
    }
    
    private fun loadTeachers() {
        adminViewModel.loadAllTeachers()
    }
    
    private fun updateStats(courses: List<AdminCourse>) {
        // Calculate stats based on currently displayed courses (filtered data)
        val displayedCourses = courses.size
        val displayedEnrollments = courses.sumOf { it.enrollmentCount }
        val displayedActiveCourses = courses.count { it.status == "active" }
        
        // Calculate global stats from all courses for reference
        val totalCourses = allCourses.size
        val totalEnrollments = allCourses.sumOf { it.enrollmentCount }
        val totalActiveCourses = allCourses.count { it.status == "active" }
        
        // Show filtered stats (what user is currently viewing)
        binding.textViewTotalCourses.text = displayedCourses.toString()
        binding.textViewTotalEnrollments.text = displayedEnrollments.toString()
        binding.textViewActiveCourses.text = displayedActiveCourses.toString()
    }
    
    private fun showAddCourseDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_course, null)
        
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val spinnerTeacher = dialogView.findViewById<Spinner>(R.id.spinnerTeacher)
        val radioGroupStatus = dialogView.findViewById<RadioGroup>(R.id.radioGroupStatus)
        val checkBoxPublic = dialogView.findViewById<CheckBox>(R.id.checkBoxPublic)
        val checkBoxEnrollmentOpen = dialogView.findViewById<CheckBox>(R.id.checkBoxEnrollmentOpen)
        
        // Setup teacher spinner with real teachers
        val teacherNames = teachers.map { "${it.firstName} ${it.lastName}" }
        val teacherAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            teacherNames
        )
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTeacher.adapter = teacherAdapter
        
        AlertDialog.Builder(requireContext())
            .setTitle("➕ Ajouter un cours")
            .setView(dialogView)
            .setPositiveButton("Créer") { _, _ ->
                val title = editTextTitle.text.toString().trim()
                val description = editTextDescription.text.toString().trim()
                val selectedTeacherIndex = spinnerTeacher.selectedItemPosition
                
                // Get status from radio group
                val status = when (radioGroupStatus.checkedRadioButtonId) {
                    R.id.radioButtonActive -> "active"
                    R.id.radioButtonInactive -> "inactive"
                    R.id.radioButtonPending -> "pending"
                    else -> "active"
                }
                
                val isPublic = checkBoxPublic.isChecked
                val enrollmentOpen = checkBoxEnrollmentOpen.isChecked
                
                if (selectedTeacherIndex >= 0 && selectedTeacherIndex < teachers.size) {
                    val teacherId = teachers[selectedTeacherIndex].id
                    
                    if (validateCourseInput(title, description)) {
                        adminViewModel.createCourse(title, description, teacherId, status, isPublic, enrollmentOpen)
                    }
                } else {
                    Toast.makeText(requireContext(), "Veuillez sélectionner un enseignant", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showEditCourseDialog(course: AdminCourse) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_course, null)
        
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val editTextDescription = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val spinnerTeacher = dialogView.findViewById<Spinner>(R.id.spinnerTeacher)
        val radioGroupStatus = dialogView.findViewById<RadioGroup>(R.id.radioGroupStatus)
        val radioButtonActive = dialogView.findViewById<RadioButton>(R.id.radioButtonActive)
        val radioButtonInactive = dialogView.findViewById<RadioButton>(R.id.radioButtonInactive)
        val radioButtonPending = dialogView.findViewById<RadioButton>(R.id.radioButtonPending)
        val checkBoxPublic = dialogView.findViewById<CheckBox>(R.id.checkBoxPublic)
        val checkBoxEnrollmentOpen = dialogView.findViewById<CheckBox>(R.id.checkBoxEnrollmentOpen)
        
        // Pre-fill fields
        editTextTitle.setText(course.title)
        editTextDescription.setText(course.description)
        
        // Setup teacher spinner with real teachers
        val teacherNames = teachers.map { "${it.firstName} ${it.lastName}" }
        val teacherAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            teacherNames
        )
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTeacher.adapter = teacherAdapter
        
        // Pre-select current teacher
        val currentTeacherIndex = teachers.indexOfFirst { it.id == course.teacherId }
        if (currentTeacherIndex >= 0) {
            spinnerTeacher.setSelection(currentTeacherIndex)
        }
        
        // Pre-select current status
        when (course.status) {
            "active" -> radioButtonActive.isChecked = true
            "inactive" -> radioButtonInactive.isChecked = true
            "pending" -> radioButtonPending.isChecked = true
            else -> radioButtonActive.isChecked = true
        }
        
        // Pre-select current visibility options
        checkBoxPublic.isChecked = course.isPublic
        checkBoxEnrollmentOpen.isChecked = course.enrollmentOpen
        
        AlertDialog.Builder(requireContext())
            .setTitle("✏️ Modifier ${course.title}")
            .setView(dialogView)
            .setPositiveButton("Sauvegarder") { _, _ ->
                val title = editTextTitle.text.toString().trim()
                val description = editTextDescription.text.toString().trim()
                val selectedTeacherIndex = spinnerTeacher.selectedItemPosition
                
                // Get status from radio group
                val status = when (radioGroupStatus.checkedRadioButtonId) {
                    R.id.radioButtonActive -> "active"
                    R.id.radioButtonInactive -> "inactive"
                    R.id.radioButtonPending -> "pending"
                    else -> "active"
                }
                
                val isPublic = checkBoxPublic.isChecked
                val enrollmentOpen = checkBoxEnrollmentOpen.isChecked
                
                if (selectedTeacherIndex >= 0 && selectedTeacherIndex < teachers.size) {
                    val teacherId = teachers[selectedTeacherIndex].id
                    
                    if (validateCourseInput(title, description)) {
                        adminViewModel.updateCourse(course.id, title, description, teacherId, status, isPublic, enrollmentOpen)
                    }
                } else {
                    Toast.makeText(requireContext(), "Veuillez sélectionner un enseignant", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showDeleteCourseDialog(course: AdminCourse) {
        AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Supprimer le cours")
            .setMessage("Êtes-vous sûr de vouloir supprimer définitivement :\n\n" +
                    "📚 ${course.title}\n" +
                    "👨‍🏫 ${course.teacherName}\n" +
                    "👥 ${course.enrollmentCount} inscription(s)\n\n" +
                    "Cette action est irréversible !")
            .setPositiveButton("Supprimer") { _, _ ->
                adminViewModel.deleteCourse(course.id)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showCourseDetails(course: AdminCourse) {
        val message = """
            📚 Titre: ${course.title}
            
            📝 Description: ${course.description}
            
            👨‍🏫 Enseignant: ${course.teacherName}
            
            👥 Inscriptions: ${course.enrollmentCount}
            
            📅 Créé le: ${course.createdAt}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📋 Détails du cours")
            .setMessage(message)
            .setPositiveButton("Fermer", null)
            .show()
    }
    
    private fun validateCourseInput(title: String, description: String): Boolean {
        return when {
            title.isEmpty() -> {
                Toast.makeText(requireContext(), "Le titre est requis", Toast.LENGTH_SHORT).show()
                false
            }
            title.length < 3 -> {
                Toast.makeText(requireContext(), "Le titre doit contenir au moins 3 caractères", Toast.LENGTH_SHORT).show()
                false
            }
            description.isEmpty() -> {
                Toast.makeText(requireContext(), "La description est requise", Toast.LENGTH_SHORT).show()
                false
            }
            description.length < 10 -> {
                Toast.makeText(requireContext(), "La description doit contenir au moins 10 caractères", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
    
    private fun toggleCourseStatus(course: AdminCourse) {
        val statusOptions = arrayOf("active", "inactive", "pending")
        val currentIndex = statusOptions.indexOf(course.status)
        
        AlertDialog.Builder(requireContext())
            .setTitle("🔄 Changer le statut du cours")
            .setMessage("Cours: ${course.title}\nStatut actuel: ${course.status}")
            .setSingleChoiceItems(statusOptions, currentIndex) { dialog, which ->
                val newStatus = statusOptions[which]
                if (newStatus != course.status) {
                    adminViewModel.toggleCourseStatus(course.id, newStatus)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun manageCourseEnrollments(course: AdminCourse) {
        try {
            val fragment = AdminManageEnrollmentsFragment.newInstance(course.id)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminManageEnrollments")
                .commit()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "📝 Gestion des inscriptions pour: ${course.title}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun viewCourseStatistics(course: AdminCourse) {
        val message = """
            📊 STATISTIQUES - ${course.title}
            
            👥 INSCRIPTIONS
            • Total: ${course.enrollmentCount} étudiants
            • Taux de remplissage: ${(course.enrollmentCount * 100) / 50}%
            
            📈 PERFORMANCE
            • Popularité: ${if (course.enrollmentCount > 15) "⭐⭐⭐" else if (course.enrollmentCount > 8) "⭐⭐" else "⭐"}
            • Statut: ${course.status}
            
            📅 INFORMATIONS
            • Créé le: ${course.createdAt}
            • Enseignant: ${course.teacherName}
            • Visibilité: ${if (course.isPublic) "Public" else "Privé"}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📊 Statistiques")
            .setMessage(message)
            .setPositiveButton("Fermer", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}