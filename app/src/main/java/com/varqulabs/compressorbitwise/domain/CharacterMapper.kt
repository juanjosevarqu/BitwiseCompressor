package com.varqulabs.compressorbitwise.domain

import kotlin.math.ceil
import kotlin.math.log2

interface CharacterMapper {
    val bitsPerCharacter: Int
    fun mapToChar(index: Int): Char
    fun mapToIndex(char: Char): Int
    fun isAllowed(char: Char): Boolean
}

class CharacterMapperImpl(
    allowedChars: List<Char>,
    private val fallbackChar: Char,
    private val fallbackIndex: Int
) : CharacterMapper {

    private val charactersToIndexes: Map<Char, Int> = allowedChars.withIndex().associate { it.value to it.index }
    private val indexesToCharacters: Map<Int, Char> = allowedChars.withIndex().associate { it.index to it.value }

    override val bitsPerCharacter: Int = ceil(log2(allowedChars.size.toDouble())).toInt()

    override fun mapToChar(index: Int): Char = indexesToCharacters[index] ?: fallbackChar

    override fun mapToIndex(char: Char): Int = charactersToIndexes[char] ?: fallbackIndex

    override fun isAllowed(char: Char): Boolean = charactersToIndexes.containsKey(char)
}