package org.example;

import java.util.List;

/** Contrato de servicio de usuarios para el dashboard Admin. */
public interface UsuarioService {
    List<Estudiante> listarEstudiantes();
    List<Profesor> listarProfesores();

    // Stubs de CRUD (implementados en memoria)
    void agregarEstudiante(Estudiante e);
    void actualizarEstudiante(Estudiante e);
    void eliminarEstudiante(Estudiante e);

    void agregarProfesor(Profesor p);
    void actualizarProfesor(Profesor p);
    void eliminarProfesor(Profesor p);

    boolean restablecerContrasena(String identificacion);
}
