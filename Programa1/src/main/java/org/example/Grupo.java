package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un grupo de un curso.
 * <p>
 * Cada grupo tiene un rango de fechas, un profesor asignado, una lista de estudiantes matriculados
 * y las evaluaciones asociadas. Permite verificar capacidad, vigencia y gestionar matrículas.
 * </p>
 */
public class Grupo {

    // -- Atributos --

    /** Curso al que pertenece el grupo. */
    private Curso curso;

    /** Identificador único del grupo. */
    private int idGrupo;

    /** Fecha de inicio del grupo. */
    private LocalDate fechaInicio;

    /** Fecha de finalización del grupo. */
    private LocalDate fechaFinal;

    /** Profesor asignado al grupo. */
    private Profesor profesor;

    /** Lista de matrículas del grupo. */
    private List<Matricula> matriculas;

    /** Lista de evaluaciones asignadas al grupo. */
    private List<EvaluacionAsignada> evaluacionesAsignadas;

    /** Contador interno para generar IDs únicos. */
    private static int contadorId = 0;

    // -- Constructor --

    /**
     * Crea un nuevo grupo para un curso con fechas definidas.
     *
     * @param curso curso al que pertenece el grupo
     * @param fechaInicio fecha de inicio del grupo
     * @param fechaFinal fecha de finalización del grupo
     */
    public Grupo(Curso curso, LocalDate fechaInicio, LocalDate fechaFinal) {
        this.curso = curso;
        this.idGrupo = ++contadorId;
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
        this.profesor = null;
        this.matriculas = new ArrayList<>();
        this.evaluacionesAsignadas = new ArrayList<>();
    }

    // -- Getters --

    public Curso getCurso() { return curso; }
    public int getIdGrupo() { return idGrupo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFinal() { return fechaFinal; }
    public Profesor getProfesor() { return profesor; }
    public List<Matricula> getMatriculas() { return matriculas; }
    public List<EvaluacionAsignada> getEvaluacionesAsignadas() { return evaluacionesAsignadas; }

    // -- Setters --

    public void setCurso(Curso curso) { this.curso = curso; }
    public void setIdGrupo(int idGrupo) { this.idGrupo = idGrupo; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFechaFinal(LocalDate fechaFinal) { this.fechaFinal = fechaFinal; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }
    public void setEvaluacionesAsignadas(List<EvaluacionAsignada> nuevasEval) { this.evaluacionesAsignadas = nuevasEval; }
    public void setMatriculas(List<Matricula> nuevaLista) { this.matriculas = nuevaLista; }

    // -- Métodos principales --

    /**
     * Agrega una matrícula al grupo si tiene cupo disponible.
     *
     * @param matricula matrícula a agregar
     * @return {@code true} si se agregó correctamente, {@code false} si el grupo está lleno o no corresponde
     * @throws IllegalArgumentException si la matrícula es {@code null}
     */
    public boolean agregarMatricula(Matricula matricula) {
        if (matricula == null) throw new IllegalArgumentException("La matrícula no puede ser null");

        if (!validarCapacidad()) {
            System.out.println("No se puede agregar: el grupo está lleno.");
            return false;
        }

        if (matricula.getGrupo() != this) {
            System.out.println("Error: la matrícula no corresponde a este grupo.");
            return false;
        }

        matriculas.add(matricula);
        System.out.println("Matrícula agregada. Total estudiantes: " + matriculas.size());
        return true;
    }

    /**
     * Verifica si el grupo tiene espacio disponible según el máximo de estudiantes del curso.
     *
     * @return {@code true} si hay cupo disponible, {@code false} si está lleno
     */
    public boolean validarCapacidad() {
        int maxEstudiantes = curso.getMaxEstu();
        return matriculas.size() < maxEstudiantes;
    }

    /**
     * Calcula cuántos cupos quedan disponibles en el grupo.
     *
     * @return cantidad de cupos libres
     */
    public int obtenerCuposDisponibles() {
        return curso.getMaxEstu() - matriculas.size();
    }

    /**
     * Indica si el grupo puede abrirse según el mínimo de estudiantes requerido.
     *
     * @return {@code true} si cumple el mínimo, {@code false} en caso contrario
     */
    public boolean puedeAbrirse() {
        return matriculas.size() >= curso.getMinEstu();
    }

    // -- Estado del grupo --

    /**
     * Indica si el grupo está vigente en una fecha específica.
     *
     * @param fecha fecha a verificar
     * @return {@code true} si está vigente en esa fecha
     */
    public boolean esVigente(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha no puede ser null");
        return fechaFinal.isAfter(fecha) || fechaFinal.isEqual(fecha);
    }

    /**
     * Indica si el grupo está vigente hoy (fecha actual).
     *
     * @return {@code true} si el grupo sigue vigente
     */
    public boolean esVigenteHoy() {
        return esVigente(LocalDate.now());
    }

    /**
     * Verifica si el grupo ya ha iniciado según la fecha actual.
     *
     * @return {@code true} si ya comenzó
     */
    public boolean haIniciado() {
        LocalDate hoy = LocalDate.now();
        return hoy.isAfter(fechaInicio) || hoy.isEqual(fechaInicio);
    }

    // -- Evaluaciones --

    /**
     * Asocia una evaluación al grupo con una fecha y hora de inicio específica.
     *
     * @param evaluacion evaluación a asignar
     * @param fechaHoraInicio fecha y hora de inicio
     * @return {@code true} si se asignó correctamente
     */
    public boolean asignarEvaluacion(Evaluacion evaluacion, LocalDateTime fechaHoraInicio) {
        if (evaluacion == null) throw new IllegalArgumentException("La evaluación no puede ser null");
        if (fechaHoraInicio == null) throw new IllegalArgumentException("La fecha y hora no pueden ser null");

        // TODO: Cuando se implemente EvaluacionAsignada, habilitar esta parte:
        /*
        EvaluacionAsignada ea = new EvaluacionAsignada(evaluacion, this, fechaHoraInicio);
        evaluacionesAsignadas.add(ea);
        System.out.println("Evaluación asignada exitosamente al grupo " + idGrupo);
        return true;
        */
        System.out.println("Método asignarEvaluacion: pendiente de implementación EvaluacionAsignada");
        return false;
    }

    /**
     * Obtiene una copia de las evaluaciones asignadas al grupo.
     *
     * @return lista de evaluaciones asignadas
     */
    public List<EvaluacionAsignada> obtenerEvaluacionesAsignadas() {
        return new ArrayList<>(evaluacionesAsignadas);
    }

    // -- Consultas sobre estudiantes --

    /**
     * Devuelve la cantidad actual de estudiantes matriculados.
     *
     * @return número de estudiantes en el grupo
     */
    public int obtenerCantidadEstudiantes() {
        return matriculas.size();
    }

    /**
     * Busca una matrícula por el ID del estudiante.
     *
     * @param idEstudiante identificador del estudiante
     * @return matrícula correspondiente, o {@code null} si no existe
     */
    public Matricula buscarMatriculaPorEstudiante(String idEstudiante) {
        for (Matricula m : matriculas) {
            if (m.getEstudiante().getIdUsuario().equals(idEstudiante)) return m;
        }
        return null;
    }

    // -- Validaciones y utilidades --

    /**
     * Verifica que los datos del grupo sean válidos.
     *
     * @return {@code true} si todos los datos son correctos
     */
    public boolean validarDatos() {
        if (curso == null || idGrupo < 1 || fechaInicio == null || fechaFinal == null) return false;
        return fechaFinal.isAfter(fechaInicio);
    }

    /** Reinicia el contador de IDs del grupo (útil para pruebas). */
    public static void reiniciarContador() { contadorId = 0; }

    /**
     * Define manualmente el contador de IDs (para restaurar desde datos guardados).
     *
     * @param valor nuevo valor del contador
     */
    public static void setContador(int valor) { contadorId = valor; }

    // -- Representación --

    @Override
    public String toString() {
        return String.format("Grupo %d \nCurso: %s \n%s a %s \nProfesor: %s \nEstudiantes: %d/%d \nEstado: %s",
                idGrupo,
                curso.getNombre(),
                fechaInicio,
                fechaFinal,
                profesor != null ? profesor.obtenerNombreCompleto() : "Sin asignar",
                matriculas.size(),
                curso.getMaxEstu(),
                esVigenteHoy() ? "Vigente" : "Finalizado");
    }
}
