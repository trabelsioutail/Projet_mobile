package com.edunova.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.edunova.mobile.domain.model.*

data class QuizDto(
    val id: Int,
    @SerializedName("course_id")
    val courseId: Int,
    @SerializedName("teacher_id")
    val teacherId: Int,
    val title: String,
    val description: String?,
    val type: String = "quiz",
    val status: String = "draft",
    @SerializedName("due_date")
    val dueDate: String? = null,
    @SerializedName("time_limit")
    val timeLimit: Int? = null,
    @SerializedName("max_attempts")
    val maxAttempts: Int = 1,
    @SerializedName("passing_score")
    val passingScore: Double = 0.0,
    @SerializedName("total_points")
    val totalPoints: Int = 0,
    @SerializedName("course_title")
    val courseTitle: String? = null,
    val questions: List<StudentQuizQuestionDto> = emptyList(),
    val submissions: List<StudentQuizSubmissionDto> = emptyList(),
    @SerializedName("user_submission")
    val userSubmission: StudentQuizSubmissionDto? = null,
    @SerializedName("is_completed")
    val isCompleted: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String? = null
) {
    fun toDomainModel(): Quiz {
        return Quiz(
            id = id,
            courseId = courseId,
            teacherId = teacherId,
            title = title,
            description = description,
            type = AssignmentType.fromString(type),
            status = AssignmentStatus.fromString(status),
            dueDate = dueDate,
            timeLimit = timeLimit,
            maxAttempts = maxAttempts,
            passingScore = passingScore,
            totalPoints = totalPoints,
            courseTitle = courseTitle,
            questions = questions.map { it.toDomainModel() },
            submissions = submissions.map { it.toDomainModel() },
            userSubmission = userSubmission?.toDomainModel(),
            isCompleted = isCompleted,
            createdAt = createdAt
        )
    }
}

data class StudentQuizQuestionDto(
    val id: Int,
    @SerializedName("assignment_id")
    val assignmentId: Int,
    @SerializedName("question_text")
    val questionText: String,
    @SerializedName("question_type")
    val questionType: String,
    val options: List<String> = emptyList(),
    @SerializedName("correct_answer")
    val correctAnswer: String? = null,
    val points: Int = 1,
    @SerializedName("order_index")
    val orderIndex: Int = 0,
    @SerializedName("answer_text")
    val answerText: String? = null,
    @SerializedName("is_correct")
    val isCorrect: Boolean? = null,
    @SerializedName("points_earned")
    val pointsEarned: Double? = null
) {
    fun toDomainModel(): QuizQuestion {
        return QuizQuestion(
            id = id,
            assignmentId = assignmentId,
            questionText = questionText,
            questionType = QuestionType.fromString(questionType),
            options = options.mapIndexed { index, text -> 
                QuizOption(
                    id = ('a' + index).toString(),
                    text = text,
                    isCorrect = correctAnswer == ('a' + index).toString()
                )
            },
            correctAnswer = correctAnswer,
            points = points,
            orderIndex = orderIndex
        )
    }
}

data class StudentQuizSubmissionDto(
    val id: Int,
    @SerializedName("assignment_id")
    val assignmentId: Int,
    @SerializedName("student_id")
    val studentId: Int,
    val status: String = "pending",
    val score: Double = 0.0,
    @SerializedName("submitted_at")
    val submittedAt: String? = null,
    @SerializedName("graded_at")
    val gradedAt: String? = null,
    val feedback: String? = null,
    @SerializedName("attempt_number")
    val attemptNumber: Int = 1,
    @SerializedName("time_spent")
    val timeSpent: Int = 0,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("correct_answers")
    val correctAnswers: Int = 0,
    @SerializedName("total_questions")
    val totalQuestions: Int = 0,
    val answers: List<QuizAnswerDto> = emptyList()
) {
    fun toDomainModel(): QuizSubmission {
        return QuizSubmission(
            id = id,
            quizId = assignmentId,
            studentId = studentId,
            answers = emptyMap(), // Will be filled from answers if needed
            score = score.toInt(),
            earnedPoints = correctAnswers * 10,
            totalPoints = totalQuestions * 10,
            passed = score >= 70.0,
            submittedAt = System.currentTimeMillis(),
            attemptNumber = attemptNumber
        )
    }
}

data class QuizAnswerDto(
    val id: Int,
    @SerializedName("submission_id")
    val submissionId: Int,
    @SerializedName("question_id")
    val questionId: Int,
    @SerializedName("answer_text")
    val answerText: String?,
    @SerializedName("is_correct")
    val isCorrect: Boolean = false,
    @SerializedName("points_earned")
    val pointsEarned: Double = 0.0
) {
    fun toDomainModel(): QuizAnswer {
        return QuizAnswer(
            id = id,
            submissionId = submissionId,
            questionId = questionId,
            answerText = answerText,
            isCorrect = isCorrect,
            pointsEarned = pointsEarned
        )
    }
}