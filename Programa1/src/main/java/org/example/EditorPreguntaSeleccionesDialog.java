package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditorPreguntaSeleccionesDialog extends JDialog {

    private final int idPregunta;
    private final TipoPregunta tipo;
    private final Pregunta preguntaOriginal; // puede ser null
    private Pregunta preguntaResultado;      // nueva instancia reconstruida
    private boolean guardado = false;

    private final JTextArea txtDescripcion = new JTextArea(4, 30);
    private final JSpinner spPuntos = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JTable tabla = new JTable();
    private final ModeloRespuestas model = new ModeloRespuestas();

    public EditorPreguntaSeleccionesDialog(Window owner, Pregunta original, TipoPregunta tipo, int idPregunta) {
        super(owner, "Pregunta " + tipo, ModalityType.APPLICATION_MODAL);
        this.preguntaOriginal = original;
        this.tipo = tipo;
        this.idPregunta = idPregunta;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 500));
        setLocationRelativeTo(owner);

        construirUI();
        cargar();
        pack();
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setContentPane(root);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JLabel lblTipo = new JLabel("Tipo: " + tipo);
        c.gridx=0; c.gridy=0; c.gridwidth=2; form.add(lblTipo, c);
        c.gridwidth=1;

        c.gridx=0; c.gridy=1; form.add(new JLabel("Descripción"), c);
        c.gridx=1; c.gridy=1; txtDescripcion.setLineWrap(true); txtInstruccionesWrap();
        form.add(new JScrollPane(txtDescripcion), c);

        c.gridx=0; c.gridy=2; form.add(new JLabel("Puntos"), c);
        c.gridx=1; c.gridy=2; form.add(spPuntos, c);

        root.add(form, BorderLayout.NORTH);

        tabla.setModel(model);
        root.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Agregar opción");
        JButton btnEdit = new JButton("Editar opción");
        JButton btnDel = new JButton("Eliminar opción");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDel);
        actions.add(btnGuardar);
        actions.add(btnCancelar);
        root.add(actions, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addOpcion());
        btnEdit.addActionListener(e -> editOpcion());
        btnDel.addActionListener(e -> delOpcion());
        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void txtInstruccionesWrap() {
        txtDescripcion.setWrapStyleWord(true);
    }

    private void cargar() {
        model.setData(new ArrayList<>());
        if (preguntaOriginal != null) {
            txtDescripcion.setText(preguntaOriginal.obtenerDescripcion());
            spPuntos.setValue(preguntaOriginal.obtenerPuntos());
            if (preguntaOriginal.getRespuestas() != null) {
                for (Respuesta r : preguntaOriginal.getRespuestas()) {
                    // clonamos a modelo editable
                    model.data.add(new Respuesta(r.getTexto(), r.isCorrecta(), r.getOrden()));
                }
                model.fireTableDataChanged();
            }
        } else {
            // Defaults para VF (2 opciones)
            if (tipo == TipoPregunta.VERDADERO_FALSO) {
                model.data.add(new Respuesta("Verdadero", false, 1));
                model.data.add(new Respuesta("Falso", false, 2));
                model.fireTableDataChanged();
            }
        }
    }

    private void addOpcion() {
        String texto = JOptionPane.showInputDialog(this, "Texto de la opción:");
        if (texto == null || texto.trim().isEmpty()) return;

        int orden = model.data.size() + 1;
        int esOk = JOptionPane.showConfirmDialog(this, "¿Es correcta?", "Marcar correcta", JOptionPane.YES_NO_OPTION);
        boolean correcta = (esOk == JOptionPane.YES_OPTION);

        model.data.add(new Respuesta(texto.trim(), correcta, orden));
        model.fireTableDataChanged();
    }

    private void editOpcion() {
        int r = tabla.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una opción."); return; }
        Respuesta op = model.data.get(r);

        String texto = JOptionPane.showInputDialog(this, "Texto de la opción:", op.getTexto());
        if (texto == null || texto.trim().isEmpty()) return;

        int esOk = JOptionPane.showConfirmDialog(this, "¿Es correcta?", "Marcar correcta", JOptionPane.YES_NO_OPTION);
        boolean correcta = (esOk == JOptionPane.YES_OPTION);

        op.setTexto(texto.trim());
        op.setCorrecta(correcta);
        // mantener orden estable (no cambiamos aquí)
        model.fireTableRowsUpdated(r, r);
    }

    private void delOpcion() {
        int r = tabla.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una opción."); return; }
        model.data.remove(r);
        // reordenar consecutivo
        for (int i = 0; i < model.data.size(); i++) {
            model.data.get(i).setOrden(i+1);
        }
        model.fireTableDataChanged();
    }

    private void onGuardar() {
        try {
            String desc = txtDescripcion.getText().trim();
            int puntos = (Integer) spPuntos.getValue();

            // Reconstruir nueva Pregunta con este id
            Pregunta nueva = new Pregunta(idPregunta, tipo, desc, puntos);

            // Cargar respuestas (con orden consecutivo)
            for (int i = 0; i < model.data.size(); i++) {
                Respuesta r = model.data.get(i);
                // asegurar orden i+1
                nueva.agregarRespuesta(new Respuesta(r.getTexto(), r.isCorrecta(), i+1));
            }

            // Validación de tu modelo
            if (!nueva.validarDatos()) {
                JOptionPane.showMessageDialog(this, "Datos inválidos para el tipo seleccionado.");
                return;
            }

            this.preguntaResultado = nueva;
            this.guardado = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isGuardado() { return guardado; }

    /** Nueva instancia de Pregunta (con mismo id si venías a editar) para insertar/reemplazar. */
    public Pregunta getPreguntaFinal() { return preguntaResultado; }

    // ===== modelo tabla respuestas =====
    static class ModeloRespuestas extends AbstractTableModel {
        private final String[] cols = {"#", "Texto", "Correcta"};
        List<Respuesta> data = new ArrayList<>();

        public void setData(List<Respuesta> list) { data = list; fireTableDataChanged(); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Respuesta x = data.get(r);
            return switch (c) {
                case 0 -> x.getOrden();
                case 1 -> x.getTexto();
                case 2 -> x.isCorrecta() ? "✔" : "—";
                default -> "";
            };
        }
    }
}
