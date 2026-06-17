package ni.edu.uam.ProyectoFinalPOO.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@Tab(properties = "sesion.estudiante.nombres, sesion.estudiante.apellidos, aciertos, totalPreguntas, porcentaje")
@View(members =
        "sesion;" +
                "marcador [" +
                "   aciertos, totalPreguntas, porcentaje" +
                "];" +
                "observacion"
)
public class Resultado {

    @Id @Hidden
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oid;

    @OneToOne(fetch = FetchType.LAZY) @Required
    @JoinColumn(name = "SESION_OID")
    @DescriptionsList(descriptionProperties = "estudiante.nombres, estudiante.apellidos")
    @NoCreate @NoModify
    private SesionTest sesion;

    @ReadOnly
    private int aciertos;

    @ReadOnly
    private int totalPreguntas;

    @ReadOnly
    private double porcentaje;

    @TextArea @ReadOnly
    private String observacion;

    public void calcularPorcentaje() {
        this.porcentaje = (totalPreguntas > 0)
                ? Math.round((aciertos * 100.0 / totalPreguntas) * 100.0) / 100.0   // 2 decimales
                : 0.0;
    }

    public void generarObservacion() {
        if (porcentaje >= 90)      observacion = "Excelente";
        else if (porcentaje >= 70) observacion = "Satisfactorio";
        else if (porcentaje >= 60) observacion = "Aceptable";
        else                       observacion = "Necesita refuerzo";
    }
}