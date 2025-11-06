package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Matricula {
    private Estudiante estudiante;
    private Grupo grupo;
    private LocalDate fechaMatricula;
    private double calificacionFinal;
    private List<IntentoEvaluacion> intentosEvaluacion;

    public Matricula(Estudiante estudiante, Grupo grupo) {
        this.estudiante = estudiante;
        this.grupo = grupo;
        this.fechaMatricula = LocalDate.now();
        this.calificacionFinal = 0.0;
        this.intentosEvaluacion = new ArrayList<>();
    }

    public double calcularCalificacionFinal() {
        if (intentosEvaluacion.isEmpty()) {
            return 0.0;
        }
        double suma = 0.0;
        for (IntentoEvaluacion intento : intentosEvaluacion) {
            suma += intento.getCalificacion();
        }
        calificacionFinal = suma / intentosEvaluacion.size();
        return calificacionFinal;
    }

    public void agregarIntento(IntentoEvaluacion intento) {
        intentosEvaluacion.add(intento);
        calcularCalificacionFinal();
    }

    public List<IntentoEvaluacion> obtenerIntentosEvaluacion() {
        return new ArrayList<>(intentosEvaluacion);
    }

    // Getters y setters
    public Estudiante getEstudiante() {
        return estudiante;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public LocalDate getFechaMatricula() {
        return fechaMatricula;
    }

    public double getCalificacionFinal() {
        return calificacionFinal;
    }
}
