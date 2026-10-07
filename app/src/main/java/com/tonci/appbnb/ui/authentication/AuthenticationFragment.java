package com.tonci.appbnb.ui.authentication;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tonci.appbnb.R;
import com.tonci.appbnb.databinding.FragmentAuthenticationBinding;
import com.tonci.appbnb.ui.common.InsetsHelper;
import com.tonci.appbnb.ui.common.StepHeader;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Paso 2: recomendaciones previas a la prueba de autenticación (con lectura en voz alta). */
public class AuthenticationFragment extends Fragment {

    private static final String UTTERANCE_ID = "auth_tip";

    private static final List<AuthTip> TIPS = Arrays.asList(
            new AuthTip(R.string.tip_lighting, R.drawable.illustration_lighting),
            new AuthTip(R.string.tip_accessories, R.drawable.illustration_accessories),
            new AuthTip(R.string.tip_camera, R.drawable.illustration_phone),
            new AuthTip(R.string.tip_frame, R.drawable.illustration_frame));

    private FragmentAuthenticationBinding binding;
    @Nullable private TextToSpeech textToSpeech;
    private boolean speechReady;

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
        StepHeader.bind(binding.header, 2, R.string.step_authentication, R.drawable.ic_face_scan,
                () -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        binding.tipsPager.setAdapter(new AuthTipsAdapter(TIPS));
        new TabLayoutMediator(binding.tipsDots, binding.tipsPager, (tab, position) -> { }).attach();
        binding.tipsPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                stopSpeaking(); // la lectura corresponde a la recomendación visible
            }
        });

        initTextToSpeech();
        binding.listenButton.setOnClickListener(v -> speakCurrentTip());
        binding.nextButton.setOnClickListener(v -> onNextClicked());
    }

    /** Avanza por las recomendaciones; al terminar, el paso 3 queda fuera del alcance. */
    private void onNextClicked() {
        int current = binding.tipsPager.getCurrentItem();
        if (current < TIPS.size() - 1) {
            binding.tipsPager.setCurrentItem(current + 1, true);
        } else {
            showMessage(R.string.message_next_step_pending);
        }
    }

    // ---- Lectura en voz alta ----

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (textToSpeech == null) return; // el fragment ya fue destruido
            speechReady = status == TextToSpeech.SUCCESS
                    && textToSpeech.setLanguage(new Locale("es")) >= TextToSpeech.LANG_AVAILABLE;
        });
    }

    private void speakCurrentTip() {
        if (!speechReady || textToSpeech == null) {
            showMessage(R.string.message_tts_unavailable);
            return;
        }
        AuthTip tip = TIPS.get(binding.tipsPager.getCurrentItem());
        String text = getString(R.string.authentication_intro) + " " + getString(tip.getText());
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID);
    }

    private void stopSpeaking() {
        if (textToSpeech != null) textToSpeech.stop();
    }

    private void showMessage(int messageRes) {
        Snackbar.make(binding.getRoot(), messageRes, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.nextButton).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        binding = null;
    }
}
