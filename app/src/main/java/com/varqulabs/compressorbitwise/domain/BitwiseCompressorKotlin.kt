package com.varqulabs.compressorbitwise.domain

interface Compressor {
    fun compress(input: String): ByteArray
    fun decompress(data: ByteArray): String
}

class BitwiseCompressorKotlin(
    private val characterMapper: CharacterMapper
) : Compressor {

    // Convierte una cadena en un ByteArray comprimido, usando bits compactos por carácter.
    override fun compress(input: String): ByteArray {
        val bitsPerChar = characterMapper.bitsPerCharacter
        val totalBits = input.length * bitsPerChar
        val totalBytes = (totalBits + 7) / 8 // Redondea hacia arriba para acomodar bits sobrantes
        val compressedBytes = ByteArray(totalBytes)

        var currentBitIndex = 0

        for (char in input) {
            val charIndex = characterMapper.mapToIndex(char)
            writeBitsToByteArray(
                byteArray = compressedBytes,
                value = charIndex,
                bitCount = bitsPerChar,
                startBitIndex = currentBitIndex
            )
            currentBitIndex += bitsPerChar
        }

        return compressedBytes
    }

    // Convierte un ByteArray comprimido de vuelta a su representación original como cadena.
    override fun decompress(data: ByteArray): String {
        val bitsPerChar = characterMapper.bitsPerCharacter
        val totalBits = data.size * 8
        val totalCharacters = totalBits / bitsPerChar
        val decompressedString = StringBuilder(totalCharacters)

        var currentBitIndex = 0
        for (i in 0 until totalCharacters) {
            val charIndex = readBitsFromByteArray(
                byteArray = data,
                bitCount = bitsPerChar,
                startBitIndex = currentBitIndex
            )
            decompressedString.append(characterMapper.mapToChar(charIndex))
            currentBitIndex += bitsPerChar
        }

        return decompressedString.toString()
    }

    // Escribe los bits de un valor entero en un ByteArray a partir de un índice de bit específico.
    private fun writeBitsToByteArray(byteArray: ByteArray, value: Int, bitCount: Int, startBitIndex: Int) {
        for (bitOffset in 0 until bitCount) {
            val bitValue = (value shr (bitCount - 1 - bitOffset)) and 1
            val byteIndex = (startBitIndex + bitOffset) / 8
            val bitPositionInByte = 7 - ((startBitIndex + bitOffset) % 8)
            byteArray[byteIndex] = (byteArray[byteIndex].toInt() or (bitValue shl bitPositionInByte)).toByte()
        }
    }

    // Lee un conjunto de bits desde un ByteArray, a partir de un índice de bit específico, y devuelve el valor entero resultante.
    private fun readBitsFromByteArray(byteArray: ByteArray, bitCount: Int, startBitIndex: Int): Int {
        var result = 0
        for (bitOffset in 0 until bitCount) {
            val byteIndex = (startBitIndex + bitOffset) / 8
            val bitPositionInByte = 7 - ((startBitIndex + bitOffset) % 8)
            val bitValue = (byteArray[byteIndex].toInt() shr bitPositionInByte) and 1
            result = (result shl 1) or bitValue
        }
        return result
    }

}
