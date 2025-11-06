package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 Respuesta de un estudiante a una pregunta
 */
public class RespuestaEstudiante {
    // Atributos

    IPregunta pregunta;
    // Para preguntas de selección/VF
    List<Integer> ordenesSeleccionados = new ArrayList<>();

    /** EN STAND BY, puedes que las use para pareo y sopa pero aun ns
    // Para preguntas basadas en texto
    String respuestaTexto;
    List<String> respuestasTexto;
    */

    int puntosObtenidos;
    boolean esCorrecta;

    public RespuestaEstudiante(IPregunta pregunta) {
        this.pregunta = pregunta;
    }


    /** agrega el orden seleccionado (para selección única/múltiple/VF). */
    public void agregarOrdenSeleccionado(int orden) {
        this.ordenesSeleccionados.add(orden);
    }

    /** Devuelve una lista fija de los órdenes seleccionados. */
    public List<Integer> getOrdenesSeleccionados() {
        return Collections.unmodifiableList(ordenesSeleccionados);
    }

    public List<String> obtenerRespuestaCorrecta() {
        List<String> textos = new ArrayList<>();
        if (pregunta instanceof Pregunta p) {
            for (Respuesta r : p.getRespuestas()) {
                if (r.isCorrecta()) {
                    textos.add(r.getTexto());
                }
            }
        }
        return textos;
    }

    // Getters y Setters
    public IPregunta getPregunta() { return pregunta; }

    /**
    public String getRespuestaTexto() { return respuestaTexto; }
    public void setRespuestaTexto(String respuestaTexto) { this.respuestaTexto = (respuestaTexto == null ? "" : respuestaTexto); }

    public List<String> getRespuestasTexto() { return respuestasTexto == null ? List.of() : Collections.unmodifiableList(respuestasTexto); }
    public void setRespuestasTexto(List<String> respuestasTexto) {
        this.respuestasTexto = (respuestasTexto == null ? new ArrayList<>() : new ArrayList<>(respuestasTexto));
    }
     */

    public int getPuntosObtenidos() { return puntosObtenidos; }
    public void setPuntosObtenidos(int puntosObtenidos) { this.puntosObtenidos = puntosObtenidos; }

    public boolean isEsCorrecta() { return esCorrecta; }
    public void setEsCorrecta(boolean esCorrecta) { this.esCorrecta = esCorrecta; }
}
