package com.tonci.appbnb.ui.information;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

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
import com.tonci.appbnb.App;
import com.tonci.appbnb.R;
import com.tonci.appbnb.databinding.FragmentInformationBinding;
import com.tonci.appbnb.domain.validation.FormErrors;
import com.tonci.appbnb.domain.validation.FormValidator;
import com.tonci.appbnb.domain.validation.ValidationError;
import com.tonci.appbnb.ui.common.InputFilters;
import com.tonci.appbnb.ui.common.InsetsHelper;
import com.tonci.appbnb.ui.common.StepHeader;
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
        StepHeader.bind(binding.header, 1, R.string.step_information, R.drawable.ic_add,
                () -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupInputs() {
        binding.phoneInput.setFilters(InputFilters.digits(FormValidator.PHONE_LENGTH));
        binding.identityCardInput.setFilters(
                InputFilters.digits(FormValidator.IDENTITY_CARD_LENGTH));
        binding.complementInput.setFilters(
                InputFilters.alphanumericUpperCase(FormValidator.COMPLEMENT_LENGTH));

        clearErrorOnEdit(binding.phoneInput, binding.phoneError);
        clearErrorOnEdit(binding.identityCardInput, binding.identityCardError);
        clearErrorOnEdit(binding.complementInput, binding.complementError);

        binding.hasComplementCheck.setOnCheckedChangeListener(
                (button, checked) -> setComplementVisible(checked));
        setComplementVisible(binding.hasComplementCheck.isChecked());

        binding.identityCardInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v);
                return true;
            }
            return false;
        });
        binding.complementInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onNextClicked();
                return true;
            }
            return false;
        });
    }

    /** El complemento es opcional: solo se muestra, valida y envía si el usuario lo indica. */
    private void setComplementVisible(boolean visible) {
        binding.complementGroup.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) {
            binding.complementInput.setText("");
            showError(binding.complementError, null);
        }
    }

    private void onNextClicked() {
        hideKeyboard(binding.nextButton);
        viewModel.onNextClicked(
                textOf(binding.phoneInput),
                textOf(binding.identityCardInput),
                binding.hasComplementCheck.isChecked() ? textOf(binding.complementInput) : "",
                hasLocationPermission());
    }

    // ---- Render ----

    private void renderErrors(FormErrors errors) {
        showError(binding.phoneError, numericErrorText(errors.getPhone(),
                R.string.error_phone_required, R.string.error_phone_length));
        showError(binding.identityCardError, numericErrorText(errors.getIdentityCard(),
                R.string.error_identity_card_required, R.string.error_identity_card_length));
        showError(binding.complementError, complementErrorText(errors.getComplement()));
    }

    private static void showError(TextView view, @Nullable String message) {
        view.setText(message);
        view.setVisibility(message == null ? View.GONE : View.VISIBLE);
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
        binding.phoneInput.setEnabled(!loading);
        binding.identityCardInput.setEnabled(!loading);
        binding.complementInput.setEnabled(!loading);
        binding.hasComplementCheck.setEnabled(!loading);
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

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static void clearErrorOnEdit(EditText input, TextView errorView) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (errorView.getVisibility() == View.VISIBLE) showError(errorView, null);
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
