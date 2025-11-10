package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Panel principal del estudiante.
 * <p>
 * Permite consultar la información personal del estudiante, realizar matrículas en cursos y grupos,
 * y acceder a las evaluaciones asignadas con la posibilidad de rendirlas o visualizar notas.
 * </p>
 */
public class EstudianteDashboardPanel extends JPanel {

    // -- Servicios y referencias --

    /** Servicio de usuarios en memoria. */
    private final UsuarioServiceMem usuarioService;

    /** Servicio de cursos y grupos. */
    private final CursoService cursoService;

    /** Estudiante actual que usa el panel. */
    private final Estudiante estudiante;

    /** Acción a ejecutar cuando se matricula un estudiante en un grupo. */
    private final Consumer<Matricula> onMatricular;

    /** Proveedor que entrega la lista de evaluaciones asignadas al estudiante. */
    private final Supplier<List<EvaluacionAsignada>> proveedorAsignaciones;

    /** Proveedor que entrega los intentos realizados por el estudiante. */
    private final Supplier<List<IntentoEvaluacion>> proveedorIntentosEstudiante;

    /** Acción a ejecutar cuando se guarda un intento de evaluación. */
    private final Consumer<IntentoEvaluacion> onGuardarIntento;

    // -- Componentes principales --

    private final JTabbedPane tabs = new JTabbedPane();

    private final JTable tblCursos = new JTable();
    private final JTable tblGrupos = new JTable();
    private final CursosModel cursosModel = new CursosModel();
    private final GruposModel gruposModel = new GruposModel();
    private final JButton btnMatricular = new JButton("Matricular en grupo seleccionado");

    private final JTable tblAsignadas = new JTable();
    private final AsignadasModel asignadasModel = new AsignadasModel();
    private final JButton btnRefrescarEval = new JButton("Refrescar");
    private final JButton btnRendir = new JButton("Rendir");
    private final JButton btnVerNota = new JButton("Ver Nota");

    // -- Constructor --

    /**
     * Crea el panel principal del estudiante.
     *
     * @param usuarioService servicio de usuarios
     * @param cursoService servicio de cursos
     * @param estudiante instancia del estudiante actual
     * @param onMatricular acción al matricularse
     * @param proveedorAsignaciones proveedor de evaluaciones asignadas
     * @param proveedorIntentosEstudiante proveedor de intentos del estudiante
     * @param onGuardarIntento acción al guardar un intento de evaluación
     */
    public EstudianteDashboardPanel(UsuarioServiceMem usuarioService,
                                    CursoService cursoService,
                                    Estudiante estudiante,
                                    Consumer<Matricula> onMatricular,
                                    Supplier<List<EvaluacionAsignada>> proveedorAsignaciones,
                                    Supplier<List<IntentoEvaluacion>> proveedorIntentosEstudiante,
                                    Consumer<IntentoEvaluacion> onGuardarIntento) {

        this.usuarioService = Objects.requireNonNull(usuarioService);
        this.cursoService = Objects.requireNonNull(cursoService);
        this.estudiante = Objects.requireNonNull(estudiante);
        this.onMatricular = Objects.requireNonNull(onMatricular);
        this.proveedorAsignaciones = Objects.requireNonNull(proveedorAsignaciones);
        this.proveedorIntentosEstudiante = Objects.requireNonNull(proveedorIntentosEstudiante);
        this.onGuardarIntento = Objects.requireNonNull(onGuardarIntento);

        setLayout(new BorderLayout());
        construirUI();
        cargarCursos();
        cargarEvaluaciones();
    }

    // -- Construcción de interfaz --

    /**
     * Construye la interfaz gráfica del panel con sus pestañas.
     */
    private void construirUI() {
        JPanel info = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.anchor = GridBagConstraints.WEST;

        int r = 0;
        infoAdd(info, c, r++, "Nombre:", estudiante.getNombre() + " " + estudiante.getApellido1() + " " + estudiante.getApellido2());
        infoAdd(info, c, r++, "ID:", estudiante.getIdUsuario());
        infoAdd(info, c, r++, "Correo:", estudiante.getCorreo());
        infoAdd(info, c, r++, "Teléfono:", estudiante.getTelefono());
        infoAdd(info, c, r++, "Dirección:", estudiante.getDireccion());
        infoAdd(info, c, r++, "Org. DL:", estudiante.getOrgDL());
        infoAdd(info, c, r++, "Temas interés:", (estudiante.getTemIN()==null?"-":String.join(", ", estudiante.getTemIN())));

        tabs.addTab("Información", new JScrollPane(info));

        // -- Pestaña de matrícula --
        JPanel matricula = new JPanel(new BorderLayout(8,8));
        JPanel top = new JPanel(new GridLayout(1,2,8,8));

        tblCursos.setModel(cursosModel);
        tblGrupos.setModel(gruposModel);
        top.add(wrap("Cursos", new JScrollPane(tblCursos)));
        top.add(wrap("Grupos del curso seleccionado", new JScrollPane(tblGrupos)));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnMatricular);

        matricula.add(top, BorderLayout.CENTER);
        matricula.add(south, BorderLayout.SOUTH);

        tblCursos.getSelectionModel().addListSelectionListener(e -> {
            int row = tblCursos.getSelectedRow();
            if (row >= 0) {
                Curso csel = cursosModel.getAt(row);
                cargarGrupos(csel);
            } else {
                gruposModel.setData(new ArrayList<>());
            }
        });

        btnMatricular.addActionListener(e -> onMatricularClicked());
        tabs.addTab("Matrícula", matricula);

        // -- Pestaña de evaluaciones --
        JPanel eval = new JPanel(new BorderLayout(8,8));
        tblAsignadas.setModel(asignadasModel);
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("Evaluaciones asignadas"));
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        east.add(btnRefrescarEval);
        east.add(btnRendir);
        east.add(btnVerNota);

        JPanel topEval = new JPanel(new BorderLayout());
        topEval.add(north, BorderLayout.WEST);
        topEval.add(east, BorderLayout.EAST);

        btnRefrescarEval.addActionListener(e -> cargarEvaluaciones());
        btnRendir.addActionListener(e -> onRendirEvaluacion());
        btnVerNota.addActionListener(e -> onVerNota());

        eval.add(topEval, BorderLayout.NORTH);
        eval.add(new JScrollPane(tblAsignadas), BorderLayout.CENTER);
        tabs.addTab("Evaluaciones", eval);

        add(tabs, BorderLayout.CENTER);
    }

    // -- Auxiliares de interfaz --

    /** Agrega una fila de información al panel de datos personales. */
    private void infoAdd(JPanel p, GridBagConstraints c, int row, String label, String value) {
        c.gridx=0; c.gridy=row; c.weightx=0; p.add(new JLabel(label), c);
        c.gridx=1; c.gridy=row; c.weightx=1; p.add(new JLabel(value == null ? "-" : value), c);
    }

    /** Envuelve un componente con un borde titulado. */
    private JPanel wrap(String title, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    // -- Carga de datos --

    /** Carga los cursos disponibles desde el servicio. */
    private void cargarCursos() {
        try {
            List<Curso> cursos = cursoService.listarCursos();
            cursosModel.setData(cursos == null ? new ArrayList<>() : cursos);
            if (!cursosModel.data.isEmpty()) {
                tblCursos.setRowSelectionInterval(0,0);
                cargarGrupos(cursosModel.getAt(0));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No fue posible cargar los cursos: " + ex.getMessage());
        }
    }

    /** Carga los grupos pertenecientes al curso seleccionado. */
    private void cargarGrupos(Curso curso) {
        try {
            List<Grupo> grupos = (curso == null) ? new ArrayList<>() : curso.grupos;
            gruposModel.setData(grupos == null ? new ArrayList<>() : grupos);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No fue posible cargar los grupos: " + ex.getMessage());
        }
    }

    /** Carga las evaluaciones asignadas al estudiante. */
    private void cargarEvaluaciones() {
        try {
            List<EvaluacionAsignada> lst = proveedorAsignaciones.get();
            asignadasModel.setData(lst == null ? new ArrayList<>() : lst);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No fue posible cargar evaluaciones: " + ex.getMessage());
        }
    }

    // -- Matrícula --

    /**
     * Ejecuta el proceso de matrícula del estudiante en el grupo seleccionado.
     */
    private void onMatricularClicked() {
        int rc = tblCursos.getSelectedRow();
        int rg = tblGrupos.getSelectedRow();
        if (rc < 0 || rg < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un curso y un grupo.");
            return;
        }
        Curso csel = cursosModel.getAt(rc);
        Grupo gsel = gruposModel.getAt(rg);
        if (gsel == null) {
            JOptionPane.showMessageDialog(this, "Grupo inválido.");
            return;
        }

        try {
            Matricula m = new Matricula(estudiante, gsel);
            onMatricular.accept(m);
            JOptionPane.showMessageDialog(this, "¡Matrícula registrada!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No fue posible matricular: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -- Evaluaciones --

    /** Obtiene la evaluación actualmente seleccionada en la tabla. */
    private EvaluacionAsignada asignadaSeleccionada() {
        int r = tblAsignadas.getSelectedRow();
        return (r < 0) ? null : asignadasModel.getAt(r);
    }

    /**
     * Permite al estudiante rendir una evaluación asignada.
     */
    private void onRendirEvaluacion() {
        EvaluacionAsignada ea = asignadaSeleccionada();
        if (ea == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una evaluación.");
            return;
        }
        if (yaExisteIntento(ea)) {
            JOptionPane.showMessageDialog(this, "Ya realizaste esta evaluación.");
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        EstudianteAplicarDialog dlg = new EstudianteAplicarDialog(owner, estudiante, ea, onGuardarIntento);
        dlg.setVisible(true);
        if (dlg.isGuardado()) {
            JOptionPane.showMessageDialog(this, "Tu intento fue guardado.");
            cargarEvaluaciones();
        }
    }

    /**
     * Muestra la nota obtenida en una evaluación ya realizada.
     */
    private void onVerNota() {
        EvaluacionAsignada ea = asignadaSeleccionada();
        if (ea == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una evaluación.");
            return;
        }
        IntentoEvaluacion it = intentoDelEstudiante(ea);
        if (it == null) {
            JOptionPane.showMessageDialog(this, "Aún no has realizado esta evaluación.");
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Puntaje: " + it.getPuntajeObtenido() + " / " +
                        (ea.getEvaluacion() == null ? "-" : ea.getEvaluacion().getPuntajeTotal()) +
                        "\nCalificación: " + String.format("%.2f", it.getCalificacion()));
    }

    // -- Verificación de intentos --

    /** Verifica si el estudiante ya realizó la evaluación. */
    private boolean yaExisteIntento(EvaluacionAsignada ea) {
        return intentoDelEstudiante(ea) != null;
    }

    /**
     * Busca un intento del estudiante para una evaluación específica.
     *
     * @param ea evaluación asignada
     * @return intento correspondiente o null si no existe
     */
    private IntentoEvaluacion intentoDelEstudiante(EvaluacionAsignada ea) {
        List<IntentoEvaluacion> intentos = proveedorIntentosEstudiante.get();
        if (intentos == null) return null;
        String miId = estudiante.getIdUsuario();
        int gid = ea.getGrupo() == null ? -1 : ea.getGrupo().getIdGrupo();
        int eid = ea.getEvaluacion() == null ? -1 : ea.getEvaluacion().getId();
        for (IntentoEvaluacion it : intentos) {
            if (it.getEstudiante() != null
                    && miId.equals(it.getEstudiante().getIdUsuario())
                    && it.getGrupo() != null
                    && it.getEvaluacion() != null
                    && it.getGrupo().getIdGrupo() == gid
                    && it.getEvaluacion().getId() == eid) {
                return it;
            }
        }
        return null;
    }

    // -- Modelos de tablas auxiliares --

    /** Modelo de tabla para cursos disponibles. */
    static class CursosModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Nombre", "Modalidad", "Tipo", "Grupos"};
        private List<Curso> data = new ArrayList<>();
        public void setData(List<Curso> list) { data = list == null ? new ArrayList<>() : list; fireTableDataChanged(); }
        public Curso getAt(int r) { return data.get(r); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Curso x = data.get(r);
            return switch (c) {
                case 0 -> x.getId();
                case 1 -> x.getNombre();
                case 2 -> x.getModalidad();
                case 3 -> x.getTipo();
                case 4 -> (x.grupos == null ? 0 : x.grupos.size());
                default -> "";
            };
        }
    }

    /** Modelo de tabla para grupos pertenecientes a un curso. */
    static class GruposModel extends AbstractTableModel {
        private final String[] cols = {"Sigla", "Curso", "Profesor", "Cupo", "Matriculados"};
        private List<Grupo> data = new ArrayList<>();
        public void setData(List<Grupo> list) { data = list == null ? new ArrayList<>() : list; fireTableDataChanged(); }
        public Grupo getAt(int r) { return data.get(r); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Grupo g = data.get(r);
            return switch (c) {
                case 0 -> g.getIdGrupo();
                case 1 -> g.getCurso() == null ? "-" : g.getCurso().getNombre();
                case 2 -> (g.getProfesor() == null ? "-" : g.getProfesor().getNombre());
                case 3 -> g.getCurso() == null ? 0 : g.getCurso().getMaxEstu();
                case 4 -> g.getMatriculas() == null ? 0 : g.getMatriculas().size();
                default -> "";
            };
        }
    }

    /** Modelo de tabla para evaluaciones asignadas al estudiante. */
    static class AsignadasModel extends AbstractTableModel {
        private final String[] cols = {"Evaluación", "Grupo", "Inicio", "Fin", "Duración (min)", "Mi calificación"};
        private List<EvaluacionAsignada> data = new ArrayList<>();
        public void setData(List<EvaluacionAsignada> list) { data = list == null ? new ArrayList<>() : list; fireTableDataChanged(); }
        public EvaluacionAsignada getAt(int r) { return data.get(r); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            EvaluacionAsignada ea = data.get(r);
            return switch (c) {
                case 0 -> ea.getEvaluacion() == null ? "-" : ea.getEvaluacion().getNombre();
                case 1 -> ea.getGrupo() == null ? "-" : ea.getGrupo().getIdGrupo();
                case 2 -> ea.getFechaHoraInicio();
                case 3 -> ea.getFechaHoraFinal();
                case 4 -> ea.getEvaluacion() == null ? 0 : ea.getEvaluacion().getDuracionMinutos();
                case 5 -> "-";
                default -> "";
            };
        }
    }
}
