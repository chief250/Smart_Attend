package com.smartattend.app.api;

import com.smartattend.app.BuildConfig;
import com.smartattend.app.utils.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static SessionManager sessionManager;

    public static void init(SessionManager sm) {
        sessionManager = sm;
    }

    public static Retrofit getClient() {
        String baseUrl = (sessionManager != null) ? sessionManager.getBaseUrl() : BuildConfig.BASE_URL;
        if (retrofit != null && !retrofit.baseUrl().toString().equals(baseUrl)) {
            retrofit = null;
        }
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            // Never log request bodies: login requests contain passwords and other endpoints
            // may contain face enrollment data.
            loggingInterceptor.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BASIC
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("Content-Type", "application/json")
                                    .header("Accept", "application/json");

                            if (sessionManager != null) {
                                String token = sessionManager.getToken();
                                if (token != null && !token.isEmpty()) {
                                    requestBuilder.header("Authorization", "Bearer " + token);
                                }
                            }

                            return chain.proceed(requestBuilder.build());
                        }
                    })
                    .addInterceptor(loggingInterceptor);

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }

    /**
     * Reset client — call when base URL or token changes.
     */
    public static void reset() {
        retrofit = null;
    }
}
