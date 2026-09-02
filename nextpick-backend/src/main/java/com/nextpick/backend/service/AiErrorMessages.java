package com.nextpick.backend.service;

import java.util.Locale;

/** Mensajes seguros y estables que pueden mostrarse directamente en la interfaz. */
public final class AiErrorMessages {
    public static final String NOT_CONFIGURED = "El servicio de IA no está disponible en este momento.";
    public static final String HIGH_DEMAND = "Estamos experimentando una alta demanda. Inténtalo de nuevo más tarde.";
    public static final String RATE_LIMIT = "Se ha alcanzado temporalmente el límite del servicio de IA. Inténtalo más tarde.";
    public static final String TIMEOUT = "El servicio de IA está tardando más de lo esperado. Inténtalo de nuevo.";
    public static final String TEMPORARY = "El servicio de IA no está disponible temporalmente. Inténtalo de nuevo más tarde.";
    public static final String INVALID_REQUEST = "No se pudo procesar la consulta. Prueba a reformularla.";
    public static final String SAFETY = "No podemos responder a esa consulta. Prueba con otra petición sobre cine o series.";
    public static final String INCOMPLETE = "La respuesta no pudo completarse. Inténtalo de nuevo.";
    public static final String TOO_LONG = "La respuesta era demasiado larga y no pudo completarse. Prueba con una consulta más concreta.";
    public static final String UNEXPECTED = "No se pudo completar la respuesta del asistente. Inténtalo de nuevo más tarde.";

    private AiErrorMessages() {}

    static String temporaryProviderFailure(String internalMessage) {
        if (internalMessage == null) return TEMPORARY;
        String normalized = internalMessage.toLowerCase(Locale.ROOT);
        return normalized.contains("high demand")
                || normalized.contains("overloaded")
                || normalized.contains("capacity")
                ? HIGH_DEMAND
                : TEMPORARY;
    }
}
