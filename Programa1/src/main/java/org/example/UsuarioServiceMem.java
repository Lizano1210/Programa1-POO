package org.example;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 Servicio de usuarios en memoria con datos de prueba
 */
public class UsuarioServiceMem implements UsuarioService {

    private final List<Estudiante> estudiantes = new ArrayList<>();
    private final List<Profesor> profesores = new ArrayList<>();
    private final Autenticacion auth;

    public UsuarioServiceMem(Autenticacion auth) {
        this.auth = auth;
    }

// para testeo
    public void seedDemo() {
        //admin
        Password pwAdmin = new Password("admin", "", false);
        pwAdmin.encriptar("secret");
        auth.upsertUsuario("admin", "admin@demo.com", Roles.ADMIN, pwAdmin);

        // estudiantes
        Estudiante e1 = new Estudiante(
                "Ana", "Zúñiga", "Soto",
                "E100", "88880001", "ana.zuniga@demo.com", "San José, centro",
                "UCR", java.util.List.of("Algoritmos", "Progra")
        );
        Estudiante e2 = new Estudiante(
                "Bruno", "Mora", "Lopez",
                "E101", "88880002", "bruno.mora@demo.com", "Heredia, centro",
                "TEC", java.util.List.of("Estructuras", "Bases de Datos I")
        );
        Estudiante e3 = new Estudiante(
                "Carla", "Rojas", "Vargas",
                "E102", "88880003", "carla.rojas@demo.com", "Alajuela, centro",
                "UNA", java.util.List.of("Redes", "Sistemas")
        );
        agregarEstudiante(e1);
        agregarEstudiante(e2);
        agregarEstudiante(e3);

        // registramos credenciales de testeo
        Password pEst1 = new Password(e1.getIdUsuario(), "secret", false);
        pEst1.encriptar("secret");
        auth.upsertUsuario(e1.getIdUsuario(), e1.getCorreo(), Roles.ESTUDIANTE, pEst1);
        Password pEst2 = new Password(e1.getIdUsuario(), "secret", false);
        pEst2.encriptar("secret");
        auth.upsertUsuario(e2.getIdUsuario(), e2.getCorreo(), Roles.ESTUDIANTE, pEst2);
        Password pEst3 = new Password(e1.getIdUsuario(), "secret", false);
        pEst3.encriptar("secret");
        auth.upsertUsuario(e3.getIdUsuario(), e3.getCorreo(), Roles.ESTUDIANTE, pEst3);

        // profes
        Profesor p1 = new Profesor(
                "Mario", "Rojas", "Céspedes",
                "P200", "88880010", "mario.rojas@demo.com", "Cartago, centro"
        );
        Profesor p2 = new Profesor(
                "Natalia", "Solis", "Cambronero",
                "P201", "88880011", "natalia.solis@demo.com", "San José, Escazú"
        );
        agregarProfesor(p1);
        agregarProfesor(p2);

        Password pPro1 = new Password(p1.getIdUsuario(), "secret", false);
        pPro1.encriptar("secret");
        auth.upsertUsuario(p1.getIdUsuario(), p1.getCorreo(), Roles.PROFESOR, pPro1);
        Password pPro2 = new Password(p2.getIdUsuario(), "secret", false);
        pPro2.encriptar("secret");
        auth.upsertUsuario(p2.getIdUsuario(), p2.getCorreo(), Roles.PROFESOR, pPro2);
    }

    // listas
    @Override public List<Estudiante> listarEstudiantes() { return Collections.unmodifiableList(estudiantes); }
    @Override public List<Profesor> listarProfesores()   { return Collections.unmodifiableList(profesores); }

    // crud estudiantes
    @Override
    public void agregarEstudiante(Estudiante e) {
        if (e == null) throw new IllegalArgumentException("Estudiante nulo.");
        String id = e.getIdUsuario();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Identificación del estudiante requerida.");
        if (indexOfEstudiantePorId(id) >= 0) throw new IllegalArgumentException("Ya existe un estudiante con esa identificación.");
        estudiantes.add(e);
    }

    @Override
    public void actualizarEstudiante(Estudiante e) {
        if (e == null) throw new IllegalArgumentException("Estudiante nulo.");
        String id = e.getIdUsuario();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Identificación del estudiante requerida.");
        int idx = indexOfEstudiantePorId(id);
        if (idx < 0) throw new IllegalArgumentException("No existe un estudiante con esa identificación.");
        estudiantes.set(idx, e);
    }

    @Override
    public void eliminarEstudiante(Estudiante e) {
        if (e == null) return;
        String id = e.getIdUsuario();
        int idx = (id == null) ? -1 : indexOfEstudiantePorId(id);
        if (idx >= 0) estudiantes.remove(idx);
    }

    // crud profes
    @Override
    public void agregarProfesor(Profesor p) {
        if (p == null) throw new IllegalArgumentException("Profesor nulo.");
        String id = p.getIdUsuario();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Identificación del profesor requerida.");
        if (indexOfProfesorPorId(id) >= 0) throw new IllegalArgumentException("Ya existe un profesor con esa identificación.");
        profesores.add(p);
    }

    @Override
    public void actualizarProfesor(Profesor p) {
        if (p == null) throw new IllegalArgumentException("Profesor nulo.");
        String id = p.getIdUsuario();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Identificación del profesor requerida.");
        int idx = indexOfProfesorPorId(id);
        if (idx < 0) throw new IllegalArgumentException("No existe un profesor con esa identificación.");
        profesores.set(idx, p);
    }

    @Override
    public void eliminarProfesor(Profesor p) {
        if (p == null) return;
        String id = p.getIdUsuario();
        int idx = (id == null) ? -1 : indexOfProfesorPorId(id);
        if (idx >= 0) profesores.remove(idx);
    }

    @Override
    public boolean restablecerContrasena(String identificacion) {
        if (identificacion == null || identificacion.isBlank()) return false;
        return auth.recuperarContrasena(identificacion);
    }

    // Auxiliares
    private int indexOfEstudiantePorId(String id) {
        for (int i = 0; i < estudiantes.size(); i++) {
            String cur = estudiantes.get(i).getIdUsuario();
            if (id.equals(cur)) return i;
        }
        return -1;
    }

    private int indexOfProfesorPorId(String id) {
        for (int i = 0; i < profesores.size(); i++) {
            String cur = profesores.get(i).getIdUsuario();
            if (id.equals(cur)) return i;
        }
        return -1;
    }
}
