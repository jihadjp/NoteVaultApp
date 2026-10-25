package com.jptechgenius.notevault.data.remote;

import com.jptechgenius.notevault.data.remote.api.ApiService;
import com.jptechgenius.notevault.utils.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit client for API communication.
 */
public class RetrofitClient {

    private static Retrofit retrofit;
    private static ApiService apiService;
    private static OkHttpClient sharedHttpClient;

    private RetrofitClient() {}

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }

    public static OkHttpClient getHttpClient() {
        if (sharedHttpClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            // Use BASIC level to avoid logging sensitive bodies or large binary streams
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            sharedHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)
                    .writeTimeout(45, TimeUnit.SECONDS)
                    .build();
        }
        return sharedHttpClient;
    }

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(getHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Reset the client (useful for changing base URL in tests).
     */
    public static void reset() {
        retrofit = null;
        apiService = null;
        sharedHttpClient = null;
    }
}
