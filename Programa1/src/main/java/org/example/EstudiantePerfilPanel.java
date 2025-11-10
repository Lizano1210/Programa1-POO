package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Panel que muestra y permite editar el perfil de un estudiante.
 * <p>
 * Permite visualizar la información personal del estudiante, actualizar ciertos campos
 * como teléfono o dirección, y guardar los cambios mediante el servicio de usuario.
 * </p>
 */
public class EstudiantePerfilPanel extends JPanel {

    // -- Atributos principales --

    /** Estudiante actual. */
    private final Estudiante estudiante;

    /** Servicio de usuario utilizado para actualizar los datos. */
    private final UsuarioService usuarioService;

    // -- Campos de formulario --

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtApe1 = new JTextField(20);
    private final JTextField txtApe2 = new JTextField(20);
    private final JTextField txtId = new JTextField(12);
    private final JTextField txtCorreo = new JTextField(24);
    private final JTextField txtTelefono = new JTextField(12);
    private final JTextField txtDireccion = new JTextField(30);
    private final JTextField txtOrgDL = new JTextField(20);
    private final JTextField txtTemIN = new JTextField(30);

    // -- Constructor --

    /**
     * Crea el panel de perfil del estudiante.
     *
     * @param estudiante instancia del estudiante actual
     * @param usuarioService servicio encargado de actualizar la información
     */
    public EstudiantePerfilPanel(Estudiante estudiante, UsuarioService usuarioService) {
        this.estudiante = estudiante;
        this.usuarioService = usuarioService;

        setLayout(new BorderLayout(8,8));

        // -- Formulario --
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

        // -- Botón de guardar --
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar cambios");
        actions.add(btnGuardar);
        add(actions, BorderLayout.SOUTH);

        // Cargar datos iniciales
        cargar();

        // Campos de solo lectura
        txtNombre.setEnabled(false);
        txtApe1.setEnabled(false);
        txtApe2.setEnabled(false);
        txtId.setEnabled(false);
        txtCorreo.setEnabled(false);
        txtOrgDL.setEnabled(false);
        txtTemIN.setEnabled(false);

        btnGuardar.addActionListener(e -> onGuardar());
    }

    // -- Carga de datos --

    /**
     * Carga los datos actuales del estudiante en los campos de texto.
     */
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

    // -- Guardado --

    /**
     * Guarda los cambios realizados en los campos editables del perfil.
     * <p>
     * Se asume que {@link UsuarioService} expone un método
     * {@code actualizarEstudiante(Estudiante)} para persistir la información.
     * </p>
     */
    private void onGuardar() {
        try {
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

    // -- Auxiliares de interfaz --

    /**
     * Agrega una fila con etiqueta y campo al formulario.
     *
     * @param panel panel donde se agrega la fila
     * @param c restricciones de diseño
     * @param row número de fila
     * @param label texto de la etiqueta
     * @param comp componente asociado
     */
    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx=0; c.gridy=row; c.weightx=0; panel.add(new JLabel(label), c);
        c.gridx=1; c.gridy=row; c.weightx=1; panel.add(comp, c);
    }
}
