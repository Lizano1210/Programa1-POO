package org.example;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel de Reportes del Administrador.
 * - Fecha de vigencia (YYYY-MM-DD)
 * - Alcance: Todos / Curso / Grupo (con combos dependientes)
 * - Botones: Generar Lista (CSV) y Generar Estadística (TXT)
 */
public class AdminReportesPanel extends JPanel {

    private final CursoService cursoService;
    private final ReporteService reporteService;

    private final JTextField txtFecha = new JTextField(10); // YYYY-MM-DD
    private final JRadioButton rbTodos = new JRadioButton("Todos", true);
    private final JRadioButton rbCurso = new JRadioButton("Curso");
    private final JRadioButton rbGrupo = new JRadioButton("Grupo");
    private final JComboBox<Curso> cbCurso = new JComboBox<>();
    private final JComboBox<Grupo> cbGrupo = new JComboBox<>();

    public AdminReportesPanel(CursoService cursoService, ReporteService reporteService) {
        this.cursoService = cursoService;
        this.reporteService = reporteService;

        setLayout(new BorderLayout(8,8));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbTodos); bg.add(rbCurso); bg.add(rbGrupo);

        int row = 0;
        addRow(form, c, row++, new JLabel("Fecha vigencia (YYYY-MM-DD):"), txtFecha);
        addRow(form, c, row++, new JLabel("Alcance:"), paneAlcance());

        add(form, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLista = new JButton("Generar Lista (CSV)");
        JButton btnEstad = new JButton("Generar Estadística (TXT)");
        actions.add(btnLista);
        actions.add(btnEstad);

        add(actions, BorderLayout.SOUTH);

        // Eventos alcance
        rbTodos.addActionListener(_evt -> updateEnable());
        rbCurso.addActionListener(_evt -> updateEnable());
        rbGrupo.addActionListener(_evt -> updateEnable());
        cbCurso.addActionListener(_evt -> cargarGrupos());

        // Carga inicial combos
        cargarCursos();
        updateEnable();

        // Acciones
        btnLista.addActionListener(_evt -> generarLista());
        btnEstad.addActionListener(_evt -> generarEstadistica());
    }

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

    private void addRow(JPanel panel, GridBagConstraints c, int row, JComponent left, JComponent right) {
        c.gridx=0; c.gridy=row; c.weightx=0; panel.add(left, c);
        c.gridx=1; c.gridy=row; c.weightx=1; panel.add(right, c);
    }

    private void cargarCursos() {
        cbCurso.removeAllItems();
        List<Curso> cursos = cursoService.listarCursos();
        for (Curso c : cursos) cbCurso.addItem(c);
        if (cbCurso.getItemCount() > 0) cbCurso.setSelectedIndex(0);
        cargarGrupos();
    }

    private void cargarGrupos() {
        cbGrupo.removeAllItems();
        Curso c = (Curso) cbCurso.getSelectedItem();
        if (c == null) return;
        for (Grupo g : c.grupos) cbGrupo.addItem(g);
        if (cbGrupo.getItemCount() > 0) cbGrupo.setSelectedIndex(0);
    }

    private void updateEnable() {
        boolean curso = rbCurso.isSelected() || rbGrupo.isSelected();
        boolean grupo = rbGrupo.isSelected();
        cbCurso.setEnabled(curso);
        cbGrupo.setEnabled(grupo);
    }

    private LocalDate parseFecha() {
        try { return LocalDate.parse(txtFecha.getText().trim()); }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fecha inválida. Use formato YYYY-MM-DD.");
            return null;
        }
    }

    private ReporteService.Scope scopeActual() {
        if (rbTodos.isSelected()) return ReporteService.Scope.todos();
        Curso c = (Curso) cbCurso.getSelectedItem();
        if (c == null) { JOptionPane.showMessageDialog(this, "Seleccione un curso."); return null; }
        if (rbCurso.isSelected()) return ReporteService.Scope.curso(c.getId());
        Grupo g = (Grupo) cbGrupo.getSelectedItem();
        if (g == null) { JOptionPane.showMessageDialog(this, "Seleccione un grupo."); return null; }
        return ReporteService.Scope.grupo(c.getId(), g.getIdGrupo());
    }

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
