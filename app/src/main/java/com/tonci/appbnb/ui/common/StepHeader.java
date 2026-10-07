package com.tonci.appbnb.ui.common;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.tonci.appbnb.R;
import com.tonci.appbnb.databinding.ViewStepHeaderBinding;

/** Encabezado compartido por las pantallas del flujo: título, paso actual y progreso. */
public final class StepHeader {

    public static final int TOTAL_STEPS = 6;

    private StepHeader() { }

    public static void bind(@NonNull ViewStepHeaderBinding header, int step,
                            @StringRes int title, @DrawableRes int icon,
                            @NonNull Runnable onBack) {
        Context context = header.getRoot().getContext();
        header.toolbarTitle.setText(title);
        header.stepLabel.setText(context.getString(R.string.step_label_format, step, TOTAL_STEPS));
        header.stepTitle.setText(title);
        header.stepIcon.setImageResource(icon);
        header.stepProgress.setMax(TOTAL_STEPS);
        header.stepProgress.setProgressCompat(step, false);
        header.backButton.setOnClickListener(v -> onBack.run());
    }
}
