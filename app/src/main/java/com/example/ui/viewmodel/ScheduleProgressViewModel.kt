package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.local.CachedScheduleEntity
import com.example.data.local.OfflineStudyDao
import com.example.data.repository.AiTeacherRepository
import com.example.data.repository.FirestoreRepository
import com.example.model.PracticeTestAttemptDoc
import com.example.model.StudyScheduleItemDoc
import com.example.model.UiState
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

class ScheduleProgressViewModel(
    private val currentUserId: String,
    private val firestoreRepo: FirestoreRepository,
    private val offlineDao: OfflineStudyDao,
    private val aiRepo: AiTeacherRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ScheduleProgressVM"
    }

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _planError = MutableStateFlow<String?>(null)
    val planError: StateFlow<String?> = _planError.asStateFlow()

    private val remoteScheduleFlow = firestoreRepo.observeStudySchedule(currentUserId)
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            emit(emptyList())
        }

    private val localScheduleFlow = offlineDao.observeScheduleForUser(currentUserId)

    val scheduleState: StateFlow<UiState<List<StudyScheduleItemDoc>>> = combine(
        remoteScheduleFlow,
        localScheduleFlow
    ) { remote, local ->
        val merged = if (remote.isNotEmpty()) {
            remote
        } else {
            local.map {
                StudyScheduleItemDoc(
                    id = it.id,
                    userId = it.userId,
                    trackId = it.trackId,
                    subject = it.subject,
                    topicTitle = it.topicTitle,
                    dayLabel = it.dayLabel,
                    durationMinutes = it.durationMinutes,
                    activityType = it.activityType,
                    completed = it.completed,
                    sortOrder = it.sortOrder
                )
            }
        }
        UiState.Success(merged)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val testAttemptsState: StateFlow<UiState<List<PracticeTestAttemptDoc>>> =
        firestoreRepo.observeTestAttempts(currentUserId)
            .map<List<PracticeTestAttemptDoc>, UiState<List<PracticeTestAttemptDoc>>> { UiState.Success(it) }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(UiState.Error((fsErr ?: error).message ?: "Failed to load test attempts"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    fun generateAiStudySchedule(
        trackId: String,
        dailyMinutes: Long,
        examGoal: String,
        isOnline: Boolean,
        onPlanSaved: () -> Unit
    ) {
        val track = CurriculumCatalog.tracks.firstOrNull { it.id == trackId } ?: CurriculumCatalog.tracks.first()
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            _planError.value = null
            try {
                if (isOnline) {
                    firestoreRepo.updateStudyPreferences(currentUserId, dailyMinutes, examGoal)
                }
                val items = if (isOnline) {
                    aiRepo.generateStudySchedule(
                        userId = currentUserId,
                        trackId = track.id,
                        trackTitle = track.title,
                        subjects = track.subjects,
                        dailyMinutes = dailyMinutes,
                        examGoal = examGoal
                    )
                } else {
                    buildOfflineScheduleFallback(track.id, track.subjects, dailyMinutes)
                }

                // Clear old remote schedule items cleanly if online, then save new ones
                val currentItems = (scheduleState.value as? UiState.Success)?.data.orEmpty()
                if (isOnline) {
                    currentItems.forEach { old ->
                        firestoreRepo.deleteScheduleItem(currentUserId, old.id)
                    }
                }

                offlineDao.upsertScheduleItems(
                    items.map {
                        CachedScheduleEntity(
                            id = it.id,
                            userId = currentUserId,
                            trackId = it.trackId,
                            subject = it.subject,
                            topicTitle = it.topicTitle,
                            dayLabel = it.dayLabel,
                            durationMinutes = it.durationMinutes,
                            activityType = it.activityType,
                            completed = it.completed,
                            sortOrder = it.sortOrder,
                            pendingSync = !isOnline,
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    }
                )

                if (isOnline) {
                    items.forEach { item ->
                        firestoreRepo.saveStudyScheduleItem(
                            userId = currentUserId,
                            itemId = item.id,
                            trackId = item.trackId,
                            subject = item.subject,
                            topicTitle = item.topicTitle,
                            dayLabel = item.dayLabel,
                            durationMinutes = item.durationMinutes,
                            activityType = item.activityType,
                            completed = item.completed,
                            sortOrder = item.sortOrder
                        )
                    }
                }
                onPlanSaved()
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Schedule AI timed out", e)
                _planError.value = "Schedule generation timed out. Please retry."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Schedule generation failed", e)
                _planError.value = e.localizedMessage ?: "Failed to generate schedule."
            } finally {
                _isGeneratingPlan.value = false
            }
        }
    }

    fun addCustomScheduleTask(
        trackId: String,
        subject: String,
        topicTitle: String,
        dayLabel: String,
        durationMinutes: Long,
        activityType: String,
        isOnline: Boolean,
        onAdded: () -> Unit
    ) {
        val cleanTopic = topicTitle.trim()
        if (cleanTopic.isEmpty()) return
        viewModelScope.launch {
            val id = "sched_${System.currentTimeMillis()}"
            val sortOrder = ((scheduleState.value as? UiState.Success)?.data?.size ?: 0).toLong()
            offlineDao.upsertScheduleItem(
                CachedScheduleEntity(
                    id = id,
                    userId = currentUserId,
                    trackId = trackId,
                    subject = subject,
                    topicTitle = cleanTopic,
                    dayLabel = dayLabel,
                    durationMinutes = durationMinutes,
                    activityType = activityType,
                    completed = false,
                    sortOrder = sortOrder,
                    pendingSync = !isOnline,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            if (isOnline) {
                firestoreRepo.saveStudyScheduleItem(
                    userId = currentUserId,
                    itemId = id,
                    trackId = trackId,
                    subject = subject,
                    topicTitle = cleanTopic,
                    dayLabel = dayLabel,
                    durationMinutes = durationMinutes,
                    activityType = activityType,
                    completed = false,
                    sortOrder = sortOrder
                )
            }
            onAdded()
        }
    }

    fun toggleTaskCompleted(item: StudyScheduleItemDoc, isOnline: Boolean) {
        viewModelScope.launch {
            val next = !item.completed
            offlineDao.upsertScheduleItem(
                CachedScheduleEntity(
                    id = item.id,
                    userId = currentUserId,
                    trackId = item.trackId,
                    subject = item.subject,
                    topicTitle = item.topicTitle,
                    dayLabel = item.dayLabel,
                    durationMinutes = item.durationMinutes,
                    activityType = item.activityType,
                    completed = next,
                    sortOrder = item.sortOrder,
                    pendingSync = !isOnline,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            if (isOnline) {
                firestoreRepo.toggleScheduleCompleted(currentUserId, item.id, next)
            }
        }
    }

    private fun buildOfflineScheduleFallback(
        trackId: String,
        subjects: List<String>,
        dailyMinutes: Long
    ): List<StudyScheduleItemDoc> {
        val types = listOf("LESSON", "LAB_3D", "QUIZ", "MOCK_TEST", "REVISION")
        val days = listOf("Today", "Today", "Tomorrow", "Day 3", "Day 4")
        return (0 until 5).map { idx ->
            val subj = subjects[idx % subjects.size]
            StudyScheduleItemDoc(
                id = "offline_sched_${System.currentTimeMillis()}_$idx",
                userId = currentUserId,
                trackId = trackId,
                subject = subj,
                topicTitle = "$subj ${types[idx].lowercase().replace('_', ' ')} block",
                dayLabel = days[idx],
                durationMinutes = (dailyMinutes / 2).coerceIn(10L, 45L),
                activityType = types[idx],
                completed = false,
                sortOrder = idx.toLong()
            )
        }
    }

    fun clearError() {
        _planError.value = null
    }
}
