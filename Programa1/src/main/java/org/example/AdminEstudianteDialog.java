package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Diálogo modal para crear o editar un estudiante.
 * Permite ingresar y validar los datos personales del estudiante
 * antes de guardarlos o actualizarlos.
 */
public class AdminEstudianteDialog extends JDialog {

    /** Campo de texto para el nombre del estudiante. */
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

    /** Campo de texto para la organización de origen. */
    private JTextField txtOrgDL = new JTextField(20);

    /** Campo de texto para los temas de interés (separados por comas). */
    private JTextField txtTemIN = new JTextField(30);

    /** Resultado del diálogo: estudiante creado o editado (o null si se cancela). */
    private Estudiante resultado;

    /**
     * Crea un nuevo diálogo para registrar o editar un estudiante.
     *
     * @param owner ventana propietaria del diálogo
     * @param base  estudiante base a editar; si es {@code null}, se creará uno nuevo
     */
    public AdminEstudianteDialog(Window owner, Estudiante base) {
        super(owner, "Estudiante", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        // --- Formulario principal ---
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

        // --- Botones de acción ---
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");
        actions.add(btnOk);
        actions.add(btnCancel);
        add(actions, BorderLayout.SOUTH);

        // --- Precargar datos si es edición ---
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

        // --- Eventos ---
        btnOk.addActionListener(e -> onGuardar(base != null));
        btnCancel.addActionListener(e -> {
            resultado = null;
            dispose();
        });

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Agrega una fila al formulario con etiqueta y componente.
     *
     * @param panel panel al que se agregará
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

    /**
     * Valida los datos ingresados y crea un nuevo objeto {@link Estudiante}.
     *
     * @param esEdicion indica si se está editando un registro existente
     */
    private void onGuardar(boolean esEdicion) {
        try {
            String nombre = req(txtNombre.getText(), "Nombre", 2);
            String a1 = req(txtApe1.getText(), "Apellido 1", 2);
            String a2 = req(txtApe2.getText(), "Apellido 2", 0); // puede estar vacío
            String id = esEdicion ? txtId.getText() : req(txtId.getText(), "ID Usuario", 1);
            String tel = req(txtTel.getText(), "Teléfono", 8);
            String correo = req(txtCorreo.getText(), "Correo", 5);
            String dir = req(txtDir.getText(), "Dirección", 5);
            String org = req(txtOrgDL.getText(), "Org. de origen", 2);
            List<String> tem = parseTemIN(txtTemIN.getText());

            Estudiante e = new Estudiante(nombre, a1, a2, id, tel, correo, dir, org, tem);
            this.resultado = e;
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Verifica que un campo tenga una longitud mínima.
     *
     * @param v     valor a evaluar
     * @param campo nombre del campo (para el mensaje)
     * @param min   longitud mínima
     * @return texto validado
     * @throws IllegalArgumentException si no cumple el mínimo
     */
    private String req(String v, String campo, int min) {
        String s = v == null ? "" : v.trim();
        if (s.length() < min)
            throw new IllegalArgumentException("Campo '" + campo + "' inválido (mín. " + min + ").");
        return s;
    }

    /**
     * Convierte el texto de temas de interés en una lista.
     *
     * @param v texto con temas separados por comas
     * @return lista de temas
     */
    private List<String> parseTemIN(String v) {
        if (v == null || v.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(v.split("\\s*,\\s*")));
    }

    /**
     * Devuelve el estudiante creado o editado.
     *
     * @return el objeto {@link Estudiante} o {@code null} si se canceló
     */
    public Estudiante getResultado() {
        return resultado;
    }
}

