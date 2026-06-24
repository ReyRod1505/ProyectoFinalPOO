package ni.edu.uam.ProyectoFinalPOO.modelo;

import java.time.*;
import javax.persistence.*;
import javax.validation.constraints.*;   // <-- nuevo import (validacion)
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Getter @Setter
@View(members=
        "datos { nombres; apellidos; fechaNacimiento; edad; sexo }" +
                "formacion { estudiosRealizados; profesion }" +
                "contacto { email }" +
                "acceso { password }"
)
@Tab(properties="nombres, apellidos, fechaNacimiento, sexo, profesion, email")
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

    @Enumerated(EnumType.STRING)
    @Column(length=20)
    private Sexo sexo;

    @Column(length=100)
    private String estudiosRealizados;

    @Column(length=60)
    private String profesion;

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
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        return edad < 0 ? 0 : edad;   // nunca muestra negativo
    }

    // Validacion: bloquea el guardado si la fecha es imposible
    @AssertTrue(message="La fecha de nacimiento no es valida: debe ser pasada y dar una edad entre 5 y 100 anios.")
    @Hidden
    public boolean isFechaNacimientoValida() {
        if (fechaNacimiento == null) return true;   // del null se encarga @Required
        LocalDate hoy = LocalDate.now();
        if (fechaNacimiento.isAfter(hoy)) return false;   // fecha futura
        int edad = Period.between(fechaNacimiento, hoy).getYears();
        return edad >= 5 && edad <= 100;
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