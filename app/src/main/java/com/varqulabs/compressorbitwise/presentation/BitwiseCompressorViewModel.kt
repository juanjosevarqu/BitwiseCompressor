package com.varqulabs.compressorbitwise.presentation

import androidx.lifecycle.ViewModel
import com.varqulabs.compressorbitwise.domain.BitwiseCompressor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class BitwiseCompressorViewModel : ViewModel() {

    private val characterMapper = CharacterMapper(
        allowedChars = buildList {
            addAll(('A'..'Z'))
            addAll(('a'..'z'))
            addAll('0'..'9')
            add('\n')
            add(' ')
        },
    )

    private val bitwiseCompressor = BitwiseCompressor(characterMapper)

    private val _state = MutableStateFlow(BitwiseCompressorState())
    val state: StateFlow<BitwiseCompressorState> = _state

    fun updateAndCalculateSizes(input: String) {
        for (char in input) {
            if (!characterMapper.isAllowed(char)) {
                _state.update {
                    it.copy(
                        inputText = input,
                        errorMessage = "Carácter '$char' no permitido.",
                        originalSizeBytes = input.length,
                        compressedSizeBytes = 0
                    )
                }
                return
            }
        }

        val originalSizeBytes = input.length
        val bitsPerChar = characterMapper.bitsPerCharacter
        val compressedSizeBytes = ((originalSizeBytes * bitsPerChar + 7) / 8) + 1 + 2

        _state.update {
            it.copy(
                inputText = input,
                errorMessage = null,
                originalSizeBytes = originalSizeBytes,
                compressedSizeBytes = compressedSizeBytes,
            )
        }
    }

    fun compressInput(): ByteArray? {
        if (state.value.inputText.isEmpty()) {
            return null
        }
        _state.update { it.copy(isLoading = true) }
        return try {
            val compressed = bitwiseCompressor.compress(state.value.inputText)
            _state.update { it.copy(isLoading = false) }
            compressed
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false) }
            null
        }
    }

    fun decompressData(data: ByteArray): String? {
        _state.update { it.copy(isLoading = true) }
        return try {
            val decompressedText = bitwiseCompressor.decompress(data)
            _state.update {
                it.copy(
                    selectedTextFromDocument = decompressedText,
                    isLoading = false
                )
            }
            decompressedText
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false) }
            null
        }
    }

    fun updateSelectedTextFromDocument(selectedText: String) {
        _state.update { it.copy(selectedTextFromDocument = selectedText) }
    }
}