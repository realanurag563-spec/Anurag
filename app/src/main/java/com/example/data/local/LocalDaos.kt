package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineStudyDao {
    @Query("SELECT * FROM cached_lessons ORDER BY orderIndex ASC, updatedAtMillis DESC")
    fun observeAllLessons(): Flow<List<CachedLessonEntity>>

    @Query("SELECT * FROM cached_lessons WHERE trackId = :trackId AND subject = :subject ORDER BY orderIndex ASC, updatedAtMillis DESC")
    fun observeLessonsForTrackSubject(trackId: String, subject: String): Flow<List<CachedLessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessons(lessons: List<CachedLessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: CachedLessonEntity)

    @Query("DELETE FROM cached_lessons WHERE id = :id")
    suspend fun deleteLesson(id: String)

    @Query("SELECT * FROM cached_tests ORDER BY updatedAtMillis DESC")
    fun observeAllTests(): Flow<List<CachedPracticeTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTest(test: CachedPracticeTestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTests(tests: List<CachedPracticeTestEntity>)

    @Query("SELECT * FROM cached_3d_concepts ORDER BY updatedAtMillis DESC")
    fun observeAll3DConcepts(): Flow<List<Cached3DConceptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert3DConcept(concept: Cached3DConceptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert3DConcepts(concepts: List<Cached3DConceptEntity>)

    @Query("SELECT * FROM cached_progress WHERE userId = :userId")
    fun observeProgressForUser(userId: String): Flow<List<CachedProgressEntity>>

    @Query("SELECT * FROM cached_progress WHERE userId = :userId AND pendingSync = 1")
    suspend fun getPendingProgressSync(userId: String): List<CachedProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(item: CachedProgressEntity)

    @Query("SELECT * FROM cached_schedule WHERE userId = :userId ORDER BY sortOrder ASC")
    fun observeScheduleForUser(userId: String): Flow<List<CachedScheduleEntity>>

    @Query("SELECT * FROM cached_schedule WHERE userId = :userId AND pendingSync = 1")
    suspend fun getPendingScheduleSync(userId: String): List<CachedScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScheduleItems(items: List<CachedScheduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScheduleItem(item: CachedScheduleEntity)
}
