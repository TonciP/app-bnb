package com.tonci.appbnb.ui.information;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.tonci.appbnb.App;
import com.tonci.appbnb.R;
import com.tonci.appbnb.databinding.FragmentInformationBinding;
import com.tonci.appbnb.domain.validation.FormErrors;
import com.tonci.appbnb.domain.validation.FormValidator;
import com.tonci.appbnb.domain.validation.ValidationError;
import com.tonci.appbnb.ui.common.InputFilters;
import com.tonci.appbnb.ui.common.InsetsHelper;
import com.tonci.appbnb.ui.location.LocationPermissionBottomSheet;

/** Paso 1 de 3: captura celular, carnet y complemento, y asegura el permiso de ubicación. */
public class InformationFragment extends Fragment {

    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FragmentInformationBinding binding;
    private InformationViewModel viewModel;

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> viewModel.onLocationPermissionResult(
                    hasLocationPermission(), canAskLocationPermissionAgain()));

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App app = (App) requireActivity().getApplication();
        viewModel = new ViewModelProvider(this, app.getContainer().getViewModelFactory())
                .get(InformationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInformationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        InsetsHelper.applySystemBars(binding.getRoot(), binding.header.getRoot(), binding.bottomBar);
        setupHeader();
        setupInputs();
        binding.nextButton.setOnClickListener(v -> onNextClicked());

        getChildFragmentManager().setFragmentResultListener(
                LocationPermissionBottomSheet.REQUEST_KEY, getViewLifecycleOwner(),
                (key, bundle) -> permissionLauncher.launch(LOCATION_PERMISSIONS));

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), this::renderErrors);
        viewModel.isLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        viewModel.getEvents().observe(getViewLifecycleOwner(), event -> {
            InformationEvent content = event.getContentIfNotHandled();
            if (content != null) handleEvent(content);
        });
    }

    private void setupHeader() {
        binding.header.stepLabel.setText(getString(R.string.step_label_format, 1, 3));
        binding.header.stepTitle.setText(R.string.step_information);
        binding.header.stepIcon.setImageResource(R.drawable.ic_person);
        binding.header.backButton.setOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupInputs() {
        binding.phoneInput.setFilters(InputFilters.digits(FormValidator.PHONE_LENGTH));
        binding.identityCardInput.setFilters(
                InputFilters.digits(FormValidator.IDENTITY_CARD_LENGTH));
        binding.complementInput.setFilters(
                InputFilters.alphanumericUpperCase(FormValidator.COMPLEMENT_LENGTH));

        clearErrorOnEdit(binding.phoneInput, binding.phoneLayout);
        clearErrorOnEdit(binding.identityCardInput, binding.identityCardLayout);
        clearErrorOnEdit(binding.complementInput, binding.complementLayout);

        binding.complementInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onNextClicked();
                return true;
            }
            return false;
        });
    }

    private void onNextClicked() {
        viewModel.onNextClicked(
                textOf(binding.phoneInput),
                textOf(binding.identityCardInput),
                textOf(binding.complementInput),
                hasLocationPermission());
    }

    // ---- Render ----

    private void renderErrors(FormErrors errors) {
        binding.phoneLayout.setError(numericErrorText(errors.getPhone(),
                R.string.error_phone_required, R.string.error_phone_length));
        binding.identityCardLayout.setError(numericErrorText(errors.getIdentityCard(),
                R.string.error_identity_card_required, R.string.error_identity_card_length));
        binding.complementLayout.setError(complementErrorText(errors.getComplement()));
    }

    @Nullable
    private String numericErrorText(@Nullable ValidationError error,
                                    @StringRes int required, @StringRes int length) {
        if (error == null) return null;
        switch (error) {
            case REQUIRED: return getString(required);
            case INVALID_FORMAT: return getString(R.string.error_digits_only);
            default: return getString(length);
        }
    }

    @Nullable
    private String complementErrorText(@Nullable ValidationError error) {
        if (error == null) return null;
        return getString(error == ValidationError.INVALID_FORMAT
                ? R.string.error_alphanumeric_only
                : R.string.error_complement_length);
    }

    private void renderLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        binding.nextButton.setEnabled(!loading);
        binding.phoneLayout.setEnabled(!loading);
        binding.identityCardLayout.setEnabled(!loading);
        binding.complementLayout.setEnabled(!loading);
    }

    // ---- Eventos del ViewModel ----

    private void handleEvent(@NonNull InformationEvent event) {
        switch (event.getType()) {
            case REQUEST_LOCATION_PERMISSION:
                showLocationBottomSheet();
                break;
            case SHOW_MESSAGE:
                showSnackbar(event.getMessageRes(), 0, null);
                break;
            case SHOW_APP_SETTINGS_MESSAGE:
                showSnackbar(event.getMessageRes(), R.string.action_settings, this::openAppSettings);
                break;
            case SHOW_LOCATION_SETTINGS_MESSAGE:
                showSnackbar(event.getMessageRes(), R.string.action_settings,
                        this::openLocationSettings);
                break;
            case NAVIGATE_TO_AUTHENTICATION:
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_information_to_authentication);
                break;
        }
    }

    private void showLocationBottomSheet() {
        if (getChildFragmentManager().isStateSaved()
                || getChildFragmentManager().findFragmentByTag(LocationPermissionBottomSheet.TAG) != null) {
            return;
        }
        new LocationPermissionBottomSheet()
                .show(getChildFragmentManager(), LocationPermissionBottomSheet.TAG);
    }

    private void showSnackbar(@StringRes int message, @StringRes int actionLabel,
                              @Nullable Runnable action) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.nextButton);
        if (action != null) {
            snackbar.setAction(actionLabel, v -> action.run());
        }
        snackbar.show();
    }

    private void openAppSettings() {
        startSettingsActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireContext().getPackageName(), null)));
    }

    private void openLocationSettings() {
        startSettingsActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    private void startSettingsActivity(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
            // Dispositivo sin pantalla de ajustes: el mensaje ya explicó qué hacer.
        }
    }

    // ---- Permisos ----

    /** Basta con uno: el usuario puede elegir ubicación "aproximada" en el diálogo del sistema. */
    private boolean hasLocationPermission() {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    /** Tras una denegación, false significa "No volver a preguntar" (denegado definitivamente). */
    private boolean canAskLocationPermissionAgain() {
        for (String permission : LOCATION_PERMISSIONS) {
            if (shouldShowRequestPermissionRationale(permission)) return true;
        }
        return false;
    }

    // ---- Utilidades de vista ----

    private static String textOf(EditText editText) {
        Editable text = editText.getText();
        return text == null ? "" : text.toString();
    }

    private static void clearErrorOnEdit(EditText input, TextInputLayout layout) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.getError() != null) layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
