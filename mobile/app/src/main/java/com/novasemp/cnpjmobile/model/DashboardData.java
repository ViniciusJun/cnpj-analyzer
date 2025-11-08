package com.novasemp.cnpjmobile.model;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;

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

    @SerializedName("classificacao_sucesso")
    private String classificacaoSucesso;

    @SerializedName("fatores_criticos")
    private String[] fatoresCriticos;

    @SerializedName("recomendacao")
    private String recomendacao;

    @SerializedName("estrategias")
    private String[] estrategias;

    // Construtor
    public DashboardData() {}

    // Getters e Setters
    public int getQuantidadeEmpresas() {
        return quantidadeEmpresas;
    }

    public void setQuantidadeEmpresas(int quantidadeEmpresas) {
        this.quantidadeEmpresas = quantidadeEmpresas;
    }

    public double getCapitalSocialMedio() {
        return capitalSocialMedio;
    }

    public void setCapitalSocialMedio(double capitalSocialMedio) {
        this.capitalSocialMedio = capitalSocialMedio;
    }

    public String getCnae() {
        return cnae;
    }

    public void setCnae(String cnae) {
        this.cnae = cnae;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public double getProbabilidadeSucesso() {
        return probabilidadeSucesso;
    }

    public void setProbabilidadeSucesso(double probabilidadeSucesso) {
        this.probabilidadeSucesso = probabilidadeSucesso;
    }

    public String getClassificacaoSucesso() {
        return classificacaoSucesso;
    }

    public void setClassificacaoSucesso(String classificacaoSucesso) {
        this.classificacaoSucesso = classificacaoSucesso;
    }

    public String[] getFatoresCriticos() {
        return fatoresCriticos;
    }

    public void setFatoresCriticos(String[] fatoresCriticos) {
        this.fatoresCriticos = fatoresCriticos;
    }

    public String getRecomendacao() {
        return recomendacao;
    }

    public void setRecomendacao(String recomendacao) {
        this.recomendacao = recomendacao;
    }

    public String[] getEstrategias() {
        return estrategias;
    }

    public void setEstrategias(String[] estrategias) {
        this.estrategias = estrategias;
    }

    @Override
    public String toString() {
        return "DashboardData{" +
                "quantidadeEmpresas=" + quantidadeEmpresas +
                ", capitalSocialMedio=" + capitalSocialMedio +
                ", cnae='" + cnae + '\'' +
                ", municipio='" + municipio + '\'' +
                ", probabilidadeSucesso=" + probabilidadeSucesso +
                ", classificacaoSucesso='" + classificacaoSucesso + '\'' +
                ", fatoresCriticos=" + Arrays.toString(fatoresCriticos) +
                ", recomendacao='" + recomendacao + '\'' +
                ", estrategias=" + Arrays.toString(estrategias) +
                '}';
    }
}