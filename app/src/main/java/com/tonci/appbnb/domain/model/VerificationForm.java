package com.tonci.appbnb.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Datos personales ya validados que ingresa el usuario en el paso "Información". */
public final class VerificationForm {

    @NonNull private final String phoneNumber;
    @NonNull private final String identityCard;
    @Nullable private final String complement;

    public VerificationForm(@NonNull String phoneNumber,
                            @NonNull String identityCard,
                            @Nullable String complement) {
        this.phoneNumber = phoneNumber;
        this.identityCard = identityCard;
        this.complement = (complement == null || complement.isEmpty()) ? null : complement;
    }

    @NonNull public String getPhoneNumber() { return phoneNumber; }

    @NonNull public String getIdentityCard() { return identityCard; }

    /** @return el complemento o {@code null} si el usuario no lo ingresó. */
    @Nullable public String getComplement() { return complement; }
}
