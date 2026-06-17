package ni.edu.uam.ProyectoFinalPOO.modelo;   // <-- CONSERVA TU PAQUETE

import javax.persistence.*;
import org.openxava.annotations.*;
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

    @Required
    private int numero;

    @Enumerated(EnumType.STRING) @Required
    private Forma forma;

    @Column(length = 500) @Required @TextArea
    private String enunciado;

    @Column(length = 1) @Required
    private String respuestaCorrecta;

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