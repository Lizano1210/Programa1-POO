package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 Diálogo modal para crear/editar Estudiante.
 */
public class AdminEstudianteDialog extends JDialog {

    private JTextField txtNombre = new JTextField(20);
    private JTextField txtApe1 = new JTextField(20);
    private JTextField txtApe2 = new JTextField(20);
    private JTextField txtId = new JTextField(12);
    private JTextField txtTel = new JTextField(12);
    private JTextField txtCorreo = new JTextField(24);
    private JTextField txtDir = new JTextField(30);
    private JTextField txtOrgDL = new JTextField(20);
    private JTextField txtTemIN = new JTextField(30); // coma-separado

    private Estudiante resultado; // null si canceló

    /** Crea el diálogo. Si 'base' != null, precarga datos para edición. */
    public AdminEstudianteDialog(Window owner, Estudiante base) {
        super(owner, "Estudiante", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
        addRow(form, c, 3, "ID Usuario", txtId);
        addRow(form, c, 4, "Teléfono", txtTel);
        addRow(form, c, 5, "Correo", txtCorreo);
        addRow(form, c, 6, "Dirección", txtDir);
        addRow(form, c, 7, "Org. de origen (orgDL)", txtOrgDL);
        addRow(form, c, 8, "Temas de interés (temIN, coma-sep.)", txtTemIN);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");
        actions.add(btnOk);
        actions.add(btnCancel);
        add(actions, BorderLayout.SOUTH);

        // Precargar si edita
        if (base != null) {
            txtNombre.setText(base.getNombre());
            txtApe1.setText(base.getApellido1());
            txtApe2.setText(base.getApellido2());
            txtId.setText(base.getIdUsuario());
            txtId.setEnabled(false); // ID no editable
            txtTel.setText(base.getTelefono());
            txtCorreo.setText(base.getCorreo());
            txtDir.setText(base.getDireccion());
            txtOrgDL.setText(base.getOrgDL());
            List<String> t = base.getTemIN();
            txtTemIN.setText(t == null ? "" : String.join(", ", t));
        }

        btnOk.addActionListener(e -> onGuardar(base != null));
        btnCancel.addActionListener(e -> { resultado = null; dispose(); });

        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        panel.add(comp, c);
    }

    private void onGuardar(boolean esEdicion) {
        try {
            String nombre = req(txtNombre.getText(), "Nombre", 2);
            String a1 = req(txtApe1.getText(), "Apellido 1", 2);
            String a2 = req(txtApe2.getText(), "Apellido 2", 0); // puede ir vacío
            String id = esEdicion ? txtId.getText() : req(txtId.getText(), "ID Usuario", 1);
            String tel = req(txtTel.getText(), "Teléfono", 8);
            String correo = req(txtCorreo.getText(), "Correo", 5);
            String dir = req(txtDir.getText(), "Dirección", 5);
            String org = req(txtOrgDL.getText(), "Org. de origen", 2);
            List<String> tem = parseTemIN(txtTemIN.getText());

            // Constructor completo (confirmado)
            Estudiante e = new Estudiante(nombre, a1, a2, id, tel, correo, dir, org, tem);
            this.resultado = e;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String req(String v, String campo, int min) {
        String s = v == null ? "" : v.trim();
        if (s.length() < min) throw new IllegalArgumentException("Campo '" + campo + "' inválido (mín. " + min + ").");
        return s;
    }
    private List<String> parseTemIN(String v) {
        if (v == null || v.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(v.split("\\s*,\\s*")));
    }

    /** Devuelve el Estudiante creado/editado o null si se canceló. */
    public Estudiante getResultado() { return resultado; }
}
