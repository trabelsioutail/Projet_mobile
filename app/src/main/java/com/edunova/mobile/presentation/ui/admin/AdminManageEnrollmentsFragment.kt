package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentAdminManageEnrollmentsBinding
import com.edunova.mobile.data.repository.AdminCourse
import com.edunova.mobile.data.repository.AdminUser
import com.edunova.mobile.presentation.adapter.AdminEnrollmentsAdapter
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminManageEnrollmentsFragment : Fragment() {
    
    companion object {
        private const val ARG_COURSE_ID = "course_id"
        
        fun newInstance(courseId: Int? = null): AdminManageEnrollmentsFragment {
            val fragment = AdminManageEnrollmentsFragment()
            val args = Bundle()
            courseId?.let { args.putInt(ARG_COURSE_ID, it) }
            fragment.arguments = args
            return fragment
        }
    }
    
    private var _binding: FragmentAdminManageEnrollmentsBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var enrollmentsAdapter: AdminEnrollmentsAdapter
    private var allEnrollments: List<com.edunova.mobile.data.repository.AdminEnrollment> = emptyList()
    private var filteredEnrollments: List<com.edunova.mobile.data.repository.AdminEnrollment> = emptyList()
    private var currentFilter: String = "all" // "all", "active", "pending"
    private var allCourses: List<AdminCourse> = emptyList()
    private var allUsers: List<AdminUser> = emptyList()
    private var selectedCourseId: Int? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminManageEnrollmentsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get course ID from arguments if provided
        selectedCourseId = arguments?.getInt(ARG_COURSE_ID)
        
        setupUI()
        setupRecyclerView()
        observeData()
        loadData()
    }
    
    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.fabAddEnrollment.setOnClickListener {
            showAddEnrollmentDialog()
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }
        
        binding.chipAllEnrollments.setOnClickListener {
            showAllEnrollments()
        }
        
        binding.chipActiveEnrollments.setOnClickListener {
            filterActiveEnrollments()
        }
        
        binding.chipPendingEnrollments.setOnClickListener {
            filterPendingEnrollments()
        }
        
        binding.buttonBulkActions.setOnClickListener {
            showBulkActionsDialog()
        }
    }
    
    private fun setupRecyclerView() {
        enrollmentsAdapter = AdminEnrollmentsAdapter(
            onApproveEnrollment = { enrollment -> updateEnrollmentStatus(enrollment, "active") },
            onRejectEnrollment = { enrollment -> updateEnrollmentStatus(enrollment, "inactive") },
            onPendingEnrollment = { enrollment -> showPendingDialog(enrollment) },
            onRemoveEnrollment = { enrollment -> removeEnrollment(enrollment) },
            onViewDetails = { enrollment -> showEnrollmentDetails(enrollment) },
            onSendMessage = { enrollment -> showSendMessageDialog(enrollment) }
        )
        
        binding.recyclerViewEnrollments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = enrollmentsAdapter
        }
    }
    
    private fun observeData() {
        // Observer enrollments
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.enrollmentsState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                    is Resource.Success -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        resource.data?.let { enrollments ->
                            allEnrollments = enrollments
                            applyCurrentFilter()
                            updateEnrollmentStats(enrollments)
                        }
                    }
                    is Resource.Error -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                    }
                    null -> { /* Initial state */ }
                }
            }
        }
        
        // Observer enrollment actions
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.enrollmentActionState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading if needed
                    }
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT).show()
                        adminViewModel.clearEnrollmentActionState()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        adminViewModel.clearEnrollmentActionState()
                    }
                    null -> { /* Initial state */ }
                }
            }
        }
        
        // Observer courses
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.coursesState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { courses ->
                            allCourses = courses
                        }
                    }
                    else -> { /* Handle other states */ }
                }
            }
        }
        
        // Observer users
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.usersState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { users ->
                            allUsers = users.filter { it.role == "etudiant" } // Only students
                        }
                    }
                    else -> { /* Handle other states */ }
                }
            }
        }
    }
    
    private fun loadData() {
        adminViewModel.loadAllEnrollments()
        adminViewModel.loadAllCourses()
        adminViewModel.loadAllUsers()
    }
    
    private fun showAllEnrollments() {
        currentFilter = "all"
        applyCurrentFilter()
        updateFilterChips()
        Toast.makeText(requireContext(), "Affichage de toutes les inscriptions", Toast.LENGTH_SHORT).show()
    }
    
    private fun filterActiveEnrollments() {
        currentFilter = "active"
        applyCurrentFilter()
        updateFilterChips()
        Toast.makeText(requireContext(), "Filtrage des inscriptions actives", Toast.LENGTH_SHORT).show()
    }
    
    private fun filterPendingEnrollments() {
        currentFilter = "pending"
        applyCurrentFilter()
        updateFilterChips()
        Toast.makeText(requireContext(), "Filtrage des inscriptions en attente", Toast.LENGTH_SHORT).show()
    }
    
    private fun applyCurrentFilter() {
        filteredEnrollments = when (currentFilter) {
            "active" -> allEnrollments.filter { 
                it.status?.lowercase() in listOf("active", "approved", "enrolled") 
            }
            "pending" -> allEnrollments.filter { 
                it.status?.lowercase() == "pending" 
            }
            else -> allEnrollments
        }
        enrollmentsAdapter.submitList(filteredEnrollments)
    }
    
    private fun updateFilterChips() {
        // Update chip appearance based on current filter
        binding.chipAllEnrollments.isChecked = currentFilter == "all"
        binding.chipActiveEnrollments.isChecked = currentFilter == "active"
        binding.chipPendingEnrollments.isChecked = currentFilter == "pending"
    }
    
    private fun showAddEnrollmentDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_enrollment, null)
        
        val spinnerStudent = dialogView.findViewById<Spinner>(R.id.spinnerStudent)
        val spinnerCourse = dialogView.findViewById<Spinner>(R.id.spinnerCourse)
        
        // Setup student spinner
        val studentNames = allUsers.map { "${it.firstName} ${it.lastName}" }
        val studentAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, studentNames)
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStudent.adapter = studentAdapter
        
        // Setup course spinner
        val courseNames = allCourses.map { it.title }
        val courseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courseNames)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourse.adapter = courseAdapter
        
        AlertDialog.Builder(requireContext())
            .setTitle("➕ Nouvelle inscription")
            .setView(dialogView)
            .setPositiveButton("Inscrire") { _, _ ->
                val selectedStudent = allUsers.getOrNull(spinnerStudent.selectedItemPosition)
                val selectedCourse = allCourses.getOrNull(spinnerCourse.selectedItemPosition)
                
                if (selectedStudent != null && selectedCourse != null) {
                    enrollStudent(selectedStudent, selectedCourse)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showBulkActionsDialog() {
        val actions = arrayOf(
            "✅ Approuver toutes les inscriptions en attente",
            "❌ Rejeter toutes les inscriptions en attente",
            "📧 Envoyer notification aux étudiants",
            "📊 Exporter la liste des inscriptions",
            "🔄 Synchroniser avec le système externe"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("⚡ Actions en lot")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> approveAllPendingEnrollments()
                    1 -> rejectAllPendingEnrollments()
                    2 -> sendNotificationToStudents()
                    3 -> exportEnrollmentsList()
                    4 -> syncWithExternalSystem()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun enrollStudent(student: AdminUser, course: AdminCourse) {
        adminViewModel.createEnrollment(course.id, student.id, "active")
    }
    
    private fun updateEnrollmentStatus(enrollment: com.edunova.mobile.data.repository.AdminEnrollment, newStatus: String) {
        val statusText = when(newStatus) {
            "active" -> "approuvée"
            "inactive" -> "rejetée"
            "pending" -> "mise en attente"
            else -> "modifiée"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("🔄 Modifier l'inscription")
            .setMessage("Êtes-vous sûr de vouloir que l'inscription soit $statusText ?")
            .setPositiveButton("Confirmer") { _, _ ->
                adminViewModel.updateEnrollment(enrollment.id, newStatus)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showPendingDialog(enrollment: com.edunova.mobile.data.repository.AdminEnrollment) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.select_dialog_singlechoice, null)
        
        val reasons = arrayOf(
            "Documents manquants",
            "Prérequis non satisfaits",
            "Capacité du cours atteinte",
            "Validation en cours",
            "Problème de paiement",
            "Autre raison"
        )
        
        var selectedReason = reasons[0]
        
        AlertDialog.Builder(requireContext())
            .setTitle("📋 Mettre en attente")
            .setMessage("Pourquoi mettre cette inscription en attente ?")
            .setSingleChoiceItems(reasons, 0) { _, which ->
                selectedReason = reasons[which]
            }
            .setPositiveButton("Mettre en attente") { _, _ ->
                adminViewModel.updateEnrollment(enrollment.id, "pending")
                sendPendingMessage(enrollment, selectedReason)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun sendPendingMessage(enrollment: com.edunova.mobile.data.repository.AdminEnrollment, reason: String) {
        val studentName = enrollment.studentName ?: "Étudiant"
        val courseTitle = enrollment.courseTitle ?: "le cours"
        
        val message = """
            Bonjour $studentName,
            
            Votre inscription au cours "$courseTitle" a été mise en attente.
            
            Raison: $reason
            
            Nous vous contacterons dès que possible pour résoudre cette situation.
            
            Cordialement,
            L'équipe EduNova
        """.trimIndent()
        
        // Simuler l'envoi du message
        Toast.makeText(requireContext(), 
            "Message envoyé à ${enrollment.studentEmail ?: "l'étudiant"}", 
            Toast.LENGTH_LONG).show()
    }
    
    private fun showSendMessageDialog(enrollment: com.edunova.mobile.data.repository.AdminEnrollment) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.select_dialog_item, null)
        
        val messageTypes = arrayOf(
            "📧 Rappel de cours",
            "⚠️ Problème d'inscription", 
            "✅ Confirmation d'inscription",
            "📚 Informations sur le cours",
            "💬 Message personnalisé"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("💬 Envoyer un message")
            .setItems(messageTypes) { _, which ->
                when (which) {
                    0 -> sendCourseReminder(enrollment)
                    1 -> sendEnrollmentProblem(enrollment)
                    2 -> sendEnrollmentConfirmation(enrollment)
                    3 -> sendCourseInfo(enrollment)
                    4 -> showCustomMessageDialog(enrollment)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun sendCourseReminder(enrollment: com.edunova.mobile.data.repository.AdminEnrollment) {
        Toast.makeText(requireContext(), 
            "Rappel de cours envoyé à ${enrollment.studentEmail ?: "l'étudiant"}", 
            Toast.LENGTH_SHORT).show()
    }
    
    private fun sendEnrollmentProblem(enrollment: com.edunova.mobile.data.repository.AdminEnrollment) {
        Toast.makeText(requireContext(), 
            "Message de problème envoyé à ${enrollment.studentEmail ?: "l'étudiant"}", 
            Toast.LENGTH_SHORT).show()
    }
    
    private fun sendEnrollmentConfirmation(enrollment: com.edunova.mobile.data.repository.AdminEnrollment) {
        Toast.makeText(requireContext(), 
            "Confirmation d'inscription envoyée à ${enrollment.studentEmail ?: "l'étudiant"}", 
            Toast.LENGTH_SHORT).show()
    }
    
    private fun sendCourseInfo(enrollment: com.edunova.mobile.data.repository.AdminEnrollment) {
        Toast.makeText(requireContext(), 
            "Informations de cours envoyées à ${enrollment.studentEmail ?: "l'étudiant"}", 
            Toast.LENGTH_SHORT).show()
    }
    
    private fun showCustomMessageDialog(enrollment: com.edunova.mobile.data.repository.AdminEnrollment) {
        val editText = android.widget.EditText(requireContext())
        editText.hint = "Tapez votre message personnalisé..."
        editText.minLines = 3
        
        AlertDialog.Builder(requireContext())
            .setTitle("💬 Message personnalisé")
            .setView(editText)
            .setPositiveButton("Envoyer") { _, _ ->
                val message = editText.text.toString()
                if (message.isNotBlank()) {
                    Toast.makeText(requireContext(), 
                        "Message personnalisé envoyé à ${enrollment.studentEmail ?: "l'étudiant"}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun removeEnrollment(enrollment: com.edunova.mobile.data.repository.AdminEnrollment) {
        val studentName = enrollment.studentName?.takeIf { it.isNotBlank() } ?: "Étudiant inconnu"
        val courseTitle = enrollment.courseTitle?.takeIf { it.isNotBlank() } ?: "Cours inconnu"
        
        AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Supprimer l'inscription")
            .setMessage("Êtes-vous sûr de vouloir supprimer l'inscription de $studentName au cours $courseTitle ?")
            .setPositiveButton("Supprimer") { _, _ ->
                adminViewModel.deleteEnrollment(enrollment.id)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showEnrollmentDetails(enrollment: com.edunova.mobile.data.repository.AdminEnrollment) {
        val studentName = enrollment.studentName?.takeIf { it.isNotBlank() } ?: "Étudiant inconnu"
        val studentEmail = enrollment.studentEmail?.takeIf { it.isNotBlank() } ?: "Email non disponible"
        val courseTitle = enrollment.courseTitle?.takeIf { it.isNotBlank() } ?: "Cours sans titre"
        val courseDescription = enrollment.courseDescription?.takeIf { it.isNotBlank() } ?: "Aucune description"
        val teacherName = enrollment.teacherName?.takeIf { it.isNotBlank() } ?: "Enseignant non assigné"
        val enrolledAt = enrollment.enrolledAt?.takeIf { it.isNotBlank() } ?: "Date inconnue"
        val status = enrollment.status?.takeIf { it.isNotBlank() } ?: "active"
        
        val message = """
            👤 Étudiant: $studentName
            📧 Email: $studentEmail
            📚 Cours: $courseTitle
            📝 Description: $courseDescription
            👨‍🏫 Enseignant: $teacherName
            📅 Date d'inscription: $enrolledAt
            📊 Statut: $status
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📋 Détails de l'inscription")
            .setMessage(message)
            .setPositiveButton("Fermer", null)
            .show()
    }
    
    private fun approveAllPendingEnrollments() {
        AlertDialog.Builder(requireContext())
            .setTitle("✅ Approuver toutes les inscriptions")
            .setMessage("Êtes-vous sûr de vouloir approuver toutes les inscriptions en attente ?")
            .setPositiveButton("Approuver") { _, _ ->
                Toast.makeText(requireContext(), "Toutes les inscriptions ont été approuvées", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun rejectAllPendingEnrollments() {
        AlertDialog.Builder(requireContext())
            .setTitle("❌ Rejeter toutes les inscriptions")
            .setMessage("Êtes-vous sûr de vouloir rejeter toutes les inscriptions en attente ?")
            .setPositiveButton("Rejeter") { _, _ ->
                Toast.makeText(requireContext(), "Toutes les inscriptions ont été rejetées", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun sendNotificationToStudents() {
        Toast.makeText(requireContext(), "Notifications envoyées aux étudiants", Toast.LENGTH_SHORT).show()
    }
    
    private fun exportEnrollmentsList() {
        Toast.makeText(requireContext(), "Liste des inscriptions exportée", Toast.LENGTH_SHORT).show()
    }
    
    private fun syncWithExternalSystem() {
        Toast.makeText(requireContext(), "Synchronisation avec le système externe", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateEnrollmentStats(enrollments: List<com.edunova.mobile.data.repository.AdminEnrollment>) {
        val totalEnrollments = enrollments.size
        val activeEnrollments = enrollments.count { 
            it.status?.lowercase() in listOf("active", "approved", "enrolled") 
        }
        val pendingEnrollments = enrollments.count { 
            it.status?.lowercase() == "pending" 
        }
        
        binding.textViewTotalEnrollments.text = totalEnrollments.toString()
        binding.textViewActiveCourses.text = activeEnrollments.toString()
        binding.textViewAverageEnrollments.text = pendingEnrollments.toString()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}