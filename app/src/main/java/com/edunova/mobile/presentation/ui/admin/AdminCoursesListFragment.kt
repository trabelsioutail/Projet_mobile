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
import com.edunova.mobile.databinding.FragmentAdminCoursesListBinding
import com.edunova.mobile.data.repository.AdminCourse
import com.edunova.mobile.presentation.adapter.AdminCoursesAdapter
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminCoursesListFragment : Fragment() {
    
    private var _binding: FragmentAdminCoursesListBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var coursesAdapter: AdminCoursesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminCoursesListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupRecyclerView()
        setupClickListeners()
        observeCourses()
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
    
    private fun setupClickListeners() {
        // Filter chips
        binding.chipAllCourses.setOnClickListener {
            loadCourses()
        }
        
        binding.chipActiveCourses.setOnClickListener {
            filterActiveCourses()
        }
        
        binding.chipPendingCourses.setOnClickListener {
            filterPendingCourses()
        }
        
        // Actions button
        binding.buttonActions.setOnClickListener {
            showActionsMenu()
        }
        
        // FAB for adding new course
        binding.fabAddCourse.setOnClickListener {
            addNewCourse()
        }
    }
    
    private fun observeCourses() {
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.coursesState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        resource.data?.let { courses ->
                            coursesAdapter.submitList(courses)
                            updateCourseStats(courses)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Erreur: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
        
        // Observer for course actions (update, delete, etc.)
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.courseActionState.collect { resource ->
                resource?.let {
                    when (it) {
                        is Resource.Loading -> {
                            // Show loading if needed
                        }
                        is Resource.Success -> {
                            Toast.makeText(requireContext(), it.data ?: "Action réussie", Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), it.message ?: "Erreur", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun loadCourses() {
        binding.progressBar.visibility = View.VISIBLE
        adminViewModel.loadAllCourses()
    }
    
    private fun filterActiveCourses() {
        val currentCourses = coursesAdapter.currentList
        val activeCourses = currentCourses.filter { it.status == "active" }
        coursesAdapter.submitList(activeCourses)
        Toast.makeText(requireContext(), "Affichage: ${activeCourses.size} cours actifs", Toast.LENGTH_SHORT).show()
    }
    
    private fun filterPendingCourses() {
        val currentCourses = coursesAdapter.currentList
        val pendingCourses = currentCourses.filter { it.status == "pending" || it.status == "draft" }
        coursesAdapter.submitList(pendingCourses)
        Toast.makeText(requireContext(), "Affichage: ${pendingCourses.size} cours en attente", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateCourseStats(courses: List<AdminCourse>) {
        val totalCourses = courses.size
        val activeCourses = courses.count { it.status == "active" }
        val totalEnrollments = courses.sumOf { it.enrollmentCount }
        val averageEnrollments = if (totalCourses > 0) totalEnrollments / totalCourses else 0
        
        binding.textViewTotalCourses.text = totalCourses.toString()
        binding.textViewActiveCourses.text = activeCourses.toString()
        binding.textViewAverageEnrollments.text = averageEnrollments.toString()
    }
    
    private fun viewCourseDetails(course: AdminCourse) {
        val statusText = when (course.status) {
            "active" -> "✅ Actif"
            "inactive" -> "❌ Inactif"
            "pending" -> "⏳ En attente"
            "draft" -> "📝 Brouillon"
            else -> "❓ Inconnu"
        }
        
        val message = """
            📚 DÉTAILS DU COURS
            
            Titre: ${course.title}
            Description: ${course.description}
            Enseignant: ${course.teacherName}
            Statut: $statusText
            Inscriptions: ${course.enrollmentCount} étudiants
            Créé le: ${course.createdAt}
            ID: ${course.id}
            
            📊 STATISTIQUES
            • Taux d'inscription: ${if (course.enrollmentCount > 0) "Élevé" else "Faible"}
            • Popularité: ${if (course.enrollmentCount > 10) "Très populaire" else if (course.enrollmentCount > 5) "Populaire" else "En développement"}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📚 ${course.title}")
            .setMessage(message)
            .setPositiveButton("Gérer inscriptions") { _, _ ->
                manageCourseEnrollments(course)
            }
            .setNeutralButton("Modifier") { _, _ ->
                editCourse(course)
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun editCourse(course: AdminCourse) {
        android.util.Log.d("AdminCoursesListFragment", "editCourse called for course: ${course.title}")
        
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
            android.util.Log.e("AdminCoursesListFragment", "Error in editCourse", e)
            Toast.makeText(requireContext(), "❌ Erreur dans editCourse: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showEditCourseDialog(course: AdminCourse) {
        android.util.Log.d("AdminCoursesListFragment", "showEditCourseDialog called for course: ${course.title}")
        
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
            android.util.Log.e("AdminCoursesListFragment", "Error showing edit dialog", e)
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
    
    private fun showDeleteCourseDialog(course: AdminCourse) {
        val warningMessage = if (course.enrollmentCount > 0) {
            "⚠️ ATTENTION: Ce cours a ${course.enrollmentCount} étudiants inscrits. La suppression affectera ces inscriptions."
        } else {
            "Êtes-vous sûr de vouloir supprimer ce cours ?"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Supprimer le cours")
            .setMessage("Cours: ${course.title}\n\n$warningMessage")
            .setPositiveButton("Supprimer") { _, _ ->
                deleteCourse(course)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun deleteCourse(course: AdminCourse) {
        // TODO: Implement actual deletion via AdminViewModel
        Toast.makeText(requireContext(), "🗑️ Cours supprimé: ${course.title}", Toast.LENGTH_SHORT).show()
        
        // Remove from current list for immediate UI feedback
        val currentList = coursesAdapter.currentList.toMutableList()
        currentList.remove(course)
        coursesAdapter.submitList(currentList)
        updateCourseStats(currentList)
    }
    
    private fun toggleCourseStatus(course: AdminCourse) {
        val newStatus = if (course.status == "active") "inactive" else "active"
        val statusText = if (newStatus == "active") "activé" else "désactivé"
        
        Toast.makeText(requireContext(), "📊 Cours $statusText: ${course.title}", Toast.LENGTH_SHORT).show()
        
        // TODO: Update via AdminViewModel
        // For now, update local list
        val updatedCourse = course.copy(status = newStatus)
        val currentList = coursesAdapter.currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == course.id }
        if (index != -1) {
            currentList[index] = updatedCourse
            coursesAdapter.submitList(currentList)
            updateCourseStats(currentList)
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
            📊 STATISTIQUES DÉTAILLÉES
            
            Cours: ${course.title}
            
            👥 INSCRIPTIONS
            • Total: ${course.enrollmentCount} étudiants
            • Taux de remplissage: ${(course.enrollmentCount * 100) / 50}%
            • Statut: ${if (course.enrollmentCount > 20) "Complet" else "Places disponibles"}
            
            📈 PERFORMANCE
            • Popularité: ${if (course.enrollmentCount > 15) "⭐⭐⭐" else if (course.enrollmentCount > 8) "⭐⭐" else "⭐"}
            • Tendance: ${if (course.enrollmentCount > 10) "📈 Croissante" else "📊 Stable"}
            
            🎯 RECOMMANDATIONS
            ${if (course.enrollmentCount < 5) "• Promouvoir le cours\n• Améliorer la description" else "• Cours performant\n• Maintenir la qualité"}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📊 Statistiques - ${course.title}")
            .setMessage(message)
            .setPositiveButton("Exporter") { _, _ ->
                Toast.makeText(requireContext(), "📤 Export des statistiques...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showActionsMenu() {
        val actions = arrayOf(
            "📊 Voir toutes les statistiques",
            "📤 Exporter les données",
            "🔄 Actualiser les cours",
            "⚙️ Paramètres avancés",
            "📋 Rapport détaillé"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("⚡ Actions disponibles")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> showAllStatistics()
                    1 -> exportCourseData()
                    2 -> loadCourses()
                    3 -> openAdvancedSettings()
                    4 -> generateDetailedReport()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun addNewCourse() {
        Toast.makeText(requireContext(), "➕ Ajout d'un nouveau cours...", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to create course fragment
    }
    
    private fun showAllStatistics() {
        val courses = coursesAdapter.currentList
        val totalCourses = courses.size
        val activeCourses = courses.count { it.status == "active" }
        val totalEnrollments = courses.sumOf { it.enrollmentCount }
        val averageEnrollments = if (totalCourses > 0) totalEnrollments / totalCourses else 0
        val mostPopular = courses.maxByOrNull { it.enrollmentCount }
        
        val message = """
            📊 STATISTIQUES GLOBALES
            
            📚 COURS
            • Total: $totalCourses cours
            • Actifs: $activeCourses cours
            • Inactifs: ${totalCourses - activeCourses} cours
            
            👥 INSCRIPTIONS
            • Total: $totalEnrollments étudiants
            • Moyenne par cours: $averageEnrollments étudiants
            
            🏆 COURS LE PLUS POPULAIRE
            ${mostPopular?.let { "• ${it.title} (${it.enrollmentCount} étudiants)" } ?: "• Aucun cours"}
            
            📈 PERFORMANCE GLOBALE
            • Taux d'activité: ${(activeCourses * 100) / totalCourses}%
            • Engagement: ${if (averageEnrollments > 10) "Élevé" else "Moyen"}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📊 Statistiques Globales")
            .setMessage(message)
            .setPositiveButton("Fermer", null)
            .show()
    }
    
    private fun exportCourseData() {
        Toast.makeText(requireContext(), "📤 Export des données en cours...", Toast.LENGTH_SHORT).show()
    }
    
    private fun openAdvancedSettings() {
        Toast.makeText(requireContext(), "⚙️ Ouverture des paramètres avancés...", Toast.LENGTH_SHORT).show()
    }
    
    private fun generateDetailedReport() {
        Toast.makeText(requireContext(), "📋 Génération du rapport détaillé...", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}