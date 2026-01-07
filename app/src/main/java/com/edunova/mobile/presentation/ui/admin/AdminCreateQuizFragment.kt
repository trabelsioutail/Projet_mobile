package com.edunova.mobile.presentation.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.R
import com.edunova.mobile.databinding.FragmentCreateQuizBinding
import com.edunova.mobile.domain.model.QuizQuestion
import com.edunova.mobile.domain.model.QuizOption
import com.edunova.mobile.domain.model.QuestionType
import com.edunova.mobile.presentation.adapter.QuizQuestionAdapter
import com.edunova.mobile.presentation.viewmodel.AdminViewModel
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminCreateQuizFragment : Fragment() {
    
    private var _binding: FragmentCreateQuizBinding? = null
    private val binding get() = _binding!!
    
    private val adminViewModel: AdminViewModel by viewModels()
    
    private lateinit var questionAdapter: QuizQuestionAdapter
    private val questions = mutableListOf<QuizQuestion>()
    private var selectedCourseId: Int = 1 // Default course ID
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateQuizBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupValidation()
        setupClickListeners()
        setupRecyclerView()
        observeViewModel()
        loadCourses()
    }
    
    private fun setupUI() {
        // Modifier le titre de la toolbar pour l'admin
        binding.toolbar.title = "🎯 Créer un Quiz (Admin)"
        
        // Pré-remplir avec des valeurs par défaut
        binding.etMaxAttempts.setText("3")
        binding.etPassingScore.setText("60")
        binding.etTimeLimit.setText("30")
        binding.etPoints.setText("1")
    }
    
    private fun loadCourses() {
        adminViewModel.loadAllCourses()
    }
    
    private fun setupValidation() {
        binding.apply {
            etQuizTitle.addTextChangedListener {
                tilQuizTitle.error = null
                validateForm()
            }
            
            etQuizDescription.addTextChangedListener {
                tilQuizDescription.error = null
                validateForm()
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            btnAddQuestion.setOnClickListener {
                addNewQuestion()
            }
            
            btnSaveQuiz.setOnClickListener {
                if (validateForm()) {
                    showCourseSelectionDialog()
                }
            }
            
            btnCancel.setOnClickListener {
                showCancelConfirmation()
            }
            
            toolbar.setNavigationOnClickListener {
                showCancelConfirmation()
            }
        }
    }
    
    private fun showCourseSelectionDialog() {
        // Forcer le rechargement des cours
        adminViewModel.loadAllCourses()
        
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.coursesState.collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val courses = resource.data ?: emptyList()
                        android.util.Log.d("AdminCreateQuiz", "Courses loaded: ${courses.size}")
                        
                        if (courses.isNotEmpty()) {
                            val courseNames = courses.map { "${it.title} (ID: ${it.id})" }.toTypedArray()
                            
                            AlertDialog.Builder(requireContext())
                                .setTitle("📚 Sélectionner un cours")
                                .setMessage("Choisissez le cours auquel associer ce quiz :\n\n${courses.size} cours disponibles")
                                .setSingleChoiceItems(courseNames, 0) { dialog, which ->
                                    selectedCourseId = courses[which].id
                                    android.util.Log.d("AdminCreateQuiz", "Course selected: ${courses[which].title} (ID: ${selectedCourseId})")
                                    dialog.dismiss()
                                    saveQuiz()
                                }
                                .setNeutralButton("🆕 Créer un cours") { _, _ ->
                                    createDefaultCourseAndSaveQuiz()
                                }
                                .setNegativeButton("❌ Annuler", null)
                                .show()
                        } else {
                            // Aucun cours trouvé - proposer de créer un cours par défaut
                            AlertDialog.Builder(requireContext())
                                .setTitle("📚 Aucun cours disponible")
                                .setMessage("Aucun cours n'a été trouvé dans la base de données.\n\nVoulez-vous créer un cours par défaut pour vos quiz ?")
                                .setPositiveButton("✅ Créer un cours") { _, _ ->
                                    createDefaultCourseAndSaveQuiz()
                                }
                                .setNeutralButton("🔄 Recharger") { _, _ ->
                                    showCourseSelectionDialog() // Retry
                                }
                                .setNegativeButton("❌ Annuler", null)
                                .show()
                        }
                        return@collect
                    }
                    is Resource.Error -> {
                        android.util.Log.e("AdminCreateQuiz", "Error loading courses: ${resource.message}")
                        
                        AlertDialog.Builder(requireContext())
                            .setTitle("❌ Erreur de chargement")
                            .setMessage("Impossible de charger les cours.\n\nErreur: ${resource.message}\n\nVoulez-vous créer un cours par défaut ?")
                            .setPositiveButton("✅ Créer un cours") { _, _ ->
                                createDefaultCourseAndSaveQuiz()
                            }
                            .setNeutralButton("🔄 Réessayer") { _, _ ->
                                showCourseSelectionDialog() // Retry
                            }
                            .setNegativeButton("❌ Annuler", null)
                            .show()
                        return@collect
                    }
                    is Resource.Loading -> {
                        // Show loading - wait for data
                        android.util.Log.d("AdminCreateQuiz", "Loading courses...")
                    }
                    else -> {
                        android.util.Log.d("AdminCreateQuiz", "Courses state: null")
                    }
                }
            }
        }
    }
    
    private fun createDefaultCourseAndSaveQuiz() {
        AlertDialog.Builder(requireContext())
            .setTitle("🆕 Créer un cours par défaut")
            .setMessage("Un cours par défaut va être créé pour vos quiz admin.\n\nTitre: \"Quiz Généraux - Admin\"\nDescription: \"Cours par défaut pour les quiz créés par l'administrateur\"")
            .setPositiveButton("✅ Créer") { _, _ ->
                Toast.makeText(requireContext(), "🏗️ Création du cours par défaut...", Toast.LENGTH_SHORT).show()
                
                // Créer un cours par défaut pour les quiz admin
                adminViewModel.createCourse(
                    title = "Quiz Généraux - Admin",
                    description = "Cours par défaut pour les quiz créés par l'administrateur",
                    teacherId = 1, // Admin teacher ID
                    status = "active",
                    isPublic = true,
                    enrollmentOpen = true
                )
                
                // Observer la création du cours
                viewLifecycleOwner.lifecycleScope.launch {
                    adminViewModel.courseActionState.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                Toast.makeText(requireContext(), "✅ Cours créé avec succès !", Toast.LENGTH_SHORT).show()
                                
                                // Recharger les cours pour obtenir le nouveau cours
                                adminViewModel.loadAllCourses()
                                
                                // Attendre un peu puis utiliser le cours créé
                                kotlinx.coroutines.delay(1000)
                                
                                // Utiliser le premier cours disponible ou un ID par défaut
                                selectedCourseId = 1
                                saveQuiz()
                                return@collect
                            }
                            is Resource.Error -> {
                                Toast.makeText(requireContext(), "❌ Erreur lors de la création du cours: ${resource.message}", Toast.LENGTH_LONG).show()
                                
                                // Fallback - utiliser un ID par défaut
                                selectedCourseId = 1
                                saveQuiz()
                                return@collect
                            }
                            else -> {}
                        }
                    }
                }
            }
            .setNegativeButton("❌ Annuler", null)
            .show()
    }
    
    private fun setupRecyclerView() {
        questionAdapter = QuizQuestionAdapter(
            onEditQuestion = { question, position ->
                editQuestion(question, position)
            },
            onDeleteQuestion = { position ->
                deleteQuestion(position)
            }
        )
        
        binding.rvQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = questionAdapter
        }
    }
    
    private fun observeViewModel() {
        // Observer les actions de quiz
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.quizActionState.collect { resource ->
                resource?.let {
                    when (it) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnSaveQuiz.isEnabled = false
                            binding.btnSaveQuiz.text = "Création en cours..."
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSaveQuiz.isEnabled = true
                            binding.btnSaveQuiz.text = "Créer le Quiz"
                            
                            Toast.makeText(requireContext(), "✅ Quiz créé avec succès !", Toast.LENGTH_LONG).show()
                            
                            // Attendre un peu puis retourner à la liste
                            kotlinx.coroutines.delay(1500)
                            parentFragmentManager.popBackStack()
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSaveQuiz.isEnabled = true
                            binding.btnSaveQuiz.text = "Créer le Quiz"
                            
                            Toast.makeText(requireContext(), "❌ Erreur: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
    
    private fun addNewQuestion() {
        val questionText = binding.etQuestionText.text.toString().trim()
        
        if (questionText.isEmpty()) {
            binding.tilQuestionText.error = "Veuillez saisir une question"
            return
        }
        
        val options = getQuestionOptions()
        if (options.size < 2) {
            Toast.makeText(requireContext(), "Ajoutez au moins 2 options de réponse", Toast.LENGTH_SHORT).show()
            return
        }
        
        val correctAnswer = binding.etCorrectAnswer.text.toString().trim()
        if (correctAnswer.isEmpty()) {
            Toast.makeText(requireContext(), "Spécifiez la bonne réponse", Toast.LENGTH_SHORT).show()
            return
        }
        
        val newQuestion = QuizQuestion(
            id = 0, // Sera généré par le serveur
            assignmentId = 0, // Sera défini lors de la sauvegarde
            questionText = questionText,
            questionType = QuestionType.MULTIPLE_CHOICE,
            options = options,
            correctAnswer = correctAnswer,
            points = binding.etPoints.text.toString().toIntOrNull() ?: 1,
            orderIndex = questions.size
        )
        
        questions.add(newQuestion)
        questionAdapter.submitList(questions.toList())
        
        // Réinitialiser le formulaire de question
        clearQuestionForm()
        updateQuestionCount()
        
        Toast.makeText(requireContext(), "✅ Question ajoutée (${questions.size} questions)", Toast.LENGTH_SHORT).show()
    }
    
    private fun editQuestion(question: QuizQuestion, position: Int) {
        // Remplir le formulaire avec les données de la question
        binding.apply {
            etQuestionText.setText(question.questionText)
            etCorrectAnswer.setText(question.correctAnswer)
            etPoints.setText(question.points.toString())
            
            // Remplir les options
            val options = question.options
            if (options.isNotEmpty()) etOption1.setText(options.getOrNull(0)?.text ?: "")
            if (options.size > 1) etOption2.setText(options.getOrNull(1)?.text ?: "")
            if (options.size > 2) etOption3.setText(options.getOrNull(2)?.text ?: "")
            if (options.size > 3) etOption4.setText(options.getOrNull(3)?.text ?: "")
        }
        
        // Supprimer la question de la liste (elle sera re-ajoutée avec les modifications)
        questions.removeAt(position)
        questionAdapter.submitList(questions.toList())
        updateQuestionCount()
        
        Toast.makeText(requireContext(), "✏️ Question chargée pour modification", Toast.LENGTH_SHORT).show()
    }
    
    private fun deleteQuestion(position: Int) {
        if (position < questions.size) {
            val deletedQuestion = questions[position]
            questions.removeAt(position)
            questionAdapter.submitList(questions.toList())
            updateQuestionCount()
            
            Toast.makeText(requireContext(), "🗑️ Question supprimée", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getQuestionOptions(): List<QuizOption> {
        val options = mutableListOf<QuizOption>()
        binding.apply {
            val option1 = etOption1.text.toString().trim()
            val option2 = etOption2.text.toString().trim()
            val option3 = etOption3.text.toString().trim()
            val option4 = etOption4.text.toString().trim()
            
            if (option1.isNotEmpty()) options.add(QuizOption(id = "A", text = option1))
            if (option2.isNotEmpty()) options.add(QuizOption(id = "B", text = option2))
            if (option3.isNotEmpty()) options.add(QuizOption(id = "C", text = option3))
            if (option4.isNotEmpty()) options.add(QuizOption(id = "D", text = option4))
        }
        return options
    }
    
    private fun clearQuestionForm() {
        binding.apply {
            etQuestionText.text?.clear()
            etOption1.text?.clear()
            etOption2.text?.clear()
            etOption3.text?.clear()
            etOption4.text?.clear()
            etCorrectAnswer.text?.clear()
            etPoints.setText("1")
            
            // Remettre le focus sur la question
            etQuestionText.requestFocus()
        }
    }
    
    private fun updateQuestionCount() {
        val totalPoints = questions.sumOf { it.points }
        binding.tvQuestionCount.text = "${questions.size} question(s)"
        binding.tvTotalPoints.text = "$totalPoints points au total"
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        binding.apply {
            // Validation du titre
            if (etQuizTitle.text.toString().trim().isEmpty()) {
                tilQuizTitle.error = "Le titre est requis"
                isValid = false
            }
            
            // Validation de la description
            if (etQuizDescription.text.toString().trim().isEmpty()) {
                tilQuizDescription.error = "La description est requise"
                isValid = false
            }
            
            // Validation des questions
            if (questions.isEmpty()) {
                Toast.makeText(requireContext(), "❌ Ajoutez au moins une question", Toast.LENGTH_LONG).show()
                isValid = false
            }
        }
        
        return isValid
    }
    
    private fun saveQuiz() {
        val title = binding.etQuizTitle.text.toString().trim()
        val description = binding.etQuizDescription.text.toString().trim()
        val timeLimit = binding.etTimeLimit.text.toString().toIntOrNull() ?: 30
        val maxAttempts = binding.etMaxAttempts.text.toString().toIntOrNull() ?: 3
        val passingScore = binding.etPassingScore.text.toString().toIntOrNull() ?: 60
        
        Toast.makeText(requireContext(), "💾 Sauvegarde du quiz avec ${questions.size} questions...", Toast.LENGTH_SHORT).show()
        
        // Créer le quiz via AdminViewModel avec les questions
        adminViewModel.createQuiz(
            title = title,
            description = description,
            courseId = selectedCourseId,
            timeLimit = timeLimit,
            maxAttempts = maxAttempts,
            passingScore = passingScore,
            questions = questions
        )
    }
    
    private fun showCancelConfirmation() {
        if (questions.isNotEmpty() || 
            binding.etQuizTitle.text.toString().trim().isNotEmpty() ||
            binding.etQuizDescription.text.toString().trim().isNotEmpty()) {
            
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Annuler la création")
                .setMessage("Vous avez des modifications non sauvegardées. Voulez-vous vraiment annuler ?")
                .setPositiveButton("Oui, annuler") { _, _ ->
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton("Continuer l'édition", null)
                .show()
        } else {
            parentFragmentManager.popBackStack()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}