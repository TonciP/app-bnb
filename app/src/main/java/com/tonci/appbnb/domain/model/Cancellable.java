package com.tonci.appbnb.domain.model;

/** Handle para cancelar una operación asíncrona en curso (por ejemplo al destruirse el ViewModel). */
public interface Cancellable {

    Cancellable NONE = () -> { };

    void cancel();
}
