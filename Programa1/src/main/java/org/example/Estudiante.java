package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa a un estudiante dentro del sistema.
 * <p>
 * Extiende la clase {@link Usuario} e incluye información adicional
 * como la organización donde labora, sus temas de interés y las
 * matrículas asociadas a los grupos o cursos.
 * </p>
 */
public class Estudiante extends Usuario {

    // -- Atributos --

    /** Organización donde labora el estudiante (máx. 40 caracteres). */
    private String orgDL;

    /** Lista de temas de interés del estudiante (cada uno entre 5 y 30 caracteres). */
    private List<String> temIN = new ArrayList<>();

    /** Lista de matrículas registradas del estudiante. */
    private List<Matricula> matriculas = new ArrayList<>();

    // -- Constructor --

    /**
     * Crea un nuevo estudiante con toda su información personal y académica.
     *
     * @param nombre nombre del estudiante
     * @param apellido1 primer apellido
     * @param apellido2 segundo apellido
     * @param idUsuario identificador único del usuario
     * @param telefono número de teléfono
     * @param correo correo electrónico
     * @param direccion dirección física
     * @param orgDL organización donde labora
     * @param temIN lista de temas de interés
     */
    public Estudiante(String nombre, String apellido1, String apellido2,
                      String idUsuario, String telefono, String correo, String direccion,
                      String orgDL, List<String> temIN) {
        super(nombre, apellido1, apellido2, idUsuario, telefono, correo, direccion);
        setOrgDL(orgDL);
        setTemIN(temIN);
    }

    // -- Getters y Setters --

    /** @return organización donde labora el estudiante */
    public String getOrgDL() { return orgDL; }

    /**
     * Define la organización donde labora el estudiante.
     *
     * @param orgDL nombre de la organización (máx. 40 caracteres)
     * @throws IllegalArgumentException si supera el límite de caracteres
     */
    public void setOrgDL(String orgDL) {
        if (orgDL != null && orgDL.length() > 40) {
            throw new IllegalArgumentException("La organización no puede tener más de 40 caracteres");
        }
        this.orgDL = orgDL;
    }

    /** @return lista inmodificable con los temas de interés */
    public List<String> getTemIN() { return Collections.unmodifiableList(temIN); }

    /**
     * Establece los temas de interés del estudiante.
     * <p>
     * Cada tema debe tener entre 5 y 30 caracteres.
     * </p>
     *
     * @param temIN lista de temas a registrar
     * @throws IllegalArgumentException si algún tema es nulo o no cumple las longitudes permitidas
     */
    public void setTemIN(List<String> temIN) {
        this.temIN.clear();
        if (temIN == null) return;

        for (String tema : temIN) {
            if (tema == null) {
                throw new IllegalArgumentException("El tema de interés no puede ser nulo");
            }
            int len = tema.length();
            if (len < 5 || len > 30) {
                throw new IllegalArgumentException("Cada tema debe tener entre 5 y 30 caracteres");
            }
            this.temIN.add(tema);
        }
    }

    // -- Métodos principales --

    /**
     * Agrega una nueva matrícula al estudiante.
     * <p>
     * Se asume que la clase {@link Matricula} tiene un constructor
     * {@code Matricula(Estudiante, Grupo)}.
     * </p>
     *
     * @param grupo grupo en el que se matricula
     * @throws IllegalArgumentException si el grupo es nulo
     */
    public void matricularCurso(Grupo grupo) {
        if (grupo == null) {
            throw new IllegalArgumentException("El grupo no puede ser nulo");
        }
        Matricula nuevaMatricula = new Matricula(this, grupo);
        matriculas.add(nuevaMatricula);
    }

    /**
     * Devuelve la lista de matrículas asociadas al estudiante.
     *
     * @return lista inmodificable de matrículas
     */
    public List<Matricula> obtenerMatriculas() {
        return Collections.unmodifiableList(matriculas);
    }

    /**
     * Reemplaza la lista completa de matrículas.
     *
     * @param matriculas nueva lista de matrículas
     */
    public void setMatriculas(List<Matricula> matriculas) {
        this.matriculas = matriculas;
    }
}

