package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un curso dentro del sistema académico.
 * Contiene información general del curso, sus parámetros y los grupos asociados.
 */
public class Curso {

    // -- Atributos --

    /** Identificador único del curso (debe tener 6 caracteres). */
    String id;

    /** Nombre del curso (entre 5 y 40 caracteres). */
    String nombre;

    /** Descripción del curso (entre 5 y 400 caracteres). */
    String descripcion;

    /** Cantidad de horas por día (1 a 8). */
    int hrsDia;

    /** Modalidad del curso (presencial, virtual, etc.). */
    TipoModalidad modalidad;

    /** Número mínimo de estudiantes permitidos (1 a 5). */
    int minEstu;

    /** Número máximo de estudiantes permitidos (mínimo hasta 20). */
    int maxEstu;

    /** Tipo de curso (teórico, práctico, mixto, etc.). */
    TipoCurso tipo;

    /** Calificación mínima requerida para aprobar (0 a 100). */
    int aprobCalificacion;

    /** Lista de grupos asociados a este curso. */
    ArrayList<Grupo> grupos;

    // -- Constructor --

    /**
     * Crea un curso con todos sus parámetros definidos.
     *
     * @param pId identificador del curso
     * @param pNombre nombre del curso
     * @param pDescripcion descripción del curso
     * @param pHrs horas por día
     * @param pModalidad modalidad del curso
     * @param pMinEstu cantidad mínima de estudiantes
     * @param pMaxEstu cantidad máxima de estudiantes
     * @param pTipo tipo de curso
     * @param pAprobCalificacion calificación mínima para aprobar
     */
    public Curso(String pId, String pNombre, String pDescripcion, int pHrs, TipoModalidad pModalidad,
                 int pMinEstu, int pMaxEstu, TipoCurso pTipo, int pAprobCalificacion) {
        setId(pId);
        setNombre(pNombre);
        setDescripcion(pDescripcion);
        setHrsDia(pHrs);
        setModalidad(pModalidad);
        setMinEstu(pMinEstu);
        setMaxEstu(pMaxEstu);
        setTipo(pTipo);
        setAprobCalificacion(pAprobCalificacion);
        grupos = new ArrayList<>();
    }

    // -- Getters y Setters --

    public String getId() { return id; }
    public void setId(String pId) {
        if (checkId(pId)) this.id = pId;
        else throw new IllegalArgumentException("ID: Parámetro ingresado es inválido.");
    }

    public String getNombre() { return nombre; }
    public void setNombre(String pNombre) {
        if (checkNombre(pNombre)) this.nombre = pNombre;
        else throw new IllegalArgumentException("NOMBRE: Parámetro ingresado es inválido.");
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String pDesc) {
        if (checkDesc(pDesc)) this.descripcion = pDesc;
        else throw new IllegalArgumentException("DESCRIPCIÓN: Parámetro ingresado es inválido.");
    }

    public int getHrsDia() { return hrsDia; }
    public void setHrsDia(int hrs) {
        if (checkHrsDia(hrs)) this.hrsDia = hrs;
        else throw new IllegalArgumentException("HORAS_DIA: Parámetro ingresado es inválido.");
    }

    public TipoModalidad getModalidad() { return modalidad; }
    public void setModalidad(TipoModalidad nMod) { this.modalidad = nMod; }

    public int getMinEstu() { return minEstu; }
    public void setMinEstu(int nMin) {
        if (checkMinEstu(nMin)) this.minEstu = nMin;
        else throw new IllegalArgumentException("MIN_ESTUDIANTES: Parámetro ingresado es inválido.");
    }

    public int getMaxEstu() { return maxEstu; }
    public void setMaxEstu(int nMax) {
        if (checkMaxEstu(nMax)) this.maxEstu = nMax;
        else throw new IllegalArgumentException("MAX_ESTUDIANTES: Parámetro ingresado es inválido.");
    }

    public TipoCurso getTipo() { return tipo; }
    public void setTipo(TipoCurso nTipo) { this.tipo = nTipo; }

    public int getAprobCalificacion() { return aprobCalificacion; }
    public void setAprobCalificacion(int nCal) {
        if (checkCal(nCal)) this.aprobCalificacion = nCal;
        else throw new IllegalArgumentException("CALIFICACIÓN: Parámetro ingresado es inválido.");
    }

    // -- Validaciones auxiliares --

    /**
     * Verifica que el ID tenga exactamente 6 caracteres.
     */
    private boolean checkId(String pId) {
        return pId != null && pId.length() == 6;
    }

    /**
     * Verifica que el nombre tenga entre 5 y 40 caracteres.
     */
    private boolean checkNombre(String pNombre) {
        return pNombre != null && pNombre.length() >= 5 && pNombre.length() <= 40;
    }

    /**
     * Verifica que la descripción tenga entre 5 y 400 caracteres.
     */
    private boolean checkDesc(String pDescripcion) {
        return pDescripcion != null && pDescripcion.length() >= 5 && pDescripcion.length() <= 400;
    }

    /**
     * Verifica que las horas diarias estén entre 1 y 8.
     */
    private boolean checkHrsDia(int hrs) {
        return hrs >= 1 && hrs <= 8;
    }

    /**
     * Verifica que el mínimo de estudiantes esté entre 1 y 5.
     */
    private boolean checkMinEstu(int pMin) {
        return pMin >= 1 && pMin <= 5;
    }

    /**
     * Verifica que el máximo de estudiantes sea válido.
     */
    private boolean checkMaxEstu(int pMax) {
        return pMax >= minEstu && pMax <= 20;
    }

    /**
     * Verifica que la calificación esté entre 0 y 100.
     */
    private boolean checkCal(int cal) {
        return cal >= 0 && cal <= 100;
    }

    // -- Métodos principales --

    /**
     * Muestra una representación legible del curso y sus grupos.
     *
     * @return cadena descriptiva del curso
     */
    public String toString() {
        System.out.printf(
                "\n===== CURSO =====\n" +
                        "ID: %s\n" +
                        "Nombre: %s\n" +
                        "Descripción: %s\n" +
                        "Horas al día: %d\n" +
                        "Modalidad: %s\n" +
                        "Mínimo de estudiantes: %d\n" +
                        "Máximo de estudiantes: %d\n" +
                        "Tipo: %s\n" +
                        "Calificación mínima para aprobar: %d\n" +
                        "Grupos registrados: ",
                id, nombre, descripcion, hrsDia, modalidad, minEstu, maxEstu, tipo, aprobCalificacion);
        mostrarGrupos();
        return "";
    }

    /**
     * Crea un nuevo grupo asociado al curso.
     *
     * @param fechaInicio fecha de inicio del grupo
     * @param fechaFinal fecha de finalización del grupo
     */
    public void crearGrupo(LocalDate fechaInicio, LocalDate fechaFinal) {
        Grupo nuevoGrupo = new Grupo(this, fechaInicio, fechaFinal);
        grupos.add(nuevoGrupo);
        System.out.println("Grupo #" + nuevoGrupo.getIdGrupo() + " creado exitosamente.");
    }

    /**
     * Muestra todos los grupos registrados del curso.
     */
    public void mostrarGrupos() {
        if (grupos.isEmpty()) {
            System.out.println("No hay grupos registrados para este curso.");
            return;
        }

        System.out.println("\n========== GRUPOS DEL CURSO: " + nombre + " ==========\n");
        for (Grupo grupo : grupos) {
            System.out.println(grupo);
            System.out.println("─".repeat(50) + "\n");
        }
    }

    /**
     * Valida todos los datos del curso.
     *
     * @return {@code true} si los datos son válidos
     */
    public boolean validarDatos() {
        return checkNombre(this.nombre) &&
                checkId(this.id) &&
                checkCal(this.aprobCalificacion) &&
                checkMaxEstu(this.maxEstu) &&
                checkMinEstu(this.minEstu) &&
                checkHrsDia(this.hrsDia) &&
                checkDesc(this.descripcion);
    }
}
