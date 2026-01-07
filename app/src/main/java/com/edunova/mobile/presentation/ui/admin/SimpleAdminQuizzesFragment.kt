package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
class SimpleAdminQuizzesFragment : Fragment() {
    
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
        
        try {
            setupBackButton()
            setupRecyclerView()
            setupClickListeners()
            observeQuizzes()
            loadQuizzes()
            
            Toast.makeText(requireContext(), "Fragment quiz chargé avec succès", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors du chargement: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private var allQuizzes: List<AdminQuiz> = emptyList()
    private var currentFilter: QuizFilter = QuizFilter.ALL
    
    enum class QuizFilter {
        ALL, ACTIVE, INACTIVE
    }
    
    private fun setupRecyclerView() {
        quizzesAdapter = AdminQuizzesAdapter(
            onViewQuiz = { quiz -> 
                showQuizDetailsDialog(quiz)
            },
            onEditQuiz = { quiz -> 
                showEditQuizDialog(quiz)
            },
            onDeleteQuiz = { quiz -> 
                showDeleteQuizDialog(quiz)
            },
            onToggleQuizStatus = { quiz -> 
                toggleQuizStatus(quiz)
            }
        )
        
        binding.recyclerViewQuizzes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quizzesAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.chipAllQuizzes.setOnClickListener {
            currentFilter = QuizFilter.ALL
            updateChipSelection()
            applyFilter()
        }
        
        binding.chipActiveQuizzes.setOnClickListener {
            currentFilter = QuizFilter.ACTIVE
            updateChipSelection()
            applyFilter()
        }
        
        binding.chipInactiveQuizzes.setOnClickListener {
            currentFilter = QuizFilter.INACTIVE
            updateChipSelection()
            applyFilter()
        }
        
        binding.buttonGlobalStats.setOnClickListener {
            Toast.makeText(requireContext(), "Statistiques globales", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateChipSelection() {
        // Reset all chips
        binding.chipAllQuizzes.isChecked = false
        binding.chipActiveQuizzes.isChecked = false
        binding.chipInactiveQuizzes.isChecked = false
        
        // Set selected chip
        when (currentFilter) {
            QuizFilter.ALL -> binding.chipAllQuizzes.isChecked = true
            QuizFilter.ACTIVE -> binding.chipActiveQuizzes.isChecked = true
            QuizFilter.INACTIVE -> binding.chipInactiveQuizzes.isChecked = true
        }
    }
    
    private fun applyFilter() {
        val filteredQuizzes = when (currentFilter) {
            QuizFilter.ALL -> allQuizzes
            QuizFilter.ACTIVE -> allQuizzes.filter { it.status == "active" }
            QuizFilter.INACTIVE -> allQuizzes.filter { it.status == "inactive" }
        }
        
        quizzesAdapter.submitList(filteredQuizzes)
        updateQuizStats(filteredQuizzes)
        
        val filterText = when (currentFilter) {
            QuizFilter.ALL -> "tous les quiz"
            QuizFilter.ACTIVE -> "quiz actifs"
            QuizFilter.INACTIVE -> "quiz inactifs"
        }
        Toast.makeText(requireContext(), "Affichage de ${filteredQuizzes.size} $filterText", Toast.LENGTH_SHORT).show()
    }
    
    private fun observeQuizzes() {
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.quizzesState.collect { resource ->
                resource?.let {
                    when (it) {
                        is com.edunova.mobile.utils.Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            Toast.makeText(requireContext(), "Chargement des quiz...", Toast.LENGTH_SHORT).show()
                        }
                        is com.edunova.mobile.utils.Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            it.data?.let { quizzes ->
                                allQuizzes = quizzes // Stocker tous les quiz
                                Toast.makeText(requireContext(), "${quizzes.size} quiz trouvés", Toast.LENGTH_SHORT).show()
                                applyFilter() // Appliquer le filtre actuel
                            }
                        }
                        is com.edunova.mobile.utils.Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun loadQuizzes() {
        Toast.makeText(requireContext(), "Chargement des quiz...", Toast.LENGTH_SHORT).show()
        adminViewModel.loadAllQuizzes()
    }
    
    private fun toggleQuizStatus(quiz: AdminQuiz) {
        val currentStatus = quiz.status
        val newStatus = if (currentStatus == "active") "inactif" else "actif"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔄 Changer le statut du quiz")
            .setMessage("Voulez-vous changer le statut du quiz \"${quiz.title}\" de $currentStatus à $newStatus ?")
            .setPositiveButton("Confirmer") { _, _ ->
                adminViewModel.toggleQuizStatus(quiz.id)
                observeQuizActionResult()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun activateQuiz(quiz: AdminQuiz) {
        if (quiz.status.lowercase() != "active") {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("✅ Activer le quiz")
                .setMessage("Voulez-vous activer le quiz \"${quiz.title}\" ?\n\nLe quiz deviendra disponible pour les étudiants.")
                .setPositiveButton("Activer") { _, _ ->
                    adminViewModel.toggleQuizStatus(quiz.id)
                    observeQuizActionResult()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }
    
    private fun deactivateQuiz(quiz: AdminQuiz) {
        if (quiz.status.lowercase() != "inactive") {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("❌ Désactiver le quiz")
                .setMessage("Voulez-vous désactiver le quiz \"${quiz.title}\" ?\n\nLe quiz ne sera plus disponible pour les étudiants.")
                .setPositiveButton("Désactiver") { _, _ ->
                    adminViewModel.toggleQuizStatus(quiz.id)
                    observeQuizActionResult()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }
    
    private fun observeQuizActionResult() {
        // Observer le résultat
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.quizActionState.collect { resource ->
                resource?.let {
                    when (it) {
                        is com.edunova.mobile.utils.Resource.Success -> {
                            Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                            // Recharger la liste pour refléter les changements
                            loadQuizzes()
                        }
                        is com.edunova.mobile.utils.Resource.Error -> {
                            Toast.makeText(requireContext(), "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                        is com.edunova.mobile.utils.Resource.Loading -> {
                            // Optionnel: afficher un indicateur de chargement
                        }
                    }
                }
            }
        }
    }
    
    private fun updateQuizStats(quizzes: List<AdminQuiz>) {
        val activeQuizzes = quizzes.count { it.status == "active" }
        val inactiveQuizzes = quizzes.count { it.status == "inactive" }
        val totalQuestions = quizzes.sumOf { it.questionCount }
        val averageTimeLimit = if (quizzes.isNotEmpty()) {
            quizzes.map { it.timeLimit }.average().toInt()
        } else 0
        
        binding.textViewTotalQuizzes.text = quizzes.size.toString()
        binding.textViewActiveQuizzes.text = activeQuizzes.toString()
        binding.textViewInactiveQuizzes.text = inactiveQuizzes.toString()
        binding.textViewTotalQuestions.text = totalQuestions.toString()
        binding.textViewAverageTimeLimit.text = "${averageTimeLimit}min"
    }
    
    private fun showQuizDetailsDialog(quiz: AdminQuiz) {
        val statusText = when {
            quiz.totalSubmissions > 0 -> "✅ Actif (${quiz.totalSubmissions} soumissions)"
            quiz.questionCount > 0 -> "⚠️ Prêt (${quiz.questionCount} questions)"
            else -> "❌ Inactif (aucune question)"
        }
        
        val successRate = if (quiz.totalSubmissions > 0) {
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
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📋 Détails complets du quiz")
            .setMessage(message)
            .setPositiveButton("Fermer", null)
            .show()
    }
    
    private fun showEditQuizDialog(quiz: AdminQuiz) {
        // Créer un dialog personnalisé avec des champs d'édition
        val editTexts = mutableListOf<android.widget.EditText>()
        
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }
        
        // Titre
        val titleEdit = android.widget.EditText(requireContext()).apply {
            hint = "Titre du quiz"
            setText(quiz.title)
        }
        layout.addView(android.widget.TextView(requireContext()).apply { text = "Titre:" })
        layout.addView(titleEdit)
        editTexts.add(titleEdit)
        
        // Description
        val descEdit = android.widget.EditText(requireContext()).apply {
            hint = "Description du quiz"
            setText(quiz.description)
            maxLines = 3
        }
        layout.addView(android.widget.TextView(requireContext()).apply { text = "Description:" })
        layout.addView(descEdit)
        editTexts.add(descEdit)
        
        // Temps limite
        val timeEdit = android.widget.EditText(requireContext()).apply {
            hint = "Temps limite (minutes)"
            setText(quiz.timeLimit.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(android.widget.TextView(requireContext()).apply { text = "Temps limite (minutes):" })
        layout.addView(timeEdit)
        editTexts.add(timeEdit)
        
        // Tentatives max
        val attemptsEdit = android.widget.EditText(requireContext()).apply {
            hint = "Tentatives maximales"
            setText(quiz.maxAttempts.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(android.widget.TextView(requireContext()).apply { text = "Tentatives maximales:" })
        layout.addView(attemptsEdit)
        editTexts.add(attemptsEdit)
        
        // Score de passage
        val scoreEdit = android.widget.EditText(requireContext()).apply {
            hint = "Score de passage (%)"
            setText(quiz.passingScore.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        layout.addView(android.widget.TextView(requireContext()).apply { text = "Score de passage (%):" })
        layout.addView(scoreEdit)
        editTexts.add(scoreEdit)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("✏️ Modifier le quiz")
            .setView(layout)
            .setPositiveButton("Sauvegarder") { _, _ ->
                val title = titleEdit.text.toString().trim()
                val description = descEdit.text.toString().trim()
                val timeLimit = timeEdit.text.toString().toIntOrNull() ?: quiz.timeLimit
                val maxAttempts = attemptsEdit.text.toString().toIntOrNull() ?: quiz.maxAttempts
                val passingScore = scoreEdit.text.toString().toIntOrNull() ?: quiz.passingScore
                
                if (title.isNotEmpty()) {
                    updateQuiz(quiz.id, title, description, timeLimit, maxAttempts, passingScore)
                } else {
                    Toast.makeText(requireContext(), "Le titre ne peut pas être vide", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showDeleteQuizDialog(quiz: AdminQuiz) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Supprimer le quiz")
            .setMessage("Êtes-vous sûr de vouloir supprimer le quiz \"${quiz.title}\" ?\n\nCette action est irréversible et supprimera :\n• Le quiz et toutes ses questions\n• Toutes les soumissions des étudiants\n• Toutes les statistiques associées")
            .setPositiveButton("Supprimer") { _, _ ->
                deleteQuiz(quiz.id)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun updateQuiz(quizId: Int, title: String, description: String, timeLimit: Int, maxAttempts: Int, passingScore: Int) {
        adminViewModel.updateQuiz(quizId, title, description, timeLimit, maxAttempts, passingScore)
        
        // Observer le résultat
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.quizActionState.collect { resource ->
                resource?.let {
                    when (it) {
                        is com.edunova.mobile.utils.Resource.Success -> {
                            Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                        }
                        is com.edunova.mobile.utils.Resource.Error -> {
                            Toast.makeText(requireContext(), "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }
    
    private fun deleteQuiz(quizId: Int) {
        adminViewModel.deleteQuiz(quizId)
        
        // Observer le résultat
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.quizActionState.collect { resource ->
                resource?.let {
                    when (it) {
                        is com.edunova.mobile.utils.Resource.Success -> {
                            Toast.makeText(requireContext(), it.data, Toast.LENGTH_SHORT).show()
                        }
                        is com.edunova.mobile.utils.Resource.Error -> {
                            Toast.makeText(requireContext(), "Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}