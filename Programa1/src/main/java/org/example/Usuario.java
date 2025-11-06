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
    // Registros estáticos para validar unicidad dentro del sistema
    private static final java.util.Set<String> usedIds = new java.util.HashSet<>();
    private static final java.util.Set<String> usedEmails = new java.util.HashSet<>();

    /**
     * Valida los atributos del usuario según las reglas:
     * - nombre: 2-20
     * - apellido1: 2-20
     * - apellido2: 2-20
     * - idUsuario: >=9 y único
     * - telefono: >=8
     * - correo: formato parte1@parte2 (sin espacios) y único
     * - direccion: 5-60
     *
     * Si la validación es correcta, registra idUsuario y correo en los sets de unicidad.
     *
     * retorna true si todo es válido y se registraron id y correo, false si hay errores y los imprime.
     */
    public boolean validarDatos() {
        java.util.List<String> errores = new java.util.ArrayList<>();

        // Nombre
        if (this.nombre == null || this.nombre.trim().length() < 2 || this.nombre.trim().length() > 20) {
            errores.add("El nombre debe tener entre 2 y 20 caracteres");
        }

        // Apellidos
        if (this.apellido1 == null || this.apellido1.trim().length() < 2 || this.apellido1.trim().length() > 20) {
            errores.add("El primer apellido debe tener entre 2 y 20 caracteres");
        }
        if (this.apellido2 == null || this.apellido2.trim().length() < 2 || this.apellido2.trim().length() > 20) {
            errores.add("El segundo apellido debe tener entre 2 y 20 caracteres");
        }

        // ID usuario
        if (this.idUsuario == null || this.idUsuario.trim().length() < 9) {
            errores.add("El ID de usuario debe tener 9 o más caracteres");
        } else if (usedIds.contains(this.idUsuario.trim())) {
            errores.add("El ID de usuario ya existe en el sistema");
        }

        // Teléfono
        if (this.telefono == null || this.telefono.trim().length() < 8) {
            errores.add("El teléfono debe tener 8 o más caracteres");
        }

        // Correo
        if (this.correo == null) {
            errores.add("El correo no puede ser nulo");
        } else {
            String correoRecortado = this.correo.trim();
            if (correoRecortado.contains(" ")) {
            errores.add("El correo no debe contener espacios");
            } else {
            int indiceArroba = correoRecortado.indexOf('@');
            int indiceUltimoPunto = correoRecortado.lastIndexOf('.');
            if (indiceArroba <= 0 || indiceUltimoPunto <= indiceArroba + 1 || indiceUltimoPunto == correoRecortado.length() - 1) {
                errores.add("El correo debe tener un formato válido (parte1@parte2)");
            }
            }
            if (usedEmails.contains(correoRecortado)) {
            errores.add("El correo ya existe en el sistema");
            }
        }

        // Dirección
        if (this.direccion == null || this.direccion.trim().length() < 5 || this.direccion.trim().length() > 60) {
            errores.add("La dirección debe tener entre 5 y 60 caracteres");
        }

        // Mostrar errores o registrar unicidad
        if (!errores.isEmpty()) {
            for (String e : errores) {
                System.out.println(e);
            }
            return false;
        }

        // Registrar id y correo 
        synchronized (Usuario.class) {
            usedIds.add(this.idUsuario.trim());
            usedEmails.add(this.correo.trim());
        }

        return true;
    }

    /**
     * Genera una contraseña temporal segura y la "envía" al correo del usuario.
     * Aquí la acción de envío está simulada con un mensaje; sustituir por integración de correo real si se requiere.
     *
     * retorna la contraseña temporal generada (null si no se pudo enviar por datos inválidos)
     */
    public String cambiarContraseña() {
        // Validar que exista correo válido antes de generar
        if (this.correo == null || this.correo.trim().isEmpty()) {
            System.out.println("No se puede cambiar la contraseña: correo no proporcionado");
            return null;
        }
        // Generar contraseña temporal
        String nueva = generarContrasenaTemporal(10);
        // Simular envío por correo
        System.out.println("Se ha enviado la contraseña temporal a: " + this.correo.trim());
        // En una implementación real aquí se integraría con un servicio SMTP/API de correo
        return nueva;
    }

    // Helper privado para generar contraseña segura
    private String generarContrasenaTemporal(int longitud) {
        final String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*()-_=+";
        java.security.SecureRandom generadorSeguro = new java.security.SecureRandom();
        StringBuilder resultado = new StringBuilder(longitud);
        for (int indice = 0; indice < longitud; indice++) {
            resultado.append(caracteres.charAt(generadorSeguro.nextInt(caracteres.length())));
        }
        return resultado.toString();
    }

    public String obtenerNombreCompleto() {
        return String.format("%s %s %s", nombre, apellido1, apellido2);
    }
}
