package org.example;

// Imports
import java.time.LocalDate;


public abstract class Usuario {
    // Atributos
    String nombre;
    String apellido1;
    String apellido2;
    String idUsuario;
    String telefono;
    String correo;
    String direccion;
    LocalDate fechaRegistro;

    // Constructor
    public Usuario(String nombre, String apellido1, String apellido2, String idUsuario, String telefono, String correo, String direccion) {
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.idUsuario = idUsuario;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.fechaRegistro = LocalDate.now();
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getApellido1() {
        return apellido1;
    }
    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }
    public String getApellido2() {
        return apellido2;
    }
    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }
    public String getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // Métodos
    public void validarDatos(){
        if(this.nombre.length() < 2 || this.nombre.length() > 20){
            System.out.println("El nombre debe tener entre 2 y 20 caracteres");
        }
        if(this.apellido1.length() < 2 || this.apellido1.length() > 20){
            System.out.println("El primer apellido debe tener entre 2 y 20 caracteres");
        }
        if(this.apellido2.length() < 2 || this.apellido2.length() > 20){
            System.out.println("El segundo apellido debe tener entre 2 y 20 caracteres");
        }
        if(this.idUsuario.length() < 9){
            System.out.println("El ID de usuario debe tener de 9 a mas caracteres");
        }
        if(this.telefono.length() != 8){
            System.out.println("El teléfono debe tener 8 caracteres");
        }
        if(!this.correo.contains("@") || !this.correo.contains(".")){
            System.out.println("El correo debe ser válido");
        }
        if(this.direccion.length() < 5 || this.direccion.length() > 60){
            System.out.println("La dirección debe tener entre 5 y 60 caracteres");
        }

    }

    public String obtenerNombreCompleto() {
        return String.format("%s %s %s", nombre, apellido1, apellido2);
    }
}
