package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Asocia una evaluación a un grupo con fecha/hora de inicio y fin.
 * La fecha/hora final se calcula a partir de la duración de la evaluación.
 */
public class EvaluacionAsignada {
    // Atributos
    Evaluacion evaluacion;
    Grupo grupo;
    LocalDateTime fechaHoraInicio;
    LocalDateTime fechaHoraFinal;

    public EvaluacionAsignada(Evaluacion evaluacion, Grupo grupo, LocalDateTime fechaHoraInicio) {
        this.evaluacion = evaluacion;
        this.grupo = grupo;
        this.fechaHoraInicio = fechaHoraInicio;
        calcularFechaHoraFinal();
    }

    /** Suma la duración de la evaluación a la fecha/hora de inicio. */
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

    /**
     * true si "ahora" esta en el rango [inicio, fin] y el grupo está dentro de sus fechas.
     */
    public boolean esActiva() {
        if (fechaHoraInicio == null || fechaHoraFinal == null) return false;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(fechaHoraInicio) || now.isAfter(fechaHoraFinal)) return false;

        // Verifica rango del grupo (si se dispone de sus fechas)
        if (grupo != null) {
            LocalDate hoy = now.toLocalDate();
            if (grupo.getFechaInicio() != null && hoy.isBefore(grupo.getFechaInicio())) return false;
            if (grupo.getFechaFinal() != null && hoy.isAfter(grupo.getFechaFinal()))   return false;
        }
        return true;
    }

    /** Puede desasociarse si la fecha/hora de inicio aún no ha llegado. */
    public boolean canDesasociar() {
        return fechaHoraInicio != null && fechaHoraInicio.isAfter(LocalDateTime.now());
    }

    // Getters
    public Evaluacion getEvaluacion() { return evaluacion; }
    public Grupo getGrupo() { return grupo; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public LocalDateTime getFechaHoraFinal() { return fechaHoraFinal; }

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
