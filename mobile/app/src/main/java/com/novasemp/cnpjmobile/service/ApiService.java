package com.novasemp.cnpjmobile.service;

import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.model.EmpresaResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
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
}