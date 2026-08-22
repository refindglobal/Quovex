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

        // Verify table creation contains all required note fields
        assertTrue(createTableSql.contains("CREATE TABLE IF NOT EXISTS `notes`"))
        assertTrue(createTableSql.contains("`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL"))
        assertTrue(createTableSql.contains("`title` TEXT NOT NULL"))
        assertTrue(createTableSql.contains("`subject` TEXT NOT NULL"))
        assertTrue(createTableSql.contains("`content` TEXT NOT NULL"))
        assertTrue(createTableSql.contains("`status` TEXT NOT NULL DEFAULT 'READY'"))
        assertTrue(createTableSql.contains("`inputType` TEXT NOT NULL DEFAULT 'TEXT'"))
        assertTrue(createTableSql.contains("`storageRef` TEXT"))
        assertTrue(createTableSql.contains("`keyPointsJson` TEXT"))
        assertTrue(createTableSql.contains("`flashcardCount` INTEGER NOT NULL DEFAULT 0"))

        // Verify indices
        assertTrue(indexSubjectSql.contains("index_notes_subject"))
        assertTrue(indexCreatedAtSql.contains("index_notes_createdAt"))

        assertEquals(1, QuovexDatabase.MIGRATION_1_2.startVersion)
        assertEquals(2, QuovexDatabase.MIGRATION_1_2.endVersion)
    }
}
