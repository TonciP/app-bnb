package com.tonci.appbnb.domain.repository;

import androidx.annotation.NonNull;

import com.tonci.appbnb.domain.model.Cancellable;
import com.tonci.appbnb.domain.model.DeviceLocation;
import com.tonci.appbnb.domain.model.ResultCallback;
import com.tonci.appbnb.domain.model.VerificationForm;
import com.tonci.appbnb.domain.model.VerificationSession;

public interface VerificationRepository {

    @NonNull
    Cancellable startVerification(@NonNull VerificationForm form,
                                  @NonNull DeviceLocation location,
                                  @NonNull ResultCallback<VerificationSession> callback);
}
