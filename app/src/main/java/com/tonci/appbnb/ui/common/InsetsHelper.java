package com.tonci.appbnb.ui.common;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Manejo de insets para la UI edge-to-edge (obligatoria desde targetSdk 35). */
public final class InsetsHelper {

    private InsetsHelper() { }

    /**
     * Separa el contenido de las barras del sistema: {@code top} deja espacio a la barra de
     * estado, {@code bottom} a la barra de navegación o al teclado, lo que sea mayor.
     */
    public static void applySystemBars(@NonNull View root, @NonNull View top, @NonNull View bottom) {
        final int topPadding = top.getPaddingTop();
        final int bottomPadding = bottom.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, 0, bars.right, 0);
            top.setPadding(top.getPaddingLeft(), topPadding + bars.top,
                    top.getPaddingRight(), top.getPaddingBottom());
            bottom.setPadding(bottom.getPaddingLeft(), bottom.getPaddingTop(),
                    bottom.getPaddingRight(), bottomPadding + Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
