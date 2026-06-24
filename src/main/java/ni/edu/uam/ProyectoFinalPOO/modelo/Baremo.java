package ni.edu.uam.ProyectoFinalPOO.modelo;

/**
 * Baremo de conversion aciertos -> percentil para los tests de vocabulario.
 *
 * ADAPTADO a examenes de 35 preguntas por forma. La tabla original estaba
 * estandarizada para 24 items; aqui los rangos fueron re-escalados
 * proporcionalmente (factor 35/24) para cubrir 0-35 aciertos.
 * NOTA: es una adaptacion proporcional, no la norma original validada.
 *
 * Regla de tope: si el alumno acierta mas que el rango superior, obtiene el
 * percentil maximo (99). El rango superior usa Integer.MAX_VALUE como techo.
 *
 * Compatible con Java 8+ (sin records ni List.of).
 * Sin tildes/ñ por el encoding ISO-8859-1 del proyecto.
 */
public final class Baremo {

    /** Un tramo del baremo: [min, max] aciertos -> percentil. */
    private static final class Rango {
        final int min;
        final int max;
        final int percentil;

        Rango(int min, int max, int percentil) {
            this.min = min;
            this.max = max;
            this.percentil = percentil;
        }

        boolean contiene(int aciertos) {
            return aciertos >= min && aciertos <= max;
        }
    }

    private static final int PERCENTIL_MINIMO = 1;

    // Forma A re-escalada a 35 preguntas. Rango superior abierto.
    private static final Rango[] FORMA_A = {
            new Rango(26, Integer.MAX_VALUE, 99),
            new Rango(23, 25, 97),
            new Rango(22, 22, 95),
            new Rango(20, 21, 90),
            new Rango(19, 19, 85),
            new Rango(17, 18, 80),
            new Rango(16, 16, 75),
            new Rango(15, 15, 70),
            new Rango(13, 14, 60),
            new Rango(12, 12, 50),
            new Rango(10, 11, 45),
            new Rango(9, 9, 30),
            new Rango(7, 8, 25),
            new Rango(6, 6, 10),
            new Rango(3, 5, 5),
            new Rango(0, 2, 1)
    };

    // Forma B re-escalada a 35 preguntas. Rango superior abierto.
    private static final Rango[] FORMA_B = {
            new Rango(31, Integer.MAX_VALUE, 99),
            new Rango(29, 30, 97),
            new Rango(26, 28, 95),
            new Rango(25, 25, 90),
            new Rango(23, 24, 85),
            new Rango(22, 22, 80),
            new Rango(20, 21, 75),
            new Rango(19, 19, 65),
            new Rango(17, 18, 55),
            new Rango(16, 16, 50),
            new Rango(15, 15, 40),
            new Rango(13, 14, 30),
            new Rango(11, 12, 25),
            new Rango(10, 10, 20),
            new Rango(9, 9, 15),
            new Rango(7, 8, 10),
            new Rango(4, 6, 5),
            new Rango(0, 3, 1)
    };

    private Baremo() { } // utilidad, no instanciable

    /**
     * Convierte aciertos -> percentil segun la forma.
     * Aciertos negativos se tratan como 0; por encima del baremo -> percentil maximo.
     */
    public static int percentil(Forma forma, int aciertos) {
        if (forma == null) {
            throw new IllegalArgumentException("La forma no puede ser nula");
        }
        Rango[] tabla = tablaDe(forma);
        int valor = Math.max(0, aciertos);
        for (Rango rango : tabla) {
            if (rango.contiene(valor)) {
                return rango.percentil;
            }
        }
        return PERCENTIL_MINIMO; // inalcanzable; defensa por si se edita la tabla
    }

    private static Rango[] tablaDe(Forma forma) {
        switch (forma) {
            case A: return FORMA_A;
            case B: return FORMA_B;
            default:
                throw new IllegalArgumentException("Forma no soportada: " + forma);
        }
    }

    /** Categoria cualitativa derivada del percentil (para observacion/reportes). */
    public static String categoria(int percentil) {
        if (percentil >= 95) return "Muy superior";
        if (percentil >= 75) return "Superior";
        if (percentil >= 50) return "Medio";
        if (percentil >= 25) return "Medio bajo";
        return "Bajo";
    }
}