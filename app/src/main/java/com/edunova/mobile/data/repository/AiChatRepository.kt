package com.edunova.mobile.data.repository

import com.edunova.mobile.data.remote.api.*
import com.edunova.mobile.domain.model.*
import com.edunova.mobile.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiChatRepository @Inject constructor(
    private val aiChatApiService: AiChatApiService
) {
    
    // Conversation history for context awareness
    private val conversationHistory = mutableMapOf<String, MutableList<AiChatMessage>>()
    
    // Enhanced AI knowledge base with more sophisticated responses
    private val aiKnowledgeBase = AiKnowledgeBase()
    
    // Context tracking for better responses
    private val contextTracker = AiContextTracker()
    
    fun sendMessage(message: String, sessionId: String, userRole: String): Flow<Resource<AiChatMessage>> = flow {
        try {
            emit(Resource.Loading())
            
            // Add message to conversation history
            val userMessage = AiChatMessage(
                id = System.currentTimeMillis().toString(),
                content = message,
                isFromUser = true,
                timestamp = System.currentTimeMillis()
            )
            
            addToHistory(sessionId, userMessage)
            
            // Simulate realistic AI response time (1-3 seconds)
            val responseTime = (1000..3000).random().toLong()
            kotlinx.coroutines.delay(responseTime)
            
            // Update context with user message
            contextTracker.updateContext(sessionId, message, userRole)
            
            // Generate intelligent response
            val response = generateIntelligentResponse(message, sessionId, userRole)
            val suggestions = generateContextualSuggestions(message, userRole)
            
            val aiMessage = AiChatMessage(
                id = System.currentTimeMillis().toString(),
                content = response,
                isFromUser = false,
                timestamp = System.currentTimeMillis(),
                suggestions = suggestions.take(3).map { it.text }
            )
            
            addToHistory(sessionId, aiMessage)
            
            emit(Resource.Success(aiMessage))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur de communication avec l'IA"))
        }
    }
    
    fun getSuggestions(userRole: String): Flow<Resource<List<AiSuggestion>>> = flow {
        try {
            emit(Resource.Loading())
            val suggestions = getSuggestionsForRole(userRole)
            emit(Resource.Success(suggestions))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur lors de la récupération des suggestions"))
        }
    }
    
    private fun addToHistory(sessionId: String, message: AiChatMessage) {
        if (!conversationHistory.containsKey(sessionId)) {
            conversationHistory[sessionId] = mutableListOf()
        }
        conversationHistory[sessionId]?.add(message)
        
        // Keep only last 20 messages for performance
        conversationHistory[sessionId]?.let { history ->
            if (history.size > 20) {
                history.removeFirst()
            }
        }
    }
    
    private fun generateIntelligentResponse(message: String, sessionId: String, userRole: String): String {
        val history = conversationHistory[sessionId] ?: emptyList()
        val context = contextTracker.getContext(sessionId)
        val cleanMessage = message.lowercase().trim()
        
        // Check for conversation patterns and context
        val response = when {
            // Greeting patterns
            isGreeting(cleanMessage) -> generateGreetingResponse(userRole, history.size > 2)
            
            // Question patterns
            isQuestion(cleanMessage) -> generateQuestionResponse(cleanMessage, userRole, context)
            
            // Help requests
            isHelpRequest(cleanMessage) -> generateHelpResponse(userRole, context)
            
            // Specific domain queries
            isDomainSpecific(cleanMessage) -> generateDomainResponse(cleanMessage, userRole, context)
            
            // Follow-up responses
            isFollowUp(cleanMessage, history) -> generateFollowUpResponse(cleanMessage, history, userRole)
            
            // Emotional support
            needsMotivation(cleanMessage) -> generateMotivationalResponse(userRole)
            
            // Default intelligent response
            else -> generateContextualResponse(cleanMessage, userRole, context, history)
        }
        
        return response
    }
    
    private fun generateGreetingResponse(userRole: String, isReturning: Boolean): String {
        val greetings = when (userRole) {
            "admin" -> if (isReturning) {
                listOf(
                    "👋 Content de vous revoir ! Comment puis-je vous assister avec l'administration aujourd'hui ?",
                    "🔧 Bonjour ! Prêt à gérer votre plateforme EduNova ?",
                    "👋 Salut ! Quelles tâches administratives puis-je vous aider à accomplir ?"
                )
            } else {
                listOf(
                    "👋 Bonjour ! Je suis votre assistant IA administratif. Je peux vous aider avec la gestion des utilisateurs, les statistiques, les cours et bien plus encore !",
                    "🤖 Salut ! Assistant administratif EduNova à votre service. Comment puis-je optimiser votre gestion aujourd'hui ?",
                    "👋 Bienvenue ! Je suis là pour simplifier vos tâches d'administration. Par où commençons-nous ?"
                )
            }
            
            "enseignant" -> if (isReturning) {
                listOf(
                    "👋 Ravi de vous retrouver ! Comment vont vos cours ?",
                    "📚 Salut ! Prêt à créer du contenu pédagogique inspirant ?",
                    "👋 Bonjour ! Comment puis-je vous aider avec vos étudiants aujourd'hui ?"
                )
            } else {
                listOf(
                    "👋 Bonjour ! Je suis votre assistant pédagogique IA. Je peux vous aider à créer des cours engageants, concevoir des quiz efficaces et suivre vos étudiants !",
                    "🎓 Salut ! Assistant enseignant EduNova ici. Ensemble, rendons l'apprentissage extraordinaire !",
                    "👋 Bienvenue ! Je suis spécialisé dans l'aide aux enseignants. Comment puis-je enrichir votre pédagogie ?"
                )
            }
            
            else -> if (isReturning) {
                listOf(
                    "👋 Content de te revoir ! Comment se passent tes études ?",
                    "📚 Salut ! Prêt à apprendre de nouvelles choses ?",
                    "👋 Hey ! Comment puis-je t'accompagner dans ton apprentissage aujourd'hui ?"
                )
            } else {
                listOf(
                    "👋 Salut ! Je suis ton assistant d'apprentissage IA. Je peux t'aider avec tes cours, tes quiz, ton organisation et te motiver dans tes études !",
                    "🎯 Hey ! Assistant étudiant EduNova ici. Ensemble, atteignons tes objectifs d'apprentissage !",
                    "👋 Bienvenue ! Je suis là pour t'accompagner dans ton parcours éducatif. Comment puis-je t'aider ?"
                )
            }
        }
        
        return greetings.random()
    }
    
    private fun generateQuestionResponse(question: String, userRole: String, context: AiContext): String {
        return when {
            question.contains("comment") && question.contains("créer") -> {
                when (userRole) {
                    "admin" -> "🔧 Pour créer du contenu en tant qu'admin, vous avez accès à tous les outils. Souhaitez-vous créer des utilisateurs, des cours, ou des quiz ? Je peux vous guider étape par étape !"
                    "enseignant" -> "📚 Excellente question ! Pour créer du contenu engageant, commencez par définir vos objectifs pédagogiques. Voulez-vous créer un cours ou un quiz ? Je peux vous donner des conseils spécifiques !"
                    else -> "🎯 Pour bien créer tes projets d'étude, commence par organiser tes idées. Sur quoi travailles-tu ? Je peux t'aider à structurer ton approche !"
                }
            }
            
            question.contains("pourquoi") -> {
                "🤔 C'est une excellente question ! Le 'pourquoi' est souvent la clé de la compréhension. Pouvez-vous me donner plus de contexte sur ce qui vous intrigue ?"
            }
            
            question.contains("quand") -> {
                "⏰ La gestion du temps est cruciale ! Pouvez-vous préciser de quelle échéance ou planification vous parlez ? Je peux vous aider à organiser votre planning."
            }
            
            question.contains("où") -> {
                "📍 Pour vous orienter au mieux, pouvez-vous me dire dans quelle section de l'application ou quel domaine vous cherchez cette information ?"
            }
            
            else -> generateContextualQuestionResponse(question, userRole, context)
        }
    }
    
    private fun generateHelpResponse(userRole: String, context: AiContext): String {
        val capabilities = when (userRole) {
            "admin" -> listOf(
                "👥 Gestion des utilisateurs (création, modification, suppression)",
                "📊 Analyse des statistiques et rapports détaillés",
                "📚 Administration des cours et du contenu",
                "📝 Supervision des quiz et évaluations",
                "💾 Gestion des sauvegardes et maintenance",
                "🔧 Configuration système et paramètres"
            )
            
            "enseignant" -> listOf(
                "📚 Création et gestion de cours interactifs",
                "📝 Conception de quiz et évaluations",
                "👥 Suivi et analyse des performances étudiantes",
                "💬 Communication avec les étudiants",
                "📊 Génération de rapports pédagogiques",
                "🎯 Conseils pédagogiques personnalisés"
            )
            
            else -> listOf(
                "📚 Navigation dans tes cours et ressources",
                "📝 Préparation aux quiz et examens",
                "📊 Suivi de tes progrès et résultats",
                "💬 Communication avec tes enseignants",
                "🎯 Définition et suivi d'objectifs d'apprentissage",
                "💪 Motivation et conseils d'étude"
            )
        }
        
        return "🤖 Je suis là pour vous aider ! Voici ce que je peux faire pour vous :\n\n" +
                capabilities.joinToString("\n") +
                "\n\nDites-moi simplement ce dont vous avez besoin, et je vous guiderai !"
    }
    
    private fun generateDomainResponse(message: String, userRole: String, context: AiContext): String {
        return when {
            message.contains("cours") -> generateCourseResponse(userRole, context)
            message.contains("quiz") -> generateQuizResponse(userRole, context)
            message.contains("étudiant") || message.contains("student") -> generateStudentResponse(userRole, context)
            message.contains("statistique") || message.contains("stats") -> generateStatsResponse(userRole, context)
            message.contains("problème") || message.contains("erreur") -> generateProblemResponse(userRole, context)
            else -> generateGenericDomainResponse(message, userRole, context)
        }
    }
    
    private fun generateFollowUpResponse(message: String, history: List<AiChatMessage>, userRole: String): String {
        val lastAiMessage = history.lastOrNull { !it.isFromUser }?.content ?: ""
        
        return when {
            message.contains("oui") || message.contains("d'accord") -> {
                "👍 Parfait ! Comment souhaitez-vous procéder ? Je peux vous donner des instructions détaillées ou vous guider étape par étape."
            }
            
            message.contains("non") || message.contains("pas vraiment") -> {
                "🤔 Je comprends. Pouvez-vous me dire ce qui ne correspond pas à vos attentes ? Je peux adapter ma réponse à vos besoins spécifiques."
            }
            
            message.contains("plus") || message.contains("détail") -> {
                "📋 Bien sûr ! Je vais vous donner plus de détails. Sur quel aspect souhaitez-vous que je me concentre ?"
            }
            
            else -> {
                "🤖 Je vois que vous continuez notre conversation. Pouvez-vous préciser votre demande pour que je puisse mieux vous aider ?"
            }
        }
    }
    
    private fun generateMotivationalResponse(userRole: String): String {
        val motivationalMessages = when (userRole) {
            "admin" -> listOf(
                "💪 Gérer une plateforme éducative est un défi noble ! Votre travail impacte directement la réussite de nombreux apprenants.",
                "🌟 Chaque amélioration que vous apportez au système bénéficie à toute la communauté éducative. Continuez !",
                "🚀 L'administration efficace est la base d'un apprentissage de qualité. Vous faites un travail essentiel !"
            )
            
            "enseignant" -> listOf(
                "🎓 Enseigner, c'est allumer des flammes dans l'esprit des étudiants. Votre passion fait la différence !",
                "💡 Chaque cours que vous créez peut transformer la vie d'un étudiant. Votre impact est immense !",
                "🌱 Vous plantez des graines de connaissance qui grandiront longtemps après vos cours. Continuez à inspirer !"
            )
            
            else -> listOf(
                "🎯 Chaque effort d'apprentissage vous rapproche de vos objectifs. Vous progressez plus que vous ne le pensez !",
                "💪 Les défis d'aujourd'hui sont les compétences de demain. Persévérez, vous êtes sur la bonne voie !",
                "🌟 Apprendre demande du courage et de la patience. Vous avez les deux ! Continuez à briller !",
                "🚀 Votre curiosité et votre détermination sont vos plus grands atouts. Gardez cette énergie !"
            )
        }
        
        return motivationalMessages.random()
    }
    
    private fun generateContextualResponse(message: String, userRole: String, context: AiContext, history: List<AiChatMessage>): String {
        // Analyze conversation flow and context
        val recentTopics = context.recentTopics.takeLast(3)
        val conversationLength = history.size
        
        return when {
            conversationLength > 10 -> {
                "🤖 Nous avons eu une belle conversation ! Y a-t-il autre chose sur lequel je peux vous aider aujourd'hui ?"
            }
            
            recentTopics.isNotEmpty() -> {
                "🔄 Je vois que nous parlions de ${recentTopics.last()}. Souhaitez-vous approfondir ce sujet ou aborder autre chose ?"
            }
            
            else -> {
                val responses = when (userRole) {
                    "admin" -> listOf(
                        "🤖 En tant qu'administrateur, vous avez accès à de nombreux outils. Que souhaitez-vous accomplir ?",
                        "🔧 Je peux vous aider avec toutes les tâches administratives. Quelle est votre priorité actuelle ?",
                        "📊 Voulez-vous que nous examinions les performances du système ou gérons du contenu ?"
                    )
                    
                    "enseignant" -> listOf(
                        "🎓 Comment puis-je vous aider à améliorer l'expérience d'apprentissage de vos étudiants ?",
                        "📚 Travaillons ensemble sur vos projets pédagogiques. Que préparez-vous actuellement ?",
                        "💡 Avez-vous des idées de cours ou d'activités que vous aimeriez développer ?"
                    )
                    
                    else -> listOf(
                        "🎯 Comment puis-je t'accompagner dans ton apprentissage aujourd'hui ?",
                        "📚 Sur quels sujets aimerais-tu progresser ? Je suis là pour t'aider !",
                        "💪 Raconte-moi tes défis actuels, nous trouverons des solutions ensemble !"
                    )
                }
                
                responses.random()
            }
        }
    }
    
    // Helper methods for pattern recognition
    private fun isGreeting(message: String): Boolean {
        val greetingWords = listOf("bonjour", "salut", "hello", "hi", "hey", "bonsoir", "bonne")
        return greetingWords.any { message.contains(it) }
    }
    
    private fun isQuestion(message: String): Boolean {
        val questionWords = listOf("comment", "pourquoi", "quand", "où", "que", "qui", "quoi", "combien")
        return questionWords.any { message.contains(it) } || message.endsWith("?")
    }
    
    private fun isHelpRequest(message: String): Boolean {
        val helpWords = listOf("aide", "help", "assistance", "soutien", "guide", "expliquer")
        return helpWords.any { message.contains(it) }
    }
    
    private fun isDomainSpecific(message: String): Boolean {
        val domainWords = listOf("cours", "quiz", "étudiant", "statistique", "problème", "erreur", "admin", "enseignant")
        return domainWords.any { message.contains(it) }
    }
    
    private fun isFollowUp(message: String, history: List<AiChatMessage>): Boolean {
        if (history.size < 2) return false
        val followUpWords = listOf("oui", "non", "d'accord", "pas vraiment", "plus", "détail", "aussi", "encore")
        return followUpWords.any { message.contains(it) } && message.length < 50
    }
    
    private fun needsMotivation(message: String): Boolean {
        val motivationWords = listOf("difficile", "dur", "compliqué", "décourager", "abandonner", "fatigue", "stress")
        return motivationWords.any { message.contains(it) }
    }
    
    // Domain-specific response generators
    private fun generateCourseResponse(userRole: String, context: AiContext): String {
        return when (userRole) {
            "admin" -> "📚 Pour la gestion des cours, vous pouvez créer, modifier, activer/désactiver et organiser le contenu. Quelle action souhaitez-vous effectuer ?"
            "enseignant" -> "📚 Créer un cours efficace nécessite une structure claire et des objectifs définis. Voulez-vous que je vous guide dans la création ou l'amélioration d'un cours ?"
            else -> "📚 Pour bien suivre tes cours, organise ton temps d'étude et prends des notes actives. Sur quel cours as-tu besoin d'aide ?"
        }
    }
    
    private fun generateQuizResponse(userRole: String, context: AiContext): String {
        return when (userRole) {
            "admin" -> "📝 Vous pouvez superviser tous les quiz, voir les statistiques et gérer leur disponibilité. Que souhaitez-vous faire ?"
            "enseignant" -> "📝 Un bon quiz évalue la compréhension réelle. Je peux vous aider à créer des questions pertinentes et variées. Quel type de quiz préparez-vous ?"
            else -> "📝 Pour réussir tes quiz, révise régulièrement et pratique avec des exercices. As-tu un quiz à préparer prochainement ?"
        }
    }
    
    private fun generateStudentResponse(userRole: String, context: AiContext): String {
        return when (userRole) {
            "admin" -> "👥 Vous pouvez gérer tous les comptes étudiants, voir leurs progrès et résoudre leurs problèmes. Quelle action administrative voulez-vous effectuer ?"
            "enseignant" -> "👥 Pour mieux accompagner vos étudiants, variez vos méthodes et donnez des retours constructifs. Avez-vous des préoccupations spécifiques concernant vos étudiants ?"
            else -> "👥 Si tu as besoin d'aide avec tes camarades ou veux contacter un enseignant, je peux te guider. Que se passe-t-il ?"
        }
    }
    
    private fun generateStatsResponse(userRole: String, context: AiContext): String {
        return when (userRole) {
            "admin" -> "📊 Vous avez accès à toutes les statistiques : utilisateurs actifs, performances des cours, résultats des quiz. Quelles données vous intéressent ?"
            "enseignant" -> "📊 Je peux vous aider à analyser les performances de vos étudiants et l'efficacité de vos cours. Quelles métriques voulez-vous examiner ?"
            else -> "📊 Tu peux suivre tes progrès, tes résultats aux quiz et ton avancement dans les cours. Veux-tu voir tes statistiques ?"
        }
    }
    
    private fun generateProblemResponse(userRole: String, context: AiContext): String {
        return "🔧 Je suis là pour vous aider à résoudre les problèmes ! Pouvez-vous me décrire précisément ce qui ne fonctionne pas ? Plus vous me donnez de détails, mieux je peux vous assister."
    }
    
    private fun generateGenericDomainResponse(message: String, userRole: String, context: AiContext): String {
        return "🤖 Je comprends votre demande concernant '${message.take(30)}...'. Pouvez-vous me donner plus de contexte pour que je puisse vous aider de manière plus précise ?"
    }
    
    private fun generateContextualQuestionResponse(question: String, userRole: String, context: AiContext): String {
        return "❓ C'est une question intéressante ! Pour vous donner la meilleure réponse possible, pouvez-vous me préciser le contexte ou l'aspect spécifique qui vous préoccupe ?"
    }
    
    private fun generateContextualSuggestions(message: String, userRole: String): List<AiSuggestion> {
        val baseSuggestions = getSuggestionsForRole(userRole)
        
        // Add contextual suggestions based on message content
        val contextualSuggestions = mutableListOf<AiSuggestion>()
        
        when {
            message.contains("cours") -> {
                contextualSuggestions.add(AiSuggestion("ctx1", "📚 Voir tous les cours", "view_courses", "📚"))
                if (userRole != "etudiant") {
                    contextualSuggestions.add(AiSuggestion("ctx2", "➕ Créer un nouveau cours", "create_course", "➕"))
                }
            }
            
            message.contains("quiz") -> {
                contextualSuggestions.add(AiSuggestion("ctx3", "📝 Voir les quiz", "view_quizzes", "📝"))
                if (userRole != "etudiant") {
                    contextualSuggestions.add(AiSuggestion("ctx4", "➕ Créer un quiz", "create_quiz", "➕"))
                }
            }
            
            message.contains("aide") || message.contains("help") -> {
                contextualSuggestions.add(AiSuggestion("ctx5", "❓ Guide d'utilisation", "help_guide", "❓"))
                contextualSuggestions.add(AiSuggestion("ctx6", "📞 Support technique", "support", "📞"))
            }
        }
        
        return (contextualSuggestions + baseSuggestions.shuffled()).take(6)
    }
    
    private fun getSuggestionsForRole(userRole: String): List<AiSuggestion> {
        return when (userRole) {
            "admin" -> AiSuggestions.ADMIN_SUGGESTIONS
            "enseignant" -> AiSuggestions.TEACHER_SUGGESTIONS
            "etudiant" -> AiSuggestions.STUDENT_SUGGESTIONS
            else -> AiSuggestions.STUDENT_SUGGESTIONS
        }
    }
}

// Enhanced AI Context Tracking
data class AiContext(
    val sessionId: String,
    val userRole: String,
    val recentTopics: MutableList<String> = mutableListOf(),
    val userPreferences: MutableMap<String, String> = mutableMapOf(),
    val conversationFlow: MutableList<String> = mutableListOf(),
    var lastActivity: Long = System.currentTimeMillis()
)

class AiContextTracker {
    private val contexts = mutableMapOf<String, AiContext>()
    
    fun updateContext(sessionId: String, message: String, userRole: String) {
        val context = contexts.getOrPut(sessionId) { 
            AiContext(sessionId, userRole) 
        }
        
        // Extract topics from message
        val topics = extractTopics(message)
        topics.forEach { topic ->
            if (!context.recentTopics.contains(topic)) {
                context.recentTopics.add(topic)
                if (context.recentTopics.size > 5) {
                    context.recentTopics.removeAt(0)
                }
            }
        }
        
        // Track conversation flow
        context.conversationFlow.add(message.take(50))
        if (context.conversationFlow.size > 10) {
            context.conversationFlow.removeAt(0)
        }
        
        context.lastActivity = System.currentTimeMillis()
    }
    
    fun getContext(sessionId: String): AiContext {
        return contexts[sessionId] ?: AiContext(sessionId, "etudiant")
    }
    
    private fun extractTopics(message: String): List<String> {
        val topicKeywords = mapOf(
            "cours" to listOf("cours", "course", "leçon", "matière"),
            "quiz" to listOf("quiz", "test", "examen", "évaluation"),
            "étudiant" to listOf("étudiant", "student", "élève", "apprenant"),
            "enseignant" to listOf("enseignant", "teacher", "professeur", "formateur"),
            "statistiques" to listOf("statistique", "stats", "données", "rapport"),
            "aide" to listOf("aide", "help", "assistance", "support"),
            "problème" to listOf("problème", "erreur", "bug", "dysfonctionnement")
        )
        
        val foundTopics = mutableListOf<String>()
        val lowerMessage = message.lowercase()
        
        topicKeywords.forEach { (topic, keywords) ->
            if (keywords.any { lowerMessage.contains(it) }) {
                foundTopics.add(topic)
            }
        }
        
        return foundTopics
    }
}

// Enhanced AI Knowledge Base
class AiKnowledgeBase {
    
    fun getEducationalTips(subject: String): List<String> {
        return when (subject.lowercase()) {
            "mathématiques", "maths" -> listOf(
                "Pratiquez régulièrement avec des exercices variés",
                "Comprenez les concepts avant de mémoriser les formules",
                "Utilisez des exemples concrets pour visualiser les problèmes"
            )
            
            "sciences" -> listOf(
                "Reliez la théorie à des expériences pratiques",
                "Posez-vous toujours 'pourquoi' et 'comment'",
                "Utilisez des schémas et diagrammes pour comprendre"
            )
            
            "langues" -> listOf(
                "Pratiquez l'expression orale régulièrement",
                "Immergez-vous dans la culture de la langue",
                "N'ayez pas peur de faire des erreurs, c'est normal !"
            )
            
            else -> listOf(
                "Organisez vos notes de manière claire et structurée",
                "Révisez régulièrement plutôt que tout à la dernière minute",
                "Posez des questions quand vous ne comprenez pas"
            )
        }
    }
    
    fun getMotivationalQuotes(): List<String> {
        return listOf(
            "💪 'Le succès, c'est tomber sept fois et se relever huit fois.' - Proverbe japonais",
            "🌟 'L'éducation est l'arme la plus puissante pour changer le monde.' - Nelson Mandela",
            "🎯 'La seule façon d'apprendre les mathématiques est de faire des mathématiques.' - Paul Halmos",
            "🚀 'L'apprentissage n'est jamais fait sans erreur et défaite.' - Vladimir Lénine",
            "💡 'Dis-moi et j'oublie, enseigne-moi et je me souviens, implique-moi et j'apprends.' - Benjamin Franklin"
        )
    }
    
    fun getStudyTechniques(): List<String> {
        return listOf(
            "🍅 Technique Pomodoro : 25 min de travail, 5 min de pause",
            "🗂️ Méthode Cornell : divisez vos notes en sections",
            "🔄 Révision espacée : révisez à intervalles croissants",
            "🎯 Technique Feynman : expliquez le concept simplement",
            "🗺️ Mind mapping : créez des cartes mentales visuelles"
        )
    }
}