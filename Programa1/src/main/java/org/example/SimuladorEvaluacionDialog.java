package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

/**
 * Simulador de evaluación (no persiste intentos).
 * - Selección (Pregunta): radios/checkboxes
 * - Pareo: listas + asociaciones visibles
 * - Sopa de letras: cuadrícula + checkboxes de palabras
 */
public class SimuladorEvaluacionDialog extends JDialog {

    private final Evaluacion evaluacion;
    private final List<IPregunta> ordenPreguntas;

    // Estado de navegación
    private int idx = 0;

    // Respuestas parciales por id de pregunta (clave = id Pregunta/Pareo/Sopa)
    private final Map<Integer, RespuestaEstudiante> respuestas = new HashMap<>();

    // UI fija
    private final JLabel lblTitulo = new JLabel();
    private final JTextArea txtInstr = new JTextArea(3, 40);
    private final JPanel center = new JPanel(new BorderLayout());
    private final JButton btnPrev = new JButton("← Anterior");
    private final JButton btnNext = new JButton("Siguiente →");
    private final JButton btnEnviar = new JButton("Calcular puntaje");

    // === Temporizador ===
    private final JLabel lblTiempo = new JLabel("00:00");
    private final JProgressBar pbTiempo = new JProgressBar();
    private Timer timer;
    private int totalSeconds;
    private int remaining;

    // === UI para Selección ===
    private ButtonGroup groupUnica;
    private java.util.List<JCheckBox> checksMultiple;

    // === UI para Pareo ===
    private JList<String> listEnunciados;
    private JList<String> listRespuestas;
    private JButton btnAsociar;
    private JButton btnQuitarAsoc;
    private JTable tblAsoc;
    private AsocModel asocModel; // almacena pares enIdx->respIdx (para UI)
    // Nota: Al guardar convertimos esos pares a [enIdx, respIdx, enIdx2, respIdx2...]

    // === UI para Sopa ===
    private JTextArea txtGrid;
    private JPanel pnlWords; // checkboxes por palabra
    private java.util.List<JCheckBox> checksWords;

    public SimuladorEvaluacionDialog(Window owner, Evaluacion evaluacion) {
        super(owner, "Simulación: " + (evaluacion == null ? "" : evaluacion.getNombre()), ModalityType.APPLICATION_MODAL);
        if (evaluacion == null) throw new IllegalArgumentException("Evaluación requerida");
        this.evaluacion = evaluacion;

        // Orden de preguntas (respeta bandera de aleatoriedad)
        List<IPregunta> base = evaluacion.getPreguntas() == null ? new ArrayList<>() : new ArrayList<>(evaluacion.getPreguntas());
        if (evaluacion.isPreguntasAleatorias()) Collections.shuffle(base);
        this.ordenPreguntas = base;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 640));
        setLocationRelativeTo(owner);

        construirUI();
        prepararTemporizador(); // <<-- prepara reloj y barra
        cargarPregunta();
        iniciarTemporizador();  // <<-- arranca el conteo
        pack();
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setContentPane(root);

        // Cabecera con título + instrucciones + reloj
        JPanel north = new JPanel(new BorderLayout(6,6));

        // fila superior: título a la izquierda, reloj a la derecha
        JPanel topRow = new JPanel(new BorderLayout(8,0));
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        topRow.add(lblTitulo, BorderLayout.WEST);

        JPanel reloj = new JPanel(new BorderLayout(6,0));
        JLabel lblTxt = new JLabel("Tiempo:");
        lblTiempo.setFont(lblTiempo.getFont().deriveFont(Font.BOLD, 14f));
        reloj.add(lblTxt, BorderLayout.WEST);
        reloj.add(lblTiempo, BorderLayout.CENTER);
        reloj.add(pbTiempo, BorderLayout.SOUTH);

        topRow.add(reloj, BorderLayout.EAST);
        north.add(topRow, BorderLayout.NORTH);

        txtInstr.setEditable(false);
        txtInstr.setLineWrap(true);
        txtInstr.setWrapStyleWord(true);
        north.add(new JScrollPane(txtInstr), BorderLayout.CENTER);

        root.add(north, BorderLayout.NORTH);

        // Centro dinámico
        root.add(center, BorderLayout.CENTER);

        // Pie de navegación
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnPrev);
        south.add(btnNext);
        south.add(btnEnviar);
        root.add(south, BorderLayout.SOUTH);

        btnPrev.addActionListener(e -> { if (idx > 0) { guardarParcial(); idx--; cargarPregunta(); } });
        btnNext.addActionListener(e -> { if (idx < ordenPreguntas.size()-1) { guardarParcial(); idx++; cargarPregunta(); } });
        btnEnviar.addActionListener(e -> onEnviar());
    }

    // temporizador
    private void prepararTemporizador() {
        int mins = Math.max(1, evaluacion.getDuracionMinutos());
        totalSeconds = mins * 60;
        remaining = totalSeconds;

        pbTiempo.setMinimum(0);
        pbTiempo.setMaximum(totalSeconds);
        pbTiempo.setValue(totalSeconds);
        actualizarReloj();

        ActionListener tick = e -> {
            remaining--;
            if (remaining < 0) remaining = 0;
            actualizarReloj();
            if (remaining == 0) {
                timer.stop();
                finalizarPorTiempo();
            }
        };
        timer = new Timer(1000, tick);
    }

    private void iniciarTemporizador() {
        if (timer != null && !timer.isRunning()) timer.start();
    }

    private void actualizarReloj() {
        int mm = remaining / 60;
        int ss = remaining % 60;
        lblTiempo.setText(String.format("%02d:%02d", mm, ss));
        pbTiempo.setValue(remaining);
        setTitle(evaluacion.getNombre() + " — Tiempo: " + lblTiempo.getText());
    }

    private void finalizarPorTiempo() {
        // Deshabilitar UI
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        btnEnviar.setEnabled(false);
        center.setEnabled(false);

        onFinalizarEvaluacionPorTiempo();
    }

    private void onFinalizarEvaluacionPorTiempo() {
        guardarParcial();
        // Calcula igual que onEnviar, pero con mensaje de tiempo agotado
        int total = 0;
        int obtenido = 0;

        for (IPregunta p : ordenPreguntas) {
            total += p.obtenerPuntos();
            RespuestaEstudiante re = null;

            if (p instanceof Pregunta q) re = respuestas.get(q.getId());
            else if (p instanceof Pareo pr) re = respuestas.get(pr.getId());
            else if (p instanceof SopaDeLetras sl) re = respuestas.get(sl.getId());

            int pts = p.calificar(re);
            obtenido += pts;
        }

        double porcentaje = (total == 0) ? 0.0 : (obtenido * 100.0 / total);
        JOptionPane.showMessageDialog(this,
                "Tiempo agotado.\nPuntaje: " + obtenido + " / " + total + " (" + String.format("%.1f", porcentaje) + "%)",
                "Evaluación entregada automáticamente",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    @Override
    public void dispose() {
        if (timer != null && timer.isRunning()) timer.stop();
        super.dispose();
    }

    // ===================== Navegación y render =====================
    private void cargarPregunta() {
        center.removeAll();

        if (ordenPreguntas.isEmpty()) {
            center.add(new JLabel("La evaluación no tiene preguntas."), BorderLayout.CENTER);
            actualizarCabecera();
            habilitarNav(false);
            revalidate(); repaint();
            return;
        }

        IPregunta p = ordenPreguntas.get(idx);
        actualizarCabecera();

        if (p instanceof Pregunta q) {
            center.add(renderPreguntaSeleccion(q), BorderLayout.CENTER);
        } else if (p instanceof Pareo pr) {
            center.add(renderPareo(pr), BorderLayout.CENTER);
        } else if (p instanceof SopaDeLetras sl) {
            center.add(renderSopa(sl), BorderLayout.CENTER);
        } else {
            center.add(renderNoImplementado("Tipo de pregunta no reconocido"), BorderLayout.CENTER);
        }

        habilitarNav(true);
        revalidate(); repaint();
    }

    private void actualizarCabecera() {
        lblTitulo.setText(evaluacion.getNombre() + "  —  Pregunta " + (idx+1) + " / " + Math.max(1, ordenPreguntas.size()));
        txtInstr.setText(evaluacion.getInstrucciones() == null ? "" : evaluacion.getInstrucciones());
    }

    private void habilitarNav(boolean hasData) {
        btnPrev.setEnabled(hasData && idx > 0);
        btnNext.setEnabled(hasData && idx < ordenPreguntas.size()-1);
        btnEnviar.setEnabled(hasData && !ordenPreguntas.isEmpty());
    }

    private JComponent renderNoImplementado(String msg) {
        JPanel p = new JPanel(new BorderLayout());
        JTextArea ta = new JTextArea(msg + " — vista no implementada.");
        ta.setEditable(false);
        p.add(ta, BorderLayout.CENTER);
        return p;
    }

    // Preguntas de selección
    private JComponent renderPreguntaSeleccion(Pregunta q) {
        JPanel p = new JPanel(new BorderLayout(6,6));

        JTextArea enun = new JTextArea(q.obtenerDescripcion());
        enun.setLineWrap(true);
        enun.setWrapStyleWord(true);
        enun.setEditable(false);
        enun.setBorder(BorderFactory.createTitledBorder("Enunciado"));
        p.add(new JScrollPane(enun), BorderLayout.NORTH);

        JPanel opciones = new JPanel();
        opciones.setBorder(BorderFactory.createTitledBorder("Opciones"));
        opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));

        // Recuperar selección parcial
        RespuestaEstudiante parcial = respuestas.get(q.getId());

        switch (q.getTipo()) {
            case SELECCION_UNICA, VERDADERO_FALSO -> {
                groupUnica = new ButtonGroup();
                for (Respuesta r : q.getRespuestas()) {
                    JRadioButton rb = new JRadioButton(formatearOpcion(r));
                    rb.setActionCommand(String.valueOf(r.getOrden()));
                    groupUnica.add(rb);
                    opciones.add(rb);
                }
                if (parcial != null && parcial.getOrdenesSeleccionados() != null && !parcial.getOrdenesSeleccionados().isEmpty()) {
                    String ord = String.valueOf(parcial.getOrdenesSeleccionados().get(0));
                    Enumeration<AbstractButton> en = groupUnica.getElements();
                    while (en.hasMoreElements()) {
                        AbstractButton b = en.nextElement();
                        if (Objects.equals(b.getActionCommand(), ord)) { b.setSelected(true); break; }
                    }
                }
            }
            case SELECCION_MULTIPLE -> {
                checksMultiple = new ArrayList<>();
                for (Respuesta r : q.getRespuestas()) {
                    JCheckBox cb = new JCheckBox(formatearOpcion(r));
                    cb.putClientProperty("orden", r.getOrden());
                    checksMultiple.add(cb);
                    opciones.add(cb);
                }
                if (parcial != null && parcial.getOrdenesSeleccionados() != null) {
                    java.util.Set<Integer> sel = new java.util.HashSet<>(parcial.getOrdenesSeleccionados());
                    for (JCheckBox cb : checksMultiple) {
                        Integer ord = (Integer) cb.getClientProperty("orden");
                        cb.setSelected(ord != null && sel.contains(ord));
                    }
                }
            }
            default -> {}
        }
        p.add(opciones, BorderLayout.CENTER);
        return p;
    }

    private String formatearOpcion(Respuesta r) {
        return "[" + r.getOrden() + "] " + r.getTexto();
    }

    // ===================== PAREO =====================
    private JComponent renderPareo(Pareo pr) {
        JPanel root = new JPanel(new BorderLayout(8,8));

        // Panel izquierdo: enunciados
        DefaultListModel<String> mEn = new DefaultListModel<>();
        for (int i = 0; i < pr.getEnunciados().size(); i++) {
            mEn.addElement("[" + i + "] " + pr.getEnunciados().get(i));
        }
        listEnunciados = new JList<>(mEn);
        listEnunciados.setBorder(BorderFactory.createTitledBorder("Enunciados (izquierda)"));

        // Panel derecho: respuestas
        DefaultListModel<String> mRe = new DefaultListModel<>();
        for (int j = 0; j < pr.getRespuestas().size(); j++) {
            mRe.addElement("[" + j + "] " + pr.getRespuestas().get(j));
        }
        listRespuestas = new JList<>(mRe);
        listRespuestas.setBorder(BorderFactory.createTitledBorder("Respuestas (derecha)"));

        JPanel lists = new JPanel(new GridLayout(1,2,8,8));
        lists.add(new JScrollPane(listEnunciados));
        lists.add(new JScrollPane(listRespuestas));

        // Acciones de asociación
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAsociar = new JButton("Asociar seleccionados");
        btnQuitarAsoc = new JButton("Eliminar asociación");
        actions.add(btnAsociar);
        actions.add(btnQuitarAsoc);

        // Tabla de asociaciones actuales (enIdx -> respIdx)
        asocModel = new AsocModel();
        // precargar desde respuesta parcial si existiera
        RespuestaEstudiante parcial = respuestas.get(pr.getId());
        if (parcial != null && parcial.getOrdenesSeleccionados() != null) {
            List<Integer> pares = parcial.getOrdenesSeleccionados();
            for (int i = 0; i+1 < pares.size(); i += 2) {
                asocModel.add(pares.get(i), pares.get(i+1));
            }
        }
        tblAsoc = new JTable(asocModel);
        tblAsoc.setBorder(BorderFactory.createTitledBorder("Asociaciones"));
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setBorder(BorderFactory.createTitledBorder("Asociaciones definidas"));
        tableWrap.add(new JScrollPane(tblAsoc), BorderLayout.CENTER);

        // Cableado botones
        btnAsociar.addActionListener(e -> {
            int i = listEnunciados.getSelectedIndex();
            int j = listRespuestas.getSelectedIndex();
            if (i < 0 || j < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione un enunciado y una respuesta.");
                return;
            }
            asocModel.add(i, j);
        });

        btnQuitarAsoc.addActionListener(e -> {
            int r = tblAsoc.getSelectedRow();
            if (r >= 0) asocModel.remove(r);
        });

        root.add(lists, BorderLayout.NORTH);
        root.add(actions, BorderLayout.CENTER);
        root.add(tableWrap, BorderLayout.SOUTH);
        return root;
    }

    // ===================== SOPA =====================
    private JComponent renderSopa(SopaDeLetras sl) {
        JPanel root = new JPanel(new BorderLayout(8,8));

        // Generar cuadrícula si no hay (o si está vacía)
        boolean necesitaGenerar = (sl.getPalabrasEncontradas() == null || sl.getPalabrasEncontradas().isEmpty());
        if (necesitaGenerar) {
            sl.generarCuadricula(); // usa la lógica de tu modelo
        }

        // Cuadrícula visual
        txtGrid = new JTextArea(sl.cuadriculaToString());
        txtGrid.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        txtGrid.setEditable(false);
        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setBorder(BorderFactory.createTitledBorder("Cuadrícula"));
        gridWrap.add(new JScrollPane(txtGrid), BorderLayout.CENTER);

        // Lista de palabras con checkbox (marcar encontradas)
        pnlWords = new JPanel();
        pnlWords.setLayout(new BoxLayout(pnlWords, BoxLayout.Y_AXIS));
        pnlWords.setBorder(BorderFactory.createTitledBorder("Palabras (marque las encontradas)"));

        checksWords = new ArrayList<>();
        List<SopaDeLetras.Enunciado> ens = sl.getEnunciados();
        for (int i = 0; i < ens.size(); i++) {
            SopaDeLetras.Enunciado en = ens.get(i);
            JCheckBox cb = new JCheckBox("[" + i + "] " + en.getPalabra() + " — " + en.getPista());
            cb.putClientProperty("idx", i);
            checksWords.add(cb);
            pnlWords.add(cb);
        }

        // Preseleccionar desde respuesta parcial si existiera
        RespuestaEstudiante parcial = respuestas.get(sl.getId());
        if (parcial != null && parcial.getOrdenesSeleccionados() != null) {
            Set<Integer> sel = new HashSet<>(parcial.getOrdenesSeleccionados());
            for (JCheckBox cb : checksWords) {
                Integer ix = (Integer) cb.getClientProperty("idx");
                cb.setSelected(ix != null && sel.contains(ix));
            }
        }

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gridWrap, new JScrollPane(pnlWords));
        split.setResizeWeight(0.6);
        return split;
    }

    // Guardado
    private void guardarParcial() {
        if (ordenPreguntas.isEmpty()) return;
        IPregunta p = ordenPreguntas.get(idx);

        if (p instanceof Pregunta q) {
            List<Integer> seleccion = new ArrayList<>();
            switch (q.getTipo()) {
                case SELECCION_UNICA, VERDADERO_FALSO -> {
                    if (groupUnica != null) {
                        ButtonModel bm = groupUnica.getSelection();
                        if (bm != null) {
                            try { seleccion.add(Integer.parseInt(bm.getActionCommand())); } catch (NumberFormatException ignore) {}
                        }
                    }
                }
                case SELECCION_MULTIPLE -> {
                    if (checksMultiple != null) {
                        for (JCheckBox cb : checksMultiple) {
                            if (cb.isSelected()) {
                                Integer ord = (Integer) cb.getClientProperty("orden");
                                if (ord != null) seleccion.add(ord);
                            }
                        }
                    }
                }
                default -> {}
            }
            RespuestaEstudiante re = new RespuestaEstudiante(q);
            re.setOrdenesSeleccionados(seleccion);
            respuestas.put(q.getId(), re);
            return;
        }

        if (p instanceof Pareo pr) {
            // Convertir asociaciones del modelo UI a pares [en, resp, en, resp...]
            List<Integer> pares = new ArrayList<>();
            if (asocModel != null) {
                for (Asoc a : asocModel.data) {
                    pares.add(a.enIdx);
                    pares.add(a.respIdx);
                }
            }
            RespuestaEstudiante re = new RespuestaEstudiante(pr);
            re.setOrdenesSeleccionados(pares);
            respuestas.put(pr.getId(), re);
            return;
        }

        if (p instanceof SopaDeLetras sl) {
            List<Integer> marcadas = new ArrayList<>();
            if (checksWords != null) {
                for (JCheckBox cb : checksWords) {
                    if (cb.isSelected()) {
                        Integer ix = (Integer) cb.getClientProperty("idx");
                        if (ix != null) marcadas.add(ix);
                    }
                }
            }
            RespuestaEstudiante re = new RespuestaEstudiante(sl);
            re.setOrdenesSeleccionados(marcadas);
            respuestas.put(sl.getId(), re);
        }
    }

    // Finalizar prueba
    private void onEnviar() {
        if (timer != null && timer.isRunning()) timer.stop();
        guardarParcial(); // guarda la actual también

        int total = 0;
        int obtenido = 0;

        for (IPregunta p : ordenPreguntas) {
            total += p.obtenerPuntos();
            RespuestaEstudiante re = null;

            if (p instanceof Pregunta q) re = respuestas.get(q.getId());
            else if (p instanceof Pareo pr) re = respuestas.get(pr.getId());
            else if (p instanceof SopaDeLetras sl) re = respuestas.get(sl.getId());

            int pts = p.calificar(re); // usa tu lógica de cada tipo
            obtenido += pts;
        }

        double porcentaje = (total == 0) ? 0.0 : (obtenido * 100.0 / total);
        JOptionPane.showMessageDialog(this,
                "Puntaje obtenido: " + obtenido + " / " + total + " (" + String.format("%.1f", porcentaje) + "%)",
                "Resultado de la simulación",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    // UI pareo
    static class Asoc {
        final int enIdx;
        final int respIdx;
        Asoc(int e, int r) { this.enIdx = e; this.respIdx = r; }
    }

    static class AsocModel extends AbstractTableModel {
        private final String[] cols = {"Enunciado idx", "Respuesta idx"};
        final java.util.List<Asoc> data = new ArrayList<>();
        public void add(int en, int re) {
            // Si ya existe entrada para ese enunciado, la reemplazamos (una correcta por enunciado)
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).enIdx == en) {
                    data.set(i, new Asoc(en, re));
                    fireTableRowsUpdated(i, i);
                    return;
                }
            }
            data.add(new Asoc(en, re));
            fireTableRowsInserted(data.size()-1, data.size()-1);
        }
        public void remove(int row) {
            if (row < 0 || row >= data.size()) return;
            data.remove(row);
            fireTableDataChanged();
        }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Asoc a = data.get(r);
            return c == 0 ? a.enIdx : a.respIdx;
        }
    }
}
