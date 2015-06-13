package proy_arqui;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Procesador extends Thread
{
    public int deboDeregistrarme = 0;
    private static Multiprocesador myMp;
    private final Estructuras estr;
    private int myNumP;
    private int pcA;
    private int limit;
    private boolean puedoSeguir = true;
    private boolean final_programa = false;
    //private int ciclo = 0;
    private int stop = 0;
    private int cont = 0;
    private int destruir = 0;

    // COLUMNAS DE ESTADO EN CACHE Y DIRECTORIOS
    private final int ID = 0;
    private final int EST = 1;
    private final int E = 1;
    ////////////////////////////////////////////////////////////////////////////
    
    // ESTADOS DE BLOQUES
    private final int C = 0;
    private final int M = 1;
    private final int I = 2;
    private final int U = 3;
    ////////////////////////////////////////////////////////////////////////////
    
    private int PC;                     // Contador de programa
    private int IR;                     // Registro de instruccion
    private int regs[] = new int[32];   // 32 registros
   
    // CONSTRUCTOR
    public Procesador(int numP, Multiprocesador mp, Estructuras es)
    {
        myMp = mp;
        estr = es;
        myNumP = numP;
        
        for (int i = 0; i < 4; ++i)
        {
            setEstBloqueCache(i, I);
            setIdBloqueCache(i, -1);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // SECCIÓN DE SETS Y GETS, NO IMPORTA DE DONDE VENGA LA MEMORIA Y CACHE,
    // LOS CAMBIOS SE HACEN SOLO ACÁ Y NO EN EL RESTO DEL CÓDIGO
    public void setPalabraCache(int indiceBloque, int indicePalabra, int valor)
    {
        estr.setPalabraCache(myNumP, indiceBloque, indicePalabra, valor);
    }

    public int getPalabraCache(int indiceBloque, int indicePalabra)
    {
        return estr.getPalabraCache(myNumP, indiceBloque, indicePalabra);
    }

    private void setEstBloqueCache(int indiceBloque, int estado)
    {
        estr.setEstBloqueCache(myNumP, indiceBloque, estado);
    }

    public int getEstBloqueCache(int indiceBloque)
    {
        return estr.getEstBloqueCache(myNumP, indiceBloque);
    }

    private void setIdBloqueCache(int indiceBloque, int id)
    {
        estr.setIdBloqueCache(myNumP, indiceBloque, id);
    }

    public int getIdBloqueCache(int indiceBloque)
    {
        return estr.getIdBloqueCache(myNumP, indiceBloque);
    }

    public void setPalabraMem(int indiceMem, int valor)
    {
        estr.setPalabraMem(myNumP, indiceMem, valor);
    }

    public int getPalabraMem(int indiceMem)
    {
        return estr.getPalabraMem(myNumP, indiceMem);
    }
    
    // CAMBIA EL ESTADO DE UN BLOQUE EN EL DIRECTORIO
    // RECIBE: NUM DEL DIRECTORIO, ID DE BLOQUE EN MEMORIA, NUEVO ESTADO
    public void setEstDir(int numDir, int idBloque, int nuevoEstado)
    {
        int indiceDir = -1;
        if (idBloque >= 0 && idBloque <= 31)
        {
            indiceDir = idBloque / 4;
        }
        if (idBloque >= 32 && idBloque <= 63)
        {
            indiceDir = (idBloque - 32) / 4;
        }
        if (idBloque >= 64 && idBloque <= 95)
        {
            indiceDir = (idBloque - 64) / 4;
        }
        estr.setEntradaDir(numDir, indiceDir, E, nuevoEstado);
    }

    // RECIBE EL ESTADO DE UN BLOQUE EN EL DIRECTORIO
    public int getEstDir(int idBloque)
    {
        return estr.getEstadoBloqueDir(idBloque);
    }

    //FIN DE LA SECCION DE SETS Y GETS
    ////////////////////////////////////////////////////////////////////////////

    // COPIA EL BLOQUE ENTERO EN EL LUGAR QUE LE CORRESPONDE EN CACHÉ 
    public void cargarACache(int direccionMemoria, int direccionCache)
    {
        int j = direccionMemoria;
        
        for (int i = 0; i < 4; i++)
        {
            setPalabraCache(direccionCache, i, getPalabraMem(j));
            j++;
        }

        for (int i = 0; i < 16; i++)
        {
            puedoSeguir = false;
            try
            {
                myMp.phaser.arriveAndAwaitAdvance();
                //myMp.barrera.await();
                //ciclo++;
                myMp.ciclo++;
                cont++;
                //System.out.println("Ciclo #"+ciclo+". No puede cambiar de instrucción.");
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        puedoSeguir = true;
        //System.out.println("Ya puede cambiar de instrucción.");
    }

    // GUARDA EL BLOQUE ENTERO DESDE CACHÉ HASTA SU POSICIÓN EN MEMORIA
    public void guardarEnMemoria(int direccionMemoria, int direccionCache)
    {
        int j = direccionMemoria;
        for (int i = 0; i < 4; i++)
        {
            setPalabraMem(j, getPalabraCache(direccionCache, i));
            j++;
        }

        for (int i = 0; i < 16; i++)
        {
            puedoSeguir = false;
            try
            {
                myMp.phaser.arriveAndAwaitAdvance();
                //myMp.barrera.await();
                //ciclo++;
                myMp.ciclo++;
                cont++;
                //System.out.println("Ciclo #"+ciclo+". No puede cambiar de instrucción.");
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        puedoSeguir = true;
        //System.out.println("Ya puede cambiar de instrucción.");
    }

    public void pedirParaLeer(int idBloque, int direccionMemoria, int direccionCache) {
        int papa = estr.directorioPapa(idBloque); //1, 2, 3
        int estado = estr.getEstadoBloqueDir(idBloque); // 'C', 'M', 'U'
        //int duenoBloque = estr.consultarDuenoBloqueDir(myNumP,idBloque);
        switch (estado) {
            case C:
                //estr.cargarACache(int numCache, int direccionMemoria, int direccionCache);
                cargarACache(direccionMemoria, direccionCache);
                estr.anadirProcesador(idBloque, myNumP);
                // cargo el bloque en mi cache desde memoria
                // agregarme a la lista de compartidos
                break;
            case M:
                int idDueno = estr.consultarDuenoBloqueDir(idBloque);
                estr.guardarEnMemoria(idDueno, direccionMemoria, direccionCache);    // hasta aqui llegamos 
                estr.setEntradaDir(idDueno, (direccionMemoria / 4), 1, C);
                // subir lo de la cache dueña en memoria
                // poner el directorio como compartido
                // bajar lo de memoria a cache
                // agregar en la lista de compartidos
                break;
            case U:

                break;
        }
    }
    
    // Leer una palabra
    // LW RX, n(RY)
    // Rx  M(n + (Ry))
    // Y X n
    public void LW(int Y, int X, int n)
    {
        int numByte = regs[Y] + n;                                  // Numero del byte que quiero leer de memoria 
        int numBloqMem = Math.floorDiv(numByte, 16);                // Indice del bloque en memoria (0-24)
        int numPalabra = (numByte % 16) / 4;
        int dirBloqCache = numBloqMem % 4;                          // Indice donde debe estar el bloque en cache
        int idBloqEnCache = getIdBloqueCache(dirBloqCache);         // ID del bloque que ocupa actualmente esa direccion en cache
        int estadoBloqEnCache = getEstBloqueCache(dirBloqCache);    // Estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        int dirNumBloqMem = numBloqMem * 4;                         // Conversion para mapear la direccion inicial del bloque en memoria

        // BLOQUEO MI CACHÉ
        estr.waitC(myNumP);
        //System.out.println(getEstDir(numBloqMem));
        System.out.println("INICIA LW");
        //CASO 1: HAY OTRO BLOQUE DIFERENTE PERO VÁLIDO
        if(idBloqEnCache != dirNumBloqMem && idBloqEnCache != -1)
        {
            switch(estadoBloqEnCache)
            {
                case C:
                    System.out.println("HAY OTRO BLOQUE -> ESTÁ COMPARTIDO");
                    // Para saber si el directorio que voy a utilizar esta ocupado o no
                    if (estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0)
                    {
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        // USA DIRECTORIO EN EL SIGUIENTE CICLO //                        
                        estr.quitarProcesador(idBloqEnCache, myNumP);

                        estr.verificarUncached(idBloqEnCache); //por si solo uno lo tiene compartido, q se ponga 'U'
                        estr.signalD(estr.directorioPapa(idBloqEnCache));

                        estr.verificarUncached(idBloqEnCache);
                        estr.signalD(estr.directorioPapa(numBloqMem));

                        estr.verificarUncached(idBloqEnCache); //por si solo uno lo tiene compartido, q se ponga 'U'
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);
                    }
                break;
                case M:
                    System.out.println("HAY OTRO BLOQUE -> ESTÁ MODIFICADO");
                    if (estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0)
                    {
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));                        
                        // USA DIRECTORIO EN EL SIGUIENTE CICLO //                        
                        guardarEnMemoria(idBloqEnCache, dirBloqCache);

                        estr.quitarProcesador(idBloqEnCache, myNumP);  
                        estr.verificarUncached(idBloqEnCache); //por si solo uno lo tiene compartido, q se ponga 'U'
                           
                        //setEstDir(estr.directorioPapa(idBloqEnCache), numBloqMem, U); //pero tengo q poner para quienes esta C
                        //estr.quitarProcesador(idBloqEnCache, myNumP);                        
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                        
                        setEstDir(estr.directorioPapa(idBloqEnCache), numBloqMem, U); //pero tengo q poner para quienes esta C
                        estr.quitarProcesador(idBloqEnCache, myNumP);                        
                        estr.signalD(estr.directorioPapa(numBloqMem));

                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);
                    }
                break;
                case I:
                    System.out.println("HAY OTRO BLOQUE -> ESTÁ INVÁLIDO");
                    setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                    setEstBloqueCache(dirBloqCache, I);
                break;
            }
        }
        
        //CASO 2: ESTÁ EL BLOQUE BLOQUE (HIT) O NO HAY NINGUNO
        if(idBloqEnCache == dirNumBloqMem || idBloqEnCache == -1)
        {
            System.out.println("ESTÁ EL BLOQUE O NO HAY NINGUNO");
            switch(estadoBloqEnCache)
            {
                case C:
                    System.out.println("ESTÁ EL BLOQUE -> COMPARTIDO");
                    // Carga la palabra que se ocupa al registro
                    regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                    estr.signalC(myNumP);
                break;
                case M:
                    System.out.println("ESTÁ EL BLOQUE -> MODIFICADO");
                    if (estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0)
                    {
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache);
                        setEstDir(estr.directorioPapa(idBloqEnCache), numBloqMem, C);
                        estr.anadirProcesador(idBloqEnCache, myNumP);       

                        regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                        estr.signalC(myNumP);
                        estr.signalD(estr.directorioPapa(numBloqMem));
                    }
                break;
                case I:
                    System.out.println("ESTÁ EL BLOQUE -> INVÁLIDO");
                    if (estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0)
                    {
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        System.out.println(dirBloqCache);
                        if (estr.getEstadoBloqueDir(dirBloqCache) == C)
                        {
                            System.out.println("ESTADO DIRECTORIO -> COMPARTIDO");
                            cargarACache(dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);
                            estr.anadirProcesador(idBloqEnCache, myNumP);

                            regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                            estr.signalD(estr.directorioPapa(numBloqMem));
                            estr.signalC(myNumP);
                        } else if (estr.getEstadoBloqueDir(dirBloqCache) == M)
                        {
                            System.out.println("ESTADO DIRECTORIO -> MODIFICADO");
                            int cacheDuena = estr.consultarDuenoBloqueDir(numBloqMem);
                            if (estr.disponibleC(cacheDuena) == 0)
                            {
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                                LW(Y, X, n);
                            } else
                            {
                                estr.waitC(cacheDuena);  //si la logro agarrar
                                estr.guardarEnMemoria(cacheDuena, numBloqMem, dirBloqCache);
                                estr.setEstBloqueCache(cacheDuena, numBloqMem, C);
                                estr.signalC(cacheDuena);
                                setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C);
                                //Pero tengo que poner para quiénes esta C
                                cargarACache(dirNumBloqMem, dirBloqCache);
                                setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                                setEstBloqueCache(dirBloqCache, C);
                                estr.anadirProcesador(numBloqMem, myNumP);
                                // Carga la palabra que se ocupa al registro
                                regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                            }
                        } else if (estr.getEstadoBloqueDir(dirBloqCache) == U)
                        {
                            System.out.println("ESTADO DIRECTORIO -> UNCACHED");
                            estr.cargarACache(myNumP, dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);
                            setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C);
                            estr.anadirProcesador(numBloqMem, myNumP);
                            // Carga la palabra que se ocupa al registro
                            regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(numBloqMem));
                        }
                    }
                break;
            }   
        } 
    }

    /*// LEER UNA PALABRA
    public void LW(int Y, int X, int n)
    {
        int numByte = regs[Y] + n;                                  // Numero del byte que quiero leer de memoria 
        int numBloqMem = Math.floorDiv(numByte, 16);                // Indice del bloque en memoria (0-24)
        int numPalabra = (numByte % 16) / 4;
        int dirBloqCache = numBloqMem % 4;                          // Indice donde debe estar el bloque en cache
        int idBloqEnCache = getIdBloqueCache(dirBloqCache);         // ID del bloque que ocupa actualmente esa direccion en cache
        int estadoBloqEnCache = getEstBloqueCache(dirBloqCache);    // Estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        int dirNumBloqMem = numBloqMem * 4;                         // Conversion para mapear la direccion inicial del bloque en memoria

        // BLOQUEO MI CACHÉ
        estr.waitC(myNumP);
        
        //CASO 1: HAY OTRO BLOQUE DIFERENTE PERO VÁLIDO
        if(idBloqEnCache != dirNumBloqMem && idBloqEnCache != -1)
        {
            switch(estadoBloqEnCache){
                case C:
                    if (estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0) { // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else {
                        estr.waitD(estr.directorioPapa(idBloqEnCache));
                        estr.quitarProcesador(idBloqEnCache, myNumP);
                        estr.verificarUncached(idBloqEnCache);
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                        
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);                      
                    }
                break;
                case M:
                    if (estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0) { // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache);
                        setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, U); //pero tengo q poner para quienes esta C
                        estr.quitarProcesador(numBloqMem, myNumP);                        
                        estr.signalD(estr.directorioPapa(numBloqMem));
                        
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);
                    }
                break;
                case I:
                    setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                    setEstBloqueCache(dirBloqCache, I);
                break;
            }
        }
        
        //CASO 2: ESTÁ EL BLOQUE BLOQUE (HIT) O NO HAY NINGUNO
        if(idBloqEnCache == dirNumBloqMem || idBloqEnCache == -1)
        {
            switch(estadoBloqEnCache){
                case C:
                    regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                    estr.signalC(myNumP);
                    break;
                case M:
                    if (estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0) { // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache);
                        setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C); //pero tengo q poner para quienes esta C
                        estr.anadirProcesador(numBloqMem, myNumP);
                        regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                        estr.signalC(myNumP);
                        estr.signalD(estr.directorioPapa(numBloqMem));
                    }
                    break;
                case I:
                    if (estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0) { // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        if (estr.getEstadoBloqueDir(numBloqMem) == C) {
                            cargarACache(dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);
                            estr.anadirProcesador(numBloqMem, myNumP);
                            regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(numBloqMem));
                        } else if (estr.getEstadoBloqueDir(numBloqMem) == M) {
                            int cacheDuena = estr.consultarDuenoBloqueDir(numBloqMem);
                            if (estr.disponibleC(cacheDuena) == 0) {
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                                LW(Y, X, n);
                            } else {
                                estr.waitC(cacheDuena);  //si la logro agarrar
                                estr.guardarEnMemoria(cacheDuena, numBloqMem, dirBloqCache);
                                estr.setEstBloqueCache(cacheDuena, numBloqMem, C);
                                estr.signalC(cacheDuena);
                                setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C); //pero tengo q poner para quienes esta C
                                cargarACache(dirNumBloqMem, dirBloqCache);
                                setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                                setEstBloqueCache(dirBloqCache, C);
                                estr.anadirProcesador(numBloqMem, myNumP);
                                regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                            }
                        } else { // el caso en que este uncached y aqui terminamos antes de que iva lo haga mas complicado el trabajo
                            estr.cargarACache(myNumP, dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);
                            setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C); //pero tengo q poner para quienes esta C
                            estr.anadirProcesador(numBloqMem, myNumP);
                            regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(numBloqMem));
                        }
                    }
                break;
            }   
        } 
    }*/    
    
    // Escribir una palabra
    // SW RX, n(RY)
    // M(n + (Ry))  Rx
    // Y X n
    public void SW(int Y, int X, int n)
    {
        int numByte = regs[Y] + n;                                  // Numero del byte que quiero leer de memoria 
        int numBloqMem = Math.floorDiv(numByte, 16);                // Indice del bloque en memoria (0-24)
        int numPalabra = (numByte % 16) / 4;
        int dirBloqCache = numBloqMem % 4;    
        int idBloqEnCache = getIdBloqueCache(dirBloqCache);         // ID del bloque que ocupa actualmente esa direccion en cache
        int estadoBloqEnCache = getEstBloqueCache(dirBloqCache);    // Estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        int dirNumBloqMem = numBloqMem * 4;

        estr.waitC(myNumP);
        
        // CASO 1: HAY OTRO BLOQUE VÁLIDO
        if(idBloqEnCache != dirNumBloqMem && idBloqEnCache != -1)
        {
            switch(estadoBloqEnCache)
            {
                case C:
                    if (estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0)
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    }else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        estr.quitarProcesador(idBloqEnCache, myNumP);
                        estr.verificarUncached(idBloqEnCache);
                        estr.signalD(estr.directorioPapa(numBloqMem));
                        
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);                      
                    }
                break;
                case M:
                    if(estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0)
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    }else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache);
                        setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, U); //pero tengo q poner para quienes esta C
                        estr.quitarProcesador(numBloqMem, myNumP);                        
                        estr.signalD(estr.directorioPapa(numBloqMem));
                        
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);
                    }
                break;
                case I:
                    setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                    setEstBloqueCache(dirBloqCache, I);
                break;
            }
        }
        
        //CASO 2: ESTÁ EL BLOQUE BLOQUE (HIT) O NO HAY NINGUNO
        if(idBloqEnCache == dirNumBloqMem || idBloqEnCache == -1)
        {   System.out.println("NO HAY NINGUN BLOQUE");
            switch(estadoBloqEnCache){
                case C:
                    if(estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0) // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        estr.quitarCompartidos(numBloqMem, myNumP);
                        estr.anadirProcesador(numBloqMem, myNumP);
                        setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, M);
                        estr.signalD(estr.directorioPapa(numBloqMem));
                        
                        // Carga la palabra que se ocupa al registro
                        setPalabraCache(dirBloqCache, numPalabra, regs[X]);
                        setEstBloqueCache(dirBloqCache, M);  
                        
                        estr.signalC(myNumP);
                        
                        // Quitar compartidos
                        // Envío el estado del directorio que estaba modificado
                        // libero el directorio
                        // guardo a memoria
                        // me pongo como modificado y me modifico
                        // me libero
                    }
                    break;
                case M:
                    guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache);
                    
                    // Carga la palabra que se ocupa al registro 
                    setPalabraCache(dirBloqCache, numPalabra, regs[X]);                       
                    estr.signalC(myNumP);
                    break;
                case I:  // aqui quedamos //////////////////////////////////////
                    System.out.println("EL BLOQUE ESTÁ INVÁLIDO");
                    if(estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0)
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    } else
                    {
                        System.out.println("AGARRÉ EL DIRECTORIO");
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        if (estr.getEstadoBloqueDir(numBloqMem) == C)
                        {   
                            System.out.println("ESTÁ COMPARTIDO");
                            cargarACache(dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);
                            estr.anadirProcesador(numBloqMem, myNumP);
                            
                            // Carga la palabra que se ocupa al registro
                            setPalabraCache(dirBloqCache, numPalabra, regs[X]);
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(numBloqMem));
                        } else if(estr.getEstadoBloqueDir(numBloqMem) == M)
                        {   System.out.println("MODIFICADO");
                            int cacheDuena = estr.consultarDuenoBloqueDir(numBloqMem);
                            if (estr.disponibleC(cacheDuena) == 0)
                            {
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                                SW(Y, X, n);
                            } else
                            {
                                estr.waitC(cacheDuena);  //si la logro agarrar
                                estr.guardarEnMemoria(cacheDuena, numBloqMem, dirBloqCache);
                                estr.setEstBloqueCache(cacheDuena, numBloqMem, C);
                                estr.signalC(cacheDuena);
                                setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C);
                                // Pero tengo q poner para quienes esta C
                                cargarACache(dirNumBloqMem, dirBloqCache);
                                setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                                setEstBloqueCache(dirBloqCache, C);
                                estr.anadirProcesador(numBloqMem, myNumP);
                                // Carga la palabra que se ocupa al registro
                                setPalabraCache(dirBloqCache, numPalabra, regs[X]);
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                            }
                        } else if(estr.getEstadoBloqueDir(numBloqMem) == U)
                        {
                            System.out.println("ESTÁ UNCACHED");
                            // El caso en que este uncached y aqui terminamos antes de que iva lo haga mas complicado el trabajo
                            estr.cargarACache(myNumP, dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);
                            setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C);
                            // Pero tengo q poner para quienes esta C
                            estr.anadirProcesador(numBloqMem, myNumP);
                            // Carga la palabra que se ocupa al registro
                            setPalabraCache(dirBloqCache, numPalabra, regs[X]);
                            //regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(numBloqMem));
                        }
                    }
                break;
            }
        }
    }

    /*public void SW(int Y, int X, int n)
    {
        int numByte = regs[Y] + n;                                  // Numero del byte que quiero leer de memoria 
        int numBloqMem = Math.floorDiv(numByte, 16);                // Indice del bloque en memoria (0-24)
        int numPalabra = (numByte % 16) / 4;
        int dirBloqCache = numBloqMem % 4;    
        estr.waitC(myNumP);                                         // Bloque mi cache ya que la voy a usar// Indice donde debe estar el bloque en cache
        int idBloqEnCache = getIdBloqueCache(dirBloqCache);         // ID del bloque que ocupa actualmente esa direccion en cache
        int estadoBloqEnCache = getEstBloqueCache(dirBloqCache);    // Estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        int dirNumBloqMem = numBloqMem * 4;

        // CASO 1: HAY OTRO BLOQUE VÁLIDO
        if(idBloqEnCache != dirNumBloqMem && idBloqEnCache != -1)
        {
            switch(estadoBloqEnCache)
            {
                case C:
                    if (estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0)
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    }else
                    {
                        estr.waitD(estr.directorioPapa(idBloqEnCache));
                        estr.quitarProcesador(idBloqEnCache, myNumP);
                        estr.verificarUncached(idBloqEnCache);
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                        
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);                      
                    }
                break;
                case M:
                    if(estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0)
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    }else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache);
                        setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, U); //pero tengo q poner para quienes esta C
                        estr.quitarProcesador(numBloqMem, myNumP);                        
                        estr.signalD(estr.directorioPapa(numBloqMem));
                        
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);
                    }
                break;
                case I:
                    setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                    setEstBloqueCache(dirBloqCache, I);
                break;
            }
        }
        
        //CASO 2: ESTÁ EL BLOQUE BLOQUE (HIT) O NO HAY NINGUNO
        if(idBloqEnCache == dirNumBloqMem || idBloqEnCache == -1)
        {
            switch(estadoBloqEnCache){
                case C:
                    if(estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0) // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        estr.quitarCompartidos(numBloqMem, myNumP);
                        estr.anadirProcesador(numBloqMem, myNumP);
                        setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, M);
                        estr.signalD(estr.directorioPapa(numBloqMem));
                        
                        setPalabraCache(dirBloqCache, numPalabra, regs[X]);     // Carga la palabra que se ocupa al registro 
                        setEstBloqueCache(dirBloqCache, M);  
                        
                        estr.signalC(myNumP);
                        
                        // Quitar compartidos
                        // Envío el estado del directorio que estaba modificado
                        // libero el directorio
                        // guardo a memoria
                        // me pongo como modificado y me modifico
                        // me libero
                    }
                    break;
                case M:
                    guardarEnMemoria(getIdBloqueCache(dirBloqCache), dirBloqCache);
                    setPalabraCache(dirBloqCache, numPalabra, regs[X]);     // Carga la palabra que se ocupa al registro                        
                    estr.signalC(myNumP);
                    break;
                case I:  // aqui quedamos ////////////////////////////////////////////////////////////////////////////////////
                    if(estr.disponibleD(estr.directorioPapa(numBloqMem)) == 0) // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(numBloqMem));
                        if (estr.getEstadoBloqueDir(numBloqMem) == C)
                        {
                            cargarACache(dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);
                            estr.anadirProcesador(numBloqMem, myNumP);
                            regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(numBloqMem));
                        } else if(estr.getEstadoBloqueDir(numBloqMem) == M)
                        {
                            int cacheDuena = estr.consultarDuenoBloqueDir(numBloqMem);
                            if (estr.disponibleC(cacheDuena) == 0)
                            {
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                                SW(Y, X, n);
                            } else
                            {
                                estr.waitC(cacheDuena);  //si la logro agarrar
                                estr.guardarEnMemoria(cacheDuena, numBloqMem, dirBloqCache);
                                estr.setEstBloqueCache(cacheDuena, numBloqMem, C);
                                estr.signalC(cacheDuena);
                                setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C); //pero tengo q poner para quienes esta C
                                cargarACache(dirNumBloqMem, dirBloqCache);
                                setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                                setEstBloqueCache(dirBloqCache, C);
                                estr.anadirProcesador(numBloqMem, myNumP);
                                regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(numBloqMem));
                            }
                        } else { // el caso en que este uncached y aqui terminamos antes de que iva lo haga mas complicado el trabajo
                            estr.cargarACache(myNumP, dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);
                            setEstDir(estr.directorioPapa(numBloqMem), numBloqMem, C); //pero tengo q poner para quienes esta C
                            estr.anadirProcesador(numBloqMem, myNumP);
                            regs[X] = getPalabraCache(dirBloqCache, numPalabra);     // Carga la palabra que se ocupa al registro
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(numBloqMem));
                        }
                    }
                break;
            }   
        }
    }*/    
    
    //RX, ETIQ
    //Si Rx = 0 SALTA
    //X 0 n
    public void BEQZ(int X, int n)
    {
        if (regs[X] == 0)
        {
            if (n >= 0)
            {
                PC += 4 * (n - 1);
            } else
            {
                PC += 4 * (n);
            }
        }
    }

    //RX, ETIQ
    //Si Rx != 0 SALTA
    //X 0 n
    public void BNEZ(int X, int n)
    {
        if (regs[X] != 0)
        {
            if (n >= 0)
            {
                PC += 4 * (n - 1);
            } else
            {
                PC += 4 * (n);
            }
        }
    }

    //RX, RY, #n
    //Rx  (Ry) + n
    //Y X n
    public void DADDI(int Y, int X, int n)
    {
        regs[X] = regs[Y] + n;
    }

    //RX, RY, RZ
    //Rx  (Ry) + (Rz)
    //Y Z X
    public void DADD(int Y, int Z, int X)
    {
        regs[X] = regs[Y] + regs[Z];
    }

    //DSUB RX, RY, RZ
    //Rx  (Ry) - (Rz)
    //Y Z X
    //34 5 1 5
    public void DSUB(int Y, int Z, int X)
    {
        regs[X] = regs[Y] - regs[Z];
    }

    public void FIN()
    {
        stop = 1;
    }

    // PROCESA UNA INSTRUCCIÓN
    public void procesarInstruccion(int i)
    {
        PC += 4;
        int cod, p1, p2, p3;
        cod = myMp.getInstIdx(i);
        p1 = myMp.getInstIdx(i + 1);
        p2 = myMp.getInstIdx(i + 2);
        p3 = myMp.getInstIdx(i + 3);

        switch (cod)
        {
            case 8:
                DADDI(p1, p2, p3);
                break;
            case 32:
                DADD(p1, p2, p3);
                break;
            case 34:
                DSUB(p1, p2, p3);
                break;
            case 35:
                LW(p1, p2, p3);
                break;
            case 43:
                //SW(p1, p2, p3);
                break;
            case 4:
                BEQZ(p1, p3);
                break;
            case 5:
                BNEZ(p1, p3);
                break;
            case 63:
                FIN();
                break;
        }
        puedoSeguir = true;
        
        try{
            //myMp.barrera.await();
            myMp.phaser.arriveAndAwaitAdvance();
            //myMp.phaser.arriveAndDeregister();
            //ciclo++;
            myMp.ciclo++;
            cont++;
            //System.out.println("Ciclo #"+ciclo+". Puede cambiar de instrucción.\n");
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }

    // SET VARIABLES NECESARIAS PARA RECONOCER EL PROGRAMA CORRIENDO 
    public void setPcAyLimit(int pcActual, int limite)
    {
        this.pcA = pcActual;
        this.limit = limite;
    }

    // PROCESA LAS INSTRUCCIONES QUE TENGA EL PROGRAMA
    
    public void procesar()
    {
        int cont = 0;
        //myMp.setClock(ciclo);
        myMp.setClock();
        stop = 0;
        IR = PC = pcA;
        
        while (stop != 1 && IR < limit)
        {
            IR = PC;
            
            if (puedoSeguir)
            {
                procesarInstruccion(IR);
                if (stop == 1) 
                { 
                    //verEstado();
                    myMp.verEstadisticas(myNumP);
                    System.out.println("Soy hilo: "+myNumP+" me salgo y actualmente hay: "+ myMp.phaser.getRegisteredParties()+ " por los cuales esperar.");
                }
            }
        }
    }

    // MÉTODO RUN DEL HILO, AQUÍ SE HACE LA COHERENCIA CON EL HILO PRINCIPAL
    public void run() {
        //procesar();
        
        while(pcA >= 0){
            System.out.println("Soy hilo: "+myNumP+" agarro un hilo y actualmente hay: "+ myMp.phaser.getRegisteredParties()+ " por los cuales esperar.");
            //System.out.println("Hola, soy "+myNumP+" y tengo el PC "+pcA);
            //System.out.println(limit);
            procesar();
            System.out.println("lo hice");
            pcA = myMp.getFreePC();
            limit = myMp.getActualPC();
            /*synchronized(this){
                notify();
                stop = 0;
                try{
                    wait();
                }catch(InterruptedException e){
                    System.out.println(e.getMessage());
                }
            }*/
        }
        if(deboDeregistrarme == 1)
            myMp.phaser.arriveAndDeregister();
    }

    // SET DE SENTINELA PARA TERMINAR EL HILO QUE SE ESTÁ CORRIENDO
    public void salir()
    {
        destruir = 1;
    }

    // MUESTRA EL ESTADO DEL PROCESADOR
    public String verEstado()
    {
        String estado = "";
        
        estado += "PROCESADOR " + myNumP + ":\n";
        estado += "El PC es: " + PC + "\n";
        estado += "El IR es: " + IR + "\n";
        estado += "Los registros de procesador son:\n";
        
        for (int i = 0; i < 32; i++)
        {
            estado += regs[i] + ", ";
        }
        
        estado += "\n";
        estado += "La memoria cache contiene:\n";
        
        for (int i = 0; i < 4; i++)
        {
            estado += "Bloque " + i + ", estado: " + getEstBloqueCache(i) + ", idBloque: " + getIdBloqueCache(i) + " --> ";
            
            for (int j = 0; j < 4; j++)
            {
                estado += getPalabraCache(i, j) + ", ";
            }
            estado += "\n";
        }
        
        estado += "La memoria de datos contiene:\n";
        
        for (int i = 0; i < 32; i++)
        {
            estado += getPalabraMem(i) + ", ";
        }
        
        estado += "\n";
        estado += "La cantidad de ciclos que tardó el hilo es: " + cont + "\n\n";
        //System.out.println(estado);
        return estado;
    }

}
