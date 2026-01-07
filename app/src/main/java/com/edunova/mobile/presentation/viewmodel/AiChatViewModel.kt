package com.edunova.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edunova.mobile.data.repository.AiChatRepository
import com.edunova.mobile.domain.model.AiChatMessage
import com.edunova.mobile.domain.model.AiSuggestion
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiChatRepository: AiChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _suggestions = MutableStateFlow<List<AiSuggestion>>(emptyList())
    val suggestions: StateFlow<List<AiSuggestion>> = _suggestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private var currentSessionId = System.currentTimeMillis().toString()
    private var userRole = "etudiant"
    private var conversationStartTime = System.currentTimeMillis()

    fun initializeChat(role: String) {
        userRole = role
        conversationStartTime = System.currentTimeMillis()
        loadSuggestions()
        
        // Enhanced welcome message based on time and role
        val welcomeMessage = AiChatMessage(
            id = "welcome",
            content = getEnhancedWelcomeMessage(role),
            isFromUser = false,
            timestamp = System.currentTimeMillis()
        )
        
        _messages.value = listOf(welcomeMessage)
        
        // Add initial contextual suggestions
        updateSuggestionsBasedOnRole(role)
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        // Show typing indicator
        _isTyping.value = true

        // Add user message immediately for better UX
        val userMessage = AiChatMessage(
            id = System.currentTimeMillis().toString(),
            content = content,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )

        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        // Send to AI repository
        viewModelScope.launch {
            aiChatRepository.sendMessage(content, currentSessionId, userRole).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                        _isTyping.value = true
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _isTyping.value = false
                        resource.data?.let { aiMessage ->
                            _messages.value = _messages.value + aiMessage
                            
                            // Update suggestions based on AI response
                            updateSuggestionsBasedOnResponse(aiMessage.content)
                        }
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _isTyping.value = false
                        _error.value = resource.message
                        
                        // Add error recovery message
                        val errorMessage = AiChatMessage(
                            id = System.currentTimeMillis().toString(),
                            content = "😅 Désolé, j'ai eu un petit problème technique. Pouvez-vous répéter votre question ? Je suis là pour vous aider !",
                            isFromUser = false,
                            timestamp = System.currentTimeMillis()
                        )
                        _messages.value = _messages.value + errorMessage
                    }
                }
            }
        }
    }

    fun sendSuggestion(suggestion: AiSuggestion) {
        // Handle special suggestion actions
        when (suggestion.action) {
            "stats" -> handleStatsAction()
            "users" -> handleUsersAction()
            "courses" -> handleCoursesAction()
            "create_course" -> handleCreateCourseAction()
            "create_quiz" -> handleCreateQuizAction()
            "my_courses" -> handleMyCoursesAction()
            "help_guide" -> handleHelpGuideAction()
            else -> sendMessage(suggestion.text)
        }
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            aiChatRepository.getSuggestions(userRole).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { suggestions ->
                            _suggestions.value = suggestions
                        }
                    }
                    is Resource.Error -> {
                        _error.value = resource.message
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    private fun getEnhancedWelcomeMessage(role: String): String {
        val timeOfDay = getTimeOfDay()
        val personalizedGreeting = when (role) {
            "admin" -> {
                "👋 $timeOfDay ! Je suis votre assistant IA administratif EduNova.\n\n" +
                "🔧 Je peux vous aider avec :\n" +
                "• Gestion des utilisateurs et permissions\n" +
                "• Analyse des statistiques système\n" +
                "• Administration des cours et quiz\n" +
                "• Maintenance et sauvegardes\n\n" +
                "💡 Astuce : Utilisez les suggestions ci-dessous ou posez-moi directement vos questions !"
            }
            
            "enseignant" -> {
                "👋 $timeOfDay ! Je suis votre assistant pédagogique IA.\n\n" +
                "🎓 Je peux vous accompagner pour :\n" +
                "• Créer des cours engageants et structurés\n" +
                "• Concevoir des quiz efficaces\n" +
                "• Analyser les performances de vos étudiants\n" +
                "• Améliorer vos méthodes pédagogiques\n\n" +
                "💡 Partagez vos défis d'enseignement, je suis là pour vous inspirer !"
            }
            
            else -> {
                "👋 $timeOfDay ! Je suis ton assistant d'apprentissage IA.\n\n" +
                "🎯 Je peux t'aider à :\n" +
                "• Organiser tes études efficacement\n" +
                "• Préparer tes quiz et examens\n" +
                "• Comprendre tes cours difficiles\n" +
                "• Rester motivé dans ton parcours\n\n" +
                "💪 Raconte-moi tes objectifs, nous les atteindrons ensemble !"
            }
        }
        
        return personalizedGreeting
    }

    private fun getTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Bonjour"
            in 12..17 -> "Bon après-midi"
            in 18..22 -> "Bonsoir"
            else -> "Bonne nuit"
        }
    }

    private fun updateSuggestionsBasedOnRole(role: String) {
        viewModelScope.launch {
            aiChatRepository.getSuggestions(role).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let { suggestions ->
                            // Add time-based suggestions
                            val enhancedSuggestions = suggestions.toMutableList()
                            
                            // Add contextual suggestions based on time
                            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                            when {
                                hour in 8..10 -> enhancedSuggestions.add(0, 
                                    AiSuggestion("morning", "🌅 Planifier ma journée", "plan_day", "🌅"))
                                hour in 12..14 -> enhancedSuggestions.add(0,
                                    AiSuggestion("lunch", "🍽️ Pause déjeuner productive", "lunch_break", "🍽️"))
                                hour in 17..19 -> enhancedSuggestions.add(0,
                                    AiSuggestion("evening", "📝 Bilan de la journée", "day_summary", "📝"))
                            }
                            
                            _suggestions.value = enhancedSuggestions.take(6)
                        }
                    }
                    is Resource.Error -> {
                        _error.value = resource.message
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    private fun updateSuggestionsBasedOnResponse(response: String) {
        val contextualSuggestions = mutableListOf<AiSuggestion>()
        
        when {
            response.contains("cours") -> {
                contextualSuggestions.add(AiSuggestion("ctx1", "📚 En savoir plus sur les cours", "course_details", "📚"))
                if (userRole != "etudiant") {
                    contextualSuggestions.add(AiSuggestion("ctx2", "➕ Créer un cours", "create_course", "➕"))
                }
            }
            
            response.contains("quiz") -> {
                contextualSuggestions.add(AiSuggestion("ctx3", "📝 Conseils pour les quiz", "quiz_tips", "📝"))
                if (userRole == "etudiant") {
                    contextualSuggestions.add(AiSuggestion("ctx4", "🎯 Techniques de révision", "study_tips", "🎯"))
                }
            }
            
            response.contains("statistique") -> {
                contextualSuggestions.add(AiSuggestion("ctx5", "📊 Voir les détails", "detailed_stats", "📊"))
            }
            
            response.contains("aide") -> {
                contextualSuggestions.add(AiSuggestion("ctx6", "❓ Guide complet", "full_guide", "❓"))
                contextualSuggestions.add(AiSuggestion("ctx7", "💬 Contacter le support", "contact_support", "💬"))
            }
        }
        
        if (contextualSuggestions.isNotEmpty()) {
            _suggestions.value = contextualSuggestions + _suggestions.value.take(4)
        }
    }

    // Action handlers for special suggestions
    private fun handleStatsAction() {
        sendMessage("Peux-tu me montrer les statistiques détaillées du système ?")
    }

    private fun handleUsersAction() {
        sendMessage("Comment puis-je gérer les utilisateurs efficacement ?")
    }

    private fun handleCoursesAction() {
        sendMessage("Aide-moi avec la gestion des cours")
    }

    private fun handleCreateCourseAction() {
        sendMessage("Guide-moi pour créer un nouveau cours étape par étape")
    }

    private fun handleCreateQuizAction() {
        sendMessage("Comment créer un quiz efficace et engageant ?")
    }

    private fun handleMyCoursesAction() {
        sendMessage("Montre-moi mes cours et mes progrès")
    }

    private fun handleHelpGuideAction() {
        val helpMessage = AiChatMessage(
            id = System.currentTimeMillis().toString(),
            content = generateHelpGuide(),
            isFromUser = false,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + helpMessage
    }

    private fun generateHelpGuide(): String {
        return when (userRole) {
            "admin" -> """
                📋 **Guide Administrateur EduNova**
                
                🔧 **Fonctions principales :**
                • Gestion des utilisateurs (création, modification, suppression)
                • Supervision des cours et quiz
                • Analyse des statistiques et rapports
                • Configuration système et sauvegardes
                
                💡 **Conseils d'utilisation :**
                • Utilisez les filtres pour trouver rapidement les informations
                • Consultez régulièrement les statistiques pour optimiser la plateforme
                • Créez des sauvegardes avant les modifications importantes
                
                ❓ **Besoin d'aide ?** Posez-moi vos questions spécifiques !
            """.trimIndent()
            
            "enseignant" -> """
                📚 **Guide Enseignant EduNova**
                
                🎓 **Outils pédagogiques :**
                • Création de cours interactifs et structurés
                • Conception de quiz adaptatifs
                • Suivi des performances étudiantes
                • Communication avec les apprenants
                
                💡 **Bonnes pratiques :**
                • Définissez des objectifs clairs pour chaque cours
                • Variez les types de questions dans vos quiz
                • Donnez des retours constructifs à vos étudiants
                
                🚀 **Prêt à créer ?** Demandez-moi des conseils spécifiques !
            """.trimIndent()
            
            else -> """
                🎯 **Guide Étudiant EduNova**
                
                📚 **Fonctionnalités disponibles :**
                • Accès à tous vos cours et ressources
                • Participation aux quiz et évaluations
                • Suivi de vos progrès et résultats
                • Communication avec vos enseignants
                
                💪 **Conseils pour réussir :**
                • Organisez votre temps d'étude
                • Participez activement aux quiz
                • N'hésitez pas à poser des questions
                • Suivez régulièrement vos progrès
                
                🌟 **Besoin de motivation ?** Je suis là pour t'encourager !
            """.trimIndent()
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearChat() {
        _messages.value = emptyList()
        currentSessionId = System.currentTimeMillis().toString()
        conversationStartTime = System.currentTimeMillis()
        initializeChat(userRole)
    }

    fun exportConversation(): String {
        val messages = _messages.value
        val export = StringBuilder()
        export.append("=== Conversation EduNova AI ===\n")
        export.append("Rôle: $userRole\n")
        export.append("Date: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n")
        export.append("Messages: ${messages.size}\n\n")
        
        messages.forEach { message ->
            val sender = if (message.isFromUser) "Vous" else "Assistant IA"
            val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
            export.append("[$time] $sender: ${message.content}\n\n")
        }
        
        return export.toString()
    }
}