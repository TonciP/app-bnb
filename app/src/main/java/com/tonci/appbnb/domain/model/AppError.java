package com.tonci.appbnb.domain.model;

/** Error de negocio/infraestructura independiente de Android; la UI decide cómo mostrarlo. */
public final class AppError {

    public enum Type {
        NETWORK,
        SERVER,
        LOCATION_DISABLED,
        LOCATION_UNAVAILABLE,
        PERMISSION_DENIED,
        UNKNOWN
    }

    private final Type type;

    public AppError(Type type) {
        this.type = type;
    }

    public Type getType() { return type; }
}
