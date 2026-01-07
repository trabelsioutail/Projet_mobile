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
import com.edunova.mobile.databinding.FragmentAdminUsersBinding
import com.edunova.mobile.data.repository.AdminUser
import com.edunova.mobile.presentation.adapter.AdminUsersAdapter
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminUsersFragment : Fragment() {
    
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var usersAdapter: AdminUsersAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupRecyclerView()
        observeUsers()
        observeUserActions()
        setupClickListeners()
        loadUsers()
    }
    
    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
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
    
    private fun observeUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.usersState.collect { resource ->
                resource?.let {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.recyclerViewUsers.visibility = View.GONE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerViewUsers.visibility = View.VISIBLE
                            it.data?.let { users ->
                                usersAdapter.submitList(users)
                                updateUserStats(users)
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerViewUsers.visibility = View.VISIBLE
                            Toast.makeText(requireContext(), "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun observeUserActions() {
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.userActionState.collect { resource ->
                resource?.let {
                    when (it) {
                        is Resource.Loading -> {
                            // Optionally show loading indicator
                        }
                        is Resource.Success -> {
                            Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                            loadUsers() // Refresh the list
                            adminViewModel.clearUserActionState()
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                            adminViewModel.clearUserActionState()
                        }
                    }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddUser.setOnClickListener {
            showAddUserDialog()
        }
        
        binding.chipAllUsers.setOnClickListener {
            loadUsers()
        }
        
        binding.chipStudents.setOnClickListener {
            filterUsersByRole("student")
        }
        
        binding.chipTeachers.setOnClickListener {
            filterUsersByRole("teacher")
        }
        
        binding.chipAdmins.setOnClickListener {
            filterUsersByRole("admin")
        }
    }
    
    private fun loadUsers() {
        adminViewModel.loadAllUsers()
    }
    
    private fun filterUsersByRole(role: String) {
        val currentUsers = usersAdapter.currentList
        val filteredUsers = currentUsers.filter { it.role == role }
        usersAdapter.submitList(filteredUsers)
        updateUserStats(filteredUsers)
    }
    
    private fun updateUserStats(users: List<AdminUser>) {
        val students = users.count { it.role == "student" }
        val teachers = users.count { it.role == "teacher" }
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
            .setTitle("Ajouter un utilisateur")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val email = editTextEmail.text.toString().trim()
                val firstName = editTextFirstName.text.toString().trim()
                val lastName = editTextLastName.text.toString().trim()
                val password = editTextPassword.text.toString().trim()
                val rolePosition = spinnerRole.selectedItemPosition
                val roles = resources.getStringArray(R.array.user_roles)
                val role = if (rolePosition >= 0 && rolePosition < roles.size) roles[rolePosition] else "student"
                
                if (email.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && password.isNotEmpty()) {
                    adminViewModel.createUser(firstName, lastName, email, password, role)
                } else {
                    Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
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
            .setTitle("Modifier l'utilisateur")
            .setView(dialogView)
            .setPositiveButton("Modifier") { _, _ ->
                val email = editTextEmail.text.toString().trim()
                val firstName = editTextFirstName.text.toString().trim()
                val lastName = editTextLastName.text.toString().trim()
                val rolePosition = spinnerRole.selectedItemPosition
                val role = if (rolePosition >= 0 && rolePosition < roles.size) roles[rolePosition] else user.role
                
                if (email.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty()) {
                    adminViewModel.updateUser(user.id, firstName, lastName, email, role)
                } else {
                    Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showDeleteUserDialog(user: AdminUser) {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer l'utilisateur")
            .setMessage("Êtes-vous sûr de vouloir supprimer ${user.firstName} ${user.lastName} ?")
            .setPositiveButton("Supprimer") { _, _ ->
                adminViewModel.deleteUser(user.id)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}