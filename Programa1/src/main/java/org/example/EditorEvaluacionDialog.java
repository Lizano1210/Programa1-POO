package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para crear o editar una evaluación.
 * <p>
 * Si {@code evaluacion == null} → modo creación.<br>
 * Si {@code evaluacion != null} → modo edición (modifica la instancia existente).
 * </p>
 */
public class EditorEvaluacionDialog extends JDialog {

    // -- Modelo --

    /** Evaluación a editar (null si es modo creación). */
    private final Evaluacion evaluacion;

    /** Evaluación creada (solo se asigna en modo creación). */
    private Evaluacion nuevaEvaluacion;

    /** Indica si el usuario guardó los cambios. */
    private boolean guardado = false;

    // -- Componentes de la interfaz --

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

    // -- Constructor --

    /**
     * Crea el diálogo para editar o crear una evaluación.
     *
     * @param owner ventana propietaria
     * @param evaluacion evaluación a editar (null para crear una nueva)
     */
    public EditorEvaluacionDialog(Window owner, Evaluacion evaluacion) {
        super(owner, "Datos de la evaluación", ModalityType.APPLICATION_MODAL);
        this.evaluacion = evaluacion;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(owner);

        construirUI();
        cargarDesdeModelo();
        wireEvents();
        pack();
    }

    // -- Construcción de la interfaz --

    /**
     * Construye la interfaz gráfica del diálogo.
     */
    private void construirUI() {
        JPanel datosPanel = new JPanel(new BorderLayout(8, 8));
        datosPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        addRow(form, c, 0, "Nombre", txtNombre);

        // Instrucciones
        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        form.add(new JLabel("Instrucciones"), c);
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

        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        form.add(new JLabel("Objetivos"), c);
        c.gridx = 1; c.gridy = 2; c.weightx = 1;
        form.add(objetivosPanel, c);

        // Duración y banderas
        JPanel durFlags = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        durFlags.add(new JLabel("Duración (min):"));
        durFlags.add(spDuracion);
        durFlags.add(chkPregAleat);
        durFlags.add(chkOpcAleat);

        c.gridx = 0; c.gridy = 3; c.weightx = 0;
        form.add(new JLabel("Parámetros"), c);
        c.gridx = 1; c.gridy = 3; c.weightx = 1;
        form.add(durFlags, c);

        datosPanel.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnGuardar);
        south.add(btnCancelar);
        datosPanel.add(south, BorderLayout.SOUTH);

        // Pestañas (datos y preguntas)
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Datos", datosPanel);

        if (this.evaluacion != null) {
            tabs.addTab("Preguntas", new EditorPreguntasPanel(this.evaluacion));
        }

        setContentPane(tabs);
    }

    // -- Configuración de eventos --

    /**
     * Asigna los eventos a los botones y controles del diálogo.
     */
    private void wireEvents() {
        btnAddObj.addActionListener(e -> {
            String t = txtNuevoObjetivo.getText().trim();
            if (!t.isEmpty()) {
                objetivosModel.addElement(t);
                txtNuevoObjetivo.setText("");
            }
        });

        btnDelObj.addActionListener(e -> {
            int idx = listObjetivos.getSelectedIndex();
            if (idx >= 0) objetivosModel.remove(idx);
        });

        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    // -- Carga de datos --

    /**
     * Carga los datos existentes de la evaluación (modo edición).
     */
    private void cargarDesdeModelo() {
        if (evaluacion == null) return;
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

    // -- Guardar --

    /**
     * Guarda los cambios de la evaluación.
     * <p>
     * Si está en modo creación, crea una nueva instancia de {@link Evaluacion}.<br>
     * Si está en modo edición, actualiza la existente.
     * </p>
     */
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
                nuevaEvaluacion = new Evaluacion(
                        nombre, instrucciones, objetivos, duracion, pregAleat, opcAleat
                );
            } else {
                evaluacion.setNombre(nombre);
                evaluacion.setInstrucciones(instrucciones);
                evaluacion.setObjetivos(objetivos);
                evaluacion.setDuracionMinutos(duracion);
                evaluacion.setPreguntasAleatorias(pregAleat);
                evaluacion.setOpcionesAleatorias(opcAleat);

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
                evaluacion.calcularPuntajeTotal();
            }

            guardado = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- Métodos auxiliares --

    /**
     * Agrega una fila al formulario con etiqueta y campo.
     *
     * @param panel panel contenedor
     * @param c restricciones de diseño
     * @param row número de fila
     * @param label texto de la etiqueta
     * @param comp componente de entrada
     */
    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        panel.add(comp, c);
    }

    // -- Getters --

    /** Devuelve la evaluación recién creada (solo en modo creación). */
    public Evaluacion getNuevaEvaluacion() { return nuevaEvaluacion; }

    /** Indica si el usuario guardó los cambios. */
    public boolean isGuardado() { return guardado; }
}

