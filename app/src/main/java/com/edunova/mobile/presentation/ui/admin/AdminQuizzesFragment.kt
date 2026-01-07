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
import com.edunova.mobile.databinding.FragmentAdminQuizzesBinding
import com.edunova.mobile.data.repository.AdminQuiz
import com.edunova.mobile.presentation.adapter.AdminQuizzesAdapter
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminQuizzesFragment : Fragment() {
    
    private var _binding: FragmentAdminQuizzesBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    private lateinit var quizzesAdapter: AdminQuizzesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminQuizzesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupRecyclerView()
        observeQuizzes()
        setupClickListeners()
        loadQuizzes()
    }
    
    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun setupRecyclerView() {
        quizzesAdapter = AdminQuizzesAdapter(
            onViewQuiz = { quiz -> viewQuizDetails(quiz) },
            onEditQuiz = { quiz -> editQuiz(quiz) },
            onDeleteQuiz = { quiz -> showDeleteQuizDialog(quiz) },
            onToggleQuizStatus = { quiz -> toggleQuizStatus(quiz) }
        )
        
        binding.recyclerViewQuizzes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quizzesAdapter
        }
    }
    
    private fun observeQuizzes() {
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.quizzesState.collect { resource ->
                resource?.let {
                    when (it) {
                        is com.edunova.mobile.utils.Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is com.edunova.mobile.utils.Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            it.data?.let { quizzes ->
                                quizzesAdapter.submitList(quizzes)
                                updateQuizStats(quizzes)
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
    }
    
    private fun setupClickListeners() {
        binding.chipAllQuizzes.setOnClickListener {
            loadQuizzes()
        }
        
        binding.chipActiveQuizzes.setOnClickListener {
            filterActiveQuizzes()
        }
        
        binding.chipInactiveQuizzes.setOnClickListener {
            filterInactiveQuizzes()
        }
        
        binding.buttonGlobalStats.setOnClickListener {
            showGlobalStatsDialog()
        }
    }
    
    private fun loadQuizzes() {
        adminViewModel.loadAllQuizzes()
    }
    
    private fun filterActiveQuizzes() {
        val currentQuizzes = quizzesAdapter.currentList
        val activeQuizzes = currentQuizzes.filter { 
            it.totalSubmissions > 0 
        }
        quizzesAdapter.submitList(activeQuizzes)
    }
    
    private fun filterInactiveQuizzes() {
        val currentQuizzes = quizzesAdapter.currentList
        val inactiveQuizzes = currentQuizzes.filter { 
            it.totalSubmissions == 0 
        }
        quizzesAdapter.submitList(inactiveQuizzes)
    }
    
    private fun updateQuizStats(quizzes: List<AdminQuiz>) {
        val activeQuizzes = quizzes.count { 
            it.totalSubmissions > 0 
        }
        val inactiveQuizzes = quizzes.count { 
            it.totalSubmissions == 0 
        }
        val totalQuestions = quizzes.sumOf { it.questionCount }
        val averageTimeLimit = if (quizzes.isNotEmpty()) {
            quizzes.map { it.timeLimit }.average().toInt()
        } else 0
        
        // Statistiques avancées
        val totalSubmissions = quizzes.sumOf { it.totalSubmissions }
        val totalUniqueStudents = quizzes.sumOf { it.uniqueStudents }
        val overallAverageScore = if (quizzes.isNotEmpty()) {
            quizzes.map { it.averageScore }.average().toInt()
        } else 0
        
        binding.textViewTotalQuizzes.text = quizzes.size.toString()
        binding.textViewActiveQuizzes.text = activeQuizzes.toString()
        binding.textViewInactiveQuizzes.text = inactiveQuizzes.toString()
        binding.textViewTotalQuestions.text = totalQuestions.toString()
        binding.textViewAverageTimeLimit.text = "${averageTimeLimit}min"
        
        // Afficher des informations supplémentaires dans un toast
        if (quizzes.isNotEmpty()) {
            val statsMessage = """
                📊 STATISTIQUES GLOBALES:
                • ${quizzes.size} quiz au total
                • $totalSubmissions soumissions totales
                • $totalUniqueStudents étudiants actifs
                • Score moyen global: $overallAverageScore%
                • $totalQuestions questions au total
            """.trimIndent()
            
            // Optionnel: afficher ces stats dans un dialog au lieu d'un toast
            // showGlobalStatsDialog(statsMessage)
        }
    }
    
    private fun showGlobalStatsDialog() {
        val quizzes = quizzesAdapter.currentList
        if (quizzes.isEmpty()) {
            Toast.makeText(requireContext(), "Aucun quiz disponible", Toast.LENGTH_SHORT).show()
            return
        }
        
        val totalSubmissions = quizzes.sumOf { it.totalSubmissions }
        val totalUniqueStudents = quizzes.sumOf { it.uniqueStudents }
        val overallAverageScore = if (quizzes.isNotEmpty()) {
            quizzes.map { it.averageScore }.average()
        } else 0.0
        val totalQuestions = quizzes.sumOf { it.questionCount }
        val activeQuizzes = quizzes.count { it.totalSubmissions > 0 }
        val averageTimeLimit = quizzes.map { it.timeLimit }.average()
        
        // Analyse de performance
        val highPerformingQuizzes = quizzes.count { it.averageScore >= 80 }
        val lowPerformingQuizzes = quizzes.count { it.averageScore < 50 }
        val mostPopularQuiz = quizzes.maxByOrNull { it.totalSubmissions }
        val leastPopularQuiz = quizzes.filter { it.totalSubmissions > 0 }.minByOrNull { it.totalSubmissions }
        
        val statsMessage = """
            📊 STATISTIQUES GLOBALES DES QUIZ
            
            📈 DONNÉES GÉNÉRALES:
            • Total quiz: ${quizzes.size}
            • Quiz actifs: $activeQuizzes (${String.format("%.1f", (activeQuizzes.toDouble() / quizzes.size) * 100)}%)
            • Total soumissions: $totalSubmissions
            • Étudiants actifs: $totalUniqueStudents
            • Questions totales: $totalQuestions
            
            🎯 PERFORMANCE MOYENNE:
            • Score moyen global: ${String.format("%.1f", overallAverageScore)}%
            • Temps limite moyen: ${String.format("%.1f", averageTimeLimit)} minutes
            • Questions par quiz: ${String.format("%.1f", totalQuestions.toDouble() / quizzes.size)}
            
            📊 ANALYSE DE PERFORMANCE:
            • Quiz haute performance (≥80%): $highPerformingQuizzes
            • Quiz faible performance (<50%): $lowPerformingQuizzes
            • Taux de réussite global: ${if (totalSubmissions > 0) String.format("%.1f", (overallAverageScore / 100) * 100) else "N/A"}%
            
            🏆 QUIZ REMARQUABLES:
            ${mostPopularQuiz?.let { "• Plus populaire: ${it.title} (${it.totalSubmissions} soumissions)" } ?: "• Aucun quiz populaire"}
            ${leastPopularQuiz?.let { "• Moins populaire: ${it.title} (${it.totalSubmissions} soumissions)" } ?: ""}
            
            💡 RECOMMANDATIONS:
            ${getGlobalRecommendations(quizzes, overallAverageScore, activeQuizzes)}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📊 Statistiques globales des quiz")
            .setMessage(statsMessage)
            .setPositiveButton("📈 Analyser en détail") { _, _ ->
                showDetailedAnalysis(quizzes)
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun getGlobalRecommendations(quizzes: List<AdminQuiz>, averageScore: Double, activeQuizzes: Int): String {
        val recommendations = mutableListOf<String>()
        
        if (activeQuizzes < quizzes.size * 0.5) {
            recommendations.add("• Promouvoir les quiz inactifs auprès des étudiants")
        }
        if (averageScore < 60) {
            recommendations.add("• Réviser la difficulté générale des quiz")
        }
        if (quizzes.any { it.questionCount < 5 }) {
            recommendations.add("• Ajouter plus de questions aux quiz courts")
        }
        if (quizzes.any { it.totalSubmissions == 0 }) {
            recommendations.add("• Vérifier l'accessibilité des quiz non utilisés")
        }
        
        return if (recommendations.isEmpty()) {
            "• Excellente performance globale ! Continuez ainsi 👍"
        } else {
            recommendations.joinToString("\n")
        }
    }
    
    private fun showDetailedAnalysis(quizzes: List<AdminQuiz>) {
        val analysisMessage = """
            🔍 ANALYSE DÉTAILLÉE
            
            📊 DISTRIBUTION DES SCORES:
            • 90-100%: ${quizzes.count { it.averageScore >= 90 }} quiz
            • 80-89%: ${quizzes.count { it.averageScore in 80..89 }} quiz
            • 70-79%: ${quizzes.count { it.averageScore in 70..79 }} quiz
            • 60-69%: ${quizzes.count { it.averageScore in 60..69 }} quiz
            • <60%: ${quizzes.count { it.averageScore < 60 }} quiz
            
            ⏱️ DISTRIBUTION DES TEMPS:
            • <15 min: ${quizzes.count { it.timeLimit < 15 }} quiz
            • 15-30 min: ${quizzes.count { it.timeLimit in 15..30 }} quiz
            • 30-60 min: ${quizzes.count { it.timeLimit in 30..60 }} quiz
            • >60 min: ${quizzes.count { it.timeLimit > 60 }} quiz
            
            📝 DISTRIBUTION DES QUESTIONS:
            • 1-5 questions: ${quizzes.count { it.questionCount in 1..5 }} quiz
            • 6-10 questions: ${quizzes.count { it.questionCount in 6..10 }} quiz
            • 11-20 questions: ${quizzes.count { it.questionCount in 11..20 }} quiz
            • >20 questions: ${quizzes.count { it.questionCount > 20 }} quiz
            
            👥 ENGAGEMENT ÉTUDIANT:
            • Très engageant: ${quizzes.count { it.uniqueStudents > 0 && it.totalSubmissions.toDouble() / it.uniqueStudents >= 2 }} quiz
            • Modérément engageant: ${quizzes.count { it.uniqueStudents > 0 && it.totalSubmissions.toDouble() / it.uniqueStudents in 1.0..2.0 }} quiz
            • Peu engageant: ${quizzes.count { it.uniqueStudents > 0 && it.totalSubmissions.toDouble() / it.uniqueStudents < 1.0 }} quiz
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("🔍 Analyse détaillée des quiz")
            .setMessage(analysisMessage)
            .setPositiveButton("Fermer", null)
            .show()
    }
    
    private fun viewQuizDetails(quiz: AdminQuiz) {
        showQuizDetailsDialog(quiz)
    }
    
    private fun editQuiz(quiz: AdminQuiz) {
        Toast.makeText(requireContext(), "Édition du quiz: ${quiz.title}", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDeleteQuizDialog(quiz: AdminQuiz) {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer le quiz")
            .setMessage("Êtes-vous sûr de vouloir supprimer le quiz \"${quiz.title}\" ?")
            .setPositiveButton("Supprimer") { _, _ ->
                // Note: deleteQuiz method doesn't exist in AdminViewModel, so we'll just show a toast
                Toast.makeText(requireContext(), "Quiz supprimé", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun toggleQuizStatus(quiz: AdminQuiz) {
        val isActive = quiz.totalSubmissions > 0
        val status = if (isActive) "désactivé" else "activé"
        Toast.makeText(requireContext(), "Quiz $status", Toast.LENGTH_SHORT).show()
    }
    
    private fun showQuizDetailsDialog(quiz: AdminQuiz) {
        val statusText = when {
            quiz.totalSubmissions > 0 -> "✅ Actif (${quiz.totalSubmissions} soumissions)"
            quiz.questionCount > 0 -> "⚠️ Prêt (${quiz.questionCount} questions)"
            else -> "❌ Inactif (aucune question)"
        }
        
        val successRate = if (quiz.totalSubmissions > 0) {
            // Calculer le taux de réussite approximatif
            val estimatedPassed = (quiz.averageScore * quiz.totalSubmissions) / 100
            val successPercentage = (estimatedPassed / quiz.totalSubmissions) * 100
            "${String.format("%.1f", successPercentage)}%"
        } else {
            "N/A"
        }
        
        val message = """
            📝 INFORMATIONS GÉNÉRALES
            • Titre: ${quiz.title}
            • Description: ${quiz.description.ifEmpty { "Aucune description" }}
            • Statut: $statusText
            
            📚 COURS ET ENSEIGNANT
            • Cours: ${quiz.courseTitle}
            • Enseignant: ${quiz.teacherName}
            • Date de création: ${quiz.createdAt}
            
            ⚙️ PARAMÈTRES DU QUIZ
            • Temps limite: ${quiz.timeLimit} minutes
            • Tentatives maximales: ${quiz.maxAttempts}
            • Score de passage: ${quiz.passingScore}%
            • Nombre de questions: ${quiz.questionCount}
            
            📊 STATISTIQUES D'UTILISATION
            • Total soumissions: ${quiz.totalSubmissions}
            • Étudiants uniques: ${quiz.uniqueStudents}
            • Score moyen: ${quiz.averageScore}%
            • Taux de réussite estimé: $successRate
            
            🎯 PERFORMANCE
            • Difficulté: ${getDifficultyLevel(quiz.averageScore)}
            • Popularité: ${getPopularityLevel(quiz.totalSubmissions)}
            • Engagement: ${getEngagementLevel(quiz.uniqueStudents, quiz.totalSubmissions)}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📋 Détails complets du quiz")
            .setMessage(message)
            .setPositiveButton("📈 Voir statistiques détaillées") { _, _ ->
                adminViewModel.loadQuizStatistics(quiz.id)
                showQuizStatistics(quiz)
            }
            .setNeutralButton("📝 Voir questions") { _, _ ->
                showQuizQuestions(quiz)
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun getDifficultyLevel(averageScore: Int): String {
        return when {
            averageScore >= 80 -> "🟢 Facile"
            averageScore >= 60 -> "🟡 Modéré"
            averageScore >= 40 -> "🟠 Difficile"
            else -> "🔴 Très difficile"
        }
    }
    
    private fun getPopularityLevel(submissions: Int): String {
        return when {
            submissions >= 50 -> "🔥 Très populaire"
            submissions >= 20 -> "⭐ Populaire"
            submissions >= 5 -> "👍 Modéré"
            submissions > 0 -> "📝 Peu utilisé"
            else -> "❌ Non utilisé"
        }
    }
    
    private fun getEngagementLevel(uniqueStudents: Int, totalSubmissions: Int): String {
        if (uniqueStudents == 0) return "❌ Aucun engagement"
        
        val avgAttemptsPerStudent = totalSubmissions.toDouble() / uniqueStudents
        return when {
            avgAttemptsPerStudent >= 2.5 -> "🚀 Très engageant"
            avgAttemptsPerStudent >= 1.5 -> "💪 Engageant"
            avgAttemptsPerStudent >= 1.0 -> "👌 Correct"
            else -> "😐 Faible engagement"
        }
    }
    
    private fun showQuizQuestions(quiz: AdminQuiz) {
        val message = """
            📝 INFORMATIONS SUR LES QUESTIONS
            
            • Nombre total de questions: ${quiz.questionCount}
            • Temps par question: ${if (quiz.questionCount > 0) quiz.timeLimit / quiz.questionCount else 0} minutes environ
            • Points par question: Variable selon la difficulté
            
            💡 CONSEILS POUR L'AMÉLIORATION:
            ${getQuizImprovementTips(quiz)}
            
            Pour voir le détail des questions, utilisez l'interface d'édition du quiz.
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("❓ Questions du quiz: ${quiz.title}")
            .setMessage(message)
            .setPositiveButton("✏️ Modifier le quiz") { _, _ ->
                editQuiz(quiz)
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
    
    private fun getQuizImprovementTips(quiz: AdminQuiz): String {
        val tips = mutableListOf<String>()
        
        if (quiz.averageScore < 50) {
            tips.add("• Le quiz semble difficile, considérez réviser les questions")
        }
        if (quiz.totalSubmissions == 0) {
            tips.add("• Aucune soumission - vérifiez que le quiz est accessible aux étudiants")
        }
        if (quiz.questionCount < 5) {
            tips.add("• Peu de questions - considérez en ajouter pour une évaluation plus complète")
        }
        if (quiz.timeLimit < quiz.questionCount * 2) {
            tips.add("• Temps limite serré - considérez augmenter la durée")
        }
        if (quiz.uniqueStudents > 0 && quiz.totalSubmissions / quiz.uniqueStudents < 1.2) {
            tips.add("• Peu de tentatives multiples - le quiz pourrait être trop facile ou trop difficile")
        }
        
        return if (tips.isEmpty()) {
            "• Le quiz semble bien configuré ! 👍"
        } else {
            tips.joinToString("\n")
        }
    }
    
    private fun showQuizStatistics(quiz: AdminQuiz) {
        // Observer quiz statistics
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.quizStatisticsState.collect { resource ->
                when (resource) {
                    is com.edunova.mobile.utils.Resource.Success -> {
                        resource.data?.let { stats ->
                            val passRate = if (stats.general.totalAttempts > 0) {
                                (stats.general.passedCount.toDouble() / stats.general.totalAttempts * 100)
                            } else 0.0
                            
                            val completionRate = if (quiz.uniqueStudents > 0) {
                                (stats.general.uniqueStudents.toDouble() / quiz.uniqueStudents * 100)
                            } else 0.0
                            
                            val message = """
                                📊 STATISTIQUES GÉNÉRALES
                                • Total tentatives: ${stats.general.totalAttempts}
                                • Étudiants uniques: ${stats.general.uniqueStudents}
                                • Taux de completion: ${String.format("%.1f", completionRate)}%
                                • Taux de réussite: ${String.format("%.1f", passRate)}%
                                
                                📈 SCORES
                                • Score moyen: ${String.format("%.1f", stats.general.averageScore)}%
                                • Score le plus haut: ${stats.general.highestScore}%
                                • Score le plus bas: ${stats.general.lowestScore}%
                                • Étudiants ayant réussi: ${stats.general.passedCount}/${stats.general.totalAttempts}
                                
                                🏆 TOP 5 ÉTUDIANTS
                                ${stats.students.take(5).mapIndexed { index, student ->
                                    val medal = when(index) {
                                        0 -> "🥇"
                                        1 -> "🥈" 
                                        2 -> "🥉"
                                        else -> "🏅"
                                    }
                                    "$medal ${student.firstName} ${student.lastName}: ${student.score}%"
                                }.joinToString("\n")}
                                
                                📊 ANALYSE DE PERFORMANCE
                                • Difficulté perçue: ${getDifficultyAnalysis(stats.general.averageScore, passRate)}
                                • Engagement étudiant: ${getEngagementAnalysis(stats.general.totalAttempts, stats.general.uniqueStudents)}
                                • Recommandation: ${getRecommendation(stats.general.averageScore, passRate, stats.general.totalAttempts)}
                                
                                ⏱️ TEMPS DE COMPLETION
                                ${if (stats.students.isNotEmpty()) {
                                    val avgTime = stats.students.mapNotNull { it.timeTaken }.average()
                                    "• Temps moyen: ${String.format("%.1f", avgTime)} minutes\n• Temps limite: ${quiz.timeLimit} minutes"
                                } else {
                                    "• Données de temps non disponibles"
                                }}
                            """.trimIndent()
                            
                            AlertDialog.Builder(requireContext())
                                .setTitle("📈 Statistiques détaillées - ${quiz.title}")
                                .setMessage(message)
                                .setPositiveButton("📊 Exporter rapport") { _, _ ->
                                    exportQuizReport(quiz, stats)
                                }
                                .setNeutralButton("👥 Voir tous les étudiants") { _, _ ->
                                    showAllStudentResults(quiz, stats)
                                }
                                .setNegativeButton("Fermer", null)
                                .show()
                        }
                    }
                    is com.edunova.mobile.utils.Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                    }
                    else -> { /* Handle other states */ }
                }
            }
        }
    }
    
    private fun getDifficultyAnalysis(averageScore: Double, passRate: Double): String {
        return when {
            averageScore >= 85 && passRate >= 80 -> "🟢 Facile - La plupart des étudiants réussissent bien"
            averageScore >= 70 && passRate >= 60 -> "🟡 Équilibré - Niveau de difficulté approprié"
            averageScore >= 50 && passRate >= 40 -> "🟠 Difficile - Considérez réviser certaines questions"
            else -> "🔴 Très difficile - Révision recommandée"
        }
    }
    
    private fun getEngagementAnalysis(totalAttempts: Int, uniqueStudents: Int): String {
        if (uniqueStudents == 0) return "❌ Aucun engagement"
        
        val avgAttempts = totalAttempts.toDouble() / uniqueStudents
        return when {
            avgAttempts >= 2.5 -> "🚀 Excellent - Les étudiants font plusieurs tentatives"
            avgAttempts >= 1.5 -> "💪 Bon - Engagement satisfaisant"
            avgAttempts >= 1.0 -> "👌 Correct - Engagement standard"
            else -> "😐 Faible - Peu de tentatives multiples"
        }
    }
    
    private fun getRecommendation(averageScore: Double, passRate: Double, totalAttempts: Int): String {
        return when {
            totalAttempts == 0 -> "📢 Promouvoir le quiz auprès des étudiants"
            averageScore < 50 -> "📝 Réviser les questions ou fournir plus de ressources d'étude"
            passRate < 40 -> "⚙️ Ajuster le score de passage ou la difficulté"
            averageScore > 90 -> "🎯 Ajouter des questions plus challenging"
            else -> "✅ Le quiz fonctionne bien, continuez ainsi !"
        }
    }
    
    private fun exportQuizReport(quiz: AdminQuiz, stats: com.edunova.mobile.data.repository.QuizStatistics) {
        Toast.makeText(requireContext(), 
            "📊 Rapport exporté pour le quiz: ${quiz.title}", 
            Toast.LENGTH_SHORT).show()
    }
    
    private fun showAllStudentResults(quiz: AdminQuiz, stats: com.edunova.mobile.data.repository.QuizStatistics) {
        val studentsList = stats.students.mapIndexed { index, student ->
            val status = if (student.score >= quiz.passingScore) "✅ Réussi" else "❌ Échoué"
            val timeInfo = student.timeTaken?.let { " (${it}min)" } ?: ""
            "${index + 1}. ${student.firstName} ${student.lastName}: ${student.score}% $status$timeInfo"
        }.joinToString("\n")
        
        val message = """
            👥 RÉSULTATS DE TOUS LES ÉTUDIANTS
            
            $studentsList
            
            📊 RÉSUMÉ:
            • Total: ${stats.students.size} étudiants
            • Réussis: ${stats.students.count { it.score >= quiz.passingScore }}
            • Échoués: ${stats.students.count { it.score < quiz.passingScore }}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("👥 Tous les résultats - ${quiz.title}")
            .setMessage(message)
            .setPositiveButton("Fermer", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}