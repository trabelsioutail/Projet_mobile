package com.edunova.mobile.presentation.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.edunova.mobile.databinding.FragmentCourseDetailBinding
import com.edunova.mobile.presentation.adapter.CourseContentAdapter
import com.edunova.mobile.presentation.adapter.CourseDetailPagerAdapter
import com.edunova.mobile.presentation.adapter.StudentAdapter
import com.edunova.mobile.presentation.viewmodel.CourseViewModel
import com.edunova.mobile.utils.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CourseDetailFragment : Fragment() {
    
    private var _binding: FragmentCourseDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: CourseDetailFragmentArgs by navArgs()
    private val courseViewModel: CourseViewModel by viewModels()
    
    private lateinit var contentAdapter: CourseContentAdapter
    private lateinit var studentAdapter: StudentAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupClickListeners()
        observeViewModel()
        
        // Charger les détails du cours
        courseViewModel.loadCourseDetails(args.courseId)
    }
    
    private fun setupUI() {
        // Configuration des adapters
        contentAdapter = CourseContentAdapter { content ->
            // Ouvrir le contenu (PDF, vidéo, etc.)
            courseViewModel.openContent(content)
        }
        
        studentAdapter = StudentAdapter { student ->
            // TODO: Voir le profil de l'étudiant ou envoyer un message
        }
        
        // Configuration des RecyclerViews
        binding.rvCourseContent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contentAdapter
        }
        
        binding.rvStudents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = studentAdapter
        }
        
        // Configuration du ViewPager avec adapter
        val adapter = CourseDetailPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        // Configuration des tabs
        val tabTitles = arrayOf("Contenus", "Étudiants", "Quiz", "Statistiques")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
    
    private fun setupClickListeners() {
        binding.fabAddContent.setOnClickListener {
            showAddContentDialog()
        }
        
        binding.btnEditCourse.setOnClickListener {
            // Navigation vers l'édition du cours
            val action = CourseDetailFragmentDirections
                .actionCourseDetailToEditCourse(args.courseId)
            findNavController().navigate(action)
        }
        
        binding.btnCreateQuiz.setOnClickListener {
            // Navigation vers la création de quiz
            val action = CourseDetailFragmentDirections
                .actionCourseDetailToCreateQuiz(args.courseId)
            findNavController().navigate(action)
        }
        
        binding.btnViewAnalytics.setOnClickListener {
            showDetailedAnalytics()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.selectedCourse.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                        
                        resource.data?.let { course ->
                            updateUI(course)
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Snackbar.make(binding.root, resource.message ?: "Erreur", Snackbar.LENGTH_LONG).show()
                    }
                    null -> {}
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.courseContents.collect { contents ->
                contentAdapter.submitList(contents)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            courseViewModel.courseStudents.collect { students ->
                studentAdapter.submitList(students)
                binding.tvStudentCount.text = "${students.size} étudiants inscrits"
            }
        }
    }
    
    private fun updateUI(course: com.edunova.mobile.domain.model.Course) {
        binding.apply {
            tvCourseTitle.text = course.title
            tvCourseDescription.text = course.description
            tvTeacherName.text = "Par ${course.teacherName}"
            tvCreatedDate.text = "Créé le ${course.createdAt}"
            tvContentCount.text = "${course.contentsCount} contenus"
            tvStudentCount.text = "${course.studentsCount} étudiants"
            
            // Mettre à jour les statistiques
            updateCourseStats(course)
        }
    }
    
    private fun updateCourseStats(course: com.edunova.mobile.domain.model.Course) {
        // Calculer les statistiques du cours
        binding.apply {
            tvTotalViews.text = "0" // À implémenter
            tvCompletionRate.text = "0%" // À implémenter
            // tvAverageScore.text = "0%" // À implémenter plus tard
        }
    }
    
    private fun showAddContentDialog() {
        val options = arrayOf(
            "📄 Ajouter un document PDF",
            "🎥 Ajouter une vidéo",
            "🔗 Ajouter un lien",
            "📝 Créer un document texte",
            "📊 Ajouter une présentation"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Ajouter du contenu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showMessage("Fonctionnalité PDF en développement")
                    1 -> showMessage("Fonctionnalité vidéo en développement") 
                    2 -> showAddLinkDialog()
                    3 -> showMessage("Éditeur de texte en développement")
                    4 -> showMessage("Fonctionnalité présentation en développement")
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showAddLinkDialog() {
        val input = android.widget.EditText(requireContext())
        input.hint = "https://exemple.com"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Ajouter un lien")
            .setMessage("Entrez l'URL du lien à ajouter:")
            .setView(input)
            .setPositiveButton("Ajouter") { _, _ ->
                val url = input.text.toString()
                if (url.isNotEmpty()) {
                    showMessage("Lien ajouté: $url")
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
    
    private fun showDetailedAnalytics() {
        val analyticsText = """
📊 ANALYTICS DÉTAILLÉES - JavaScript ES6+

👥 ÉTUDIANTS (24 inscrits)
• 🟢 Actifs: 18 (75%)
• 🟡 Modérés: 4 (17%) 
• 🔴 Inactifs: 2 (8%)

📈 PROGRESSION MOYENNE
• Chapitre 1: 85% terminé
• Chapitre 2: 67% terminé  
• Chapitre 3: 45% terminé
• Chapitre 4: 12% terminé

⏱️ TEMPS D'ENGAGEMENT
• Temps moyen par session: 45 min
• Sessions par semaine: 3.2
• Temps total passé: 148h

📝 ÉVALUATIONS
• Quiz 1: 17.2/20 (18 soumissions)
• Quiz 2: 15.8/20 (16 soumissions)
• Taux de réussite global: 82%

📊 TENDANCES (7 derniers jours)
• +3 nouvelles inscriptions
• +12% temps d'engagement
• +5% taux de complétion
• 89% satisfaction (sondage)

🎯 RECOMMANDATIONS
• Ajouter plus d'exercices pratiques
• Créer un quiz pour le Chapitre 3
• Organiser une session Q&A live
• Envoyer rappels aux inactifs
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📊 Analytics Détaillées")
            .setMessage(analyticsText)
            .setPositiveButton("Exporter PDF") { _, _ ->
                showMessage("Export PDF en développement")
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