package org.example;

import javax.swing.*;
import java.awt.*;

public class EstudiantePerfilPanel extends JPanel {

    private final Estudiante estudiante;
    private final UsuarioService usuarioService;

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtApe1 = new JTextField(20);
    private final JTextField txtApe2 = new JTextField(20);
    private final JTextField txtId = new JTextField(12);
    private final JTextField txtCorreo = new JTextField(24);
    private final JTextField txtTelefono = new JTextField(12);
    private final JTextField txtDireccion = new JTextField(30);
    private final JTextField txtOrgDL = new JTextField(20);
    private final JTextField txtTemIN = new JTextField(30);

    public EstudiantePerfilPanel(Estudiante estudiante, UsuarioService usuarioService) {
        this.estudiante = estudiante;
        this.usuarioService = usuarioService;

        setLayout(new BorderLayout(8,8));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        addRow(form, c, 0, "Nombre", txtNombre);
        addRow(form, c, 1, "Apellido 1", txtApe1);
        addRow(form, c, 2, "Apellido 2", txtApe2);
        addRow(form, c, 3, "ID", txtId);
        addRow(form, c, 4, "Correo", txtCorreo);
        addRow(form, c, 5, "Teléfono", txtTelefono);
        addRow(form, c, 6, "Dirección", txtDireccion);
        addRow(form, c, 7, "Org. origen", txtOrgDL);
        addRow(form, c, 8, "Temas interés (coma)", txtTemIN);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar cambios");
        actions.add(btnGuardar);
        add(actions, BorderLayout.SOUTH);

        // Cargar datos
        cargar();

        // Bloquear campos solo lectura
        txtNombre.setEnabled(false);
        txtApe1.setEnabled(false);
        txtApe2.setEnabled(false);
        txtId.setEnabled(false);
        txtCorreo.setEnabled(false);
        txtOrgDL.setEnabled(false);
        txtTemIN.setEnabled(false);

        btnGuardar.addActionListener(e -> onGuardar());
    }

    private void cargar() {
        txtNombre.setText(estudiante.getNombre());
        txtApe1.setText(estudiante.getApellido1());
        txtApe2.setText(estudiante.getApellido2());
        txtId.setText(estudiante.getIdUsuario());
        txtCorreo.setText(estudiante.getCorreo());
        txtTelefono.setText(estudiante.getTelefono());
        txtDireccion.setText(estudiante.getDireccion());
        txtOrgDL.setText(estudiante.getOrgDL());
        java.util.List<String> tem = estudiante.getTemIN();
        txtTemIN.setText(tem == null ? "" : String.join(", ", tem));
    }

    private void onGuardar() {
        try {
            // Asumo que UsuarioService expone actualizarEstudiante(Estudiante)
            Estudiante actualizado = new Estudiante(
                    estudiante.getNombre(),
                    estudiante.getApellido1(),
                    estudiante.getApellido2(),
                    estudiante.getIdUsuario(),
                    txtTelefono.getText().trim(),
                    estudiante.getCorreo(),
                    txtDireccion.getText().trim(),
                    estudiante.getOrgDL(),
                    estudiante.getTemIN()
            );
            usuarioService.actualizarEstudiante(actualizado);
            JOptionPane.showMessageDialog(this, "Datos actualizados.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx=0; c.gridy=row; c.weightx=0; panel.add(new JLabel(label), c);
        c.gridx=1; c.gridy=row; c.weightx=1; panel.add(comp, c);
    }
}
