package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa la respuesta de un estudiante a una pregunta durante una evaluación.
 * <p>
 * Contiene los índices seleccionados por el estudiante, los puntos obtenidos
 * y una referencia a la pregunta correspondiente.
 * </p>
 * <p>
 * Se utiliza principalmente en la clase {@code IntentoEvaluacion} para
 * registrar las respuestas de un intento de evaluación.
 * </p>
 */
public class RespuestaEstudiante {

    // -- Atributos --

    /** Pregunta asociada a la respuesta. */
    private IPregunta pregunta;

    /** Índices o posiciones seleccionadas por el estudiante. */
    private List<Integer> ordenesSeleccionados;

    /** Puntos obtenidos por esta respuesta. */
    private int puntosObtenidos;

    /** Indica si la respuesta fue correcta. */
    private boolean esCorrecta;

    // -- Constructor --

    /**
     * Crea una respuesta asociada a una pregunta específica.
     *
     * @param pregunta pregunta a la cual pertenece esta respuesta
     */
    public RespuestaEstudiante(IPregunta pregunta) {
        this.pregunta = pregunta;
        this.ordenesSeleccionados = new ArrayList<>();
    }

    // -- Getters y Setters --

    /** Devuelve la pregunta asociada. */
    public IPregunta getPregunta() { return pregunta; }

    /** Asigna la pregunta asociada. */
    public void setPregunta(IPregunta pregunta) { this.pregunta = pregunta; }

    /**
     * Devuelve una copia inmutable de los índices seleccionados.
     * <p>
     * Esto evita que la lista original pueda ser modificada desde el exterior.
     * </p>
     */
    public List<Integer> getOrdenesSeleccionados() {
        return Collections.unmodifiableList(ordenesSeleccionados);
    }

    /**
     * Establece los índices seleccionados a partir de una lista externa.
     * <p>
     * Se realiza una copia defensiva para preservar la integridad de los datos.
     * </p>
     *
     * @param seleccion lista de índices seleccionados
     */
    public void setOrdenesSeleccionados(List<Integer> seleccion) {
        this.ordenesSeleccionados = (seleccion == null)
                ? new ArrayList<>()
                : new ArrayList<>(seleccion);
    }

    /**
     * Establece los índices seleccionados utilizando un arreglo variable de enteros.
     * <p>
     * Este método es útil para preguntas de tipo pareo o selección múltiple.
     * </p>
     *
     * @param valores índices seleccionados por el estudiante
     */
    public void setOrdenesSeleccionados(int... valores) {
        this.ordenesSeleccionados = new ArrayList<>();
        if (valores != null) {
            for (int v : valores) this.ordenesSeleccionados.add(v);
        }
    }

    // -- Métodos de utilidad --

    /** Limpia completamente la selección actual. */
    public void limpiarSeleccion() {
        this.ordenesSeleccionados.clear();
    }

    /**
     * Agrega un índice seleccionado a la lista.
     *
     * @param idx índice a agregar
     */
    public void agregarOrdenSeleccionado(int idx) {
        if (this.ordenesSeleccionados == null) this.ordenesSeleccionados = new ArrayList<>();
        this.ordenesSeleccionados.add(idx);
    }

    /** Devuelve los puntos obtenidos por la respuesta. */
    public int getPuntosObtenidos() { return puntosObtenidos; }

    /** Asigna los puntos obtenidos por la respuesta. */
    public void setPuntosObtenidos(int puntosObtenidos) { this.puntosObtenidos = puntosObtenidos; }

    /** Indica si la respuesta es correcta. */
    public boolean isEsCorrecta() { return esCorrecta; }

    /** Define si la respuesta es correcta. */
    public void setEsCorrecta(boolean esCorrecta) { this.esCorrecta = esCorrecta; }
}
