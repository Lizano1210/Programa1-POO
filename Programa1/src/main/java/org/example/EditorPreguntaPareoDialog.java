package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Diálogo para crear o editar una pregunta de tipo Pareo.
 * <p>
 * Permite definir enunciados, respuestas y asociaciones entre ambos.
 * Si se pasa un objeto {@link Pareo} existente, el diálogo se abre en modo edición;
 * si es {@code null}, se crea una nueva instancia.
 * </p>
 */
public class EditorPreguntaPareoDialog extends JDialog {

    // -- Atributos principales --

    /** Pregunta original (null si es creación). */
    private Pareo original;

    /** Resultado final: nueva instancia de Pareo. */
    private Pareo resultado;

    /** Identificador de la pregunta (se conserva en modo edición). */
    private final int idPregunta;

    /** Indica si el usuario guardó los cambios. */
    private boolean guardado = false;

    // -- Componentes de interfaz --

    private final JTextArea txtDescripcion = new JTextArea(3, 30);
    private final JSpinner spPuntos = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

    private final DefaultListModel<String> enunModel = new DefaultListModel<>();
    private final JList<String> lstEnun = new JList<>(enunModel);
    private final JTextField txtNuevoEnun = new JTextField(20);

    private final DefaultListModel<String> respModel = new DefaultListModel<>();
    private final JList<String> lstResp = new JList<>(respModel);
    private final JTextField txtNuevoResp = new JTextField(20);

    private final DefaultListModel<String> pairModel = new DefaultListModel<>();
    private final JList<String> lstPairs = new JList<>(pairModel);

    // -- Constructor --

    /**
     * Crea el diálogo de edición o creación de una pregunta de Pareo.
     *
     * @param owner ventana propietaria
     * @param original objeto existente de Pareo (null para crear uno nuevo)
     * @param idPregunta identificador de la pregunta
     */
    public EditorPreguntaPareoDialog(Window owner, Pareo original, int idPregunta) {
        super(owner, "Pregunta de Pareo", ModalityType.APPLICATION_MODAL);
        this.original = original;
        this.idPregunta = (original != null ? original.getId() : idPregunta);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(owner);

        construirUI();
        cargar();
        pack();
    }

    // -- Construcción de interfaz --

    /**
     * Construye la interfaz del diálogo.
     */
    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setContentPane(root);

        // Panel superior: descripción y puntos
        JPanel north = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.gridx = 0; c.gridy = 0;
        north.add(new JLabel("Descripción"), c);
        c.gridx = 1; c.gridy = 0;
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        north.add(new JScrollPane(txtDescripcion), c);

        c.gridx = 0; c.gridy = 1;
        north.add(new JLabel("Puntos"), c);
        c.gridx = 1; c.gridy = 1;
        north.add(spPuntos, c);

        root.add(north, BorderLayout.NORTH);

        // -- Sección central (enunciados, respuestas y asociaciones) --
        JPanel center = new JPanel(new GridLayout(1,3,8,8));

        center.add(crearColumna("Enunciados", lstEnun, txtNuevoEnun,
                e -> addTo(enunModel, txtNuevoEnun),
                e -> delFrom(enunModel, lstEnun)));

        center.add(crearColumna("Respuestas", lstResp, txtNuevoResp,
                e -> addTo(respModel, txtNuevoResp),
                e -> delFrom(respModel, lstResp)));

        // Panel de asociaciones
        JPanel parejas = new JPanel(new BorderLayout(4,4));
        parejas.setBorder(BorderFactory.createTitledBorder("Asociaciones (enunciadoIdx → respuestaIdx)"));
        parejas.add(new JScrollPane(lstPairs), BorderLayout.CENTER);

        JPanel pairActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnLink = new JButton("Asociar selección");
        JButton btnDel = new JButton("Eliminar asociación");
        pairActions.add(btnLink);
        pairActions.add(btnDel);
        parejas.add(pairActions, BorderLayout.SOUTH);

        btnLink.addActionListener(e -> onLink());
        btnDel.addActionListener(e -> {
            int i = lstPairs.getSelectedIndex();
            if (i >= 0) pairModel.remove(i);
        });

        center.add(parejas);
        root.add(center, BorderLayout.CENTER);

        // Panel inferior (acciones)
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        south.add(btnGuardar);
        south.add(btnCancelar);
        root.add(south, BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    // -- Creación de columnas --

    /**
     * Crea una columna con lista, campo de texto y botones de agregar/eliminar.
     */
    private JPanel crearColumna(String titulo, JList<String> list, JTextField txt,
                                java.awt.event.ActionListener add, java.awt.event.ActionListener del) {
        JPanel p = new JPanel(new BorderLayout(4,4));
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        p.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bAdd = new JButton("Agregar");
        JButton bDel = new JButton("Eliminar");
        south.add(new JLabel("Nuevo:"));
        south.add(txt);
        south.add(bAdd);
        south.add(bDel);
        bAdd.addActionListener(add);
        bDel.addActionListener(del);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    // -- Métodos auxiliares --

    /**
     * Agrega el contenido del campo de texto a la lista.
     */
    private void addTo(DefaultListModel<String> m, JTextField t) {
        String s = t.getText().trim();
        if (!s.isEmpty()) {
            m.addElement(s);
            t.setText("");
        }
    }

    /**
     * Elimina el elemento seleccionado de la lista.
     */
    private void delFrom(DefaultListModel<String> m, JList<String> l) {
        int i = l.getSelectedIndex();
        if (i >= 0) m.remove(i);
    }

    /**
     * Crea una asociación entre el enunciado y la respuesta seleccionados.
     */
    private void onLink() {
        int ei = lstEnun.getSelectedIndex();
        int ri = lstResp.getSelectedIndex();
        if (ei < 0 || ri < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione 1 enunciado y 1 respuesta.");
            return;
        }
        pairModel.addElement(ei + " → " + ri);
    }

    // -- Carga de datos --

    /**
     * Carga los datos existentes de la pregunta original (modo edición).
     */
    private void cargar() {
        if (original == null) return;

        txtDescripcion.setText(original.obtenerDescripcion());
        spPuntos.setValue(original.obtenerPuntos());

        if (original.getEnunciados() != null)
            original.getEnunciados().forEach(enunModel::addElement);

        if (original.getRespuestas() != null)
            original.getRespuestas().forEach(respModel::addElement);

        if (original.getAsociaciones() != null) {
            for (Map.Entry<Integer,Integer> e : original.getAsociaciones().entrySet()) {
                pairModel.addElement(e.getKey() + " → " + e.getValue());
            }
        }
    }

    // -- Guardado --

    /**
     * Guarda los datos ingresados, validando y reconstruyendo la instancia de Pareo.
     */
    private void onGuardar() {
        try {
            String desc = txtDescripcion.getText().trim();
            int puntos = (Integer) spPuntos.getValue();

            Pareo nueva = new Pareo(idPregunta, desc, puntos);

            // Enunciados
            for (int i = 0; i < enunModel.size(); i++) {
                if (!nueva.agregarEnunciado(enunModel.get(i))) {
                    JOptionPane.showMessageDialog(this, "Enunciado inválido en fila " + (i+1));
                    return;
                }
            }

            // Respuestas
            for (int i = 0; i < respModel.size(); i++) {
                if (!nueva.agregarRespuesta(respModel.get(i))) {
                    JOptionPane.showMessageDialog(this, "Respuesta inválida en fila " + (i+1));
                    return;
                }
            }

            // Asociaciones
            for (int i = 0; i < pairModel.size(); i++) {
                String s = pairModel.get(i); // formato: "a → b"
                String[] parts = s.split("→");
                int a = Integer.parseInt(parts[0].trim());
                int b = Integer.parseInt(parts[1].trim());
                if (!nueva.definirAsociacion(a, b)) {
                    JOptionPane.showMessageDialog(this, "Asociación inválida: " + s);
                    return;
                }
            }

            if (!nueva.validarDatos()) {
                JOptionPane.showMessageDialog(this, "Datos inválidos para Pareo.");
                return;
            }

            this.resultado = nueva;
            this.guardado = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- Getters --

    /** @return {@code true} si el usuario guardó los cambios. */
    public boolean isGuardado() { return guardado; }

    /** @return la pregunta de pareo resultante. */
    public Pareo getPreguntaFinal() { return resultado; }
}
