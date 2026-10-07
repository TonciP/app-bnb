package com.tonci.appbnb.di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.tonci.appbnb.BuildConfig;
import com.tonci.appbnb.data.location.AndroidLocationProvider;
import com.tonci.appbnb.data.remote.ApiClient;
import com.tonci.appbnb.data.repository.VerificationRepositoryImpl;
import com.tonci.appbnb.domain.repository.LocationProvider;
import com.tonci.appbnb.domain.repository.VerificationRepository;
import com.tonci.appbnb.ui.information.InformationViewModel;

/**
 * Inyección de dependencias manual: crea las dependencias una vez y las entrega a los
 * ViewModels. Para esta cantidad de clases evita el costo de Hilt/Dagger.
 */
public final class AppContainer {

    private final VerificationRepository verificationRepository;
    private final LocationProvider locationProvider;

    public AppContainer(@NonNull Context context) {
        verificationRepository = new VerificationRepositoryImpl(
                ApiClient.create(BuildConfig.BASE_URL, BuildConfig.USE_MOCK_API));
        locationProvider = new AndroidLocationProvider(context);
    }

    @NonNull
    public ViewModelProvider.Factory getViewModelFactory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(InformationViewModel.class)) {
                    return (T) new InformationViewModel(verificationRepository, locationProvider);
                }
                throw new IllegalArgumentException("ViewModel desconocido: " + modelClass.getName());
            }
        };
    }
}
