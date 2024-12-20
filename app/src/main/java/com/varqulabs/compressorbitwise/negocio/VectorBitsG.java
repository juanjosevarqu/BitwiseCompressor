package com.varqulabs.compressorbitwise.negocio;

public class VectorBitsG {

    private final byte[] arregloBytes; // 8 bits en vez de 32 bits
    private final int cantidadElementos;
    private final int bitsPorElemento;

    public VectorBitsG(int cantidadElementos, int bitsPorElemento) {
        this.bitsPorElemento = bitsPorElemento;
        this.cantidadElementos = cantidadElementos;
        int totalBits = cantidadElementos * bitsPorElemento;
        int totalBytes = (totalBits + 7) / 8;
        this.arregloBytes = new byte[totalBytes];
    }

    public void insertar(int valor, int posicion) {
        if (valor >= (1 << bitsPorElemento)) {
            throw new IllegalArgumentException("El valor excede el límite permitido");
        }
        if (posicion < 1 || posicion > cantidadElementos) {
            throw new IllegalArgumentException("La posición está fuera del rango");
        }

        int bitInicial = (posicion - 1) * bitsPorElemento;
        int byteInicial = bitInicial / 8;
        int desplazamiento = bitInicial % 8;
        int bitsRestantes = bitsPorElemento;

        while (bitsRestantes > 0) {
            int bitsDisponibles = 8 - desplazamiento;
            int bitsAEscribir = Math.min(bitsDisponibles, bitsRestantes);
            int mascara = (1 << bitsAEscribir) - 1;
            int bitsLimpios = ~(mascara << desplazamiento) & 255;
            arregloBytes[byteInicial] = (byte) ((arregloBytes[byteInicial] & bitsLimpios) | ((valor & mascara) << desplazamiento));
            bitsRestantes = bitsRestantes - bitsAEscribir;
            valor = valor >>> bitsAEscribir;
            byteInicial++;
            desplazamiento = 0;
        }
    }

    public int Get(int posicion, int bitsPorElemento) {
        if (posicion < 1 || posicion > cantidadElementos) {
            throw new IllegalArgumentException("La posición está fuera del rango");
        }

        int bitInicial = (posicion - 1) * bitsPorElemento;
        int byteInicial = bitInicial / 8;
        int desplazamiento = bitInicial % 8;
        int bitsRestantes = bitsPorElemento;
        int resultado = 0;
        int bitsLeidos = 0;

        while (bitsRestantes > 0) {
            int bitsDisponibles = 8 - desplazamiento;
            int bitsALeer = Math.min(bitsDisponibles, bitsRestantes);
            int mascara = (1 << bitsALeer) - 1;
            resultado = resultado | (((arregloBytes[byteInicial] >>> desplazamiento) & mascara) << bitsLeidos);
            bitsRestantes = bitsRestantes - bitsALeer;
            bitsLeidos = bitsLeidos + bitsALeer;
            byteInicial++;
            desplazamiento = 0;
        }

        return resultado;
    }

    public int obtenerCantidadElementos() {
        return cantidadElementos;
    }

    public int obtenerBitsPorElemento() {
        return bitsPorElemento;
    }

    public byte[] obtenerArregloBytes() {
        return arregloBytes.clone();
    }

    public void actualizarDesdeByteArray(byte[] datos) {
        if (datos.length != arregloBytes.length) {
            throw new IllegalArgumentException("El tamaño del arreglo no coincide con el vector");
        }
        System.arraycopy(datos, 0, arregloBytes, 0, arregloBytes.length);
    }

    @Override
    public String toString() {
        StringBuilder cadena = new StringBuilder("[ ");
        for (int i = 1; i <= cantidadElementos; i++) {
            cadena.append(Get(i, 6)).append(", ");
        }
        if (cantidadElementos > 0) {
            cadena.setLength(cadena.length() - 2);
        }
        cadena.append(" ]");
        return cadena.toString();
    }

    public static void main(String[] args) {
        VectorBitsG vector = new VectorBitsG(7, 6);
        vector.insertar(10, 1);
        vector.insertar(15, 2);
        vector.insertar(25, 3);
        vector.insertar(40, 4);
        vector.insertar(50, 5);
        vector.insertar(63, 6);
        vector.insertar(1, 7);
        System.out.println("Vector comprimido: " + vector);
    }
}
