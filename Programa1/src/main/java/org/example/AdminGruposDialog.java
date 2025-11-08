package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Gestión de grupos para un curso: listar, crear, editar fechas, eliminar, asignar profesor. */
public class AdminGruposDialog extends JDialog {

    private final Curso curso;
    private final CursoService cursoService;
    private final UsuarioService usuarioService;

    private final JTable tbl = new JTable();
    private final GruposModel model = new GruposModel();

    private final JSpinner spInicio = new JSpinner(new SpinnerDateModel());
    private final JSpinner spFinal  = new JSpinner(new SpinnerDateModel());

    public AdminGruposDialog(Window owner, Curso curso, CursoService cursoService, UsuarioService usuarioService) {
        super(owner, "Grupos de " + curso.getNombre(), ModalityType.APPLICATION_MODAL);
        this.curso = curso;
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;

        setLayout(new BorderLayout(8,8));

        // Tabla
        tbl.setModel(model);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        // Acciones
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo = new JButton("Nuevo grupo");
        JButton btnEditar = new JButton("Editar fechas");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnAsignar = new JButton("Asignar profesor");
        JButton btnCerrar = new JButton("Cerrar");
        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnAsignar);
        actions.add(btnCerrar);
        add(actions, BorderLayout.SOUTH);

        btnNuevo.addActionListener(_evt -> onNuevo());
        btnEditar.addActionListener(_evt -> onEditar());
        btnEliminar.addActionListener(_evt -> onEliminar());
        btnAsignar.addActionListener(_evt -> onAsignar());
        btnCerrar.addActionListener(_evt -> dispose());

        refrescar();
        setSize(800, 400);
        setLocationRelativeTo(owner);
    }

    private void refrescar() {
        model.setData(cursoService.listarGrupos(curso));
    }

    private Grupo getSel() {
        int r = tbl.getSelectedRow();
        return (r < 0) ? null : model.getAt(r);
    }

    private void onNuevo() {
        // fechas rápidas con input dialog (o podríamos usar date pickers terceros)
        LocalDate inicio = askFecha("Fecha de inicio (YYYY-MM-DD):");
        if (inicio == null) return;
        LocalDate fin = askFecha("Fecha final (YYYY-MM-DD):");
        if (fin == null) return;
        try {
            cursoService.crearGrupo(curso, inicio, fin);
            refrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEditar() {
        Grupo g = getSel();
        if (g == null) { JOptionPane.showMessageDialog(this, "Seleccione un grupo."); return; }
        LocalDate inicio = askFecha("Nueva fecha de inicio (YYYY-MM-DD), actual: " + g.getFechaInicio());
        if (inicio == null) return;
        LocalDate fin = askFecha("Nueva fecha final (YYYY-MM-DD), actual: " + g.getFechaFinal());
        if (fin == null) return;
        try {
            cursoService.actualizarGrupoFechas(curso, g.getIdGrupo(), inicio, fin);
            refrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEliminar() {
        Grupo g = getSel();
        if (g == null) { JOptionPane.showMessageDialog(this, "Seleccione un grupo."); return; }
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar grupo #" + g.getIdGrupo() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                cursoService.eliminarGrupo(curso, g.getIdGrupo());
                refrescar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAsignar() {
        Grupo g = getSel();
        if (g == null) { JOptionPane.showMessageDialog(this, "Seleccione un grupo."); return; }
        List<Profesor> profs = usuarioService.listarProfesores();
        if (profs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay profesores registrados.");
            return;
        }
        Profesor elegido = (Profesor) JOptionPane.showInputDialog(
                this,
                "Seleccione profesor:",
                "Asignar profesor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                profs.toArray(),
                g.getProfesor()
        );
        if (elegido != null) {
            try {
                cursoService.asignarProfesor(curso, g.getIdGrupo(), elegido);
                refrescar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private LocalDate askFecha(String msg) {
        String s = JOptionPane.showInputDialog(this, msg);
        if (s == null) return null;
        try { return LocalDate.parse(s.trim()); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato inválido. Use YYYY-MM-DD.");
            return null;
        }
    }

    // ---- TableModel grupos ----
    private static class GruposModel extends AbstractTableModel {
        private final String[] cols = {"#", "Inicio", "Final", "Profesor"};
        private List<Grupo> data = List.of();
        public void setData(List<Grupo> list) { data = (list==null?List.of():list); fireTableDataChanged(); }
        public Grupo getAt(int row) { return data.get(row); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Grupo g = data.get(r);
            return switch (c) {
                case 0 -> g.getIdGrupo();
                case 1 -> String.valueOf(g.getFechaInicio());
                case 2 -> String.valueOf(g.getFechaFinal());
                case 3 -> g.getProfesor() == null ? "(sin asignar)" :
                        (g.getProfesor().getNombre() + " " + g.getProfesor().getApellido1());
                default -> "";
            };
        }
    }
}
