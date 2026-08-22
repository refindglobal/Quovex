package com.quovex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quovex.data.local.dao.FlashcardDao
import com.quovex.data.local.dao.MaterialDao
import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.dao.QuovexDao
import com.quovex.data.local.dao.SessionDao
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.FlashcardEntity
import com.quovex.data.local.entity.NoteEntity
import com.quovex.data.local.entity.QuizMistakeEntity
import com.quovex.data.local.entity.QuizQuestionEntity
import com.quovex.data.local.entity.QuizResultEntity
import com.quovex.data.local.entity.SessionEntity
import com.quovex.data.local.entity.SubjectEntity

@Database(
    entities = [
        DeckEntity::class,
        FlashcardEntity::class,
        SessionEntity::class,
        NoteEntity::class,
        SubjectEntity::class,
        QuizQuestionEntity::class,
        QuizResultEntity::class,
        QuizMistakeEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class QuovexDatabase : RoomDatabase() {
    abstract fun dao(): QuovexDao
    abstract fun materialDao(): MaterialDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun sessionDao(): SessionDao
    abstract fun quizDao(): QuizDao

    companion object {
        const val DATABASE_NAME = "quovex_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cloudId` TEXT,
                        `title` TEXT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `status` TEXT NOT NULL DEFAULT 'READY',
                        `inputType` TEXT NOT NULL DEFAULT 'TEXT',
                        `sourceUrl` TEXT,
                        `storageRef` TEXT,
                        `keyPointsJson` TEXT,
                        `flashcardCount` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_subject` ON `notes` (`subject`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_createdAt` ON `notes` (`createdAt`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. notes table: 9 new columns for Learning Transformation System
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `topic` TEXT")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `summary` TEXT")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `formulasJson` TEXT")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `inferredSubject` TEXT")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `inferredTopic` TEXT")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `inferredConfidence` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `flashcardDeckId` INTEGER")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `quizGenerated` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `syncStatus` TEXT NOT NULL DEFAULT 'PENDING_SYNC'")

                // 2. decks table: 1 new column for source material linkage
                db.execSQL("ALTER TABLE `decks` ADD COLUMN `sourceMaterialId` INTEGER")

                // 3. flashcards table: 4 new columns for remedial cards and tags
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `tags` TEXT")
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `formulaLatex` TEXT")
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `isRemedial` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `difficulty` INTEGER NOT NULL DEFAULT 3")

                // 4. sessions table: 1 new column for subject tracking
                db.execSQL("ALTER TABLE `sessions` ADD COLUMN `subject` TEXT NOT NULL DEFAULT ''")

                // 5. New tables: subjects
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `subjects` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `examRelevance` TEXT NOT NULL DEFAULT '',
                        `masteryLevel` INTEGER NOT NULL DEFAULT 1,
                        `totalMaterials` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // 6. New tables: quiz_questions
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `quiz_questions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `materialId` INTEGER NOT NULL,
                        `question` TEXT NOT NULL,
                        `optionsJson` TEXT NOT NULL,
                        `correctIndex` INTEGER NOT NULL,
                        `explanation` TEXT NOT NULL,
                        `relatedConcept` TEXT NOT NULL,
                        `difficulty` INTEGER NOT NULL DEFAULT 3
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_quiz_questions_materialId` ON `quiz_questions` (`materialId`)")

                // 7. New tables: quiz_results
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `quiz_results` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `materialId` INTEGER NOT NULL,
                        `takenAt` INTEGER NOT NULL,
                        `score` INTEGER NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `accuracyPercent` REAL NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_quiz_results_materialId` ON `quiz_results` (`materialId`)")

                // 8. New tables: quiz_mistakes
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `quiz_mistakes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `resultId` INTEGER NOT NULL,
                        `questionId` INTEGER NOT NULL,
                        `questionText` TEXT NOT NULL,
                        `studentAnswer` TEXT NOT NULL,
                        `correctAnswer` TEXT NOT NULL,
                        `explanation` TEXT NOT NULL,
                        `concept` TEXT NOT NULL,
                        `remedialCardId` INTEGER
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_quiz_mistakes_resultId` ON `quiz_mistakes` (`resultId`)")
            }
        }
    }
}
