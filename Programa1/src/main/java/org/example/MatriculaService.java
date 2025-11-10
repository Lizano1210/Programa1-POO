package org.example;

import java.util.List;

/**
 * Servicio que define las operaciones para la gestión de matrículas de estudiantes.
 * <p>
 * Permite matricular y desmatricular estudiantes, listar sus grupos y obtener detalles
 * de las matrículas registradas. Esta interfaz actúa como contrato para las implementaciones
 * de persistencia o manejo en memoria.
 * </p>
 */
public interface MatriculaService {

    // -- Consultas --

    /**
     * Obtiene la lista de grupos en los que está matriculado un estudiante.
     *
     * @param idEstudiante identificador único del estudiante
     * @return lista de grupos donde está inscrito
     */
    List<Grupo> gruposDelEstudiante(String idEstudiante);

    /**
     * Lista las matrículas completas de un estudiante, incluyendo detalles
     * como fechas, calificaciones e intentos de evaluación.
     *
     * @param idEstudiante identificador único del estudiante
     * @return lista de matrículas asociadas al estudiante
     */
    List<Matricula> listarMatriculas(String idEstudiante);

    // -- Operaciones de matrícula --

    /**
     * Intenta matricular al estudiante en un grupo específico.
     * <p>
     * La implementación debe validar que el grupo tenga cupo disponible,
     * que el estudiante no esté ya matriculado, y que las fechas del grupo sean válidas.
     * </p>
     *
     * @param idEstudiante identificador del estudiante
     * @param curso curso al que pertenece el grupo
     * @param idGrupo identificador del grupo
     * @return {@code true} si la matrícula se realizó con éxito
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws IllegalStateException si la matrícula no puede realizarse
     */
    boolean matricular(String idEstudiante, Curso curso, int idGrupo);

    /**
     * Intenta desmatricular al estudiante de un grupo.
     * <p>
     * La implementación debe asegurarse de que el estudiante esté matriculado
     * y que el grupo permita cancelaciones según sus fechas.
     * </p>
     *
     * @param idEstudiante identificador del estudiante
     * @param curso curso del que se desea retirar
     * @param idGrupo identificador del grupo
     * @return {@code true} si la desmatriculación fue exitosa
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws IllegalStateException si no puede realizarse la desmatriculación
     */
    boolean desmatricular(String idEstudiante, Curso curso, int idGrupo);
}
