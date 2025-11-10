package org.example;

/**
 * Clase principal del programa.
 * <p>
 * Punto de entrada de la aplicación. Crea y muestra la ventana principal del sistema,
 * estableciendo su comportamiento de cierre.
 * </p>
 *
 * <p>
 * Elaborado por: <b>Elías Lizano Valerio</b> y <b>Giancarlo Artavia Chávez</b><br>
 * Fecha de creación: 10/10/2025
 * </p>
 */
public class Main {

    // -- Método principal --

    /**
     * Método principal del programa.
     * <p>
     * Inicializa la aplicación gráfica y muestra la ventana principal del sistema.
     * </p>
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        Ventana main = new Ventana();
        main.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }
}
