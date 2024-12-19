package com.varqulabs.compressorbitwise.domain;

public class BitwiseCompressor {

    int BITS_POR_CARACTER = 6;
    int MARCA_DE_AGUA;
    int LONGITUD_MAXIMA_CADENA = 4093;
    String caracteresValidos = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public BitwiseCompressor() {
        MARCA_DE_AGUA = 61;
    }

    public VectorBitsG comprimir(String cadena) {
        validarEntrada(cadena);

        int longitud = cadena.length();
        if (longitud > LONGITUD_MAXIMA_CADENA) {
            throw new IllegalArgumentException("Longitud de la cadena excede el máximo permitido");
        }

        int totalBits = longitud * BITS_POR_CARACTER + 6 + 12; // 6 bits para la marca de agua y 12 para la longitud
        int totalDeElementos = (totalBits + 5) / 6;

        VectorBitsG vector = new VectorBitsG(totalDeElementos, BITS_POR_CARACTER);

        vector.insertar(MARCA_DE_AGUA, 1);
        int primeraParteLongitud = longitud >> 6; // BIT por caracter
        int segundaParteLongitud = longitud & 63; //
        vector.insertar(primeraParteLongitud, 2);
        vector.insertar(segundaParteLongitud, 3);

        int posicionSiguiente = 4;
        for (int i = 0; i < longitud; i++) {
            char caracter = cadena.charAt(i);
            int indiceCaracter = caracteresValidos.indexOf(caracter);
            if (indiceCaracter == -1) { indiceCaracter = 63; }
            vector.insertar(indiceCaracter, posicionSiguiente);
            posicionSiguiente++;
        }

        return vector;
    }

    public String descomprimir(VectorBitsG vector) {
        validarMarcaDeAgua(vector);

        int longitud = obtenerLongitud(vector);
        validarEstructura(vector, longitud);

        StringBuilder cadenaDescomprimida = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            int indiceCaracter = vector.Get(3 + i);
            char caracter = caracteresValidos.charAt(indiceCaracter);
            cadenaDescomprimida.append(caracter);
        }

        return cadenaDescomprimida.toString();
    }

    private void validarMarcaDeAgua(VectorBitsG vector) {
        int marcaDeAgua = vector.Get(1);
        if (marcaDeAgua != MARCA_DE_AGUA) {
            throw new IllegalArgumentException("Archivo no válido: marca de agua no encontrada, Este archivo no fue comprimido por este compresor o por nosotros");
        }
    }

    private int obtenerLongitud(VectorBitsG vector) {
        int primeraParte = vector.Get(2);
        int segundaParte = vector.Get(3);
        int longitud = (primeraParte << 6) | segundaParte;

        if (longitud > LONGITUD_MAXIMA_CADENA) {
            throw new IllegalArgumentException("Longitud de la cadena del Vector excede el máximo permitido");
        }
        return longitud;
    }

    private void validarEntrada(String cadena) {
        if (cadena == null || cadena.isEmpty()) {
            throw new IllegalArgumentException("La cadena de entrada no puede ser nula o vacía");
        } else {
            for (char c : cadena.toCharArray()) {
                if (caracteresValidos.indexOf(c) == -1) {
                    throw new IllegalArgumentException("Caracter no válido: " + c);
                }
            }
        }
    }

    private void validarEstructura(VectorBitsG vector, int longitud) {
        int totalBits = longitud * BITS_POR_CARACTER + 6 + 12;
        int totalElementosRequeridos = (totalBits + 5) / 6;
        if (vector.getDimImaginaria() < totalElementosRequeridos) {
            throw new IllegalArgumentException("El vector comprimido no tiene suficientes elementos para descomprimir");
        }
    }
}