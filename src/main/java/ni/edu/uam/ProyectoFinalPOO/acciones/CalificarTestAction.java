package ni.edu.uam.ProyectoFinalPOO.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import ni.edu.uam.ProyectoFinalPOO.modelo.*;

public class CalificarTestAction extends ViewBaseAction {

    public void execute() throws Exception {
        Object oid = getView().getValue("oid");
        if (oid == null) {
            addError("No hay una sesion cargada.");
            return;
        }
        SesionTest sesion = XPersistence.getManager().find(SesionTest.class, oid);
        if (sesion == null) {
            addError("No se encontro la sesion.");
            return;
        }
        if (!sesion.estaFinalizada()) {
            addError("Debe finalizar el test antes de calificarlo.");
            return;
        }
        if (sesion.getRespuestas() == null || sesion.getRespuestas().isEmpty()) {
            addError("La sesion no tiene respuestas registradas. Agregue respuestas antes de calificar.");
            return;
        }

        int aciertos = 0;
        int total = 0;
        for (Respuesta r : sesion.getRespuestas()) {
            r.evaluar();
            if (r.isCorrecta()) aciertos++;
            total++;
        }

        Resultado resultado = sesion.getResultado();
        if (resultado == null) {
            resultado = new Resultado();
            resultado.setSesion(sesion);
            sesion.setResultado(resultado);
            XPersistence.getManager().persist(resultado);
        }
        resultado.setAciertos(aciertos);
        resultado.setTotalPreguntas(total);
        resultado.calcularPorcentaje();
        resultado.generarObservacion();

        sesion.calificar();
        XPersistence.getManager().flush();
        getView().refresh();
        addMessage("Test calificado: " + aciertos + "/" + total
                + " (" + resultado.getObservacion() + ").");
    }
}