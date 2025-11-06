package org.example;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase Administrador
 * Hereda de Usuario y tiene permisos para gestionar todo el sistema.
 */
public class Administrador extends Usuario {
    // Atributos
    private int nivelAcceso;  // 1=básico, 2=intermedio, 3=completo

    // Listas del sistema (normalmente estarían en una clase Sistema o Main)
    private static List<Estudiante> listaEstudiantes = new ArrayList<>();
    private static List<Profesor> listaProfesores = new ArrayList<>();
    private static List<Curso> listaCursos = new ArrayList<>();

    // Constructor
    public Administrador(String nombre, String apellido1, String apellido2,
                         String id, String telefono, String correo, String direccion,
                         int nivelAcceso) {
        super(nombre, apellido1, apellido2, id, telefono, correo, direccion);
        this.nivelAcceso = nivelAcceso;
    }

    // Getters y Setters
    public int getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(int nivelAcceso) {
        if (nivelAcceso < 1 || nivelAcceso > 3) {
            throw new IllegalArgumentException("Nivel de acceso debe ser entre 1 y 3");
        }
        this.nivelAcceso = nivelAcceso;
    }

     
    // GESTIÓN DE ESTUDIANTES
     

    public boolean registrarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            System.out.println("Error: El estudiante no puede ser null");
            return false;
        }

        if (!estudiante.validarDatos()) {
            System.out.println("Error: Los datos del estudiante son inválidos");
            return false;
        }

        // Verificar que no exista un estudiante con el mismo ID
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

        // Reemplazar en la lista
        int index = listaEstudiantes.indexOf(estudianteExistente);
        listaEstudiantes.set(index, estudianteModificado);

        System.out.println("Estudiante modificado exitosamente: " + estudianteModificado.obtenerNombreCompleto());
        
        return true;
    }

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

    private Estudiante buscarEstudiante(String id) {
        for (Estudiante e : listaEstudiantes) {
            if (e.getIdUsuario().equals(id)) {
                return e;
            }
        }
        return null;
    }

     
    // GESTIÓN DE PROFESORES
     

    public boolean registrarProfesor(Profesor profesor) {
        if (profesor == null) {
            System.out.println("Error: El profesor no puede ser null");
            return false;
        }

        if (!profesor.validarDatos()) {
            System.out.println("Error: Los datos del profesor son inválidos");
            return false;
        }

        // Verificar que no exista un profesor con el mismo ID
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

        // Reemplazar en la lista
        int index = listaProfesores.indexOf(profesorExistente);
        listaProfesores.set(index, profesorModificado);

        System.out.println("Profesor modificado exitosamente: " + profesorModificado.obtenerNombreCompleto());

        return true;
    }

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

    private Profesor buscarProfesor(String id) {
        for (Profesor p : listaProfesores) {
            if (p.getIdUsuario().equals(id)) {
                return p;
            }
        }
        return null;
    }

     
    // GESTIÓN DE CURSOS
     

    public boolean crearCurso(Curso curso) {
        if (curso == null) {
            System.out.println("Error: El curso no puede ser null");
            return false;
        }

        if (!curso.validarDatos()) {
            System.out.println("Error: Los datos del curso son inválidos");
            return false;
        }

        // Verificar que no exista un curso con el mismo ID
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

        // Reemplazar en la lista
        int index = listaCursos.indexOf(cursoExistente);
        listaCursos.set(index, cursoModificado);

        System.out.println("Curso modificado exitosamente: " + cursoModificado.getNombre());
        return true;
    }

    public boolean eliminarCurso(String idCurso) {
        Curso curso = buscarCurso(idCurso);

        if (curso == null) {
            System.out.println("Error: No se encontró curso con ID " + idCurso);
            return false;
        }

        // Verificar que no tenga grupos vigentes
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

    private Curso buscarCurso(String id) {
        for (Curso c : listaCursos) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

     
    // REPORTES
     

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

    // Métodos de acceso a listas (para usar desde otras clases)
    public static List<Estudiante> getListaEstudiantes() {
        return listaEstudiantes;
    }

    public static List<Profesor> getListaProfesores() {
        return listaProfesores;
    }

    public static List<Curso> getListaCursos() {
        return listaCursos;
    }

    @Override
    public boolean validarDatos() {
        return super.validarDatos() && nivelAcceso >= 1 && nivelAcceso <= 3;
    }

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

    private String obtenerNombreNivelAcceso() {
        switch (nivelAcceso) {
            case 1: return "Básico";
            case 2: return "Intermedio";
            case 3: return "Completo";
            default: return "Desconocido";
        }
    }
}


 
// CLASE GESTORA PARA ADMINISTRADOR POR DEFECTO
 

/**
 * Clase que gestiona el administrador por defecto del sistema.
 * Usa el patrón Singleton para asegurar un único administrador inicial.
 */
class GestorAdministrador {
    private static Administrador adminPorDefecto = null;

    /**
     * Crea y retorna el administrador por defecto del sistema.
     * Si ya existe, retorna el mismo.
     */
    public static Administrador obtenerAdminPorDefecto() {
        if (adminPorDefecto == null) {
            adminPorDefecto = new Administrador(
                    "Admin",           // nombre
                    "Sistema",         // apellido1
                    "Principal",       // apellido2
                    "ADMIN001",        // id
                    "88888888",        // telefono
                    "admin@sistema.edu", // correo
                    "Oficina Central",   // direccion
                    3                   // nivel de acceso completo
            );

            System.out.println("✓ Administrador por defecto creado exitosamente");
            System.out.println("  ID: ADMIN001");
            System.out.println("  Contraseña inicial: admin123 (cambiar al primer uso)");
        }

        return adminPorDefecto;
    }

    /**
     * Verifica si las credenciales corresponden al administrador por defecto.
     */
    public static boolean validarCredencialesAdmin(String id, String contrasena) {
        if (adminPorDefecto == null) {
            obtenerAdminPorDefecto();
        }

        // En un sistema real, verificarías con la clase Contraseña
        return id.equals("ADMIN001") && contrasena.equals("admin123");
    }

    /**
     * Permite cambiar el administrador por defecto (útil si se elimina).
     */
    public static void establecerAdminPorDefecto(Administrador admin) {
        adminPorDefecto = admin;
    }
}