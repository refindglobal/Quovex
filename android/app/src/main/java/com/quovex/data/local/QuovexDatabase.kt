package com.quovex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quovex.data.local.dao.CommunityDao
import com.quovex.data.local.dao.FlashcardDao
import com.quovex.data.local.dao.MaterialDao
import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.dao.QuovexDao
import com.quovex.data.local.dao.SessionDao
import com.quovex.data.local.dao.StreakDao
import com.quovex.data.local.dao.StudyPlanDao
import com.quovex.data.local.dao.UserStatsDao
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.FlashcardEntity
import com.quovex.data.local.entity.FriendEntity
import com.quovex.data.local.entity.LeaderboardCacheEntity
import com.quovex.data.local.entity.NoteEntity
import com.quovex.data.local.entity.QuizMistakeEntity
import com.quovex.data.local.entity.QuizQuestionEntity
import com.quovex.data.local.entity.QuizResultEntity
import com.quovex.data.local.entity.SessionEntity
import com.quovex.data.local.entity.StreakEntity
import com.quovex.data.local.entity.StudyBattleEntity
import com.quovex.data.local.entity.StudyPlanEntity
import com.quovex.data.local.entity.StudyTaskEntity
import com.quovex.data.local.entity.SubjectEntity
import com.quovex.data.local.entity.UserStatsEntity

@Database(
    entities = [
        DeckEntity::class,
        FlashcardEntity::class,
        SessionEntity::class,
        NoteEntity::class,
        SubjectEntity::class,
        QuizQuestionEntity::class,
        QuizResultEntity::class,
        QuizMistakeEntity::class,
        UserStatsEntity::class,
        FriendEntity::class,
        StudyBattleEntity::class,
        LeaderboardCacheEntity::class,
        StudyPlanEntity::class,
        StudyTaskEntity::class,
        StreakEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class QuovexDatabase : RoomDatabase() {
    abstract fun dao(): QuovexDao
    abstract fun materialDao(): MaterialDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun sessionDao(): SessionDao
    abstract fun quizDao(): QuizDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun communityDao(): CommunityDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun streakDao(): StreakDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_stats` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `currentStreak` INTEGER NOT NULL DEFAULT 1,
                        `longestStreak` INTEGER NOT NULL DEFAULT 1,
                        `rescueTokens` INTEGER NOT NULL DEFAULT 1,
                        `totalXp` INTEGER NOT NULL DEFAULT 0,
                        `scholarLevel` INTEGER NOT NULL DEFAULT 1,
                        `lastStudyDateMillis` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT OR IGNORE INTO `user_stats` (`id`, `currentStreak`, `longestStreak`, `rescueTokens`, `totalXp`, `scholarLevel`, `lastStudyDateMillis`)
                    VALUES (1, 1, 1, 1, 0, 1, 0)
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // friends table — locally cached friend profiles
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `friends` (
                        `friendId` TEXT PRIMARY KEY NOT NULL,
                        `username` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `avatarId` INTEGER NOT NULL,
                        `scholarRank` TEXT NOT NULL,
                        `streakDays` INTEGER NOT NULL,
                        `totalStudyHours` REAL NOT NULL,
                        `topSubject` TEXT NOT NULL,
                        `isStudyingNow` INTEGER NOT NULL,
                        `cachedAtMillis` INTEGER NOT NULL
                    )
                """.trimIndent())

                // study_battles table — 1v1 weekly study challenges
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `study_battles` (
                        `battleId` TEXT PRIMARY KEY NOT NULL,
                        `challengerId` TEXT NOT NULL,
                        `challengerName` TEXT NOT NULL,
                        `challengerAvatarId` INTEGER NOT NULL,
                        `opponentId` TEXT NOT NULL,
                        `opponentName` TEXT NOT NULL,
                        `opponentAvatarId` INTEGER NOT NULL,
                        `targetExam` TEXT NOT NULL,
                        `challengerMinutes` INTEGER NOT NULL,
                        `opponentMinutes` INTEGER NOT NULL,
                        `startDateMillis` INTEGER NOT NULL,
                        `endDateMillis` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `cachedAtMillis` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_battles_challengerId` ON `study_battles` (`challengerId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_battles_opponentId` ON `study_battles` (`opponentId`)")

                // leaderboard_cache table — weekly leaderboard snapshots
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `leaderboard_cache` (
                        `leaderboardType` TEXT NOT NULL,
                        `subjectFilter` TEXT NOT NULL DEFAULT 'ALL',
                        `userId` TEXT NOT NULL,
                        `userName` TEXT NOT NULL,
                        `avatarId` INTEGER NOT NULL,
                        `scholarRank` TEXT NOT NULL,
                        `studyMinutes` INTEGER NOT NULL,
                        `xp` INTEGER NOT NULL,
                        `rank` INTEGER NOT NULL,
                        `isCurrentUser` INTEGER NOT NULL,
                        `trend` TEXT NOT NULL,
                        `weekKey` TEXT NOT NULL,
                        `cachedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY (`leaderboardType`, `userId`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // study_plans table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `study_plans` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `targetExam` TEXT NOT NULL,
                        `examDateMillis` INTEGER NOT NULL,
                        `dailyStudyHours` REAL NOT NULL,
                        `targetSubjectsCsv` TEXT NOT NULL,
                        `weakTopicsCsv` TEXT NOT NULL,
                        `totalDays` INTEGER NOT NULL,
                        `currentDay` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                """.trimIndent())

                // study_tasks table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `study_tasks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `planId` INTEGER NOT NULL,
                        `dayNumber` INTEGER NOT NULL,
                        `dateMillis` INTEGER NOT NULL,
                        `subject` TEXT NOT NULL,
                        `topic` TEXT NOT NULL,
                        `taskType` TEXT NOT NULL,
                        `estimatedMinutes` INTEGER NOT NULL,
                        `completedMinutes` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_tasks_planId` ON `study_tasks` (`planId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_tasks_dateMillis` ON `study_tasks` (`dateMillis`)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `streaks_cemetery` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `streakDays` INTEGER NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `endDate` INTEGER NOT NULL,
                        `isBroken` INTEGER NOT NULL DEFAULT 1,
                        `causeOfDeath` TEXT NOT NULL DEFAULT 'Missed daily focus goal',
                        `reflectionNote` TEXT,
                        `tokensUsed` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
    }
}
