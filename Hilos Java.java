/**
 * CafeteriaUniversitaria.java
 * Simulador de atención concurrente en cafetería con capacidad limitada
 * Autor: Andrea Contreras
 * Fecha: Mayo 2026
 */
public class CafeteriaUniversitaria {
    
    /**
     * Clase que representa un estudiante como hilo independiente
     */
    static class Estudiante implements Runnable {
        private final int numero;
        private final ControlCajeros cajeros;
        
        public Estudiante(int numero, ControlCajeros cajeros) {
            this.numero = numero;
            this.cajeros = cajeros;
        }
        
        @Override
        public void run() {
            try {
                // Estudiante llega a la cafetería
                System.out.println("Estudiante " + numero + " llega a la cafetería");
                
                // Solicitar acceso a un cajero (espera si ambos están ocupados)
                int numeroCajero = cajeros.solicitarCajero(numero);
                
                // Simular tiempo de atención (1000 a 3000 ms)
                int tiempoAtencion = 1000 + (int)(Math.random() * 2000);
                Thread.sleep(tiempoAtencion);
                
                System.out.println("Estudiante " + numero + " es atendido, tiempo: " 
                                 + tiempoAtencion + " ms");
                
                // Liberar el cajero
                cajeros.liberarCajero(numeroCajero, numero);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Estudiante " + numero + " fue interrumpido");
            }
        }
    }
    
    /**
     * Clase que controla el acceso sincronizado a los dos cajeros
     * Usa un arreglo booleano para rastrear disponibilidad
     */
    static class ControlCajeros {
        private final boolean[] cajerosDisponibles = {true, true}; // true = libre
        
        /**
         * Método sincronizado para solicitar un cajero
         * Bloquea hasta que haya uno disponible
         */
        public synchronized int solicitarCajero(int estudiante) throws InterruptedException {
            // Esperar mientras ambos cajeros estén ocupados
            // IMPORTANTE: usar while para re-verificar después de wait()
            while (!cajerosDisponibles[0] && !cajerosDisponibles[1]) {
                wait(); // Libera el monitor y espera notificación
            }
            
            // Buscar el primer cajero disponible
            int cajero = cajerosDisponibles[0] ? 0 : 1;
            cajerosDisponibles[cajero] = false; // Marcar como ocupado
            
            System.out.println("Estudiante " + estudiante + " pasa a caja (Cajero " 
                             + (cajero + 1) + ")");
            
            return cajero;
        }
        
        /**
         * Método sincronizado para liberar un cajero
         * Notifica a los estudiantes en espera
         */
        public synchronized void liberarCajero(int cajero, int estudiante) {
            cajerosDisponibles[cajero] = true; // Marcar como disponible
            System.out.println("Estudiante " + estudiante + " sale. Cajero " 
                             + (cajero + 1) + " libre.");
            
            // Notificar a todos los hilos en espera que hay un cajero disponible
            notifyAll();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== SIMULADOR CAFETERÍA POLITÉCNICO GRANCOLOMBIANO ===");
        System.out.println("Cajeros disponibles: 2");
        System.out.println("Estudiantes: 6\n");
        
        // Recurso compartido que controla los dos cajeros
        ControlCajeros cajeros = new ControlCajeros();
        
        // Crear arreglo de hilos para los 6 estudiantes
        Thread[] estudiantes = new Thread[6];
        
        // Crear e iniciar los 6 hilos de estudiantes
        for (int i = 0; i < 6; i++) {
            estudiantes[i] = new Thread(
                new Estudiante(i + 1, cajeros), 
                "Hilo-Estudiante-" + (i + 1)
            );
            estudiantes[i].start();
        }
        
        // Esperar a que todos los estudiantes terminen
        try {
            for (Thread estudiante : estudiantes) {
                estudiante.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Hilo principal interrumpido");
        }
        
        System.out.println("\n=== TODOS LOS ESTUDIANTES HAN SIDO ATENDIDOS ===");
    }
}