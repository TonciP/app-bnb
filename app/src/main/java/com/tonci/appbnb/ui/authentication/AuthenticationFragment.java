package com.tonci.appbnb.ui.authentication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tonci.appbnb.R;
import com.tonci.appbnb.databinding.FragmentAuthenticationBinding;
import com.tonci.appbnb.ui.common.InsetsHelper;

import java.util.Arrays;
import java.util.List;

/** Paso 2 de 3: recomendaciones previas a la prueba de autenticación. */
public class AuthenticationFragment extends Fragment {

    private static final List<AuthTip> TIPS = Arrays.asList(
            new AuthTip(R.string.tip_lighting, R.drawable.ic_illustration_light),
            new AuthTip(R.string.tip_accessories, R.drawable.ic_illustration_face),
            new AuthTip(R.string.tip_camera, R.drawable.ic_illustration_phone));

    private FragmentAuthenticationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAuthenticationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        InsetsHelper.applySystemBars(binding.getRoot(), binding.header.getRoot(), binding.bottomBar);

        binding.header.stepLabel.setText(getString(R.string.step_label_format, 2, 3));
        binding.header.stepTitle.setText(R.string.step_authentication);
        binding.header.stepIcon.setImageResource(R.drawable.ic_face);
        binding.header.backButton.setOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        binding.tipsPager.setAdapter(new AuthTipsAdapter(TIPS));
        new TabLayoutMediator(binding.tipsDots, binding.tipsPager, (tab, position) -> { }).attach();

        binding.nextButton.setOnClickListener(v -> onNextClicked());
    }

    /** Avanza por las recomendaciones; al terminar, el paso 3 queda fuera del alcance. */
    private void onNextClicked() {
        int current = binding.tipsPager.getCurrentItem();
        if (current < TIPS.size() - 1) {
            binding.tipsPager.setCurrentItem(current + 1, true);
        } else {
            Snackbar.make(binding.getRoot(), R.string.message_next_step_pending,
                    Snackbar.LENGTH_SHORT).setAnchorView(binding.nextButton).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
