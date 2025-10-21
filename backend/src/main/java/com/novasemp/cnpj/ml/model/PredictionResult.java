package com.novasemp.cnpj.ml.model;

public class PredictionResult {
    private double probabilidadeSucesso;
    private String classificacao; // ALTA, MEDIA, BAIXA
    private String[] fatoresCriticos;
    private String recomendacao;
    
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
}