package org.example;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase Profesor
 * Hereda de Usuario y agrega información específica de profesores.
 * Gestiona los grupos que imparte y las evaluaciones que crea.
 */
public class Profesor extends Usuario {
    // Atributos específicos
    private List<String> tituOb;  // Títulos obtenidos
    private List<String> certEs;  // Certificaciones de estudios
    private List<Grupo> grupos;   // Grupos que imparte
    private List<Evaluacion> evaluaciones;  // Evaluaciones creadas

    // Constructor
    public Profesor(String nombre, String apellido1, String apellido2,
                    String id, String telefono, String correo, String direccion) {
        super(nombre, apellido1, apellido2, id, telefono, correo, direccion);
        this.tituOb = new ArrayList<>();
        this.certEs = new ArrayList<>();
        this.grupos = new ArrayList<>();
        this.evaluaciones = new ArrayList<>();
    }

    // Constructor con títulos y certificaciones
    public Profesor(String nombre, String apellido1, String apellido2,
                    String id, String telefono, String correo, String direccion,
                    List<String> tituOb, List<String> certEs) {
        super(nombre, apellido1, apellido2, id, telefono, correo, direccion);
        this.tituOb = tituOb != null ? new ArrayList<>(tituOb) : new ArrayList<>();
        this.certEs = certEs != null ? new ArrayList<>(certEs) : new ArrayList<>();
        this.grupos = new ArrayList<>();
        this.evaluaciones = new ArrayList<>();
    }

    // Getters
    public List<String> getTituOb() {
        return tituOb;
    }

    public List<String> getCertEs() {
        return certEs;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public List<Evaluacion> getEvaluaciones() {
        return evaluaciones;
    }

    // Setters
    public void setTituOb(List<String> tituOb) {
        if (!validarTitulos(tituOb)) {
            throw new IllegalArgumentException("Títulos inválidos: cada uno debe tener entre 5 y 40 caracteres");
        }
        this.tituOb = tituOb;
    }

    public void setCertEs(List<String> certEs) {
        if (!validarCertificaciones(certEs)) {
            throw new IllegalArgumentException("Certificaciones inválidas: cada una debe tener entre 5 y 40 caracteres");
        }
        this.certEs = certEs;
    }

    // Métodos para gestionar títulos
    public void agregarTitulo(String titulo) {
        if (titulo == null || titulo.length() < 5 || titulo.length() > 40) {
            throw new IllegalArgumentException("Título inválido: debe tener entre 5 y 40 caracteres");
        }
        tituOb.add(titulo);
    }

    public boolean removerTitulo(String titulo) {
        return tituOb.remove(titulo);
    }

    // Métodos para gestionar certificaciones
    public void agregarCertificacion(String certificacion) {
        if (certificacion == null || certificacion.length() < 5 || certificacion.length() > 40) {
            throw new IllegalArgumentException("Certificación inválida: debe tener entre 5 y 40 caracteres");
        }
        certEs.add(certificacion);
    }

    public boolean removerCertificacion(String certificacion) {
        return certEs.remove(certificacion);
    }

    // Métodos principales

    /**
     * Asigna un grupo al profesor.
     * El profesor también se asigna al grupo automáticamente.
     */
    public void asignarGrupo(Grupo grupo) {
        if (grupo == null) {
            throw new IllegalArgumentException("El grupo no puede ser null");
        }

        if (grupos.contains(grupo)) {
            System.out.println("El profesor ya tiene asignado este grupo");
            return;
        }

        grupos.add(grupo);
        grupo.setProfesor(this);  // Asigna el profesor al grupo también
        System.out.println("Grupo #" + grupo.getIdGrupo() + " asignado exitosamente al profesor");
    }

    /**
     * Desasigna un grupo del profesor.
     */
    public boolean desasignarGrupo(Grupo grupo) {
        if (grupo == null) {
            throw new IllegalArgumentException("El grupo no puede ser null");
        }

        if (!grupos.contains(grupo)) {
            System.out.println("El profesor no tiene asignado este grupo");
            return false;
        }

        grupos.remove(grupo);
        if (grupo.getProfesor() == this) {
            grupo.setProfesor(null);  // Remueve el profesor del grupo
        }
        System.out.println("Grupo #" + grupo.getIdGrupo() + " desasignado exitosamente");
        return true;
    }

    /**
     * Crea una nueva evaluación.
     * Por ahora retorna null hasta que la clase Evaluacion esté implementada.
     */
    public Evaluacion crearEvaluacion(String nombre, String instrucciones,
                                      List<String> objetivos, int duracionMinutos,
                                      boolean preguntasAleatorias, boolean opcionesAleatorias) {
        // TODO: Cuando se implemente Evaluacion, descomentar:
        /*
        Evaluacion nuevaEvaluacion = new Evaluacion(nombre, instrucciones, objetivos,
                                                     duracionMinutos, preguntasAleatorias,
                                                     opcionesAleatorias);
        evaluaciones.add(nuevaEvaluacion);
        System.out.println("Evaluación creada exitosamente: " + nombre);
        return nuevaEvaluacion;
        */

        System.out.println("Método crearEvaluacion: pendiente de implementar clase Evaluacion");
        return null;
    }

    /**
     * Agrega una evaluación ya creada a la lista del profesor.
     */
    public void agregarEvaluacion(Evaluacion evaluacion) {
        if (evaluacion == null) {
            throw new IllegalArgumentException("La evaluación no puede ser null");
        }

        if (!evaluaciones.contains(evaluacion)) {
            evaluaciones.add(evaluacion);
            System.out.println("Evaluación agregada a la lista del profesor");
        }
    }

    /**
     * Obtiene todas las evaluaciones asociadas a un grupo específico.
     */
    public List<Evaluacion> obtenerEvaluacionesPorGrupo(Grupo grupo) {
        if (grupo == null) {
            throw new IllegalArgumentException("El grupo no puede ser null");
        }

        List<Evaluacion> evaluacionesDelGrupo = new ArrayList<>();

        // TODO: Cuando se implemente EvaluacionAsignada, descomentar:
        /*
        for (EvaluacionAsignada ea : grupo.getEvaluacionesAsignadas()) {
            if (evaluaciones.contains(ea.getEvaluacion())) {
                evaluacionesDelGrupo.add(ea.getEvaluacion());
            }
        }
        */

        return evaluacionesDelGrupo;
    }

    /**
     * Obtiene la cantidad de grupos que imparte el profesor.
     */
    public int obtenerCantidadGrupos() {
        return grupos.size();
    }

    /**
     * Obtiene la cantidad de evaluaciones creadas.
     */
    public int obtenerCantidadEvaluaciones() {
        return evaluaciones.size();
    }

    /**
     * Muestra todos los grupos del profesor.
     */
    public void mostrarGrupos() {
        if (grupos.isEmpty()) {
            System.out.println("El profesor no tiene grupos asignados");
            return;
        }

        System.out.println("\n===== GRUPOS DEL PROFESOR: " + obtenerNombreCompleto() + " =====\n");
        for (Grupo grupo : grupos) {
            System.out.println(grupo);
            System.out.println("─".repeat(50) + "\n");
        }
    }

    /**
     * Muestra todas las evaluaciones del profesor.
     */
    public void mostrarEvaluaciones() {
        if (evaluaciones.isEmpty()) {
            System.out.println("El profesor no ha creado evaluaciones");
            return;
        }

        System.out.println("\n===== EVALUACIONES DEL PROFESOR: " + obtenerNombreCompleto() + " =====\n");
        for (Evaluacion eval : evaluaciones) {
            System.out.println(eval);
            System.out.println("─".repeat(50) + "\n");
        }
    }

    // Validaciones privadas
    private boolean validarTitulos(List<String> titulos) {
        if (titulos == null) return false;

        for (String titulo : titulos) {
            if (titulo == null || titulo.length() < 5 || titulo.length() > 40) {
                return false;
            }
        }
        return true;
    }

    private boolean validarCertificaciones(List<String> certificaciones) {
        if (certificaciones == null) return false;

        for (String cert : certificaciones) {
            if (cert == null || cert.length() < 5 || cert.length() > 40) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validarDatos() {
        return super.validarDatos()
                && validarTitulos(tituOb)
                && validarCertificaciones(certEs);
    }

    // toString
    @Override
    public String toString() {
        return String.format(
                "===== PROFESOR =====\n" +
                        "%s\n" +  // Datos de Usuario (usando super.toString())
                        "Títulos obtenidos: %d\n" +
                        "%s" +
                        "Certificaciones: %d\n" +
                        "%s" +
                        "Grupos asignados: %d\n" +
                        "Evaluaciones creadas: %d",
                super.toString(),
                tituOb.size(),
                mostrarListaConIndentacion(tituOb, "  - "),
                certEs.size(),
                mostrarListaConIndentacion(certEs, "  - "),
                grupos.size(),
                evaluaciones.size()
        );
    }

    // Método auxiliar para que se vean bien bonitas las listas
    private String mostrarListaConIndentacion(List<String> lista, String prefijo) {
        if (lista.isEmpty()) {
            return prefijo + "(ninguno)\n";
        }

        StringBuilder sb = new StringBuilder();
        for (String item : lista) {
            sb.append(prefijo).append(item).append("\n");
        }
        return sb.toString();
    }
}
