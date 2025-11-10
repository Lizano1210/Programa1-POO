package org.example;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un administrador del sistema.
 * Hereda de {@link Usuario} y puede gestionar estudiantes, profesores y cursos.
 */
public class Administrador extends Usuario {

    // -- Atributos --
    /** Nivel de acceso del administrador: 1=básico, 2=intermedio, 3=completo. */
    private int nivelAcceso;

    // -- Listas del sistema (en una app real vivirían en una capa de servicio) --
    private static List<Estudiante> listaEstudiantes = new ArrayList<>();
    private static List<Profesor> listaProfesores = new ArrayList<>();
    private static List<Curso> listaCursos = new ArrayList<>();

    // -- Constructor --
    /**
     * Crea un administrador con sus datos personales y nivel de acceso.
     *
     * @param nombre    nombre
     * @param apellido1 primer apellido
     * @param apellido2 segundo apellido
     * @param id        identificador único
     * @param telefono  teléfono de contacto
     * @param correo    correo electrónico
     * @param direccion dirección
     * @param nivelAcceso nivel de acceso (1 a 3)
     */
    public Administrador(String nombre, String apellido1, String apellido2,
                         String id, String telefono, String correo, String direccion,
                         int nivelAcceso) {
        super(nombre, apellido1, apellido2, id, telefono, correo, direccion);
        this.nivelAcceso = nivelAcceso;
    }

    // -- Getters y Setters --
    /**
     * Obtiene el nivel de acceso del administrador.
     *
     * @return nivel de acceso (1-3)
     */
    public int getNivelAcceso() {
        return nivelAcceso;
    }

    /**
     * Establece el nivel de acceso del administrador.
     *
     * @param nivelAcceso nuevo nivel (1-3)
     * @throws IllegalArgumentException si el nivel está fuera de 1-3
     */
    public void setNivelAcceso(int nivelAcceso) {
        if (nivelAcceso < 1 || nivelAcceso > 3) {
            throw new IllegalArgumentException("Nivel de acceso debe ser entre 1 y 3");
        }
        this.nivelAcceso = nivelAcceso;
    }

    // -- Gestión de estudiantes --

    /**
     * Registra un estudiante, validando datos y evitando IDs duplicados.
     *
     * @param estudiante estudiante a registrar
     * @return {@code true} si se registró; {@code false} en caso contrario
     */
    public boolean registrarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            System.out.println("Error: El estudiante no puede ser null");
            return false;
        }

        if (!estudiante.validarDatos()) {
            System.out.println("Error: Los datos del estudiante son inválidos");
            return false;
        }

        for (Estudiante e : listaEstudiantes) {
            if (e.getIdUsuario().equals(estudiante.getIdUsuario())) {
                System.out.println("Error: Ya existe un estudiante con ID " + estudiante.getIdUsuario());
                return false;
            }
        }

        listaEstudiantes.add(estudiante);
        System.out.println("Estudiante registrado exitosamente: " + estudiante.obtenerNombreCompleto());
        return true;
    }

    /**
     * Modifica los datos de un estudiante ya existente.
     *
     * @param estudianteModificado datos actualizados
     * @return {@code true} si se modificó; {@code false} si no existe o no es válido
     */
    public boolean modificarEstudiante(Estudiante estudianteModificado) {
        if (estudianteModificado == null) {
            System.out.println("Error: El estudiante no puede ser null");
            return false;
        }

        Estudiante estudianteExistente = buscarEstudiante(estudianteModificado.getIdUsuario());
        if (estudianteExistente == null) {
            System.out.println("Error: No se encontró estudiante con ID " + estudianteModificado.getIdUsuario());
            return false;
        }

        if (!estudianteModificado.validarDatos()) {
            System.out.println("Error: Los datos modificados son inválidos");
            return false;
        }

        int index = listaEstudiantes.indexOf(estudianteExistente);
        listaEstudiantes.set(index, estudianteModificado);

        System.out.println("Estudiante modificado exitosamente: " + estudianteModificado.obtenerNombreCompleto());
        return true;
    }

    /**
     * Elimina un estudiante por su ID.
     *
     * @param idEstudiante ID del estudiante
     * @return {@code true} si se eliminó; {@code false} si no existe
     */
    public boolean eliminarEstudiante(String idEstudiante) {
        Estudiante estudiante = buscarEstudiante(idEstudiante);

        if (estudiante == null) {
            System.out.println("Error: No se encontró estudiante con ID " + idEstudiante);
            return false;
        }

        listaEstudiantes.remove(estudiante);
        System.out.println("Estudiante eliminado exitosamente: " + estudiante.obtenerNombreCompleto());
        return true;
    }

    /**
     * Busca un estudiante por ID.
     *
     * @param id ID del estudiante
     * @return el estudiante o {@code null} si no existe
     */
    private Estudiante buscarEstudiante(String id) {
        for (Estudiante e : listaEstudiantes) {
            if (e.getIdUsuario().equals(id)) {
                return e;
            }
        }
        return null;
    }

    // -- Gestión de profesores --

    /**
     * Registra un profesor, validando datos y evitando IDs duplicados.
     *
     * @param profesor profesor a registrar
     * @return {@code true} si se registró; {@code false} en caso contrario
     */
    public boolean registrarProfesor(Profesor profesor) {
        if (profesor == null) {
            System.out.println("Error: El profesor no puede ser null");
            return false;
        }

        if (!profesor.validarDatos()) {
            System.out.println("Error: Los datos del profesor son inválidos");
            return false;
        }

        for (Profesor p : listaProfesores) {
            if (p.getIdUsuario().equals(profesor.getIdUsuario())) {
                System.out.println("Error: Ya existe un profesor con ID " + profesor.getIdUsuario());
                return false;
            }
        }

        listaProfesores.add(profesor);
        System.out.println("Profesor registrado exitosamente: " + profesor.obtenerNombreCompleto());
        return true;
    }

    /**
     * Modifica los datos de un profesor existente.
     *
     * @param profesorModificado datos actualizados
     * @return {@code true} si se modificó; {@code false} si no existe o no es válido
     */
    public boolean modificarProfesor(Profesor profesorModificado) {
        if (profesorModificado == null) {
            System.out.println("Error: El profesor no puede ser null");
            return false;
        }

        Profesor profesorExistente = buscarProfesor(profesorModificado.getIdUsuario());
        if (profesorExistente == null) {
            System.out.println("Error: No se encontró profesor con ID " + profesorModificado.getIdUsuario());
            return false;
        }

        if (!profesorModificado.validarDatos()) {
            System.out.println("Error: Los datos modificados son inválidos");
            return false;
        }

        int index = listaProfesores.indexOf(profesorExistente);
        listaProfesores.set(index, profesorModificado);

        System.out.println("Profesor modificado exitosamente: " + profesorModificado.obtenerNombreCompleto());
        return true;
    }

    /**
     * Elimina un profesor por su ID.
     *
     * @param idProfesor ID del profesor
     * @return {@code true} si se eliminó; {@code false} si no existe
     */
    public boolean eliminarProfesor(String idProfesor) {
        Profesor profesor = buscarProfesor(idProfesor);

        if (profesor == null) {
            System.out.println("Error: No se encontró profesor con ID " + idProfesor);
            return false;
        }

        listaProfesores.remove(profesor);
        System.out.println("Profesor eliminado exitosamente: " + profesor.obtenerNombreCompleto());
        return true;
    }

    /**
     * Busca un profesor por ID.
     *
     * @param id ID del profesor
     * @return el profesor o {@code null} si no existe
     */
    private Profesor buscarProfesor(String id) {
        for (Profesor p : listaProfesores) {
            if (p.getIdUsuario().equals(id)) {
                return p;
            }
        }
        return null;
    }

    // -- Gestión de cursos --

    /**
     * Crea un curso, validando datos y evitando IDs duplicados.
     *
     * @param curso curso a crear
     * @return {@code true} si se creó; {@code false} en caso contrario
     */
    public boolean crearCurso(Curso curso) {
        if (curso == null) {
            System.out.println("Error: El curso no puede ser null");
            return false;
        }

        if (!curso.validarDatos()) {
            System.out.println("Error: Los datos del curso son inválidos");
            return false;
        }

        for (Curso c : listaCursos) {
            if (c.getId().equals(curso.getId())) {
                System.out.println("Error: Ya existe un curso con ID " + curso.getId());
                return false;
            }
        }

        listaCursos.add(curso);
        System.out.println("Curso creado exitosamente: " + curso.getNombre());
        return true;
    }

    /**
     * Modifica un curso existente.
     *
     * @param cursoModificado datos actualizados
     * @return {@code true} si se modificó; {@code false} si no existe o no es válido
     */
    public boolean modificarCurso(Curso cursoModificado) {
        if (cursoModificado == null) {
            System.out.println("Error: El curso no puede ser null");
            return false;
        }

        Curso cursoExistente = buscarCurso(cursoModificado.getId());
        if (cursoExistente == null) {
            System.out.println("Error: No se encontró curso con ID " + cursoModificado.getId());
            return false;
        }

        if (!cursoModificado.validarDatos()) {
            System.out.println("Error: Los datos modificados son inválidos");
            return false;
        }

        int index = listaCursos.indexOf(cursoExistente);
        listaCursos.set(index, cursoModificado);

        System.out.println("Curso modificado exitosamente: " + cursoModificado.getNombre());
        return true;
    }

    /**
     * Elimina un curso por ID, siempre que no tenga grupos vigentes.
     *
     * @param idCurso ID del curso
     * @return {@code true} si se eliminó; {@code false} si no existe o tiene grupos vigentes
     */
    public boolean eliminarCurso(String idCurso) {
        Curso curso = buscarCurso(idCurso);

        if (curso == null) {
            System.out.println("Error: No se encontró curso con ID " + idCurso);
            return false;
        }

        for (Grupo g : curso.grupos) {
            if (g.esVigenteHoy()) {
                System.out.println("Error: No se puede eliminar el curso porque tiene grupos vigentes");
                return false;
            }
        }

        listaCursos.remove(curso);
        System.out.println("Curso eliminado exitosamente: " + curso.getNombre());
        return true;
    }

    /**
     * Busca un curso por su ID.
     *
     * @param id ID del curso
     * @return curso encontrado o {@code null}
     */
    private Curso buscarCurso(String id) {
        for (Curso c : listaCursos) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    // -- Reportes --

    /**
     * Imprime un reporte general del sistema en consola.
     * Muestra conteos de entidades y resumen de cursos y grupos.
     */
    public void generarReporte() {
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║         REPORTE DEL SISTEMA ACADÉMICO          ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        System.out.println("Fecha del reporte: " + LocalDate.now());
        System.out.println("Generado por: " + obtenerNombreCompleto());
        System.out.println("\n" + "─".repeat(50));

        System.out.println("\n ESTADÍSTICAS GENERALES:");
        System.out.println("  • Total de estudiantes: " + listaEstudiantes.size());
        System.out.println("  • Total de profesores: " + listaProfesores.size());
        System.out.println("  • Total de cursos: " + listaCursos.size());

        int totalGrupos = 0;
        int gruposVigentes = 0;
        for (Curso c : listaCursos) {
            totalGrupos += c.grupos.size();
            for (Grupo g : c.grupos) {
                if (g.esVigenteHoy()) gruposVigentes++;
            }
        }
        System.out.println("  • Total de grupos: " + totalGrupos);
        System.out.println("  • Grupos vigentes: " + gruposVigentes);

        System.out.println("\n" + "─".repeat(50));
        System.out.println("\nCURSOS:");
        if (listaCursos.isEmpty()) {
            System.out.println("  (No hay cursos registrados)");
        } else {
            for (Curso c : listaCursos) {
                System.out.println("  • " + c.getNombre() + " (" + c.getId() + ") - " + c.grupos.size() + " grupos");
            }
        }

        System.out.println("\n" + "─".repeat(50));
        System.out.println();
    }

    // -- Acceso a listas (para otras clases) --
    /** @return lista global de estudiantes. */
    public static List<Estudiante> getListaEstudiantes() { return listaEstudiantes; }
    /** @return lista global de profesores. */
    public static List<Profesor> getListaProfesores() { return listaProfesores; }
    /** @return lista global de cursos. */
    public static List<Curso> getListaCursos() { return listaCursos; }

    // -- Validaciones y utilidades --
    /**
     * Valida los datos del administrador (incluye rango de nivel de acceso).
     *
     * @return {@code true} si los datos son válidos
     */
    @Override
    public boolean validarDatos() {
        return super.validarDatos() && nivelAcceso >= 1 && nivelAcceso <= 3;
    }

    /**
     * Representación de texto del administrador.
     *
     * @return cadena con datos y nivel de acceso
     */
    @Override
    public String toString() {
        return String.format(
                "===== ADMINISTRADOR =====\n" +
                        "%s\n" +
                        "Nivel de acceso: %d (%s)",
                super.toString(),
                nivelAcceso,
                obtenerNombreNivelAcceso()
        );
    }

    /**
     * Obtiene el nombre legible del nivel de acceso.
     *
     * @return texto del nivel
     */
    private String obtenerNombreNivelAcceso() {
        switch (nivelAcceso) {
            case 1: return "Básico";
            case 2: return "Intermedio";
            case 3: return "Completo";
            default: return "Desconocido";
        }
    }
}


// -- Clase gestora para administrador por defecto --

/**
 * Gestiona el administrador por defecto del sistema.
 * Implementa un enfoque de tipo Singleton para asegurar una única instancia.
 */
class GestorAdministrador {

    /** Referencia al administrador por defecto (único). */
    private static Administrador adminPorDefecto = null;

    /**
     * Obtiene el administrador por defecto; si no existe, lo crea.
     *
     * @return administrador por defecto
     */
    public static Administrador obtenerAdminPorDefecto() {
        if (adminPorDefecto == null) {
            adminPorDefecto = new Administrador(
                    "Admin",
                    "Sistema",
                    "Principal",
                    "ADMIN001",
                    "88888888",
                    "admin@sistema.edu",
                    "Oficina Central",
                    3
            );

            System.out.println("✓ Administrador por defecto creado exitosamente");
            System.out.println("  ID: ADMIN001");
            System.out.println("  Contraseña inicial: admin123 (cambiar al primer uso)");
        }

        return adminPorDefecto;
    }

    /**
     * Valida credenciales contra el administrador por defecto.
     * En un sistema real, esto se integraría con la gestión de contraseñas.
     *
     * @param id         identificador
     * @param contrasena contraseña
     * @return {@code true} si coinciden con las credenciales por defecto
     */
    public static boolean validarCredencialesAdmin(String id, String contrasena) {
        if (adminPorDefecto == null) {
            obtenerAdminPorDefecto();
        }
        return id.equals("ADMIN001") && contrasena.equals("admin123");
    }

    /**
     * Establece una nueva instancia como administrador por defecto.
     *
     * @param admin administrador a fijar como predeterminado
     */
    public static void establecerAdminPorDefecto(Administrador admin) {
        adminPorDefecto = admin;
    }
}
