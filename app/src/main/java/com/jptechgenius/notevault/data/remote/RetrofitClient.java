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

    private RetrofitClient() {}

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Logging interceptor (only active in debug builds)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
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
    }
}
