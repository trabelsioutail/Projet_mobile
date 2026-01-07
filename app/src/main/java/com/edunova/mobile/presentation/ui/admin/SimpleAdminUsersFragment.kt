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
import com.edunova.mobile.databinding.FragmentSimpleAdminUsersBinding
import com.edunova.mobile.data.repository.AdminUser
import com.edunova.mobile.presentation.adapter.AdminUsersAdapter
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SimpleAdminUsersFragment : Fragment() {
    
    private var _binding: FragmentSimpleAdminUsersBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var usersAdapter: AdminUsersAdapter
    private var allUsers: List<AdminUser> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        observeData()
        loadUsers()
    }
    
    private fun setupUI() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        binding.fabAddUser.setOnClickListener {
            showAddUserDialog()
        }
        
        binding.chipAllUsers.setOnClickListener {
            showAllUsers()
        }
        
        binding.chipStudents.setOnClickListener {
            filterUsersByRole("etudiant")
        }
        
        binding.chipTeachers.setOnClickListener {
            filterUsersByRole("enseignant")
        }
        
        binding.chipAdmins.setOnClickListener {
            filterUsersByRole("admin")
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadUsers()
        }
    }
    
    private fun setupRecyclerView() {
        usersAdapter = AdminUsersAdapter(
            onEditUser = { user -> showEditUserDialog(user) },
            onDeleteUser = { user -> showDeleteUserDialog(user) }
        )
        
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = usersAdapter
        }
    }
    
    private fun observeData() {
        // Observe users list
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.usersState.collect { resource ->
                binding.swipeRefreshLayout.isRefreshing = false
                
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.textViewError.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.textViewError.visibility = View.GONE
                        
                        resource.data?.let { users ->
                            allUsers = users
                            usersAdapter.submitList(users)
                            updateStats(users)
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
        
        // Observe user actions
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.userActionState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT).show()
                        loadUsers() // Refresh list
                        adminViewModel.clearUserActionState()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), 
                            "Erreur: ${resource.message}", 
                            Toast.LENGTH_LONG).show()
                        adminViewModel.clearUserActionState()
                    }
                    else -> {
                        // Loading or null state
                    }
                }
            }
        }
    }
    
    private fun loadUsers() {
        adminViewModel.loadAllUsers()
    }
    
    private fun showAllUsers() {
        usersAdapter.submitList(allUsers)
        updateStats(allUsers)
    }
    
    private fun filterUsersByRole(role: String) {
        val filteredUsers = allUsers.filter { it.role == role }
        usersAdapter.submitList(filteredUsers)
        updateStats(filteredUsers)
    }
    
    private fun updateStats(users: List<AdminUser>) {
        val students = users.count { it.role == "etudiant" }
        val teachers = users.count { it.role == "enseignant" }
        val admins = users.count { it.role == "admin" }
        
        binding.textViewStudentCount.text = students.toString()
        binding.textViewTeacherCount.text = teachers.toString()
        binding.textViewAdminCount.text = admins.toString()
        binding.textViewTotalUsers.text = users.size.toString()
    }
    
    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_user, null)
        
        val editTextEmail = dialogView.findViewById<EditText>(R.id.editTextEmail)
        val editTextFirstName = dialogView.findViewById<EditText>(R.id.editTextFirstName)
        val editTextLastName = dialogView.findViewById<EditText>(R.id.editTextLastName)
        val editTextPassword = dialogView.findViewById<EditText>(R.id.editTextPassword)
        val spinnerRole = dialogView.findViewById<Spinner>(R.id.spinnerRole)
        
        // Setup role spinner
        val roleAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.user_roles_display,
            android.R.layout.simple_spinner_item
        )
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = roleAdapter
        
        AlertDialog.Builder(requireContext())
            .setTitle("➕ Ajouter un utilisateur")
            .setView(dialogView)
            .setPositiveButton("Créer") { _, _ ->
                val email = editTextEmail.text.toString().trim()
                val firstName = editTextFirstName.text.toString().trim()
                val lastName = editTextLastName.text.toString().trim()
                val password = editTextPassword.text.toString().trim()
                val rolePosition = spinnerRole.selectedItemPosition
                val roles = resources.getStringArray(R.array.user_roles)
                val role = if (rolePosition >= 0 && rolePosition < roles.size) roles[rolePosition] else "etudiant"
                
                if (validateUserInput(email, firstName, lastName, password)) {
                    adminViewModel.createUser(firstName, lastName, email, password, role)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showEditUserDialog(user: AdminUser) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_user, null)
        
        val editTextEmail = dialogView.findViewById<EditText>(R.id.editTextEmail)
        val editTextFirstName = dialogView.findViewById<EditText>(R.id.editTextFirstName)
        val editTextLastName = dialogView.findViewById<EditText>(R.id.editTextLastName)
        val spinnerRole = dialogView.findViewById<Spinner>(R.id.spinnerRole)
        
        // Pre-fill fields
        editTextEmail.setText(user.email)
        editTextFirstName.setText(user.firstName)
        editTextLastName.setText(user.lastName)
        
        // Setup role spinner
        val roleAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.user_roles_display,
            android.R.layout.simple_spinner_item
        )
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = roleAdapter
        
        // Set current role
        val roles = resources.getStringArray(R.array.user_roles)
        val currentRoleIndex = roles.indexOf(user.role)
        if (currentRoleIndex >= 0) {
            spinnerRole.setSelection(currentRoleIndex)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("✏️ Modifier ${user.firstName} ${user.lastName}")
            .setView(dialogView)
            .setPositiveButton("Sauvegarder") { _, _ ->
                val email = editTextEmail.text.toString().trim()
                val firstName = editTextFirstName.text.toString().trim()
                val lastName = editTextLastName.text.toString().trim()
                val rolePosition = spinnerRole.selectedItemPosition
                val role = if (rolePosition >= 0 && rolePosition < roles.size) roles[rolePosition] else user.role
                
                if (validateUserInputForEdit(email, firstName, lastName)) {
                    adminViewModel.updateUser(user.id, firstName, lastName, email, role)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showDeleteUserDialog(user: AdminUser) {
        AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Supprimer l'utilisateur")
            .setMessage("Êtes-vous sûr de vouloir supprimer définitivement :\n\n" +
                    "👤 ${user.firstName} ${user.lastName}\n" +
                    "📧 ${user.email}\n" +
                    "🏷️ ${getRoleDisplayName(user.role)}\n\n" +
                    "Cette action est irréversible !")
            .setPositiveButton("Supprimer") { _, _ ->
                adminViewModel.deleteUser(user.id)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun validateUserInput(email: String, firstName: String, lastName: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(requireContext(), "L'email est requis", Toast.LENGTH_SHORT).show()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(requireContext(), "Format d'email invalide", Toast.LENGTH_SHORT).show()
                false
            }
            firstName.isEmpty() -> {
                Toast.makeText(requireContext(), "Le prénom est requis", Toast.LENGTH_SHORT).show()
                false
            }
            lastName.isEmpty() -> {
                Toast.makeText(requireContext(), "Le nom est requis", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(requireContext(), "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
    
    private fun validateUserInputForEdit(email: String, firstName: String, lastName: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(requireContext(), "L'email est requis", Toast.LENGTH_SHORT).show()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(requireContext(), "Format d'email invalide", Toast.LENGTH_SHORT).show()
                false
            }
            firstName.isEmpty() -> {
                Toast.makeText(requireContext(), "Le prénom est requis", Toast.LENGTH_SHORT).show()
                false
            }
            lastName.isEmpty() -> {
                Toast.makeText(requireContext(), "Le nom est requis", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
    
    private fun getRoleDisplayName(role: String): String {
        return when (role) {
            "etudiant" -> "Étudiant"
            "enseignant" -> "Enseignant"
            "admin" -> "Administrateur"
            else -> role
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}