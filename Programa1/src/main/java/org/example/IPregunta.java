package org.example;

/**
 * Interfaz que define el comportamiento general de una pregunta dentro de una evaluación.
 * <p>
 * Permite obtener la información básica de la pregunta, validar sus datos,
 * y calcular la calificación de una respuesta específica.
 * </p>
 */
public interface IPregunta {

    // -- Información básica --

    /**
     * Devuelve el puntaje total asignado a la pregunta.
     *
     * @return puntos que vale la pregunta
     */
    int obtenerPuntos();

    /**
     * Devuelve la descripción o enunciado de la pregunta.
     *
     * @return texto de la pregunta
     */
    String obtenerDescripcion();

    /**
     * Obtiene el tipo de pregunta (por ejemplo: selección única, múltiple, abierta, etc.).
     *
     * @return tipo de pregunta definido en {@link TipoPregunta}
     */
    TipoPregunta getTipo();

    // -- Validación y calificación --

    /**
     * Verifica que la pregunta tenga datos válidos.
     *
     * @return {@code true} si la pregunta está correctamente definida
     */
    boolean validarDatos();

    /**
     * Calcula el puntaje obtenido por una respuesta específica del estudiante.
     *
     * @param respuesta respuesta del estudiante a esta pregunta
     * @return puntos obtenidos según la corrección de la respuesta
     */
    int calificar(RespuestaEstudiante respuesta);
}
