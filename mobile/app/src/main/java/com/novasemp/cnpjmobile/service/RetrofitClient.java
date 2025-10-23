package com.novasemp.cnpjmobile.service;

import com.novasemp.cnpjmobile.util.Constants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String BASE_URL = Constants.BASE_URL;
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        System.out.println("DEBUG: RetrofitClient - Iniciando getClient()");
        System.out.println("DEBUG: RetrofitClient - BASE_URL: " + BASE_URL);

        if (retrofit == null) {
            try {
                System.out.println("DEBUG: RetrofitClient - Criando nova instância do Retrofit");

                // Configurar interceptor para logging detalhado
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                System.out.println("DEBUG: RetrofitClient - Interceptor de logging configurado");

                OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                httpClient.addInterceptor(logging);
                httpClient.connectTimeout(60, TimeUnit.SECONDS);
                httpClient.readTimeout(60, TimeUnit.SECONDS);
                httpClient.writeTimeout(60, TimeUnit.SECONDS);
                System.out.println("DEBUG: RetrofitClient - OkHttpClient configurado");

                // Adicionar interceptor para headers
                httpClient.addInterceptor(chain -> {
                    System.out.println("DEBUG: RetrofitClient - Adicionando headers à requisição");
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("User-Agent", "CNPJAnalyzer-Android")
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                });

                System.out.println("DEBUG: RetrofitClient - Construindo Retrofit...");
                retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(httpClient.build())
                        .build();

                System.out.println("DEBUG: RetrofitClient - Retrofit construído com SUCESSO");

            } catch (Exception e) {
                System.out.println("DEBUG: RetrofitClient - ERRO ao construir Retrofit: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Falha ao criar cliente HTTP", e);
            }
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        System.out.println("DEBUG: RetrofitClient - getApiService() chamado");
        try {
            ApiService service = getClient().create(ApiService.class);
            System.out.println("DEBUG: RetrofitClient - ApiService criado com SUCESSO");
            return service;
        } catch (Exception e) {
            System.out.println("DEBUG: RetrofitClient - ERRO ao criar ApiService: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}