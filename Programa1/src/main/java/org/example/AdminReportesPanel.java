package org.example;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel de reportes del administrador.
 * <p>
 * Permite generar listados y estadísticas de matrícula según el alcance:
 * - Todos los cursos
 * - Un curso específico
 * - Un grupo específico dentro de un curso
 * </p>
 * Incluye selección de fecha de vigencia, alcance y botones para generar
 * los archivos de reporte (CSV y TXT).
 */
public class AdminReportesPanel extends JPanel {

    // -- Servicios --

    /** Servicio encargado de la gestión de cursos. */
    private final CursoService cursoService;

    /** Servicio encargado de la generación de reportes. */
    private final ReporteService reporteService;

    // -- Componentes de interfaz --

    /** Campo para ingresar la fecha de vigencia (formato YYYY-MM-DD). */
    private final JTextField txtFecha = new JTextField(10);

    /** Opción para generar reportes de todos los cursos. */
    private final JRadioButton rbTodos = new JRadioButton("Todos", true);

    /** Opción para generar reportes de un curso específico. */
    private final JRadioButton rbCurso = new JRadioButton("Curso");

    /** Opción para generar reportes de un grupo específico. */
    private final JRadioButton rbGrupo = new JRadioButton("Grupo");

    /** Lista desplegable de cursos disponibles. */
    private final JComboBox<Curso> cbCurso = new JComboBox<>();

    /** Lista desplegable de grupos (dependiente del curso seleccionado). */
    private final JComboBox<Grupo> cbGrupo = new JComboBox<>();

    // -- Constructor --

    /**
     * Crea un nuevo panel de reportes.
     *
     * @param cursoService servicio de cursos
     * @param reporteService servicio de reportes
     */
    public AdminReportesPanel(CursoService cursoService, ReporteService reporteService) {
        this.cursoService = cursoService;
        this.reporteService = reporteService;

        setLayout(new BorderLayout(8,8));

        // -- Formulario principal --
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbTodos);
        bg.add(rbCurso);
        bg.add(rbGrupo);

        int row = 0;
        addRow(form, c, row++, new JLabel("Fecha vigencia (YYYY-MM-DD):"), txtFecha);
        addRow(form, c, row++, new JLabel("Alcance:"), paneAlcance());
        add(form, BorderLayout.NORTH);

        // -- Panel de botones --
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLista = new JButton("Generar Lista (CSV)");
        JButton btnEstad = new JButton("Generar Estadística (TXT)");
        actions.add(btnLista);
        actions.add(btnEstad);
        add(actions, BorderLayout.SOUTH);

        // -- Eventos de selección --
        rbTodos.addActionListener(_evt -> updateEnable());
        rbCurso.addActionListener(_evt -> updateEnable());
        rbGrupo.addActionListener(_evt -> updateEnable());
        cbCurso.addActionListener(_evt -> cargarGrupos());

        // -- Carga inicial --
        cargarCursos();
        updateEnable();

        // -- Acciones de botones --
        btnLista.addActionListener(_evt -> generarLista());
        btnEstad.addActionListener(_evt -> generarEstadistica());
    }

    // -- Panel de alcance --

    /**
     * Crea el subpanel con las opciones de alcance (todos, curso o grupo).
     *
     * @return panel con los controles de selección
     */
    private JPanel paneAlcance() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; p.add(rbTodos, c);
        c.gridx=0; c.gridy=1; p.add(rbCurso, c);
        c.gridx=1; c.gridy=1; p.add(cbCurso, c);
        c.gridx=0; c.gridy=2; p.add(rbGrupo, c);
        c.gridx=1; c.gridy=2; p.add(cbGrupo, c);
        return p;
    }

    // -- Utilidades de interfaz --

    /**
     * Agrega una fila con dos componentes a un panel.
     *
     * @param panel panel destino
     * @param c restricciones de diseño
     * @param row número de fila
     * @param left componente izquierdo (etiqueta)
     * @param right componente derecho (campo)
     */
    private void addRow(JPanel panel, GridBagConstraints c, int row, JComponent left, JComponent right) {
        c.gridx=0; c.gridy=row; c.weightx=0; panel.add(left, c);
        c.gridx=1; c.gridy=row; c.weightx=1; panel.add(right, c);
    }

    // -- Carga de combos --

    /**
     * Carga la lista de cursos en el combo correspondiente.
     */
    private void cargarCursos() {
        cbCurso.removeAllItems();
        List<Curso> cursos = cursoService.listarCursos();
        for (Curso c : cursos) cbCurso.addItem(c);
        if (cbCurso.getItemCount() > 0) cbCurso.setSelectedIndex(0);
        cargarGrupos();
    }

    /**
     * Carga los grupos del curso seleccionado en el combo correspondiente.
     */
    private void cargarGrupos() {
        cbGrupo.removeAllItems();
        Curso c = (Curso) cbCurso.getSelectedItem();
        if (c == null) return;
        for (Grupo g : c.grupos) cbGrupo.addItem(g);
        if (cbGrupo.getItemCount() > 0) cbGrupo.setSelectedIndex(0);
    }

    // -- Control de habilitación --

    /**
     * Actualiza el estado de los combos según el alcance seleccionado.
     */
    private void updateEnable() {
        boolean curso = rbCurso.isSelected() || rbGrupo.isSelected();
        boolean grupo = rbGrupo.isSelected();
        cbCurso.setEnabled(curso);
        cbGrupo.setEnabled(grupo);
    }

    // -- Validación y obtención de datos --

    /**
     * Convierte el texto del campo de fecha en un objeto {@link LocalDate}.
     *
     * @return fecha válida o {@code null} si el formato es incorrecto
     */
    private LocalDate parseFecha() {
        try {
            return LocalDate.parse(txtFecha.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fecha inválida. Use formato YYYY-MM-DD.");
            return null;
        }
    }

    /**
     * Determina el alcance actual seleccionado en la interfaz.
     *
     * @return objeto {@link ReporteService.Scope} que representa el alcance
     */
    private ReporteService.Scope scopeActual() {
        if (rbTodos.isSelected()) return ReporteService.Scope.todos();
        Curso c = (Curso) cbCurso.getSelectedItem();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un curso.");
            return null;
        }
        if (rbCurso.isSelected()) return ReporteService.Scope.curso(c.getId());
        Grupo g = (Grupo) cbGrupo.getSelectedItem();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un grupo.");
            return null;
        }
        return ReporteService.Scope.grupo(c.getId(), g.getIdGrupo());
    }

    // -- Generación de reportes --

    /**
     * Genera un reporte de lista de estudiantes en formato CSV.
     */
    private void generarLista() {
        LocalDate f = parseFecha();
        if (f == null) return;
        ReporteService.Scope s = scopeActual();
        if (s == null) return;
        try {
            Path out = reporteService.generarListaEstudiantes(f, s);
            JOptionPane.showMessageDialog(this, "Lista generada:\n" + out.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera un reporte de estadísticas de matrícula en formato TXT.
     */
    private void generarEstadistica() {
        LocalDate f = parseFecha();
        if (f == null) return;
        ReporteService.Scope s = scopeActual();
        if (s == null) return;
        try {
            Path out = reporteService.generarEstadisticaMatricula(f, s);
            JOptionPane.showMessageDialog(this, "Estadística generada:\n" + out.toAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

