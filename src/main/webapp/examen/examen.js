const params = new URLSearchParams(window.location.search);
const sesion = params.get("sesion");
let clave = "";
const $ = (id) => document.getElementById(id);
const API = "/ProyectoFinalPOO/api/examen/";

$("btn-entrar").addEventListener("click", entrar);
$("btn-enviar").addEventListener("click", enviar);
["nombre","clave"].forEach(id => {
    $(id).addEventListener("keydown", e => { if (e.key === "Enter") entrar(); });
});

function entrar() {
    const nombre = $("nombre").value.trim();
    clave = $("clave").value;
    if (!nombre) { $("error-clave").textContent = "Escribe tu nombre."; return; }
    const body = new URLSearchParams();
    body.append("sesion", sesion);
    body.append("nombre", nombre);
    body.append("clave", clave);
    fetch(API + "acceso", { method: "POST", body: body })
        .then(r => r.json())
        .then(data => {
            if (!data.ok) { $("error-clave").textContent = data.error || "Error"; return; }
            $("error-clave").textContent = "";
            $("info-forma").textContent = nombre + " — Forma " + data.forma + ". Responde todas las preguntas:";
            const cont = $("lista-preguntas");
            cont.innerHTML = "";
            data.preguntas.forEach(p => {
                const div = document.createElement("div");
                div.className = "pregunta";
                let html = "<p><strong>" + p.numero + ".</strong> " + p.enunciado + "</p>";
                ["A","B","C","D"].forEach(op => {
                    html += '<label><input type="radio" name="p_' + p.oid + '" value="' + op + '"> ' + op + '</label>';
                });
                div.innerHTML = html;
                cont.appendChild(div);
            });
            $("pantalla-clave").style.display = "none";
            $("pantalla-preguntas").style.display = "block";
        })
        .catch(e => { $("error-clave").textContent = "Error de conexion: " + e; });
}

function enviar() {
    const body = new URLSearchParams();
    body.append("sesion", sesion);
    body.append("clave", clave);
    document.querySelectorAll("#lista-preguntas input[type=radio]:checked").forEach(r => {
        body.append(r.name, r.value);
    });
    fetch(API + "enviar", { method: "POST", body: body })
        .then(r => r.json())
        .then(data => {
            if (!data.ok) { alert(data.error || "Error"); return; }
            $("res-aciertos").textContent = "Aciertos: " + data.aciertos + " / " + data.total;
            $("res-porcentaje").textContent = "Porcentaje: " + data.porcentaje + "%";
            $("res-observacion").textContent = "Observacion: " + data.observacion;
            $("pantalla-preguntas").style.display = "none";
            $("pantalla-resultado").style.display = "block";
        })
        .catch(e => { alert("Error de conexion: " + e); });
}