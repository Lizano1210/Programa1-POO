package org.example;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registra un intento concreto de un estudiante resolviendo una evaluación.
 */
public class IntentoEvaluacion {
    // Atributos
    Estudiante estudiante;
    Evaluacion evaluacion;
    Grupo grupo;
    LocalDateTime fechaHoraInicio;
    LocalDateTime fechaHoraFinal;
    List<RespuestaEstudiante> respuestasEstudiante = new ArrayList<>();
    List<Integer> ordenPreguntasUsado = new ArrayList<>(); // id/orden mostrado
    int puntajeObtenido;   // suma de puntos
    double calificacion;   // (puntajeObtenido / puntajeTotal) * 100

    public IntentoEvaluacion(Estudiante estudiante,
                             Evaluacion evaluacion,
                             Grupo grupo,
                             LocalDateTime fechaHoraInicio,
                             LocalDateTime fechaHoraFinal,
                             List<RespuestaEstudiante> respuestasEstudiante,
                             int puntajeObtenido,
                             double calificacion,
                             List<Integer> ordenPreguntasUsado) {
        this.estudiante = estudiante;
        this.evaluacion = evaluacion;
        this.grupo = grupo;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFinal = fechaHoraFinal;

        // Clonar defensivamente las colecciones recibidas
        this.respuestasEstudiante = new ArrayList<>();
        if (respuestasEstudiante != null) {
            this.respuestasEstudiante.addAll(respuestasEstudiante);
        }

        this.puntajeObtenido = puntajeObtenido;
        this.calificacion = calificacion;

        setOrdenPreguntasUsado(ordenPreguntasUsado); // ya tienes este setter defensivo
    }


    /**
     Calcula el puntaje total y la calificación (0..100).
     */
    public double calcularCalificacion() {
        if (evaluacion == null) return 0.0;
        int total = 0;
        for (RespuestaEstudiante r : respuestasEstudiante) {
            if (r == null || r.getPregunta() == null) continue;
            int puntos = r.getPregunta().calificar(r);
            r.setPuntosObtenidos(puntos);
            r.setEsCorrecta(puntos == r.getPregunta().obtenerPuntos());
            total += puntos;
        }
        this.puntajeObtenido = total;
        int totalEval = Math.max(1, evaluacion.getPuntajeTotal()); // evita división por cero
        this.calificacion = (total * 100.0) / totalEval;
        return this.calificacion;
    }

    /** Devuelve cuanto duro el estudiante */
    public Duration obtenerTiempoUsado() {
        if (fechaHoraInicio == null || fechaHoraFinal == null) return Duration.ZERO;
        return Duration.between(fechaHoraInicio, fechaHoraFinal);
    }

    public RespuestaEstudiante obtenerDetalleRespuesta(int numPregunta) {
        for (RespuestaEstudiante r : respuestasEstudiante) {
            if (r != null && r.getPregunta() instanceof Pregunta p && p.getId() == numPregunta) {
                return r;
            }
        }
        if (numPregunta >= 0 && numPregunta < respuestasEstudiante.size()) {
            return respuestasEstudiante.get(numPregunta);
        }
        return null;
    }

    // metodos auxiliares
    public void setFechaHoraFinal(LocalDateTime fin) { this.fechaHoraFinal = fin; }

    public void setOrdenPreguntasUsado(List<Integer> orden) {
        this.ordenPreguntasUsado = (orden == null) ? new ArrayList<>() : new ArrayList<>(orden);
    }

    // Getters
    public Estudiante getEstudiante() { return estudiante; }
    public Evaluacion getEvaluacion() { return evaluacion; }
    public Grupo getGrupo() { return grupo; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public LocalDateTime getFechaHoraFinal() { return fechaHoraFinal; }
    public List<RespuestaEstudiante> getRespuestasEstudiante() { return Collections.unmodifiableList(respuestasEstudiante); }
    public int getPuntajeObtenido() { return puntajeObtenido; }
    public double getCalificacion() { return calificacion; }
    public List<Integer> getOrdenPreguntasUsado() { return Collections.unmodifiableList(ordenPreguntasUsado); }
}
