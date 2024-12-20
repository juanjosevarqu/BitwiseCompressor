package com.varqulabs.compressorbitwise.negocio

import kotlin.math.ceil
import kotlin.math.log2

class ValidadorDeCaracteres(caracteresPermitidos: List<Char>) {
   // caracteresPermitidos = ['A', 'B', 'C']
    private val caracteresConIndices: Map<Char, Int> = caracteresPermitidos.withIndex().associate { it.value to it.index }

    private val indicesConCaracteres: Map<Int, Char> = caracteresPermitidos.withIndex().associate { it.index to it.value }

    val bitsPorCaracter: Byte = ceil(log2(caracteresPermitidos.size.toDouble())).toInt().toByte()

    fun obtenerCaracter(indice: Int): Char = indicesConCaracteres[indice] ?: ' '

    fun obtenerIndice(caracter: Char): Int = caracteresConIndices[caracter] ?: 63

    fun esValido(caracter: Char): Boolean = caracteresConIndices.containsKey(caracter)
}