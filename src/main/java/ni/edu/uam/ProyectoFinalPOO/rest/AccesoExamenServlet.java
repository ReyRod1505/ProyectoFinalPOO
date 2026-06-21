package ni.edu.uam.ProyectoFinalPOO.rest;   // <-- AJUSTA al paquete donde lo tengas (web o rest)

import java.io.*;
import java.util.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import javax.persistence.*;
import org.openxava.jpa.XPersistence;
import ni.edu.uam.ProyectoFinalPOO.modelo.*;

public class AccesoExamenServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String pSesion = req.getParameter("sesion");
        String clave = req.getParameter("clave");
        Integer sesionId = null;
        if (pSesion != null && !pSesion.equals("null") && !pSesion.trim().isEmpty()) {
            try { sesionId = Integer.valueOf(pSesion.trim()); } catch (Exception e) {}
        }

        if (sesionId == null) { out.print("{\"ok\":false,\"error\":\"Sesion no valida\"}"); return; }
        EntityManager em = XPersistence.getManager();
        SesionTest sesion = em.find(SesionTest.class, sesionId);
        if (sesion == null) { out.print("{\"ok\":false,\"error\":\"La sesion no existe\"}"); return; }

        Estudiante est = sesion.getEstudiante();
        if (est == null) { out.print("{\"ok\":false,\"error\":\"La sesion no tiene estudiante\"}"); return; }
        String hashIngresado = Estudiante.hashSHA256(clave == null ? "" : clave);
        if (est.getPassword() == null || !est.getPassword().equals(hashIngresado)) {
            out.print("{\"ok\":false,\"error\":\"Clave incorrecta\"}"); return;
        }

        List<Pregunta> preguntas = em.createQuery(
                        "from Pregunta p where p.forma = :f order by p.numero", Pregunta.class)
                .setParameter("f", sesion.getForma()).getResultList();
        StringBuilder json = new StringBuilder();
        json.append("{\"ok\":true,\"forma\":\"").append(sesion.getForma()).append("\",\"preguntas\":[");
        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta p = preguntas.get(i);
            if (i > 0) json.append(",");
            json.append("{\"oid\":").append(p.getOid())
                    .append(",\"numero\":").append(p.getNumero())
                    .append(",\"enunciado\":\"").append(esc(p.getEnunciado())).append("\"}");
        }
        json.append("]}");
        out.print(json.toString());
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ").replace("\r"," ");
    }
}