package org.example;

/**
 * Enum que representa las modalidades posibles en las que se puede impartir un curso.
 * <p>
 * Define si el curso se realiza de manera presencial, virtual o combinada.
 * </p>
 */
public enum TipoModalidad {

    // -- Tipos de modalidad --

    /** Curso impartido completamente de forma presencial. */
    PRESENCIAL,

    /** Curso virtual en el que el estudiante trabaja de manera asincrónica. */
    VIRTUAL_ASINCRONICO,

    /** Curso virtual con clases o actividades en tiempo real. */
    VIRTUAL_SINCRONICO,

    /** Curso con una mezcla de sesiones presenciales y virtuales. */
    VIRTUAL_HIBRIDO,

    /** Curso semipresencial, con alternancia entre clases presenciales y en línea. */
    SEMIPRESENCIAL
}
