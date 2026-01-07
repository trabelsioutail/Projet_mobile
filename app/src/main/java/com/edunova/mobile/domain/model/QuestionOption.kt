package com.edunova.mobile.domain.model

data class QuestionOption(
    val id: Int,
    val questionId: Int,
    val optionText: String,
    val isCorrect: Boolean
)