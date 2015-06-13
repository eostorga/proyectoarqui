package proy_arqui;


import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import proy_arqui.CargadorArchivos;
import java.util.concurrent.Phaser;

public class Multiprocesador {
    
    //public final CyclicBarrier barrera = new CyclicBarrier(1);
    public final Phaser phaser = new Phaser();
    int ciclo = 0; 
    
    //estructuras para el multiprocesador
    private Simulacion sim;
    private Estructuras estructura = new Estructuras();
    private Procesador proc1 = new Procesador(1,this,estructura);
    private Procesador proc2 = new Procesador(2,this,estructura);
    private Procesador proc3 = new Procesador(3,this,estructura);
    private ArrayList<Integer> instrucciones = new ArrayList<Integer>();
    private ArrayList<Integer> pcs = new ArrayList<Integer>();
    private int numHilitos; //cantidad de archivos cargados por el usuario
    
    //constructor
    public Multiprocesador(Simulacion sim){
        this.sim = sim;
        //estructura.Estructuras();
    }
    
    //agrega un numero al arreglo de instrucciones global
    public void agregarInstruccion(int num){
        instrucciones.add(num);
    }
    
    // paso de las PC´s
    
    public int getFreePC(){
        int x;
        if(pcs.size()>0){
            x = pcs.get(0);
            pcs.remove(0);
        }else{
            x=-1;
        }
        return x;
    }
    
    public int getActualPC(){
        if(pcs.size()==0){
            return instrucciones.size();
        }else{
            return pcs.get(0);
        }
    }
    
    public int getSizePCs(){
        return pcs.size();
    }
    
    //muestra las instrucciones cargadas en el multiprocesador
    public void verInstrucciones(){
        System.out.println("Se han cargado "+numHilitos+" programas.");
        System.out.println("El arreglo de instrucciones hasta el momento es el siguiente:");
        for(int i=0; i < instrucciones.size(); i++){
            System.out.print(instrucciones.get(i)+ " ");
        }
        System.out.println();
        System.out.println("Los indices donde inicia cada programa(PCs) son los siguientes:");
        for(int i=0; i < pcs.size(); i++){
            System.out.print(pcs.get(i)+ " ");
        }
        System.out.println();
        System.out.println();
    }
    
    //aumenta si se carga un nuevo programa
    public void sumarHilito(){
        numHilitos++;
    }
    
    //agrega un nuevo contador de programa
    public void agregarPc(){
        pcs.add(instrucciones.size());
    }
    
    //retorna la palabra que esta en el indice idx
    public int getInstIdx(int idx){
        return instrucciones.get(idx);
    }
    
    //se encarga de la logica del programa, de correr cada programa cargado y de hacer la sincronizacion de los hilos
    public void correrProgramas(){
        int pcActual;
        int limite = -1;
        
        int pc;
        pc = getFreePC();
        if(pc>=0){
            proc1.setPcAyLimit(pc,getActualPC());
            proc1.deboDeregistrarme = 1;
            phaser.register();
        }else{
            proc1.setPcAyLimit(-1,-1);
        }
        pc = getFreePC();
        if(pc>=0){
            proc2.setPcAyLimit(pc,getActualPC());
            proc2.deboDeregistrarme = 1;
            phaser.register();
        }else{
            proc2.setPcAyLimit(-1,-1);
        }
        pc = getFreePC();
        if(pc>=0){
            proc3.setPcAyLimit(pc,getActualPC());
            proc3.deboDeregistrarme = 1;
            phaser.register();
        }else{
            proc3.setPcAyLimit(-1,-1);
        }
        
        proc1.start();
        proc2.start();
        proc3.start();
        
        /*if(numHilitos!=0){
            for(int i = 0; i < numHilitos; i++){
                synchronized(proc1){
                    pcActual = pcs.get(i);
                    if((i+1)<pcs.size()) limite = pcs.get(i+1); else limite = instrucciones.size();
                    System.out.println(limite);
                    proc1.setPcAyLimit(pcActual, limite);
                    if(i==0)proc1.start();
                    System.out.println("sincronizando");
                    if(i!=0)proc1.notify();
                    try{
                        System.out.println("espero.....");
                        proc1.wait();
                        System.out.println("me sali");
                    }catch(InterruptedException e){
                        System.out.println(e.getMessage());
                    }
                }  
            }*/
            
        try{
            proc1.join();
            proc2.join();
            proc3.join();
        }catch(InterruptedException e){
            System.out.println(e.getMessage());
        }
        /*
        synchronized(proc1){
            proc1.salir();
            proc1.notify();
        }*/

        sim.setProc1((int) proc1.getId());
        sim.setProc2((int) proc2.getId());
        sim.setProc3((int) proc3.getId());
        //verEstadisticas();
    }
    
    //SET DEL RELOJ EN LA VENTANA DE SIMULACIÓN
    /*public void setClock(int r)
    {
        sim.setReloj(r);
    }*/
    public int setClock()
    {
        //sim.setReloj(ciclo);
        return ciclo;
    }
    
    //SET DE LAS ESTADÍSTICAS EN LA VENTANA DE SIMULACIÓN
    public void verEstadisticas(int numP)
    {
        if(numP==1) sim.setEstadisticas(proc1.verEstado());
        if(numP==2) sim.setEstadisticas(proc2.verEstado());
        if(numP==3) sim.setEstadisticas(proc3.verEstado());
        
    }
    
    public static void main(String[] args)
    {
        Simulacion sim = new Simulacion();
        Multiprocesador mp = new Multiprocesador(sim);
        CargadorArchivos crg = new CargadorArchivos(mp, sim);
        crg.setVisible(true);
    }
 
}
