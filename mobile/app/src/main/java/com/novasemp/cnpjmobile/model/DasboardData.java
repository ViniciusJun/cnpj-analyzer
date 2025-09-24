package com.novasemp.cnpjmobile.model;

import com.google.gson.annotations.SerializedName;

public class DashboardData {
    @SerializedName("quantidade_empresas")
    private int quantidadeEmpresas;

    @SerializedName("capital_social_medio")
    private double capitalSocialMedio;

    @SerializedName("cnae")
    private String cnae;

    @SerializedName("municipio")
    private String municipio;

    @SerializedName("probabilidade_sucesso")
    private double probabilidadeSucesso;

    @SerializedName("estrategias")
    private String[] estrategias;

    // Getters e Setters
    public int getQuantidadeEmpresas() { return quantidadeEmpresas; }
    public void setQuantidadeEmpresas(int quantidadeEmpresas) { this.quantidadeEmpresas = quantidadeEmpresas; }

    public double getCapitalSocialMedio() { return capitalSocialMedio; }
    public void setCapitalSocialMedio(double capitalSocialMedio) { this.capitalSocialMedio = capitalSocialMedio; }

    public String getCnae() { return cnae; }
    public void setCnae(String cnae) { this.cnae = cnae; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public double getProbabilidadeSucesso() { return probabilidadeSucesso; }
    public void setProbabilidadeSucesso(double probabilidadeSucesso) { this.probabilidadeSucesso = probabilidadeSucesso; }

    public String[] getEstrategias() { return estrategias; }
    public void setEstrategias(String[] estrategias) { this.estrategias = estrategias; }
}