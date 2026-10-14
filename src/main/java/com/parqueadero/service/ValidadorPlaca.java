package com.parqueadero.service;

import java.util.regex.Pattern;

import com.parqueadero.model.TipoVehiculo;

/**
 * Formatos reales: carro AAA111, moto AAA11A, bicicleta 3 caracteres alfanumericos
 * (los 3 primeros del serial del cuadro).
 */
public final class ValidadorPlaca {

    private static final Pattern PATRON_CARRO = Pattern.compile("^[A-Z]{3}[0-9]{3}$");
    private static final Pattern PATRON_MOTO = Pattern.compile("^[A-Z]{3}[0-9]{2}[A-Z]$");
    private static final Pattern PATRON_BICICLETA = Pattern.compile("^[A-Z0-9]{3}$");

    private ValidadorPlaca() {
    }

    public static void validarParaTipo(String placaNormalizada, TipoVehiculo tipoVehiculo) {
        switch (tipoVehiculo) {
            case CARRO -> exigir(PATRON_CARRO.matcher(placaNormalizada).matches(),
                    "La placa de un carro debe tener el formato AAA111 (3 letras y 3 numeros)");
            case MOTO -> exigir(PATRON_MOTO.matcher(placaNormalizada).matches(),
                    "La placa de una moto debe tener el formato AAA11A (3 letras, 2 numeros y 1 letra)");
            case BICICLETA -> exigir(PATRON_BICICLETA.matcher(placaNormalizada).matches(),
                    "El serial de la bicicleta debe tener 3 caracteres alfanumericos");
        }
    }

    public static boolean esAlgunFormatoValido(String placaNormalizada) {
        return PATRON_CARRO.matcher(placaNormalizada).matches()
                || PATRON_MOTO.matcher(placaNormalizada).matches()
                || PATRON_BICICLETA.matcher(placaNormalizada).matches();
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
