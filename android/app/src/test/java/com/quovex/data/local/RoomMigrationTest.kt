package com.quovex.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class RoomMigrationTest {

    @Test
    fun `migration 1 to 2 executes create table notes and indices`() {
        val executedSqlList = mutableListOf<String>()

        val handler = InvocationHandler { _, method: Method, args: Array<out Any?>? ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                val sql = args[0] as? String
                if (sql != null) {
                    executedSqlList.add(sql)
                }
            }
            null
        }

        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
            handler
        ) as SupportSQLiteDatabase

        QuovexDatabase.MIGRATION_1_2.migrate(mockDb)

        assertEquals(3, executedSqlList.size)

        val createTableSql = executedSqlList[0]
        val indexSubjectSql = executedSqlList[1]
        val indexCreatedAtSql = executedSqlList[2]

        assertTrue(createTableSql.contains("CREATE TABLE IF NOT EXISTS `notes`"))
        assertTrue(createTableSql.contains("`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL"))
        assertTrue(createTableSql.contains("`title` TEXT NOT NULL"))
        assertTrue(createTableSql.contains("`subject` TEXT NOT NULL"))
        assertTrue(createTableSql.contains("`content` TEXT NOT NULL"))

        assertTrue(indexSubjectSql.contains("index_notes_subject"))
        assertTrue(indexCreatedAtSql.contains("index_notes_createdAt"))

        assertEquals(1, QuovexDatabase.MIGRATION_1_2.startVersion)
        assertEquals(2, QuovexDatabase.MIGRATION_1_2.endVersion)
    }

    @Test
    fun `migration 2 to 3 executes all 15 column additions and 4 new tables`() {
        val executedSqlList = mutableListOf<String>()

        val handler = InvocationHandler { _, method: Method, args: Array<out Any?>? ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                val sql = args[0] as? String
                if (sql != null) {
                    executedSqlList.add(sql)
                }
            }
            null
        }

        val mockDb = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
            handler
        ) as SupportSQLiteDatabase

        QuovexDatabase.MIGRATION_2_3.migrate(mockDb)

        assertEquals(2, QuovexDatabase.MIGRATION_2_3.startVersion)
        assertEquals(3, QuovexDatabase.MIGRATION_2_3.endVersion)

        val allSql = executedSqlList.joinToString("\n")

        // 1. Check notes additions (9 columns)
        assertTrue(allSql.contains("ALTER TABLE `notes` ADD COLUMN `topic` TEXT"))
        assertTrue(allSql.contains("ALTER TABLE `notes` ADD COLUMN `summary` TEXT"))
        assertTrue(allSql.contains("ALTER TABLE `notes` ADD COLUMN `formulasJson` TEXT"))
        assertTrue(allSql.contains("ALTER TABLE `notes` ADD COLUMN `inferredSubject` TEXT"))
        assertTrue(allSql.contains("ALTER TABLE `notes` ADD COLUMN `inferredTopic` TEXT"))
        assertTrue(allSql.contains("ALTER TABLE `notes` ADD COLUMN `inferredConfidence` REAL"))
        assertTrue(allSql.contains("ALTER TABLE `notes` ADD COLUMN `flashcardDeckId` INTEGER"))
        assertTrue(allSql.contains("ALTER TABLE `notes` ADD COLUMN `quizGenerated` INTEGER"))
        assertTrue(allSql.contains("ALTER TABLE `notes` ADD COLUMN `syncStatus` TEXT"))

        // 2. Check decks addition (1 column)
        assertTrue(allSql.contains("ALTER TABLE `decks` ADD COLUMN `sourceMaterialId` INTEGER"))

        // 3. Check flashcards additions (4 columns)
        assertTrue(allSql.contains("ALTER TABLE `flashcards` ADD COLUMN `tags` TEXT"))
        assertTrue(allSql.contains("ALTER TABLE `flashcards` ADD COLUMN `formulaLatex` TEXT"))
        assertTrue(allSql.contains("ALTER TABLE `flashcards` ADD COLUMN `isRemedial` INTEGER"))
        assertTrue(allSql.contains("ALTER TABLE `flashcards` ADD COLUMN `difficulty` INTEGER"))

        // 4. Check sessions addition (1 column)
        assertTrue(allSql.contains("ALTER TABLE `sessions` ADD COLUMN `subject` TEXT"))

        // 5. Check new tables
        assertTrue(allSql.contains("CREATE TABLE IF NOT EXISTS `subjects`"))
        assertTrue(allSql.contains("CREATE TABLE IF NOT EXISTS `quiz_questions`"))
        assertTrue(allSql.contains("CREATE TABLE IF NOT EXISTS `quiz_results`"))
        assertTrue(allSql.contains("CREATE TABLE IF NOT EXISTS `quiz_mistakes`"))
        assertTrue(allSql.contains("index_quiz_questions_materialId"))
        assertTrue(allSql.contains("index_quiz_results_materialId"))
        assertTrue(allSql.contains("index_quiz_mistakes_resultId"))
    }
}
