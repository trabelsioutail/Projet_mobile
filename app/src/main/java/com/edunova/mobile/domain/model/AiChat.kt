package com.edunova.mobile.domain.model

data class AiChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val messageType: AiMessageType = AiMessageType.TEXT,
    val suggestions: List<String> = emptyList(),
    val quickActions: List<AiQuickAction> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

enum class AiMessageType {
    TEXT,
    SUGGESTION,
    QUICK_ACTION,
    HELP,
    WELCOME,
    ERROR,
    SYSTEM
}

data class AiQuickAction(
    val id: String,
    val title: String,
    val action: String,
    val icon: String,
    val description: String = ""
)

data class AiChatSession(
    val sessionId: String,
    val userRole: String, // admin, enseignant, etudiant
    val messages: List<AiChatMessage>,
    val isActive: Boolean = true,
    val lastActivity: Long = System.currentTimeMillis(),
    val context: Map<String, Any> = emptyMap(),
    val preferences: Map<String, String> = emptyMap()
)

data class AiSuggestion(
    val id: String,
    val text: String,
    val action: String,
    val icon: String,
    val category: String = "general",
    val priority: Int = 0
)

// Enhanced suggestions with categories and context awareness
object AiSuggestions {
    val ADMIN_SUGGESTIONS = listOf(
        AiSuggestion("1", "📊 Voir les statistiques du système", "stats", "📊", "analytics", 1),
        AiSuggestion("2", "👥 Gérer les utilisateurs", "users", "👥", "management", 2),
        AiSuggestion("3", "📚 Gérer les cours", "courses", "📚", "content", 2),
        AiSuggestion("4", "📝 Gérer les quiz", "quizzes", "📝", "content", 2),
        AiSuggestion("5", "📋 Voir les inscriptions", "enrollments", "📋", "management", 3),
        AiSuggestion("6", "💾 Créer une sauvegarde", "backup", "💾", "maintenance", 4),
        AiSuggestion("7", "🔧 Configuration système", "settings", "🔧", "maintenance", 4),
        AiSuggestion("8", "📈 Rapports détaillés", "reports", "📈", "analytics", 3)
    )
    
    val TEACHER_SUGGESTIONS = listOf(
        AiSuggestion("1", "📚 Créer un nouveau cours", "create_course", "📚", "creation", 1),
        AiSuggestion("2", "📝 Créer un quiz", "create_quiz", "📝", "creation", 1),
        AiSuggestion("3", "👥 Voir mes étudiants", "students", "👥", "management", 2),
        AiSuggestion("4", "📊 Statistiques de mes cours", "course_stats", "📊", "analytics", 2),
        AiSuggestion("5", "💬 Messages des étudiants", "messages", "💬", "communication", 3),
        AiSuggestion("6", "📈 Analyser les performances", "analytics", "📈", "analytics", 3),
        AiSuggestion("7", "🎯 Conseils pédagogiques", "teaching_tips", "🎯", "help", 4),
        AiSuggestion("8", "📋 Planifier mes cours", "course_planning", "📋", "planning", 4)
    )
    
    val STUDENT_SUGGESTIONS = listOf(
        AiSuggestion("1", "📚 Voir mes cours", "my_courses", "📚", "learning", 1),
        AiSuggestion("2", "📝 Quiz disponibles", "available_quizzes", "📝", "assessment", 1),
        AiSuggestion("3", "📊 Mes résultats", "my_results", "📊", "progress", 2),
        AiSuggestion("4", "💬 Contacter un enseignant", "contact_teacher", "💬", "communication", 3),
        AiSuggestion("5", "📅 Mon planning", "schedule", "📅", "planning", 2),
        AiSuggestion("6", "🎯 Mes objectifs", "goals", "🎯", "motivation", 3),
        AiSuggestion("7", "💡 Techniques d'étude", "study_tips", "💡", "help", 4),
        AiSuggestion("8", "🏆 Mes réussites", "achievements", "🏆", "motivation", 4)
    )
    
    // Contextual suggestions that appear based on conversation
    val CONTEXTUAL_SUGGESTIONS = mapOf(
        "course_creation" to listOf(
            AiSuggestion("ctx1", "📋 Structure de cours", "course_structure", "📋", "help"),
            AiSuggestion("ctx2", "🎯 Objectifs pédagogiques", "learning_objectives", "🎯", "help"),
            AiSuggestion("ctx3", "📊 Évaluation des acquis", "assessment_methods", "📊", "help")
        ),
        
        "quiz_creation" to listOf(
            AiSuggestion("ctx4", "❓ Types de questions", "question_types", "❓", "help"),
            AiSuggestion("ctx5", "⏱️ Gestion du temps", "time_management", "⏱️", "help"),
            AiSuggestion("ctx6", "📈 Analyse des résultats", "result_analysis", "📈", "help")
        ),
        
        "study_help" to listOf(
            AiSuggestion("ctx7", "📝 Prise de notes", "note_taking", "📝", "help"),
            AiSuggestion("ctx8", "🧠 Mémorisation", "memory_techniques", "🧠", "help"),
            AiSuggestion("ctx9", "⏰ Organisation du temps", "time_organization", "⏰", "help")
        ),
        
        "motivation" to listOf(
            AiSuggestion("ctx10", "💪 Rester motivé", "stay_motivated", "💪", "motivation"),
            AiSuggestion("ctx11", "🎯 Fixer des objectifs", "set_goals", "🎯", "motivation"),
            AiSuggestion("ctx12", "🌟 Célébrer les réussites", "celebrate_success", "🌟", "motivation")
        )
    )
}

// AI Personality traits for more human-like responses
data class AiPersonality(
    val enthusiasm: Float = 0.7f, // 0.0 to 1.0
    val formality: Float = 0.5f,  // 0.0 (casual) to 1.0 (formal)
    val supportiveness: Float = 0.8f,
    val humor: Float = 0.3f,
    val patience: Float = 0.9f
) {
    companion object {
        fun forRole(role: String): AiPersonality {
            return when (role) {
                "admin" -> AiPersonality(
                    enthusiasm = 0.6f,
                    formality = 0.7f,
                    supportiveness = 0.7f,
                    humor = 0.2f,
                    patience = 0.8f
                )
                "enseignant" -> AiPersonality(
                    enthusiasm = 0.8f,
                    formality = 0.6f,
                    supportiveness = 0.9f,
                    humor = 0.4f,
                    patience = 0.9f
                )
                "etudiant" -> AiPersonality(
                    enthusiasm = 0.9f,
                    formality = 0.3f,
                    supportiveness = 0.9f,
                    humor = 0.5f,
                    patience = 1.0f
                )
                else -> AiPersonality()
            }
        }
    }
}