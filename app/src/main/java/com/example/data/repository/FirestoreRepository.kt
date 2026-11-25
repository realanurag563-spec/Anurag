package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.model.DoubtMessageDoc
import com.example.model.DoubtSessionDoc
import com.example.model.LessonProgressDoc
import com.example.model.PracticeTestAttemptDoc
import com.example.model.StudyScheduleItemDoc
import com.example.model.UserProgressProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProgressProfile?> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(
                        UserProgressProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(
        userId: String,
        displayName: String
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            val docRef = db.collection("users").document(userId)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val safeName = displayName.trim().ifBlank { "Explorer" }.take(80)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "selectedTrackId" to "class_5",
                    "selectedSubject" to "Mathematics",
                    "totalXp" to 0L,
                    "gems" to 10L,
                    "streakDays" to 1L,
                    "hearts" to 5L,
                    "completedLessonsCount" to 0L,
                    "testsCompletedCount" to 0L,
                    "unlockedMilestones" to emptyList<String>(),
                    "dailyGoalMinutes" to 20L,
                    "targetExamDate" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateTrackAndSubject(
        userId: String,
        trackId: String,
        subject: String
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.collection("users").document(userId).update(
                mapOf(
                    "selectedTrackId" to trackId.take(60),
                    "selectedSubject" to subject.take(60),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileRewards(
        userId: String,
        newTotalXp: Long,
        newGems: Long,
        newHearts: Long,
        newCompletedLessons: Long,
        newTestsCompleted: Long,
        milestones: List<String>
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.collection("users").document(userId).update(
                mapOf(
                    "totalXp" to newTotalXp.coerceIn(0L, 10_000_000L),
                    "gems" to newGems.coerceIn(0L, 1_000_000L),
                    "hearts" to newHearts.coerceIn(0L, 5L),
                    "completedLessonsCount" to newCompletedLessons.coerceIn(0L, 100_000L),
                    "testsCompletedCount" to newTestsCompleted.coerceIn(0L, 100_000L),
                    "unlockedMilestones" to milestones.distinct().take(30),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateStudyPreferences(
        userId: String,
        dailyGoalMinutes: Long,
        targetExamDate: String
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.collection("users").document(userId).update(
                mapOf(
                    "dailyGoalMinutes" to dailyGoalMinutes.coerceIn(5L, 360L),
                    "targetExamDate" to targetExamDate.take(40),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeLessonProgress(userId: String): Flow<List<LessonProgressDoc>> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId/lesson_progress"
        emitAll(
            db.collection("users").document(userId).collection("lesson_progress")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LessonProgressDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveLessonProgress(
        userId: String,
        lessonId: String,
        trackId: String,
        subject: String,
        title: String,
        xpEarned: Long,
        stars: Long,
        accuracyPercent: Long
    ): Result<Unit> {
        val safeDocId = sanitizeId(lessonId)
        val path = "users/$userId/lesson_progress/$safeDocId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val docRef = db.collection("users").document(userId)
                .collection("lesson_progress").document(safeDocId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val prevXp = existing.getLong("xpEarned") ?: 0L
                val prevStars = existing.getLong("stars") ?: 0L
                val prevAcc = existing.getLong("bestAccuracyPercent") ?: 0L
                docRef.update(
                    mapOf(
                        "xpEarned" to maxOf(prevXp, xpEarned).coerceIn(0L, 5000L),
                        "stars" to maxOf(prevStars, stars).coerceIn(0L, 3L),
                        "bestAccuracyPercent" to maxOf(prevAcc, accuracyPercent).coerceIn(0L, 100L),
                        "completed" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "lessonId" to safeDocId.take(80),
                        "trackId" to trackId.take(60),
                        "subject" to subject.take(60),
                        "title" to title.ifBlank { "Lesson" }.take(120),
                        "xpEarned" to xpEarned.coerceIn(0L, 5000L),
                        "stars" to stars.coerceIn(0L, 3L),
                        "bestAccuracyPercent" to accuracyPercent.coerceIn(0L, 100L),
                        "completed" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeStudySchedule(userId: String): Flow<List<StudyScheduleItemDoc>> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId/study_schedule"
        emitAll(
            db.collection("users").document(userId).collection("study_schedule")
                .orderBy("sortOrder", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        StudyScheduleItemDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveStudyScheduleItem(
        userId: String,
        itemId: String,
        trackId: String,
        subject: String,
        topicTitle: String,
        dayLabel: String,
        durationMinutes: Long,
        activityType: String,
        completed: Boolean,
        sortOrder: Long
    ): Result<Unit> {
        val safeDocId = sanitizeId(itemId)
        val path = "users/$userId/study_schedule/$safeDocId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val safeType = if (activityType in listOf("LESSON", "QUIZ", "LAB_3D", "MOCK_TEST", "REVISION")) {
                activityType
            } else {
                "LESSON"
            }
            val docRef = db.collection("users").document(userId)
                .collection("study_schedule").document(safeDocId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "trackId" to trackId.take(60),
                        "subject" to subject.take(60),
                        "topicTitle" to topicTitle.ifBlank { "Study Task" }.take(140),
                        "dayLabel" to dayLabel.ifBlank { "Today" }.take(40),
                        "durationMinutes" to durationMinutes.coerceIn(5L, 240L),
                        "activityType" to safeType,
                        "completed" to completed,
                        "sortOrder" to sortOrder.coerceIn(0L, 500L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "trackId" to trackId.take(60),
                        "subject" to subject.take(60),
                        "topicTitle" to topicTitle.ifBlank { "Study Task" }.take(140),
                        "dayLabel" to dayLabel.ifBlank { "Today" }.take(40),
                        "durationMinutes" to durationMinutes.coerceIn(5L, 240L),
                        "activityType" to safeType,
                        "completed" to completed,
                        "sortOrder" to sortOrder.coerceIn(0L, 500L),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleScheduleCompleted(
        userId: String,
        scheduleId: String,
        completed: Boolean
    ): Result<Unit> {
        val safeDocId = sanitizeId(scheduleId)
        val path = "users/$userId/study_schedule/$safeDocId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.collection("users").document(userId)
                .collection("study_schedule").document(safeDocId)
                .update(
                    mapOf(
                        "completed" to completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteScheduleItem(userId: String, scheduleId: String): Result<Unit> {
        val safeDocId = sanitizeId(scheduleId)
        val path = "users/$userId/study_schedule/$safeDocId"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            db.collection("users").document(userId)
                .collection("study_schedule").document(safeDocId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun observeTestAttempts(userId: String): Flow<List<PracticeTestAttemptDoc>> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId/test_attempts"
        emitAll(
            db.collection("users").document(userId).collection("test_attempts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PracticeTestAttemptDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun recordTestAttempt(
        userId: String,
        trackId: String,
        subject: String,
        testTitle: String,
        score: Long,
        totalQuestions: Long,
        xpAwarded: Long,
        durationSeconds: Long
    ): Result<Unit> {
        val path = "users/$userId/test_attempts"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val total = totalQuestions.coerceIn(1L, 200L)
            val safeScore = score.coerceIn(0L, total)
            val docRef = db.collection("users").document(userId)
                .collection("test_attempts").document()
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "trackId" to trackId.take(60),
                    "subject" to subject.take(60),
                    "testTitle" to testTitle.ifBlank { "Practice Test" }.take(140),
                    "score" to safeScore,
                    "totalQuestions" to total,
                    "xpAwarded" to xpAwarded.coerceIn(0L, 5000L),
                    "durationSeconds" to durationSeconds.coerceIn(0L, 36000L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeDoubtSessions(userId: String): Flow<List<DoubtSessionDoc>> = flow {
        require(userId.isNotBlank())
        val path = "users/$userId/doubt_sessions"
        emitAll(
            db.collection("users").document(userId).collection("doubt_sessions")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        DoubtSessionDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeDoubtMessages(userId: String, sessionId: String): Flow<List<DoubtMessageDoc>> = flow {
        require(userId.isNotBlank() && sessionId.isNotBlank())
        val safeSession = sanitizeId(sessionId)
        val path = "users/$userId/doubt_sessions/$safeSession/messages"
        emitAll(
            db.collection("users").document(userId)
                .collection("doubt_sessions").document(safeSession)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        DoubtMessageDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == userId) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createDoubtSession(
        userId: String,
        tutorId: String,
        tutorName: String,
        tutorSpecialty: String,
        subject: String,
        topic: String,
        initialQuestion: String,
        studentName: String
    ): Result<String> {
        val path = "users/$userId/doubt_sessions"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val sessionRef = db.collection("users").document(userId)
                .collection("doubt_sessions").document()
            val cleanQuestion = initialQuestion.trim().ifBlank { "Need help with $topic" }.take(500)
            sessionRef.set(
                mapOf(
                    "userId" to uid,
                    "tutorId" to sanitizeId(tutorId).take(60),
                    "tutorName" to tutorName.take(80),
                    "tutorSpecialty" to tutorSpecialty.take(80),
                    "subject" to subject.take(60),
                    "topic" to topic.ifBlank { subject }.take(160),
                    "status" to "ACTIVE",
                    "lastMessage" to cleanQuestion,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            sessionRef.collection("messages").document().set(
                mapOf(
                    "userId" to uid,
                    "senderRole" to "STUDENT",
                    "senderName" to studentName.ifBlank { "Student" }.take(80),
                    "text" to initialQuestion.trim().ifBlank { "Need help with $topic" }.take(4000),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(sessionRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun postDoubtMessage(
        userId: String,
        sessionId: String,
        senderRole: String,
        senderName: String,
        text: String,
        statusOverride: String? = null
    ): Result<Unit> {
        val safeSession = sanitizeId(sessionId)
        val path = "users/$userId/doubt_sessions/$safeSession/messages"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val cleanText = text.trim().take(4000)
            require(cleanText.isNotEmpty()) { "Message cannot be empty" }
            val sessionRef = db.collection("users").document(userId)
                .collection("doubt_sessions").document(safeSession)

            sessionRef.collection("messages").document().set(
                mapOf(
                    "userId" to uid,
                    "senderRole" to if (senderRole in listOf("STUDENT", "TUTOR", "AI_BRIDGE")) senderRole else "STUDENT",
                    "senderName" to senderName.ifBlank { "User" }.take(80),
                    "text" to cleanText,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val updates = mutableMapOf<String, Any>(
                "lastMessage" to cleanText.take(500),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (statusOverride != null && statusOverride in listOf("ACTIVE", "QUEUED_HUMAN", "RESOLVED")) {
                updates["status"] = statusOverride
            }
            sessionRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateDoubtSessionStatus(
        userId: String,
        sessionId: String,
        status: String
    ): Result<Unit> {
        val safeSession = sanitizeId(sessionId)
        val path = "users/$userId/doubt_sessions/$safeSession"
        return try {
            val uid = requireUserId()
            check(uid == userId)
            val safeStatus = if (status in listOf("ACTIVE", "QUEUED_HUMAN", "RESOLVED")) status else "ACTIVE"
            db.collection("users").document(userId)
                .collection("doubt_sessions").document(safeSession)
                .update(
                    mapOf(
                        "status" to safeStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    private fun sanitizeId(raw: String): String {
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
        return cleaned.ifBlank { "doc_${System.currentTimeMillis()}" }
    }
}
