package com.quovex.di

import android.content.Context
import androidx.room.Room
import com.quovex.data.local.QuovexDatabase
import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.QuovexDao
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
        .addMigrations(QuovexDatabase.MIGRATION_1_2)
        .build()
    }

    @Provides
    fun provideQuovexDao(database: QuovexDatabase): QuovexDao {
        return database.dao()
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
