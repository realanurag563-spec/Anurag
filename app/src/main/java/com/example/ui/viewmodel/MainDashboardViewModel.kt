package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.NetworkMonitor
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.local.CachedProgressEntity
import com.example.data.local.OfflineSerializer
import com.example.data.local.OfflineStudyDao
import com.example.data.repository.AiTeacherRepository
import com.example.data.repository.FirestoreRepository
import com.example.model.LearningTrack
import com.example.model.LessonProgressDoc
import com.example.model.MilestoneReward
import com.example.model.PracticeTestPack
import com.example.model.QuestLesson
import com.example.model.TrackCategory
import com.example.model.UiState
import com.example.model.UserProgressProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardActionFeedback(
    val message: String? = null,
    val newlyUnlockedMilestone: MilestoneReward? = null,
    val xpGained: Int = 0,
    val gemsGained: Int = 0
)

class MainDashboardViewModel(
    private val currentUserId: String,
    private val displayName: String,
    private val firestoreRepo: FirestoreRepository,
    private val offlineDao: OfflineStudyDao,
    private val aiRepo: AiTeacherRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    companion object {
        private const val TAG = "MainDashboardVM"
    }

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnlineFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), networkMonitor.isCurrentlyOnline())

    private val _selectedCategory = MutableStateFlow(TrackCategory.SCHOOL)
    val selectedCategory: StateFlow<TrackCategory> = _selectedCategory.asStateFlow()

    private val _selectedTrackId = MutableStateFlow("class_5")
    val selectedTrackId: StateFlow<String> = _selectedTrackId.asStateFlow()

    private val _selectedSubject = MutableStateFlow("Mathematics")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _aiGenerationLoading = MutableStateFlow(false)
    val aiGenerationLoading: StateFlow<Boolean> = _aiGenerationLoading.asStateFlow()

    private val _aiGenerationError = MutableStateFlow<String?>(null)
    val aiGenerationError: StateFlow<String?> = _aiGenerationError.asStateFlow()

    private val _feedback = MutableStateFlow<DashboardActionFeedback?>(null)
    val feedback: StateFlow<DashboardActionFeedback?> = _feedback.asStateFlow()

    val profileState: StateFlow<UiState<UserProgressProfile>> =
        firestoreRepo.observeUserProfile(currentUserId)
            .map<UserProgressProfile?, UiState<UserProgressProfile>> { doc ->
                val resolved = doc ?: UserProgressProfile(
                    userId = currentUserId,
                    displayName = displayName,
                    selectedTrackId = _selectedTrackId.value,
                    selectedSubject = _selectedSubject.value
                )
                UiState.Success(resolved)
            }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                Log.w(TAG, "Profile stream error", fsErr ?: error)
                emit(UiState.Error((fsErr ?: error).message ?: "Failed to load profile"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    private val remoteProgressFlow = firestoreRepo.observeLessonProgress(currentUserId)
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            emit(emptyList())
        }

    private val localProgressFlow = offlineDao.observeProgressForUser(currentUserId)

    val lessonProgressMap: StateFlow<Map<String, LessonProgressDoc>> = combine(
        remoteProgressFlow,
        localProgressFlow
    ) { remote, local ->
        val merged = linkedMapOf<String, LessonProgressDoc>()
        remote.forEach { merged[it.lessonId] = it }
        local.forEach { loc ->
            val existing = merged[loc.lessonId]
            if (existing == null || loc.xpEarned > existing.xpEarned || loc.completed && !existing.completed) {
                merged[loc.lessonId] = LessonProgressDoc(
                    id = loc.lessonId,
                    userId = loc.userId,
                    lessonId = loc.lessonId,
                    trackId = loc.trackId,
                    subject = loc.subject,
                    title = loc.title,
                    xpEarned = loc.xpEarned,
                    stars = loc.stars,
                    bestAccuracyPercent = loc.bestAccuracyPercent,
                    completed = loc.completed
                )
            }
        }
        merged
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyMap())

    val cachedLessons: StateFlow<List<QuestLesson>> = offlineDao.observeAllLessons()
        .map { list -> list.map { OfflineSerializer.entityToLesson(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cachedTests: StateFlow<List<PracticeTestPack>> = offlineDao.observeAllTests()
        .map { list -> list.map { OfflineSerializer.entityToTest(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeLessons: StateFlow<List<QuestLesson>> = combine(
        _selectedTrackId,
        _selectedSubject,
        cachedLessons
    ) { trackId, subject, cached ->
        val builtIn = CurriculumCatalog.getBuiltInLessonsFor(trackId, subject)
        val cachedForSelection = cached.filter {
            it.trackId == trackId && it.subject.equals(subject, ignoreCase = true)
        }
        val byId = linkedMapOf<String, QuestLesson>()
        builtIn.forEach { byId[it.id] = it }
        cachedForSelection.forEach { byId[it.id] = it }
        byId.values.sortedBy { it.orderIndex }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val activeTests: StateFlow<List<PracticeTestPack>> = combine(
        _selectedTrackId,
        _selectedSubject,
        cachedTests
    ) { trackId, subject, cached ->
        val builtIn = CurriculumCatalog.getBuiltInTestsFor(trackId, subject)
        val cachedForSelection = cached.filter { it.trackId == trackId }
        val byId = linkedMapOf<String, PracticeTestPack>()
        cachedForSelection.forEach { byId[it.id] = it }
        builtIn.forEach { if (!byId.containsKey(it.id)) byId[it.id] = it }
        byId.values.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    init {
        viewModelScope.launch {
            firestoreRepo.ensureUserProfile(currentUserId, displayName)
            seedDefaultOfflinePacks()
        }
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online) {
                    syncPendingProgress()
                }
            }
        }
        viewModelScope.launch {
            profileState.collect { state ->
                if (state is UiState.Success) {
                    val track = CurriculumCatalog.tracks.firstOrNull { it.id == state.data.selectedTrackId }
                    if (track != null) {
                        _selectedCategory.value = track.category
                        _selectedTrackId.value = track.id
                        _selectedSubject.value = if (state.data.selectedSubject in track.subjects) {
                            state.data.selectedSubject
                        } else {
                            track.subjects.first()
                        }
                    }
                }
            }
        }
    }

    private suspend fun seedDefaultOfflinePacks() {
        val defaultLessons = CurriculumCatalog.getBuiltInLessonsFor("class_5", "Mathematics") +
            CurriculumCatalog.getBuiltInLessonsFor("class_10", "Science") +
            CurriculumCatalog.getBuiltInLessonsFor("upsc_cse", "Polity")
        offlineDao.upsertLessons(defaultLessons.map { OfflineSerializer.lessonToEntity(it) })

        val defaultTests = CurriculumCatalog.getBuiltInTestsFor("class_5", "Mathematics") +
            CurriculumCatalog.getBuiltInTestsFor("upsc_cse", "Polity")
        offlineDao.upsertTests(defaultTests.map { OfflineSerializer.testToEntity(it) })
    }

    fun selectCategory(category: TrackCategory) {
        _selectedCategory.value = category
        val firstTrack = CurriculumCatalog.tracks.first { it.category == category }
        selectTrack(firstTrack)
    }

    fun selectTrack(track: LearningTrack) {
        _selectedCategory.value = track.category
        _selectedTrackId.value = track.id
        val firstSubject = track.subjects.first()
        _selectedSubject.value = firstSubject
        viewModelScope.launch {
            if (isOnline.value) {
                firestoreRepo.updateTrackAndSubject(currentUserId, track.id, firstSubject)
            }
            saveCurrentTrackOffline()
        }
    }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        val trackId = _selectedTrackId.value
        viewModelScope.launch {
            if (isOnline.value) {
                firestoreRepo.updateTrackAndSubject(currentUserId, trackId, subject)
            }
            saveCurrentTrackOffline()
        }
    }

    fun saveCurrentTrackOffline() {
        viewModelScope.launch {
            val trackId = _selectedTrackId.value
            val subject = _selectedSubject.value
            val lessons = CurriculumCatalog.getBuiltInLessonsFor(trackId, subject)
            val tests = CurriculumCatalog.getBuiltInTestsFor(trackId, subject)
            offlineDao.upsertLessons(lessons.map { OfflineSerializer.lessonToEntity(it) })
            offlineDao.upsertTests(tests.map { OfflineSerializer.testToEntity(it) })
            _feedback.value = DashboardActionFeedback(
                message = "Saved $subject offline! You can study anytime without internet."
            )
        }
    }

    fun completeLesson(
        lesson: QuestLesson,
        correctAnswers: Int,
        totalQuestions: Int,
        heartsRemaining: Int
    ) {
        viewModelScope.launch {
            val safeTotal = totalQuestions.coerceAtLeast(1)
            val accuracy = ((correctAnswers * 100f) / safeTotal).toInt().coerceIn(0, 100)
            val stars = when {
                accuracy >= 90 -> 3L
                accuracy >= 60 -> 2L
                else -> 1L
            }
            val xpEarned = lesson.xpReward + (if (accuracy == 100) 15 else 0)

            offlineDao.upsertProgress(
                CachedProgressEntity(
                    lessonId = lesson.id,
                    userId = currentUserId,
                    trackId = lesson.trackId,
                    subject = lesson.subject,
                    title = lesson.title,
                    xpEarned = xpEarned.toLong(),
                    stars = stars,
                    bestAccuracyPercent = accuracy.toLong(),
                    completed = true,
                    pendingSync = !isOnline.value,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )

            val currentProfile = (profileState.value as? UiState.Success)?.data ?: UserProgressProfile(userId = currentUserId)
            val newXp = currentProfile.totalXp + xpEarned
            val newCompleted = currentProfile.completedLessonsCount + 1
            val updatedMilestones = currentProfile.unlockedMilestones.toMutableList()
            var newMilestone: MilestoneReward? = null
            var gemBonus = 2

            if ("first_lesson" !in updatedMilestones) {
                updatedMilestones.add("first_lesson")
                newMilestone = CurriculumCatalog.milestones.firstOrNull { it.id == "first_lesson" }
                gemBonus += newMilestone?.gemBonus ?: 0
            }
            if (accuracy == 100 && "quiz_ace" !in updatedMilestones) {
                updatedMilestones.add("quiz_ace")
                val m = CurriculumCatalog.milestones.firstOrNull { it.id == "quiz_ace" }
                if (newMilestone == null) newMilestone = m
                gemBonus += m?.gemBonus ?: 0
            }
            if (newXp >= 100 && "xp_100" !in updatedMilestones) {
                updatedMilestones.add("xp_100")
                val m = CurriculumCatalog.milestones.firstOrNull { it.id == "xp_100" }
                if (newMilestone == null) newMilestone = m
                gemBonus += m?.gemBonus ?: 0
            }
            if (newXp >= 500 && "xp_500" !in updatedMilestones) {
                updatedMilestones.add("xp_500")
                val m = CurriculumCatalog.milestones.firstOrNull { it.id == "xp_500" }
                if (newMilestone == null) newMilestone = m
                gemBonus += m?.gemBonus ?: 0
            }

            if (isOnline.value) {
                firestoreRepo.saveLessonProgress(
                    userId = currentUserId,
                    lessonId = lesson.id,
                    trackId = lesson.trackId,
                    subject = lesson.subject,
                    title = lesson.title,
                    xpEarned = xpEarned.toLong(),
                    stars = stars,
                    accuracyPercent = accuracy.toLong()
                )
                firestoreRepo.updateProfileRewards(
                    userId = currentUserId,
                    newTotalXp = newXp,
                    newGems = currentProfile.gems + gemBonus,
                    newHearts = heartsRemaining.coerceIn(1, 5).toLong(),
                    newCompletedLessons = newCompleted,
                    newTestsCompleted = currentProfile.testsCompletedCount,
                    milestones = updatedMilestones
                )
            }

            _feedback.value = DashboardActionFeedback(
                message = "Quest complete! +$xpEarned XP and +$gemBonus gems",
                newlyUnlockedMilestone = newMilestone,
                xpGained = xpEarned,
                gemsGained = gemBonus
            )
        }
    }

    fun recordPracticeTestResult(
        test: PracticeTestPack,
        score: Int,
        durationSeconds: Int
    ) {
        viewModelScope.launch {
            val total = test.questions.size.coerceAtLeast(1)
            val xpAwarded = (score * 20) + 20
            val accuracy = (score * 100) / total
            val currentProfile = (profileState.value as? UiState.Success)?.data ?: UserProgressProfile(userId = currentUserId)
            val newXp = currentProfile.totalXp + xpAwarded
            val milestones = currentProfile.unlockedMilestones.toMutableList()
            var newMilestone: MilestoneReward? = null
            var gemBonus = 3

            if (accuracy == 100 && "quiz_ace" !in milestones) {
                milestones.add("quiz_ace")
                newMilestone = CurriculumCatalog.milestones.firstOrNull { it.id == "quiz_ace" }
                gemBonus += newMilestone?.gemBonus ?: 0
            }

            if (isOnline.value) {
                firestoreRepo.recordTestAttempt(
                    userId = currentUserId,
                    trackId = test.trackId,
                    subject = test.subject,
                    testTitle = test.title,
                    score = score.toLong(),
                    totalQuestions = total.toLong(),
                    xpAwarded = xpAwarded.toLong(),
                    durationSeconds = durationSeconds.toLong()
                )
                firestoreRepo.updateProfileRewards(
                    userId = currentUserId,
                    newTotalXp = newXp,
                    newGems = currentProfile.gems + gemBonus,
                    newHearts = currentProfile.hearts,
                    newCompletedLessons = currentProfile.completedLessonsCount,
                    newTestsCompleted = currentProfile.testsCompletedCount + 1,
                    milestones = milestones
                )
            }
            _feedback.value = DashboardActionFeedback(
                message = "Test scored $score/$total! +$xpAwarded XP earned",
                newlyUnlockedMilestone = newMilestone,
                xpGained = xpAwarded,
                gemsGained = gemBonus
            )
        }
    }

    fun generateCustomAiLesson(topic: String, onGenerated: (QuestLesson) -> Unit) {
        val cleanTopic = topic.trim()
        if (cleanTopic.isEmpty()) return
        viewModelScope.launch {
            _aiGenerationLoading.value = true
            _aiGenerationError.value = null
            try {
                val track = CurriculumCatalog.tracks.firstOrNull { it.id == _selectedTrackId.value }
                    ?: CurriculumCatalog.tracks.first()
                val lesson = aiRepo.generatePlayableLesson(
                    trackId = track.id,
                    trackTitle = track.title,
                    subject = _selectedSubject.value,
                    topic = cleanTopic
                )
                offlineDao.upsertLesson(OfflineSerializer.lessonToEntity(lesson))
                onGenerated(lesson)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI lesson timed out", e)
                _aiGenerationError.value = "Lesson generation timed out. Check connection and try again."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI lesson failed", e)
                _aiGenerationError.value = e.localizedMessage ?: "Failed to generate lesson."
            } finally {
                _aiGenerationLoading.value = false
            }
        }
    }

    fun generateCustomPracticeTest(
        topic: String,
        difficulty: String,
        questionCount: Int,
        onGenerated: (PracticeTestPack) -> Unit
    ) {
        val cleanTopic = topic.trim().ifBlank { _selectedSubject.value }
        viewModelScope.launch {
            _aiGenerationLoading.value = true
            _aiGenerationError.value = null
            try {
                val track = CurriculumCatalog.tracks.firstOrNull { it.id == _selectedTrackId.value }
                    ?: CurriculumCatalog.tracks.first()
                val test = aiRepo.generatePracticeTest(
                    trackId = track.id,
                    trackTitle = track.title,
                    subject = _selectedSubject.value,
                    topic = cleanTopic,
                    difficulty = difficulty,
                    questionCount = questionCount
                )
                offlineDao.upsertTest(OfflineSerializer.testToEntity(test))
                onGenerated(test)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI test timed out", e)
                _aiGenerationError.value = "Practice test generation timed out."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI test failed", e)
                _aiGenerationError.value = e.localizedMessage ?: "Failed to generate practice test."
            } finally {
                _aiGenerationLoading.value = false
            }
        }
    }

    fun unlockMilestoneById(milestoneId: String) {
        viewModelScope.launch {
            val currentProfile = (profileState.value as? UiState.Success)?.data ?: return@launch
            if (milestoneId in currentProfile.unlockedMilestones) return@launch
            val reward = CurriculumCatalog.milestones.firstOrNull { it.id == milestoneId } ?: return@launch
            val updated = currentProfile.unlockedMilestones + milestoneId
            if (isOnline.value) {
                firestoreRepo.updateProfileRewards(
                    userId = currentUserId,
                    newTotalXp = currentProfile.totalXp + 15L,
                    newGems = currentProfile.gems + reward.gemBonus,
                    newHearts = currentProfile.hearts,
                    newCompletedLessons = currentProfile.completedLessonsCount,
                    newTestsCompleted = currentProfile.testsCompletedCount,
                    milestones = updated
                )
            }
        }
    }

    fun clearFeedback() {
        _feedback.value = null
    }

    fun clearAiError() {
        _aiGenerationError.value = null
    }

    private suspend fun syncPendingProgress() {
        val pending = offlineDao.getPendingProgressSync(currentUserId)
        pending.forEach { item ->
            val res = firestoreRepo.saveLessonProgress(
                userId = currentUserId,
                lessonId = item.lessonId,
                trackId = item.trackId,
                subject = item.subject,
                title = item.title,
                xpEarned = item.xpEarned,
                stars = item.stars,
                accuracyPercent = item.bestAccuracyPercent
            )
            if (res.isSuccess) {
                offlineDao.upsertProgress(item.copy(pendingSync = false))
            }
        }
    }
}
