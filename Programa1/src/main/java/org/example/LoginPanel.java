package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 Panel de login con identificación y contraseña.
 */
public class LoginPanel extends JPanel {
    // Componentes
    private final JTextField txtIdentificacion = new JTextField(18);
    private final JPasswordField pwdContrasena = new JPasswordField(18);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JButton btnRecuperar = new JButton("Olvidé mi contraseña");
    private final JCheckBox chkMostrar = new JCheckBox("Mostrar");
    private final JLabel lblMensaje = new JLabel(" ");
    private final char echoDefault; // para tapar contraseña

    // callbacks que usamos para validar lo que ingrese el usuario
    private BiConsumer<String, char[]> onLogin;
    private java.util.function.Consumer<String> onRecover;

    /** Construye el formulario de login. */
    public LoginPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Iniciar sesión");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        // Fila 0: título
        c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
        add(title, c);

        // Fila 1: identificación
        c.gridwidth = 1; c.weightx = 0;
        JLabel lblId = new JLabel("Identificación");
        c.gridx = 0; c.gridy = 1;
        add(lblId, c);

        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        add(txtIdentificacion, c);

        c.gridx = 2; c.gridy = 1; c.weightx = 0;
        add(Box.createHorizontalStrut(1), c);

        // Fila 2: contraseña + mostrar
        JLabel lblPass = new JLabel("Contraseña");
        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        add(lblPass, c);

        c.gridx = 1; c.gridy = 2; c.weightx = 1;
        add(pwdContrasena, c);

        c.gridx = 2; c.gridy = 2; c.weightx = 0;
        add(chkMostrar, c);

        // Fila 3: ingresar
        c.gridx = 1; c.gridy = 3; c.gridwidth = 1; c.weightx = 0;
        add(btnIngresar, c);

        // Fila 4: recuperar
        c.gridx = 1; c.gridy = 4;
        add(btnRecuperar, c);

        // Fila 5: mensajes
        c.gridx = 0; c.gridy = 5; c.gridwidth = 3;
        lblMensaje.setForeground(new Color(180, 0, 0));
        add(lblMensaje, c);

        // Guardar el echo char original
        this.echoDefault = pwdContrasena.getEchoChar();

        // Toggle mostrar/ocultar contraseña
        chkMostrar.addActionListener(e ->
                pwdContrasena.setEchoChar(chkMostrar.isSelected() ? (char) 0 : echoDefault));

        // Botones
        btnIngresar.addActionListener(e -> intentarLogin());
        btnRecuperar.addActionListener(e -> {
            if (onRecover != null) onRecover.accept(txtIdentificacion.getText().trim());
        });
    }

    // callback que se asigna al ingresar
    public void setOnLogin(BiConsumer<String, char[]> onLogin) { this.onLogin = onLogin; }

    // callback que se activa al recuperar contraseña
    public void setOnRecover(java.util.function.Consumer<String> onRecover) { this.onRecover = onRecover; }

    // muestra los mensajes de error
    public void setMensaje(String msg) { lblMensaje.setText(msg == null ? " " : msg); }

    private void intentarLogin() {
        // Validación local antes de delegar
        String id = txtIdentificacion.getText().trim();
        char[] pass = pwdContrasena.getPassword();

        if (id.isEmpty()) {
            setMensaje("Ingrese su identificación.");
            txtIdentificacion.requestFocusInWindow();
            return;
        }
        if (pass == null || pass.length == 0) {
            setMensaje("Ingrese su contraseña.");
            pwdContrasena.requestFocusInWindow();
            return;
        }

        if (onLogin != null) {
            try {
                onLogin.accept(id, pass);
            } catch (IllegalArgumentException ex) {
                // Errores mostrados al usuario
                setMensaje(ex.getMessage() == null ? "Datos inválidos." : ex.getMessage());
            } catch (Exception ex) {
                setMensaje("Ocurrió un error al iniciar sesión.");
            }
        }
    }
}


