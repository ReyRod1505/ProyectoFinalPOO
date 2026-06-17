package ni.edu.uam.ProyectoFinalPOO.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import ni.edu.uam.ProyectoFinalPOO.modelo.*;

public class IniciarTestAction extends ViewBaseAction {

    public void execute() throws Exception {
        Object oid = getView().getValue("oid");
        if (oid == null) {
            addError("Primero guarde la sesion (boton Guardar) antes de iniciar el test.");
            return;
        }
        SesionTest sesion = XPersistence.getManager().find(SesionTest.class, oid);
        if (sesion == null) {
            addError("No se encontro la sesion.");
            return;
        }
        if (sesion.getEstado() != EstadoSesion.PENDIENTE) {
            addError("La sesion ya fue iniciada o procesada.");
            return;
        }
        sesion.iniciar();
        XPersistence.getManager().flush();
        getView().refresh();
        addMessage("Test iniciado correctamente.");
    }
}