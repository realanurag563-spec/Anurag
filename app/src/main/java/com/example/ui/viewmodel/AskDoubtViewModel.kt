package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AiTeacherRepository
import com.example.data.repository.FirestoreRepository
import com.example.model.DoubtMessageDoc
import com.example.model.DoubtSessionDoc
import com.example.model.ExpertTutorProfile
import com.example.model.OwlChatMessage
import com.example.model.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AskDoubtViewModel(
    private val currentUserId: String,
    private val studentName: String,
    private val aiRepo: AiTeacherRepository,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AskDoubtViewModel"
    }

    private val _messages = MutableStateFlow(
        listOf(
            OwlChatMessage(
                id = "owl_welcome",
                fromOwl = true,
                text = "Hi! I'm Owl. Ask me any topic and I'll research Google when needed and explain it step by step, with a 3D visual when it helps."
            )
        )
    )
    val owlMessages: StateFlow<List<OwlChatMessage>> = _messages.asStateFlow()

    private val _owlError = MutableStateFlow<String?>(null)
    val owlError: StateFlow<String?> = _owlError.asStateFlow()

    private val _isAskingOwl = MutableStateFlow(false)
    val isAskingOwl: StateFlow<Boolean> = _isAskingOwl.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val _tutorLoading = MutableStateFlow(false)
    val tutorLoading: StateFlow<Boolean> = _tutorLoading.asStateFlow()

    private val _tutorError = MutableStateFlow<String?>(null)
    val tutorError: StateFlow<String?> = _tutorError.asStateFlow()

    val doubtSessionsState: StateFlow<UiState<List<DoubtSessionDoc>>> =
        firestoreRepo.observeDoubtSessions(currentUserId)
            .map<List<DoubtSessionDoc>, UiState<List<DoubtSessionDoc>>> { UiState.Success(it) }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(UiState.Error((fsErr ?: error).message ?: "Failed to load doubt sessions"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessagesState: StateFlow<UiState<List<DoubtMessageDoc>>> =
        _activeSessionId.flatMapLatest { sessionId ->
            if (sessionId.isNullOrBlank()) {
                flowOf(UiState.Success(emptyList()))
            } else {
                firestoreRepo.observeDoubtMessages(currentUserId, sessionId)
                    .map<List<DoubtMessageDoc>, UiState<List<DoubtMessageDoc>>> { UiState.Success(it) }
                    .catch { error ->
                        val fsErr = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (fsErr == null && error is CancellationException) throw error
                        emit(UiState.Error((fsErr ?: error).message ?: "Failed to load chat messages"))
                    }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Success(emptyList()))

    fun askOwl(
        question: String,
        trackTitle: String,
        subject: String,
        isOnline: Boolean,
        onAsked: () -> Unit
    ) {
        val clean = question.trim()
        if (clean.isEmpty() || _isAskingOwl.value) return

        val userMsg = OwlChatMessage(
            id = "user_${System.currentTimeMillis()}",
            fromOwl = false,
            text = clean
        )
        val replyId = "owl_${System.currentTimeMillis() + 1}"
        _messages.update { it + userMsg }

        if (!isOnline) {
            val offlineMsg = OwlChatMessage(
                id = replyId,
                fromOwl = true,
                text = "You're currently **offline**, so live Google research is paused. You can still study all downloaded lessons, practice tests, and interactive **3D models** in the Learn and 3D Lab tabs!"
            )
            _messages.update { it + offlineMsg }
            return
        }

        _isAskingOwl.value = true
        _owlError.value = null
        _messages.update {
            it + OwlChatMessage(
                id = replyId,
                fromOwl = true,
                text = "Researching on Google...",
                isStreaming = true
            )
        }

        viewModelScope.launch {
            try {
                aiRepo.askOwlWithGoogleSearch(clean, trackTitle, subject).collect { chunk ->
                    _messages.update { list ->
                        list.map { msg ->
                            if (msg.id == replyId) {
                                msg.copy(
                                    text = chunk.accumulatedText.ifBlank { "Thinking..." },
                                    searchQueries = chunk.searchQueries,
                                    sources = chunk.sources,
                                    suggested3dTopic = clean.take(60),
                                    isStreaming = !chunk.done
                                )
                            } else {
                                msg
                            }
                        }
                    }
                }
                onAsked()
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Owl search timed out", e)
                _owlError.value = "Google research timed out. Please try again."
                _messages.update { list -> list.filterNot { it.id == replyId && it.isStreaming } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Owl research failed", e)
                _owlError.value = e.localizedMessage ?: "Failed to get Owl's answer."
                _messages.update { list -> list.filterNot { it.id == replyId && it.isStreaming } }
            } finally {
                _isAskingOwl.value = false
            }
        }
    }

    fun startTutorDoubtSession(
        tutor: ExpertTutorProfile,
        subject: String,
        topic: String,
        question: String
    ) {
        viewModelScope.launch {
            _tutorLoading.value = true
            _tutorError.value = null
            val res = firestoreRepo.createDoubtSession(
                userId = currentUserId,
                tutorId = tutor.id,
                tutorName = tutor.name,
                tutorSpecialty = tutor.specialty,
                subject = subject,
                topic = topic,
                initialQuestion = question,
                studentName = studentName
            )
            res.onSuccess { sessionId ->
                _activeSessionId.value = sessionId
                // Automatically trigger immediate AI Mentor Assist in the room while live tutor reviews
                requestMentorAssist(
                    sessionId = sessionId,
                    tutorName = tutor.name,
                    specialty = tutor.specialty,
                    subject = subject,
                    topic = topic,
                    question = question
                )
            }.onFailure { err ->
                _tutorError.value = err.localizedMessage ?: "Could not open doubt session."
            }
            _tutorLoading.value = false
        }
    }

    fun openSession(sessionId: String) {
        _activeSessionId.value = sessionId
    }

    fun closeSessionView() {
        _activeSessionId.value = null
    }

    fun sendStudentDoubtMessage(session: DoubtSessionDoc, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            _tutorError.value = null
            val res = firestoreRepo.postDoubtMessage(
                userId = currentUserId,
                sessionId = session.id,
                senderRole = "STUDENT",
                senderName = studentName,
                text = clean
            )
            if (res.isFailure) {
                _tutorError.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to send message."
            }
        }
    }

    fun requestMentorAssist(
        sessionId: String,
        tutorName: String,
        specialty: String,
        subject: String,
        topic: String,
        question: String
    ) {
        viewModelScope.launch {
            _tutorLoading.value = true
            try {
                val reply = aiRepo.generateTutorBridgeReply(
                    tutorName = tutorName,
                    specialty = specialty,
                    subject = subject,
                    topic = topic,
                    studentQuestion = question
                )
                firestoreRepo.postDoubtMessage(
                    userId = currentUserId,
                    sessionId = sessionId,
                    senderRole = "AI_BRIDGE",
                    senderName = "$tutorName (Instant Assist)",
                    text = reply
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Instant mentor assist failed", e)
                _tutorError.value = e.localizedMessage ?: "Mentor assist unavailable right now."
            } finally {
                _tutorLoading.value = false
            }
        }
    }

    fun queueForLiveHumanTutor(sessionId: String) {
        viewModelScope.launch {
            firestoreRepo.postDoubtMessage(
                userId = currentUserId,
                sessionId = sessionId,
                senderRole = "TUTOR",
                senderName = "Tutor Desk",
                text = "Your doubt is queued in the priority 1-on-1 live tutor desk. You'll receive real-time updates right in this thread.",
                statusOverride = "QUEUED_HUMAN"
            )
        }
    }

    fun resolveSession(sessionId: String) {
        viewModelScope.launch {
            firestoreRepo.updateDoubtSessionStatus(currentUserId, sessionId, "RESOLVED")
        }
    }

    fun clearErrors() {
        _owlError.value = null
        _tutorError.value = null
    }
}
