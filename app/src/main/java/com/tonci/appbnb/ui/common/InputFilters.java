package com.tonci.appbnb.ui.common;

import android.text.InputFilter;

/**
 * Filtros de entrada. A diferencia de {@code android:digits}, también depuran texto pegado o
 * enviado por el teclado, y se combinan con el límite de longitud en un único lugar.
 */
public final class InputFilters {

    private InputFilters() { }

    /** Solo dígitos 0-9, hasta {@code maxLength}. */
    public static InputFilter[] digits(int maxLength) {
        return new InputFilter[] {
                keepOnly(InputFilters::isAsciiDigit),
                new InputFilter.LengthFilter(maxLength)
        };
    }

    /** Solo letras A-Z y dígitos (sin tildes ni símbolos), en mayúsculas, hasta {@code maxLength}. */
    public static InputFilter[] alphanumericUpperCase(int maxLength) {
        return new InputFilter[] {
                keepOnly(InputFilters::isAsciiLetterOrDigit),
                new InputFilter.AllCaps(),
                new InputFilter.LengthFilter(maxLength)
        };
    }

    private interface CharPredicate {
        boolean test(char c);
    }

    private static InputFilter keepOnly(CharPredicate allowed) {
        return (source, start, end, dest, dstart, dend) -> {
            StringBuilder kept = null;
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (allowed.test(c)) {
                    if (kept != null) kept.append(c);
                } else if (kept == null) {
                    kept = new StringBuilder(source.subSequence(start, i));
                }
            }
            // null = aceptar el texto tal cual; "" o parcial = reemplazarlo por lo permitido.
            return kept;
        };
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAsciiLetterOrDigit(char c) {
        return isAsciiDigit(c) || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
}
