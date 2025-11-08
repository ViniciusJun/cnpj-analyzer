package com.novasemp.cnpjmobile.service;

import com.novasemp.cnpjmobile.model.PredicaoResponse;
import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.model.HistoricoBusca;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // ✅ DASHBOARD - CORRIGIDO: Adicionar parâmetros missing
    @GET("dashboard")
    Call<DashboardData> getDashboardData(
            @Query("cnae") String cnae,
            @Query("municipio") String municipio,
            @Query("capital") Double capitalSocial,      // ✅ ADICIONADO
            @Query("sessionId") String sessionId         // ✅ ADICIONADO
    );

    // ✅ HISTÓRICO - CORRIGIDO
    @POST("historico")
    Call<Void> salvarHistorico(@Body HistoricoBusca historico);

    @GET("historico")
    Call<List<HistoricoBusca>> listarHistorico(@Query("sessionId") String sessionId);

    @DELETE("historico")
    Call<Void> deletarHistorico(
            @Query("id") Integer id,
            @Query("sessionId") String sessionId
    );

    // ✅ ML PREDIÇÃO - CORRIGIDO
    @GET("ml/predicao")
    Call<PredicaoResponse> getPredicaoML(
            @Query("cnae") String cnae,
            @Query("municipio") String municipio,
            @Query("capital_social") Double capitalSocial
    );

    // ✅ ANÁLISE DE MERCADO - CORRIGIDO: Usar Object pois estrutura é diferente
    @GET("ml/analise-mercado")
    Call<Object> getAnaliseMercado(
            @Query("cnae") String cnae,
            @Query("municipio") String municipio
    );

    // ✅ TENDÊNCIA SETOR - CORRIGIDO: Usar Object
    @GET("ml/tendencia-setor")
    Call<Object> getTendenciaSetor(
            @Query("cnae") String cnae
    );

    // ✅ FEEDBACK - CORRIGIDO
    @POST("ml/feedback")
    Call<Void> enviarFeedback(
            @Query("cnae") String cnae,
            @Query("municipio") String municipio,
            @Query("capital_social") Double capitalSocial,
            @Query("probabilidade_real") Double probabilidadeReal,
            @Query("sucesso_real") Boolean sucessoReal
    );

    // ✅ HEALTH CHECK - CORRIGIDO: Usar Object
    @GET("health")
    Call<Object> healthCheck();

    // ✅ DEBUG ML - NOVO
    @GET("debug/ml")
    Call<Object> debugML();
}