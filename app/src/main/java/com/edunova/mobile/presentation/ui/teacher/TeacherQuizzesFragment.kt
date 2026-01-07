package com.edunova.mobile.presentation.ui.teacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.edunova.mobile.databinding.FragmentTeacherQuizzesBinding
import com.edunova.mobile.presentation.adapter.QuizAdapter
import com.edunova.mobile.presentation.base.BaseFragment
import com.edunova.mobile.presentation.viewmodel.QuizViewModel
import com.edunova.mobile.utils.Resource
import com.edunova.mobile.utils.collectSafely
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeacherQuizzesFragment : BaseFragment<FragmentTeacherQuizzesBinding>() {
    
    private val quizViewModel: QuizViewModel by viewModels()
    private lateinit var quizAdapter: QuizAdapter
    
    override fun createBinding(
        inflater: LayoutInflater, 
        container: ViewGroup?
    ): FragmentTeacherQuizzesBinding {
        return FragmentTeacherQuizzesBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        
        // Charger les quiz
        quizViewModel.loadTeacherQuizzes(1)
    }
    
    override fun observeData() {
        // Observer les quiz avec le nouveau système sécurisé
        quizViewModel.teacherQuizzesState.collectSafely(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    safeWithBinding { binding ->
                        binding.progressBar.visibility = View.VISIBLE
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
                is Resource.Success -> {
                    safeWithBinding { binding ->
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        resource.data?.let { quizzes ->
                            quizAdapter.submitList(quizzes)
                            binding.emptyState.visibility = 
                                if (quizzes.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }
                is Resource.Error -> {
                    safeWithBinding { binding ->
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                    }
                    showError(resource.message ?: "Erreur inconnue")
                }
                null -> {
                    // État initial
                }
            }
        }
        
        // Observer les messages de succès
        quizViewModel.successMessage.collectSafely(viewLifecycleOwner) { message ->
            message?.let {
                showSuccess(it)
                quizViewModel.loadTeacherQuizzes(1)
                quizViewModel.clearMessages()
            }
        }
    }
    
    override fun setupListeners() {
        safeWithBinding { binding ->
            binding.swipeRefresh.setOnRefreshListener {
                quizViewModel.loadTeacherQuizzes(1)
            }
            
            binding.fabCreateQuiz.setOnClickListener {
                ifViewHealthy {
                    val action = TeacherQuizzesFragmentDirections
                        .actionQuizzesToCreateQuiz()
                    findNavController().navigate(action)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Rafraîchir la liste quand on revient sur cet écran
        quizViewModel.loadTeacherQuizzes(1)
    }
    
    private fun setupRecyclerView() {
        quizAdapter = QuizAdapter(
            onQuizClick = { quiz ->
                ifViewHealthy {
                    val action = TeacherQuizzesFragmentDirections
                        .actionQuizzesToQuizDetail(quiz.id)
                    findNavController().navigate(action)
                }
            },
            onEditClick = { quiz ->
                showSuccess("Édition en développement")
            },
            onDeleteClick = { quiz ->
                quizViewModel.deleteQuiz(quiz.id)
            }
        )
        
        safeWithBinding { binding ->
            binding.recyclerViewQuizzes.apply {
                adapter = quizAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        // Déjà configuré dans setupListeners()
    }
    
    private fun setupFab() {
        // Déjà configuré dans setupListeners()
    }
}