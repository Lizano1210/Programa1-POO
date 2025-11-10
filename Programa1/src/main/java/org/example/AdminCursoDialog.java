package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana de diálogo para administrar cursos.
 * Permite crear o editar cursos con los datos necesarios,
 * incluyendo validaciones de entrada.
 */
public class AdminCursoDialog extends JDialog {

    /** Campo de texto para el ID del curso. */
    private JTextField txtId = new JTextField(8);

    /** Campo de texto para el nombre del curso. */
    private JTextField txtNombre = new JTextField(24);

    /** Área de texto para la descripción del curso. */
    private JTextArea txtDesc = new JTextArea(4, 24);

    /** Selector para las horas diarias del curso (1-8). */
    private JSpinner spHoras = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));

    /** Selector para el número mínimo de estudiantes (1-5). */
    private JSpinner spMin = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));

    /** Selector para el número máximo de estudiantes (5-20). */
    private JSpinner spMax = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));

    /** Lista desplegable para seleccionar la modalidad del curso. */
    private JComboBox<TipoModalidad> cbModalidad = new JComboBox<>(TipoModalidad.values());

    /** Lista desplegable para seleccionar el tipo de curso. */
    private JComboBox<TipoCurso> cbTipo = new JComboBox<>(TipoCurso.values());

    /** Selector para la calificación mínima de aprobación (0-100). */
    private JSpinner spAprob = new JSpinner(new SpinnerNumberModel(70, 0, 100, 1));

    /** Resultado final del diálogo, representa el curso creado o editado. */
    private Curso resultado;

    /**
     * Crea un nuevo diálogo para administrar cursos.
     *
     * @param owner ventana principal que abre este diálogo
     * @param base  curso base a editar; si es {@code null}, se crea uno nuevo
     */
    public AdminCursoDialog(Window owner, Curso base) {
        super(owner, "Curso", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8,8));

        // --- Panel de formulario ---
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

        // --- Botones de acción ---
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");
        south.add(btnOk);
        south.add(btnCancel);
        add(south, BorderLayout.SOUTH);

        // --- Rellenar datos si se edita un curso ---
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

        // --- Eventos de botones ---
        btnOk.addActionListener(_evt -> onGuardar(base != null));
        btnCancel.addActionListener(_evt -> { resultado = null; dispose(); });

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Agrega una fila al formulario con su etiqueta y componente.
     *
     * @param panel panel donde se agregará la fila
     * @param c     restricciones de diseño
     * @param row   número de fila
     * @param label texto de la etiqueta
     * @param comp  componente asociado
     */
    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx=0; c.gridy=row; c.weightx=0; panel.add(new JLabel(label), c);
        c.gridx=1; c.gridy=row; c.weightx=1; panel.add(comp, c);
    }

    /**
     * Intenta guardar los datos del curso, validando los campos.
     * Si hay errores, muestra un mensaje de advertencia.
     *
     * @param edicion indica si se está editando un curso existente
     */
    private void onGuardar(boolean edicion) {
        try {
            String id = edicion ? txtId.getText() : reqLen(txtId.getText(), 6, 6, "ID");
            String nom = reqLen(txtNombre.getText(), 5, 40, "Nombre");
            String desc = reqLen(txtDesc.getText(), 5, 400, "Descripción");
            int horas = (int) spHoras.getValue();
            int min = (int) spMin.getValue();
            int max = (int) spMax.getValue();

            if (max < min)
                throw new IllegalArgumentException("Máx estudiantes no puede ser menor que Mín.");

            TipoModalidad mod = (TipoModalidad) cbModalidad.getSelectedItem();
            TipoCurso tipo = (TipoCurso) cbTipo.getSelectedItem();
            int aprob = (int) spAprob.getValue();

            Curso c = new Curso(id, nom, desc, horas, mod, min, max, tipo, aprob);

            // Validación final
            if (!c.validarDatos())
                throw new IllegalArgumentException("Los datos no cumplen las validaciones del curso.");

            this.resultado = c;
            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Verifica que una cadena tenga una longitud dentro de un rango válido.
     *
     * @param v     texto a evaluar
     * @param min   longitud mínima permitida
     * @param max   longitud máxima permitida
     * @param campo nombre del campo (para el mensaje de error)
     * @return el texto validado
     * @throws IllegalArgumentException si no cumple con el rango
     */
    private String reqLen(String v, int min, int max, String campo) {
        String s = v==null ? "" : v.trim();
        if (s.length() < min || s.length() > max) {
            throw new IllegalArgumentException("Campo '" + campo + "' debe tener entre " + min + " y " + max + " caracteres.");
        }
        return s;
    }

    /**
     * Devuelve el curso creado o editado tras cerrar el diálogo.
     *
     * @return el objeto {@link Curso} resultante o {@code null} si se canceló
     */
    public Curso getResultado() {
        return resultado;
    }
}

