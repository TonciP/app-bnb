package com.tonci.appbnb.data.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;

import com.tonci.appbnb.domain.model.AppError;
import com.tonci.appbnb.domain.model.Cancellable;
import com.tonci.appbnb.domain.model.DeviceLocation;
import com.tonci.appbnb.domain.model.ResultCallback;
import com.tonci.appbnb.domain.repository.LocationProvider;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Obtiene la ubicación con el {@link LocationManager} de la plataforma (sin depender de Google
 * Play Services). Si no llega una lectura a tiempo, usa la última ubicación conocida.
 */
public final class AndroidLocationProvider implements LocationProvider {

    private static final long TIMEOUT_MS = 15_000;

    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AndroidLocationProvider(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    @SuppressLint("MissingPermission") // Se verifica con hasPermission() antes de cada acceso.
    @NonNull
    @Override
    public Cancellable getCurrentLocation(@NonNull ResultCallback<DeviceLocation> callback) {
        boolean fine = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean coarse = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (!fine && !coarse) {
            callback.onError(new AppError(AppError.Type.PERMISSION_DENIED));
            return Cancellable.NONE;
        }

        LocationManager manager =
                (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null || !LocationManagerCompat.isLocationEnabled(manager)) {
            callback.onError(new AppError(AppError.Type.LOCATION_DISABLED));
            return Cancellable.NONE;
        }

        String provider = chooseProvider(manager, fine);
        if (provider == null) {
            callback.onError(new AppError(AppError.Type.LOCATION_UNAVAILABLE));
            return Cancellable.NONE;
        }

        AtomicBoolean finished = new AtomicBoolean(false);
        CancellationSignal signal = new CancellationSignal();

        Runnable onTimeout = () -> {
            if (!finished.compareAndSet(false, true)) return;
            signal.cancel();
            deliver(lastKnown(manager, provider), callback);
        };
        mainHandler.postDelayed(onTimeout, TIMEOUT_MS);

        try {
            LocationManagerCompat.getCurrentLocation(manager, provider, signal,
                    ContextCompat.getMainExecutor(appContext), location -> {
                        if (!finished.compareAndSet(false, true)) return;
                        mainHandler.removeCallbacks(onTimeout);
                        deliver(location != null ? location : lastKnown(manager, provider),
                                callback);
                    });
        } catch (SecurityException e) {
            // El permiso pudo revocarse entre la verificación y la llamada.
            if (finished.compareAndSet(false, true)) {
                mainHandler.removeCallbacks(onTimeout);
                callback.onError(new AppError(AppError.Type.PERMISSION_DENIED));
            }
        }

        return () -> {
            if (finished.compareAndSet(false, true)) {
                mainHandler.removeCallbacks(onTimeout);
                signal.cancel();
            }
        };
    }

    /** GPS solo con permiso preciso; la red sirve también con permiso aproximado. */
    @Nullable
    private static String chooseProvider(LocationManager manager, boolean fine) {
        boolean gps = fine && manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps) return LocationManager.GPS_PROVIDER;
        if (network) return LocationManager.NETWORK_PROVIDER;
        return null;
    }

    @SuppressLint("MissingPermission")
    @Nullable
    private static Location lastKnown(LocationManager manager, String provider) {
        try {
            return manager.getLastKnownLocation(provider);
        } catch (SecurityException e) {
            return null;
        }
    }

    private static void deliver(@Nullable Location location,
                                ResultCallback<DeviceLocation> callback) {
        if (location == null) {
            callback.onError(new AppError(AppError.Type.LOCATION_UNAVAILABLE));
        } else {
            callback.onSuccess(new DeviceLocation(location.getLatitude(), location.getLongitude()));
        }
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(appContext, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
}
