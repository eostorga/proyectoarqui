/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proy_arqui;

/**
 *
 * @author Iva
 */
public class Estructuras {
    
    private final int ID = 0;
    private final int EST = 1;
    
    private final int B = 0;
    private final int E = 1;
    private final int P1 = 2;
    private final int P2 = 3;
    private final int P3 = 4;
    
    private final int C = 0;
    private final int M = 1;
    private final int I = 2;
    private final int U = 3;
    
    //0-31|32-63|64-95
    // P1 | P2  | P3 
    private int smem[] = new int[96];    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //DIRECTORIOS
    
    // ID|EST|P1|P2|P3
    // 8 BLOQUES
    private int dir1[][] = new int[8][5];
    
    // ID|EST|P1|P2|P3
    // 8 BLOQUES
    private int dir2[][] = new int[8][5];
    
    // ID|EST|P1|P2|P3
    // 8 BLOQUES
    private int dir3[][] = new int[8][5];
    
    //FIN DE LOS DIRECTORIOS
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CACHES
 
    public int dcache1[][] = new int[4][4];     // Cache de datos (4 bloques, cada bloque con 4 palabras, cada palabra 4 bytes)
    public int estCache1[][] = new int[4][2];   // 8bloques*4 = 32 palabras ---> 32palabras*4 = 128 direcciones de palabras
    
    public int dcache2[][] = new int[4][4];     // Cache de datos (4 bloques, cada bloque con 4 palabras, cada palabra 4 bytes)
    public int estCache2[][] = new int[4][2];   // 8bloques*4 = 32 palabras ---> 32palabras*4 = 128 direcciones de palabras
    
    public int dcache3[][] = new int[4][4];     // Cache de datos (4 bloques, cada bloque con 4 palabras, cada palabra 4 bytes)
    public int estCache3[][] = new int[4][2];   // 8bloques*4 = 32 palabras ---> 32palabras*4 = 128 direcciones de palabras

    //FIN DE LAS CACHES
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void Estructuras(){
        //inicializarDir();
        inicializarCaches();
    }
    
    /*
    public void inicializarDir(){
        for(int i = 0; i < 32; i+=4){
            dir[0][i][B] = i;
            dir[1][i][B] = 32+i;
            dir[2][i][B] = 64+i;
        }
    }*/
    
    public void inicializarCaches(){
        for(int i =0; i < 4; ++i){
            setEstBloqueCache(1, i, I);
            setIdBloqueCache(1, i, I);
            setEstBloqueCache(2, i, I);
            setIdBloqueCache(2, i, I);
            setEstBloqueCache(3, i, I);
            setIdBloqueCache(3, i, I);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //SECCION DE SETS Y GETS, NO IMPORTA DE DONDE VENGA LA MEMORIA Y CACHE, LOS CAMBIOS
    //SE HACEN SOLO ACA Y NO EN EL RESTO DEL CODIGO
    public void setPalabraCache(int numCache, int indiceBloque, int indicePalabra, int valor){
        switch(numCache){
            case 1:
                dcache1[indiceBloque][indicePalabra] = valor;
            break;
            case 2:
                dcache2[indiceBloque][indicePalabra] = valor;
            break;
            case 3:
                dcache2[indiceBloque][indicePalabra] = valor;
            break;
        }
    }
    
    public int getPalabraCache(int numCache, int indiceBloque, int indicePalabra){
        int palabra = -1;
        switch(numCache){
            case 1:
                palabra = dcache1[indiceBloque][indicePalabra];
            break;
            case 2:
                palabra = dcache2[indiceBloque][indicePalabra];
            break;
            case 3:
                palabra = dcache2[indiceBloque][indicePalabra];
            break;
        }
        return palabra; 
    }
    
    public void setEstBloqueCache(int numCache, int indiceBloque, int estado){
        switch(numCache){
            case 1:
                estCache1[indiceBloque][EST] = estado;
            break;
            case 2:
                estCache2[indiceBloque][EST] = estado;
            break;
            case 3:
                estCache3[indiceBloque][EST] = estado;
            break;
        }
    }
    
    public int getEstBloqueCache(int numCache, int indiceBloque){
        int estado = -1;
        switch(numCache){
            case 1:
                estado = estCache1[indiceBloque][EST];
            break;
            case 2:
                estado = estCache2[indiceBloque][EST];
            break;
            case 3:
                estado = estCache3[indiceBloque][EST];
            break;
        }
        return estado;
    }
    
    public void setIdBloqueCache(int numCache, int indiceBloque, int id){
        switch(numCache){
            case 1:
                estCache1[indiceBloque][ID] = id;
            break;
            case 2:
                estCache2[indiceBloque][ID] = id;
            break;
            case 3:
                estCache3[indiceBloque][ID] = id;
            break;
        }
    }
    
    public int getIdBloqueCache(int numCache, int indiceBloque){
        int id = -1;
        switch(numCache){
            case 1:
                id = estCache1[indiceBloque][ID];
            break;
            case 2:
                id = estCache2[indiceBloque][ID];
            break;
            case 3:
                id = estCache3[indiceBloque][ID];
            break;
        }
        return id;
    }
    
    public void setPalabraMem(int numCache, int indiceMem, int valor){
        smem[indiceMem] = valor;
    }
    
    public int getPalabraMem(int numCache, int indiceMem){
        return smem [indiceMem];
    }
    //FIN DE LA SECCION DE SETS Y GETS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
}
