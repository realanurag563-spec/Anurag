package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.local.Cached3DConceptEntity
import com.example.data.local.OfflineSerializer
import com.example.data.local.OfflineStudyDao
import com.example.data.repository.AiTeacherRepository
import com.example.model.Concept3DScene
import com.example.model.SceneNode3D
import com.example.model.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class Lab3DViewModel(
    private val offlineDao: OfflineStudyDao,
    private val aiRepo: AiTeacherRepository
) : ViewModel() {

    companion object {
        private const val TAG = "Lab3DViewModel"
    }

    val availableScenes: StateFlow<List<Concept3DScene>> = offlineDao.observeAll3DConcepts()
        .map { cached ->
            val map = linkedMapOf<String, Concept3DScene>()
            CurriculumCatalog.builtIn3DConcepts.forEach { map[it.id] = it }
            cached.forEach { map[it.id] = OfflineSerializer.entityToScene3D(it) }
            map.values.toList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), CurriculumCatalog.builtIn3DConcepts)

    private val _selectedSceneId = MutableStateFlow(CurriculumCatalog.builtIn3DConcepts.first().id)
    val selectedSceneId: StateFlow<String> = _selectedSceneId.asStateFlow()

    private val _selectedNode = MutableStateFlow<SceneNode3D?>(CurriculumCatalog.builtIn3DConcepts.first().nodes.firstOrNull())
    val selectedNode: StateFlow<SceneNode3D?> = _selectedNode.asStateFlow()

    private val _generationState = MutableStateFlow<UiState<Concept3DScene>?>(null)
    val generationState: StateFlow<UiState<Concept3DScene>?> = _generationState.asStateFlow()

    init {
        viewModelScope.launch {
            val entities = CurriculumCatalog.builtIn3DConcepts.map { scene ->
                Cached3DConceptEntity(
                    id = scene.id,
                    title = scene.title,
                    subject = scene.subject,
                    levelLabel = scene.levelLabel,
                    owlSummary = scene.owlSummary,
                    payloadJson = OfflineSerializer.scene3DToJson(scene),
                    isAiGenerated = false,
                    updatedAtMillis = 0L
                )
            }
            offlineDao.upsert3DConcepts(entities)
        }
    }

    fun selectSceneById(sceneId: String) {
        val found = availableScenes.value.firstOrNull { it.id == sceneId } ?: return
        _selectedSceneId.value = found.id
        _selectedNode.value = found.nodes.firstOrNull()
    }

    fun selectNode(node: SceneNode3D) {
        _selectedNode.value = node
    }

    fun generateCustom3DConcept(
        topic: String,
        trackTitle: String,
        subject: String,
        onComplete: () -> Unit
    ) {
        val clean = topic.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            _generationState.value = UiState.Loading
            try {
                val scene = aiRepo.generate3DConcept(clean, trackTitle, subject)
                offlineDao.upsert3DConcept(
                    Cached3DConceptEntity(
                        id = scene.id,
                        title = scene.title,
                        subject = scene.subject,
                        levelLabel = scene.levelLabel,
                        owlSummary = scene.owlSummary,
                        payloadJson = OfflineSerializer.scene3DToJson(scene),
                        isAiGenerated = true,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
                _selectedSceneId.value = scene.id
                _selectedNode.value = scene.nodes.firstOrNull()
                _generationState.value = UiState.Success(scene)
                onComplete()
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "3D scene generation timed out", e)
                _generationState.value = UiState.Error("3D generation timed out after 60s. Please retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "3D scene generation failed", e)
                _generationState.value = UiState.Error(e.localizedMessage ?: "Failed to build 3D concept.")
            }
        }
    }

    fun clearGenerationStatus() {
        _generationState.value = null
    }
}
