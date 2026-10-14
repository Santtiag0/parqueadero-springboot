const API = "/api/v1";

const formatoMoneda = new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
});

let tarifasPorTipo = {};
let activosCache = [];
let filtroPlaca = "";
let cifrasOcultas = false;
let formato24h = true;

// ---------------------------------------------------------------------
// Sesion (login con JWT)
// ---------------------------------------------------------------------

let token = sessionStorage.getItem("parqueadero-token");
let usuarioActual = sessionStorage.getItem("parqueadero-usuario");

function cerrarSesion() {
    sessionStorage.removeItem("parqueadero-token");
    sessionStorage.removeItem("parqueadero-usuario");
    window.location.reload();
}

// ---------------------------------------------------------------------
// Utilidades HTTP
// ---------------------------------------------------------------------

async function peticion(url, opciones = {}) {
    const headers = Object.assign({}, opciones.headers || {});
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const respuesta = await fetch(url, { ...opciones, headers });

    if (respuesta.status === 401) {
        cerrarSesion();
        throw new Error("Tu sesion expiro, inicia sesion de nuevo");
    }

    const texto = await respuesta.text();
    const cuerpo = texto ? JSON.parse(texto) : null;
    if (!respuesta.ok) {
        const detalle = cuerpo && cuerpo.detalles && cuerpo.detalles.length
            ? cuerpo.detalles.join(", ")
            : (cuerpo && cuerpo.message) || "Ocurrio un error inesperado";
        throw new Error(detalle);
    }
    return cuerpo;
}

function mostrarMensaje(elementoId, texto, esError) {
    const el = document.getElementById(elementoId);
    el.textContent = texto;
    el.className = "mensaje " + (esError ? "error" : "ok");
}

// ---------------------------------------------------------------------
// Replica en el navegador de la regla de cobro del backend
// (misma formula que CalculadoraCobro.java: fraccion de minuto completa,
// tiempo de gracia exento y tope diario por cada 24h)
// ---------------------------------------------------------------------

function calcularCobroEnVivo(horaEntradaIso, tarifa) {
    if (!tarifa) return 0;
    const entrada = new Date(horaEntradaIso);
    const ahora = new Date();
    const segundos = Math.max(0, Math.floor((ahora - entrada) / 1000));
    const minutos = Math.ceil(segundos / 60);

    if (minutos <= tarifa.minutosGracia) return 0;

    const minutosPorDia = 24 * 60;
    const diasCompletos = Math.floor(minutos / minutosPorDia);
    const minutosRestantes = minutos % minutosPorDia;

    const costoDias = tarifa.topeDia * diasCompletos;
    let costoRestante = tarifa.valorMinuto * minutosRestantes;
    if (costoRestante > tarifa.topeDia) costoRestante = tarifa.topeDia;

    return costoDias + costoRestante;
}

function formatearDuracion(horaEntradaIso) {
    const entrada = new Date(horaEntradaIso);
    const segundosTotales = Math.max(0, Math.floor((new Date() - entrada) / 1000));
    const horas = Math.floor(segundosTotales / 3600);
    const minutos = Math.floor((segundosTotales % 3600) / 60);
    const segundos = segundosTotales % 60;

    if (horas > 0) return `${horas}h ${minutos}m ${segundos}s`;
    if (minutos > 0) return `${minutos}m ${segundos}s`;
    return `${segundos}s`;
}

const ICONO_TIPO = {
    CARRO: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icono-vehiculo"><path d="M3 13l1.5-4.5A2 2 0 0 1 6.4 7h11.2a2 2 0 0 1 1.9 1.5L21 13"/><rect x="2" y="13" width="20" height="5" rx="1.5"/><circle cx="7" cy="18" r="1.6"/><circle cx="17" cy="18" r="1.6"/></svg>',
    MOTO: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icono-vehiculo"><circle cx="5.5" cy="17" r="2.5"/><circle cx="18.5" cy="17" r="2.5"/><path d="M8 17h5l2-5h3M13 12l-2-3H7l-1.5 3"/></svg>',
    BICICLETA: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icono-vehiculo"><circle cx="5.5" cy="17.5" r="3.5"/><circle cx="18.5" cy="17.5" r="3.5"/><path d="M5.5 17.5 10 8h4l-2.5 5h5l3 4.5M10 8l2 3"/></svg>',
};

// ---------------------------------------------------------------------
// Reloj
// ---------------------------------------------------------------------

function actualizarReloj() {
    const ahora = new Date();
    const horas = formato24h
        ? String(ahora.getHours()).padStart(2, "0")
        : String(((ahora.getHours() + 11) % 12) + 1).padStart(2, "0");
    const minutos = String(ahora.getMinutes()).padStart(2, "0");
    const segundos = String(ahora.getSeconds()).padStart(2, "0");
    const sufijo = formato24h ? "" : (ahora.getHours() >= 12 ? " p. m." : " a. m.");
    document.getElementById("relojHora").textContent = `${horas}:${minutos}:${segundos}${sufijo}`;

    const weekday = ahora.toLocaleDateString("es-CO", { weekday: "long" });
    const mes = ahora.toLocaleDateString("es-CO", { month: "long" });
    document.getElementById("relojFecha").textContent =
        `${weekday}, ${ahora.getDate()} de ${mes}, ${ahora.getFullYear()}`;
}

document.getElementById("btnFormato").addEventListener("click", () => {
    formato24h = !formato24h;
    document.getElementById("btnFormato").classList.toggle("activo", formato24h);
    actualizarReloj();
});
document.getElementById("btnFormato").classList.add("activo");

document.getElementById("btnTema").addEventListener("click", () => {
    const actual = document.documentElement.getAttribute("data-theme");
    const nuevo = actual === "dark" ? "light" : "dark";
    document.documentElement.setAttribute("data-theme", nuevo);
    try { localStorage.setItem("parqueadero-tema", nuevo); } catch (e) { /* almacenamiento no disponible */ }
});

try {
    const temaGuardado = localStorage.getItem("parqueadero-tema");
    if (temaGuardado) document.documentElement.setAttribute("data-theme", temaGuardado);
} catch (e) { /* almacenamiento no disponible */ }

document.getElementById("btnOcultar").addEventListener("click", () => {
    cifrasOcultas = !cifrasOcultas;
    document.querySelector(".stats-card").classList.toggle("cifras-ocultas", cifrasOcultas);
});

// ---------------------------------------------------------------------
// Carga de datos desde la API
// ---------------------------------------------------------------------

async function cargarTarifas() {
    const datos = await peticion(`${API}/tarifas`);
    tarifasPorTipo = Object.fromEntries(datos.map((t) => [t.tipoVehiculo, t]));

    const cuerpo = document.getElementById("tablaTarifas");
    cuerpo.innerHTML = "";
    datos.forEach((tarifa) => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td>${ICONO_TIPO[tarifa.tipoVehiculo] || ""} ${tarifa.tipoVehiculo}</td>
            <td>${formatoMoneda.format(tarifa.valorMinuto)}</td>
            <td>${formatoMoneda.format(tarifa.topeDia)}</td>
            <td>${tarifa.minutosGracia} min</td>
            <td><button class="btn-editar-tarifa" data-tipo="${tarifa.tipoVehiculo}" data-minuto="${tarifa.valorMinuto}" data-tope="${tarifa.topeDia}" data-gracia="${tarifa.minutosGracia}">Editar</button></td>
        `;
        cuerpo.appendChild(fila);
    });

    cuerpo.querySelectorAll("button[data-tipo]").forEach((boton) => {
        boton.addEventListener("click", async () => {
            const tipo = boton.dataset.tipo;
            const valorMinuto = prompt("Valor por minuto:", boton.dataset.minuto);
            if (valorMinuto === null) return;
            const topeDia = prompt("Tope del dia:", boton.dataset.tope);
            if (topeDia === null) return;
            const minutosGracia = prompt("Minutos de gracia:", boton.dataset.gracia);
            if (minutosGracia === null) return;
            try {
                await peticion(`${API}/tarifas/${tipo}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        valorMinuto: Number(valorMinuto),
                        topeDia: Number(topeDia),
                        minutosGracia: Number(minutosGracia),
                    }),
                });
                await cargarTarifas();
            } catch (error) {
                alert("No se pudo actualizar la tarifa: " + error.message);
            }
        });
    });
}

async function cargarDisponibilidad() {
    const datos = await peticion(`${API}/cupos/disponibilidad`);
    const totalLibres = datos.reduce((acc, item) => acc + item.libres, 0);
    const totalCupos = datos.reduce((acc, item) => acc + item.total, 0);
    document.getElementById("identityCupos").textContent = `${totalLibres} cupos libres de ${totalCupos}`;
}

async function cargarActivos() {
    activosCache = await peticion(`${API}/tickets?estado=ACTIVO`);
    renderTablaActivos();
    recalcularPendiente();
}

async function cargarCierreDeHoy() {
    const hoy = new Date().toISOString().slice(0, 10);
    const cierre = await peticion(`${API}/reportes/cierre?fecha=${hoy}`);
    document.getElementById("statSalieron").textContent = cierre.totalVehiculos;
    document.getElementById("statRecaudado").textContent = formatoMoneda.format(cierre.totalIngresos);

    const entraronHoy = activosCache.filter((t) => t.horaEntrada.slice(0, 10) === hoy).length + cierre.totalVehiculos;
    document.getElementById("statEntraron").textContent = entraronHoy;
}

async function cargarMensualidades() {
    const mensualidades = await peticion(`${API}/mensualidades`);
    document.getElementById("badgeMensualidades").textContent = mensualidades.length;

    const cuerpo = document.getElementById("tablaMensualidades");
    cuerpo.innerHTML = "";
    if (mensualidades.length === 0) {
        cuerpo.innerHTML = `<tr><td colspan="5" class="vacio">Aun no hay mensualidades registradas</td></tr>`;
        return;
    }

    mensualidades.forEach((m) => {
        const fila = document.createElement("tr");
        fila.innerHTML = `
            <td><span class="placa-chip">${m.placa}</span></td>
            <td>${m.titular}</td>
            <td>${m.fechaInicio}</td>
            <td>${m.fechaFin}</td>
            <td><span class="pill-estado ${m.vigente ? "pill-vigente" : ""}">${m.vigente ? "VIGENTE" : "VENCIDA"}</span></td>
        `;
        cuerpo.appendChild(fila);
    });
}

async function cargarTodo() {
    await cargarTarifas();
    await Promise.all([cargarDisponibilidad(), cargarActivos(), cargarMensualidades()]);
    await cargarCierreDeHoy();
}

// ---------------------------------------------------------------------
// Tabla de vehiculos activos
// ---------------------------------------------------------------------

function renderTablaActivos() {
    const cuerpo = document.getElementById("tablaActivos");
    const filtrados = activosCache.filter((t) =>
        t.placa.toLowerCase().includes(filtroPlaca.toLowerCase()));

    document.getElementById("badgeActivos").textContent = `${activosCache.length} activos`;
    document.getElementById("statParqueados").textContent = activosCache.length;

    cuerpo.innerHTML = "";
    if (filtrados.length === 0) {
        cuerpo.innerHTML = `<tr><td colspan="8" class="vacio">No hay vehiculos que coincidan con la busqueda</td></tr>`;
        return;
    }

    filtrados.forEach((ticket) => {
        const horaEntrada = new Date(ticket.horaEntrada).toLocaleTimeString("es-CO", { hour: "2-digit", minute: "2-digit" });
        const fila = document.createElement("tr");
        fila.dataset.id = ticket.id;
        fila.innerHTML = `
            <td><span class="placa-chip">${ticket.placa}</span>${ticket.cedulaPropietario ? `<div class="subtexto-celda">CC ${ticket.cedulaPropietario}</div>` : ""}</td>
            <td><span class="tipo-celda">${ICONO_TIPO[ticket.tipoVehiculo] || ""} ${ticket.tipoVehiculo}</span></td>
            <td>${horaEntrada}</td>
            <td id="tiempo-${ticket.id}">${formatearDuracion(ticket.horaEntrada)}</td>
            <td><span class="pill-tarifa">Por minuto</span></td>
            <td class="monto-celda" id="monto-${ticket.id}">${formatoMoneda.format(calcularCobroEnVivo(ticket.horaEntrada, tarifasPorTipo[ticket.tipoVehiculo]))}</td>
            <td><span class="pill-estado">PARQUEADO</span>${ticket.conMensualidad ? '<span class="pill-mensual">MENSUAL</span>' : ""}</td>
            <td class="acciones-celda">
                <button class="accion-btn accion-cobrar" title="Registrar salida" data-accion="salida" data-placa="${ticket.placa}">
                    <span class="icono-dolar">$</span>
                </button>
                <button class="accion-btn accion-imprimir" title="Imprimir ticket" data-accion="imprimir" data-id="${ticket.id}">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9V3h12v6M6 18H4a1 1 0 0 1-1-1v-5a1 1 0 0 1 1-1h16a1 1 0 0 1 1 1v5a1 1 0 0 1-1 1h-2M6 14h12v7H6z"/></svg>
                </button>
                <button class="accion-btn accion-historial" title="Ver historial" data-accion="historial" data-placa="${ticket.placa}">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 3"/></svg>
                </button>
            </td>
        `;
        cuerpo.appendChild(fila);
    });
}

function recalcularPendiente() {
    const pendiente = activosCache.reduce((acc, t) => {
        if (t.conMensualidad) return acc;
        return acc + calcularCobroEnVivo(t.horaEntrada, tarifasPorTipo[t.tipoVehiculo]);
    }, 0);
    document.getElementById("statPendiente").textContent = formatoMoneda.format(pendiente);
}

function tick() {
    actualizarReloj();
    activosCache.forEach((ticket) => {
        const celdaTiempo = document.getElementById(`tiempo-${ticket.id}`);
        const celdaMonto = document.getElementById(`monto-${ticket.id}`);
        if (celdaTiempo) celdaTiempo.textContent = formatearDuracion(ticket.horaEntrada);
        if (celdaMonto) celdaMonto.textContent = formatoMoneda.format(
            calcularCobroEnVivo(ticket.horaEntrada, tarifasPorTipo[ticket.tipoVehiculo]));
    });
    recalcularPendiente();
}

document.getElementById("buscarPlaca").addEventListener("input", (evento) => {
    filtroPlaca = evento.target.value.trim();
    renderTablaActivos();
});

document.getElementById("tablaActivos").addEventListener("click", async (evento) => {
    const boton = evento.target.closest("button[data-accion]");
    if (!boton) return;

    if (boton.dataset.accion === "salida") {
        await procesarSalida(boton.dataset.placa);
    } else if (boton.dataset.accion === "imprimir") {
        const ticket = activosCache.find((t) => String(t.id) === boton.dataset.id);
        if (ticket) imprimirTicket(ticket);
    } else if (boton.dataset.accion === "historial") {
        await abrirHistorial(boton.dataset.placa);
    }
});

// ---------------------------------------------------------------------
// Campo de placa: detecta automaticamente entrada o salida
// ---------------------------------------------------------------------

const placaInput = document.getElementById("placaInput");
const plateWrap = document.getElementById("plateWrap");
const heroHint = document.getElementById("heroHint");
const tipoTabs = document.querySelectorAll(".tipo-tab");
let tipoSeleccionado = "CARRO";

const cedulaExtra = document.getElementById("cedulaExtra");
const cedulaPropietarioInput = document.getElementById("cedulaPropietarioInput");

const MARCADOR_POSICION = { CARRO: "ABC123", MOTO: "ABC12D", BICICLETA: "A12" };
const LONGITUD_MAXIMA = { CARRO: 6, MOTO: 6, BICICLETA: 3 };
const PATRONES = {
    CARRO: /^[A-Z]{3}[0-9]{3}$/,
    MOTO: /^[A-Z]{3}[0-9]{2}[A-Z]$/,
    BICICLETA: /^[A-Z0-9]{3}$/,
};
const MENSAJE_FORMATO = {
    CARRO: "La placa de un carro debe tener el formato AAA111 (3 letras y 3 números)",
    MOTO: "La placa de una moto debe tener el formato AAA11A (3 letras, 2 números y 1 letra)",
    BICICLETA: "El serial debe tener 3 caracteres alfanuméricos",
};
const TEXTO_AYUDA = {
    CARRO: 'Formato AAA111 (3 letras y 3 números). Presiona <kbd>Enter</kbd> o el botón para procesar.',
    MOTO: 'Formato AAA11A (3 letras, 2 números y 1 letra). Presiona <kbd>Enter</kbd> o el botón para procesar.',
    BICICLETA: 'Las bicicletas no tienen placa: se registran con los 3 primeros caracteres del serial del cuadro (suele empezar con una letra) y la cédula del propietario.',
};

function seleccionarTipo(tipo) {
    tipoSeleccionado = tipo;
    tipoTabs.forEach((tab) => tab.classList.toggle("activo", tab.dataset.tipo === tipo));
    plateWrap.dataset.tipo = tipo;
    placaInput.placeholder = MARCADOR_POSICION[tipo];
    placaInput.maxLength = LONGITUD_MAXIMA[tipo];
    placaInput.value = "";
    heroHint.innerHTML = TEXTO_AYUDA[tipo];
    cedulaExtra.classList.toggle("oculto", tipo !== "BICICLETA");
    cedulaPropietarioInput.value = "";
    mostrarMensaje("mensajeProceso", "", false);
    placaInput.focus();
}

tipoTabs.forEach((tab) => tab.addEventListener("click", () => seleccionarTipo(tab.dataset.tipo)));

placaInput.addEventListener("input", () => {
    placaInput.value = placaInput.value.toUpperCase()
        .replace(/[^A-Z0-9]/g, "")
        .slice(0, LONGITUD_MAXIMA[tipoSeleccionado]);
});

cedulaPropietarioInput.addEventListener("input", () => {
    cedulaPropietarioInput.value = cedulaPropietarioInput.value.replace(/\D/g, "").slice(0, 15);
});

async function procesarPlaca() {
    const identificador = placaInput.value.trim().toUpperCase()
        .replace(/[^A-Z0-9]/g, "")
        .slice(0, LONGITUD_MAXIMA[tipoSeleccionado]);

    if (!identificador) {
        mostrarMensaje("mensajeProceso",
            tipoSeleccionado === "BICICLETA" ? "Escribe los 3 primeros caracteres del serial" : "Escribe una placa para continuar",
            true);
        return;
    }

    const yaActivo = activosCache.some((t) => t.placa === identificador);
    if (yaActivo) {
        await procesarSalida(identificador);
        return;
    }

    if (!PATRONES[tipoSeleccionado].test(identificador)) {
        mostrarMensaje("mensajeProceso", MENSAJE_FORMATO[tipoSeleccionado], true);
        return;
    }

    let cedulaPropietario = null;
    if (tipoSeleccionado === "BICICLETA") {
        cedulaPropietario = cedulaPropietarioInput.value.trim();
        if (!cedulaPropietario) {
            mostrarMensaje("mensajeProceso", "Escribe la cédula del propietario de la bicicleta", true);
            return;
        }
    }

    await procesarEntrada(identificador, tipoSeleccionado, cedulaPropietario);
}

async function procesarEntrada(placa, tipoVehiculo, cedulaPropietario) {
    try {
        const ticket = await peticion(`${API}/tickets/entrada`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ placa, tipoVehiculo, cedulaPropietario }),
        });
        mostrarMensaje("mensajeProceso", `Entrada registrada: ${ticket.placa} en el cupo ${ticket.cupoCodigo}`, false);
        placaInput.value = "";
        cedulaPropietarioInput.value = "";
        await Promise.all([cargarDisponibilidad(), cargarActivos()]);
    } catch (error) {
        mostrarMensaje("mensajeProceso", error.message, true);
    }
}

async function procesarSalida(placa) {
    try {
        const ticket = await peticion(`${API}/tickets/salida`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ placa }),
        });
        mostrarMensaje("mensajeProceso", `Salida registrada: ${ticket.placa} - cobro ${formatoMoneda.format(ticket.valorCobrado)}`, false);
        placaInput.value = "";
        await Promise.all([cargarDisponibilidad(), cargarActivos(), cargarCierreDeHoy()]);
    } catch (error) {
        mostrarMensaje("mensajeProceso", error.message, true);
    }
}

document.getElementById("btnProcesar").addEventListener("click", procesarPlaca);
placaInput.addEventListener("keydown", (evento) => {
    if (evento.key === "Enter") procesarPlaca();
});

// ---------------------------------------------------------------------
// Historial por placa
// ---------------------------------------------------------------------

const modalHistorial = document.getElementById("modalHistorial");

async function abrirHistorial(placa) {
    document.getElementById("historialPlacaTexto").textContent = placa;
    const contenido = document.getElementById("historialContenido");
    contenido.innerHTML = "<p>Cargando...</p>";
    modalHistorial.classList.remove("oculto");

    try {
        const historial = await peticion(`${API}/tickets/historial/${placa}`);
        if (historial.length === 0) {
            contenido.innerHTML = "<p>Sin registros previos para esta placa.</p>";
            return;
        }
        const tieneCedulas = historial.some((t) => t.cedulaPropietario);
        contenido.innerHTML = `
            <table class="tabla-simple">
                <thead><tr><th>Entrada</th><th>Salida</th><th>Cupo</th>${tieneCedulas ? "<th>Cédula</th>" : ""}<th>Cobro</th><th>Estado</th></tr></thead>
                <tbody>
                    ${historial.map((t) => `
                        <tr>
                            <td>${new Date(t.horaEntrada).toLocaleString("es-CO")}</td>
                            <td>${t.horaSalida ? new Date(t.horaSalida).toLocaleString("es-CO") : "—"}</td>
                            <td>${t.cupoCodigo}</td>
                            ${tieneCedulas ? `<td>${t.cedulaPropietario || "—"}</td>` : ""}
                            <td>${t.valorCobrado !== null ? formatoMoneda.format(t.valorCobrado) : "—"}</td>
                            <td>${t.estado}</td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    } catch (error) {
        contenido.innerHTML = `<p class="mensaje error">${error.message}</p>`;
    }
}

document.getElementById("btnCerrarHistorial").addEventListener("click", () => {
    modalHistorial.classList.add("oculto");
});

// ---------------------------------------------------------------------
// Recibo de impresion
// ---------------------------------------------------------------------

function imprimirTicket(ticket) {
    const tarifa = tarifasPorTipo[ticket.tipoVehiculo];
    const monto = calcularCobroEnVivo(ticket.horaEntrada, tarifa);
    const recibo = document.getElementById("reciboImprimir");
    recibo.innerHTML = `
        <h2>Parqueadero Central</h2>
        <p>Ticket de parqueo</p>
        <hr>
        <p><strong>Placa / Serial:</strong> ${ticket.placa}</p>
        ${ticket.cedulaPropietario ? `<p><strong>Cédula del propietario:</strong> ${ticket.cedulaPropietario}</p>` : ""}
        <p><strong>Tipo:</strong> ${ticket.tipoVehiculo}</p>
        <p><strong>Cupo:</strong> ${ticket.cupoCodigo}</p>
        <p><strong>Hora de entrada:</strong> ${new Date(ticket.horaEntrada).toLocaleString("es-CO")}</p>
        <p><strong>Monto acumulado:</strong> ${formatoMoneda.format(monto)}</p>
        <hr>
        <p>Conserve este ticket. Monto sujeto a cambio hasta el momento de la salida.</p>
    `;
    window.print();
}

// ---------------------------------------------------------------------
// Mensualidad y cierre de caja
// ---------------------------------------------------------------------

const mensualidadPlacaInput = document.getElementById("mensualidadPlaca");
mensualidadPlacaInput.addEventListener("input", () => {
    mensualidadPlacaInput.value = mensualidadPlacaInput.value.toUpperCase().replace(/[^A-Z0-9]/g, "").slice(0, 6);
});

document.getElementById("formMensualidad").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const placa = mensualidadPlacaInput.value.trim().toUpperCase();
    const titular = document.getElementById("mensualidadTitular").value.trim();
    const fechaInicio = document.getElementById("mensualidadInicio").value;
    const fechaFin = document.getElementById("mensualidadFin").value;

    const esValida = Object.values(PATRONES).some((patron) => patron.test(placa));
    if (!esValida) {
        mostrarMensaje("mensajeMensualidad",
            "La placa/serial no corresponde a ningun formato valido: carro (AAA111), moto (AAA11A) o bicicleta (3 caracteres alfanuméricos)",
            true);
        return;
    }

    try {
        await peticion(`${API}/mensualidades`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ placa, titular, fechaInicio, fechaFin }),
        });
        mostrarMensaje("mensajeMensualidad", "Mensualidad registrada", false);
        document.getElementById("formMensualidad").reset();
        await cargarMensualidades();
    } catch (error) {
        mostrarMensaje("mensajeMensualidad", error.message, true);
    }
});

document.getElementById("formCierre").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const fecha = document.getElementById("cierreFecha").value;
    const contenedor = document.getElementById("resultadoCierre");
    try {
        const cierre = await peticion(`${API}/reportes/cierre?fecha=${fecha}`);
        const filas = cierre.detalle.map((item) => `
            <tr><td>${item.tipoVehiculo}</td><td>${item.vehiculos}</td><td>${formatoMoneda.format(item.total)}</td></tr>
        `).join("");
        contenedor.innerHTML = `
            <div class="total">${formatoMoneda.format(cierre.totalIngresos)}</div>
            <div>${cierre.totalVehiculos} vehiculos atendidos el ${cierre.fecha}</div>
            <table class="tabla-simple">
                <thead><tr><th>Tipo</th><th>Vehiculos</th><th>Total</th></tr></thead>
                <tbody>${filas || '<tr><td colspan="3" class="vacio">Sin movimientos ese dia</td></tr>'}</tbody>
            </table>
        `;
    } catch (error) {
        contenedor.innerHTML = `<p class="mensaje error">${error.message}</p>`;
    }
});

// ---------------------------------------------------------------------
// Navegacion entre pestanas (Parqueadero / Mensualidades)
// ---------------------------------------------------------------------

const vistaTabs = document.querySelectorAll(".vista-tab");
const vistas = {
    parqueadero: document.getElementById("vistaParqueadero"),
    mensualidades: document.getElementById("vistaMensualidades"),
};

vistaTabs.forEach((tab) => {
    tab.addEventListener("click", () => {
        vistaTabs.forEach((t) => t.classList.toggle("activa", t === tab));
        Object.entries(vistas).forEach(([clave, elemento]) => {
            elemento.classList.toggle("oculto", clave !== tab.dataset.vista);
        });
    });
});

// ---------------------------------------------------------------------
// Login
// ---------------------------------------------------------------------

const loginOverlay = document.getElementById("loginOverlay");
const app = document.getElementById("app");

function mostrarLogin() {
    loginOverlay.classList.remove("oculto");
    app.classList.add("oculto");
}

function iniciarAplicacion() {
    loginOverlay.classList.add("oculto");
    app.classList.remove("oculto");
    document.getElementById("usuarioActual").textContent = usuarioActual;

    document.getElementById("cierreFecha").value = new Date().toISOString().slice(0, 10);
    actualizarReloj();
    cargarTodo();
    setInterval(tick, 1000);
    setInterval(cargarTodo, 8000);
}

document.getElementById("formLogin").addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const username = document.getElementById("loginUsername").value.trim();
    const password = document.getElementById("loginPassword").value;

    try {
        const respuesta = await fetch(`${API}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
        });
        const cuerpo = await respuesta.json();
        if (!respuesta.ok) {
            throw new Error(cuerpo.message || "No se pudo iniciar sesion");
        }

        token = cuerpo.token;
        usuarioActual = cuerpo.username;
        sessionStorage.setItem("parqueadero-token", token);
        sessionStorage.setItem("parqueadero-usuario", usuarioActual);
        iniciarAplicacion();
    } catch (error) {
        mostrarMensaje("mensajeLogin", error.message, true);
    }
});

document.getElementById("btnLogout").addEventListener("click", cerrarSesion);

// ---------------------------------------------------------------------
// Arranque
// ---------------------------------------------------------------------

if (token) {
    iniciarAplicacion();
} else {
    mostrarLogin();
}
