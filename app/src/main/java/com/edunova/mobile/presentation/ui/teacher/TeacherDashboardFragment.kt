package com.edunova.mobile.presentation.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.databinding.FragmentTeacherDashboardBinding
import com.edunova.mobile.presentation.adapter.CourseCompactAdapter
import com.edunova.mobile.presentation.viewmodel.DashboardViewModel
import com.edunova.mobile.presentation.ui.common.AiChatFragment
import com.edunova.mobile.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TeacherDashboardFragment : Fragment() {
    
    private var _binding: FragmentTeacherDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var recentCoursesAdapter: CourseCompactAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        setupAiChatButton()
        observeData()
    }
    
    private fun setupRecyclerView() {
        recentCoursesAdapter = CourseCompactAdapter { _ ->
            // TODO: Navigation vers les détails du cours
            // findNavController().navigate(
            //     TeacherDashboardFragmentDirections.actionToCourseDetails(course.id)
            // )
        }
        
        binding.recyclerViewRecentCourses.apply {
            adapter = recentCoursesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupClickListeners() {
        binding.cardCreateCourse.setOnClickListener {
            showCreateCourseOptions()
        }
        
        binding.cardViewQuizzes.setOnClickListener {
            showQuizzesQuickActions()
        }
        
        binding.cardMessages.setOnClickListener {
            showMessagesQuickActions()
        }
        
        binding.buttonViewAllCourses.setOnClickListener {
            showDetailedStats()
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
            val aiChatFragment = AiChatFragment.newInstance("enseignant")
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_main, aiChatFragment)
                .addToBackStack("AiChat")
                .commit()
        } catch (e: Exception) {
            try {
                val aiChatFragment = AiChatFragment.newInstance("enseignant")
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, aiChatFragment)
                    .addToBackStack("AiChat")
                    .commit()
            } catch (ex: Exception) {
                showMessage("🤖 Assistant IA temporairement indisponible")
            }
        }
    }
    
    private fun observeData() {
        // Observer l'utilisateur actuel
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.currentUser.collect { user ->
                _binding?.let { binding ->
                    user?.let {
                        binding.textTeacherName.text = "${it.firstName} ${it.lastName}"
                    }
                }
            }
        }
        
        // Observer les statistiques
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.dashboardStats.collect { stats ->
                _binding?.let { binding ->
                    binding.textCoursesCount.text = stats.totalCourses.toString()
                    binding.textStudentsCount.text = stats.totalStudents.toString()
                    binding.textResourcesCount.text = stats.totalResources.toString()
                    binding.textPendingEvaluations.text = stats.pendingEvaluations.toString()
                }
            }
        }
        
        // Observer les cours récents
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.recentCourses.collect { courses ->
                _binding?.let { 
                    recentCoursesAdapter.submitList(courses)
                }
            }
        }
        
        // Observer les erreurs
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.errorMessage.collect { error ->
                _binding?.let { binding ->
                    error?.let {
                        com.google.android.material.snackbar.Snackbar.make(
                            binding.root, 
                            it, 
                            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                        ).show()
                        dashboardViewModel.clearError()
                    }
                }
            }
        }
    }
    
    private fun showCreateCourseOptions() {
        val options = arrayOf(
            "📚 Créer un nouveau cours",
            "📋 Utiliser un modèle",
            "📂 Importer un cours",
            "🔄 Dupliquer un cours existant"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Créer un cours")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showMessage("Navigation vers création de cours")
                    1 -> showCourseTemplates()
                    2 -> showMessage("Import de cours en développement")
                    3 -> showDuplicateCourseDialog()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showCourseTemplates() {
        val templates = arrayOf(
            "💻 Cours de Programmation",
            "🎨 Cours de Design",
            "📊 Cours de Marketing",
            "🔬 Cours de Sciences",
            "🌍 Cours de Langues"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Choisir un modèle")
            .setItems(templates) { _, which ->
                showMessage("Modèle sélectionné: ${templates[which]}")
            }
            .setNegativeButton("Retour", null)
            .show()
    }
    
    private fun showDuplicateCourseDialog() {
        val courses = arrayOf(
            "JavaScript ES6+",
            "React Avancé", 
            "Node.js Backend",
            "Python Data Science"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Dupliquer un cours")
            .setItems(courses) { _, which ->
                showMessage("Duplication de: ${courses[which]}")
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showQuizzesQuickActions() {
        val actions = arrayOf(
            "📝 Créer un nouveau quiz",
            "📊 Voir les résultats récents",
            "⏰ Quiz en attente de correction",
            "📈 Statistiques des quiz",
            "🔄 Dupliquer un quiz existant"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Gestion des Quiz")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> showMessage("Navigation vers création de quiz")
                    1 -> showRecentQuizResults()
                    2 -> showPendingCorrections()
                    3 -> showQuizStatistics()
                    4 -> showMessage("Duplication de quiz en développement")
                }
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showRecentQuizResults() {
        val results = """
📊 RÉSULTATS RÉCENTS (7 derniers jours)

📝 Quiz: Variables JavaScript
   • 12 nouvelles soumissions
   • Note moyenne: 16.8/20
   • Taux de réussite: 85%

📝 Quiz: Fonctions Asynchrones  
   • 8 nouvelles soumissions
   • Note moyenne: 14.2/20
   • Taux de réussite: 72%

📝 Quiz: Modules ES6
   • 5 nouvelles soumissions
   • Note moyenne: 18.1/20
   • Taux de réussite: 95%

🎯 TENDANCES
   • +15% participation cette semaine
   • +2.3 points de moyenne générale
   • Temps moyen: 12 minutes
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Résultats Récents")
            .setMessage(results)
            .setPositiveButton("Voir détails", null)
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showPendingCorrections() {
        val pending = """
⏰ CORRECTIONS EN ATTENTE (3)

📝 Quiz: Programmation Orientée Objet
   • 4 soumissions à corriger
   • Soumis il y a 2 heures
   • Questions ouvertes: 2/10

📝 Quiz: Algorithmes Avancés
   • 2 soumissions à corriger  
   • Soumis il y a 5 heures
   • Questions ouvertes: 3/15

📝 Quiz: Projet Final
   • 1 soumission à corriger
   • Soumis il y a 1 jour
   • Questions ouvertes: 5/8

⚡ Action requise: 7 corrections au total
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⏰ Corrections en Attente")
            .setMessage(pending)
            .setPositiveButton("Commencer corrections") { _, _ ->
                showMessage("Navigation vers corrections")
            }
            .setNegativeButton("Plus tard", null)
            .show()
    }
    
    private fun showQuizStatistics() {
        val stats = """
📈 STATISTIQUES GLOBALES DES QUIZ

📊 PERFORMANCE (30 derniers jours)
   • 45 quiz créés
   • 234 soumissions totales
   • Note moyenne: 15.7/20
   • Taux de réussite: 78%

⏱️ ENGAGEMENT
   • Temps moyen par quiz: 14 min
   • Taux de participation: 89%
   • Tentatives multiples: 23%

🎯 TOP PERFORMERS
   • Ahmed Ben Ali: 19.2/20 moyenne
   • Fatima Zahra: 18.8/20 moyenne  
   • Leila Mansouri: 18.5/20 moyenne

📉 DIFFICULTÉS IDENTIFIÉES
   • Programmation asynchrone: 65% réussite
   • Gestion d'erreurs: 58% réussite
   • Optimisation: 52% réussite
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📈 Statistiques Quiz")
            .setMessage(stats)
            .setPositiveButton("Export PDF") { _, _ ->
                showMessage("Export en développement")
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showMessagesQuickActions() {
        val actions = arrayOf(
            "💬 Nouveaux messages (5)",
            "📢 Envoyer annonce générale",
            "👥 Messages de groupe",
            "❓ Questions fréquentes",
            "📋 Modèles de réponse"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Messages")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> showNewMessages()
                    1 -> showCreateAnnouncement()
                    2 -> showMessage("Messages de groupe en développement")
                    3 -> showFAQ()
                    4 -> showMessageTemplates()
                }
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showNewMessages() {
        val messages = """
💬 NOUVEAUX MESSAGES (5)

👤 Ahmed Ben Ali - Il y a 2h
   "Pouvez-vous expliquer les closures en JavaScript?"

👤 Fatima Zahra - Il y a 3h  
   "Le projet final doit-il inclure des tests unitaires?"

👤 Mohamed Tounsi - Il y a 5h
   "Problème avec l'installation de Node.js"

👤 Leila Mansouri - Il y a 1 jour
   "Merci pour le cours sur les Promises!"

👤 Karim Hadj - Il y a 2 jours
   "Quand aura lieu le prochain cours en direct?"
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("💬 Nouveaux Messages")
            .setMessage(messages)
            .setPositiveButton("Répondre") { _, _ ->
                showMessage("Navigation vers messages")
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showCreateAnnouncement() {
        val input = android.widget.EditText(requireContext())
        input.hint = "Tapez votre annonce ici..."
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📢 Nouvelle Annonce")
            .setMessage("Cette annonce sera envoyée à tous vos étudiants:")
            .setView(input)
            .setPositiveButton("Envoyer") { _, _ ->
                val announcement = input.text.toString()
                if (announcement.isNotEmpty()) {
                    showMessage("Annonce envoyée: $announcement")
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showFAQ() {
        val faq = """
❓ QUESTIONS FRÉQUENTES

Q: Comment installer Node.js?
R: Téléchargez depuis nodejs.org et suivez l'assistant d'installation.

Q: Où trouver les exercices pratiques?
R: Dans l'onglet "Contenus" de chaque cours.

Q: Comment soumettre un projet?
R: Utilisez le bouton "Soumettre" dans la section Quiz.

Q: Les cours sont-ils enregistrés?
R: Oui, tous les cours live sont disponibles en replay.

Q: Comment contacter le professeur?
R: Via l'onglet Messages ou par email.
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("❓ FAQ")
            .setMessage(faq)
            .setPositiveButton("Ajouter FAQ") { _, _ ->
                showMessage("Ajout de FAQ en développement")
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showMessageTemplates() {
        val templates = arrayOf(
            "✅ Félicitations pour votre progression",
            "📚 Rappel: nouveau contenu disponible", 
            "⏰ Date limite approche",
            "❓ Besoin d'aide avec le cours?",
            "🎯 Encouragement personnalisé"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📋 Modèles de Messages")
            .setItems(templates) { _, which ->
                showMessage("Modèle sélectionné: ${templates[which]}")
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showDetailedStats() {
        val detailedStats = """
📊 STATISTIQUES DÉTAILLÉES - TABLEAU DE BORD

👨‍🏫 VOTRE ACTIVITÉ
   • 8 cours actifs
   • 156 étudiants au total
   • 89% taux de satisfaction
   • 4.7/5 évaluation moyenne

📈 CETTE SEMAINE
   • +12 nouvelles inscriptions
   • +34 soumissions de quiz
   • +67 heures d'engagement
   • +8 messages reçus

🎯 PERFORMANCE DES COURS
   • JavaScript ES6+: 24 étudiants (78% complétion)
   • React Avancé: 18 étudiants (85% complétion)
   • Node.js Backend: 22 étudiants (65% complétion)
   • Python Data Science: 15 étudiants (92% complétion)

💡 RECOMMANDATIONS
   • Ajouter plus d'exercices pratiques en Node.js
   • Organiser une session Q&A pour JavaScript
   • Créer des quiz intermédiaires pour React
   • Féliciter les étudiants Python (excellent taux!)

🏆 ACHIEVEMENTS RÉCENTS
   • 🥇 Top Teacher du mois
   • 📚 100+ heures de contenu créé
   • ⭐ 50+ avis 5 étoiles reçus
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Statistiques Complètes")
            .setMessage(detailedStats)
            .setPositiveButton("Export Rapport") { _, _ ->
                showMessage("Export rapport en développement")
            }
            .setNeutralButton("Partager") { _, _ ->
                showMessage("Partage en développement")
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun showMessage(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root, 
            message, 
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}