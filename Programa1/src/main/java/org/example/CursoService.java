package org.example;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrato del módulo de Cursos y Grupos.
 * <p>
 * Define las operaciones principales para la gestión de cursos
 * y sus grupos asociados, incluyendo creación, edición, eliminación
 * y asignación de profesores.
 * </p>
 */
public interface CursoService {

    // -- Cursos --

    /**
     * Lista todos los cursos registrados en el sistema.
     *
     * @return lista de cursos disponibles
     */
    List<Curso> listarCursos();

    /**
     * Agrega un nuevo curso al sistema.
     *
     * @param c curso a agregar
     */
    void agregarCurso(Curso c);

    /**
     * Actualiza los datos de un curso existente.
     *
     * @param c curso con los datos actualizados
     */
    void actualizarCurso(Curso c);

    /**
     * Elimina un curso según su identificador.
     *
     * @param idCurso identificador del curso a eliminar
     */
    void eliminarCurso(String idCurso);

    // -- Grupos --

    /**
     * Obtiene los grupos asociados a un curso.
     *
     * @param curso curso del cual se listarán los grupos
     * @return lista de grupos del curso
     */
    List<Grupo> listarGrupos(Curso curso);

    /**
     * Crea un nuevo grupo dentro de un curso.
     *
     * @param curso curso al que pertenece el grupo
     * @param inicio fecha de inicio
     * @param fin fecha de finalización
     * @return grupo creado
     */
    Grupo crearGrupo(Curso curso, LocalDate inicio, LocalDate fin);

    /**
     * Actualiza las fechas de un grupo existente.
     *
     * @param curso curso al que pertenece el grupo
     * @param idGrupo identificador del grupo
     * @param inicio nueva fecha de inicio
     * @param fin nueva fecha de finalización
     */
    void actualizarGrupoFechas(Curso curso, int idGrupo, LocalDate inicio, LocalDate fin);

    /**
     * Asigna un profesor responsable a un grupo.
     *
     * @param curso curso al que pertenece el grupo
     * @param idGrupo identificador del grupo
     * @param profesor profesor asignado
     */
    void asignarProfesor(Curso curso, int idGrupo, Profesor profesor);

    /**
     * Elimina un grupo de un curso.
     *
     * @param curso curso al que pertenece el grupo
     * @param idGrupo identificador del grupo
     */
    void eliminarGrupo(Curso curso, int idGrupo);
}
