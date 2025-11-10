package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa la asignación de una {@link Evaluacion} a un {@link Grupo}.
 * <p>
 * Cada asignación contiene la fecha y hora de inicio, y calcula automáticamente
 * la fecha/hora de finalización en función de la duración establecida en la evaluación.
 * </p>
 */
public class EvaluacionAsignada {

    // -- Atributos --

    /** Evaluación asociada. */
    private Evaluacion evaluacion;

    /** Grupo al que se asigna la evaluación. */
    private Grupo grupo;

    /** Fecha y hora en que inicia la evaluación. */
    private LocalDateTime fechaHoraInicio;

    /** Fecha y hora en que finaliza la evaluación (calculada automáticamente). */
    private LocalDateTime fechaHoraFinal;

    // -- Constructor --

    /**
     * Crea una nueva asignación de evaluación a un grupo.
     *
     * @param evaluacion evaluación que se aplicará
     * @param grupo grupo al que se asigna
     * @param fechaHoraInicio fecha y hora de inicio de la evaluación
     */
    public EvaluacionAsignada(Evaluacion evaluacion, Grupo grupo, LocalDateTime fechaHoraInicio) {
        this.evaluacion = evaluacion;
        this.grupo = grupo;
        this.fechaHoraInicio = fechaHoraInicio;
        calcularFechaHoraFinal();
    }

    // -- Cálculo de duración --

    /**
     * Calcula y actualiza la fecha/hora final en función de la duración de la evaluación.
     *
     * @return fecha/hora final calculada o {@code null} si no fue posible
     */
    public LocalDateTime calcularFechaHoraFinal() {
        if (evaluacion == null || fechaHoraInicio == null) {
            System.out.println("No se puede calcular la fecha final (evaluación o inicio nulos).");
            fechaHoraFinal = null;
            return null;
        }

        int minutos = evaluacion.getDuracionMinutos();
        if (minutos < 1) {
            System.out.println("Duración inválida (>= 1 minuto).");
            fechaHoraFinal = null;
            return null;
        }

        fechaHoraFinal = fechaHoraInicio.plusMinutes(minutos);
        return fechaHoraFinal;
    }

    // -- Estado de la evaluación --

    /**
     * Indica si la evaluación está activa actualmente.
     * <p>
     * Se considera activa si la hora actual está dentro del rango [inicio, fin]
     * y el grupo asociado se encuentra también vigente según sus fechas.
     * </p>
     *
     * @return {@code true} si la evaluación está activa
     */
    public boolean esActiva() {
        if (fechaHoraInicio == null || fechaHoraFinal == null) return false;

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(fechaHoraInicio) || now.isAfter(fechaHoraFinal)) return false;

        // Verifica si el grupo está activo en sus fechas
        if (grupo != null) {
            LocalDate hoy = now.toLocalDate();
            if (grupo.getFechaInicio() != null && hoy.isBefore(grupo.getFechaInicio())) return false;
            if (grupo.getFechaFinal() != null && hoy.isAfter(grupo.getFechaFinal()))   return false;
        }

        return true;
    }

    /**
     * Indica si la evaluación puede desasociarse del grupo.
     * <p>
     * Solo puede eliminarse si la fecha/hora de inicio aún no ha ocurrido.
     * </p>
     *
     * @return {@code true} si la evaluación puede desasociarse
     */
    public boolean canDesasociar() {
        return fechaHoraInicio != null && fechaHoraInicio.isAfter(LocalDateTime.now());
    }

    // -- Getters --

    /** @return evaluación asociada */
    public Evaluacion getEvaluacion() { return evaluacion; }

    /** @return grupo asignado */
    public Grupo getGrupo() { return grupo; }

    /** @return fecha/hora de inicio de la evaluación */
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }

    /** @return fecha/hora final de la evaluación */
    public LocalDateTime getFechaHoraFinal() { return fechaHoraFinal; }

    // -- Representación --

    @Override
    public String toString() {
        return "EvaluacionAsignada{" +
                "evaluacion=" + (evaluacion != null ? evaluacion.getNombre() : "null") +
                ", grupo=" + (grupo != null ? ("Curso=" +
                (grupo.getCurso() != null ? grupo.getCurso().nombre : "¿?") + " G" + grupo.getIdGrupo()) : "null") +
                ", inicio=" + fechaHoraInicio +
                ", fin=" + fechaHoraFinal +
                '}';
    }
}
