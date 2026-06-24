package ni.edu.uam.ProyectoFinalPOO.modelo;

import javax.persistence.*;
import org.openxava.annotations.*;
import org.openxava.jpa.*;
import lombok.*;

@Entity
@Getter @Setter
@Tab(properties = "numero, forma, enunciado, respuestaCorrecta")
@View(members =
        "numero, forma;" +
                "enunciado;" +
                "respuestaCorrecta"
)
public class Pregunta {

    @Id @Hidden
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oid;

    // El numero lo asigna el programa por forma (ver alCrear). El admin no lo escribe.
    @ReadOnly
    private int numero;

    @Enumerated(EnumType.STRING) @Required
    private Forma forma;

    @Column(length = 500) @Required @TextArea
    private String enunciado;

    @Column(length = 1) @Required
    private String respuestaCorrecta;

    /**
     * Antes de guardar una pregunta nueva, le asigna el siguiente numero
     * correlativo DENTRO de su forma (A y B numeran por separado).
     * Solo actua si el numero aun no fue asignado (== 0).
     */
    @PrePersist
    private void alCrear() {
        if (numero == 0 && forma != null) {
            Integer max = XPersistence.getManager()
                    .createQuery(
                            "select max(p.numero) from Pregunta p where p.forma = :f",
                            Integer.class)
                    .setParameter("f", forma)
                    .getSingleResult();
            this.numero = (max == null ? 0 : max) + 1;
        }
    }

    // Normaliza a mayuscula sin espacios. Lombok respeta este setter manual.
    public void setRespuestaCorrecta(String respuestaCorrecta) {
        this.respuestaCorrecta = (respuestaCorrecta == null)
                ? null : respuestaCorrecta.trim().toUpperCase();
    }

    public boolean esDeForma(Forma f) {
        return this.forma == f;
    }

    public boolean verificarRespuesta(String valor) {
        return respuestaCorrecta != null
                && respuestaCorrecta.equalsIgnoreCase(valor == null ? null : valor.trim());
    }
}