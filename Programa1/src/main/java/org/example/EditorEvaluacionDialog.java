package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor de Evaluación:
 * - Si evaluacion == null  -> MODO CREAR (construye nueva Evaluacion al guardar)
 * - Si evaluacion != null  -> MODO EDITAR (modifica la instancia recibida)
 */
public class EditorEvaluacionDialog extends JDialog {

    // ---- Modelo ----
    private final Evaluacion evaluacion;   // null => crear; no-null => editar
    private Evaluacion nuevaEvaluacion;    // solo se setea en modo crear al guardar
    private boolean guardado = false;

    // ---- UI (declaraciones) ----
    private final JTextField txtNombre = new JTextField(20);
    private final JTextArea txtInstrucciones = new JTextArea(5, 30);

    private final DefaultListModel<String> objetivosModel = new DefaultListModel<>();
    private final JList<String> listObjetivos = new JList<>(objetivosModel);
    private final JTextField txtNuevoObjetivo = new JTextField(20);
    private final JButton btnAddObj = new JButton("Agregar objetivo");
    private final JButton btnDelObj = new JButton("Eliminar seleccionado");

    private final JSpinner spDuracion = new JSpinner(new SpinnerNumberModel(60, 1, 10000, 1));
    private final JCheckBox chkPregAleat = new JCheckBox("Preguntas aleatorias");
    private final JCheckBox chkOpcAleat = new JCheckBox("Opciones aleatorias");

    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCancelar = new JButton("Cancelar");

    // ---- Constructores ----
    public EditorEvaluacionDialog(Window owner, Evaluacion evaluacion) {
        super(owner, "Datos de la evaluación", ModalityType.APPLICATION_MODAL);
        this.evaluacion = evaluacion;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(owner);

        construirUI();
        cargarDesdeModelo(); // si evaluacion == null, deja campos vacíos
        wireEvents();
        pack();
    }

    // ---- Construcción de UI ----
    private void construirUI() {
        // --- Panel "Datos" (igual que antes, solo que ahora lo metemos en un subpanel) ---
        JPanel datosPanel = new JPanel(new BorderLayout(8, 8));
        datosPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        // Nombre
        addRow(form, c, 0, "Nombre", txtNombre);

        // Instrucciones
        c.gridx = 0; c.gridy = 1; c.weightx = 0; form.add(new JLabel("Instrucciones"), c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        txtInstrucciones.setLineWrap(true);
        txtInstrucciones.setWrapStyleWord(true);
        form.add(new JScrollPane(txtInstrucciones), c);

        // Objetivos
        JPanel objetivosPanel = new JPanel(new BorderLayout(4, 4));
        objetivosPanel.add(new JScrollPane(listObjetivos), BorderLayout.CENTER);
        JPanel objActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        objActions.add(new JLabel("Nuevo objetivo:"));
        objActions.add(txtNuevoObjetivo);
        objActions.add(btnAddObj);
        objActions.add(btnDelObj);
        objetivosPanel.add(objActions, BorderLayout.SOUTH);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; form.add(new JLabel("Objetivos"), c);
        c.gridx = 1; c.gridy = 2; c.weightx = 1; form.add(objetivosPanel, c);

        // Duración + banderas
        JPanel durFlags = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        durFlags.add(new JLabel("Duración (min):"));
        durFlags.add(spDuracion);
        durFlags.add(chkPregAleat);
        durFlags.add(chkOpcAleat);

        c.gridx = 0; c.gridy = 3; c.weightx = 0; form.add(new JLabel("Parámetros"), c);
        c.gridx = 1; c.gridy = 3; c.weightx = 1; form.add(durFlags, c);

        datosPanel.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnGuardar);
        south.add(btnCancelar);
        datosPanel.add(south, BorderLayout.SOUTH);

        // --- Tabs: Datos (+ Preguntas si es edición) ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Datos", datosPanel);

        // Importante: la pestaña "Preguntas" solo tiene sentido si YA existe la Evaluacion (modo editar).
        if (this.evaluacion != null) {
            tabs.addTab("Preguntas", new EditorPreguntasPanel(this.evaluacion));
        }

        // Setear los tabs como contenido del diálogo
        setContentPane(tabs);
    }


    private void wireEvents() {
        // Objetivos: agregar
        btnAddObj.addActionListener(e -> {
            String t = txtNuevoObjetivo.getText().trim();
            if (t.isEmpty()) return;
            objetivosModel.addElement(t);
            txtNuevoObjetivo.setText("");
        });

        // Objetivos: eliminar seleccionado
        btnDelObj.addActionListener(e -> {
            int idx = listObjetivos.getSelectedIndex();
            if (idx >= 0) objetivosModel.remove(idx);
        });

        // Guardar
        btnGuardar.addActionListener(e -> onGuardar());

        // Cancelar
        btnCancelar.addActionListener(e -> dispose());
    }

    private void cargarDesdeModelo() {
        if (evaluacion == null) return; // crear: sin precarga
        if (evaluacion.getNombre() != null) txtNombre.setText(evaluacion.getNombre());
        if (evaluacion.getInstrucciones() != null) txtInstrucciones.setText(evaluacion.getInstrucciones());
        if (evaluacion.getObjetivos() != null) {
            objetivosModel.clear();
            for (String s : evaluacion.getObjetivos()) objetivosModel.addElement(s);
        }
        if (evaluacion.getDuracionMinutos() > 0) spDuracion.setValue(evaluacion.getDuracionMinutos());
        chkPregAleat.setSelected(evaluacion.isPreguntasAleatorias());
        chkOpcAleat.setSelected(evaluacion.isOpcionesAleatorias());
    }

    // ---- Guardar (crear o editar) ----
    private void onGuardar() {
        try {
            String nombre = txtNombre.getText().trim();
            String instrucciones = txtInstrucciones.getText().trim();

            List<String> objetivos = new ArrayList<>();
            for (int i = 0; i < objetivosModel.size(); i++) {
                objetivos.add(objetivosModel.get(i));
            }

            int duracion = (Integer) spDuracion.getValue();
            boolean pregAleat = chkPregAleat.isSelected();
            boolean opcAleat = chkOpcAleat.isSelected();

            if (evaluacion == null) {
                // ===== MODO CREAR: construir usando el constructor completo =====
                // public Evaluacion(String nombre, String instrucciones, List<String> objetivos,
                //                   int duracionMinutos, boolean preguntasAleatorias, boolean opcionesAleatorias)
                nuevaEvaluacion = new Evaluacion(
                        nombre, instrucciones, objetivos, duracion, pregAleat, opcAleat
                );
                // El constructor ya hace validarDatos(...) y calcularPuntajeTotal()
            } else {
                // ===== MODO EDITAR: aplicar sobre la instancia recibida =====
                evaluacion.setNombre(nombre);
                evaluacion.setInstrucciones(instrucciones);
                evaluacion.setObjetivos(objetivos);
                evaluacion.setDuracionMinutos(duracion);
                evaluacion.setPreguntasAleatorias(pregAleat);
                evaluacion.setOpcionesAleatorias(opcAleat);

                // Validación con tu firma (si la necesitas aquí; si no, el servicio valida igual)
                boolean ok = evaluacion.validarDatos(
                        evaluacion.getNombre(),
                        evaluacion.getInstrucciones(),
                        evaluacion.getObjetivos(),
                        evaluacion.getDuracionMinutos(),
                        evaluacion.isPreguntasAleatorias(),
                        evaluacion.isOpcionesAleatorias()
                );
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Datos inválidos. Revise longitudes y reglas.");
                    return;
                }
                evaluacion.calcularPuntajeTotal(); // mantener consistente
            }

            guardado = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- Utilidades ----
    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; panel.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1; panel.add(comp, c);
    }

    // ---- Getters para el panel que llama ----
    /** Solo no-null cuando el diálogo se usó en modo CREAR y el usuario guardó. */
    public Evaluacion getNuevaEvaluacion() { return nuevaEvaluacion; }
    public boolean isGuardado() { return guardado; }
}

