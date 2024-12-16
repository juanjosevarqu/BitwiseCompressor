package com.varqulabs.compressorbitwise.presentation

import androidx.compose.runtime.Stable

@Stable
data class BitwiseCompressorState(
    val inputText: String = "",
    val originalSizeBytes: Int = 0,
    val compressedSizeBytes: Int = 0,
    val errorMessage: String? = null,
    val selectedTextFromDocument: String? = null,
)
