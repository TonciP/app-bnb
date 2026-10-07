package com.tonci.appbnb.domain.model;

import androidx.annotation.NonNull;

/** Sesión de verificación creada por el servicio; la usarán los pasos siguientes del flujo. */
public final class VerificationSession {

    @NonNull private final String id;

    public VerificationSession(@NonNull String id) {
        this.id = id;
    }

    @NonNull public String getId() { return id; }
}
