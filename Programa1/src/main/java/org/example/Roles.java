package org.example;

/**
 * Enum que define los roles principales de los usuarios dentro del sistema.
 * <p>
 * Cada rol determina el tipo de acceso y las acciones disponibles en la aplicación:
 * <ul>
 *     <li>{@link #ADMIN} – Tiene acceso completo a todas las funciones del sistema.</li>
 *     <li>{@link #PROFESOR} – Gestiona cursos, evaluaciones y seguimiento de estudiantes.</li>
 *     <li>{@link #ESTUDIANTE} – Realiza evaluaciones y consulta sus resultados.</li>
 * </ul>
 * </p>
 */
public enum Roles {

    // -- Roles definidos --

    /** Rol de administrador del sistema. */
    ADMIN,

    /** Rol asignado a los estudiantes. */
    ESTUDIANTE,

    /** Rol asignado a los profesores. */
    PROFESOR
}
