package com.tonci.appbnb.ui.information;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/** Acciones de una sola vez que el ViewModel le pide a la pantalla de información. */
public final class InformationEvent {

    public enum Type {
        /** Explicar y solicitar el permiso de ubicación (bottom sheet + diálogo del sistema). */
        REQUEST_LOCATION_PERMISSION,
        /** Mostrar un mensaje simple. */
        SHOW_MESSAGE,
        /** Mensaje con acción hacia los ajustes de la app (permiso denegado definitivamente). */
        SHOW_APP_SETTINGS_MESSAGE,
        /** Mensaje con acción hacia los ajustes de ubicación del sistema (GPS apagado). */
        SHOW_LOCATION_SETTINGS_MESSAGE,
        NAVIGATE_TO_AUTHENTICATION
    }

    @NonNull private final Type type;
    @StringRes private final int messageRes;

    private InformationEvent(@NonNull Type type, @StringRes int messageRes) {
        this.type = type;
        this.messageRes = messageRes;
    }

    static InformationEvent of(@NonNull Type type) {
        return new InformationEvent(type, 0);
    }

    static InformationEvent of(@NonNull Type type, @StringRes int messageRes) {
        return new InformationEvent(type, messageRes);
    }

    @NonNull public Type getType() { return type; }

    @StringRes public int getMessageRes() { return messageRes; }
}
