package com.edunova.mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean = false
) : Parcelable