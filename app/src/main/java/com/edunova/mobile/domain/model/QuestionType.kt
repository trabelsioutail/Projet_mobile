package com.edunova.mobile.domain.model

enum class QuestionType(val displayName: String) {
    MULTIPLE_CHOICE("Choix multiple"),
    TRUE_FALSE("Vrai/Faux"),
    SHORT_ANSWER("Réponse courte"),
    ESSAY("Essai");
    
    companion object {
        fun fromString(value: String): QuestionType {
            return when (value.uppercase()) {
                "MULTIPLE_CHOICE" -> MULTIPLE_CHOICE
                "TRUE_FALSE" -> TRUE_FALSE
                "SHORT_ANSWER" -> SHORT_ANSWER
                "ESSAY" -> ESSAY
                else -> MULTIPLE_CHOICE
            }
        }
    }
}

enum class AssignmentType(val displayName: String) {
    QUIZ("Quiz"),
    ASSIGNMENT("Devoir"),
    EXAM("Examen");
    
    companion object {
        fun fromString(value: String): AssignmentType {
            return when (value.uppercase()) {
                "QUIZ" -> QUIZ
                "ASSIGNMENT" -> ASSIGNMENT
                "EXAM" -> EXAM
                else -> QUIZ
            }
        }
    }
}

enum class AssignmentStatus(val displayName: String) {
    DRAFT("Brouillon"),
    PUBLISHED("Publié"),
    ARCHIVED("Archivé");
    
    companion object {
        fun fromString(value: String): AssignmentStatus {
            return when (value.uppercase()) {
                "DRAFT" -> DRAFT
                "PUBLISHED" -> PUBLISHED
                "ARCHIVED" -> ARCHIVED
                else -> DRAFT
            }
        }
    }
}