package com.quovex.di

import com.quovex.data.repository.AiGatewayRepositoryImpl
import com.quovex.data.repository.NcertPdfCacheRepositoryImpl
import com.quovex.data.repository.QuovexRepositoryImpl
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.repository.NcertPdfCacheRepository
import com.quovex.domain.repository.QuovexRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuovexRepository(
        impl: QuovexRepositoryImpl
    ): QuovexRepository

    @Binds
    @Singleton
    abstract fun bindAIRepository(
        impl: AiGatewayRepositoryImpl
    ): AIRepository

    @Binds
    @Singleton
    abstract fun bindNcertRepository(
        impl: com.quovex.data.repository.NcertRepositoryImpl
    ): com.quovex.domain.repository.NcertRepository

    /** PDF cache repository — separate from catalog repository */
    @Binds
    @Singleton
    abstract fun bindNcertPdfCacheRepository(
        impl: NcertPdfCacheRepositoryImpl
    ): NcertPdfCacheRepository

    /** Quovex Originals repository */
    @Binds
    @Singleton
    abstract fun bindQuovexOriginalsRepository(
        impl: com.quovex.data.repository.QuovexOriginalsRepositoryImpl
    ): com.quovex.domain.repository.QuovexOriginalsRepository

    /** Ambient Focus Soundscape repository */
    @Binds
    @Singleton
    abstract fun bindSoundscapeRepository(
        impl: com.quovex.data.repository.SoundscapeRepositoryImpl
    ): com.quovex.domain.repository.SoundscapeRepository

    /** Camera AI Focus Detection repository */
    @Binds
    @Singleton
    abstract fun bindFocusDetectionRepository(
        impl: com.quovex.data.repository.FocusDetectionRepositoryImpl
    ): com.quovex.domain.repository.FocusDetectionRepository

    /** Google Play Billing & Subscription repository */
    @Binds
    @Singleton
    abstract fun bindBillingRepository(
        impl: com.quovex.data.repository.BillingRepositoryImpl
    ): com.quovex.domain.repository.BillingRepository

    /** Multi-Layer Distraction Blocker repository */
    @Binds
    @Singleton
    abstract fun bindDistractionBlockerRepository(
        impl: com.quovex.data.repository.DistractionBlockerRepositoryImpl
    ): com.quovex.domain.repository.DistractionBlockerRepository

    /** Google Mobile Ads Manager */
    @Binds
    @Singleton
    abstract fun bindAdManager(
        impl: com.quovex.data.admob.AdMobManagerImpl
    ): com.quovex.domain.manager.AdManager

    /** Web Article & YouTube Content Extraction repository */
    @Binds
    @Singleton
    abstract fun bindContentExtractionRepository(
        impl: com.quovex.data.repository.ContentExtractionRepositoryImpl
    ): com.quovex.domain.repository.ContentExtractionRepository

    /** Weekly PDF Study Report Generator */
    @Binds
    @Singleton
    abstract fun bindPdfReportGenerator(
        impl: com.quovex.data.analytics.PdfReportGeneratorImpl
    ): com.quovex.domain.manager.PdfReportGenerator

    /** Daily Diagnostic Quiz & Remedial Synthesis repository */
    @Binds
    @Singleton
    abstract fun bindDiagnosticQuizRepository(
        impl: com.quovex.data.repository.DiagnosticQuizRepositoryImpl
    ): com.quovex.domain.repository.DiagnosticQuizRepository

    /** Streak Protection, Rescue Tokens & Cemetery repository */
    @Binds
    @Singleton
    abstract fun bindStreakRepository(
        impl: com.quovex.data.repository.StreakRepositoryImpl
    ): com.quovex.domain.repository.StreakRepository
}

