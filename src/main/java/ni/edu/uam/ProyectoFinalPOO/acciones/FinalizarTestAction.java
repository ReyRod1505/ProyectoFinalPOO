package ni.edu.uam.ProyectoFinalPOO.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import ni.edu.uam.ProyectoFinalPOO.modelo.*;

public class FinalizarTestAction extends ViewBaseAction {

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
        if (!sesion.estaIniciada()) {
            addError("Solo puede finalizar una sesion que este iniciada.");
            return;
        }
        sesion.finalizar();
        XPersistence.getManager().flush();
        getView().refresh();
        addMessage("Test finalizado. Duracion: " + sesion.getDuracionMinutos() + " min.");
    }
}