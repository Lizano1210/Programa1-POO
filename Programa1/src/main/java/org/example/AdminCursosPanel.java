package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/** Panel de gestión de Cursos. */
public class AdminCursosPanel extends JPanel {

    private final CursoService cursoService;
    private final UsuarioService usuarioService; // para elegir profesor en grupos

    private final JTable tblCursos = new JTable();
    private final CursosModel cursosModel = new CursosModel();

    public AdminCursosPanel(CursoService cursoService, UsuarioService usuarioService) {
        this.cursoService = cursoService;
        this.usuarioService = usuarioService;

        setLayout(new BorderLayout());

        // Tabla
        tblCursos.setModel(cursosModel);
        add(new JScrollPane(tblCursos), BorderLayout.CENTER);

        // Acciones
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo = new JButton("Nuevo curso");
        JButton btnEditar = new JButton("Editar curso");
        JButton btnEliminar = new JButton("Eliminar curso");
        JButton btnGrupos = new JButton("Gestionar grupos");
        JButton btnRefrescar = new JButton("Refrescar");
        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnGrupos);
        actions.add(btnRefrescar);
        add(actions, BorderLayout.SOUTH);

        // Listeners, el _evt es como llamar al método sin enviar parametros
        btnRefrescar.addActionListener(_evt -> refrescar());
        btnNuevo.addActionListener(_evt -> onNuevo());
        btnEditar.addActionListener(_evt -> onEditar());
        btnEliminar.addActionListener(_evt -> onEliminar());
        btnGrupos.addActionListener(_evt -> onGrupos());

        refrescar();
    }

    private void refrescar() {
        cursosModel.setData(cursoService.listarCursos());
    }

    private Curso getSeleccionado() {
        int r = tblCursos.getSelectedRow();
        return (r < 0) ? null : cursosModel.getAt(r);
    }

    private void onNuevo() {
        AdminCursoDialog dlg = new AdminCursoDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        Curso nuevo = dlg.getResultado();
        if (nuevo != null) {
            try {
                cursoService.agregarCurso(nuevo);
                refrescar();
                JOptionPane.showMessageDialog(this, "Curso agregado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEditar() {
        Curso sel = getSeleccionado();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Seleccione un curso."); return; }
        AdminCursoDialog dlg = new AdminCursoDialog(SwingUtilities.getWindowAncestor(this), sel);
        dlg.setVisible(true);
        Curso editado = dlg.getResultado();
        if (editado != null) {
            try {
                cursoService.actualizarCurso(editado);
                refrescar();
                JOptionPane.showMessageDialog(this, "Curso actualizado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEliminar() {
        Curso sel = getSeleccionado();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Seleccione un curso."); return; }
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar el curso " + sel.getNombre() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                cursoService.eliminarCurso(sel.getId());
                refrescar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onGrupos() {
        Curso sel = getSeleccionado();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Seleccione un curso."); return; }
        AdminGruposDialog dlg = new AdminGruposDialog(SwingUtilities.getWindowAncestor(this), sel, cursoService, usuarioService);
        dlg.setVisible(true);
        // Al cerrar, refrescamos por si cambió algo del curso (grupos)
        refrescar();
    }

    // Table model
    private static class CursosModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Nombre", "Modalidad", "Tipo", "Min", "Max", "Horas/día", "Aprobación"};
        private List<Curso> data = List.of();
        public void setData(List<Curso> list) { data = (list==null?List.of():list); fireTableDataChanged(); }
        public Curso getAt(int row) { return data.get(row); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Curso x = data.get(r);
            return switch (c) {
                case 0 -> x.getId();
                case 1 -> x.getNombre();
                case 2 -> String.valueOf(x.getModalidad());
                case 3 -> String.valueOf(x.getTipo());
                case 4 -> x.getMinEstu();   // getters usados por Grupo y Admin. cite
                case 5 -> x.getMaxEstu();   // idem
                case 6 -> x.getHrsDia();
                case 7 -> x.getAprobCalificacion();
                default -> "";
            };
        }
    }
}
