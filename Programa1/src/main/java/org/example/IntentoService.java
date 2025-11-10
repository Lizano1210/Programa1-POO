package org.example;

import java.util.List;

/**
 * Servicio encargado de gestionar los intentos de evaluación de los estudiantes.
 * <p>
 * Permite guardar nuevos intentos, consultar intentos por estudiante, grupo o evaluación,
 * y obtener el listado completo de intentos registrados.
 * </p>
 */
public interface IntentoService {

    // -- Operaciones principales --

    /**
     * Guarda un nuevo intento de evaluación.
     *
     * @param intento intento de evaluación a registrar
     */
    void guardar(IntentoEvaluacion intento);

    /**
     * Lista todos los intentos realizados por un estudiante.
     *
     * @param idEstudiante identificador del estudiante
     * @return lista de intentos del estudiante
     */
    List<IntentoEvaluacion> listarPorEstudiante(String idEstudiante);

    /**
     * Lista todos los intentos realizados por los estudiantes de un grupo.
     *
     * @param idGrupo identificador del grupo
     * @return lista de intentos asociados al grupo
     */
    List<IntentoEvaluacion> listarPorGrupo(int idGrupo);

    /**
     * Lista todos los intentos asociados a una evaluación específica.
     *
     * @param idEvaluacion identificador de la evaluación
     * @return lista de intentos relacionados con la evaluación
     */
    List<IntentoEvaluacion> listarPorEvaluacion(int idEvaluacion);

    /**
     * Devuelve todos los intentos registrados en el sistema.
     *
     * @return lista completa de intentos
     */
    List<IntentoEvaluacion> listarTodos();
}
