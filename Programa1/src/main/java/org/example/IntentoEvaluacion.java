package org.example;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un intento de un estudiante al resolver una evaluación.
 * <p>
 * Contiene los datos del estudiante, la evaluación, el grupo al que pertenece,
 * la duración del intento y los resultados obtenidos.
 * </p>
 */
public class IntentoEvaluacion {

    // -- Atributos --

    /** Estudiante que realizó el intento. */
    private Estudiante estudiante;

    /** Evaluación aplicada. */
    private Evaluacion evaluacion;

    /** Grupo al que pertenece el intento. */
    private Grupo grupo;

    /** Fecha y hora en que el estudiante inició la evaluación. */
    private LocalDateTime fechaHoraInicio;

    /** Fecha y hora en que el estudiante finalizó la evaluación. */
    private LocalDateTime fechaHoraFinal;

    /** Lista de respuestas dadas por el estudiante. */
    private List<RespuestaEstudiante> respuestasEstudiante = new ArrayList<>();

    /** Orden en que las preguntas fueron mostradas durante el intento. */
    private List<Integer> ordenPreguntasUsado = new ArrayList<>();

    /** Puntaje total obtenido. */
    private int puntajeObtenido;

    /** Calificación final (porcentaje del 0 al 100). */
    private double calificacion;

    // -- Constructor --

    /**
     * Crea un nuevo intento de evaluación con los datos proporcionados.
     *
     * @param estudiante estudiante que realiza el intento
     * @param evaluacion evaluación aplicada
     * @param grupo grupo del estudiante
     * @param fechaHoraInicio hora de inicio del intento
     * @param fechaHoraFinal hora de finalización del intento
     * @param respuestasEstudiante lista de respuestas dadas
     * @param puntajeObtenido puntaje obtenido (puede ser 0)
     * @param calificacion calificación inicial (0–100)
     * @param ordenPreguntasUsado orden en que se presentaron las preguntas
     */
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

        // Copia defensiva de las listas
        this.respuestasEstudiante = new ArrayList<>();
        if (respuestasEstudiante != null) {
            this.respuestasEstudiante.addAll(respuestasEstudiante);
        }

        this.puntajeObtenido = puntajeObtenido;
        this.calificacion = calificacion;

        setOrdenPreguntasUsado(ordenPreguntasUsado);
    }

    // -- Cálculo de resultados --

    /**
     * Calcula el puntaje total y la calificación final.
     * <p>
     * La calificación se expresa en porcentaje (0–100).
     * </p>
     *
     * @return calificación obtenida
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
        int totalEval = Math.max(1, evaluacion.getPuntajeTotal());
        this.calificacion = (total * 100.0) / totalEval;
        return this.calificacion;
    }

    // -- Duración y detalles --

    /**
     * Calcula la duración total del intento.
     *
     * @return duración entre inicio y finalización, o cero si no hay datos
     */
    public Duration obtenerTiempoUsado() {
        if (fechaHoraInicio == null || fechaHoraFinal == null) return Duration.ZERO;
        return Duration.between(fechaHoraInicio, fechaHoraFinal);
    }

    /**
     * Obtiene los detalles de la respuesta a una pregunta específica.
     *
     * @param numPregunta número o ID de la pregunta
     * @return respuesta correspondiente, o {@code null} si no existe
     */
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

    // -- Setters auxiliares --

    /** Define la fecha y hora de finalización del intento. */
    public void setFechaHoraFinal(LocalDateTime fin) { this.fechaHoraFinal = fin; }

    /** Define el orden en que se mostraron las preguntas (copia defensiva). */
    public void setOrdenPreguntasUsado(List<Integer> orden) {
        this.ordenPreguntasUsado = (orden == null) ? new ArrayList<>() : new ArrayList<>(orden);
    }

    // -- Getters --

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
