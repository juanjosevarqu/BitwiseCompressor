package com.varqulabs.compressorbitwise.domain

class FileSizeValidator(
    private val characterMapper: CharacterMapper
) {
    fun validateAndCalculateSizes(input: String): ValidationResult {
        for (char in input) {
            if (!characterMapper.isAllowed(char)) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Carácter '$char' no permitido.",
                    originalSizeBytes = input.length,
                    compressedSizeBytes = 0
                )
            }
        }

        val originalSizeBytes = input.length
        val bitsPerChar = characterMapper.bitsPerCharacter
        val compressedSizeBytes = (originalSizeBytes * bitsPerChar + 7) / 8

        return ValidationResult(
            isValid = true,
            errorMessage = null,
            originalSizeBytes = originalSizeBytes,
            compressedSizeBytes = compressedSizeBytes
        )
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?,
    val originalSizeBytes: Int,
    val compressedSizeBytes: Int
)
