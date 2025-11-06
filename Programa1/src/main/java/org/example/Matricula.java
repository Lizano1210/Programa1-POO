package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Matricula {
    private Estudiante estudiante;
    private Grupo grupo;
    private LocalDate fechaMatricula;
    private double calificacionFinal; // 0 - 100
    private final List<IntentoEvaluacion> intentosEvaluacion = new ArrayList<>();

    public Matricula(Estudiante estudiante, Grupo grupo) {
        this.estudiante = estudiante;
        this.grupo = grupo;
        this.fechaMatricula = LocalDate.now() ;
        this.calificacionFinal = 0.0;
    }

    // Getters y Setters
    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public LocalDate getFechaMatricula() {
        return fechaMatricula;
    }

    public void setFechaMatricula(LocalDate fechaMatricula) {
        this.fechaMatricula = fechaMatricula;
    }

    public double getCalificacionFinal() {
        return calificacionFinal;
    }

    public List<IntentoEvaluacion> obtenerIntentosEvaluacion() {
        return Collections.unmodifiableList(intentosEvaluacion);
    }

    // Añadir un intento y recalcular nota final
    public void agregarIntento(IntentoEvaluacion intento) {
        if (intento == null) {
            throw new IllegalArgumentException("El intento no puede ser null");
        }
        intentosEvaluacion.add(intento);
        calcularCalificacionFinal();
    }

    // Calcula la nota final como promedio de todos los intentos
    public double calcularCalificacionFinal() {
        if (intentosEvaluacion.isEmpty()) {
            calificacionFinal = 0.0;
        } else {
            double suma = 0.0;
            for (IntentoEvaluacion i : intentosEvaluacion) {
                suma += i.getCalificacion(); // Usa calificación del intento
            }
            calificacionFinal = clampCalificacion(suma / intentosEvaluacion.size());
        }
        return calificacionFinal;
    }

    // Limitar 0-100
    private double clampCalificacion(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            return 0.0;
        }
        if (valor < 0.0) return 0.0;
        if (valor > 100.0) return 100.0;
        return valor;
    }
}

