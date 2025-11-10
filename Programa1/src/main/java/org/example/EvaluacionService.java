package org.example;

import java.time.LocalDateTime;
import java.util.List;

public interface EvaluacionService {

    /** Lista todas las evaluaciones creadas por un profesor. */
    List<Evaluacion> listarPorProfesor(String idProfesor);

    /** Crea una evaluación (valida datos) y la asocia al profesor; asigna id autoincremental. */
    Evaluacion crear(String idProfesor, Evaluacion evaluacion);

    /** Actualiza una evaluación del profesor (si puede modificarse). */
    void actualizar(String idProfesor, Evaluacion evaluacion);

    /** Elimina una evaluación del profesor (si puede modificarse). */
    void eliminar(String idProfesor, int idEvaluacion);

    // ------- Gestión de preguntas -------
    void agregarPregunta(String idProfesor, int idEvaluacion, IPregunta pregunta);
    void actualizarPregunta(String idProfesor, int idEvaluacion, int index, IPregunta preguntaActualizada);
    void eliminarPregunta(String idProfesor, int idEvaluacion, int index);

    // ------- Asignación a grupos -------
    /** Asocia a un grupo (del curso indicado) con fecha/hora de inicio; calcula fecha final con la duración. */
    void asociarAGrupo(String idProfesor, int idEvaluacion, Curso curso, int idGrupo, LocalDateTime fechaHoraInicio);

    /** Desasocia de un grupo si la ventana aún no inicia (o según regla canDesasociar). */
    void desasociarDeGrupo(String idProfesor, int idEvaluacion, Curso curso, int idGrupo);
}
