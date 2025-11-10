package org.example;

/**
 * Representa una respuesta posible dentro de una pregunta de evaluación.
 * <p>
 * Cada respuesta contiene un texto visible al estudiante, un indicador
 * de si es correcta o no, y un orden que define su posición de aparición.
 * </p>
 */
public class Respuesta {

    // -- Atributos --

    /** Texto visible al estudiante. */
    private String texto;

    /** Indica si la respuesta es la correcta. */
    private boolean esCorrecta;

    /** Posición u orden de la respuesta dentro de la pregunta. */
    private int orden;

    // -- Constructor --

    /**
     * Crea una nueva respuesta.
     *
     * @param texto texto visible al estudiante
     * @param esCorrecta indica si la respuesta es correcta
     * @param orden posición de la respuesta
     */
    public Respuesta(String texto, boolean esCorrecta, int orden) {
        this.texto = texto == null ? "" : texto.trim();
        this.esCorrecta = esCorrecta;
        this.orden = orden;
    }

    // -- Getters y Setters --

    /** Devuelve el texto de la respuesta. */
    public String getTexto() { return texto; }

    /** Establece el texto de la respuesta. */
    public void setTexto(String texto) { this.texto = texto == null ? "" : texto.trim(); }

    /** Indica si la respuesta es correcta. */
    public boolean isCorrecta() { return esCorrecta; }

    /** Define si la respuesta es correcta. */
    public void setCorrecta(boolean correcta) { esCorrecta = correcta; }

    /** Devuelve el orden de la respuesta. */
    public int getOrden() { return orden; }

    /** Establece el orden de la respuesta. */
    public void setOrden(int orden) { this.orden = orden; }

    // -- Representación en texto --

    /** Devuelve una representación textual de la respuesta. */
    @Override
    public String toString() {
        return "Respuesta{orden=" + orden + ", correcta=" + esCorrecta + ", texto='" + texto + "'}";
    }
}
