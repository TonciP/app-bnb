package com.tonci.appbnb.data.remote;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Responde localmente con un JSON válido para poder ejecutar el flujo completo sin backend.
 * Solo se instala cuando {@code BuildConfig.USE_MOCK_API} es verdadero.
 */
public final class MockApiInterceptor implements Interceptor {

    private static final long LATENCY_MS = 800;

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        try {
            Thread.sleep(LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Mock interrumpido", e);
        }
        String json = "{\"sessionId\":\"" + UUID.randomUUID() + "\"}";
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(json, MediaType.get("application/json")))
                .build();
    }
}
