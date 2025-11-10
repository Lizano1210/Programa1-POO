package org.example;

import java.time.LocalDate;

/**
 * Clase abstracta que representa un usuario dentro del sistema.
 * <p>
 * Define los atributos comunes para todos los tipos de usuarios
 * (por ejemplo, {@link Estudiante}, {@link Profesor} o {@link Administrador}),
 * así como la lógica de validación y manejo de credenciales.
 * </p>
 */
public abstract class Usuario {

    // -- Atributos personales --

    /** Nombre del usuario. */
    String nombre;

    /** Primer apellido del usuario. */
    String apellido1;

    /** Segundo apellido del usuario. */
    String apellido2;

    /** Identificador único del usuario. */
    String idUsuario;

    /** Número telefónico del usuario. */
    String telefono;

    /** Correo electrónico del usuario. */
    String correo;

    /** Dirección física del usuario. */
    String direccion;

    /** Fecha de registro del usuario en el sistema. */
    LocalDate fechaRegistro;

    // -- Constructor --

    /**
     * Crea un nuevo usuario con los datos básicos proporcionados.
     *
     * @param nombre nombre del usuario
     * @param apellido1 primer apellido
     * @param apellido2 segundo apellido
     * @param idUsuario identificador único del usuario
     * @param telefono número telefónico
     * @param correo correo electrónico
     * @param direccion dirección física
     */
    public Usuario(String nombre, String apellido1, String apellido2, String idUsuario,
                   String telefono, String correo, String direccion) {
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.idUsuario = idUsuario;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.fechaRegistro = LocalDate.now();
    }

    // -- Getters y Setters --

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido1() { return apellido1; }
    public void setApellido1(String apellido1) { this.apellido1 = apellido1; }

    public String getApellido2() { return apellido2; }
    public void setApellido2(String apellido2) { this.apellido2 = apellido2; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    // -- Validación y control de unicidad --

    /** Conjunto de IDs registrados para asegurar unicidad. */
    private static final java.util.Set<String> usedIds = new java.util.HashSet<>();

    /** Conjunto de correos registrados para asegurar unicidad. */
    private static final java.util.Set<String> usedEmails = new java.util.HashSet<>();

    /**
     * Valida los atributos del usuario según las reglas de formato y unicidad.
     * <ul>
     *     <li>Nombre y apellidos: entre 2 y 20 caracteres.</li>
     *     <li>ID de usuario: al menos 9 caracteres, único.</li>
     *     <li>Teléfono: al menos 8 caracteres.</li>
     *     <li>Correo: formato válido (parte1@parte2) y único.</li>
     *     <li>Dirección: entre 5 y 60 caracteres.</li>
     * </ul>
     * Si los datos son válidos, se registran el ID y el correo en los conjuntos de unicidad.
     *
     * @return {@code true} si los datos son válidos, {@code false} si se detectan errores
     */
    public boolean validarDatos() {
        java.util.List<String> errores = new java.util.ArrayList<>();

        // Validación básica de nombre y apellidos
        if (nombre == null || nombre.trim().length() < 2 || nombre.trim().length() > 20)
            errores.add("El nombre debe tener entre 2 y 20 caracteres");

        if (apellido1 == null || apellido1.trim().length() < 2 || apellido1.trim().length() > 20)
            errores.add("El primer apellido debe tener entre 2 y 20 caracteres");

        if (apellido2 == null || apellido2.trim().length() < 2 || apellido2.trim().length() > 20)
            errores.add("El segundo apellido debe tener entre 2 y 20 caracteres");

        // ID de usuario
        if (idUsuario == null || idUsuario.trim().length() < 9)
            errores.add("El ID de usuario debe tener 9 o más caracteres");
        else if (usedIds.contains(idUsuario.trim()))
            errores.add("El ID de usuario ya existe en el sistema");

        // Teléfono
        if (telefono == null || telefono.trim().length() < 8)
            errores.add("El teléfono debe tener 8 o más caracteres");

        // Correo
        if (correo == null) errores.add("El correo no puede ser nulo");
        else {
            String correoRecortado = correo.trim();
            if (correoRecortado.contains(" "))
                errores.add("El correo no debe contener espacios");
            else {
                int indiceArroba = correoRecortado.indexOf('@');
                int indiceUltimoPunto = correoRecortado.lastIndexOf('.');
                if (indiceArroba <= 0 || indiceUltimoPunto <= indiceArroba + 1 || indiceUltimoPunto == correoRecortado.length() - 1)
                    errores.add("El correo debe tener un formato válido (parte1@parte2)");
            }
            if (usedEmails.contains(correoRecortado))
                errores.add("El correo ya existe en el sistema");
        }

        // Dirección
        if (direccion == null || direccion.trim().length() < 5 || direccion.trim().length() > 60)
            errores.add("La dirección debe tener entre 5 y 60 caracteres");

        // Mostrar errores o registrar
        if (!errores.isEmpty()) {
            errores.forEach(System.out::println);
            return false;
        }

        synchronized (Usuario.class) {
            usedIds.add(idUsuario.trim());
            usedEmails.add(correo.trim());
        }

        return true;
    }

    // -- Gestión de contraseñas --

    /**
     * Genera una contraseña temporal segura y simula su envío al correo del usuario.
     * <p>
     * En una implementación real, este método integraría el envío de correos mediante SMTP o API externa.
     * </p>
     *
     * @return contraseña temporal generada, o {@code null} si no hay correo válido
     */
    public String cambiarContraseña() {
        if (correo == null || correo.trim().isEmpty()) {
            System.out.println("No se puede cambiar la contraseña: correo no proporcionado");
            return null;
        }
        String nueva = generarContrasenaTemporal(10);
        System.out.println("Se ha enviado la contraseña temporal a: " + correo.trim());
        return nueva;
    }

    /** Genera una contraseña aleatoria segura con la longitud indicada. */
    private String generarContrasenaTemporal(int longitud) {
        final String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*()-_=+";
        java.security.SecureRandom generadorSeguro = new java.security.SecureRandom();
        StringBuilder resultado = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            resultado.append(caracteres.charAt(generadorSeguro.nextInt(caracteres.length())));
        }
        return resultado.toString();
    }

    // -- Utilidades --

    /** Devuelve el nombre completo del usuario (nombre + apellidos). */
    public String obtenerNombreCompleto() {
        return String.format("%s %s %s", nombre, apellido1, apellido2);
    }
}
