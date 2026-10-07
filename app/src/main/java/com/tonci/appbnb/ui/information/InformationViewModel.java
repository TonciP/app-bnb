package com.tonci.appbnb.ui.information;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tonci.appbnb.R;
import com.tonci.appbnb.domain.model.AppError;
import com.tonci.appbnb.domain.model.Cancellable;
import com.tonci.appbnb.domain.model.DeviceLocation;
import com.tonci.appbnb.domain.model.ResultCallback;
import com.tonci.appbnb.domain.model.VerificationForm;
import com.tonci.appbnb.domain.model.VerificationSession;
import com.tonci.appbnb.domain.repository.LocationProvider;
import com.tonci.appbnb.domain.repository.VerificationRepository;
import com.tonci.appbnb.domain.validation.FormErrors;
import com.tonci.appbnb.domain.validation.FormValidator;
import com.tonci.appbnb.ui.common.Event;

/**
 * Estado y lógica del paso "Información": valida el formulario, exige el permiso de ubicación
 * antes de consumir el servicio y, con la ubicación, inicia la verificación.
 *
 * <p>No conoce Views ni Context: el permiso lo consulta/solicita el Fragment y se lo informa aquí.
 */
public class InformationViewModel extends ViewModel {

    private final VerificationRepository repository;
    private final LocationProvider locationProvider;

    private final MutableLiveData<FormErrors> formErrors = new MutableLiveData<>(FormErrors.NONE);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<InformationEvent>> events = new MutableLiveData<>();

    /** Formulario validado a la espera de que se conceda el permiso de ubicación. */
    @Nullable private VerificationForm pendingForm;
    @Nullable private Cancellable runningOperation;

    public InformationViewModel(@NonNull VerificationRepository repository,
                                @NonNull LocationProvider locationProvider) {
        this.repository = repository;
        this.locationProvider = locationProvider;
    }

    @NonNull public LiveData<FormErrors> getFormErrors() { return formErrors; }

    @NonNull public LiveData<Boolean> isLoading() { return loading; }

    @NonNull public LiveData<Event<InformationEvent>> getEvents() { return events; }

    public void onNextClicked(@Nullable String phone, @Nullable String identityCard,
                              @Nullable String complement, boolean hasLocationPermission) {
        if (Boolean.TRUE.equals(loading.getValue())) return; // evita doble envío

        FormErrors errors = FormValidator.validate(phone, identityCard, complement);
        formErrors.setValue(errors);
        if (errors.hasErrors()) return;

        pendingForm = new VerificationForm(phone, identityCard, complement);
        if (hasLocationPermission) {
            startVerification();
        } else {
            emit(InformationEvent.of(InformationEvent.Type.REQUEST_LOCATION_PERMISSION));
        }
    }

    /**
     * @param granted     si alguno de los permisos de ubicación quedó concedido
     * @param canAskAgain si el sistema todavía mostraría el diálogo (false = denegado para siempre)
     */
    public void onLocationPermissionResult(boolean granted, boolean canAskAgain) {
        if (granted) {
            startVerification();
        } else if (canAskAgain) {
            emit(InformationEvent.of(InformationEvent.Type.SHOW_MESSAGE,
                    R.string.message_location_denied));
        } else {
            emit(InformationEvent.of(InformationEvent.Type.SHOW_APP_SETTINGS_MESSAGE,
                    R.string.message_location_denied_permanently));
        }
    }

    private void startVerification() {
        final VerificationForm form = pendingForm;
        if (form == null) return;

        loading.setValue(true);
        runningOperation = locationProvider.getCurrentLocation(new ResultCallback<DeviceLocation>() {
            @Override
            public void onSuccess(DeviceLocation location) {
                runningOperation = repository.startVerification(form, location,
                        new ResultCallback<VerificationSession>() {
                            @Override
                            public void onSuccess(VerificationSession session) {
                                pendingForm = null;
                                finishLoading();
                                emit(InformationEvent.of(
                                        InformationEvent.Type.NAVIGATE_TO_AUTHENTICATION));
                            }

                            @Override
                            public void onError(AppError error) {
                                fail(error);
                            }
                        });
            }

            @Override
            public void onError(AppError error) {
                fail(error);
            }
        });
    }

    private void fail(@NonNull AppError error) {
        finishLoading();
        switch (error.getType()) {
            case LOCATION_DISABLED:
                emit(InformationEvent.of(InformationEvent.Type.SHOW_LOCATION_SETTINGS_MESSAGE,
                        R.string.message_location_disabled));
                break;
            case PERMISSION_DENIED:
                emit(InformationEvent.of(InformationEvent.Type.SHOW_APP_SETTINGS_MESSAGE,
                        R.string.message_location_denied_permanently));
                break;
            default:
                emit(InformationEvent.of(InformationEvent.Type.SHOW_MESSAGE, messageFor(error)));
        }
    }

    @StringRes
    private static int messageFor(@NonNull AppError error) {
        switch (error.getType()) {
            case LOCATION_UNAVAILABLE: return R.string.message_location_unavailable;
            case NETWORK: return R.string.message_network;
            case SERVER: return R.string.message_server;
            default: return R.string.message_unknown;
        }
    }

    private void finishLoading() {
        runningOperation = null;
        loading.setValue(false);
    }

    private void emit(@NonNull InformationEvent event) {
        events.setValue(new Event<>(event));
    }

    @Override
    protected void onCleared() {
        if (runningOperation != null) runningOperation.cancel();
    }
}
