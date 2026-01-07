package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentAdminCoursesBinding
import com.edunova.mobile.data.repository.AdminCourse
import com.edunova.mobile.presentation.adapter.AdminCoursesAdapter
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminCoursesFragment : Fragment() {
    
    private var _binding: FragmentAdminCoursesBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var coursesAdapter: AdminCoursesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupRecyclerView()
        observeCourses()
        setupClickListeners()
        loadCourses()
    }
    
    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupRecyclerView() {
        coursesAdapter = AdminCoursesAdapter(
            onViewCourse = { course -> viewCourseDetails(course) },
            onEditCourse = { course -> editCourse(course) },
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
    
    private fun observeCourses() {
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.coursesState.collect { resource ->
                resource?.let {
                    when (it) {
                        is com.edunova.mobile.utils.Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is com.edunova.mobile.utils.Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            it.data?.let { courses ->
                                coursesAdapter.submitList(courses)
                                updateCourseStats(courses)
                            }
                        }
                        is com.edunova.mobile.utils.Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        
        // Observer for course actions (update, delete, etc.)
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.courseActionState.collect { resource ->
                resource?.let {
                    when (it) {
                        is com.edunova.mobile.utils.Resource.Loading -> {
                            // Show loading if needed
                        }
                        is com.edunova.mobile.utils.Resource.Success -> {
                            Toast.makeText(requireContext(), it.data ?: "Action réussie", Toast.LENGTH_SHORT).show()
                        }
                        is com.edunova.mobile.utils.Resource.Error -> {
                            Toast.makeText(requireContext(), it.message ?: "Erreur", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.chipAllCourses.setOnClickListener {
            loadCourses()
        }
        
        binding.chipActiveCourses.setOnClickListener {
            filterActiveCourses()
        }
        
        binding.chipInactiveCourses.setOnClickListener {
            filterInactiveCourses()
        }
    }
    
    private fun loadCourses() {
        adminViewModel.loadAllCourses()
    }
    
    private fun filterActiveCourses() {
        // Filter active courses based on enrollment count
        val currentCourses = coursesAdapter.currentList
        val activeCourses = currentCourses.filter { 
            it.enrollmentCount > 0 
        }
        coursesAdapter.submitList(activeCourses)
    }
    
    private fun filterInactiveCourses() {
        // Filter inactive courses based on enrollment count
        val currentCourses = coursesAdapter.currentList
        val inactiveCourses = currentCourses.filter { 
            it.enrollmentCount == 0 
        }
        coursesAdapter.submitList(inactiveCourses)
    }
    
    private fun updateCourseStats(courses: List<AdminCourse>) {
        val activeCourses = courses.count { 
            it.enrollmentCount > 0 
        }
        val inactiveCourses = courses.count { 
            it.enrollmentCount == 0 
        }
        val totalEnrollments = courses.sumOf { it.enrollmentCount }
        
        binding.textViewTotalCourses.text = courses.size.toString()
        binding.textViewActiveCourses.text = activeCourses.toString()
        binding.textViewInactiveCourses.text = inactiveCourses.toString()
        binding.textViewTotalEnrollments.text = totalEnrollments.toString()
    }
    
    private fun viewCourseDetails(course: AdminCourse) {
        // Navigate to course details or show details dialog
        showCourseDetailsDialog(course)
    }
    
    private fun editCourse(course: AdminCourse) {
        android.util.Log.d("AdminCoursesFragment", "editCourse called for course: ${course.title}")
        
        // Test très simple d'abord
        try {
            AlertDialog.Builder(requireContext())
                .setTitle("🔧 TEST - Modifier le cours")
                .setMessage("Ceci est un test pour voir si les dialogues fonctionnent.\n\nCours: ${course.title}")
                .setPositiveButton("✅ Ça marche !") { _, _ ->
                    Toast.makeText(requireContext(), "✅ Le dialogue fonctionne !", Toast.LENGTH_SHORT).show()
                    showEditCourseDialog(course)
                }
                .setNegativeButton("❌ Fermer", null)
                .show()
        } catch (e: Exception) {
            android.util.Log.e("AdminCoursesFragment", "Error in editCourse", e)
            Toast.makeText(requireContext(), "❌ Erreur dans editCourse: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showDeleteCourseDialog(course: AdminCourse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer le cours")
            .setMessage("Êtes-vous sûr de vouloir supprimer le cours \"${course.title}\" ?")
            .setPositiveButton("Supprimer") { _, _ ->
                adminViewModel.deleteCourse(course.id)
                Toast.makeText(requireContext(), "Cours supprimé", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun toggleCourseStatus(course: AdminCourse) {
        // Toggle course active status based on enrollment count
        val isActive = course.enrollmentCount > 0
        val status = if (isActive) "désactivé" else "activé"
        Toast.makeText(requireContext(), "Cours $status", Toast.LENGTH_SHORT).show()
    }
    
    private fun showCourseDetailsDialog(course: AdminCourse) {
        val statusText = if (course.enrollmentCount > 0) "Actif" else "Inactif"
        
        val message = """
            Titre: ${course.title}
            Description: ${course.description}
            Enseignant: ${course.teacherName}
            Créé: ${course.createdAt}
            Étudiants inscrits: ${course.enrollmentCount}
            Statut: $statusText
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Détails du cours")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showEditCourseDialog(course: AdminCourse) {
        android.util.Log.d("AdminCoursesFragment", "showEditCourseDialog called for course: ${course.title}")
        
        try {
            // Test simple d'abord - juste un dialogue basique
            AlertDialog.Builder(requireContext())
                .setTitle("✏️ Modifier le cours: ${course.title}")
                .setMessage("Voulez-vous modifier ce cours ?\n\nTitre: ${course.title}\nDescription: ${course.description}\nEnseignant: ${course.teacherName}")
                .setPositiveButton("✏️ Oui, modifier") { _, _ ->
                    showFullEditDialog(course)
                }
                .setNegativeButton("❌ Annuler", null)
                .show()
        } catch (e: Exception) {
            android.util.Log.e("AdminCoursesFragment", "Error showing edit dialog", e)
            Toast.makeText(requireContext(), "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showFullEditDialog(course: AdminCourse) {
        try {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_course, null)
            
            // Get references to dialog views
            val editTextTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextTitle)
            val editTextDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextDescription)
            val spinnerTeacher = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerTeacher)
            val radioGroupStatus = dialogView.findViewById<android.widget.RadioGroup>(R.id.radioGroupStatus)
            val radioButtonActive = dialogView.findViewById<android.widget.RadioButton>(R.id.radioButtonActive)
            val radioButtonInactive = dialogView.findViewById<android.widget.RadioButton>(R.id.radioButtonInactive)
            val radioButtonPending = dialogView.findViewById<android.widget.RadioButton>(R.id.radioButtonPending)
            val checkBoxPublic = dialogView.findViewById<android.widget.CheckBox>(R.id.checkBoxPublic)
            val checkBoxEnrollmentOpen = dialogView.findViewById<android.widget.CheckBox>(R.id.checkBoxEnrollmentOpen)
            
            // Pre-fill current course data
            editTextTitle?.setText(course.title)
            editTextDescription?.setText(course.description)
            
            // Set current status
            when (course.status) {
                "active" -> radioButtonActive?.isChecked = true
                "inactive" -> radioButtonInactive?.isChecked = true
                "pending" -> radioButtonPending?.isChecked = true
                else -> radioButtonActive?.isChecked = true
            }
            
            // Setup teacher spinner (simplified - you might want to load from API)
            val teachers = arrayOf("Fatima Bouaziz", "Ahmed Ben Ali", "Sarah Trabelsi", "Mohamed Karray")
            val teacherAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, teachers)
            teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTeacher?.adapter = teacherAdapter
            
            // Select current teacher
            val currentTeacherIndex = teachers.indexOf(course.teacherName)
            if (currentTeacherIndex >= 0) {
                spinnerTeacher?.setSelection(currentTeacherIndex)
            }
            
            // Create and show dialog
            AlertDialog.Builder(requireContext())
                .setTitle("✏️ Modifier le cours")
                .setView(dialogView)
                .setPositiveButton("💾 Sauvegarder") { _, _ ->
                    // Get updated values
                    val newTitle = editTextTitle?.text.toString()?.trim() ?: course.title
                    val newDescription = editTextDescription?.text.toString()?.trim() ?: course.description
                    val newTeacher = spinnerTeacher?.selectedItem?.toString() ?: course.teacherName
                    
                    val newStatus = when (radioGroupStatus?.checkedRadioButtonId) {
                        R.id.radioButtonActive -> "active"
                        R.id.radioButtonInactive -> "inactive"
                        R.id.radioButtonPending -> "pending"
                        else -> "active"
                    }
                    
                    val isPublic = checkBoxPublic?.isChecked ?: true
                    val enrollmentOpen = checkBoxEnrollmentOpen?.isChecked ?: true
                    
                    // Validate input
                    if (newTitle.isEmpty()) {
                        Toast.makeText(requireContext(), "❌ Le titre ne peut pas être vide", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    if (newDescription.isEmpty()) {
                        Toast.makeText(requireContext(), "❌ La description ne peut pas être vide", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    // Update course
                    updateCourse(course.id, newTitle, newDescription, newTeacher, newStatus, isPublic, enrollmentOpen)
                }
                .setNegativeButton("❌ Annuler", null)
                .show()
                
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors de l'ouverture du dialogue: ${e.message}", Toast.LENGTH_LONG).show()
            // Fallback - simple input dialog
            showSimpleEditDialog(course)
        }
    }
    
    private fun showSimpleEditDialog(course: AdminCourse) {
        val input = android.widget.EditText(requireContext())
        input.setText(course.title)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Modifier le titre du cours")
            .setView(input)
            .setPositiveButton("Sauvegarder") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    updateCourse(course.id, newTitle, course.description, course.teacherName, course.status, true, true)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun updateCourse(
        courseId: Int,
        title: String,
        description: String,
        teacherName: String,
        status: String,
        isPublic: Boolean,
        enrollmentOpen: Boolean
    ) {
        // Show loading
        Toast.makeText(requireContext(), "💾 Mise à jour du cours...", Toast.LENGTH_SHORT).show()
        
        // Call API to update course
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // For now, use a default teacher ID (1) - in a real app, you'd map teacher name to ID
                val teacherId = when (teacherName) {
                    "Fatima Bouaziz" -> 1
                    "Ahmed Ben Ali" -> 2
                    "Sarah Trabelsi" -> 3
                    "Mohamed Karray" -> 4
                    else -> 1
                }
                
                adminViewModel.updateCourse(courseId, title, description, teacherId, status, isPublic, enrollmentOpen)
                Toast.makeText(requireContext(), "✅ Cours mis à jour avec succès", Toast.LENGTH_SHORT).show()
                
                // Reload courses to show updated data
                loadCourses()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Erreur lors de la mise à jour: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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