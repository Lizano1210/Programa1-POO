package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa la matrícula de un estudiante en un grupo.
 * <p>
 * Contiene la información del estudiante, el grupo al que pertenece,
 * la fecha de matrícula, la calificación final y los intentos de evaluación realizados.
 * </p>
 */
public class Matricula {

    // -- Atributos --

    /** Estudiante asociado a la matrícula. */
    private Estudiante estudiante;

    /** Grupo en el que el estudiante está matriculado. */
    private Grupo grupo;

    /** Fecha en que se realizó la matrícula. */
    private LocalDate fechaMatricula;

    /** Calificación final promedio del estudiante en el curso (0–100). */
    private double calificacionFinal;

    /** Lista de intentos de evaluación asociados a esta matrícula. */
    private final List<IntentoEvaluacion> intentosEvaluacion = new ArrayList<>();

    // -- Constructor --

    /**
     * Crea una nueva matrícula para un estudiante y un grupo.
     * <p>
     * La fecha de matrícula se establece automáticamente con la fecha actual
     * y la calificación inicial es 0.
     * </p>
     *
     * @param estudiante estudiante matriculado
     * @param grupo grupo al que se inscribe
     */
    public Matricula(Estudiante estudiante, Grupo grupo) {
        this.estudiante = estudiante;
        this.grupo = grupo;
        this.fechaMatricula = LocalDate.now();
        this.calificacionFinal = 0.0;
    }

    // -- Getters y Setters --

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public Grupo getGrupo() { return grupo; }
    public void setGrupo(Grupo grupo) { this.grupo = grupo; }

    public LocalDate getFechaMatricula() { return fechaMatricula; }
    public void setFechaMatricula(LocalDate fechaMatricula) { this.fechaMatricula = fechaMatricula; }

    public double getCalificacionFinal() { return calificacionFinal; }
    public void setCalificacionFinal(double nota) { this.calificacionFinal = nota; }

    /**
     * Devuelve la lista de intentos de evaluación realizados.
     *
     * @return lista inmodificable de intentos
     */
    public List<IntentoEvaluacion> obtenerIntentosEvaluacion() {
        return Collections.unmodifiableList(intentosEvaluacion);
    }

    // -- Registro de intentos --

    /**
     * Agrega un intento de evaluación a la matrícula y recalcula la nota final.
     *
     * @param intento intento de evaluación a agregar
     * @throws IllegalArgumentException si el intento es {@code null}
     */
    public void agregarIntento(IntentoEvaluacion intento) {
        if (intento == null) {
            throw new IllegalArgumentException("El intento no puede ser null");
        }
        intentosEvaluacion.add(intento);
        calcularCalificacionFinal();
    }

    // -- Cálculo de nota final --

    /**
     * Calcula la calificación final como el promedio de las calificaciones de todos los intentos.
     * <p>
     * Si no existen intentos, la calificación se establece en 0.
     * </p>
     *
     * @return calificación final calculada (0–100)
     */
    public double calcularCalificacionFinal() {
        if (intentosEvaluacion.isEmpty()) {
            calificacionFinal = 0.0;
        } else {
            double suma = 0.0;
            for (IntentoEvaluacion i : intentosEvaluacion) {
                suma += i.getCalificacion();
            }
            calificacionFinal = clampCalificacion(suma / intentosEvaluacion.size());
        }
        return calificacionFinal;
    }

    // -- Auxiliar --

    /**
     * Asegura que la calificación esté dentro del rango válido (0–100).
     *
     * @param valor valor a validar
     * @return valor ajustado dentro del rango
     */
    private double clampCalificacion(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) return 0.0;
        if (valor < 0.0) return 0.0;
        if (valor > 100.0) return 100.0;
        return valor;
    }
}

