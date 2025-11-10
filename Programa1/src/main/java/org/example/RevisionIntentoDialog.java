package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RevisionIntentoDialog extends JDialog {

    private final IntentoEvaluacion intento;

    public RevisionIntentoDialog(Window owner, IntentoEvaluacion intento) {
        super(owner, "Revisión del intento", ModalityType.APPLICATION_MODAL);
        this.intento = intento;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(owner);

        construirUI();
        pack();
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setContentPane(root);

        // Header
        String alumno = (intento.getEstudiante() == null) ? "-" :
                intento.getEstudiante().getNombre() + " " + intento.getEstudiante().getApellido1();
        String eval = (intento.getEvaluacion() == null) ? "-" : intento.getEvaluacion().getNombre();
        String grupo = (intento.getGrupo() == null) ? "-" : String.valueOf(intento.getGrupo().getIdGrupo());

        JTextArea header = new JTextArea(
                "Estudiante: " + alumno + "\n" +
                        "Evaluación: " + eval + "\n" +
                        "Grupo: " + grupo + "\n" +
                        "Inicio: " + intento.getFechaHoraInicio() + "\n" +
                        "Fin: " + intento.getFechaHoraFinal() + "\n" +
                        "Puntaje: " + intento.getPuntajeObtenido() + " / " +
                        (intento.getEvaluacion() == null ? "-" : intento.getEvaluacion().getPuntajeTotal()) + "\n" +
                        "Calificación: " + String.format("%.2f", intento.getCalificacion())
        );
        header.setEditable(false);
        root.add(header, BorderLayout.NORTH);

        // Detalle de preguntas (simple)
        JTextArea detalle = new JTextArea();
        detalle.setEditable(false);

        List<RespuestaEstudiante> resps = intento.getRespuestasEstudiante();
        if (resps != null && intento.getEvaluacion() != null) {
            List<IPregunta> preguntas = intento.getEvaluacion().getPreguntas();
            for (int i = 0; i < preguntas.size(); i++) {
                IPregunta p = preguntas.get(i);
                RespuestaEstudiante re = (i < resps.size()) ? resps.get(i) : null;

                detalle.append("Pregunta " + (i+1) + ": " + p.obtenerDescripcion() + "\n");
                if (re != null) {
                    detalle.append("  Puntos obtenidos: " + re.getPuntosObtenidos() + "\n");
                    // si usas ordenes seleccionados:
                    List<Integer> ord = re.getOrdenesSeleccionados();
                    if (ord != null) detalle.append("  Selecciones: " + ord + "\n");
                } else {
                    detalle.append("  (Sin respuesta)\n");
                }
                detalle.append("\n");
            }
        } else {
            detalle.setText("(No hay detalle de respuestas para mostrar)");
        }

        root.add(new JScrollPane(detalle), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> dispose());
        south.add(cerrar);
        root.add(south, BorderLayout.SOUTH);
    }
}
