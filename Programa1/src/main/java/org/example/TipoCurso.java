package org.example;

/**
 * Enum que representa los distintos tipos de curso disponibles en el sistema.
 * <p>
 * Cada tipo define el enfoque principal del curso y puede afectar
 * la forma en que se planifican las actividades o evaluaciones.
 * </p>
 */
public enum TipoCurso {

    // -- Tipos de curso --

    /** Curso enfocado en la teoría y la exposición de conceptos. */
    TEORICO,

    /** Curso con enfoque práctico o de laboratorio. */
    PRACTICO,

    /** Curso de taller, generalmente orientado a proyectos o ejercicios guiados. */
    TALLER,

    /** Curso tipo seminario, con participación activa y discusión de temas específicos. */
    SEMINARIO
}
