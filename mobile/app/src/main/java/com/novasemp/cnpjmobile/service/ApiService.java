package com.novasemp.cnpjmobile.service;

import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.model.EmpresaResponse;
import com.novasemp.cnpjmobile.model.HistoricoBusca;
import com.novasemp.cnpjmobile.model.MlAnalysisRequest;
import com.novasemp.cnpjmobile.model.MlAnalysisResponse;
import com.novasemp.cnpjmobile.model.PredicaoResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("dashboard")
    Call<DashboardData> getDashboardData(
            @Query("cnae") String cnae,
            @Query("municipio") String municipio
    );

    @GET("empresas")
    Call<List<EmpresaResponse>> getEmpresas(
            @Query("cnae") String cnae,
            @Query("municipio") String municipio
    );

    @GET("empresas/count")
    Call<Integer> getEmpresasCount(
            @Query("cnae") String cnae,
            @Query("municipio") String municipio
    );

    @GET("empresas/avg-capital")
    Call<Double> getCapitalSocialMedio(
            @Query("cnae") String cnae,
            @Query("municipio") String municipio
    );

    // Endpoint específico para predição
    @GET("predicao")
    Call<PredicaoResponse> getPredicao(
            @Query("cnae") String cnae,
            @Query("municipio") String municipio,
            @Query("capital") double capitalSocial
    );

    // Histórico de buscas
    @POST("historico/salvar")
    Call<Void> salvarHistorico(@Body HistoricoBusca historico);

    @GET("historico/{sessionId}")
    Call<List<HistoricoBusca>> listarHistorico(@Path("sessionId") String sessionId);

    @DELETE("historico/{id}/{sessionId}")
    Call<Void> deletarHistorico(@Path("id") int id, @Path("sessionId") String sessionId);
}