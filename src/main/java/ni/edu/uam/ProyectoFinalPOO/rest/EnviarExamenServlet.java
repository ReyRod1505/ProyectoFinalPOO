package ni.edu.uam.ProyectoFinalPOO.rest;

import java.io.*;
import java.util.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import javax.persistence.*;
import org.openxava.jpa.XPersistence;
import ni.edu.uam.ProyectoFinalPOO.modelo.*;

public class EnviarExamenServlet extends HttpServlet {

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

        // Defensa: no re-calificar una sesion ya enviada
        if (sesion.getEstado() == EstadoSesion.CALIFICADA) {
            out.print("{\"ok\":false,\"error\":\"Este examen ya fue enviado\"}"); return;
        }

        List<Pregunta> preguntas = em.createQuery(
                        "from Pregunta p where p.forma = :f order by p.numero", Pregunta.class)
                .setParameter("f", sesion.getForma()).getResultList();
        int aciertos = 0;
        for (Pregunta p : preguntas) {
            String marca = req.getParameter("p_" + p.getOid());
            if (marca == null) marca = "";
            boolean ok = p.getRespuestaCorrecta() != null
                    && p.getRespuestaCorrecta().equalsIgnoreCase(marca);
            if (ok) aciertos++;
            Respuesta r = new Respuesta();
            r.setSesion(sesion);
            r.setPregunta(p);
            r.setValorMarcado(marca);
            r.setCorrecta(ok);
            em.persist(r);
        }
        int total = preguntas.size();

        // --- Calificacion por baremo: un solo punto de verdad ---
        Resultado res = new Resultado();
        res.setSesion(sesion);
        res.calificar(aciertos, total, sesion.getForma());

        sesion.setEstado(EstadoSesion.CALIFICADA);
        sesion.setFechaFin(new Date());
        em.persist(res);
        XPersistence.commit();

        out.print("{\"ok\":true"
                + ",\"aciertos\":" + res.getAciertos()
                + ",\"total\":" + res.getTotalPreguntas()
                + ",\"percentil\":" + res.getPercentil()
                + ",\"categoria\":\"" + res.getCategoria() + "\""
                + ",\"porcentaje\":" + res.getPorcentaje()
                + ",\"observacion\":\"" + res.getObservacion() + "\""
                + "}");
    }
}