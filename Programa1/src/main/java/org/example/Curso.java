package org.example;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Curso {
    // Atributos
    String id;
    String nombre;
    String descripcion;
    int hrsDia;
    TipoModalidad modalidad;
    int minEstu;
    int maxEstu;
    TipoCurso tipo;
    int aprobCalificacion;
    ArrayList<Grupo> grupos;

    // Constructor
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

    // Setters | Getters
    public String getId() {return id;}
    public void setId(String pId) {
        if (checkId(pId)) {
            this.id = pId;
        } else {throw new IllegalArgumentException("ID: PARAMETRO INGRESADO ES INVALIDO!");}
    }

    public String getNombre() {return nombre;}
    public void setNombre(String pNombre) {
        if (checkNombre(pNombre)) {
            this.nombre = pNombre;
        } else {throw new IllegalArgumentException("NOMBRE: PARAMETRO INGRESADO ES INVALIDO!");}
    }

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String pDesc) {
        if (checkDesc(pDesc)) {
            this.descripcion = pDesc;
        } else {throw new IllegalArgumentException("DESCRIPCION: PARAMETRO INGRESADO ES INVALIDO!");}
    }

    public int getHrsDia() {return hrsDia;}
    public void setHrsDia(int hrs) {
        if (checkHrsDia(hrs)) {
            this.hrsDia = hrs;
        } else {throw new IllegalArgumentException("HORAS_DIA: PARAMETRO INGRESADO ES INVALIDO!");}
    }

    public TipoModalidad getModalidad() {return modalidad;}
    public void setModalidad(TipoModalidad nMod) {this.modalidad = nMod;}

    public int getMinEstu() {return minEstu;}
    public void setMinEstu(int nMin) {
        if (checkMinEstu(nMin)) {
            this.minEstu = nMin;
        } else {throw new IllegalArgumentException("MIN_ESTUDIANTES: PARAMETRO INGRESADO ES INVALIDO!");}
    }

    public int getMaxEstu() {return maxEstu;}
    public void setMaxEstu(int nMax) {
        if (checkMaxEstu(nMax)) {
            this.maxEstu = nMax;
        } else {throw new IllegalArgumentException("MAX_ESTUDIANTES: PARAMETRO INGRESADO ES INVALIDO!");}
    }

    public TipoCurso getTipo() {return tipo;}
    public void setTipo(TipoCurso nTipo) {this.tipo = nTipo;}

    public int getAprobCalificacion() {return aprobCalificacion;}
    public void setAprobCalificacion(int nCal) {
        if (checkCal(nCal)) {
            this.aprobCalificacion = nCal;
        } else {throw new IllegalArgumentException("CALIFICACION: PARAMETRO INGRESADO ES INVALIDO!");}
    }

    // Validaciones
    private boolean checkId(String pId) {
        if (pId.length() == 6) {
            return true;
        } else { return false; }
    }

    private boolean checkNombre(String pNombre) {
        if (pNombre.length() < 5) {
            return false;
        } else if (pNombre.length() > 40) {
            return false;
        } else { return true; }
    }

    private boolean checkDesc(String pDescripcion) {
        if (pDescripcion.length() < 5) {
            return false;
        } else if (pDescripcion.length() > 400) {
            return false;
        } else { return true; }
    }

    private boolean checkHrsDia(int hrs) {
        if (hrs < 1) {return false;} else if (hrs > 8) { return false; } else { return true; }
        }

    private boolean checkMinEstu(int pMin) {
        if (pMin < 1) {return false;} else if (pMin > 5) { return false; } else { return true; }
    }

    private boolean checkMaxEstu(int pMax) {
        if (pMax < minEstu) {return false;} else if (pMax > 20) { return false; } else { return true; }
    }

    private boolean checkCal(int cal) {
        if (cal < 0) {return false;} else if (cal > 100) { return false; } else { return true; }
    }

    // Métodos

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
                id,
                nombre,
                descripcion,
                hrsDia,
                modalidad,
                minEstu,
                maxEstu,
                tipo,
                aprobCalificacion);
        mostrarGrupos();
        return "";
    }

    public void crearGrupo(LocalDate fechaInicio, LocalDate fechaFinal) {
        Grupo nuevoGrupo = new Grupo(this, fechaInicio, fechaFinal);
        grupos.add(nuevoGrupo);
        System.out.println("Grupo #" + nuevoGrupo.getIdGrupo() + " creado exitosamente.");
    }

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

    public boolean validarDatos() {
        return checkNombre(this.nombre) && checkId(this.id) && checkCal(this.aprobCalificacion) &&
                checkMaxEstu(this.maxEstu) && checkMinEstu(this.minEstu) && checkHrsDia(this.hrsDia) && checkDesc(this.descripcion);
    }
}