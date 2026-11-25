package com.example.data.local

import com.example.model.Concept3DScene
import com.example.model.LessonStep
import com.example.model.PracticeTestPack
import com.example.model.PrimitiveShape3D
import com.example.model.QuestLesson
import com.example.model.QuizQuestion
import com.example.model.SceneNode3D
import org.json.JSONArray
import org.json.JSONObject

object OfflineSerializer {

    fun lessonToJson(lesson: QuestLesson): String {
        val stepsArr = JSONArray()
        lesson.steps.forEach { step ->
            val obj = JSONObject().apply {
                put("title", step.title)
                put("owlPrompt", step.owlPrompt)
                put("keyFormulaOrFact", step.keyFormulaOrFact)
                step.checkpointQuestion?.let { q ->
                    put("question", questionToJsonObject(q))
                }
            }
            stepsArr.put(obj)
        }
        return JSONObject().apply {
            put("steps", stepsArr)
        }.toString()
    }

    fun jsonToSteps(jsonStr: String): List<LessonStep> {
        val root = JSONObject(jsonStr)
        val arr = root.optJSONArray("steps") ?: return emptyList()
        return (0 until arr.length()).map { idx ->
            val obj = arr.getJSONObject(idx)
            val qObj = obj.optJSONObject("question")
            LessonStep(
                title = obj.optString("title", "Concept"),
                owlPrompt = obj.optString("owlPrompt", ""),
                keyFormulaOrFact = obj.optString("keyFormulaOrFact", ""),
                checkpointQuestion = qObj?.let { jsonObjectToQuestion(it) }
            )
        }
    }

    fun questionsToJson(questions: List<QuizQuestion>): String {
        val arr = JSONArray()
        questions.forEach { arr.put(questionToJsonObject(it)) }
        return arr.toString()
    }

    fun jsonToQuestions(jsonStr: String): List<QuizQuestion> {
        val arr = JSONArray(jsonStr)
        return (0 until arr.length()).map { jsonObjectToQuestion(arr.getJSONObject(it)) }
    }

    private fun questionToJsonObject(q: QuizQuestion): JSONObject {
        return JSONObject().apply {
            put("id", q.id)
            put("prompt", q.prompt)
            put("options", JSONArray(q.options))
            put("correctIndex", q.correctIndex)
            put("explanation", q.explanation)
            put("hint", q.hint)
        }
    }

    private fun jsonObjectToQuestion(obj: JSONObject): QuizQuestion {
        val optsArr = obj.optJSONArray("options") ?: JSONArray()
        val opts = (0 until optsArr.length()).map { optsArr.optString(it) }
        return QuizQuestion(
            id = obj.optString("id", "q_${obj.hashCode()}"),
            prompt = obj.optString("prompt", ""),
            options = opts,
            correctIndex = obj.optInt("correctIndex", 0).coerceIn(0, (opts.size - 1).coerceAtLeast(0)),
            explanation = obj.optString("explanation", ""),
            hint = obj.optString("hint", "Focus on the core rule.")
        )
    }

    fun scene3DToJson(scene: Concept3DScene): String {
        val nodesArr = JSONArray()
        scene.nodes.forEach { n ->
            nodesArr.put(JSONObject().apply {
                put("id", n.id)
                put("label", n.label)
                put("detail", n.detail)
                put("shape", n.shape.name)
                put("x", n.x.toDouble())
                put("y", n.y.toDouble())
                put("z", n.z.toDouble())
                put("radiusOrSize", n.radiusOrSize.toDouble())
                put("colorHex", n.colorHex)
                put("endX", n.endX.toDouble())
                put("endY", n.endY.toDouble())
                put("endZ", n.endZ.toDouble())
                put("orbitSpeed", n.orbitSpeed.toDouble())
                put("orbitRadius", n.orbitRadius.toDouble())
                put("orbitTiltDeg", n.orbitTiltDeg.toDouble())
                put("explodeVectorX", n.explodeVectorX.toDouble())
                put("explodeVectorY", n.explodeVectorY.toDouble())
                put("explodeVectorZ", n.explodeVectorZ.toDouble())
            })
        }
        return JSONObject().apply {
            put("keyTakeaways", JSONArray(scene.keyTakeaways))
            put("nodes", nodesArr)
        }.toString()
    }

    fun entityToScene3D(entity: Cached3DConceptEntity): Concept3DScene {
        val root = JSONObject(entity.payloadJson)
        val takeawaysArr = root.optJSONArray("keyTakeaways") ?: JSONArray()
        val takeaways = (0 until takeawaysArr.length()).map { takeawaysArr.optString(it) }
        val nodesArr = root.optJSONArray("nodes") ?: JSONArray()
        val nodes = (0 until nodesArr.length()).map { idx ->
            val n = nodesArr.getJSONObject(idx)
            val shape = enumValues<PrimitiveShape3D>().firstOrNull {
                it.name.equals(n.optString("shape"), ignoreCase = true)
            } ?: PrimitiveShape3D.SPHERE
            SceneNode3D(
                id = n.optString("id", "node_$idx"),
                label = n.optString("label", "Part ${idx + 1}"),
                detail = n.optString("detail", ""),
                shape = shape,
                x = n.optDouble("x", 0.0).toFloat(),
                y = n.optDouble("y", 0.0).toFloat(),
                z = n.optDouble("z", 0.0).toFloat(),
                radiusOrSize = n.optDouble("radiusOrSize", 0.25).toFloat(),
                colorHex = n.optLong("colorHex", 0xFF28824BL),
                endX = n.optDouble("endX", 0.0).toFloat(),
                endY = n.optDouble("endY", 0.0).toFloat(),
                endZ = n.optDouble("endZ", 0.0).toFloat(),
                orbitSpeed = n.optDouble("orbitSpeed", 0.0).toFloat(),
                orbitRadius = n.optDouble("orbitRadius", 0.0).toFloat(),
                orbitTiltDeg = n.optDouble("orbitTiltDeg", 0.0).toFloat(),
                explodeVectorX = n.optDouble("explodeVectorX", 0.0).toFloat(),
                explodeVectorY = n.optDouble("explodeVectorY", 0.0).toFloat(),
                explodeVectorZ = n.optDouble("explodeVectorZ", 0.0).toFloat()
            )
        }
        return Concept3DScene(
            id = entity.id,
            title = entity.title,
            subject = entity.subject,
            levelLabel = entity.levelLabel,
            owlSummary = entity.owlSummary,
            keyTakeaways = takeaways,
            nodes = nodes,
            savedOffline = true,
            isAiGenerated = entity.isAiGenerated
        )
    }

    fun entityToLesson(entity: CachedLessonEntity): QuestLesson {
        return QuestLesson(
            id = entity.id,
            trackId = entity.trackId,
            subject = entity.subject,
            title = entity.title,
            description = entity.description,
            durationMinutes = entity.durationMinutes,
            xpReward = entity.xpReward,
            orderIndex = entity.orderIndex,
            concept3dId = entity.concept3dId,
            steps = jsonToSteps(entity.stepsJson),
            savedOffline = true,
            sourceLabel = entity.sourceLabel
        )
    }

    fun lessonToEntity(lesson: QuestLesson): CachedLessonEntity {
        return CachedLessonEntity(
            id = lesson.id,
            trackId = lesson.trackId,
            subject = lesson.subject,
            title = lesson.title,
            description = lesson.description,
            durationMinutes = lesson.durationMinutes,
            xpReward = lesson.xpReward,
            orderIndex = lesson.orderIndex,
            concept3dId = lesson.concept3dId,
            stepsJson = lessonToJson(lesson),
            sourceLabel = lesson.sourceLabel,
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    fun testToEntity(test: PracticeTestPack): CachedPracticeTestEntity {
        return CachedPracticeTestEntity(
            id = test.id,
            trackId = test.trackId,
            subject = test.subject,
            title = test.title,
            difficulty = test.difficulty,
            durationMinutes = test.durationMinutes,
            questionsJson = questionsToJson(test.questions),
            isAiGenerated = test.isAiGenerated,
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    fun entityToTest(entity: CachedPracticeTestEntity): PracticeTestPack {
        return PracticeTestPack(
            id = entity.id,
            trackId = entity.trackId,
            subject = entity.subject,
            title = entity.title,
            difficulty = entity.difficulty,
            durationMinutes = entity.durationMinutes,
            questions = jsonToQuestions(entity.questionsJson),
            savedOffline = true,
            isAiGenerated = entity.isAiGenerated
        )
    }
}
