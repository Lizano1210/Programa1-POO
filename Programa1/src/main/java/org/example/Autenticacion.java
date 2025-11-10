package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de autenticación con soporte de contraseña temporal (one-shot).
 * Administra credenciales en memoria y flujos de:
 * <ul>
 *   <li>Inicio de sesión</li>
 *   <li>Recuperación de contraseña (genera clave temporal)</li>
 *   <li>Cambio de contraseña (normal y posterior a temporal)</li>
 * </ul>
 * <p>Nota: en una app real, este almacenamiento debería reemplazarse por una
 * capa de persistencia y un servicio de correo.</p>
 */
public class Autenticacion {

    // -- Almacenamiento simulado --

    /** Mapa de contraseñas por idUsuario (credencial ya encriptada). */
    private final Map<String, Password> passwords = new HashMap<>();

    /** Mapa de roles por idUsuario. */
    private final Map<String, Roles> roles = new HashMap<>();

    /** Mapa de correos por idUsuario. */
    private final Map<String, String> correos = new HashMap<>();

    // -- DTO de resultado --

    /**
     * Resultado de login: entrega el rol y si el usuario debe cambiar la contraseña
     * (por ejemplo, cuando ingresó con una clave temporal).
     */
    public static class ResultadoLogin {
        /** Rol del usuario autenticado. */
        public final Roles rol;
        /** Indica si el usuario debe cambiar la contraseña tras ingresar. */
        public final boolean requiereCambio;

        /**
         * Crea un resultado de login.
         *
         * @param rol rol del usuario
         * @param requiereCambio {@code true} si debe cambiar la contraseña
         */
        public ResultadoLogin(Roles rol, boolean requiereCambio) {
            this.rol = rol;
            this.requiereCambio = requiereCambio;
        }
    }

    // -- Alta/actualización de usuarios --

    /**
     * Registra o actualiza un usuario en memoria.
     *
     * @param id         identificador del usuario
     * @param correo     correo asociado
     * @param rol        rol del usuario
     * @param credencial objeto {@link Password} ya encriptado
     */
    public void upsertUsuario(String id, String correo, Roles rol, Password credencial) {
        correos.put(id, correo);
        roles.put(id, rol);
        passwords.put(id, credencial);
    }

    // -- Autenticación --

    /**
     * Realiza el inicio de sesión.
     *
     * @param identificacion id del usuario
     * @param password       contraseña en texto (arreglo de chars)
     * @return {@link ResultadoLogin} si es válido; {@code null} si falla
     */
    public ResultadoLogin login(String identificacion, char[] password) {
        if (!existe(identificacion) || password == null || password.length == 0) return null;
        Password cred = passwords.get(identificacion);
        if (cred == null || !cred.verificar(new String(password))) return null;

        boolean requiereCambio = cred.getTemp(); // si es temporal, forzar cambio
        return new ResultadoLogin(roles.get(identificacion), requiereCambio);
    }

    // -- Recuperación y cambio de contraseña --

    /**
     * Inicia la recuperación de contraseña: genera una clave temporal, la marca como temporal
     * (de un solo uso para forzar cambio inmediato) y la "envía" por correo.
     *
     * @param identificacion id del usuario
     * @return {@code true} si se generó y envió la temporal; {@code false} si no procede
     */
    public boolean recuperarContrasena(String identificacion) {
        if (!existe(identificacion)) return false;
        Password cred = passwords.get(identificacion);
        if (cred == null) return false;
        String temporalPlano = cred.tempPassword(); // genera + encripta + marca temp=true
        enviarCorreo(correos.get(identificacion),
                "Tu contraseña temporal es: " + temporalPlano + ". Úsala para ingresar y debes cambiarla.");
        return true;
    }

    /**
     * Cambio normal de contraseña (cuando la actual no es temporal).
     *
     * @param identificacion id del usuario
     * @param actual         contraseña actual
     * @param nueva          nueva contraseña
     * @return {@code true} si se cambió correctamente
     */
    public boolean cambiarContrasena(String identificacion, char[] actual, String nueva) {
        if (!existe(identificacion) || actual == null || nueva == null || nueva.isBlank()) return false;
        Password cred = passwords.get(identificacion);
        if (cred == null || cred.getTemp()) return false; // si es temporal, usar establecerNuevaTrasTemporal
        if (!cred.verificar(new String(actual))) return false;
        cred.encriptar(nueva);
        cred.setTemp(false);
        return true;
    }

    /**
     * Establece una nueva contraseña después de ingresar con una clave temporal.
     *
     * @param identificacion id del usuario
     * @param nueva          nueva contraseña definitiva
     * @return {@code true} si se estableció correctamente
     */
    public boolean establecerNuevaTrasTemporal(String identificacion, String nueva) {
        if (!existe(identificacion) || nueva == null || nueva.isBlank()) return false;
        Password cred = passwords.get(identificacion);
        if (cred == null || !cred.getTemp()) return false; // debe venir de temporal
        cred.encriptar(nueva);
        cred.setTemp(false);
        return true;
    }

    // -- Auxiliares --

    /**
     * Verifica existencia básica del usuario en los mapas internos.
     *
     * @param id identificador del usuario
     * @return {@code true} si existe
     */
    private boolean existe(String id) {
        return id != null && roles.containsKey(id) && passwords.containsKey(id);
    }

    /**
     * Simula el envío de un correo electrónico.
     *
     * @param correo  destinatario
     * @param mensaje contenido del mensaje
     */
    private void enviarCorreo(String correo, String mensaje) {
        System.out.println("[EMAIL a " + correo + "] " + mensaje);
    }
}
