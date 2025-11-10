package org.example;

/**
 * Enum que define los diferentes tipos de preguntas que pueden incluirse
 * en una evaluación dentro del sistema.
 * <p>
 * Cada tipo determina la forma de presentación y validación de las respuestas.
 * </p>
 */
public enum TipoPregunta {

    // -- Tipos de pregunta --

    /** Pregunta con una única opción correcta. */
    SELECCION_UNICA,

    /** Pregunta con varias opciones correctas posibles. */
    SELECCION_MULTIPLE,

    /** Pregunta de verdadero o falso. */
    VERDADERO_FALSO,

    /** Pregunta de tipo pareo, donde se deben asociar elementos entre dos listas. */
    PAREO,

    /** Pregunta basada en una sopa de letras, para encontrar palabras clave. */
    SOPA_LETRAS
}
