package com.tonci.appbnb.ui.information;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.tonci.appbnb.R;
import com.tonci.appbnb.domain.model.AppError;
import com.tonci.appbnb.domain.model.Cancellable;
import com.tonci.appbnb.domain.model.DeviceLocation;
import com.tonci.appbnb.domain.model.ResultCallback;
import com.tonci.appbnb.domain.model.VerificationForm;
import com.tonci.appbnb.domain.model.VerificationSession;
import com.tonci.appbnb.domain.repository.LocationProvider;
import com.tonci.appbnb.domain.repository.VerificationRepository;
import com.tonci.appbnb.domain.validation.ValidationError;
import com.tonci.appbnb.ui.common.Event;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class InformationViewModelTest {

    @Rule public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();

    private FakeLocationProvider location;
    private FakeRepository repository;
    private InformationViewModel viewModel;
    private final List<InformationEvent> events = new ArrayList<>();

    @Before
    public void setUp() {
        location = new FakeLocationProvider();
        repository = new FakeRepository();
        viewModel = new InformationViewModel(repository, location);
        viewModel.getEvents().observeForever(new Observer<Event<InformationEvent>>() {
            @Override
            public void onChanged(Event<InformationEvent> event) {
                InformationEvent content = event.getContentIfNotHandled();
                if (content != null) events.add(content);
            }
        });
    }

    @Test
    public void invalidForm_showsErrors_andDoesNothingElse() {
        viewModel.onNextClicked("123", "", "", true);

        assertEquals(ValidationError.INVALID_LENGTH, viewModel.getFormErrors().getValue().getPhone());
        assertEquals(ValidationError.REQUIRED, viewModel.getFormErrors().getValue().getIdentityCard());
        assertEquals(0, location.calls);
        assertEquals(0, repository.calls);
        assertTrue(events.isEmpty());
    }

    @Test
    public void validForm_withoutPermission_requestsPermission_beforeCallingService() {
        viewModel.onNextClicked("71234567", "1234567890", "", false);

        assertEquals(1, events.size());
        assertEquals(InformationEvent.Type.REQUEST_LOCATION_PERMISSION, events.get(0).getType());
        assertEquals(0, location.calls);
        assertEquals(0, repository.calls);
    }

    @Test
    public void permissionGranted_afterRequest_consumesService_andNavigates() {
        viewModel.onNextClicked("71234567", "1234567890", "1A", false);
        events.clear();

        viewModel.onLocationPermissionResult(true, true);
        location.succeed(-16.5, -68.15);
        repository.succeed("session-1");

        assertEquals(1, repository.calls);
        assertEquals("71234567", repository.lastForm.getPhoneNumber());
        assertEquals("1A", repository.lastForm.getComplement());
        assertEquals(-16.5, repository.lastLocation.getLatitude(), 0.0);
        assertFalse(viewModel.isLoading().getValue());
        assertEquals(1, events.size());
        assertEquals(InformationEvent.Type.NAVIGATE_TO_AUTHENTICATION, events.get(0).getType());
    }

    @Test
    public void permissionAlreadyGranted_skipsPermissionRequest() {
        viewModel.onNextClicked("71234567", "1234567890", "", true);

        assertEquals(1, location.calls);
        assertTrue(viewModel.isLoading().getValue());
        assertTrue(events.isEmpty());
    }

    @Test
    public void permissionDenied_canAskAgain_showsMessage_andNeverCallsService() {
        viewModel.onNextClicked("71234567", "1234567890", "", false);
        events.clear();

        viewModel.onLocationPermissionResult(false, true);

        assertEquals(InformationEvent.Type.SHOW_MESSAGE, events.get(0).getType());
        assertEquals(R.string.message_location_denied, events.get(0).getMessageRes());
        assertEquals(0, location.calls);
        assertEquals(0, repository.calls);
    }

    @Test
    public void permissionDeniedPermanently_offersAppSettings() {
        viewModel.onNextClicked("71234567", "1234567890", "", false);
        events.clear();

        viewModel.onLocationPermissionResult(false, false);

        assertEquals(InformationEvent.Type.SHOW_APP_SETTINGS_MESSAGE, events.get(0).getType());
        assertEquals(0, repository.calls);
    }

    @Test
    public void locationServicesOff_offersLocationSettings() {
        viewModel.onNextClicked("71234567", "1234567890", "", true);
        location.fail(AppError.Type.LOCATION_DISABLED);

        assertEquals(InformationEvent.Type.SHOW_LOCATION_SETTINGS_MESSAGE, events.get(0).getType());
        assertFalse(viewModel.isLoading().getValue());
        assertEquals(0, repository.calls);
    }

    @Test
    public void networkError_showsMessage_andStopsLoading() {
        viewModel.onNextClicked("71234567", "1234567890", "", true);
        location.succeed(0, 0);
        repository.fail(AppError.Type.NETWORK);

        assertEquals(InformationEvent.Type.SHOW_MESSAGE, events.get(0).getType());
        assertEquals(R.string.message_network, events.get(0).getMessageRes());
        assertFalse(viewModel.isLoading().getValue());
    }

    @Test
    public void tappingNextWhileLoading_isIgnored() {
        viewModel.onNextClicked("71234567", "1234567890", "", true);
        viewModel.onNextClicked("71234567", "1234567890", "", true);

        assertEquals(1, location.calls);
    }

    @Test
    public void onCleared_cancelsRunningOperation() {
        viewModel.onNextClicked("71234567", "1234567890", "", true);
        assertNotNull(location.cancellable);

        viewModel.onCleared(); // protected: accesible por estar en el mismo paquete

        assertTrue(location.cancellable.cancelled);
    }

    // ---- Fakes ----

    private static final class FakeCancellable implements Cancellable {
        boolean cancelled;

        @Override
        public void cancel() {
            cancelled = true;
        }
    }

    private static final class FakeLocationProvider implements LocationProvider {
        int calls;
        FakeCancellable cancellable;
        private ResultCallback<DeviceLocation> callback;

        @NonNull
        @Override
        public Cancellable getCurrentLocation(@NonNull ResultCallback<DeviceLocation> callback) {
            calls++;
            this.callback = callback;
            cancellable = new FakeCancellable();
            return cancellable;
        }

        void succeed(double lat, double lon) {
            callback.onSuccess(new DeviceLocation(lat, lon));
        }

        void fail(AppError.Type type) {
            callback.onError(new AppError(type));
        }
    }

    private static final class FakeRepository implements VerificationRepository {
        int calls;
        VerificationForm lastForm;
        DeviceLocation lastLocation;
        private ResultCallback<VerificationSession> callback;

        @NonNull
        @Override
        public Cancellable startVerification(@NonNull VerificationForm form,
                                             @NonNull DeviceLocation location,
                                             @NonNull ResultCallback<VerificationSession> callback) {
            calls++;
            lastForm = form;
            lastLocation = location;
            this.callback = callback;
            return new FakeCancellable();
        }

        void succeed(String sessionId) {
            callback.onSuccess(new VerificationSession(sessionId));
        }

        void fail(AppError.Type type) {
            callback.onError(new AppError(type));
        }
    }
}
