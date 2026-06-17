package ni.edu.uam.ProyectoFinalPOO.modelo;

import java.time.*;
import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@Tab(properties = "nombres, apellidos, fechaNacimiento, carrera")
@View(members =
        "datos [" +
                "   nombres, apellidos;" +
                "   fechaNacimiento, edad" +
                "];" +
                "contacto [" +
                "   email, carrera" +
                "]"
)
public class Estudiante {

    @Id @Hidden
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oid;

    @Column(length = 60) @Required
    private String nombres;

    @Column(length = 60) @Required
    private String apellidos;

    @Required
    private LocalDate fechaNacimiento;

    @Column(length = 80)
    @Stereotype("EMAIL")          // valida formato y muestra enlace mailto
    private String email;

    @Column(length = 40)
    private String carrera;

    // Propiedad calculada (no se persiste). Solo en @View, nunca en @Tab.
    @Depends("fechaNacimiento")
    @Transient
    public int getEdad() {
        if (fechaNacimiento == null) return 0;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}