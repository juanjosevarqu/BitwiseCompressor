package com.varqulabs.compressorbitwise.negocio;

public class CompresorBitwise {

    public ValidadorDeCaracteres validador;

    byte MARCA_DE_AGUA;
    int LONGITUD_MAXIMA_CADENA;
    byte BITS_POR_CARACTER;
    //Indica cuántos bits necesita cada carácter en la lista permitida.
    //Esto depende del validador. Por ejemplo, si hay 4 caracteres permitidos, se necesitan 2 bits para representarlos.

    public CompresorBitwise(ValidadorDeCaracteres validadorDeCaracteres) {
        this.validador = validadorDeCaracteres;
        this.MARCA_DE_AGUA = 61;
        this.LONGITUD_MAXIMA_CADENA = 4093;
        this.BITS_POR_CARACTER = validador.getBitsPorCaracter();
    }

    public CompresorBitwise(ValidadorDeCaracteres validadorDeCaracteres, byte marcaDeAgua) {
        this.validador = validadorDeCaracteres;
        this.MARCA_DE_AGUA = marcaDeAgua;
        this.LONGITUD_MAXIMA_CADENA = 4093;
        this.BITS_POR_CARACTER = validador.getBitsPorCaracter();
    }

    public CompresorBitwise(ValidadorDeCaracteres validadorDeCaracteres, int longitudMaximaCadena, byte marcaDeAgua) {
        this.validador = validadorDeCaracteres;
        this.MARCA_DE_AGUA = marcaDeAgua;
        this.BITS_POR_CARACTER = validador.getBitsPorCaracter();
        this.LONGITUD_MAXIMA_CADENA = longitudMaximaCadena;
    }

    public VectorBitsG comprimir(String cadena) {
        validarEntrada(cadena);
        //No tiene caracteres fuera de los permitidos.
        //No es más larga que el máximo permitido.

        int longitud = cadena.length();
        if (longitud > LONGITUD_MAXIMA_CADENA) {
            throw new IllegalArgumentException("Longitud de la cadena excede el máximo permitido");
        }

        int totalBits = longitud * BITS_POR_CARACTER + 6 + 12; // 6 bits para la marca de agua y 12 para la longitud
        int totalDeElementos = (totalBits + 5) / 6;

        VectorBitsG vector = new VectorBitsG(totalDeElementos, BITS_POR_CARACTER);

        vector.insertar(MARCA_DE_AGUA, 1);
        int primeraParteLongitud = longitud >> 6;
        int segundaParteLongitud = longitud & 63;
        vector.insertar(primeraParteLongitud, 2);
        vector.insertar(segundaParteLongitud, 3);

        int posicionSiguiente = 4;
        for (int i = 0; i < longitud; i++) {
            char caracter = cadena.charAt(i);
            int indiceCaracter = validador.obtenerIndice(caracter);
            vector.insertar(indiceCaracter, posicionSiguiente);
            posicionSiguiente++;
        }

        return vector;
    }

    private void validarEntrada(String cadena) {
        if (cadena == null || cadena.isEmpty()) {
            throw new IllegalArgumentException("La cadena de entrada no puede ser nula o vacía");
        } else {
            for (char caracter : cadena.toCharArray()) {
                if (!validador.esValido(caracter)) {
                    throw new IllegalArgumentException("Caracter no válido: " + caracter);
                }
            }
        }
    }

    public String descomprimir(VectorBitsG vector) {
        validarMarcaDeAgua(vector);

        int longitud = obtenerLongitud(vector);
        validarEstructura(vector, longitud);

        StringBuilder cadenaDescomprimida = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            int indiceCaracter = vector.Get(4 + i, BITS_POR_CARACTER);
            char caracter = validador.obtenerCaracter(indiceCaracter);
            cadenaDescomprimida.append(caracter);
        }

        return cadenaDescomprimida.toString();
    }

    private void validarMarcaDeAgua(VectorBitsG vector) {
        int marcaDeAgua = vector.Get(1, BITS_POR_CARACTER);
        if (marcaDeAgua != MARCA_DE_AGUA) {
            throw new IllegalArgumentException("Archivo no válido: marca de agua no encontrada, Este archivo no fue comprimido por este compresor o por nosotros");
        }
    }

    private int obtenerLongitud(VectorBitsG vector) {
        int primeraParte = vector.Get(2, BITS_POR_CARACTER);
        int segundaParte = vector.Get(3, BITS_POR_CARACTER);
        int longitud = (primeraParte << 6) | segundaParte;

        if (longitud > LONGITUD_MAXIMA_CADENA) {
            throw new IllegalArgumentException("Longitud de la cadena del Vector excede el máximo permitido");
        }
        return longitud;
    }

    private void validarEstructura(VectorBitsG vector, int longitud) {
        int totalBits = longitud * BITS_POR_CARACTER + 6 + 12;
        int totalElementosRequeridos = (totalBits + 5) / 6;
        if (vector.getDimImaginaria() < totalElementosRequeridos) {
            throw new IllegalArgumentException("El vector comprimido no tiene suficientes elementos para descomprimir");
        }
    }
}