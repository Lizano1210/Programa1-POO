package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * Panel de login con identificación y contraseña.
 * Expone callbacks para Ingresar y Recuperar.
 */
public class LoginPanel extends JPanel {

    // --- Controles ---
    private final JTextField txtIdentificacion = new JTextField(18);
    private final JPasswordField pwdContrasena = new JPasswordField(18);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JButton btnRecuperar = new JButton("Olvidé mi contraseña");
    private final JCheckBox chkMostrar = new JCheckBox("Mostrar");
    private final JLabel lblMensaje = new JLabel(" ");

    // Carácter de enmascaramiento original del password field (solución A)
    private final char echoDefault;

    // --- Callbacks ---
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

        // Fila 3: recuperar
        c.gridx = 1; c.gridy = 3; c.gridwidth = 1; c.weightx = 0;
        add(btnRecuperar, c);

        // Fila 4: ingresar
        c.gridx = 1; c.gridy = 4;
        add(btnIngresar, c);

        // Fila 5: mensajes
        c.gridx = 0; c.gridy = 5; c.gridwidth = 3;
        lblMensaje.setForeground(new Color(180, 0, 0));
        add(lblMensaje, c);

        // Accesibilidad: mnemonics y relación label-for
        lblId.setDisplayedMnemonic('I');
        lblId.setLabelFor(txtIdentificacion);
        lblPass.setDisplayedMnemonic('C');
        lblPass.setLabelFor(pwdContrasena);

        // Guardar el echo char original (solución A)
        this.echoDefault = pwdContrasena.getEchoChar();

        // Toggle mostrar/ocultar contraseña
        chkMostrar.addActionListener(e ->
                pwdContrasena.setEchoChar(chkMostrar.isSelected() ? (char) 0 : echoDefault));

        // Enter como atajo para "Ingresar"
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "login");
        getActionMap().put("login", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                intentarLogin();
            }
        });

        // Botones
        btnIngresar.addActionListener(e -> intentarLogin());
        btnRecuperar.addActionListener(e -> {
            if (onRecover != null) onRecover.accept(txtIdentificacion.getText().trim());
        });
    }

    /** Asigna callback que se invoca al presionar Ingresar. */
    public void setOnLogin(BiConsumer<String, char[]> onLogin) { this.onLogin = onLogin; }

    /** Asigna callback que se invoca al presionar Recuperar. */
    public void setOnRecover(java.util.function.Consumer<String> onRecover) { this.onRecover = onRecover; }

    /** Muestra un mensaje bajo el formulario. */
    public void setMensaje(String msg) { lblMensaje.setText(msg == null ? " " : msg); }

    // --- Internos ---
    private void intentarLogin() {
        if (onLogin != null) onLogin.accept(txtIdentificacion.getText().trim(), pwdContrasena.getPassword());
    }
}

