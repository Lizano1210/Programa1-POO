package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de autenticación con soporte de contraseña temporal (one-shot).
 * Requiere Password (BCrypt) y maneja flujos de login, recuperación y cambio.
 */
public class Autenticacion {

    // Simulación de almacenamiento (reemplaza por tu capa de persistencia)
    private final Map<String, Password> passwords = new HashMap<>(); // idUsuario -> credencial
    private final Map<String, Roles> roles = new HashMap<>();     // idUsuario -> rol
    private final Map<String, String> correos = new HashMap<>();     // idUsuario -> correo

    /**
     * Resultado de login: rol y si el usuario debe cambiar la clave (temporal).
     */
    public static class ResultadoLogin {
        public final Roles rol;
        public final boolean requiereCambio;
        public ResultadoLogin(Roles rol, boolean requiereCambio) {
            this.rol = rol; this.requiereCambio = requiereCambio;
        }
    }

    /** Registra/actualiza un usuario en memoria (id, correo, rol, Password ya encriptada). */
    public void upsertUsuario(String id, String correo, Roles rol, Password credencial) {
        correos.put(id, correo);
        roles.put(id, rol);
        passwords.put(id, credencial);
    }

    /**
     * Login con identificación y contraseña.
     * Retorna rol y si debe cambiar contraseña (cuando es temporal).
     */
    public ResultadoLogin login(String identificacion, char[] password) {
        if (!existe(identificacion) || password == null || password.length == 0) return null;
        Password cred = passwords.get(identificacion);
        if (cred == null || !cred.verificar(new String(password))) return null; // inválido

        // Si es temporal: permitir el acceso SOLO para forzar cambio inmediato
        boolean requiereCambio = cred.getTemp(); // one-shot para establecer nueva clave
        return new ResultadoLogin(roles.get(identificacion), requiereCambio);
    }

    /**
     * Inicia recuperación: genera contraseña temporal, la marca como temporal y "envía" al correo.
     * Devuelve true si pudo generarla.
     */
    public boolean recuperarContrasena(String identificacion) {
        if (!existe(identificacion)) return false;
        Password cred = passwords.get(identificacion);
        if (cred == null) return false;
        String temporalPlano = cred.tempPassword(); // genera + encripta + marca temp=true
        // Enviar por correo (stub). La temporal sirve una única vez para entrar y cambiarla.
        enviarCorreo(correos.get(identificacion),
                "Tu contraseña temporal es: " + temporalPlano + ". Úsala para ingresar y debes cambiarla.");
        return true;
    }

    /**
     * Cambio normal de contraseña: requiere la contraseña actual (no temporal).
     */
    public boolean cambiarContrasena(String identificacion, char[] actual, String nueva) {
        if (!existe(identificacion) || actual == null || nueva == null || nueva.isBlank()) return false;
        Password cred = passwords.get(identificacion);
        if (cred == null || cred.getTemp()) return false; // si es temporal, use establecerNuevaTrasTemporal
        if (!cred.verificar(new String(actual))) return false;
        cred.encriptar(nueva);
        cred.setTemp(false);
        return true;
    }

    /**
     * Establece una nueva contraseña DESPUÉS de ingresar con una temporal (one-shot).
     * Solo funciona si la clave actual está marcada como temporal.
     */
    public boolean establecerNuevaTrasTemporal(String identificacion, String nueva) {
        if (!existe(identificacion) || nueva == null || nueva.isBlank()) return false;
        Password cred = passwords.get(identificacion);
        if (cred == null || !cred.getTemp()) return false; // debe venir de temporal
        cred.encriptar(nueva);
        cred.setTemp(false); // ya quedó definitiva
        return true;
    }

    // --- Utilitarios ---

    private boolean existe(String id) {
        return id != null && roles.containsKey(id) && passwords.containsKey(id);
    }

    /** Simula envío de correo (reemplaza por tu implementación real). */
    private void enviarCorreo(String correo, String mensaje) {
        System.out.println("[EMAIL a " + correo + "] " + mensaje);
    }
}
