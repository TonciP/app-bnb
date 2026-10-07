package com.tonci.appbnb.ui.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tonci.appbnb.databinding.SheetLocationPermissionBinding;

/**
 * Explica por qué se necesita la ubicación antes del diálogo del sistema. Avisa al fragment
 * padre con la Fragment Result API cuando el usuario pulsa "Continuar".
 */
public class LocationPermissionBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "LocationPermissionBottomSheet";
    public static final String REQUEST_KEY = "location_permission_continue";

    private SheetLocationPermissionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetLocationPermissionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.continueButton.setOnClickListener(v -> {
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, Bundle.EMPTY);
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
