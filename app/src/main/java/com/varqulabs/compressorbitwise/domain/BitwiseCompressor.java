package com.varqulabs.compressorbitwise.domain;

import androidx.annotation.NonNull;

public class BitwiseCompressor implements Compressor {

    private final CharacterMapper characterMapper;

    public BitwiseCompressor(CharacterMapper characterMapper) {
        this.characterMapper = characterMapper;
    }

    @NonNull
    @Override
    public byte[] compress(String input) {
        int bitsPerChar = characterMapper.getBitsPerCharacter();
        int totalBits = input.length() * bitsPerChar;
        int totalBytes = (totalBits + 7) / 8;
        byte[] compressedBytes = new byte[totalBytes];

        int bitIndex = 0;

        for (char character : input.toCharArray()) {
            int charIndex = characterMapper.mapToIndex(character);
            writeBitsToByteArray(compressedBytes, charIndex, bitsPerChar, bitIndex);
            bitIndex = bitIndex + bitsPerChar; // Evitamos `+=`
        }

        return compressedBytes;
    }

    @NonNull
    @Override
    public String decompress(byte[] data) {
        int bitsPerChar = characterMapper.getBitsPerCharacter();
        int totalBits = data.length * 8;
        int totalCharacters = totalBits / bitsPerChar;
        StringBuilder decompressedString = new StringBuilder(totalCharacters);

        int bitIndex = 0;

        for (int i = 0; i < totalCharacters; i++) {
            int charIndex = readBitsFromByteArray(data, bitsPerChar, bitIndex);
            decompressedString.append(characterMapper.mapToChar(charIndex));
            bitIndex = bitIndex + bitsPerChar; // Evitamos `+=`
        }

        return decompressedString.toString();
    }

    private void writeBitsToByteArray(byte[] byteArray, int value, int bitCount, int startBitIndex) {
        for (int bitOffset = 0; bitOffset < bitCount; bitOffset++) {
            // Extraer el bit individual desde "value"
            int bitValue = (value >> (bitCount - 1 - bitOffset)) & 1;

            // Calcular la posición del byte dentro del array
            int byteIndex = (startBitIndex + bitOffset) / 8;

            // Calcular la posición del bit dentro del byte actual
            int bitPositionInByte = 7 - ((startBitIndex + bitOffset) % 8);

            // Realizar la operación OR explícita para establecer el bit
            byteArray[byteIndex] = (byte) (byteArray[byteIndex] | (bitValue << bitPositionInByte));
        }
    }

    private int readBitsFromByteArray(byte[] byteArray, int bitCount, int startBitIndex) {
        int result = 0;

        for (int bitOffset = 0; bitOffset < bitCount; bitOffset++) {
            // Calcular la posición del byte dentro del array
            int byteIndex = (startBitIndex + bitOffset) / 8;

            // Calcular la posición del bit dentro del byte actual
            int bitPositionInByte = 7 - ((startBitIndex + bitOffset) % 8);

            // Extraer el bit individual desde el byte
            int bitValue = (byteArray[byteIndex] >> bitPositionInByte) & 1;

            // Realizar la operación OR explícita para acumular el bit en "result"
            result = (result << 1) | bitValue;
        }

        return result;
    }
}

