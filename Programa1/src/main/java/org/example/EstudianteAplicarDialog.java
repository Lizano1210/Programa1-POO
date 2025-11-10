package org.example;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Diálogo que permite al estudiante aplicar una evaluación.
 * <p>
 * Controla el flujo de presentación de preguntas, el temporizador y
 * la recopilación de respuestas. Al finalizar, construye un objeto
 * {@link IntentoEvaluacion} y lo envía mediante un callback para su almacenamiento.
 * </p>
 */
public class EstudianteAplicarDialog extends JDialog {

    // -- Atributos principales --

    /** Estudiante que aplica la evaluación. */
    private final Estudiante estudiante;

    /** Asignación de evaluación que contiene los datos del grupo y la evaluación. */
    private final EvaluacionAsignada asignacion;

    /** Evaluación a aplicar. */
    private final Evaluacion evaluacion;

    /** Grupo al que pertenece el estudiante. */
    private final Grupo grupo;

    /** Función callback para guardar el intento de evaluación. */
    private final Consumer<IntentoEvaluacion> onGuardar;

    /** Indica si el intento fue guardado correctamente. */
    private boolean guardado = false;

    /** @return true si el intento fue guardado correctamente. */
    public boolean isGuardado() { return guardado; }

    /** Fecha y hora de inicio del intento. */
    private LocalDateTime inicio;

    /** Fecha y hora de finalización del intento. */
    private LocalDateTime fin;

    /** Lista de preguntas a resolver. */
    private final List<IPregunta> preguntas;

    /** Respuestas del estudiante (una por pregunta). */
    private final List<RespuestaEstudiante> respuestas;

    /** Índice de la pregunta actual. */
    private int idx = 0;

    // -- Componentes de interfaz --

    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblTimer = new JLabel("00:00");
    private final JTextArea txtDescripcion = new JTextArea(5, 50);
    private final JPanel panelInteractivo = new JPanel(new BorderLayout());
    private final JButton btnPrev = new JButton("Anterior");
    private final JButton btnNext = new JButton("Siguiente");
    private final JButton btnFinalizar = new JButton("Finalizar");
    private final JButton btnCancelar = new JButton("Cancelar");

    // -- Temporizador --

    private javax.swing.Timer timer;
    private int remainingSeconds;

    // -- Controles para preguntas de selección --

    private ButtonGroup grupoRadio;       // botones de selección única / verdadero-falso
    private JPanel opcionesPanel;         // contenedor de opciones

    // -- Constructor --

    /**
     * Crea el diálogo principal para aplicar una evaluación.
     *
     * @param owner ventana propietaria
     * @param estudiante estudiante que aplica la evaluación
     * @param asignacion evaluación asignada al grupo
     * @param onGuardar función callback para guardar el intento (puede ser null)
     */
    public EstudianteAplicarDialog(Window owner,
                                   Estudiante estudiante,
                                   EvaluacionAsignada asignacion,
                                   Consumer<IntentoEvaluacion> onGuardar) {
        super(owner, "Aplicar evaluación", ModalityType.APPLICATION_MODAL);
        this.estudiante = estudiante;
        this.asignacion = asignacion;
        this.evaluacion = asignacion.getEvaluacion();
        this.grupo = asignacion.getGrupo();
        this.onGuardar = (onGuardar == null ? it -> {} : onGuardar);

        this.preguntas = (evaluacion.getPreguntas() == null)
                ? new ArrayList<>()
                : evaluacion.getPreguntas();

        this.respuestas = new ArrayList<>();
        for (IPregunta p : preguntas) {
            this.respuestas.add(new RespuestaEstudiante(p));
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(owner);

        construirUI();
        iniciarTemporizador();
        cargarPregunta(0);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                guardado = false;
                if (timer != null) timer.stop();
            }
        });
    }

    /**
     * Constructor alternativo sin callback de guardado.
     */
    public EstudianteAplicarDialog(Window owner, Estudiante estudiante, EvaluacionAsignada asignacion) {
        this(owner, estudiante, asignacion, null);
    }

    // -- Construcción de interfaz --

    /**
     * Construye los componentes visuales del diálogo.
     */
    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JPanel header = new JPanel(new BorderLayout());
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(new JLabel("Tiempo:"));
        lblTimer.setFont(lblTimer.getFont().deriveFont(Font.BOLD, 14f));
        right.add(lblTimer);

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(new JScrollPane(txtDescripcion), BorderLayout.NORTH);
        center.add(panelInteractivo, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nav.add(btnPrev);
        nav.add(btnNext);
        nav.add(btnFinalizar);
        nav.add(btnCancelar);
        root.add(nav, BorderLayout.SOUTH);

        btnPrev.addActionListener(e -> onPrev());
        btnNext.addActionListener(e -> onNext());
        btnFinalizar.addActionListener(e -> onFinalizarYGuardar());
        btnCancelar.addActionListener(e -> onCancelar());
    }

    // -- Temporizador --

    /**
     * Inicia el temporizador de cuenta regresiva según la duración de la evaluación.
     */
    private void iniciarTemporizador() {
        inicio = LocalDateTime.now();
        int durMin = Math.max(1, evaluacion.getDuracionMinutos());
        remainingSeconds = durMin * 60;

        timer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            if (remainingSeconds < 0) remainingSeconds = 0;
            lblTimer.setText(formatoTiempo(remainingSeconds));
            if (remainingSeconds == 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                JOptionPane.showMessageDialog(this, "Se agotó el tiempo. Se enviará tu intento.");
                onFinalizarYGuardar();
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    /**
     * Formatea los segundos restantes en formato mm:ss.
     */
    private String formatoTiempo(int sec) {
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }

    // -- Navegación entre preguntas --

    /**
     * Carga la pregunta en la posición indicada.
     */
    private void cargarPregunta(int i) {
        if (i < 0 || i >= preguntas.size()) return;

        idx = i;
        lblTitulo.setText("Pregunta " + (i + 1) + " de " + preguntas.size());
        IPregunta p = preguntas.get(i);
        txtDescripcion.setText(p.obtenerDescripcion());

        panelInteractivo.removeAll();
        panelInteractivo.setBorder(BorderFactory.createTitledBorder("Responde aquí"));

        if (p instanceof Pregunta q) {
            pintarPreguntaSeleccion(q, i);
        } else if (p instanceof Pareo) {
            panelInteractivo.add(new JLabel("Pareo: interfaz simplificada no implementada."), BorderLayout.CENTER);
        } else if (p instanceof SopaDeLetras) {
            panelInteractivo.add(new JLabel("Sopa de letras: interfaz simplificada no implementada."), BorderLayout.CENTER);
        } else {
            panelInteractivo.add(new JLabel("Tipo no soportado."), BorderLayout.CENTER);
        }

        panelInteractivo.revalidate();
        panelInteractivo.repaint();

        btnPrev.setEnabled(i > 0);
        btnNext.setEnabled(i < preguntas.size() - 1);
    }

    /**
     * Dibuja las opciones para preguntas de tipo selección.
     */
    private void pintarPreguntaSeleccion(Pregunta q, int index) {
        opcionesPanel = new JPanel();
        opcionesPanel.setLayout(new BoxLayout(opcionesPanel, BoxLayout.Y_AXIS));
        RespuestaEstudiante re = respuestas.get(index);

        switch (q.getTipo()) {
            case SELECCION_UNICA, VERDADERO_FALSO -> {
                grupoRadio = new ButtonGroup();
                for (var resp : q.getRespuestas()) {
                    JRadioButton rb = new JRadioButton(resp.getOrden() + ") " + resp.getTexto());
                    rb.addActionListener(e -> {
                        re.limpiarSeleccion();
                        re.setOrdenesSeleccionados(resp.getOrden());
                    });
                    grupoRadio.add(rb);
                    opcionesPanel.add(rb);
                }
                marcarRadiosDesdeRespuesta(re);
            }
            case SELECCION_MULTIPLE -> {
                for (var resp : q.getRespuestas()) {
                    JCheckBox cb = new JCheckBox(resp.getOrden() + ") " + resp.getTexto());
                    cb.addActionListener(e -> {
                        List<Integer> current = new ArrayList<>(re.getOrdenesSeleccionados());
                        int ord = resp.getOrden();
                        if (cb.isSelected()) {
                            if (!current.contains(ord)) current.add(ord);
                        } else current.remove((Integer) ord);
                        re.setOrdenesSeleccionados(current);
                    });
                    opcionesPanel.add(cb);
                }
                marcarChecksDesdeRespuesta(re);
            }
            default -> opcionesPanel.add(new JLabel("Tipo no soportado."));
        }

        panelInteractivo.add(opcionesPanel, BorderLayout.CENTER);
    }

    /**
     * Marca los botones de opción según la respuesta guardada.
     */
    private void marcarRadiosDesdeRespuesta(RespuestaEstudiante re) {
        List<Integer> seleccion = re.getOrdenesSeleccionados();
        int sel = (seleccion.isEmpty() ? -1 : seleccion.get(0));
        for (Component c : opcionesPanel.getComponents()) {
            if (c instanceof JRadioButton rb) {
                String txt = rb.getText();
                int paren = txt.indexOf(')');
                if (paren > 0) {
                    try {
                        int ord = Integer.parseInt(txt.substring(0, paren));
                        rb.setSelected(ord == sel);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    /**
     * Marca las casillas de verificación según la respuesta guardada.
     */
    private void marcarChecksDesdeRespuesta(RespuestaEstudiante re) {
        List<Integer> seleccion = re.getOrdenesSeleccionados();
        for (Component c : opcionesPanel.getComponents()) {
            if (c instanceof JCheckBox cb) {
                String txt = cb.getText();
                int paren = txt.indexOf(')');
                if (paren > 0) {
                    try {
                        int ord = Integer.parseInt(txt.substring(0, paren));
                        cb.setSelected(seleccion.contains(ord));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    // -- Control de navegación --

    /** Muestra la pregunta anterior. */
    private void onPrev() { if (idx > 0) cargarPregunta(idx - 1); }

    /** Muestra la siguiente pregunta. */
    private void onNext() { if (idx < preguntas.size() - 1) cargarPregunta(idx + 1); }

    // -- Guardado y finalización --

    /**
     * Finaliza el intento y guarda las respuestas del estudiante.
     * <p>
     * Construye un {@link IntentoEvaluacion}, calcula la calificación y
     * ejecuta el callback {@link #onGuardar}.
     * </p>
     */
    private void onFinalizarYGuardar() {
        try {
            if (timer != null) timer.stop();
            fin = LocalDateTime.now();

            List<Integer> ordenUsado = new ArrayList<>();
            for (IPregunta p : preguntas) {
                if (p instanceof Pregunta pq) ordenUsado.add(pq.getId());
                else ordenUsado.add(ordenUsado.size() + 1);
            }

            IntentoEvaluacion intento = new IntentoEvaluacion(
                    estudiante,
                    evaluacion,
                    grupo,
                    inicio,
                    fin,
                    respuestas,
                    0,
                    0.0,
                    ordenUsado
            );

            intento.calcularCalificacion();
            onGuardar.accept(intento);

            guardado = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cancela la aplicación de la evaluación sin guardar.
     */
    private void onCancelar() {
        guardado = false;
        if (timer != null) timer.stop();
        dispose();
    }
}


