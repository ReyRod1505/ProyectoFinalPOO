package ni.edu.uam.ProyectoFinalPOO.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@Tab(properties =
        "sesion.estudiante.nombres, sesion.estudiante.apellidos, sesion.forma, " +
                "aciertos, totalPreguntas, percentil, categoria, porcentaje, " +
                "observacion, sesion.fechaFin"
)@View(members =
        "sesion;" +
                "marcador [" +
                "   aciertos, totalPreguntas, porcentaje" +
                "];" +
                "observacion"
)
public class Resultado {

    @Id
    @Hidden
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oid;

    @OneToOne(fetch = FetchType.LAZY)
    @Required
    @JoinColumn(name = "SESION_OID")
    @DescriptionsList(descriptionProperties = "estudiante.nombres, estudiante.apellidos")
    @NoCreate
    @NoModify
    private SesionTest sesion;

    @ReadOnly
    private int aciertos;

    @ReadOnly
    private int percentil;

    @Column(length = 40)
    @ReadOnly
    private String categoria;

    @ReadOnly
    private int totalPreguntas;

    @ReadOnly
    private double porcentaje;

    @TextArea
    @ReadOnly
    private String observacion;

    /**
     * Unico punto de calificacion. Lo llama EnviarExamenServlet.
     */
    public void calificar(int aciertos, int totalPreguntas, Forma forma) {
        this.aciertos = Math.max(0, aciertos);
        this.totalPreguntas = totalPreguntas;
        this.percentil = Baremo.percentil(forma, this.aciertos);
        this.categoria = Baremo.categoria(this.percentil);
        // Porcentaje lineal: se CONSERVA solo para estadistica/reportes.
        this.porcentaje = totalPreguntas > 0
                ? Math.round((this.aciertos * 10000.0) / totalPreguntas) / 100.0
                : 0.0;
        this.observacion = "Percentil " + percentil + " (" + categoria + "). "
                + aciertos + " de " + totalPreguntas + " aciertos.";
    }
}