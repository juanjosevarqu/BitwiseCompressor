package com.varqulabs.compressorbitwise.di

import com.varqulabs.compressorbitwise.domain.BitwiseCompressor
import com.varqulabs.compressorbitwise.domain.CharacterMapper
import com.varqulabs.compressorbitwise.domain.CharacterMapperImpl
import com.varqulabs.compressorbitwise.domain.FileSizeValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Provides
    @Singleton
    fun provideCharacterMapper(): CharacterMapper {
        return CharacterMapperImpl(
            allowedChars = buildList {
                addAll(('A'..'Z').filter { it != 'X' })
                addAll(('a'..'z').filter { it != 'x' })
                addAll('0'..'9')
                add(' ')
                add('.')
                add(',')
                add('\n')
            },
            fallbackChar = '.',
            fallbackIndex = 61
        )
    }

    @Provides
    @Singleton
    fun provideFileSizeValidator(
        characterMapper: CharacterMapper
    ): FileSizeValidator {
        return FileSizeValidator(characterMapper)
    }

    @Provides
    @Singleton
    fun provideBitwiseCompressor(
        characterMapper: CharacterMapper
    ): BitwiseCompressor {
        return BitwiseCompressor(characterMapper)
    }

}