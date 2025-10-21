package com.novasemp.cnpjmobile.service;

import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.model.EmpresaResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    
    // ATUALIZADO: Agora o dashboard pode receber o parâmetro capital opcional
    @GET("dashboard")
    Call<DashboardData> getDashboardData(
        @Query("cnae") String cnae,
        @Query("municipio") String municipio
        // O parâmetro 'capital' é opcional na API, então não precisamos incluí-lo aqui
        // A API usará o capital médio se não for fornecido
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
    
    // NOVO: Endpoint específico para predição (se quiser usar separadamente)
    @GET("predicao")
    Call<Object> getPredicao(
        @Query("cnae") String cnae,
        @Query("municipio") String municipio,
        @Query("capital") double capitalSocial
    );
}