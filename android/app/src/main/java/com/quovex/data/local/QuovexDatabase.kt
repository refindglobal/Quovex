package com.quovex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quovex.data.local.dao.QuovexDao
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.FlashcardEntity
import com.quovex.data.local.entity.NoteEntity
import com.quovex.data.local.entity.SessionEntity

@Database(
    entities = [
        DeckEntity::class,
        FlashcardEntity::class,
        SessionEntity::class,
        NoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuovexDatabase : RoomDatabase() {
    abstract fun dao(): QuovexDao

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
    }
}
