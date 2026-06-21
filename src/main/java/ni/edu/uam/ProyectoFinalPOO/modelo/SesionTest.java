package ni.edu.uam.ProyectoFinalPOO.modelo;

import java.util.*;
import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@View(members=
        "datos { estudiante; forma; estado }" +
                "tiempos { fechaInicio; fechaFin; duracionMinutos }" +
                "resultado"
)
@Tab(properties="estudiante.nombres, estudiante.apellidos, forma, estado, fechaInicio")
public class SesionTest {

    @Id @Hidden @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer oid;

    @ManyToOne
    @Required
    @DescriptionsList(descriptionProperties="nombres, apellidos")
    @NoCreate @NoModify
    private Estudiante estudiante;

    @Enumerated(EnumType.STRING)
    @Required
    private Forma forma;

    @Enumerated(EnumType.STRING)
    @ReadOnly
    private EstadoSesion estado;

    @Stereotype("DATETIME")
    @ReadOnly
    private Date fechaInicio;

    @Stereotype("DATETIME")
    @ReadOnly
    private Date fechaFin;

    @OneToMany(mappedBy="sesion", cascade=CascadeType.ALL, orphanRemoval=true)
    private Collection<Respuesta> respuestas;

    @OneToOne(mappedBy="sesion")
    private Resultado resultado;

    @PrePersist
    private void alCrear() {
        if (estado == null) estado = EstadoSesion.PENDIENTE;
        if (fechaInicio == null) fechaInicio = new Date();
    }

    public EstadoSesion getEstado() {
        return estado == null ? EstadoSesion.PENDIENTE : estado;
    }

    @Depends("fechaInicio, fechaFin")
    public long getDuracionMinutos() {
        if (fechaInicio == null || fechaFin == null) return 0;
        return (fechaFin.getTime() - fechaInicio.getTime()) / 60000;
    }
}