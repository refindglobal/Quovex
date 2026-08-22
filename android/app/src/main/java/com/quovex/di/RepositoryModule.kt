package com.quovex.di

import com.quovex.data.repository.AiGatewayRepositoryImpl
import com.quovex.data.repository.QuovexRepositoryImpl
import com.quovex.domain.repository.AIRepository
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
}
