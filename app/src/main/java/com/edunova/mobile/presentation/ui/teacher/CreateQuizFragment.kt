package com.edunova.mobile.presentation.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.databinding.FragmentCreateQuizBinding
import com.edunova.mobile.domain.model.QuizQuestion
import com.edunova.mobile.domain.model.QuizOption
import com.edunova.mobile.domain.model.QuestionType
import com.edunova.mobile.presentation.adapter.QuizQuestionAdapter
import com.edunova.mobile.presentation.viewmodel.QuizViewModel
import com.edunova.mobile.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateQuizFragment : Fragment() {
    
    private var _binding: FragmentCreateQuizBinding? = null
    private val binding get() = _binding!!
    
    private val args: CreateQuizFragmentArgs by navArgs()
    private val quizViewModel: QuizViewModel by viewModels()
    
    private lateinit var questionAdapter: QuizQuestionAdapter
    private val questions = mutableListOf<QuizQuestion>()
    
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
    }
    
    private fun setupUI() {
        binding.apply {
            // Configuration des dropdowns
            val questionTypes = arrayOf("Choix multiple", "Vrai/Faux", "Réponse courte", "Essai")
            // TODO: Configurer les adapters pour les dropdowns
        }
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
                    saveQuiz()
                }
            }
            
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
            
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
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
        viewLifecycleOwner.lifecycleScope.launch {
            quizViewModel.successMessage.collect { message ->
                if (message != null) {
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    quizViewModel.clearMessages()
                    
                    // Attendre un peu puis naviguer vers la liste des quiz
                    kotlinx.coroutines.delay(1500)
                    findNavController().navigateUp()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            quizViewModel.errorMessage.collect { message ->
                if (message != null) {
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    quizViewModel.clearMessages()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            quizViewModel.isLoading.collect { isLoading ->
                _binding?.let { binding ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    binding.btnSaveQuiz.isEnabled = !isLoading
                    binding.btnSaveQuiz.text = if (isLoading) "Création..." else "Créer le Quiz"
                }
            }
        }
    }
    
    private fun addNewQuestion() {
        val questionText = binding.etQuestionText.text.toString().trim()
        val questionType = getSelectedQuestionType()
        
        if (questionText.isEmpty()) {
            binding.tilQuestionText.error = "Veuillez saisir une question"
            return
        }
        
        val newQuestion = QuizQuestion(
            id = 0, // Sera généré par le serveur
            assignmentId = 0, // Sera défini lors de la sauvegarde
            questionText = questionText,
            questionType = questionType,
            options = getQuestionOptions(),
            correctAnswer = getCorrectAnswer(),
            points = binding.etPoints.text.toString().toIntOrNull() ?: 1,
            orderIndex = questions.size
        )
        
        questions.add(newQuestion)
        questionAdapter.submitList(questions.toList())
        
        // Réinitialiser le formulaire de question
        clearQuestionForm()
        
        updateQuestionCount()
    }
    
    private fun editQuestion(question: QuizQuestion, position: Int) {
        // TODO: Implémenter l'édition de question
        // Peut ouvrir un dialog ou remplir le formulaire
    }
    
    private fun deleteQuestion(position: Int) {
        if (position < questions.size) {
            questions.removeAt(position)
            questionAdapter.submitList(questions.toList())
            updateQuestionCount()
        }
    }
    
    private fun getSelectedQuestionType(): QuestionType {
        // TODO: Récupérer le type sélectionné dans le dropdown
        return QuestionType.MULTIPLE_CHOICE
    }
    
    private fun getQuestionOptions(): List<QuizOption> {
        val options = mutableListOf<QuizOption>()
        binding.apply {
            if (etOption1.text.toString().trim().isNotEmpty()) {
                options.add(QuizOption(id = "a", text = etOption1.text.toString().trim()))
            }
            if (etOption2.text.toString().trim().isNotEmpty()) {
                options.add(QuizOption(id = "b", text = etOption2.text.toString().trim()))
            }
            if (etOption3.text.toString().trim().isNotEmpty()) {
                options.add(QuizOption(id = "c", text = etOption3.text.toString().trim()))
            }
            if (etOption4.text.toString().trim().isNotEmpty()) {
                options.add(QuizOption(id = "d", text = etOption4.text.toString().trim()))
            }
        }
        return options
    }
    
    private fun getCorrectAnswer(): String? {
        // TODO: Récupérer la bonne réponse selon le type de question
        return binding.etCorrectAnswer.text.toString().trim().ifEmpty { null }
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
        }
    }
    
    private fun updateQuestionCount() {
        binding.tvQuestionCount.text = "${questions.size} question(s)"
        
        val totalPoints = questions.sumOf { it.points }
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
                Snackbar.make(binding.root, "Ajoutez au moins une question", Snackbar.LENGTH_LONG).show()
                isValid = false
            }
        }
        
        return isValid
    }
    
    private fun saveQuiz() {
        val title = binding.etQuizTitle.text.toString().trim()
        val description = binding.etQuizDescription.text.toString().trim()
        val timeLimit = binding.etTimeLimit.text.toString().toIntOrNull()
        val maxAttempts = binding.etMaxAttempts.text.toString().toIntOrNull() ?: 1
        val passingScore = binding.etPassingScore.text.toString().toDoubleOrNull() ?: 0.0
        
        quizViewModel.createQuiz(
            courseId = args.courseId,
            title = title,
            description = description,
            questions = questions,
            timeLimit = timeLimit,
            maxAttempts = maxAttempts,
            passingScore = passingScore
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}