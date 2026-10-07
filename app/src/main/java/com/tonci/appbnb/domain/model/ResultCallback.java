package com.tonci.appbnb.domain.model;

/** Callback de operaciones asíncronas. Siempre se invoca en el hilo principal. */
public interface ResultCallback<T> {

    void onSuccess(T result);

    void onError(AppError error);
}
