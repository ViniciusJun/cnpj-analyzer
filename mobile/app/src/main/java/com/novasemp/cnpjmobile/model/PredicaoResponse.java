package com.novasemp.cnpjmobile.model;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;

public class PredicaoResponse {

    @SerializedName("sucesso")
    private boolean sucesso;

    @SerializedName("scoreML")
    private int scoreML;

    @SerializedName("probabilidadeSucesso")
    private double probabilidadeSucesso;

    @SerializedName("modeloUtilizado")
    private String modeloUtilizado;

    @SerializedName("classificacao")
    private String classificacao;

    @SerializedName("fatoresCriticos")
    private String[] fatoresCriticos;

    @SerializedName("recomendacao")
    private String recomendacao;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("mlOnline")
    private boolean mlOnline;

    // Construtor
    public PredicaoResponse() {}

    // Getters e Setters
    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public int getScoreML() {
        return scoreML;
    }

    public void setScoreML(int scoreML) {
        this.scoreML = scoreML;
    }

    public double getProbabilidadeSucesso() {
        return probabilidadeSucesso;
    }

    public void setProbabilidadeSucesso(double probabilidadeSucesso) {
        this.probabilidadeSucesso = probabilidadeSucesso;
    }

    public String getModeloUtilizado() {
        return modeloUtilizado;
    }

    public void setModeloUtilizado(String modeloUtilizado) {
        this.modeloUtilizado = modeloUtilizado;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public void setClassificacao(String classificacao) {
        this.classificacao = classificacao;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMlOnline() {
        return mlOnline;
    }

    public void setMlOnline(boolean mlOnline) {
        this.mlOnline = mlOnline;
    }

    @Override
    public String toString() {
        return "PredicaoResponse{" +
                "sucesso=" + sucesso +
                ", scoreML=" + scoreML +
                ", probabilidadeSucesso=" + probabilidadeSucesso +
                ", modeloUtilizado='" + modeloUtilizado + '\'' +
                ", classificacao='" + classificacao + '\'' +
                ", fatoresCriticos=" + Arrays.toString(fatoresCriticos) +
                ", recomendacao='" + recomendacao + '\'' +
                ", timestamp=" + timestamp +
                ", mlOnline=" + mlOnline +
                '}';
    }
}