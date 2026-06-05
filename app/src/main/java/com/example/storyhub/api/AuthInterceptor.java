package com.example.storyhub.api;

import com.example.storyhub.utils.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import org.json.JSONObject;

public class AuthInterceptor implements Interceptor {

    private TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenManager.getAccessToken();

        Request request = original.newBuilder()
                .header("Authorization", "Bearer " + (token != null ? token : ""))
                .build();

        Response response = chain.proceed(request);

        if (response.code() == 401) {
            response.close();

            String refreshToken = tokenManager.getRefreshToken();
            if (refreshToken == null) return response;

            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject body = new JSONObject();
                body.put("refresh_token", refreshToken);

                Request refreshRequest = new Request.Builder()
                        .url(Config.BASE_URL + ":4097/refresh")
                        .post(RequestBody.create(
                                body.toString(),
                                MediaType.get("application/json; charset=utf-8")
                        ))
                        .build();

                Response refreshResponse = client.newCall(refreshRequest).execute();

                if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                    String refreshBody = refreshResponse.body().string();
                    refreshResponse.close();

                    JSONObject json = new JSONObject(refreshBody);
                    String newAccessToken = json.getString("access_token");

                    tokenManager.saveTokens(newAccessToken, refreshToken);

                    Request retryRequest = original.newBuilder()
                            .header("Authorization", "Bearer " + newAccessToken)
                            .build();

                    return chain.proceed(retryRequest);

                } else {
                    refreshResponse.close();
                    tokenManager.clear();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return response;
    }
}