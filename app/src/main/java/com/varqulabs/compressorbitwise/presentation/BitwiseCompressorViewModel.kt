package com.varqulabs.compressorbitwise.presentation

import androidx.lifecycle.ViewModel
import com.varqulabs.compressorbitwise.domain.BitwiseCompressor
import com.varqulabs.compressorbitwise.domain.CharacterMapperImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class BitwiseCompressorViewModel : ViewModel() {

    private val characterMapper = CharacterMapperImpl(
        allowedChars = buildList {
            addAll(('A'..'Z'))
            addAll(('a'..'z'))
            addAll('0'..'9')
            add('\n')
            add(' ')
        },
        fallbackChar = ' ',
        fallbackIndex = 63
    )

    private val bitwiseCompressor = BitwiseCompressor(characterMapper)

    private val _state = MutableStateFlow(BitwiseCompressorState())
    val state: StateFlow<BitwiseCompressorState> = _state

    fun validateAndCalculateSizes(input: String) {
        for (char in input) {
            if (!characterMapper.isAllowed(char)) {
                _state.value = BitwiseCompressorState(
                    inputText = input,
                    errorMessage = "Carácter '$char' no permitido.",
                    originalSizeBytes = input.length,
                    compressedSizeBytes = 0
                )
                return
            }
        }

        val originalSizeBytes = input.length
        val bitsPerChar = characterMapper.bitsPerCharacter
        val compressedSizeBytes = (originalSizeBytes * bitsPerChar + 7) / 8

        _state.value = BitwiseCompressorState(
            inputText = input,
            errorMessage = null,
            originalSizeBytes = originalSizeBytes,
            compressedSizeBytes = compressedSizeBytes
        )
    }

    fun compressInput(): ByteArray? {
        return state.value.inputText.takeIf { it.isNotEmpty() }
            ?.let { bitwiseCompressor.compress(it) }
    }

    fun decompressData(data: ByteArray): String {
        return bitwiseCompressor.decompress(data)
    }

    fun updateSelectedTextFromDocument(selectedText: String) {
        _state.update {it.copy(selectedTextFromDocument = selectedText)}
    }
}