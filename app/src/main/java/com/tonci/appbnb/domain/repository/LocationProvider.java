package com.tonci.appbnb.domain.repository;

import androidx.annotation.NonNull;

import com.tonci.appbnb.domain.model.Cancellable;
import com.tonci.appbnb.domain.model.DeviceLocation;
import com.tonci.appbnb.domain.model.ResultCallback;

public interface LocationProvider {

    /** Obtiene la ubicación actual. Requiere que el permiso de ubicación ya esté concedido. */
    @NonNull
    Cancellable getCurrentLocation(@NonNull ResultCallback<DeviceLocation> callback);
}
