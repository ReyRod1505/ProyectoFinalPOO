package ni.edu.uam.ProyectoFinalPOO.acciones;

import org.openxava.actions.*;

public class IniciarTestAction extends ViewBaseAction implements IForwardAction {

    private String uri = null;

    public void execute() throws Exception {
        Object oid = getView().getValue("oid");
        if (oid == null) { addError("Guarda la sesion antes de iniciar el test."); return; }
        uri = "/examen/examen.html?sesion=" + oid;
    }

    public String getForwardURI() { return uri; }
    public boolean inNewWindow() { return true; }
}