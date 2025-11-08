package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 Gestión de usuarios para el Admin: Estudiantes y Profesores.
 */
public class AdminUsuariosPanel extends JPanel {

    private final UsuarioService servicio;

    private final JTable tblEstudiantes = new JTable();
    private final JTable tblProfesores = new JTable();

    private final EstudiantesModel estudiantesModel = new EstudiantesModel();
    private final ProfesoresModel profesoresModel = new ProfesoresModel();

    public AdminUsuariosPanel(UsuarioService servicio) {
        this.servicio = servicio;
        setLayout(new BorderLayout());

        // tabs por tipo de usuario
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Estudiantes", crearPanelEstudiantes());
        tabs.addTab("Profesores", crearPanelProfesores());
        add(tabs, BorderLayout.CENTER);

        // carga las pestañas
        refrescarTablas();
    }

    private JPanel crearPanelEstudiantes() {
        JPanel p = new JPanel(new BorderLayout());

        // Tabla
        tblEstudiantes.setModel(estudiantesModel);
        JScrollPane sp = new JScrollPane(tblEstudiantes); // Esto es para que se pueda mover haciendo scroll con el mouse
        p.add(sp, BorderLayout.CENTER);

        // Barra de acciones
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo = new JButton("Nuevo");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnReset = new JButton("Restablecer contraseña");
        JButton btnRefrescar = new JButton("Refrescar");

        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnReset);
        actions.add(btnRefrescar);
        p.add(actions, BorderLayout.SOUTH);

        // Funciones
        btnRefrescar.addActionListener(e -> refrescarTablas());

        btnReset.addActionListener(e -> {
            Estudiante sel = getEstudianteSeleccionado();
            if (sel == null) {
                showInfo("Seleccione un estudiante.");
                return;
            }
            boolean ok = servicio.restablecerContrasena(sel.idUsuario);
            showInfo(ok ? "Se envió una contraseña temporal al correo registrado."
                    : "No se pudo restablecer la contraseña.");
        });

        // TODO: cuando me confirmes los campos del formulario, implemento estas 3:
        btnNuevo.addActionListener(e -> showInfo("Pendiente: formulario de nuevo estudiante."));
        btnEditar.addActionListener(e -> showInfo("Pendiente: formulario de edición."));
        btnEliminar.addActionListener(e -> showInfo("Pendiente: confirmación y eliminación."));

        return p;
    }

    private JPanel crearPanelProfesores() {
        JPanel p = new JPanel(new BorderLayout());

        tblProfesores.setModel(profesoresModel);
        JScrollPane sp = new JScrollPane(tblProfesores);
        p.add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo = new JButton("Nuevo");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnReset = new JButton("Restablecer contraseña");
        JButton btnRefrescar = new JButton("Refrescar");

        actions.add(btnNuevo);
        actions.add(btnEditar);
        actions.add(btnEliminar);
        actions.add(btnReset);
        actions.add(btnRefrescar);
        p.add(actions, BorderLayout.SOUTH);

        btnRefrescar.addActionListener(e -> refrescarTablas());

        btnReset.addActionListener(e -> {
            Profesor sel = getProfesorSeleccionado();
            if (sel == null) {
                showInfo("Seleccione un profesor.");
                return;
            }
            boolean ok = servicio.restablecerContrasena(sel.idUsuario);
            showInfo(ok ? "Se envió una contraseña temporal al correo registrado."
                    : "No se pudo restablecer la contraseña.");
        });

        btnNuevo.addActionListener(e -> showInfo("Pendiente: formulario de nuevo profesor."));
        btnEditar.addActionListener(e -> showInfo("Pendiente: formulario de edición."));
        btnEliminar.addActionListener(e -> showInfo("Pendiente: confirmación y eliminación."));

        return p;
    }

    private void refrescarTablas() {
        estudiantesModel.setData(servicio.listarEstudiantes());
        profesoresModel.setData(servicio.listarProfesores());
    }

    private Estudiante getEstudianteSeleccionado() {
        int row = tblEstudiantes.getSelectedRow();
        if (row < 0) return null;
        return estudiantesModel.getAt(row);
    }

    private Profesor getProfesorSeleccionado() {
        int row = tblProfesores.getSelectedRow();
        if (row < 0) return null;
        return profesoresModel.getAt(row);
    }

    // Plantilla para mostrar mensajes al usuario
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    // Modelos de tabla

    private class EstudiantesModel extends AbstractTableModel {
        private final String[] cols = {"Identificación", "Nombre", "Correo"};
        private java.util.List<Estudiante> data = java.util.List.of();

        public void setData(List<Estudiante> lista) {
            this.data = (lista == null ? java.util.List.of() : lista);
            fireTableDataChanged();
        }

        public Estudiante getAt(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Estudiante e = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> e.idUsuario;
                case 1 -> (e.nombre + " " + e.apellido1);
                case 2 -> e.correo;
                default -> "";
            };
        }
    }

    private class ProfesoresModel extends AbstractTableModel {
        private final String[] cols = {"Identificación", "Nombre", "Correo"};
        private java.util.List<Profesor> data = java.util.List.of();

        public void setData(List<Profesor> lista) {
            this.data = (lista == null ? java.util.List.of() : lista);
            fireTableDataChanged();
        }

        public Profesor getAt(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Profesor p = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.idUsuario;
                case 1 -> (p.nombre + " " + p.apellido1);
                case 2 -> p.correo;
                default -> "";
            };
        }
    }
}
