package ni.edu.uam.ProyectoFinalPOO.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@Tab(properties = "pregunta.numero, pregunta.enunciado, valorMarcado, correcta")
@View(members = "pregunta; valorMarcado, correcta")   // 'sesion' se oculta aqui (forma correcta)
public class Respuesta {

    @Id @Hidden
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oid;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SESION_OID")
    private SesionTest sesion;

    @ManyToOne(fetch = FetchType.LAZY) @Required
    @DescriptionsList(descriptionProperties = "numero, enunciado, forma")
    @NoCreate @NoModify
    private Pregunta pregunta;

    @Column(length = 1)
    private String valorMarcado;

    @ReadOnly
    private boolean correcta;

    public void setValorMarcado(String valorMarcado) {
        this.valorMarcado = (valorMarcado == null)
                ? null : valorMarcado.trim().toUpperCase();
    }

    public void evaluar() {
        this.correcta = pregunta != null
                && pregunta.verificarRespuesta(valorMarcado);
    }
}