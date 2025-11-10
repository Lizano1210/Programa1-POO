package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RespuestaEstudiante {

    private IPregunta pregunta;
    private List<Integer> ordenesSeleccionados; // índices/órdenes elegidos (selección/pareo/sopa)
    private int puntosObtenidos;
    private boolean esCorrecta;

    public RespuestaEstudiante(IPregunta pregunta) {
        this.pregunta = pregunta;
        this.ordenesSeleccionados = new ArrayList<>();
    }

    // -------- Getters/Setters esenciales --------
    public IPregunta getPregunta() { return pregunta; }
    public void setPregunta(IPregunta pregunta) { this.pregunta = pregunta; }

    /** Devuelve copia inmutable de la selección */
    public List<Integer> getOrdenesSeleccionados() {
        return Collections.unmodifiableList(ordenesSeleccionados);
    }

    /** Reemplaza con copia defensiva */
    public void setOrdenesSeleccionados(List<Integer> seleccion) {
        this.ordenesSeleccionados = (seleccion == null)
                ? new ArrayList<>()
                : new ArrayList<>(seleccion);
    }
    // Aquí usamos varags, que es hacer que un metod pueda aceptar uno o más parametros, util para pareo
    public void setOrdenesSeleccionados(int... valores) {
        this.ordenesSeleccionados = new ArrayList<>();
        if (valores != null) {
            for (int v : valores) this.ordenesSeleccionados.add(v);
        }
    }

    public void limpiarSeleccion() {
        this.ordenesSeleccionados.clear();
    }

    public void agregarOrdenSeleccionado(int idx) {
        if (this.ordenesSeleccionados == null) this.ordenesSeleccionados = new ArrayList<>();
        this.ordenesSeleccionados.add(idx);
    }

    public int getPuntosObtenidos() { return puntosObtenidos; }
    public void setPuntosObtenidos(int puntosObtenidos) { this.puntosObtenidos = puntosObtenidos; }

    public boolean isEsCorrecta() { return esCorrecta; }
    public void setEsCorrecta(boolean esCorrecta) { this.esCorrecta = esCorrecta; }
}

