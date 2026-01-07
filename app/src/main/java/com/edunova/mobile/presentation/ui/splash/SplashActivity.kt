package com.edunova.mobile.presentation.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.edunova.mobile.R
import com.edunova.mobile.presentation.ui.auth.AuthActivity
import com.edunova.mobile.presentation.ui.main.MainActivity
import com.edunova.mobile.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Vérifier l'authentification après un délai
        lifecycleScope.launch {
            delay(2000) // Afficher le splash pendant 2 secondes
            
            if (authViewModel.isLoggedIn()) {
                // Utilisateur connecté, aller au MainActivity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                // Utilisateur non connecté, aller à l'authentification
                startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
            }
            
            finish()
        }
    }
}