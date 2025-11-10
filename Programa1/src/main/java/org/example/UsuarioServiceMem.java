package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementación en memoria del servicio de usuarios.
 * <p>
 * Esta clase almacena los datos de estudiantes y profesores en listas locales,
 * sin persistencia externa. Es utilizada principalmente con fines de prueba
 * o demostración, ya que los datos se pierden al finalizar la ejecución.
 * </p>
 * <p>
 * También incluye un método de inicialización de datos de ejemplo
 * mediante {@link #seedDemo()}.
 * </p>
 */
public class UsuarioServiceMem implements UsuarioService {

    // -- Atributos principales --

    /** Lista de estudiantes en memoria. */
    private final List<Estudiante> estudiantes = new ArrayList<>();

    /** Lista de profesores en memoria. */
    private final List<Profesor> profesores = new ArrayList<>();

    /** Servicio de autenticación utilizado para registrar usuarios y contraseñas. */
    private final Autenticacion auth;

    // -- Constructor --

    /**
     * Crea un servicio de usuarios en memoria.
     *
     * @param auth servicio de autenticación a utilizar
     */
    public UsuarioServiceMem(Autenticacion auth) {
        this.auth = auth;
    }

    // -- Datos de demostración --

    /**
     * Carga datos de prueba (administrador, estudiantes y profesores) en memoria.
     * <p>
     * Se generan credenciales y usuarios con contraseñas encriptadas.
     * </p>
     */
    public void seedDemo() {
        // -- Administrador --
        Password pwAdmin = new Password("admin", "", false);
        pwAdmin.encriptar("secret");
        auth.upsertUsuario("admin", "admin@demo.com", Roles.ADMIN, pwAdmin);

        // -- Estudiantes --
        Estudiante e1 = new Estudiante(
                "Ana", "Zúñiga", "Soto",
                "E100", "88880001", "ana.zuniga@demo.com", "San José, centro",
                "UCR", List.of("Algoritmos", "Progra")
        );
        Estudiante e2 = new Estudiante(
                "Bruno", "Mora", "Lopez",
                "E101", "88880002", "bruno.mora@demo.com", "Heredia, centro",
                "TEC", List.of("Estructuras", "Bases de Datos I")
        );
        Estudiante e3 = new Estudiante(
                "Carla", "Rojas", "Vargas",
                "E102", "88880003", "carla.rojas@demo.com", "Alajuela, centro",
                "UNA", List.of("Redes", "Sistemas")
        );
        agregarEstudiante(e1);
        agregarEstudiante(e2);
        agregarEstudiante(e3);

        // -- Credenciales de estudiantes --
        Password pEst1 = new Password(e1.getIdUsuario(), "secret", false);
        pEst1.encriptar("secret");
        auth.upsertUsuario(e1.getIdUsuario(), e1.getCorreo(), Roles.ESTUDIANTE, pEst1);

        Password pEst2 = new Password(e2.getIdUsuario(), "secret", false);
        pEst2.encriptar("secret");
        auth.upsertUsuario(e2.getIdUsuario(), e2.getCorreo(), Roles.ESTUDIANTE, pEst2);

        Password pEst3 = new Password(e3.getIdUsuario(), "secret", false);
        pEst3.encriptar("secret");
        auth.upsertUsuario(e3.getIdUsuario(), e3.getCorreo(), Roles.ESTUDIANTE, pEst3);

        // -- Profesores --
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

        // -- Credenciales de profesores --
        Password pPro1 = new Password(p1.getIdUsuario(), "secret", false);
        pPro1.encriptar("secret");
        auth.upsertUsuario(p1.getIdUsuario(), p1.getCorreo(), Roles.PROFESOR, pPro1);

        Password pPro2 = new Password(p2.getIdUsuario(), "secret", false);
        pPro2.encriptar("secret");
        auth.upsertUsuario(p2.getIdUsuario(), p2.getCorreo(), Roles.PROFESOR, pPro2);
    }

    // -- Listado de usuarios --

    /** {@inheritDoc} */
    @Override
    public List<Estudiante> listarEstudiantes() {
        return Collections.unmodifiableList(estudiantes);
    }

    /** {@inheritDoc} */
    @Override
    public List<Profesor> listarProfesores() {
        return Collections.unmodifiableList(profesores);
    }

    // -- CRUD Estudiantes --

    /** {@inheritDoc} */
    @Override
    public void agregarEstudiante(Estudiante e) {
        if (e == null) throw new IllegalArgumentException("Estudiante nulo.");
        String id = e.getIdUsuario();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Identificación del estudiante requerida.");
        if (indexOfEstudiantePorId(id) >= 0)
            throw new IllegalArgumentException("Ya existe un estudiante con esa identificación.");
        estudiantes.add(e);
    }

    /** {@inheritDoc} */
    @Override
    public void actualizarEstudiante(Estudiante e) {
        if (e == null) throw new IllegalArgumentException("Estudiante nulo.");
        String id = e.getIdUsuario();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Identificación del estudiante requerida.");
        int idx = indexOfEstudiantePorId(id);
        if (idx < 0) throw new IllegalArgumentException("No existe un estudiante con esa identificación.");
        estudiantes.set(idx, e);
    }

    /** {@inheritDoc} */
    @Override
    public void eliminarEstudiante(Estudiante e) {
        if (e == null) return;
        String id = e.getIdUsuario();
        int idx = (id == null) ? -1 : indexOfEstudiantePorId(id);
        if (idx >= 0) estudiantes.remove(idx);
    }

    // -- CRUD Profesores --

    /** {@inheritDoc} */
    @Override
    public void agregarProfesor(Profesor p) {
        if (p == null) throw new IllegalArgumentException("Profesor nulo.");
        String id = p.getIdUsuario();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Identificación del profesor requerida.");
        if (indexOfProfesorPorId(id) >= 0)
            throw new IllegalArgumentException("Ya existe un profesor con esa identificación.");
        profesores.add(p);
    }

    /** {@inheritDoc} */
    @Override
    public void actualizarProfesor(Profesor p) {
        if (p == null) throw new IllegalArgumentException("Profesor nulo.");
        String id = p.getIdUsuario();
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Identificación del profesor requerida.");
        int idx = indexOfProfesorPorId(id);
        if (idx < 0) throw new IllegalArgumentException("No existe un profesor con esa identificación.");
        profesores.set(idx, p);
    }

    /** {@inheritDoc} */
    @Override
    public void eliminarProfesor(Profesor p) {
        if (p == null) return;
        String id = p.getIdUsuario();
        int idx = (id == null) ? -1 : indexOfProfesorPorId(id);
        if (idx >= 0) profesores.remove(idx);
    }

    // -- Contraseñas --

    /** {@inheritDoc} */
    @Override
    public boolean restablecerContrasena(String identificacion) {
        if (identificacion == null || identificacion.isBlank()) return false;
        return auth.recuperarContrasena(identificacion);
    }

    // -- Métodos auxiliares --

    /** Busca el índice de un estudiante según su ID. */
    private int indexOfEstudiantePorId(String id) {
        for (int i = 0; i < estudiantes.size(); i++) {
            String cur = estudiantes.get(i).getIdUsuario();
            if (id.equals(cur)) return i;
        }
        return -1;
    }

    /** Busca el índice de un profesor según su ID. */
    private int indexOfProfesorPorId(String id) {
        for (int i = 0; i < profesores.size(); i++) {
            String cur = profesores.get(i).getIdUsuario();
            if (id.equals(cur)) return i;
        }
        return -1;
    }
}
