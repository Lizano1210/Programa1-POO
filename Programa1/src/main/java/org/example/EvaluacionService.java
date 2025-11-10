package org.example;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio que define las operaciones para la gestión de evaluaciones.
 * <p>
 * Permite crear, modificar, eliminar y asociar evaluaciones con grupos,
 * así como gestionar sus preguntas. Está pensado para uso por parte de profesores.
 * </p>
 */
public interface EvaluacionService {

    // -- Consultas básicas --

    /**
     * Lista todas las evaluaciones creadas por un profesor.
     *
     * @param idProfesor identificador del profesor
     * @return lista de evaluaciones asociadas a ese profesor
     */
    List<Evaluacion> listarPorProfesor(String idProfesor);

    // -- Gestión de evaluaciones --

    /**
     * Crea una nueva evaluación y la asocia al profesor.
     * <p>
     * Valida los datos de la evaluación y asigna un identificador autoincremental.
     * </p>
     *
     * @param idProfesor identificador del profesor creador
     * @param evaluacion evaluación a crear
     * @return evaluación creada
     */
    Evaluacion crear(String idProfesor, Evaluacion evaluacion);

    /**
     * Actualiza una evaluación existente si puede modificarse.
     *
     * @param idProfesor identificador del profesor
     * @param evaluacion evaluación con los nuevos datos
     */
    void actualizar(String idProfesor, Evaluacion evaluacion);

    /**
     * Elimina una evaluación del profesor, siempre que sea modificable.
     *
     * @param idProfesor identificador del profesor
     * @param idEvaluacion identificador de la evaluación
     */
    void eliminar(String idProfesor, int idEvaluacion);

    // -- Gestión de preguntas --

    /**
     * Agrega una pregunta a una evaluación específica.
     *
     * @param idProfesor identificador del profesor
     * @param idEvaluacion identificador de la evaluación
     * @param pregunta pregunta a agregar
     */
    void agregarPregunta(String idProfesor, int idEvaluacion, IPregunta pregunta);

    /**
     * Actualiza una pregunta dentro de una evaluación.
     *
     * @param idProfesor identificador del profesor
     * @param idEvaluacion identificador de la evaluación
     * @param index posición de la pregunta a actualizar
     * @param preguntaActualizada objeto con los nuevos datos de la pregunta
     */
    void actualizarPregunta(String idProfesor, int idEvaluacion, int index, IPregunta preguntaActualizada);

    /**
     * Elimina una pregunta de una evaluación.
     *
     * @param idProfesor identificador del profesor
     * @param idEvaluacion identificador de la evaluación
     * @param index posición de la pregunta a eliminar
     */
    void eliminarPregunta(String idProfesor, int idEvaluacion, int index);

    // -- Asignación a grupos --

    /**
     * Asocia una evaluación a un grupo específico de un curso.
     * <p>
     * Calcula automáticamente la fecha/hora de finalización
     * según la duración de la evaluación.
     * </p>
     *
     * @param idProfesor identificador del profesor
     * @param idEvaluacion identificador de la evaluación
     * @param curso curso al que pertenece el grupo
     * @param idGrupo identificador del grupo
     * @param fechaHoraInicio fecha y hora de inicio de la evaluación
     */
    void asociarAGrupo(String idProfesor, int idEvaluacion, Curso curso, int idGrupo, LocalDateTime fechaHoraInicio);

    /**
     * Desasocia una evaluación de un grupo si la fecha/hora de inicio aún no ha ocurrido.
     * <p>
     * Aplica las reglas definidas por {@link Evaluacion#canDesasociar()}.
     * </p>
     *
     * @param idProfesor identificador del profesor
     * @param idEvaluacion identificador de la evaluación
     * @param curso curso al que pertenece el grupo
     * @param idGrupo identificador del grupo
     */
    void desasociarDeGrupo(String idProfesor, int idEvaluacion, Curso curso, int idGrupo);
}
