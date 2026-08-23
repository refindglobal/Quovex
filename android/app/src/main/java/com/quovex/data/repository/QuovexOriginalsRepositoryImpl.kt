package com.quovex.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.quovex.domain.model.originals.OriginalChapter
import com.quovex.domain.model.originals.OriginalCommonMistake
import com.quovex.domain.model.originals.OriginalFlashcard
import com.quovex.domain.model.originals.OriginalQuizQuestion
import com.quovex.domain.model.originals.OriginalRealWorldExample
import com.quovex.domain.model.originals.OriginalSection
import com.quovex.domain.model.originals.OriginalWorkedExample
import com.quovex.domain.model.originals.QuovexOriginalBook
import com.quovex.domain.model.originals.SolutionStep
import com.quovex.domain.repository.QuovexOriginalsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuovexOriginalsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : QuovexOriginalsRepository {

    override fun getPublishedOriginals(
        subject: String?,
        curriculum: String?
    ): Flow<List<QuovexOriginalBook>> = callbackFlow {
        var query = firestore.collection("quovex_originals")
            .whereEqualTo("approvalStatus", "PUBLISHED")

        if (!subject.isNullOrBlank() && subject != "All") {
            query = query.whereEqualTo("subject", subject)
        }

        if (!curriculum.isNullOrBlank() && curriculum != "All") {
            query = query.whereEqualTo("curriculum", curriculum)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val books = snapshot.documents.mapNotNull { doc ->
                    mapDocumentToOriginalBook(doc)
                }
                trySend(books)
            } else {
                trySend(emptyList())
            }
        }

        awaitClose { listener.remove() }
    }

    override fun getOriginalBookDetails(bookId: String): Flow<QuovexOriginalBook?> = callbackFlow {
        val docRef = firestore.collection("quovex_originals").document(bookId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists() && snapshot.getString("approvalStatus") == "PUBLISHED") {
                val book = mapDocumentToOriginalBook(snapshot)
                trySend(book)
            } else {
                trySend(null)
            }
        }

        awaitClose { listener.remove() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapDocumentToOriginalBook(doc: DocumentSnapshot): QuovexOriginalBook? {
        return try {
            val id = doc.id
            val title = doc.getString("title") ?: return null
            val subtitle = doc.getString("subtitle")
            val description = doc.getString("description") ?: ""
            val subject = doc.getString("subject") ?: "General"
            val topic = doc.getString("topic") ?: title
            val language = doc.getString("language") ?: "en"
            val countryRegion = doc.getString("countryRegion") ?: "IN"
            val curriculum = doc.getString("curriculum") ?: "CBSE"
            val gradeClass = doc.getString("gradeClass") ?: "Class 11"
            val exam = doc.getString("exam")
            val difficulty = doc.getString("difficulty") ?: "Intermediate"
            val targetReadingTimeMinutes = (doc.getLong("targetReadingTimeMinutes") ?: 45L).toInt()
            val chapterCount = (doc.getLong("chapterCount") ?: 0L).toInt()
            val coverImageUrl = doc.getString("coverImageUrl")
            val introduction = doc.getString("introduction") ?: ""
            val learningObjectives = (doc.get("learningObjectives") as? List<String>) ?: emptyList()
            val prerequisites = (doc.get("prerequisites") as? List<String>) ?: emptyList()
            val publishedAt = doc.getLong("publishedAt") ?: 0L
            val isStaging = doc.getBoolean("isStaging") ?: false

            val rawChapters = doc.get("chapters") as? List<Map<String, Any>> ?: emptyList()
            val chapters = rawChapters.mapIndexed { index, map ->
                mapToChapter(index + 1, map)
            }

            QuovexOriginalBook(
                id = id,
                title = title,
                subtitle = subtitle,
                description = description,
                subject = subject,
                topic = topic,
                language = language,
                countryRegion = countryRegion,
                curriculum = curriculum,
                gradeClass = gradeClass,
                exam = exam,
                difficulty = difficulty,
                targetReadingTimeMinutes = targetReadingTimeMinutes,
                chapterCount = if (chapterCount > 0) chapterCount else chapters.size,
                coverImageUrl = coverImageUrl,
                introduction = introduction,
                learningObjectives = learningObjectives,
                prerequisites = prerequisites,
                chapters = chapters,
                publishedAt = publishedAt,
                isStaging = isStaging
            )
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToChapter(fallbackNumber: Int, map: Map<String, Any>): OriginalChapter {
        val chapterNumber = (map["chapterNumber"] as? Number)?.toInt() ?: fallbackNumber
        val title = (map["title"] as? String) ?: "Chapter $chapterNumber"
        val summary = (map["summary"] as? String) ?: ""
        val learningObjectives = (map["learningObjectives"] as? List<String>) ?: emptyList()
        val quickRevisionBulletPoints = (map["quickRevisionBulletPoints"] as? List<String>) ?: emptyList()

        val rawSections = map["sections"] as? List<Map<String, Any>> ?: emptyList()
        val sections = rawSections.mapIndexed { sIdx, sMap ->
            mapToSection(sIdx + 1, sMap)
        }

        val rawFlashcards = map["flashcards"] as? List<Map<String, Any>> ?: emptyList()
        val flashcards = rawFlashcards.map { fMap ->
            OriginalFlashcard(
                id = (fMap["id"] as? String) ?: "fc_${Math.random()}",
                frontPrompt = (fMap["frontPrompt"] as? String) ?: "",
                backAnswer = (fMap["backAnswer"] as? String) ?: "",
                conceptTag = (fMap["conceptTag"] as? String) ?: "",
                difficultyRating = ((fMap["difficultyRating"] as? Number)?.toInt()) ?: 2
            )
        }

        val rawQuiz = map["quizQuestions"] as? List<Map<String, Any>> ?: emptyList()
        val quizQuestions = rawQuiz.map { qMap ->
            OriginalQuizQuestion(
                id = (qMap["id"] as? String) ?: "quiz_${Math.random()}",
                question = (qMap["question"] as? String) ?: "",
                options = (qMap["options"] as? List<String>) ?: emptyList(),
                correctIndex = ((qMap["correctIndex"] as? Number)?.toInt()) ?: 0,
                pedagogicalExplanation = (qMap["pedagogicalExplanation"] as? String) ?: "",
                distractorExplanations = (qMap["distractorExplanations"] as? List<String>) ?: emptyList(),
                formulaReference = qMap["formulaReference"] as? String
            )
        }

        return OriginalChapter(
            chapterNumber = chapterNumber,
            title = title,
            summary = summary,
            learningObjectives = learningObjectives,
            sections = sections,
            quickRevisionBulletPoints = quickRevisionBulletPoints,
            flashcards = flashcards,
            quizQuestions = quizQuestions
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToSection(fallbackIndex: Int, map: Map<String, Any>): OriginalSection {
        val id = (map["id"] as? String) ?: "sec_$fallbackIndex"
        val sectionNumber = (map["sectionNumber"] as? String) ?: "$fallbackIndex.0"
        val title = (map["title"] as? String) ?: "Section $sectionNumber"
        val conceptualExplanation = (map["conceptualExplanation"] as? String) ?: ""
        val visualAnalogy = map["visualAnalogy"] as? String
        val summaryPoints = (map["summaryPoints"] as? List<String>) ?: emptyList()

        val rawWorkedExamples = map["workedExamples"] as? List<Map<String, Any>> ?: emptyList()
        val workedExamples = rawWorkedExamples.map { wMap ->
            val rawSteps = wMap["stepByStepSolution"] as? List<Map<String, Any>> ?: emptyList()
            val steps = rawSteps.mapIndexed { stepIdx, stepMap ->
                SolutionStep(
                    stepNumber = (stepMap["stepNumber"] as? Number)?.toInt() ?: (stepIdx + 1),
                    explanation = (stepMap["explanation"] as? String) ?: "",
                    mathFormula = stepMap["mathFormula"] as? String
                )
            }
            OriginalWorkedExample(
                id = (wMap["id"] as? String) ?: "we_${Math.random()}",
                problemStatement = (wMap["problemStatement"] as? String) ?: "",
                stepByStepSolution = steps,
                keyTakeaway = (wMap["keyTakeaway"] as? String) ?: "",
                difficulty = (wMap["difficulty"] as? String) ?: "Intermediate"
            )
        }

        val rawRealWorld = map["realWorldExamples"] as? List<Map<String, Any>> ?: emptyList()
        val realWorldExamples = rawRealWorld.map { rMap ->
            OriginalRealWorldExample(
                id = (rMap["id"] as? String) ?: "rw_${Math.random()}",
                domain = (rMap["domain"] as? String) ?: "Engineering",
                title = (rMap["title"] as? String) ?: "",
                narrative = (rMap["narrative"] as? String) ?: "",
                physicsOrConceptPrinciple = (rMap["physicsOrConceptPrinciple"] as? String) ?: ""
            )
        }

        val rawMistakes = map["commonMistakes"] as? List<Map<String, Any>> ?: emptyList()
        val commonMistakes = rawMistakes.map { mMap ->
            OriginalCommonMistake(
                id = (mMap["id"] as? String) ?: "cm_${Math.random()}",
                misconception = (mMap["misconception"] as? String) ?: "",
                whyStudentsMakeIt = (mMap["whyStudentsMakeIt"] as? String) ?: "",
                correctUnderstanding = (mMap["correctUnderstanding"] as? String) ?: "",
                quickCheck = (mMap["quickCheck"] as? String) ?: ""
            )
        }

        return OriginalSection(
            id = id,
            sectionNumber = sectionNumber,
            title = title,
            conceptualExplanation = conceptualExplanation,
            visualAnalogy = visualAnalogy,
            workedExamples = workedExamples,
            realWorldExamples = realWorldExamples,
            commonMistakes = commonMistakes,
            summaryPoints = summaryPoints
        )
    }
}
