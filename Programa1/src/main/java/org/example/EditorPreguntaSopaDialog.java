package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para crear o editar una pregunta de tipo Sopa de Letras.
 * <p>
 * Permite definir la descripción, puntuación, tamaño de la cuadrícula
 * y las palabras con sus respectivas pistas.
 * </p>
 */
public class EditorPreguntaSopaDialog extends JDialog {

    // -- Atributos principales --

    /** Pregunta original (null si se está creando una nueva). */
    private SopaDeLetras original;

    /** Resultado final con la nueva configuración. */
    private SopaDeLetras resultado;

    /** Identificador único de la pregunta. */
    private final int idPregunta;

    /** Indica si el usuario guardó los cambios. */
    private boolean guardado = false;

    // -- Componentes de interfaz --

    private final JTextArea txtDescripcion = new JTextArea(3, 30);
    private final JSpinner spPuntos = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JSpinner spTam = new JSpinner(new SpinnerNumberModel(15, 10, 30, 1));
    private final JTable tabla = new JTable();
    private final ModeloPalabras model = new ModeloPalabras();

    // -- Constructor --

    /**
     * Crea el diálogo de edición o creación de una Sopa de Letras.
     *
     * @param owner ventana propietaria
     * @param original objeto existente (null para crear uno nuevo)
     * @param idPregunta identificador de la pregunta
     */
    public EditorPreguntaSopaDialog(Window owner, SopaDeLetras original, int idPregunta) {
        super(owner, "Sopa de Letras", ModalityType.APPLICATION_MODAL);
        this.original = original;
        this.idPregunta = (original != null ? original.getId() : idPregunta);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(680, 520));
        setLocationRelativeTo(owner);

        construirUI();
        cargar();
        pack();
    }

    // -- Construcción de interfaz --

    /**
     * Construye la interfaz gráfica del diálogo.
     */
    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setContentPane(root);

        // Panel superior: descripción, puntos y tamaño
        JPanel north = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets=new Insets(4,4,4,4);
        c.fill=GridBagConstraints.HORIZONTAL;
        c.weightx=1;

        c.gridx=0; c.gridy=0;
        north.add(new JLabel("Descripción"),c);
        c.gridx=1; c.gridy=0;
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        north.add(new JScrollPane(txtDescripcion),c);

        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p2.add(new JLabel("Puntos:"));
        p2.add(spPuntos);
        p2.add(new JLabel("Tamaño (N x N):"));
        p2.add(spTam);

        c.gridx=0; c.gridy=1; c.gridwidth=2;
        north.add(p2,c);
        c.gridwidth=1;
        root.add(north, BorderLayout.NORTH);

        // Tabla de palabras y pistas
        tabla.setModel(model);
        tabla.setFillsViewportHeight(true);
        root.add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnAdd = new JButton("Agregar");
        JButton btnEdit = new JButton("Editar");
        JButton btnDel = new JButton("Eliminar");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        south.add(btnAdd);
        south.add(btnEdit);
        south.add(btnDel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(btnGuardar);
        right.add(btnCancelar);

        JPanel southWrap = new JPanel(new BorderLayout());
        southWrap.add(south, BorderLayout.WEST);
        southWrap.add(right, BorderLayout.EAST);
        root.add(southWrap, BorderLayout.SOUTH);

        // Eventos
        btnAdd.addActionListener(e -> addFila());
        btnEdit.addActionListener(e -> editFila());
        btnDel.addActionListener(e -> delFila());
        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    // -- Carga de datos --

    /**
     * Carga los datos de la Sopa de Letras en modo edición.
     */
    private void cargar() {
        model.setData(new ArrayList<>());
        if (original == null) return;

        txtDescripcion.setText(original.obtenerDescripcion());
        spPuntos.setValue(original.obtenerPuntos());
        spTam.setValue(original.getTamanioCuadricula());

        if (original.getEnunciados() != null) {
            for (SopaDeLetras.Enunciado e : original.getEnunciados()) {
                model.data.add(new Fila(e.getPalabra(), e.getPista()));
            }
            model.fireTableDataChanged();
        }
    }

    // -- Gestión de filas (palabras/pistas) --

    /**
     * Agrega una nueva palabra con su pista.
     */
    private void addFila() {
        JTextField tfPal = new JTextField(16);
        JTextField tfPista = new JTextField(24);
        JPanel p = new JPanel(new GridLayout(2,2,6,6));
        p.add(new JLabel("Palabra:"));
        p.add(tfPal);
        p.add(new JLabel("Pista:"));
        p.add(tfPista);

        int ok = JOptionPane.showConfirmDialog(this, p, "Nueva palabra", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            String pal = tfPal.getText().trim();
            String pista = tfPista.getText().trim();
            if (!pal.isEmpty() && !pista.isEmpty()) {
                model.data.add(new Fila(pal, pista));
                model.fireTableDataChanged();
            }
        }
    }

    /**
     * Edita una palabra existente.
     */
    private void editFila() {
        int r = tabla.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila.");
            return;
        }

        Fila f = model.data.get(r);
        JTextField tfPal = new JTextField(f.palabra, 16);
        JTextField tfPista = new JTextField(f.pista, 24);
        JPanel p = new JPanel(new GridLayout(2,2,6,6));
        p.add(new JLabel("Palabra:"));
        p.add(tfPal);
        p.add(new JLabel("Pista:"));
        p.add(tfPista);

        int ok = JOptionPane.showConfirmDialog(this, p, "Editar palabra", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            String pal = tfPal.getText().trim();
            String pista = tfPista.getText().trim();
            if (!pal.isEmpty() && !pista.isEmpty()) {
                f.palabra = pal;
                f.pista = pista;
                model.fireTableRowsUpdated(r, r);
            }
        }
    }

    /**
     * Elimina una palabra seleccionada de la lista.
     */
    private void delFila() {
        int r = tabla.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila.");
            return;
        }
        model.data.remove(r);
        model.fireTableDataChanged();
    }

    // -- Guardado --

    /**
     * Guarda los cambios y crea una nueva instancia de {@link SopaDeLetras}.
     */
    private void onGuardar() {
        try {
            String desc = txtDescripcion.getText().trim();
            int puntos = (Integer) spPuntos.getValue();
            int tam = (Integer) spTam.getValue();

            SopaDeLetras nueva = new SopaDeLetras(idPregunta, desc, puntos, tam);

            for (int i = 0; i < model.data.size(); i++) {
                Fila f = model.data.get(i);
                if (!nueva.agregarEnunciado(f.palabra, f.pista)) {
                    JOptionPane.showMessageDialog(this, "Palabra/Pista inválida en fila " + (i+1));
                    return;
                }
            }

            boolean okGrid = nueva.generarCuadricula();
            if (!okGrid) {
                JOptionPane.showMessageDialog(this, "No se pudo generar la cuadrícula. Revise el tamaño o cantidad mínima de palabras.");
                return;
            }

            if (!nueva.validarDatos()) {
                JOptionPane.showMessageDialog(this, "Datos inválidos para Sopa de Letras.");
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

    /** @return true si el usuario guardó los cambios. */
    public boolean isGuardado() { return guardado; }

    /** @return nueva instancia de la pregunta Sopa de Letras. */
    public SopaDeLetras getPreguntaFinal() { return resultado; }

    // -- Clases auxiliares --

    /**
     * Fila representando una palabra y su pista asociada.
     */
    static class Fila {
        String palabra;
        String pista;
        Fila(String p, String s) { this.palabra = p; this.pista = s; }
    }

    /**
     * Modelo de tabla que gestiona las palabras y pistas de la Sopa de Letras.
     */
    static class ModeloPalabras extends AbstractTableModel {
        private final String[] cols = {"#", "Palabra", "Pista"};
        List<Fila> data = new ArrayList<>();

        public void setData(List<Fila> list) {
            data = list;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Object getValueAt(int r, int c) {
            Fila f = data.get(r);
            return switch (c) {
                case 0 -> r + 1;
                case 1 -> f.palabra;
                case 2 -> f.pista;
                default -> "";
            };
        }
    }
}


