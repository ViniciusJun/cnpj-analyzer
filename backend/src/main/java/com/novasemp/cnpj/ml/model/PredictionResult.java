package com.novasemp.cnpj.ml.model;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.Map;

public class PredictionResult {
    
    @SerializedName("probabilidadeSucesso")
    private double probabilidadeSucesso;
    
    @SerializedName("classificacao")
    private String classificacao;
    
    @SerializedName("fatoresCriticos")
    private String[] fatoresCriticos;
    
    @SerializedName("recomendacao")
    private String recomendacao;
    
    // ✅ NOVOS CAMPOS
    @SerializedName("metricasAvancadas")
    private Map<String, Double> metricasAvancadas;
    
    @SerializedName("modeloUtilizado")
    private String modeloUtilizado;
    
    @SerializedName("dadosReais")
    private boolean dadosReais;
    
    @SerializedName("confiancaModelo")
    private double confiancaModelo;
    
    public PredictionResult() {}
    
    public PredictionResult(double probabilidadeSucesso, String classificacao, String[] fatoresCriticos, String recomendacao) {
        this.probabilidadeSucesso = probabilidadeSucesso;
        this.classificacao = classificacao;
        this.fatoresCriticos = fatoresCriticos;
        this.recomendacao = recomendacao;
    }
    
    // Getters e Setters
    public double getProbabilidadeSucesso() { return probabilidadeSucesso; }
    public void setProbabilidadeSucesso(double probabilidadeSucesso) { this.probabilidadeSucesso = probabilidadeSucesso; }
    
    public String getClassificacao() { return classificacao; }
    public void setClassificacao(String classificacao) { this.classificacao = classificacao; }
    
    public String[] getFatoresCriticos() { return fatoresCriticos; }
    public void setFatoresCriticos(String[] fatoresCriticos) { this.fatoresCriticos = fatoresCriticos; }
    
    public String getRecomendacao() { return recomendacao; }
    public void setRecomendacao(String recomendacao) { this.recomendacao = recomendacao; }
    
    public Map<String, Double> getMetricasAvancadas() { return metricasAvancadas; }
    public void setMetricasAvancadas(Map<String, Double> metricasAvancadas) { this.metricasAvancadas = metricasAvancadas; }
    
    public String getModeloUtilizado() { return modeloUtilizado; }
    public void setModeloUtilizado(String modeloUtilizado) { this.modeloUtilizado = modeloUtilizado; }
    
    public boolean isDadosReais() { return dadosReais; }
    public void setDadosReais(boolean dadosReais) { this.dadosReais = dadosReais; }
    
    public double getConfiancaModelo() { return confiancaModelo; }
    public void setConfiancaModelo(double confiancaModelo) { this.confiancaModelo = confiancaModelo; }

    @Override
    public String toString() {
        return "PredictionResult{" +
                "probabilidadeSucesso=" + probabilidadeSucesso +
                ", classificacao='" + classificacao + '\'' +
                ", fatoresCriticos=" + Arrays.toString(fatoresCriticos) +
                ", recomendacao='" + recomendacao + '\'' +
                ", modeloUtilizado='" + modeloUtilizado + '\'' +
                ", dadosReais=" + dadosReais +
                ", confiancaModelo=" + confiancaModelo +
                '}';
    }
}