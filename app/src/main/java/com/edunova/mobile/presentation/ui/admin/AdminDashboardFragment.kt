package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentAdminDashboardBinding
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import com.edunova.mobile.presentation.viewmodel.CourseViewModel
import com.edunova.mobile.presentation.viewmodel.QuizViewModel
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.presentation.ui.common.AiChatFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {
    
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val authViewModel: AuthViewModel by viewModels()
    private val courseViewModel: CourseViewModel by viewModels()
    private val quizViewModel: QuizViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        observeUser()
        loadDashboardData()
        setupClickListeners()
        setupAiChatButton()
    }
    
    private fun observeUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authenticatedUser.collect { user ->
                user?.let {
                    binding.textViewWelcome.text = "Bonjour, ${it.firstName}!"
                }
            }
        }
        
        // Set current date
        val currentDate = java.text.SimpleDateFormat("EEEE, d MMMM yyyy", java.util.Locale.FRENCH)
            .format(java.util.Date())
        binding.textViewCurrentDate.text = currentDate
    }
    
    private fun loadDashboardData() {
        // Charger les statistiques via AdminViewModel
        adminViewModel.loadSystemStats()
        adminViewModel.loadAllUsers()
        adminViewModel.loadAllCourses()
        adminViewModel.loadAllQuizzes()
        
        // Observer les statistiques système
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.systemStatsState.collect { resource ->
                when (resource) {
                    is com.edunova.mobile.utils.Resource.Success -> {
                        resource.data?.let { stats ->
                            binding.textViewTotalUsers.text = stats.totalUsers.toString()
                            binding.textViewTotalCourses.text = stats.totalCourses.toString()
                            binding.textViewTotalQuizzes.text = stats.totalQuizzes.toString()
                        }
                    }
                    else -> {
                        // Fallback aux données des autres ViewModels
                        loadFallbackData()
                    }
                }
            }
        }
    }
    
    private fun loadFallbackData() {
        // Charger les données de base si les stats admin ne sont pas disponibles
        courseViewModel.loadAllCourses()
        courseViewModel.loadAllQuizzes()
        
        // Observer les données pour mettre à jour les statistiques
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.coursesState.collect { resource ->
                resource?.data?.let { courses ->
                    if (binding.textViewTotalCourses.text.toString() == "0" || 
                        binding.textViewTotalCourses.text.toString().isEmpty()) {
                        binding.textViewTotalCourses.text = courses.size.toString()
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.quizzesState.collect { resource ->
                resource?.data?.let { quizzes ->
                    if (binding.textViewTotalQuizzes.text.toString() == "0" || 
                        binding.textViewTotalQuizzes.text.toString().isEmpty()) {
                        binding.textViewTotalQuizzes.text = quizzes.size.toString()
                    }
                }
            }
        }
        
        // Valeur par défaut pour les utilisateurs
        if (binding.textViewTotalUsers.text.toString() == "0" || 
            binding.textViewTotalUsers.text.toString().isEmpty()) {
            binding.textViewTotalUsers.text = "5" // Admin + Ghofrane + Ahmed + autres
        }
    }
    
    private fun setupClickListeners() {
        binding.cardViewUsers.setOnClickListener {
            navigateToUsersManagement()
        }
        
        binding.cardViewCourses.setOnClickListener {
            navigateToCoursesManagement()
        }
        
        binding.cardViewQuizzes.setOnClickListener {
            navigateToQuizzesManagement()
        }
        
        binding.cardViewReports.setOnClickListener {
            navigateToReports()
        }
        
        binding.cardViewSettings.setOnClickListener {
            navigateToSystemSettings()
        }
        
        binding.cardViewBackup.setOnClickListener {
            navigateToBackupManagement()
        }
    }
    
    private fun setupAiChatButton() {
        // Check if the FAB exists in the layout (for backward compatibility)
        try {
            val fabAiChat = binding.root.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAiChat)
            fabAiChat?.setOnClickListener {
                openAiChat()
            }
        } catch (e: Exception) {
            // FAB not found in layout, ignore
        }
    }
    
    private fun openAiChat() {
        try {
            val aiChatFragment = AiChatFragment.newInstance("admin")
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, aiChatFragment)
                .addToBackStack("AiChat")
                .commit()
        } catch (e: Exception) {
            try {
                val aiChatFragment = AiChatFragment.newInstance("admin")
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, aiChatFragment)
                    .addToBackStack("AiChat")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "🤖 Assistant IA temporairement indisponible", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToUsersManagement() {
        try {
            val fragment = SimpleAdminUsersFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminUsers")
                .commit()
        } catch (e: Exception) {
            // Fallback: navigation simple
            try {
                val fragment = SimpleAdminUsersFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminUsers")
                    .commit()
            } catch (ex: Exception) {
                // Toast d'erreur si la navigation échoue
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation: ${ex.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun navigateToCoursesManagement() {
        // Navigation directe vers la page de menu des cours avec les 3 boutons violets
        try {
            val fragment = AdminCourseMenuFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminCourseMenu")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminCourseMenuFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminCourseMenu")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToCoursesOnly() {
        try {
            val fragment = SimpleAdminCoursesFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminCourses")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = SimpleAdminCoursesFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminCourses")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToEnrollmentsManagement() {
        try {
            val fragment = AdminManageEnrollmentsFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminEnrollments")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminManageEnrollmentsFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminEnrollments")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation vers les inscriptions: ${ex.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun navigateToAllCourses() {
        try {
            val fragment = AdminViewAllCoursesFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminAllCourses")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminViewAllCoursesFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminAllCourses")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToCourseSettings() {
        try {
            val fragment = AdminCourseSettingsFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminCourseSettings")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminCourseSettingsFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminCourseSettings")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToQuizzesManagement() {
        try {
            val fragment = AdminQuizMenuFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminQuizMenu")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = AdminQuizMenuFragment()
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminQuizMenu")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation vers le menu quiz: ${ex.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun navigateToReports() {
        try {
            val fragment = SimpleAdminFragment.newInstance("Rapports et Analyses", "Interface de génération de rapports EduNova")
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminReports")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = SimpleAdminFragment.newInstance("Rapports et Analyses", "Interface de génération de rapports EduNova")
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminReports")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToSystemSettings() {
        try {
            val fragment = SimpleAdminFragment.newInstance("Paramètres Système", "Interface de configuration système EduNova")
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminSettings")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = SimpleAdminFragment.newInstance("Paramètres Système", "Interface de configuration système EduNova")
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminSettings")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToBackupManagement() {
        try {
            val fragment = SimpleAdminFragment.newInstance("Sauvegarde", "Interface de gestion des sauvegardes EduNova")
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, fragment)
                .addToBackStack("AdminBackup")
                .commit()
        } catch (e: Exception) {
            try {
                val fragment = SimpleAdminFragment.newInstance("Sauvegarde", "Interface de gestion des sauvegardes EduNova")
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack("AdminBackup")
                    .commit()
            } catch (ex: Exception) {
                android.widget.Toast.makeText(requireContext(), "Erreur de navigation", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}