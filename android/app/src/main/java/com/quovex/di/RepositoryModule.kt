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
}

