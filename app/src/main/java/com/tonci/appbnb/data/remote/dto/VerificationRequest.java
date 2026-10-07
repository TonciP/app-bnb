package com.tonci.appbnb.data.remote.dto;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public final class VerificationRequest {

    @SerializedName("phoneNumber") private final String phoneNumber;
    @SerializedName("identityCard") private final String identityCard;
    @SerializedName("complement") @Nullable private final String complement;
    @SerializedName("latitude") private final double latitude;
    @SerializedName("longitude") private final double longitude;

    public VerificationRequest(String phoneNumber, String identityCard, @Nullable String complement,
                               double latitude, double longitude) {
        this.phoneNumber = phoneNumber;
        this.identityCard = identityCard;
        this.complement = complement;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
