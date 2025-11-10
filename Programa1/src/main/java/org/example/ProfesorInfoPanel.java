package org.example;

import javax.swing.*;
import java.awt.*;

/** Panel "Mi información" para Profesor: permite actualizar teléfono, correo y dirección. */
public class ProfesorInfoPanel extends JPanel {

    private final UsuarioServiceMem usuarioService;
    private final Profesor profesor;

    private final JTextField txtNombre = new JTextField(30);
    private final JTextField txtId = new JTextField(15);
    private final JTextField txtCorreo = new JTextField(30);
    private final JTextField txtTelefono = new JTextField(20);
    private final JTextArea  txtDireccion = new JTextArea(3, 30);

    private final JButton btnGuardar = new JButton("Guardar cambios");

    public ProfesorInfoPanel(UsuarioServiceMem usuarioService, Profesor profesor) {
        this.usuarioService = usuarioService;
        this.profesor = profesor;

        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        add(construirForm(), BorderLayout.CENTER);
        add(construirSouth(), BorderLayout.SOUTH);

        cargarDatos();
        wire();
    }

    private JComponent construirForm() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        int r = 0;

        txtNombre.setEditable(false);
        txtId.setEditable(false);
        txtDireccion.setLineWrap(true);
        txtDireccion.setWrapStyleWord(true);

        addRow(form, c, r++, "Nombre completo:", txtNombre);
        addRow(form, c, r++, "Identificación:",  txtId);
        addRow(form, c, r++, "Correo:",          txtCorreo);
        addRow(form, c, r++, "Teléfono:",        txtTelefono);
        addRow(form, c, r++, "Dirección:",       new JScrollPane(txtDireccion));

        return form;
    }

    private JPanel construirSouth() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnGuardar);
        return south;
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx=0; c.gridy=row; c.weightx=0; p.add(new JLabel(label), c);
        c.gridx=1; c.gridy=row; c.weightx=1; p.add(comp, c);
    }

    private void cargarDatos() {
        String nombre = (profesor.getNombre()==null?"":profesor.getNombre()) + " " +
                (profesor.getApellido1()==null?"":profesor.getApellido1()) + " " +
                (profesor.getApellido2()==null?"":profesor.getApellido2());
        txtNombre.setText(nombre.trim());
        txtId.setText(profesor.getIdUsuario());
        txtCorreo.setText(profesor.getCorreo());
        txtTelefono.setText(profesor.getTelefono());
        txtDireccion.setText(profesor.getDireccion());
    }

    private void wire() {
        btnGuardar.addActionListener(e -> {
            try {
                // Validaciones mínimas
                String correo = txtCorreo.getText().trim();
                String tel    = txtTelefono.getText().trim();
                String dir    = txtDireccion.getText().trim();
                if (correo.isEmpty()) throw new IllegalArgumentException("El correo no puede estar vacío.");
                if (tel.isEmpty())    throw new IllegalArgumentException("El teléfono no puede estar vacío.");

                // Persistimos en el modelo en memoria
                profesor.setCorreo(correo);
                profesor.setTelefono(tel);
                profesor.setDireccion(dir);

                // Si tu UsuarioServiceMem necesita “upsert”, puedes llamarlo aquí.
                // En este proyecto, con listas en memoria, basta con mutar el objeto.

                JOptionPane.showMessageDialog(this, "Datos actualizados.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
