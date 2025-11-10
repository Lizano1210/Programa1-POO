package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Panel para revisar intentos de evaluaciones:
 * - Selecciona el grupo del profesor
 * - Muestra las asignaciones del grupo (EvaluacionAsignada)
 * - Lista intentos (según proveedor externo)
 * - Exporta PDF del intento (acción externa)
 *
 * NOTA: No asumimos dónde viven los intentos. Recibimos:
 *   proveedorIntentos: Grupo -> List<IntentoEvaluacion>
 *   exportadorPdf: (IntentoEvaluacion, File destino) -> boolean
 */
public class ProfesorIntentosPanel extends JPanel {

    private final UsuarioServiceMem usuarioService;
    private final CursoService cursoService;
    private final Profesor profesor;

    // Proveedores inyectados desde ProfesorDashboardPanel:
    private final Function<Grupo, List<IntentoEvaluacion>> proveedorIntentos;
    private final BiFunction<IntentoEvaluacion, File, Boolean> exportadorPdf;

    // UI
    private final JComboBox<Grupo> cmbGrupo = new JComboBox<>();
    private final JTable tabla = new JTable();
    private final IntentosModel model = new IntentosModel();

    private final JButton btnRefrescar = new JButton("Refrescar");
    private final JButton btnVer = new JButton("Ver intento");
    private final JButton btnExportar = new JButton("Exportar PDF");

    public ProfesorIntentosPanel(UsuarioServiceMem usuarioService,
                                 CursoService cursoService,
                                 Profesor profesor,
                                 Function<Grupo, List<IntentoEvaluacion>> proveedorIntentos,
                                 BiFunction<IntentoEvaluacion, File, Boolean> exportadorPdf) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.profesor = profesor;
        this.proveedorIntentos = proveedorIntentos;
        this.exportadorPdf = exportadorPdf;

        setLayout(new BorderLayout(8,8));

        // Top: selector de grupo + acciones
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        north.add(new JLabel("Grupo:"));
        cargarGruposProfesor();
        north.add(cmbGrupo);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.add(btnRefrescar);
        actions.add(btnVer);
        actions.add(btnExportar);

        JPanel top = new JPanel(new BorderLayout());
        top.add(north, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Centro: tabla
        tabla.setModel(model);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Eventos
        btnRefrescar.addActionListener(e -> cargarIntentos());
        btnVer.addActionListener(e -> onVerIntento());
        btnExportar.addActionListener(e -> onExportarPdf());

        // Al cambiar de grupo, refrescar
        cmbGrupo.addActionListener(e -> cargarIntentos());

        // Carga inicial
        if (cmbGrupo.getItemCount() > 0) cmbGrupo.setSelectedIndex(0);
        cargarIntentos();
    }

    private void cargarGruposProfesor() {
        cmbGrupo.removeAllItems();
        List<Grupo> grupos = profesor.getGrupos(); // asumiendo que Profesor mantiene sus grupos
        if (grupos != null) {
            for (Grupo g : grupos) cmbGrupo.addItem(g);
        }
    }

    private void cargarIntentos() {
        Grupo g = (Grupo) cmbGrupo.getSelectedItem();
        List<IntentoEvaluacion> data = (g == null) ? new ArrayList<>() : proveedorIntentos.apply(g);
        model.setData(data);
    }

    private IntentoEvaluacion seleccionado() {
        int r = tabla.getSelectedRow();
        return (r < 0) ? null : model.getAt(r);
    }

    // === Acciones ===
    private void onVerIntento() {
        IntentoEvaluacion it = seleccionado();
        if (it == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un intento.");
            return;
        }
        // Abre un diálogo de revisión en modo solo lectura
        RevisionIntentoDialog dlg = new RevisionIntentoDialog(
                SwingUtilities.getWindowAncestor(this), it
        );
        dlg.setVisible(true);
    }

    private void onExportarPdf() {
        IntentoEvaluacion it = seleccionado();
        if (it == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un intento.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar intento como PDF");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destino = fc.getSelectedFile();
            boolean ok = false;
            try {
                ok = exportadorPdf.apply(it, destino);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this,
                    ok ? "PDF generado correctamente." : "No se pudo generar el PDF.");
        }
    }

    // tabla
    static class IntentosModel extends AbstractTableModel {
        private final String[] cols = {"Estudiante", "Evaluación", "Grupo", "Inicio", "Fin", "Puntaje", "Calificación"};
        private List<IntentoEvaluacion> data = new ArrayList<>();
        public void setData(List<IntentoEvaluacion> list) { data = (list == null) ? new ArrayList<>() : list; fireTableDataChanged(); }
        public IntentoEvaluacion getAt(int r) { return data.get(r); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            IntentoEvaluacion it = data.get(r);
            return switch (c) {
                case 0 -> (it.getEstudiante() == null) ? "-" : it.getEstudiante().getNombre();
                case 1 -> (it.getEvaluacion() == null) ? "-" : it.getEvaluacion().getNombre();
                case 2 -> (it.getGrupo() == null) ? "-" : it.getGrupo().getIdGrupo();
                case 3 -> it.getFechaHoraInicio();
                case 4 -> it.getFechaHoraFinal();
                case 5 -> it.getPuntajeObtenido();
                case 6 -> String.format("%.2f", it.getCalificacion());
                default -> "";
            };
        }
    }
}
