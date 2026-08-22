package com.quovex.di

import android.content.Context
import androidx.room.Room
import com.quovex.data.local.QuovexDatabase
import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.FlashcardDao
import com.quovex.data.local.dao.MaterialDao
import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.dao.QuovexDao
import com.quovex.data.local.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQuovexDatabase(
        @ApplicationContext context: Context
    ): QuovexDatabase {
        return Room.databaseBuilder(
            context,
            QuovexDatabase::class.java,
            QuovexDatabase.DATABASE_NAME
        )
        .addMigrations(
            QuovexDatabase.MIGRATION_1_2,
            QuovexDatabase.MIGRATION_2_3
        )
        .build()
    }

    @Provides
    fun provideQuovexDao(database: QuovexDatabase): QuovexDao {
        return database.dao()
    }

    @Provides
    fun provideMaterialDao(database: QuovexDatabase): MaterialDao {
        return database.materialDao()
    }

    @Provides
    fun provideFlashcardDao(database: QuovexDatabase): FlashcardDao {
        return database.flashcardDao()
    }

    @Provides
    fun provideSessionDao(database: QuovexDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    fun provideQuizDao(database: QuovexDatabase): QuizDao {
        return database.quizDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesManager(
        @ApplicationContext context: Context
    ): UserPreferencesManager {
        val prefs = context.getSharedPreferences("quovex_user_prefs", Context.MODE_PRIVATE)
        return UserPreferencesManager(prefs)
    }
}
