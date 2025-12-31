package goksoft.chat.app.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Application configuration for API clients and services
 */
public class AppConfig {

    private static final String BASE_URL = "http://localhost:8080/api/";

    private static AppConfig instance;
    private final Retrofit retrofit;
    private final OkHttpClient httpClient;

    private AppConfig() {
        // HTTP logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Build OkHttp client
        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Configure Gson
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // Build Retrofit
        this.retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }
}