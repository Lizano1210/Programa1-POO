package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para crear o editar un profesor.
 * Permite ingresar y validar los datos personales del profesor.
 */
public class AdminProfesorDialog extends JDialog {

    // -- Campos de entrada --

    /** Campo de texto para el nombre del profesor. */
    private JTextField txtNombre = new JTextField(20);

    /** Campo de texto para el primer apellido. */
    private JTextField txtApe1 = new JTextField(20);

    /** Campo de texto para el segundo apellido. */
    private JTextField txtApe2 = new JTextField(20);

    /** Campo de texto para el identificador de usuario. */
    private JTextField txtId = new JTextField(12);

    /** Campo de texto para el número de teléfono. */
    private JTextField txtTel = new JTextField(12);

    /** Campo de texto para el correo electrónico. */
    private JTextField txtCorreo = new JTextField(24);

    /** Campo de texto para la dirección. */
    private JTextField txtDir = new JTextField(30);

    /** Resultado del diálogo: profesor creado o editado (o null si se cancela). */
    private Profesor resultado;

    // -- Constructor --

    /**
     * Crea un diálogo para registrar o editar un profesor.
     *
     * @param owner ventana propietaria del diálogo
     * @param base  profesor base a editar; si es {@code null}, se creará uno nuevo
     */
    public AdminProfesorDialog(Window owner, Profesor base) {
        super(owner, "Profesor", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        // -- Formulario principal --
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        addRow(form, c, 0, "Nombre", txtNombre);
        addRow(form, c, 1, "Apellido 1", txtApe1);
        addRow(form, c, 2, "Apellido 2", txtApe2);
        addRow(form, c, 3, "ID Usuario", txtId);
        addRow(form, c, 4, "Teléfono", txtTel);
        addRow(form, c, 5, "Correo", txtCorreo);
        addRow(form, c, 6, "Dirección", txtDir);
        add(form, BorderLayout.CENTER);

        // -- Botones de acción --
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");
        actions.add(btnOk);
        actions.add(btnCancel);
        add(actions, BorderLayout.SOUTH);

        // -- Precarga de datos si es edición --
        if (base != null) {
            txtNombre.setText(base.getNombre());
            txtApe1.setText(base.getApellido1());
            txtApe2.setText(base.getApellido2());
            txtId.setText(base.getIdUsuario());
            txtId.setEnabled(false);
            txtTel.setText(base.getTelefono());
            txtCorreo.setText(base.getCorreo());
            txtDir.setText(base.getDireccion());
        }

        // -- Eventos --
        btnOk.addActionListener(e -> onGuardar(base != null));
        btnCancel.addActionListener(e -> {
            resultado = null;
            dispose();
        });

        pack();
        setLocationRelativeTo(owner);
    }

    // -- Utilidad para agregar filas al formulario --

    /**
     * Agrega una fila al formulario con su etiqueta y componente.
     *
     * @param panel panel al que se agregará la fila
     * @param c     restricciones de diseño
     * @param row   número de fila
     * @param label texto de la etiqueta
     * @param comp  componente asociado
     */
    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        panel.add(comp, c);
    }

    // -- Guardar profesor --

    /**
     * Valida los datos ingresados y crea un nuevo objeto {@link Profesor}.
     *
     * @param esEdicion indica si se está editando un registro existente
     */
    private void onGuardar(boolean esEdicion) {
        try {
            String nombre = req(txtNombre.getText(), "Nombre", 2);
            String a1 = req(txtApe1.getText(), "Apellido 1", 2);
            String a2 = req(txtApe2.getText(), "Apellido 2", 0);
            String id = esEdicion ? txtId.getText() : req(txtId.getText(), "ID Usuario", 1);
            String tel = req(txtTel.getText(), "Teléfono", 8);
            String correo = req(txtCorreo.getText(), "Correo", 5);
            String dir = req(txtDir.getText(), "Dirección", 5);

            Profesor p = new Profesor(nombre, a1, a2, id, tel, correo, dir);
            this.resultado = p;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    // -- Validación de campos --

    /**
     * Verifica que un campo cumpla una longitud mínima.
     *
     * @param v     valor a evaluar
     * @param campo nombre del campo
     * @param min   longitud mínima
     * @return texto validado
     * @throws IllegalArgumentException si el campo no cumple la longitud mínima
     */
    private String req(String v, String campo, int min) {
        String s = v == null ? "" : v.trim();
        if (s.length() < min)
            throw new IllegalArgumentException("Campo '" + campo + "' inválido (mín. " + min + ").");
        return s;
    }

    // -- Resultado --

    /**
     * Devuelve el profesor creado o editado tras cerrar el diálogo.
     *
     * @return el objeto {@link Profesor} resultante o {@code null} si se canceló
     */
    public Profesor getResultado() {
        return resultado;
    }
}

