package com.varqulabs.compressorbitwise.domain

import kotlin.math.ceil
import kotlin.math.log2

class CharacterMapper(allowedChars: List<Char>) {

    private val charactersToIndexes: Map<Char, Int> = allowedChars.withIndex().associate { it.value to it.index }
    private val indexesToCharacters: Map<Int, Char> = allowedChars.withIndex().associate { it.index to it.value }

    val bitsPerCharacter: Int = ceil(log2(allowedChars.size.toDouble())).toInt()

    fun mapToChar(index: Int): Char = indexesToCharacters[index] ?: ' '

    fun mapToIndex(char: Char): Int = charactersToIndexes[char] ?: 63

    fun isAllowed(char: Char): Boolean = charactersToIndexes.containsKey(char)
}