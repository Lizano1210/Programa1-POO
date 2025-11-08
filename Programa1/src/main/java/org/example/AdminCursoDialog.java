package org.example;

import javax.swing.*;
import java.awt.*;

/** Dialog para los Cursos con las validaciones necesarias */
public class AdminCursoDialog extends JDialog {

    private JTextField txtId = new JTextField(8);
    private JTextField txtNombre = new JTextField(24);
    private JTextArea  txtDesc = new JTextArea(4, 24);
    private JSpinner spHoras = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
    private JSpinner spMin = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
    private JSpinner spMax = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
    private JComboBox<TipoModalidad> cbModalidad = new JComboBox<>(TipoModalidad.values());
    private JComboBox<TipoCurso> cbTipo = new JComboBox<>(TipoCurso.values());
    private JSpinner spAprob = new JSpinner(new SpinnerNumberModel(70, 0, 100, 1));

    private Curso resultado;

    public AdminCursoDialog(Window owner, Curso base) {
        super(owner, "Curso", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8,8));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        addRow(form, c, 0, "ID (6):", txtId);
        addRow(form, c, 1, "Nombre (5-40):", txtNombre);
        addRow(form, c, 2, "Descripción (5-400):", new JScrollPane(txtDesc));
        addRow(form, c, 3, "Horas/día (1-8):", spHoras);
        addRow(form, c, 4, "Mín estudiantes (1-5):", spMin);
        addRow(form, c, 5, "Máx estudiantes (min..20):", spMax);
        addRow(form, c, 6, "Modalidad:", cbModalidad);
        addRow(form, c, 7, "Tipo:", cbTipo);
        addRow(form, c, 8, "Calif. mínima (0-100):", spAprob);
        add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");
        south.add(btnOk); south.add(btnCancel);
        add(south, BorderLayout.SOUTH);

        if (base != null) {
            txtId.setText(base.getId());
            txtId.setEnabled(false);
            txtNombre.setText(base.getNombre());
            txtDesc.setText(base.getDescripcion());
            spHoras.setValue(base.getHrsDia());
            spMin.setValue(base.getMinEstu());
            spMax.setValue(base.getMaxEstu());
            cbModalidad.setSelectedItem(base.getModalidad());
            cbTipo.setSelectedItem(base.getTipo());
            spAprob.setValue(base.getAprobCalificacion());
        }

        btnOk.addActionListener(_evt -> onGuardar(base != null));
        btnCancel.addActionListener(_evt -> { resultado = null; dispose(); });

        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx=0; c.gridy=row; c.weightx=0; panel.add(new JLabel(label), c);
        c.gridx=1; c.gridy=row; c.weightx=1; panel.add(comp, c);
    }

    private void onGuardar(boolean edicion) {
        try {
            String id = edicion ? txtId.getText() : reqLen(txtId.getText(), 6, 6, "ID");
            String nom = reqLen(txtNombre.getText(), 5, 40, "Nombre");
            String desc = reqLen(txtDesc.getText(), 5, 400, "Descripción");
            int horas = (int) spHoras.getValue();
            int min = (int) spMin.getValue();
            int max = (int) spMax.getValue();
            if (max < min) throw new IllegalArgumentException("Máx estudiantes no puede ser menor que Mín.");
            TipoModalidad mod = (TipoModalidad) cbModalidad.getSelectedItem();
            TipoCurso tipo = (TipoCurso) cbTipo.getSelectedItem();
            int aprob = (int) spAprob.getValue();

            Curso c = new Curso(id, nom, desc, horas, mod, min, max, tipo, aprob);
            System.out.println("DEBUG c.getId()=" + c.getId());
            System.out.println("DEBUG c.getNombre()=" + c.getNombre());
            System.out.println("DEBUG c.getDescripcion() len=" + (c.getDescripcion()==null? -1 : c.getDescripcion().length()));
            System.out.println("DEBUG c.getModalidad()=" + c.getModalidad());
            System.out.println("DEBUG c.getTipo()=" + c.getTipo());
            System.out.println("DEBUG c.getMinEstu()=" + c.getMinEstu() + " max=" + c.getMaxEstu());
            System.out.println("DEBUG c.getHrsDia()=" + c.getHrsDia());
            System.out.println("DEBUG c.getAprobCalificacion()=" + c.getAprobCalificacion());
            if (!c.validarDatos()) throw new IllegalArgumentException("Los datos no cumplen las validaciones del curso.");
            this.resultado = c;
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String reqLen(String v, int min, int max, String campo) {
        String s = v==null ? "" : v.trim();
        if (s.length() < min || s.length() > max) {
            throw new IllegalArgumentException("Campo '" + campo + "' debe tener entre " + min + " y " + max + " caracteres.");
        }
        return s;
    }

    public Curso getResultado() { return resultado; }
}
