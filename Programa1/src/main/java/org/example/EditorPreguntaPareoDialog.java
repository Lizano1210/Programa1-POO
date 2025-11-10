package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditorPreguntaPareoDialog extends JDialog {

    private Pareo original;          // puede ser null si es "crear"
    private Pareo resultado;         // nueva instancia reconstruida
    private final int idPregunta;    // conservamos el id en edición
    private boolean guardado = false;

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

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setContentPane(root);

        JPanel north = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets=new Insets(4,4,4,4); c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1;

        c.gridx=0;c.gridy=0;north.add(new JLabel("Descripción"),c);
        c.gridx=1;c.gridy=0;txtDescripcion.setLineWrap(true);txtDescripcion.setWrapStyleWord(true);
        north.add(new JScrollPane(txtDescripcion),c);

        c.gridx=0;c.gridy=1;north.add(new JLabel("Puntos"),c);
        c.gridx=1;c.gridy=1;north.add(spPuntos,c);

        root.add(north, BorderLayout.NORTH);

        // Centro: 3 columnas: Enunciados | Respuestas | Parejas
        JPanel center = new JPanel(new GridLayout(1,3,8,8));

        center.add(crearColumna("Enunciados", lstEnun, txtNuevoEnun,
                e -> addTo(enunModel, txtNuevoEnun),
                e -> delFrom(enunModel, lstEnun)));

        center.add(crearColumna("Respuestas", lstResp, txtNuevoResp,
                e -> addTo(respModel, txtNuevoResp),
                e -> delFrom(respModel, lstResp)));

        JPanel parejas = new JPanel(new BorderLayout(4,4));
        parejas.setBorder(BorderFactory.createTitledBorder("Asociaciones (enunciadoIdx → respuestaIdx)"));
        parejas.add(new JScrollPane(lstPairs), BorderLayout.CENTER);

        JPanel pairActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnLink = new JButton("Asociar selección");
        JButton btnDel = new JButton("Eliminar asociación");
        pairActions.add(btnLink); pairActions.add(btnDel);
        parejas.add(pairActions, BorderLayout.SOUTH);

        btnLink.addActionListener(e -> onLink());
        btnDel.addActionListener(e -> {
            int i = lstPairs.getSelectedIndex();
            if (i >= 0) pairModel.remove(i);
        });

        center.add(parejas);
        root.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        south.add(btnGuardar); south.add(btnCancelar);
        root.add(south, BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    private JPanel crearColumna(String titulo, JList<String> list, JTextField txt, java.awt.event.ActionListener add, java.awt.event.ActionListener del) {
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

    private void addTo(DefaultListModel<String> m, JTextField t) {
        String s = t.getText().trim();
        if (!s.isEmpty()) { m.addElement(s); t.setText(""); }
    }
    private void delFrom(DefaultListModel<String> m, JList<String> l) {
        int i = l.getSelectedIndex(); if (i>=0) m.remove(i);
    }

    private void onLink() {
        int ei = lstEnun.getSelectedIndex();
        int ri = lstResp.getSelectedIndex();
        if (ei < 0 || ri < 0) { JOptionPane.showMessageDialog(this, "Seleccione 1 enunciado y 1 respuesta."); return; }
        pairModel.addElement(ei + " → " + ri);
    }

    private void cargar() {
        if (original == null) return;
        txtDescripcion.setText(original.obtenerDescripcion());
        spPuntos.setValue(original.obtenerPuntos());

        if (original.getEnunciados() != null) original.getEnunciados().forEach(enunModel::addElement);
        if (original.getRespuestas() != null) original.getRespuestas().forEach(respModel::addElement);
        if (original.getAsociaciones() != null) {
            for (Map.Entry<Integer,Integer> e : original.getAsociaciones().entrySet()) {
                pairModel.addElement(e.getKey() + " → " + e.getValue());
            }
        }
    }

    private void onGuardar() {
        try {
            String desc = txtDescripcion.getText().trim();
            int puntos = (Integer) spPuntos.getValue();

            // === RECONSTRUIR ===
            Pareo nueva = new Pareo(idPregunta, desc, puntos);

            // enunciados
            for (int i = 0; i < enunModel.size(); i++) {
                if (!nueva.agregarEnunciado(enunModel.get(i))) {
                    JOptionPane.showMessageDialog(this, "Enunciado inválido en fila " + (i+1));
                    return;
                }
            }
            // respuestas
            for (int i = 0; i < respModel.size(); i++) {
                if (!nueva.agregarRespuesta(respModel.get(i))) {
                    JOptionPane.showMessageDialog(this, "Respuesta inválida en fila " + (i+1));
                    return;
                }
            }
            // asociaciones
            for (int i = 0; i < pairModel.size(); i++) {
                String s = pairModel.get(i); // "a → b"
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

    public boolean isGuardado() { return guardado; }
    public Pareo getPreguntaFinal() { return resultado; }
}

