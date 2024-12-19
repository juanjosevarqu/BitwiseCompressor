package com.varqulabs.compressorbitwise.negocio;

public class VectorBitsG {

    public int v[]; // vector de int, c/u 32 bits
    int dimension; // dimension imaginaria
    int cantBits; // cantidad bits por elemento

    public VectorBitsG(int cantidadElementos, int nroBitsPorElem) {
        int ent = (cantidadElementos*nroBitsPorElem) / 32;
        if (cantidadElementos % 32 != 0) {
            ent++;
        }

        v = new int[ent];//ent=longitud real del vector
        this.dimension = cantidadElementos;
        this.cantBits = nroBitsPorElem;
    }

    public void insertar(int elemento, int pos) {
        int nbit = obtenernbit(pos);
        int nent = obtenerNent(pos);
        int mask = ((int) Math.pow(2, cantBits)) - 1;
        mask = mask << (nbit - 1);
        mask = ~mask;
        int copiaEle = elemento;
        v[nent] = v[nent] & mask;
        elemento = elemento << (nbit - 1);
        v[nent] = v[nent] | elemento; // insertamos con OR
        if (((nbit - 1) + cantBits) > 32) {
            int mask1 = ((int) Math.pow(2, cantBits) - 1);
            mask1 = mask1 >>> (32 - (nbit - 1));
            mask1 = ~mask1;
            v[nent + 1] = v[nent + 1] & mask1;
            copiaEle = copiaEle >>> (32 - (nbit - 1));
            v[nent + 1] = v[nent + 1] | copiaEle;
        }
    }

    public int Get(int posicion, int cantBits) {
        int ent = obtenerNent(posicion);
        int nbit = obtenernbit(posicion);
        int mask = (((int) Math.pow(2, cantBits)) - 1);
        mask = mask << (nbit - 1);
        mask = mask & v[ent];
        mask = mask >>> (nbit - 1);
        if (((nbit - 1) + cantBits) > 32) {
            int mask1 = ((int) Math.pow(2, cantBits) - 1);
            mask1 = mask1 >>> (32 - (nbit - 1));
            mask1=mask1 & v[ent+1];
            mask1=mask1<<(32-(nbit-1));
            mask=mask | mask1;
        }
        return mask;
    }

    private int obtenernbit(int pos) {
        return ((((pos - 1) * cantBits) % 32) + 1);
    }

    private int obtenerNent(int pos) {
        return (((((pos - 1) * cantBits) / 32)));
    }

    public String toString() {
        String cad = "<< ";
        for (int i = 1; i <= dimension; i++) {
            cad = cad + Get(i, 6) + " , ";
        }
        cad = cad + ">>";
        return cad;
    }

    public int getNroBits(){
        return cantBits;
    }

    public int getDimImaginaria(){
        return dimension;
    }

    public static void main(String[] args) {
        VectorBitsG A=new VectorBitsG(10,7);
        A.insertar(10,1);
        A.insertar(15, 4);
        A.insertar(127, 10);
        System.out.println(A);
    }
}
