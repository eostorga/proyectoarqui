package proy_arqui;


import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import proy_arqui.CargadorArchivos;

public class Multiprocesador {
    
    public final CyclicBarrier barrera = new CyclicBarrier(1);
    
    //estructuras para el multiprocesador
    private Simulacion sim;
    private Estructuras estructura = new Estructuras();
    private Procesador proc1 = new Procesador(1,this,estructura);
    private ArrayList<Integer> instrucciones = new ArrayList<Integer>();
    private ArrayList<Integer> pcs = new ArrayList<Integer>();
    private int numHilitos; //cantidad de archivos cargados por el usuario
    
    //constructor
    public Multiprocesador(Simulacion sim){
        this.sim = sim;
    }
    
    //agrega un numero al arreglo de instrucciones global
    public void agregarInstruccion(int num){
        instrucciones.add(num);
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
        if(numHilitos!=0){
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
            }
            synchronized(proc1){
                proc1.salir();
                proc1.notify();
            }
            sim.setProc1((int) proc1.getId());
            //verEstadisticas();
        }
    }
    
    //SET DEL RELOJ EN LA VENTANA DE SIMULACIÓN
    public void setClock(int r)
    {
        sim.setReloj(r);
    }
    
    //SET DE LAS ESTADÍSTICAS EN LA VENTANA DE SIMULACIÓN
    public void verEstadisticas()
    {
        sim.setEstadisticas(proc1.verEstado());
    }
    
    public static void main(String[] args)
    {
        Simulacion sim = new Simulacion();
        Multiprocesador mp = new Multiprocesador(sim);
        CargadorArchivos crg = new CargadorArchivos(mp, sim);
        crg.setVisible(true);
    }
 
}
