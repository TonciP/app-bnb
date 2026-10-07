package com.tonci.appbnb.data.remote.dto;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public final class VerificationResponse {

    @SerializedName("sessionId") @Nullable private String sessionId;

    @Nullable public String getSessionId() { return sessionId; }
}
