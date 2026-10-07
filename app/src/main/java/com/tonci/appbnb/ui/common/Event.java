package com.tonci.appbnb.ui.common;

import androidx.annotation.Nullable;

/**
 * Envuelve un valor de LiveData que debe consumirse una sola vez (navegación, mensajes), para que
 * no se repita al rotar la pantalla o al re-suscribirse el observador.
 */
public final class Event<T> {

    private final T content;
    private boolean handled;

    public Event(T content) {
        this.content = content;
    }

    /** @return el contenido la primera vez; {@code null} en las siguientes. */
    @Nullable
    public T getContentIfNotHandled() {
        if (handled) return null;
        handled = true;
        return content;
    }
}
