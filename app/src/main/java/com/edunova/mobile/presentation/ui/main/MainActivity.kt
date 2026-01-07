package com.edunova.mobile.presentation.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.edunova.mobile.R
import com.edunova.mobile.databinding.ActivityMainBinding
import com.edunova.mobile.domain.model.UserRole
import com.edunova.mobile.presentation.ui.auth.AuthActivity
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        observeAuthState()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        navController = navHostFragment.navController
        
        // Observer l'utilisateur connecté pour configurer la navigation
        lifecycleScope.launch {
            authViewModel.authenticatedUser.collect { user ->
                if (user != null) {
                    setupNavigationForUserRole(user.role)
                } else {
                    // Utilisateur déconnecté, rediriger vers l'authentification
                    startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                    finish()
                }
            }
        }
    }
    
    private fun setupNavigationForUserRole(userRole: UserRole) {
        // Configurer la navigation selon le rôle de l'utilisateur
        val graphResId = when (userRole) {
            UserRole.ETUDIANT -> R.navigation.nav_student
            UserRole.ENSEIGNANT -> R.navigation.nav_teacher
            UserRole.ADMIN -> R.navigation.nav_admin
        }
        
        // Définir le graphique de navigation
        navController.setGraph(graphResId)
        
        // Configurer la bottom navigation
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Configurer l'ActionBar
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard,
                R.id.nav_courses,
                R.id.nav_quizzes,
                R.id.nav_messages,
                R.id.nav_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Masquer/afficher la bottom navigation selon les fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_dashboard,
                R.id.nav_courses,
                R.id.nav_quizzes,
                R.id.nav_messages,
                R.id.nav_profile -> {
                    binding.bottomNavigation.visibility = android.view.View.VISIBLE
                }
                else -> {
                    binding.bottomNavigation.visibility = android.view.View.GONE
                }
            }
        }
    }
    
    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.authenticatedUser.collect { user ->
                if (user == null && authViewModel.isLoggedIn().not()) {
                    // Utilisateur déconnecté, rediriger vers l'authentification
                    startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                    finish()
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}