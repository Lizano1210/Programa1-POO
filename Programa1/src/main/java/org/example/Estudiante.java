package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Estudiante extends Usuario {

    private String orgDL;  // Organización donde labora (máx. 40 caracteres)
    private List<String> temIN = new ArrayList<>(); // Temas de interés
    private List<Matricula> matriculas = new ArrayList<>(); // Cursos matriculados

    // Constructor completo que llama al de Usuario
    public Estudiante(String nombre, String apellido1, String apellido2, String idUsuario, String telefono,
            String correo, String direccion, String orgDL, List<String> temIN
    ) {
        super(nombre, apellido1, apellido2, idUsuario, telefono, correo, direccion);
        setOrgDL(orgDL);
        setTemIN(temIN);
    }

    // -------------------------
    // Getters y Setters
    // -------------------------

    public String getOrgDL() {
        return orgDL;
    }

    public void setOrgDL(String orgDL) {
        if (orgDL != null && orgDL.length() > 40) {
            throw new IllegalArgumentException("La organización no puede tener más de 40 caracteres");
        }
        this.orgDL = orgDL;
    }

    public List<String> getTemIN() {
        return Collections.unmodifiableList(temIN);
    }

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

    // -------------------------
    // Métodos específicos
    // -------------------------

    /** 
     * Agrega una matrícula asociada al grupo indicado.
     * Se asume que la clase Matricula tiene un constructor Matricula(Estudiante, Grupo).
     */
    public void matricularCurso(Grupo grupo) {
        if (grupo == null) {
            throw new IllegalArgumentException("El grupo no puede ser nulo");
        }
        Matricula nuevaMatricula = new Matricula(this, grupo);
        matriculas.add(nuevaMatricula);
    }

    /**
     * Retorna una lista inmodificable con todas las matrículas del estudiante.
     */
    public List<Matricula> obtenerMatriculas() {
        return Collections.unmodifiableList(matriculas);
    }
}
