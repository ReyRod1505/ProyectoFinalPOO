package ni.edu.uam.ProyectoFinalPOO.modelo;

import java.time.*;
import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@View(members=
        "datos { nombres; apellidos; fechaNacimiento; edad }" +
                "contacto { carrera; email }" +
                "acceso { password }"
)
@Tab(properties="nombres, apellidos, fechaNacimiento, carrera, email")
public class Estudiante {

    @Id @Hidden @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer oid;

    @Column(length=60)
    @Required
    private String nombres;

    @Column(length=60)
    @Required
    private String apellidos;

    @Required
    private LocalDate fechaNacimiento;

    @Column(length=40)
    private String carrera;

    @Column(length=80)
    @Stereotype("EMAIL")
    private String email;

    @Column(length=100)
    @Required
    @Stereotype("PASSWORD")
    private String password;

    @Depends("fechaNacimiento")
    public int getEdad() {
        if (fechaNacimiento == null) return 0;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    @PrePersist @PreUpdate
    private void encriptarPassword() {
        if (password != null && !password.isEmpty() && !esHash(password)) {
            password = hashSHA256(password);
        }
    }

    private boolean esHash(String s) {
        return s != null && s.length() == 64 && s.matches("[0-9a-f]+");
    }

    public static String hashSHA256(String texto) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}