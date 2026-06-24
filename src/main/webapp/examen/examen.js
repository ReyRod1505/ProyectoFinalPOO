const params = new URLSearchParams(window.location.search);
const sesion = params.get("sesion");
let clave = "";
let timerId = null;
let tiempoRestante = 0;
let formaActual = "";
const $ = (id) => document.getElementById(id);
const API = "/ProyectoFinalPOO/api/examen/";

const INSTRUCCIONES = {
    A: {
        titulo: "VOCABULARIO — FORMA A",
        intro: "Usted va a encontrar diversos ejercicios. En cada uno aparece una " +
            "palabra escrita en MAYUSCULAS y cinco opciones (A, B, C, D, E). " +
            "Una de esas opciones tiene el MISMO significado que la palabra " +
            "principal. Debe seleccionar unicamente una respuesta. Si se equivoca " +
            "puede cambiar su respuesta antes de enviar.",
        ejemplos: [
            { palabra: "GRAMA",   correcta: "B. Cesped",
                opciones: ["A. Pastizal", "B. Cesped", "C. Hierba", "D. Hortaliza", "E. Naturaleza"] },
            { palabra: "LIVIANO", correcta: "C. Ligero",
                opciones: ["A. Fino", "B. Delgado", "C. Ligero", "D. Graso", "E. Afinado"] }
        ]
    },
    B: {
        titulo: "VOCABULARIO — FORMA B",
        intro: "Usted va a encontrar diversos ejercicios. En cada uno aparece una " +
            "palabra principal y cinco opciones (A, B, C, D, E). Cuatro de ellas " +
            "comparten un significado parecido y solo UNA tiene significado " +
            "DISTINTO. Debe seleccionar unicamente la opcion diferente. Si se " +
            "equivoca puede cambiar su respuesta antes de enviar.",
        ejemplos: [
            { palabra: "Palabra distinta del grupo", correcta: "D. Veloz",
                opciones: ["A. Alegre", "B. Contento", "C. Feliz", "D. Veloz", "E. Jovial"] },
            { palabra: "Palabra distinta del grupo", correcta: "B. Silla",
                opciones: ["A. Correr", "B. Silla", "C. Saltar", "D. Caminar", "E. Trotar"] }
        ]  // <-- pega aqui los ejemplos oficiales de Forma B (mismo formato que A)
    }
};

$("btn-entrar").addEventListener("click", entrar);
$("btn-iniciar").addEventListener("click", iniciarExamen);
$("btn-enviar").addEventListener("click", () => enviar(false));
["estudiante","clave"].forEach(id => {
    $(id).addEventListener("keydown", e => { if (e.key === "Enter") entrar(); });
});

cargarEstudiantes();

function cargarEstudiantes() {
    fetch(API + "acceso", { method: "GET" })
        .then(r => r.json())
        .then(data => {
            if (!data.ok) { $("error-clave").textContent = data.error || "No se pudo cargar la lista"; return; }
            const sel = $("estudiante");
            data.estudiantes.forEach(e => {
                const op = document.createElement("option");
                op.value = e.oid;
                op.textContent = e.nombre;
                sel.appendChild(op);
            });
        })
        .catch(e => { $("error-clave").textContent = "No se pudo cargar la lista: " + e; });
}

// PASO 1: autenticar -> ir a la pantalla de bienvenida (sin arrancar el reloj)
function entrar() {
    const sel = $("estudiante");
    const estudianteId = sel.value;
    const nombre = sel.selectedIndex >= 0 ? sel.options[sel.selectedIndex].text : "";
    clave = $("clave").value;
    if (!estudianteId) { $("error-clave").textContent = "Selecciona tu nombre."; return; }

    const body = new URLSearchParams();
    body.append("sesion", sesion);
    body.append("estudiante", estudianteId);
    body.append("clave", clave);
    fetch(API + "acceso", { method: "POST", body: body })
        .then(r => r.json())
        .then(data => {
            if (!data.ok) { $("error-clave").textContent = data.error || "Error"; return; }
            $("error-clave").textContent = "";
            formaActual = data.forma;

            // Renderizar las preguntas ya (quedan ocultas hasta iniciar)
            $("info-forma").textContent = nombre + " — Forma " + data.forma + ". Responde todas las preguntas:";
            const cont = $("lista-preguntas");
            cont.innerHTML = "";
            data.preguntas.forEach(p => {
                const div = document.createElement("div");
                div.className = "pregunta";
                let html = "<p><strong>" + p.numero + ".</strong> " + p.enunciado + "</p>";
                ["A","B","C","D","E"].forEach(op => {
                    html += '<label><input type="radio" name="p_' + p.oid + '" value="' + op + '"> ' + op + '</label>';
                });
                div.innerHTML = html;
                cont.appendChild(div);
            });

            // Poblar la pantalla de bienvenida
            $("bienvenida-saludo").textContent = "Hola, " + nombre;
            renderInstrucciones(data.forma, data.totalPreguntas, data.limiteSegundos);

            $("pantalla-clave").style.display = "none";
            $("pantalla-bienvenida").style.display = "block";
        })
        .catch(e => { $("error-clave").textContent = "Error de conexion: " + e; });
}

function renderInstrucciones(forma, totalPreguntas, limiteSegundos) {
    const info = INSTRUCCIONES[forma] || INSTRUCCIONES.A;
    let html = "<h3>" + info.titulo + "</h3>";
    html += "<p>" + info.intro + "</p>";
    info.ejemplos.forEach((ej, i) => {
        html += "<div class='ejemplo'><p><strong>Ejemplo " + (i + 1) + ": " + ej.palabra + "</strong></p><ul>";
        ej.opciones.forEach(op => {
            const marca = (op === ej.correcta) ? " &#10003;" : "";
            html += "<li>" + op + marca + "</li>";
        });
        html += "</ul></div>";
    });
    html += "<p class='resumen'>El examen consta de <strong>" + totalPreguntas +
        "</strong> preguntas. Dispondra de <strong>" + formatearDuracion(limiteSegundos) +
        "</strong>. Procure trabajar lo mas rapido posible.</p>";
    html += "<p class='aviso'><strong>El cronometro empezara unicamente cuando presione " +
        "\"Iniciar examen\".</strong> Las respuestas incorrectas no restan puntaje.</p>";
    $("bienvenida-instrucciones").innerHTML = html;
}

// PASO 2: el alumno pulsa "Iniciar examen" -> AHORA arranca el reloj
function iniciarExamen() {
    $("btn-iniciar").disabled = true;
    const body = new URLSearchParams();
    body.append("accion", "iniciar");
    body.append("sesion", sesion);
    body.append("clave", clave);
    fetch(API + "acceso", { method: "POST", body: body })
        .then(r => r.json())
        .then(data => {
            if (!data.ok) {
                $("error-bienvenida").textContent = data.error || "Error";
                $("btn-iniciar").disabled = false;
                return;
            }
            $("pantalla-bienvenida").style.display = "none";
            $("pantalla-preguntas").style.display = "block";

            const restante = data.segundosRestantes;
            if (restante <= 0) {
                $("cronometro").textContent = "Tiempo agotado";
                enviar(true);
            } else {
                iniciarCronometro(restante);
            }
        })
        .catch(e => {
            $("error-bienvenida").textContent = "Error de conexion: " + e;
            $("btn-iniciar").disabled = false;
        });
}

function iniciarCronometro(segundos) {
    tiempoRestante = segundos;
    actualizarCronometro();
    timerId = setInterval(() => {
        tiempoRestante--;
        actualizarCronometro();
        if (tiempoRestante <= 0) {
            clearInterval(timerId);
            timerId = null;
            alert("Se acabo el tiempo. Se enviaran tus respuestas.");
            enviar(true);
        }
    }, 1000);
}

function actualizarCronometro() {
    const m = Math.floor(tiempoRestante / 60);
    const s = tiempoRestante % 60;
    const txt = (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    const el = $("cronometro");
    el.textContent = "Tiempo restante: " + txt;
    if (tiempoRestante <= 30) el.classList.add("poco-tiempo");
    else el.classList.remove("poco-tiempo");
}

function enviar(auto) {
    const total = document.querySelectorAll("#lista-preguntas .pregunta").length;
    const respondidas = document.querySelectorAll("#lista-preguntas input[type=radio]:checked").length;
    if (!auto && respondidas < total) {
        if (!confirm("Te faltan " + (total - respondidas) + " pregunta(s) por responder.\n¿Enviar de todos modos?")) {
            return;
        }
    }
    if (timerId) { clearInterval(timerId); timerId = null; }
    $("btn-enviar").disabled = true;
    const body = new URLSearchParams();
    body.append("sesion", sesion);
    body.append("clave", clave);
    document.querySelectorAll("#lista-preguntas input[type=radio]:checked").forEach(r => {
        body.append(r.name, r.value);
    });
    fetch(API + "enviar", { method: "POST", body: body })
        .then(r => r.json())
        .then(data => {
            if (!data.ok) { alert(data.error || "Error"); $("btn-enviar").disabled = false; return; }
            $("res-titulo").textContent = "Vocabulary " + formaActual + ": percentil " + data.percentil;
            $("res-aciertos").textContent = "Aciertos: " + data.aciertos + " de " + data.total
                + "  (" + data.categoria + ")";
            $("res-interpretacion").textContent = interpretacionDe(data.categoria);
            $("pantalla-preguntas").style.display = "none";
            $("pantalla-resultado").style.display = "block";
        })
        .catch(e => { alert("Error de conexion: " + e); $("btn-enviar").disabled = false; });
}

function formatearDuracion(segundos) {
    const m = Math.floor(segundos / 60);
    return m + " minuto" + (m === 1 ? "" : "s");
}

function interpretacionDe(categoria) {
    switch (categoria) {
        case "Muy superior":
            return "El estudiante posee un dominio muy amplio del vocabulario evaluado " +
                "y demuestra una excelente capacidad de comprensión léxica.";
        case "Superior":
            return "El estudiante presenta un buen dominio del vocabulario, aunque aún " +
                "puede fortalecer algunas áreas mediante práctica y lectura.";
        case "Medio":
            return "El estudiante posee un nivel intermedio de vocabulario. Se recomienda " +
                "continuar desarrollando habilidades de comprensión y ampliar el " +
                "vocabulario mediante la lectura frecuente.";
        case "Medio bajo":
            return "El estudiante presenta un nivel básico de vocabulario. Se recomienda " +
                "reforzar el aprendizaje mediante ejercicios y hábitos de lectura.";
        default:
            return "El resultado indica que el estudiante requiere fortalecer " +
                "significativamente su vocabulario para mejorar su comprensión léxica.";
    }
}