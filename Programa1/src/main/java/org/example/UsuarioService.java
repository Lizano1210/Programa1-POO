package org.example;

import java.util.List;

/**
 * Interfaz que define el contrato del servicio de usuarios utilizado
 * por el módulo administrativo del sistema.
 * <p>
 * Permite realizar operaciones básicas de gestión sobre los usuarios
 * del sistema, tales como listar, agregar, actualizar y eliminar
 * tanto estudiantes como profesores.
 * </p>
 * <p>
 * Las implementaciones pueden variar según la fuente de datos
 * (por ejemplo, en memoria, base de datos o servicios externos).
 * </p>
 */
public interface UsuarioService {

    // -- Listado de usuarios --

    /**
     * Obtiene una lista completa de todos los estudiantes registrados.
     *
     * @return lista de objetos {@link Estudiante}
     */
    List<Estudiante> listarEstudiantes();

    /**
     * Obtiene una lista completa de todos los profesores registrados.
     *
     * @return lista de objetos {@link Profesor}
     */
    List<Profesor> listarProfesores();

    // -- Operaciones CRUD para estudiantes --

    /**
     * Agrega un nuevo estudiante al sistema.
     *
     * @param e estudiante a registrar
     */
    void agregarEstudiante(Estudiante e);

    /**
     * Actualiza la información de un estudiante existente.
     *
     * @param e estudiante con los datos actualizados
     */
    void actualizarEstudiante(Estudiante e);

    /**
     * Elimina a un estudiante del sistema.
     *
     * @param e estudiante a eliminar
     */
    void eliminarEstudiante(Estudiante e);

    // -- Operaciones CRUD para profesores --

    /**
     * Agrega un nuevo profesor al sistema.
     *
     * @param p profesor a registrar
     */
    void agregarProfesor(Profesor p);

    /**
     * Actualiza los datos de un profesor existente.
     *
     * @param p profesor con los datos actualizados
     */
    void actualizarProfesor(Profesor p);

    /**
     * Elimina a un profesor del sistema.
     *
     * @param p profesor a eliminar
     */
    void eliminarProfesor(Profesor p);

    // -- Gestión de contraseñas --

    /**
     * Restablece la contraseña de un usuario (profesor o estudiante)
     * mediante su número de identificación.
     * <p>
     * En una implementación real, este método podría generar una nueva
     * contraseña temporal y notificar al usuario por correo electrónico.
     * </p>
     *
     * @param identificacion identificación del usuario
     * @return {@code true} si la operación fue exitosa, {@code false} en caso contrario
     */
    boolean restablecerContrasena(String identificacion);
}
