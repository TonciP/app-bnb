package com.tonci.appbnb.data.remote;

import com.tonci.appbnb.data.remote.dto.VerificationRequest;
import com.tonci.appbnb.data.remote.dto.VerificationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/v1/verification/start")
    Call<VerificationResponse> startVerification(@Body VerificationRequest request);
}
