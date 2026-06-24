package ni.edu.uam.ProyectoFinalPOO.rest;

import java.io.*;
import java.util.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import javax.persistence.*;
import org.openxava.jpa.XPersistence;
import ni.edu.uam.ProyectoFinalPOO.modelo.*;

public class AccesoExamenServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        EntityManager em = XPersistence.getManager();
        List<Estudiante> ests = em.createQuery(
                "from Estudiante e order by e.nombres, e.apellidos", Estudiante.class).getResultList();

        StringBuilder json = new StringBuilder();
        json.append("{\"ok\":true,\"estudiantes\":[");
        for (int i = 0; i < ests.size(); i++) {
            Estudiante e = ests.get(i);
            if (i > 0) json.append(",");
            String nom = ((e.getNombres()==null?"":e.getNombres()) + " " +
                    (e.getApellidos()==null?"":e.getApellidos())).trim();
            json.append("{\"oid\":").append(e.getOid())
                    .append(",\"nombre\":\"").append(esc(nom)).append("\"}");
        }
        json.append("]}");
        out.print(json.toString());
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String accion = req.getParameter("accion");
        if ("iniciar".equals(accion)) {
            iniciar(req, out);   // (2) Arrancar cronometro
        } else {
            acceso(req, out);    // (1) Autenticar y devolver preguntas
        }
    }

    /** Paso 1: valida clave, asocia estudiante, devuelve preguntas. NO arranca el reloj. */
    private void acceso(HttpServletRequest req, PrintWriter out) {
        Integer sesionId = parseInt(req.getParameter("sesion"));
        Integer estudianteId = parseInt(req.getParameter("estudiante"));
        String clave = req.getParameter("clave");

        if (sesionId == null) { out.print("{\"ok\":false,\"error\":\"Sesion no valida\"}"); return; }
        if (estudianteId == null) { out.print("{\"ok\":false,\"error\":\"Selecciona tu nombre\"}"); return; }

        EntityManager em = XPersistence.getManager();
        SesionTest sesion = em.find(SesionTest.class, sesionId);
        if (sesion == null) { out.print("{\"ok\":false,\"error\":\"La sesion no existe\"}"); return; }

        if (sesion.getEstado() == EstadoSesion.CALIFICADA) {
            out.print("{\"ok\":false,\"error\":\"Este examen ya fue rendido\"}"); return;
        }

        Estudiante est = em.find(Estudiante.class, estudianteId);
        if (est == null) { out.print("{\"ok\":false,\"error\":\"Estudiante no encontrado\"}"); return; }

        String hashIngresado = Estudiante.hashSHA256(clave == null ? "" : clave);
        if (est.getPassword() == null || !est.getPassword().equals(hashIngresado)) {
            out.print("{\"ok\":false,\"error\":\"Clave incorrecta\"}"); return;
        }

        int limiteSegundos = (sesion.getForma() == Forma.A) ? 300 : 360;

        // Asociar estudiante, PERO sin marcar inicio todavia
        sesion.setEstudiante(est);

        List<Pregunta> preguntas = em.createQuery(
                        "from Pregunta p where p.forma = :f order by p.numero", Pregunta.class)
                .setParameter("f", sesion.getForma()).getResultList();
        String forma = String.valueOf(sesion.getForma());

        StringBuilder json = new StringBuilder();
        json.append("{\"ok\":true,\"forma\":\"").append(forma).append("\"")
                .append(",\"totalPreguntas\":").append(preguntas.size())
                .append(",\"limiteSegundos\":").append(limiteSegundos)
                .append(",\"preguntas\":[");
        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta p = preguntas.get(i);
            if (i > 0) json.append(",");
            json.append("{\"oid\":").append(p.getOid())
                    .append(",\"numero\":").append(p.getNumero())
                    .append(",\"enunciado\":\"").append(esc(p.getEnunciado())).append("\"}");
        }
        json.append("]}");

        XPersistence.commit();
        out.print(json.toString());
    }

    /** Paso 2: el alumno pulso "Iniciar examen". Aqui SI arranca el reloj. */
    private void iniciar(HttpServletRequest req, PrintWriter out) {
        Integer sesionId = parseInt(req.getParameter("sesion"));
        String clave = req.getParameter("clave");

        if (sesionId == null) { out.print("{\"ok\":false,\"error\":\"Sesion no valida\"}"); return; }

        EntityManager em = XPersistence.getManager();
        SesionTest sesion = em.find(SesionTest.class, sesionId);
        if (sesion == null) { out.print("{\"ok\":false,\"error\":\"La sesion no existe\"}"); return; }

        if (sesion.getEstado() == EstadoSesion.CALIFICADA) {
            out.print("{\"ok\":false,\"error\":\"Este examen ya fue rendido\"}"); return;
        }

        // Re-validar clave contra el estudiante ya asociado (defensa)
        Estudiante est = sesion.getEstudiante();
        if (est == null) { out.print("{\"ok\":false,\"error\":\"La sesion no tiene estudiante\"}"); return; }
        String hashIngresado = Estudiante.hashSHA256(clave == null ? "" : clave);
        if (est.getPassword() == null || !est.getPassword().equals(hashIngresado)) {
            out.print("{\"ok\":false,\"error\":\"Clave incorrecta\"}"); return;
        }

        int limiteSegundos = (sesion.getForma() == Forma.A) ? 300 : 360;

        // Marcar inicio SOLO la primera vez (resiste recargas)
        if (sesion.getFechaInicio() == null) {
            sesion.setFechaInicio(new Date());
            sesion.setEstado(EstadoSesion.INICIADA);
        }

        long transcurrido = (System.currentTimeMillis() - sesion.getFechaInicio().getTime()) / 1000;
        long restante = limiteSegundos - transcurrido;
        if (restante < 0) restante = 0;

        XPersistence.commit();
        out.print("{\"ok\":true,\"segundosRestantes\":" + restante + "}");
    }

    private Integer parseInt(String s) {
        if (s == null || s.equals("null") || s.trim().isEmpty()) return null;
        try { return Integer.valueOf(s.trim()); } catch (Exception e) { return null; }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ").replace("\r"," ");
    }
}