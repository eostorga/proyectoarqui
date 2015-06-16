package proy_arqui;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private int cicloInicio = -1;
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
        int indiceDir = estr.mapearABloque(idBloque)%8; 
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
        int cantCiclos = 0;
        
        for (int i = 0; i < 4; i++)
        {
            setPalabraCache(direccionCache, i, getPalabraMem(j));
            j++;
        }
        
        switch(myNumP)
        {
            case 1:
            {
                if(direccionMemoria >= 0 && direccionMemoria <= 31) cantCiclos = 16;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 32;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 32;
            }
            break;
            case 2:
            {
                if(direccionMemoria >= 0 && direccionMemoria <= 31) cantCiclos = 32;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 16;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 32;
            }
            break;
            case 3:
            {
                if(direccionMemoria >= 0 && direccionMemoria <= 31) cantCiclos = 32;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 32;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 16;
            }
            break;
        }

        /*
        for (int i = 0; i < cantCiclos; i++){
            puedoSeguir = false;
            try
            {
                myMp.phaser.arriveAndAwaitAdvance();
                myMp.ciclo++;
                cont++;
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }*/
        
        for (int i = 0; i < cantCiclos; i++){
            puedoSeguir = false;/*
            try{
                myMp.phaser.arriveAndAwaitAdvance();
            }catch(Exception e){}*/
            myMp.ciclo++;
            cont++;
        }

        puedoSeguir = true;
        //System.out.println("Ya puede cambiar de instrucción.");
    }

    // GUARDA EL BLOQUE ENTERO DESDE CACHÉ HASTA SU POSICIÓN EN MEMORIA
    public void guardarEnMemoria(int direccionMemoria, int direccionCache)
    {
        int j = direccionMemoria;
        int cantCiclos = 0;
                
        for (int i = 0; i < 4; i++)
        {
            setPalabraMem(j, getPalabraCache(direccionCache, i));
            j++;
        }
        
        switch(myNumP)
        {
            case 1:
            {
                if(direccionMemoria >= 0 && direccionMemoria <= 31) cantCiclos = 16;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 32;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 32;
            }
            break;
            case 2:
            {
                if(direccionMemoria >= 0 && direccionMemoria <= 31) cantCiclos = 32;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 16;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 32;
            }
            break;
            case 3:
            {
                if(direccionMemoria >= 0 && direccionMemoria <= 31) cantCiclos = 32;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 32;
                if(direccionMemoria >= 32 && direccionMemoria <= 63) cantCiclos = 16;
            }
            break;
        }
        
        for (int i = 0; i < cantCiclos; i++){
            puedoSeguir = false; /*
            try{
                myMp.phaser.arriveAndAwaitAdvance();
            }catch(Exception e){}*/
            myMp.ciclo++;
            cont++;
        }
        
        puedoSeguir = true;
    }
    
    // Leer una palabra
    // LW RX, n(RY)
    // Rx  M(n + (Ry))
    // Y X n
    public void LW(int Y, int X, int n)
    {
        int numByte = regs[Y] + n;                                  // Numero del byte que quiero leer de memoria 
        int numBloqMem = Math.floorDiv(numByte, 16);                // Indice del bloque en memoria (0-24)
        int numPalabra = (numByte % 16) / 4;                        // Va de 4 en 4 (palabras a caché digamos)
        int dirBloqCache = numBloqMem % 4;                          // Indice donde debe estar el bloque en cache (del 0 al 4)
        int idBloqEnCache = getIdBloqueCache(dirBloqCache);         // ID del bloque que ocupa actualmente esa direccion en cache (0-96)
        int estadoBloqEnCache = getEstBloqueCache(dirBloqCache);    // Estado del bloque que ocupa esa dir de cache ('M', 'C', 'I')
        int dirNumBloqMem = numBloqMem * 4;                         // Conversion para mapear la direccion inicial del bloque en memoria (0-96)

        // BLOQUEO MI CACHÉ
        estr.waitC(myNumP);
        System.out.println("INICIA LW");
        
        //CASO 1: HAY OTRO BLOQUE DIFERENTE PERO VÁLIDO
        if(idBloqEnCache != dirNumBloqMem && idBloqEnCache != -1)
        {
            switch(estadoBloqEnCache) // estadoBloqEnCache se refiere al bloque que estorba
            {
                case C:
                    if (estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0) //dir del bloque que tengo ahorita en cache
                    {
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(idBloqEnCache));
                        // USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        estr.quitarProcesador(idBloqEnCache, myNumP);
                        estr.verificarUncached(idBloqEnCache); //por si solo uno lo tiene compartido, q se ponga 'U'
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);
                    }
                break;
                case M:
                    if (estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0) //agarro el dir del bloq q si ocupo
                    {
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(idBloqEnCache));    
                        // USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        guardarEnMemoria(idBloqEnCache, dirBloqCache);
                        //estr.guardarEnMemoria(myNumP, idBloqEnCache, dirBloqCache);
                        estr.quitarProcesador(idBloqEnCache, myNumP);  
                        estr.verificarUncached(idBloqEnCache); //por si solo uno lo tiene compartido, q se ponga 'U'        
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);
                    }
                break;
                case I:
                    setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                    setEstBloqueCache(dirBloqCache, I);
                break;
            }
            idBloqEnCache = dirNumBloqMem;
        }
        
        //CASO 2: ESTÁ EL BLOQUE BLOQUE (HIT) O NO HAY NINGUNO
        if(idBloqEnCache == dirNumBloqMem || idBloqEnCache == -1)
        {
            switch(estadoBloqEnCache)
            {
                case C:
                    // Carga la palabra que se ocupa al registro
                    regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                    estr.signalC(myNumP);
                break;
                case M: 
                    if (estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0)
                    {
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(idBloqEnCache));
			// USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
			guardarEnMemoria(idBloqEnCache, dirBloqCache);
                        //estr.guardarEnMemoria(myNumP, idBloqEnCache, dirBloqCache);
                        setEstDir(estr.directorioPapa(idBloqEnCache), idBloqEnCache, C); //
                        estr.anadirProcesador(idBloqEnCache, myNumP);       
                        setEstBloqueCache(dirBloqCache, C);
                        regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                        estr.signalC(myNumP);
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                    }
                break;
                case I:
                    idBloqEnCache = dirNumBloqMem; //si habia un bloque id=-1 se vino por aca
                    if (estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0)
                    {
                        estr.signalC(myNumP);
                        LW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(idBloqEnCache));
			// USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        if (estr.getEstadoBloqueDir(numBloqMem) == C) //quiero el estado en el dir del bloque 0-24
                        {
                            cargarACache(dirNumBloqMem, dirBloqCache);
                            //estr.cargarACache(myNumP, dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem); //no es necesario
                            setEstBloqueCache(dirBloqCache, C);
                            estr.anadirProcesador(idBloqEnCache, myNumP);
                            regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                            estr.signalD(estr.directorioPapa(idBloqEnCache));
                            estr.signalC(myNumP);
                        } else if (estr.getEstadoBloqueDir(numBloqMem) == M)
                        {
                            int cacheDuena = estr.consultarDuenoBloqueDir(idBloqEnCache);
                            if (estr.disponibleC(cacheDuena) == 0)
                            {
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(idBloqEnCache));
                                LW(Y, X, n);
                            } else
                            {
                                estr.waitC(cacheDuena);  //si la logro agarrar
                                // USA DIRECTORIO EN EL SIGUIENTE CICLO //
                                // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
                                estr.guardarEnMemoria(cacheDuena, idBloqEnCache, dirBloqCache);
                                estr.setEstBloqueCache(cacheDuena, dirBloqCache, C);
                                estr.signalC(cacheDuena);
                                setEstDir(estr.directorioPapa(idBloqEnCache), idBloqEnCache, C);
                                estr.anadirProcesador(idBloqEnCache, myNumP);
                                cargarACache(dirNumBloqMem, dirBloqCache);
                                //estr.cargarACache(myNumP, dirNumBloqMem, dirBloqCache);
                                setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                                setEstBloqueCache(dirBloqCache, C);
                                // Carga la palabra que se ocupa al registro
                                regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(idBloqEnCache));
                            }
                        } else if (estr.getEstadoBloqueDir(numBloqMem) == U)
                        {
                            cargarACache(dirNumBloqMem, dirBloqCache);
                            //estr.cargarACache(myNumP, dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem); 
                            setEstBloqueCache(dirBloqCache, C);
                            setEstDir(estr.directorioPapa(idBloqEnCache), idBloqEnCache, C);
                            estr.anadirProcesador(idBloqEnCache, myNumP);
                            // Carga la palabra que se ocupa al registro
                            regs[X] = getPalabraCache(dirBloqCache, numPalabra);
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(idBloqEnCache));
                        }
                    }
                break;
            }   
        } 
    }
    
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
        System.out.println("INICIA SW");
        
        //CASO 1: HAY OTRO BLOQUE DIFERENTE PERO VÁLIDO
        if(idBloqEnCache != dirNumBloqMem && idBloqEnCache != -1)
        {
            switch(estadoBloqEnCache) // estadoBloqEnCache se refiere al bloque que estorba
            {
                case C:
                    if (estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0) //dir del bloque que tengo ahorita en cache
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(idBloqEnCache));
                        // USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        estr.quitarProcesador(idBloqEnCache, myNumP);
                        estr.verificarUncached(idBloqEnCache); //por si solo uno lo tiene compartido, q se ponga 'U'
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);
                    }
                break;
                case M:
                    if (estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0) //agarro el dir del bloq q si ocupo
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(idBloqEnCache)); 
                        // USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        guardarEnMemoria(idBloqEnCache, dirBloqCache);
                        //estr.guardarEnMemoria(myNumP, idBloqEnCache, dirBloqCache);
                        estr.quitarProcesador(idBloqEnCache, myNumP);  
                        estr.verificarUncached(idBloqEnCache); //por si solo uno lo tiene compartido, q se ponga 'U'                     
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                        setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                        setEstBloqueCache(dirBloqCache, I);
                    }
                break;
                case I:
                    setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                    setEstBloqueCache(dirBloqCache, I);
                break;
            }
            idBloqEnCache = dirNumBloqMem;
        }
        
        //CASO 2: ESTÁ EL BLOQUE BLOQUE (HIT) O NO HAY NINGUNO
        if(idBloqEnCache == dirNumBloqMem || idBloqEnCache == -1)
        {   
            switch(estadoBloqEnCache){
                case C:
                    if(estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0) // esto es para saber si el directorio que voy a utilizar esta ocupado o no
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(idBloqEnCache));
			// USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        estr.quitarCompartidos(idBloqEnCache, myNumP); //quita a los demas y me deja a mi
                        estr.anadirProcesador(idBloqEnCache, myNumP); 
                        setEstDir(estr.directorioPapa(idBloqEnCache), idBloqEnCache, M);
                        estr.signalD(estr.directorioPapa(idBloqEnCache));
                        setPalabraCache(dirBloqCache, numPalabra, regs[X]);
                        setEstBloqueCache(dirBloqCache, M);  
                        estr.signalC(myNumP);
                    }
                    break;
                case M:
                    guardarEnMemoria(idBloqEnCache, dirBloqCache);
                    //estr.guardarEnMemoria(myNumP, idBloqEnCache, dirBloqCache);
                    setPalabraCache(dirBloqCache, numPalabra, regs[X]);
                    setEstBloqueCache(dirBloqCache, M);
                    estr.signalC(myNumP);
                    break;
                case I:  
                    idBloqEnCache = dirNumBloqMem; //si id=-1 se viene por aca
                    if(estr.disponibleD(estr.directorioPapa(idBloqEnCache)) == 0)
                    {
                        estr.signalC(myNumP);
                        SW(Y, X, n);
                    } else
                    {
                        estr.waitD(estr.directorioPapa(idBloqEnCache));
			// USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
                        if (estr.getEstadoBloqueDir(numBloqMem) == C)
                        {   
                            estr.quitarCompartidos(idBloqEnCache, myNumP); //quita a los demas y me deja a mi
                            estr.anadirProcesador(idBloqEnCache, myNumP); 
                            setEstDir(estr.directorioPapa(idBloqEnCache), idBloqEnCache, M);
                            cargarACache(dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, C);
                            setPalabraCache(dirBloqCache, numPalabra, regs[X]);
                            setEstBloqueCache(dirBloqCache, M);  
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(idBloqEnCache));
                        } else if(estr.getEstadoBloqueDir(numBloqMem) == M)
                        {   
                            int cacheDuena = estr.consultarDuenoBloqueDir(idBloqEnCache);
                            if (estr.disponibleC(cacheDuena) == 0)
                            {
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(idBloqEnCache));
                                SW(Y, X, n);
                            } else
                            {
                                estr.waitC(cacheDuena);
                                // USA DIRECTORIO EN EL SIGUIENTE CICLO //
                                // -- USA DIRECTORIO EN EL SIGUIENTE CICLO //
                                estr.guardarEnMemoria(cacheDuena, idBloqEnCache, dirBloqCache);
                                estr.setEstBloqueCache(cacheDuena, dirBloqCache, I);
                                estr.signalC(cacheDuena);
                                estr.quitarCompartidos(idBloqEnCache, myNumP); //quita a los demas y me deja a mi
                                estr.anadirProcesador(idBloqEnCache, myNumP);
                                setEstDir(estr.directorioPapa(idBloqEnCache), idBloqEnCache, M);
                                cargarACache(dirNumBloqMem, dirBloqCache);
                                //estr.cargarACache(myNumP, dirNumBloqMem, dirBloqCache);
                                setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                                setEstBloqueCache(dirBloqCache, M);
                                setPalabraCache(dirBloqCache, numPalabra, regs[X]);
                                estr.signalC(myNumP);
                                estr.signalD(estr.directorioPapa(idBloqEnCache));
                            }
                        } else if(estr.getEstadoBloqueDir(numBloqMem) == U)
                        {
                            cargarACache(dirNumBloqMem, dirBloqCache);
                            //estr.cargarACache(myNumP, dirNumBloqMem, dirBloqCache);
                            setIdBloqueCache(dirBloqCache, dirNumBloqMem);
                            setEstBloqueCache(dirBloqCache, M);
                            setEstDir(estr.directorioPapa(idBloqEnCache), idBloqEnCache, M);
                            estr.anadirProcesador(idBloqEnCache, myNumP);
                            setPalabraCache(dirBloqCache, numPalabra, regs[X]);
                            estr.signalC(myNumP);
                            estr.signalD(estr.directorioPapa(idBloqEnCache));
                        }
                    }
                break;
            }
        }
    }

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
        //System.out.println("El registro 5 vale: "+regs[5]+"\n");
        //System.out.println("El registro 20 vale: "+regs[20]+"\n");
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
                SW(p1, p2, p3);
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
        cicloInicio = myMp.setClock();
        stop = 0;
        IR = PC = pcA;
        
        while (stop != 1 && IR < limit)
        {
            IR = PC;
            
            if (puedoSeguir)
            {
                //System.out.println("Instruccion "+(IR/4));
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
        
        estado += "El reloj inicia en " + cicloInicio + ":\n";
        
        estado += "El PC es: " + PC + "\n";
        estado += "El IR es: " + IR + "\n";
        estado += "Los registros de procesador son:\n";
        
        for (int i = 0; i < 32; i++)
        {
            estado += regs[i] + ", ";
        }
        
        estado += "\n";
        estado += "La memoria cache 1 contiene:\n";
        
        for (int i = 0; i < 4; i++)
        {
            estado += "Bloque " + i + ", estado: " + estr.getEstBloqueCache(1, i) + ", idBloque: " + estr.getIdBloqueCache(1, i) + " --> ";
            
            for (int j = 0; j < 4; j++)
            {
                estado += estr.getPalabraCache(1, i, j) + ", ";
            }
            estado += "\n";
        }
        
         estado += "La memoria cache 2 contiene:\n";
        
        for (int i = 0; i < 4; i++)
        {
            estado += "Bloque " + i + ", estado: " + estr.getEstBloqueCache(2, i) + ", idBloque: " + estr.getIdBloqueCache(2, i) + " --> ";
            
            for (int j = 0; j < 4; j++)
            {
                estado += estr.getPalabraCache(2, i, j) + ", ";
            }
            estado += "\n";
        }
        
         estado += "La memoria cache 3 contiene:\n";
        
        for (int i = 0; i < 4; i++)
        {
            estado += "Bloque " + i + ", estado: " + estr.getEstBloqueCache(3, i) + ", idBloque: " + estr.getIdBloqueCache(3, i) + " --> ";
            
            for (int j = 0; j < 4; j++)
            {
                estado += estr.getPalabraCache(3, i, j) + ", ";
            }
            estado += "\n";
        }
        
        estado += "La memoria de datos contiene:\n";
        
        estado += "Memoria comp P1:\n";
        for (int i = 0; i < 32; i+=4)
        {
            estado += getPalabraMem(i) + ", " + getPalabraMem(i+1) + ", " +getPalabraMem(i+2) + ", " +getPalabraMem(i+3) + "\n";
        }
        estado += "Memoria comp P2:\n";
        for (int i = 32; i < 64; i+=4)
        {
            estado += getPalabraMem(i) + ", " + getPalabraMem(i+1) + ", " +getPalabraMem(i+2) + ", " +getPalabraMem(i+3) + "\n";
        }
        estado += "Memoria comp P3:\n";
        for (int i = 64; i < 96; i+=4)
        {
            estado += getPalabraMem(i) + ", " + getPalabraMem(i+1) + ", " +getPalabraMem(i+2) + ", " +getPalabraMem(i+3) + "\n";
        }
        
        estado += "La cantidad de ciclos que tardó el hilo es: " + cont + "\n\n";
        //System.out.println(estado);
        return estado;
    }

}
