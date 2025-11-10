package org.example;

import java.nio.file.Path;
import java.time.LocalDate;

/** Contrato para generar reportes del Administrador. */
public interface ReporteService {

    enum ScopeType { TODOS, CURSO, GRUPO }

    /** Alcance del reporte: todos, por curso, o por un grupo de un curso. */
    final class Scope {
        public final ScopeType tipo;
        public final String idCurso; // requerido si tipo=CURSO o GRUPO
        public final Integer idGrupo; // requerido si tipo=GRUPO

        private Scope(ScopeType tipo, String idCurso, Integer idGrupo) {
            this.tipo = tipo;
            this.idCurso = idCurso;
            this.idGrupo = idGrupo;
        }
        public static Scope todos() { return new Scope(ScopeType.TODOS, null, null); }
        public static Scope curso(String idCurso) { return new Scope(ScopeType.CURSO, idCurso, null); }
        public static Scope grupo(String idCurso, int idGrupo) { return new Scope(ScopeType.GRUPO, idCurso, idGrupo); }
    }

    /** Genera lista alfabética de estudiantes (TXT/CSV) según vigencia y alcance; retorna ruta del archivo. */
    Path generarListaEstudiantes(LocalDate fechaVigencia, Scope scope) throws Exception;

    /** Genera estadística de matrícula (TXT/CSV) según vigencia y alcance; retorna ruta del archivo. */
    Path generarEstadisticaMatricula(LocalDate fechaVigencia, Scope scope) throws Exception;

    boolean exportarIntento(IntentoEvaluacion intento, java.io.File destino);

}
