package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un profesor dentro del sistema académico.
 * <p>
 * Hereda de {@link Usuario} e incluye información adicional como títulos,
 * certificaciones, grupos que imparte y evaluaciones creadas.
 * También permite crear y administrar evaluaciones y grupos.
 * </p>
 */
public class Profesor extends Usuario {

    // -- Atributos --

    /** Títulos obtenidos por el profesor. */
    private List<String> tituOb;

    /** Certificaciones de estudio del profesor. */
    private List<String> certEs;

    /** Grupos asignados al profesor. */
    private List<Grupo> grupos;

    /** Evaluaciones creadas por el profesor. */
    private List<Evaluacion> evaluaciones;

    // -- Constructores --

    /**
     * Crea un nuevo profesor con información básica.
     *
     * @param nombre nombre del profesor
     * @param apellido1 primer apellido
     * @param apellido2 segundo apellido
     * @param id identificador único del profesor
     * @param telefono número de teléfono
     * @param correo correo electrónico
     * @param direccion dirección del profesor
     */
    public Profesor(String nombre, String apellido1, String apellido2,
                    String id, String telefono, String correo, String direccion) {
        super(nombre, apellido1, apellido2, id, telefono, correo, direccion);
        this.tituOb = new ArrayList<>();
        this.certEs = new ArrayList<>();
        this.grupos = new ArrayList<>();
        this.evaluaciones = new ArrayList<>();
    }

    /**
     * Crea un nuevo profesor incluyendo títulos y certificaciones.
     *
     * @param nombre nombre del profesor
     * @param apellido1 primer apellido
     * @param apellido2 segundo apellido
     * @param id identificador único
     * @param telefono teléfono de contacto
     * @param correo correo electrónico
     * @param direccion dirección física
     * @param tituOb lista de títulos obtenidos
     * @param certEs lista de certificaciones
     */
    public Profesor(String nombre, String apellido1, String apellido2,
                    String id, String telefono, String correo, String direccion,
                    List<String> tituOb, List<String> certEs) {
        super(nombre, apellido1, apellido2, id, telefono, correo, direccion);
        this.tituOb = tituOb != null ? new ArrayList<>(tituOb) : new ArrayList<>();
        this.certEs = certEs != null ? new ArrayList<>(certEs) : new ArrayList<>();
        this.grupos = new ArrayList<>();
        this.evaluaciones = new ArrayList<>();
    }

    // -- Getters y Setters --

    public List<String> getTituOb() { return tituOb; }

    public List<String> getCertEs() { return certEs; }

    public List<Grupo> getGrupos() { return grupos; }

    public void setGrupos(ArrayList<Grupo> nGrupos) { this.grupos = nGrupos; }

    public List<Evaluacion> getEvaluaciones() { return evaluaciones; }

    public void setEvaluaciones(ArrayList<Evaluacion> nuevasEval) { this.evaluaciones = nuevasEval; }

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

    // -- Gestión de títulos y certificaciones --

    /** Agrega un nuevo título al profesor. */
    public void agregarTitulo(String titulo) {
        if (titulo == null || titulo.length() < 5 || titulo.length() > 40)
            throw new IllegalArgumentException("Título inválido: debe tener entre 5 y 40 caracteres");
        tituOb.add(titulo);
    }

    /** Elimina un título existente. */
    public boolean removerTitulo(String titulo) {
        return tituOb.remove(titulo);
    }

    /** Agrega una nueva certificación al profesor. */
    public void agregarCertificacion(String certificacion) {
        if (certificacion == null || certificacion.length() < 5 || certificacion.length() > 40)
            throw new IllegalArgumentException("Certificación inválida: debe tener entre 5 y 40 caracteres");
        certEs.add(certificacion);
    }

    /** Elimina una certificación existente. */
    public boolean removerCertificacion(String certificacion) {
        return certEs.remove(certificacion);
    }

    // -- Gestión de grupos --

    /**
     * Asigna un grupo al profesor y establece la relación bidireccional.
     *
     * @param grupo grupo a asignar
     */
    public void asignarGrupo(Grupo grupo) {
        if (grupo == null) throw new IllegalArgumentException("El grupo no puede ser null");
        if (grupos.contains(grupo)) {
            System.out.println("El profesor ya tiene asignado este grupo");
            return;
        }

        grupos.add(grupo);
        grupo.setProfesor(this);
        System.out.println("Grupo #" + grupo.getIdGrupo() + " asignado exitosamente al profesor");
    }

    /**
     * Desasigna un grupo del profesor, eliminando la referencia mutua.
     *
     * @param grupo grupo a desasignar
     * @return {@code true} si la operación fue exitosa
     */
    public boolean desasignarGrupo(Grupo grupo) {
        if (grupo == null) throw new IllegalArgumentException("El grupo no puede ser null");

        if (!grupos.contains(grupo)) {
            System.out.println("El profesor no tiene asignado este grupo");
            return false;
        }

        grupos.remove(grupo);
        if (grupo.getProfesor() == this) grupo.setProfesor(null);
        System.out.println("Grupo #" + grupo.getIdGrupo() + " desasignado exitosamente");
        return true;
    }

    // -- Gestión de evaluaciones --

    /**
     * Crea una nueva evaluación y la asocia al profesor.
     *
     * @param nombre nombre de la evaluación
     * @param instrucciones instrucciones de la evaluación
     * @param objetivos lista de objetivos de aprendizaje
     * @param duracionMinutos duración en minutos
     * @param preguntasAleatorias si las preguntas se mostrarán en orden aleatorio
     * @param opcionesAleatorias si las opciones dentro de las preguntas se mezclarán
     * @return evaluación creada
     */
    public Evaluacion crearEvaluacion(String nombre, String instrucciones,
                                      List<String> objetivos, int duracionMinutos,
                                      boolean preguntasAleatorias, boolean opcionesAleatorias) {
        Evaluacion nuevaEvaluacion = new Evaluacion(
                nombre, instrucciones, objetivos, duracionMinutos,
                preguntasAleatorias, opcionesAleatorias
        );
        evaluaciones.add(nuevaEvaluacion);
        System.out.println("Evaluación creada exitosamente: " + nombre);
        return nuevaEvaluacion;
    }

    /** Agrega una evaluación ya existente a la lista del profesor. */
    public void agregarEvaluacion(Evaluacion evaluacion) {
        if (evaluacion == null)
            throw new IllegalArgumentException("La evaluación no puede ser null");

        if (!evaluaciones.contains(evaluacion)) {
            evaluaciones.add(evaluacion);
            System.out.println("Evaluación agregada a la lista del profesor");
        }
    }

    /** Obtiene las evaluaciones creadas por el profesor en un grupo específico. */
    public List<Evaluacion> obtenerEvaluacionesPorGrupo(Grupo grupo) {
        if (grupo == null) throw new IllegalArgumentException("El grupo no puede ser null");

        List<Evaluacion> evaluacionesDelGrupo = new ArrayList<>();
        for (EvaluacionAsignada ea : grupo.getEvaluacionesAsignadas()) {
            if (evaluaciones.contains(ea.getEvaluacion())) evaluacionesDelGrupo.add(ea.getEvaluacion());
        }
        return evaluacionesDelGrupo;
    }

    // -- Consultas --

    /** Devuelve la cantidad total de grupos asignados al profesor. */
    public int obtenerCantidadGrupos() { return grupos.size(); }

    /** Devuelve la cantidad de evaluaciones creadas. */
    public int obtenerCantidadEvaluaciones() { return evaluaciones.size(); }

    /** Muestra en consola todos los grupos asignados al profesor. */
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

    /** Muestra en consola todas las evaluaciones creadas por el profesor. */
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

    // -- Validaciones internas --

    private boolean validarTitulos(List<String> titulos) {
        if (titulos == null) return false;
        for (String titulo : titulos)
            if (titulo == null || titulo.length() < 5 || titulo.length() > 40) return false;
        return true;
    }

    private boolean validarCertificaciones(List<String> certificaciones) {
        if (certificaciones == null) return false;
        for (String cert : certificaciones)
            if (cert == null || cert.length() < 5 || cert.length() > 40) return false;
        return true;
    }

    @Override
    public boolean validarDatos() {
        return super.validarDatos()
                && validarTitulos(tituOb)
                && validarCertificaciones(certEs);
    }

    // -- Representación --

    @Override
    public String toString() {
        return String.format(
                "===== PROFESOR =====\n%s\nTítulos obtenidos: %d\n%sCertificaciones: %d\n%sGrupos asignados: %d\nEvaluaciones creadas: %d",
                super.toString(),
                tituOb.size(), mostrarListaConIndentacion(tituOb, "  - "),
                certEs.size(), mostrarListaConIndentacion(certEs, "  - "),
                grupos.size(),
                evaluaciones.size()
        );
    }

    /** Formatea una lista con indentación para visualización. */
    private String mostrarListaConIndentacion(List<String> lista, String prefijo) {
        if (lista.isEmpty()) return prefijo + "(ninguno)\n";
        StringBuilder sb = new StringBuilder();
        for (String item : lista) sb.append(prefijo).append(item).append("\n");
        return sb.toString();
    }
}
