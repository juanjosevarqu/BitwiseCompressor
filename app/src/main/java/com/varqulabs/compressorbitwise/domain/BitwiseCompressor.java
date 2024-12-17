package com.varqulabs.compressorbitwise.domain;

public class BitwiseCompressor {

    private final CharacterMapper characterMapper;

    byte HEADER; // HEADER 1 byte, LONGITUD de la cadena 2 bytes

    public BitwiseCompressor(CharacterMapper characterMapper) {
        this.characterMapper = characterMapper;
        HEADER = (byte) ((1 << 6) | (1 << 2)); // 0100 0100 igual a 68 en decimal
    }

    public byte[] compress(String input) {
        validateInput(input);

        int bitsPerChar = characterMapper.getBitsPerCharacter();
        int totalBits = input.length() * bitsPerChar;
        int totalBytesForChars = (totalBits + 7) / 8;

        byte[] compressedBytes = new byte[1 + 2 + totalBytesForChars];

        compressedBytes[0] = HEADER;

        int originalLength = input.length();
        compressedBytes[1] = (byte) (originalLength >> 8);
        compressedBytes[2] = (byte) (originalLength & 255); // que seria 0b11111111 en binario

        int bitIndex = 3 * 8; // Saltamos los primeros 3 bytes (24 bits)

        for (char character : input.toCharArray()) {
            int charIndex = characterMapper.mapToIndex(character);
            writeBitsToByteArray(compressedBytes, charIndex, bitsPerChar, bitIndex);
            bitIndex = bitIndex + bitsPerChar;
        }

        return compressedBytes;
    }

    public String decompress(byte[] data) {
        if (data[0] != HEADER) {
            throw new IllegalArgumentException("Archivo no válido o no comprimido con BitwiseCompressor");
        }

        int originalLength = ((data[1] & 255) << 8) | (data[2] & 255);

        int bitsPerChar = characterMapper.getBitsPerCharacter();
        StringBuilder decompressedString = new StringBuilder(originalLength);

        int bitIndex = 3 * 8; // Saltamos cabecera + longitud = 24 bits
        for (int i = 0; i < originalLength; i++) {
            int charIndex = readBitsFromByteArray(data, bitsPerChar, bitIndex);
            decompressedString.append(characterMapper.mapToChar(charIndex));
            bitIndex = bitIndex + bitsPerChar;
        }

        return decompressedString.toString();
    }

    private void validateInput(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input no puede ser nulo");
        }
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Input no puede estar vacío");
        } else {
            for (char c : input.toCharArray()) {
                if (!characterMapper.isAllowed(c)) {
                    throw new IllegalArgumentException("Caracter no válido: " + c);
                }
            }
        }
    }

    private void writeBitsToByteArray(byte[] byteArray, int value, int bitCount, int startBitIndex) {
        for (int i = 0; i < bitCount; i++) {
            // Extraer el bit actual de "value"
            int bitValue = (value >> (bitCount - 1 - i)) & 1;

            // Calcular el byte y la posición del bit dentro del byte
            int byteIndex = (startBitIndex + i) / 8;
            int bitPosition = 7 - ((startBitIndex + i) % 8);

            // Establecer el bit correspondiente
            byteArray[byteIndex] = (byte) (byteArray[byteIndex] | (bitValue << bitPosition));
        }
    }

    private int readBitsFromByteArray(byte[] byteArray, int bitCount, int startBitIndex) {
        int result = 0;
        for (int i = 0; i < bitCount; i++) {
            // Calcular el byte y la posición del bit dentro del byte
            int byteIndex = (startBitIndex + i) / 8;
            int bitPosition = 7 - ((startBitIndex + i) % 8);

            // Extraer el bit y agregarlo al resultado
            int bitValue = (byteArray[byteIndex] >> bitPosition) & 1;
            result = (result << 1) | bitValue;
        }

        return result;
    }
}