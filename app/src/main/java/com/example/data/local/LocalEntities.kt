package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_lessons")
data class CachedLessonEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val subject: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val xpReward: Int,
    val orderIndex: Int,
    val concept3dId: String?,
    val stepsJson: String,
    val sourceLabel: String,
    val updatedAtMillis: Long
)

@Entity(tableName = "cached_tests")
data class CachedPracticeTestEntity(
    @PrimaryKey val id: String,
    val trackId: String,
    val subject: String,
    val title: String,
    val difficulty: String,
    val durationMinutes: Int,
    val questionsJson: String,
    val isAiGenerated: Boolean,
    val updatedAtMillis: Long
)

@Entity(tableName = "cached_3d_concepts")
data class Cached3DConceptEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val levelLabel: String,
    val owlSummary: String,
    val payloadJson: String,
    val isAiGenerated: Boolean,
    val updatedAtMillis: Long
)

@Entity(tableName = "cached_progress")
data class CachedProgressEntity(
    @PrimaryKey val lessonId: String,
    val userId: String,
    val trackId: String,
    val subject: String,
    val title: String,
    val xpEarned: Long,
    val stars: Long,
    val bestAccuracyPercent: Long,
    val completed: Boolean,
    val pendingSync: Boolean,
    val updatedAtMillis: Long
)

@Entity(tableName = "cached_schedule")
data class CachedScheduleEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val trackId: String,
    val subject: String,
    val topicTitle: String,
    val dayLabel: String,
    val durationMinutes: Long,
    val activityType: String,
    val completed: Boolean,
    val sortOrder: Long,
    val pendingSync: Boolean,
    val updatedAtMillis: Long
)
