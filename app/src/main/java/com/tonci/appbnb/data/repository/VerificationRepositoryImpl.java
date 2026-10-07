package com.tonci.appbnb.data.repository;

import androidx.annotation.NonNull;

import com.tonci.appbnb.data.remote.ApiService;
import com.tonci.appbnb.data.remote.dto.VerificationRequest;
import com.tonci.appbnb.data.remote.dto.VerificationResponse;
import com.tonci.appbnb.domain.model.AppError;
import com.tonci.appbnb.domain.model.Cancellable;
import com.tonci.appbnb.domain.model.DeviceLocation;
import com.tonci.appbnb.domain.model.ResultCallback;
import com.tonci.appbnb.domain.model.VerificationForm;
import com.tonci.appbnb.domain.model.VerificationSession;
import com.tonci.appbnb.domain.repository.VerificationRepository;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class VerificationRepositoryImpl implements VerificationRepository {

    private final ApiService api;

    public VerificationRepositoryImpl(ApiService api) {
        this.api = api;
    }

    @NonNull
    @Override
    public Cancellable startVerification(@NonNull VerificationForm form,
                                         @NonNull DeviceLocation location,
                                         @NonNull ResultCallback<VerificationSession> callback) {
        VerificationRequest request = new VerificationRequest(
                form.getPhoneNumber(),
                form.getIdentityCard(),
                form.getComplement(),
                location.getLatitude(),
                location.getLongitude());

        Call<VerificationResponse> call = api.startVerification(request);
        call.enqueue(new Callback<VerificationResponse>() {
            @Override
            public void onResponse(@NonNull Call<VerificationResponse> call,
                                   @NonNull Response<VerificationResponse> response) {
                VerificationResponse body = response.body();
                if (response.isSuccessful() && body != null && body.getSessionId() != null) {
                    callback.onSuccess(new VerificationSession(body.getSessionId()));
                } else {
                    callback.onError(new AppError(AppError.Type.SERVER));
                }
            }

            @Override
            public void onFailure(@NonNull Call<VerificationResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                callback.onError(new AppError(
                        t instanceof IOException ? AppError.Type.NETWORK : AppError.Type.UNKNOWN));
            }
        });
        return call::cancel;
    }
}
