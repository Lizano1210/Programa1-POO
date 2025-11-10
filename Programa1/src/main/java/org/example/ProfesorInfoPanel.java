package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Panel de interfaz gráfica que muestra y permite editar la información personal del profesor.
 * <p>
 * Permite actualizar datos básicos como el correo, teléfono y dirección del profesor,
 * mientras que los campos de nombre e identificación se mantienen bloqueados
 * para evitar modificaciones no autorizadas.
 * </p>
 */
public class ProfesorInfoPanel extends JPanel {

    // -- Servicios y datos --

    /** Servicio de usuarios utilizado para sincronizar los cambios. */
    private final UsuarioServiceMem usuarioService;

    /** Profesor autenticado al que pertenece la información. */
    private final Profesor profesor;

    // -- Componentes visuales --

    private final JTextField txtNombre = new JTextField(30);
    private final JTextField txtId = new JTextField(15);
    private final JTextField txtCorreo = new JTextField(30);
    private final JTextField txtTelefono = new JTextField(20);
    private final JTextArea txtDireccion = new JTextArea(3, 30);
    private final JButton btnGuardar = new JButton("Guardar cambios");

    // -- Constructor --

    /**
     * Crea el panel “Mi información” del profesor.
     *
     * @param usuarioService servicio de usuarios
     * @param profesor profesor autenticado
     */
    public ProfesorInfoPanel(UsuarioServiceMem usuarioService, Profesor profesor) {
        this.usuarioService = usuarioService;
        this.profesor = profesor;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(construirForm(), BorderLayout.CENTER);
        add(construirSouth(), BorderLayout.SOUTH);

        cargarDatos();
        wire();
    }

    // -- Construcción de la interfaz --

    /** Construye el formulario principal del panel. */
    private JComponent construirForm() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        int r = 0;

        txtNombre.setEditable(false);
        txtId.setEditable(false);
        txtDireccion.setLineWrap(true);
        txtDireccion.setWrapStyleWord(true);

        addRow(form, c, r++, "Nombre completo:", txtNombre);
        addRow(form, c, r++, "Identificación:", txtId);
        addRow(form, c, r++, "Correo:", txtCorreo);
        addRow(form, c, r++, "Teléfono:", txtTelefono);
        addRow(form, c, r++, "Dirección:", new JScrollPane(txtDireccion));

        return form;
    }

    /** Construye el panel inferior con los botones de acción. */
    private JPanel construirSouth() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnGuardar);
        return south;
    }

    /** Agrega una fila con etiqueta y campo al formulario. */
    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1; p.add(comp, c);
    }

    // -- Carga y eventos --

    /** Carga los datos del profesor en los campos del formulario. */
    private void cargarDatos() {
        String nombre = (profesor.getNombre() == null ? "" : profesor.getNombre()) + " " +
                (profesor.getApellido1() == null ? "" : profesor.getApellido1()) + " " +
                (profesor.getApellido2() == null ? "" : profesor.getApellido2());

        txtNombre.setText(nombre.trim());
        txtId.setText(profesor.getIdUsuario());
        txtCorreo.setText(profesor.getCorreo());
        txtTelefono.setText(profesor.getTelefono());
        txtDireccion.setText(profesor.getDireccion());
    }

    /**
     * Configura los eventos de interacción del panel.
     * <p>
     * Al presionar “Guardar cambios”, los datos modificados se validan
     * y se actualizan directamente en el objeto {@link Profesor}.
     * </p>
     */
    private void wire() {
        btnGuardar.addActionListener(e -> {
            try {
                String correo = txtCorreo.getText().trim();
                String tel = txtTelefono.getText().trim();
                String dir = txtDireccion.getText().trim();

                if (correo.isEmpty()) throw new IllegalArgumentException("El correo no puede estar vacío.");
                if (tel.isEmpty()) throw new IllegalArgumentException("El teléfono no puede estar vacío.");

                profesor.setCorreo(correo);
                profesor.setTelefono(tel);
                profesor.setDireccion(dir);

                JOptionPane.showMessageDialog(this, "Datos actualizados correctamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

