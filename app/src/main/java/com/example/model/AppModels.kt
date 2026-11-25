package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

// ==========================================
// FIRESTORE PERSISTENCE MODELS (1:1 PARITY)
// ==========================================

@Keep
@IgnoreExtraProperties
data class UserProgressProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Explorer",
    val selectedTrackId: String = "class_10",
    val selectedSubject: String = "Mathematics",
    val totalXp: Long = 0L,
    val gems: Long = 10L,
    val streakDays: Long = 1L,
    val hearts: Long = 5L,
    val completedLessonsCount: Long = 0L,
    val testsCompletedCount: Long = 0L,
    val unlockedMilestones: List<String> = emptyList(),
    val dailyGoalMinutes: Long = 20L,
    val targetExamDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LessonProgressDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val lessonId: String = "",
    val trackId: String = "",
    val subject: String = "",
    val title: String = "",
    val xpEarned: Long = 0L,
    val stars: Long = 0L,
    val bestAccuracyPercent: Long = 0L,
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StudyScheduleItemDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val trackId: String = "",
    val subject: String = "",
    val topicTitle: String = "",
    val dayLabel: String = "Today",
    val durationMinutes: Long = 20L,
    val activityType: String = "LESSON",
    val completed: Boolean = false,
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PracticeTestAttemptDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val trackId: String = "",
    val subject: String = "",
    val testTitle: String = "",
    val score: Long = 0L,
    val totalQuestions: Long = 1L,
    val xpAwarded: Long = 0L,
    val durationSeconds: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DoubtSessionDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tutorId: String = "",
    val tutorName: String = "",
    val tutorSpecialty: String = "",
    val subject: String = "",
    val topic: String = "",
    val status: String = "ACTIVE",
    val lastMessage: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DoubtMessageDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val senderRole: String = "STUDENT",
    val senderName: String = "",
    val text: String = "",
    val citations: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

// ==========================================
// DOMAIN & UI MODELS
// ==========================================

enum class TrackCategory(val label: String, val subtitle: String) {
    SCHOOL("School", "Classes 1–12"),
    GOVT_EXAM("Gov exams", "Build your base")
}

data class LearningTrack(
    val id: String,
    val title: String,
    val shortTitle: String,
    val category: TrackCategory,
    val tagline: String,
    val subjects: List<String>
)

data class LessonStep(
    val title: String,
    val owlPrompt: String,
    val keyFormulaOrFact: String = "",
    val checkpointQuestion: QuizQuestion? = null
)

data class QuizQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val hint: String = "Break the concept into smaller parts and check units or definitions."
)

data class QuestLesson(
    val id: String,
    val trackId: String,
    val subject: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val xpReward: Int,
    val orderIndex: Int,
    val concept3dId: String? = null,
    val steps: List<LessonStep>,
    val savedOffline: Boolean = true,
    val sourceLabel: String = "Built-in Free Pack"
)

data class PracticeTestPack(
    val id: String,
    val trackId: String,
    val subject: String,
    val title: String,
    val difficulty: String,
    val durationMinutes: Int,
    val questions: List<QuizQuestion>,
    val savedOffline: Boolean = true,
    val isAiGenerated: Boolean = false
)

// ==========================================
// INTERACTIVE 3D ENGINE MODELS
// ==========================================

enum class PrimitiveShape3D {
    SPHERE,
    RING,
    CUBE,
    CONE,
    ROD
}

data class SceneNode3D(
    val id: String,
    val label: String,
    val detail: String,
    val shape: PrimitiveShape3D,
    val x: Float,
    val y: Float,
    val z: Float,
    val radiusOrSize: Float,
    val colorHex: Long,
    val endX: Float = 0f,
    val endY: Float = 0f,
    val endZ: Float = 0f,
    val orbitSpeed: Float = 0f,
    val orbitRadius: Float = 0f,
    val orbitTiltDeg: Float = 0f,
    val explodeVectorX: Float = 0f,
    val explodeVectorY: Float = 0f,
    val explodeVectorZ: Float = 0f
)

data class Concept3DScene(
    val id: String,
    val title: String,
    val subject: String,
    val levelLabel: String,
    val owlSummary: String,
    val keyTakeaways: List<String>,
    val nodes: List<SceneNode3D>,
    val savedOffline: Boolean = true,
    val isAiGenerated: Boolean = false
)

// ==========================================
// GAMIFICATION, TUTORS & AI SEARCH
// ==========================================

data class MilestoneReward(
    val id: String,
    val title: String,
    val description: String,
    val gemBonus: Int,
    val xpThreshold: Long = 0L
)

data class ExpertTutorProfile(
    val id: String,
    val name: String,
    val specialty: String,
    val subjects: List<String>,
    val experienceYears: Int,
    val rating: String,
    val availabilityLabel: String,
    val onlineNow: Boolean,
    val bio: String
)

data class OwlChatMessage(
    val id: String,
    val fromOwl: Boolean,
    val text: String,
    val searchQueries: List<String> = emptyList(),
    val sources: List<SearchCitation> = emptyList(),
    val suggested3dTopic: String? = null,
    val isStreaming: Boolean = false
)

data class SearchCitation(
    val title: String,
    val url: String
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
