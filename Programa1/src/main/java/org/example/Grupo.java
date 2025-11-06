package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 Representa un grupo específico de un curso con fechas definidas, gestiona la matrícula
 de estudiantes y asignación de evaluaciones.
 */
public class Grupo {
    // Atributos
    private Curso curso;
    private int idGrupo;
    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private Profesor profesor;
    private List<Matricula> matriculas;
    private List<EvaluacionAsignada> evaluacionesAsignadas;
    // Contador  para generar IDs únicos por curso
    private static int contadorId = 0;

    // Constructor
    public Grupo(Curso curso, LocalDate fechaInicio, LocalDate fechaFinal) {
        this.curso = curso;
        this.idGrupo = ++contadorId; // Genera ID automáticamente
        this.fechaInicio = fechaInicio;
        this.fechaFinal = fechaFinal;
        this.profesor = null; // Se asigna después
        this.matriculas = new ArrayList<>();
        this.evaluacionesAsignadas = new ArrayList<>();
    }

    // Getters
    public Curso getCurso() {
        return curso;
    }
    public int getIdGrupo() {
        return idGrupo;
    }
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
    public LocalDate getFechaFinal() {
        return fechaFinal;
    }
    public Profesor getProfesor() {
        return profesor;
    }
    public List<Matricula> getMatriculas() {
        return matriculas;
    }
    public List<EvaluacionAsignada> getEvaluacionesAsignadas() {
        return evaluacionesAsignadas;
    }

    // Setters
    public void setCurso(Curso curso) {
        this.curso = curso;
    }
    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    public void setFechaFinal(LocalDate fechaFinal) {
        this.fechaFinal = fechaFinal;
    }
    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    // Métodos principales

    /**
     * Agrega una matrícula (estudiante) al grupo.
     * Valida que el grupo no esté lleno antes de agregar.
     *
     * @param matricula La matrícula a agregar
     * @return true si se agregó correctamente, false si el grupo está lleno
     * @throws IllegalArgumentException si la matrícula es null
     */
    public boolean agregarMatricula(Matricula matricula) {
        if (matricula == null) {
            throw new IllegalArgumentException("La matrícula no puede ser null");
        }

        // Verificar si el grupo está lleno
        if (!validarCapacidad()) {
            System.out.println("No se puede agregar: El grupo está lleno");
            return false;
        }

        // Verificar que la matrícula sea para este grupo
        if (matricula.getGrupo() != this) {
            System.out.println("Error: La matrícula no corresponde a este grupo");
            return false;
        }

        // Agregar la matrícula
        matriculas.add(matricula);
        System.out.println("Matrícula agregada exitosamente. Total estudiantes: " + matriculas.size());
        return true;
    }

    /**
     * Verifica si el grupo tiene capacidad disponible.
     *
     * @return true si hay espacio disponible, false si está lleno
     */
    public boolean validarCapacidad() {
        int maxEstudiantes = curso.getMaxEstu();
        int estudiantesActuales = matriculas.size();

        return estudiantesActuales < maxEstudiantes;
    }

    /**
     * Obtiene la cantidad de cupos disponibles en el grupo.
     *
     * @return Número de cupos disponibles
     */
    public int obtenerCuposDisponibles() {
        return curso.getMaxEstu() - matriculas.size();
    }

    /**
     * Verifica si el grupo puede abrirse (tiene el mínimo de estudiantes).
     *
     * @return true si tiene al menos el mínimo de estudiantes, false en caso contrario
     */
    public boolean puedeAbrirse() {
        int minEstudiantes = curso.getMinEstu();
        int estudiantesActuales = matriculas.size();

        return estudiantesActuales >= minEstudiantes;
    }

    /**
     * Valida si el grupo está vigente en una fecha específica.
     * Un grupo es vigente si la fecha dada es menor o igual a su fecha de finalización.
     *
     * @param fecha La fecha a verificar
     * @return true si el grupo está vigente en esa fecha, false en caso contrario
     */
    public boolean esVigente(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser null");
        }

        // El grupo es vigente si la fecha de finalización es >= a la fecha dada
        return fechaFinal.isAfter(fecha) || fechaFinal.isEqual(fecha);
    }

    /**
     * Verifica si el grupo está actualmente vigente (usando la fecha del sistema).
     *
     * @return true si el grupo está vigente hoy, false en caso contrario
     */
    public boolean esVigenteHoy() {
        return esVigente(LocalDate.now());
    }

    /**
     * Verifica si el grupo ya ha iniciado.
     *
     * @return true si la fecha actual es >= a la fecha de inicio
     */
    public boolean haIniciado() {
        LocalDate hoy = LocalDate.now();
        return hoy.isAfter(fechaInicio) || hoy.isEqual(fechaInicio);
    }

    /**
     * Asocia una evaluación al grupo con una fecha y hora de inicio específica.
     *
     * @param evaluacion La evaluación a asignar
     * @param fechaHoraInicio Fecha y hora en que inicia la evaluación
     * @return true si se asignó correctamente, false en caso contrario
     */
    public boolean asignarEvaluacion(Evaluacion evaluacion, LocalDateTime fechaHoraInicio) {
        if (evaluacion == null) {
            throw new IllegalArgumentException("La evaluación no puede ser null");
        }

        if (fechaHoraInicio == null) {
            throw new IllegalArgumentException("La fecha y hora de inicio no pueden ser null");
        }

        // TODO: Cuando se implemente EvaluacionAsignada, descomentar esto:
        /*
        // Crear la evaluación asignada
        EvaluacionAsignada evaluacionAsignada = new EvaluacionAsignada(evaluacion, this, fechaHoraInicio);

        // Agregar a la lista
        evaluacionesAsignadas.add(evaluacionAsignada);

        System.out.println("Evaluación asignada exitosamente al grupo " + idGrupo);
        return true;
        */

        // Por ahora, solo un placeholder
        System.out.println("Método asignarEvaluacion: pendiente de implementar EvaluacionAsignada");
        return false;
    }

    /**
     * Obtiene todas las evaluaciones asignadas al grupo.
     *
     * @return Lista de evaluaciones asignadas (puede estar vacía)
     */
    public List<EvaluacionAsignada> obtenerEvaluacionesAsignadas() {
        return new ArrayList<>(evaluacionesAsignadas); // Retorna copia
    }

    /**
     * Obtiene la cantidad de estudiantes matriculados en el grupo.
     *
     * @return Número de estudiantes matriculados
     */
    public int obtenerCantidadEstudiantes() {
        return matriculas.size();
    }

    /**
     * Busca una matrícula por el ID del estudiante.
     *
     * @param idEstudiante ID del estudiante a buscar
     * @return La matrícula encontrada, o null si no existe
     */
    public Matricula buscarMatriculaPorEstudiante(String idEstudiante) {
        for (Matricula m : matriculas) {
            if (m.getEstudiante().getIdUsuario().equals(idEstudiante)) {
                return m;
            }
        }
        return null;
    }

    // toString
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

    /**
     * Valida que los datos del grupo sean correctos.
     *
     * @return true si todos los datos son válidos, false en caso contrario
     */
    public boolean validarDatos() {
        if (curso == null) return false;
        if (idGrupo < 1) return false;
        if (fechaInicio == null || fechaFinal == null) return false;

        // La fecha final debe ser después de la fecha de inicio
        if (fechaFinal.isBefore(fechaInicio) || fechaFinal.isEqual(fechaInicio)) {
            return false;
        }

        return true;
    }

    /**
     * Reinicia el contador de IDs (útil para testing o al cargar datos).
     */
    public static void reiniciarContador() {
        contadorId = 0;
    }

    /**
     * Establece el contador en un valor específico (útil al cargar datos).
     *
     * @param valor El valor inicial del contador
     */
    public static void setContador(int valor) {
        contadorId = valor;
    }
}
