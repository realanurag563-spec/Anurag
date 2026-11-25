package com.example.data.repository

import android.util.Log
import com.example.model.Concept3DScene
import com.example.model.LessonStep
import com.example.model.PracticeTestPack
import com.example.model.PrimitiveShape3D
import com.example.model.QuestLesson
import com.example.model.QuizQuestion
import com.example.model.SceneNode3D
import com.example.model.SearchCitation
import com.example.model.StudyScheduleItemDoc
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class OwlStreamChunk(
    val accumulatedText: String,
    val searchQueries: List<String> = emptyList(),
    val sources: List<SearchCitation> = emptyList(),
    val done: Boolean = false
)

class AiTeacherRepository {

    companion object {
        private const val TAG = "AiTeacherRepository"
        private const val MODEL_DEFAULT = "gemini-3.8-flash"
        private const val TIMEOUT_MS = 60_000L
    }

    private val owlResearchModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_DEFAULT,
            tools = listOf(Tool.googleSearch()),
            systemInstruction = content {
                text(
                    "You are Owl, a warm, encouraging AI teacher inside GuruQuest for students in Classes 1–12 and Government Job Exams (UPSC, SSC, Banking, Railways, CTET, NDA). " +
                        "Always research up-to-date facts using Google Search when helpful. " +
                        "Explain concepts clearly step-by-step using short paragraphs, bullet points, concrete examples, and **bold** key terms. " +
                        "Keep explanations crisp, accurate, and encouraging."
                )
            }
        )
    }

    private val lessonModel by lazy {
        val questionSchema = Schema.obj(
            properties = mapOf(
                "prompt" to Schema.string("Clear question text"),
                "options" to Schema.array(Schema.string(), "4 answer options"),
                "correctIndex" to Schema.integer("Zero-based index (0 to 3) of correct option"),
                "explanation" to Schema.string("Friendly explanation from Owl"),
                "hint" to Schema.string("Short helpful hint")
            )
        )
        val stepSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("Short step title"),
                "owlPrompt" to Schema.string("Bite-sized teaching explanation by Owl"),
                "keyFormulaOrFact" to Schema.string("One memorable rule, formula, or key fact"),
                "question" to questionSchema
            )
        )
        val lessonSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("Short engaging lesson title"),
                "description" to Schema.string("One sentence lesson summary"),
                "durationMinutes" to Schema.integer("Estimated minutes (5 to 12)"),
                "concept3dTopic" to Schema.string("Suggested 3D model id: atom_bohr, dna_helix, solar_system, geometry_solids, earth_layers, plant_cell, or em_wave"),
                "steps" to Schema.array(stepSchema, "3 sequential playable learning steps")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_DEFAULT,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = lessonSchema
            }
        )
    }

    private val testModel by lazy {
        val questionSchema = Schema.obj(
            properties = mapOf(
                "prompt" to Schema.string("Exam-style question"),
                "options" to Schema.array(Schema.string(), "Exactly 4 answer choices"),
                "correctIndex" to Schema.integer("Index 0..3"),
                "explanation" to Schema.string("Concise step-by-step solution"),
                "hint" to Schema.string("Quick clue")
            )
        )
        val testSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("Practice test title"),
                "questions" to Schema.array(questionSchema, "Requested number of MCQ questions")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_DEFAULT,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = testSchema
            }
        )
    }

    private val scene3DModel by lazy {
        val nodeSchema = Schema.obj(
            properties = mapOf(
                "label" to Schema.string("Short part name"),
                "detail" to Schema.string("1-2 sentence explanation of this 3D part"),
                "shape" to Schema.enumeration(listOf("SPHERE", "RING", "CUBE", "CONE", "ROD"), "3D primitive"),
                "x" to Schema.double("X coordinate between -1.8 and 1.8"),
                "y" to Schema.double("Y coordinate between -1.8 and 1.8"),
                "z" to Schema.double("Z coordinate between -1.8 and 1.8"),
                "radiusOrSize" to Schema.double("Size between 0.1 and 1.6"),
                "colorHex" to Schema.string("Hex color like #28824B, #EA7E1E, #2B8AE2, #E04F5F"),
                "endX" to Schema.double("End X for ROD, else 0.0"),
                "endY" to Schema.double("End Y for ROD, else 0.0"),
                "endZ" to Schema.double("End Z for ROD, else 0.0"),
                "orbitSpeed" to Schema.double("0.0 if static, or 0.6 to 2.0 for orbiting sphere"),
                "orbitRadius" to Schema.double("0.0 if static, else 0.6 to 1.8")
            )
        )
        val sceneSchema = Schema.obj(
            properties = mapOf(
                "title" to Schema.string("3D concept title"),
                "subject" to Schema.string("Subject name"),
                "owlSummary" to Schema.string("2-3 sentence 3D intuition explanation by Owl"),
                "keyTakeaways" to Schema.array(Schema.string(), "3 short takeaways"),
                "nodes" to Schema.array(nodeSchema, "5 to 10 3D nodes forming a recognizable spatial diagram")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_DEFAULT,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = sceneSchema
            }
        )
    }

    private val scheduleModel by lazy {
        val taskSchema = Schema.obj(
            properties = mapOf(
                "subject" to Schema.string("Subject name"),
                "topicTitle" to Schema.string("Specific actionable topic"),
                "dayLabel" to Schema.string("e.g. Today, Tomorrow, Day 3, Day 4, Day 5"),
                "durationMinutes" to Schema.integer("Minutes between 10 and 60"),
                "activityType" to Schema.enumeration(listOf("LESSON", "QUIZ", "LAB_3D", "MOCK_TEST", "REVISION"))
            )
        )
        val scheduleSchema = Schema.obj(
            properties = mapOf(
                "tasks" to Schema.array(taskSchema, "5 to 7 balanced study tasks")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_DEFAULT,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = scheduleSchema
            }
        )
    }

    private val tutorBridgeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_DEFAULT,
            tools = listOf(Tool.googleSearch())
        )
    }

    fun askOwlWithGoogleSearch(
        question: String,
        trackTitle: String,
        subject: String
    ): Flow<OwlStreamChunk> = flow {
        val prompt = """
            Student Track: $trackTitle | Subject: $subject
            Student Question: $question
            
            Explain clearly like a supportive teacher. Include an everyday analogy or exam tip where useful.
        """.trimIndent()

        val sb = StringBuilder()
        var queries: List<String> = emptyList()
        val citations = linkedMapOf<String, SearchCitation>()

        withTimeout(TIMEOUT_MS) {
            owlResearchModel.generateContentStream(prompt).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    sb.append(chunkText)
                }
                val metadata = chunk.candidates.firstOrNull()?.groundingMetadata
                if (metadata != null) {
                    if (metadata.webSearchQueries.isNotEmpty()) {
                        queries = metadata.webSearchQueries
                    }
                    metadata.groundingChunks.forEach { gChunk ->
                        val web = gChunk.web
                        val url = web?.uri.orEmpty()
                        val title = web?.title.orEmpty().ifBlank { "Google Source" }
                        if (url.isNotBlank() && !citations.containsKey(url)) {
                            citations[url] = SearchCitation(title = title, url = url)
                        }
                    }
                }
                emit(
                    OwlStreamChunk(
                        accumulatedText = sb.toString(),
                        searchQueries = queries,
                        sources = citations.values.toList(),
                        done = false
                    )
                )
            }
        }
        emit(
            OwlStreamChunk(
                accumulatedText = sb.toString(),
                searchQueries = queries,
                sources = citations.values.toList(),
                done = true
            )
        )
    }

    suspend fun generatePlayableLesson(
        trackId: String,
        trackTitle: String,
        subject: String,
        topic: String
    ): QuestLesson {
        val prompt = """
            Create a Duolingo-style 3-step interactive lesson for:
            Track: $trackTitle
            Subject: $subject
            Topic: $topic
            Each step must teach one bite-sized idea with Owl's voice, one memorable rule/formula, and a 4-option multiple choice checkpoint question.
        """.trimIndent()

        val response = withTimeout(TIMEOUT_MS) {
            lessonModel.generateContent(prompt)
        }
        val jsonStr = response.text ?: throw IllegalStateException("Empty lesson response from AI.")
        val json = JSONObject(jsonStr)
        val stepsArr = json.getJSONArray("steps")
        val steps = (0 until stepsArr.length()).map { idx ->
            val s = stepsArr.getJSONObject(idx)
            val q = s.getJSONObject("question")
            val optsArr = q.getJSONArray("options")
            val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
            LessonStep(
                title = s.optString("title", "Step ${idx + 1}"),
                owlPrompt = s.optString("owlPrompt", ""),
                keyFormulaOrFact = s.optString("keyFormulaOrFact", ""),
                checkpointQuestion = QuizQuestion(
                    id = "ai_q_${System.currentTimeMillis()}_$idx",
                    prompt = q.optString("prompt", ""),
                    options = opts,
                    correctIndex = q.optInt("correctIndex", 0).coerceIn(0, (opts.size - 1).coerceAtLeast(0)),
                    explanation = q.optString("explanation", ""),
                    hint = q.optString("hint", "Focus on the core rule.")
                )
            )
        }
        return QuestLesson(
            id = "ai_lesson_${System.currentTimeMillis()}",
            trackId = trackId,
            subject = subject,
            title = json.optString("title", topic).take(110),
            description = json.optString("description", "AI-crafted interactive quest on $topic"),
            durationMinutes = json.optInt("durationMinutes", 7).coerceIn(4, 15),
            xpReward = 65,
            orderIndex = 99,
            concept3dId = json.optString("concept3dTopic", "atom_bohr"),
            steps = steps,
            savedOffline = true,
            sourceLabel = "AI Researched • Saved Offline"
        )
    }

    suspend fun generatePracticeTest(
        trackId: String,
        trackTitle: String,
        subject: String,
        topic: String,
        difficulty: String,
        questionCount: Int
    ): PracticeTestPack {
        val count = questionCount.coerceIn(3, 12)
        val prompt = """
            Generate a $difficulty difficulty $count-question MCQ practice test for $trackTitle in $subject focusing on "$topic".
            Ensure accurate options, exactly 4 choices per question, and clear explanations.
        """.trimIndent()

        val response = withTimeout(TIMEOUT_MS) {
            testModel.generateContent(prompt)
        }
        val jsonStr = response.text ?: throw IllegalStateException("Empty practice test response.")
        val json = JSONObject(jsonStr)
        val qArr = json.getJSONArray("questions")
        val questions = (0 until qArr.length()).map { idx ->
            val q = qArr.getJSONObject(idx)
            val optsArr = q.getJSONArray("options")
            val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
            QuizQuestion(
                id = "test_q_${System.currentTimeMillis()}_$idx",
                prompt = q.optString("prompt", ""),
                options = opts,
                correctIndex = q.optInt("correctIndex", 0).coerceIn(0, (opts.size - 1).coerceAtLeast(0)),
                explanation = q.optString("explanation", ""),
                hint = q.optString("hint", "Eliminate distractors carefully.")
            )
        }
        return PracticeTestPack(
            id = "ai_test_${System.currentTimeMillis()}",
            trackId = trackId,
            subject = subject,
            title = json.optString("title", "$subject Practice Test").take(130),
            difficulty = difficulty,
            durationMinutes = (count * 1.5).toInt().coerceAtLeast(5),
            questions = questions,
            savedOffline = true,
            isAiGenerated = true
        )
    }

    suspend fun generate3DConcept(
        topic: String,
        trackTitle: String,
        subject: String
    ): Concept3DScene {
        val prompt = """
            Design a clear 3D visual educational model explaining "$topic" for $trackTitle ($subject).
            Build a spatial arrangement using 6 to 9 primitives (SPHERE, RING, CUBE, CONE, ROD) centered around (0,0,0) with coordinates in [-1.8, 1.8].
            Give each primitive a meaningful label and student-friendly explanation.
        """.trimIndent()

        val response = withTimeout(TIMEOUT_MS) {
            scene3DModel.generateContent(prompt)
        }
        val jsonStr = response.text ?: throw IllegalStateException("Empty 3D concept response.")
        val json = JSONObject(jsonStr)
        val takeawaysArr = json.optJSONArray("keyTakeaways")
        val takeaways = if (takeawaysArr != null) {
            (0 until takeawaysArr.length()).map { takeawaysArr.getString(it) }
        } else {
            emptyList()
        }
        val nodesArr = json.getJSONArray("nodes")
        val nodes = (0 until nodesArr.length()).map { idx ->
            val n = nodesArr.getJSONObject(idx)
            val shape = enumValues<PrimitiveShape3D>().firstOrNull {
                it.name.equals(n.optString("shape"), ignoreCase = true)
            } ?: PrimitiveShape3D.SPHERE
            val x = n.optDouble("x", 0.0).toFloat().coerceIn(-1.9f, 1.9f)
            val y = n.optDouble("y", 0.0).toFloat().coerceIn(-1.9f, 1.9f)
            val z = n.optDouble("z", 0.0).toFloat().coerceIn(-1.9f, 1.9f)
            SceneNode3D(
                id = "ai3d_${idx}",
                label = n.optString("label", "Part ${idx + 1}"),
                detail = n.optString("detail", ""),
                shape = shape,
                x = x,
                y = y,
                z = z,
                radiusOrSize = n.optDouble("radiusOrSize", 0.35).toFloat().coerceIn(0.08f, 1.6f),
                colorHex = parseHexColor(n.optString("colorHex", "#28824B")),
                endX = n.optDouble("endX", 0.0).toFloat().coerceIn(-2.0f, 2.0f),
                endY = n.optDouble("endY", 0.0).toFloat().coerceIn(-2.0f, 2.0f),
                endZ = n.optDouble("endZ", 0.0).toFloat().coerceIn(-2.0f, 2.0f),
                orbitSpeed = n.optDouble("orbitSpeed", 0.0).toFloat().coerceIn(0f, 2.5f),
                orbitRadius = n.optDouble("orbitRadius", 0.0).toFloat().coerceIn(0f, 1.9f),
                explodeVectorX = x * 0.35f,
                explodeVectorY = y * 0.35f,
                explodeVectorZ = z * 0.35f
            )
        }
        return Concept3DScene(
            id = "ai_3d_${System.currentTimeMillis()}",
            title = json.optString("title", topic),
            subject = json.optString("subject", subject),
            levelLabel = trackTitle,
            owlSummary = json.optString("owlSummary", ""),
            keyTakeaways = takeaways,
            nodes = nodes,
            savedOffline = true,
            isAiGenerated = true
        )
    }

    suspend fun generateStudySchedule(
        userId: String,
        trackId: String,
        trackTitle: String,
        subjects: List<String>,
        dailyMinutes: Long,
        examGoal: String
    ): List<StudyScheduleItemDoc> {
        val prompt = """
            Create a personalized 6-task study schedule for a student preparing for $trackTitle.
            Subjects: ${subjects.joinToString(", ")}.
            Daily budget: $dailyMinutes minutes.
            Goal/Target date note: ${examGoal.ifBlank { "Steady mastery & revision" }}.
            Mix LESSON, QUIZ, LAB_3D, MOCK_TEST, and REVISION tasks across Today, Tomorrow, and Day 3-5.
        """.trimIndent()

        val response = withTimeout(TIMEOUT_MS) {
            scheduleModel.generateContent(prompt)
        }
        val jsonStr = response.text ?: throw IllegalStateException("Empty schedule response.")
        val json = JSONObject(jsonStr)
        val arr = json.getJSONArray("tasks")
        return (0 until arr.length()).map { idx ->
            val t = arr.getJSONObject(idx)
            StudyScheduleItemDoc(
                id = "ai_sched_${System.currentTimeMillis()}_$idx",
                userId = userId,
                trackId = trackId,
                subject = t.optString("subject", subjects.firstOrNull() ?: "General").take(60),
                topicTitle = t.optString("topicTitle", "Focused Study Block").take(140),
                dayLabel = t.optString("dayLabel", if (idx < 2) "Today" else "Upcoming").take(40),
                durationMinutes = t.optLong("durationMinutes", 20L).coerceIn(5L, 120L),
                activityType = t.optString("activityType", "LESSON"),
                completed = false,
                sortOrder = idx.toLong()
            )
        }
    }

    suspend fun generateTutorBridgeReply(
        tutorName: String,
        specialty: String,
        subject: String,
        topic: String,
        studentQuestion: String
    ): String {
        val prompt = """
            You are $tutorName ($specialty), providing an immediate mentor response while a student waits in their live doubt room.
            Subject: $subject | Topic: $topic
            Student Doubt: $studentQuestion
            Respond concisely, clearly, and warmly with the exact solution steps and one quick tip.
        """.trimIndent()

        return try {
            val response = withTimeout(TIMEOUT_MS) {
                tutorBridgeModel.generateContent(prompt)
            }
            response.text?.take(3900)
                ?: "I received your doubt on $topic. Let's break it down step by step — could you share which step feels tricky?"
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Tutor bridge timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Tutor bridge error", e)
            throw e
        }
    }

    private fun parseHexColor(raw: String): Long {
        return try {
            val clean = raw.trim().removePrefix("#")
            val rgb = clean.toLong(16)
            if (clean.length == 6) (0xFF000000L or rgb) else rgb
        } catch (_: Exception) {
            0xFF28824BL
        }
    }
}
