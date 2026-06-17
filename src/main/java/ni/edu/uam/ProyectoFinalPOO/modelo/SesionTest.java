package ni.edu.uam.ProyectoFinalPOO.modelo;   // <-- CONSERVA TU PAQUETE

import java.util.*;
import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@Tab(properties = "estudiante.nombres, estudiante.apellidos, forma, estado, fechaInicio")
@View(members =
        "general [" +
                "   estudiante, forma;" +
                "   estado, duracionMinutos" +
                "];" +
                "tiempos [" +
                "   fechaInicio, fechaFin" +
                "];" +
                "respuestas;" +
                "resultado"
)
public class SesionTest {

    @Id @Hidden
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oid;

    @ManyToOne(fetch = FetchType.LAZY) @Required
    @DescriptionsList(descriptionProperties = "nombres, apellidos")
    @NoCreate @NoModify
    private Estudiante estudiante;

    @Enumerated(EnumType.STRING) @Required
    private Forma forma;

    @Enumerated(EnumType.STRING) @ReadOnly
    private EstadoSesion estado = EstadoSesion.PENDIENTE;

    @ReadOnly @Stereotype("DATETIME")        // <-- Date en vez de LocalDateTime
    private Date fechaInicio;

    @ReadOnly @Stereotype("DATETIME")        // <-- Date en vez de LocalDateTime
    private Date fechaFin;

    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    @ListProperties("pregunta.numero, pregunta.enunciado, valorMarcado, correcta")
    private Collection<Respuesta> respuestas;

    @OneToOne(mappedBy = "sesion", cascade = CascadeType.REMOVE)
    @ReadOnly
    private Resultado resultado;

    // ---------- Logica de negocio ----------

    public void iniciar() {
        this.fechaInicio = new Date();
        this.estado = EstadoSesion.INICIADA;
    }

    public void finalizar() {
        this.fechaFin = new Date();
        this.estado = EstadoSesion.FINALIZADA;
    }

    public void calificar() {
        this.estado = EstadoSesion.CALIFICADA;
    }

    public boolean estaIniciada()   { return estado == EstadoSesion.INICIADA; }
    public boolean estaFinalizada() { return estado == EstadoSesion.FINALIZADA; }
    public boolean estaCalificada() { return estado == EstadoSesion.CALIFICADA; }

    @Depends("fechaInicio, fechaFin")
    @Transient
    public long getDuracionMinutos() {
        if (fechaInicio == null || fechaFin == null) return 0;
        return (fechaFin.getTime() - fechaInicio.getTime()) / 60000L;   // ms -> minutos
    }
}