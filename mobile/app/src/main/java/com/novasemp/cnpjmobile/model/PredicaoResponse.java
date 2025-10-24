package com.novasemp.cnpjmobile.model;

import java.util.List;

public class PredicaoResponse {
    private boolean success;
    private String message;
    private PredicaoData data;

    // Metodo para debug
    public void logDebug() {
        System.out.println("DEBUG: PredicaoResponse - Success: " + success);
        System.out.println("DEBUG: PredicaoResponse - Message: " + message);
        System.out.println("DEBUG: PredicaoResponse - Data: " + data);
        if (data != null) {
            System.out.println("DEBUG: PredicaoResponse - Data.Probabilidade: " + data.getProbabilidadeSucesso());
            System.out.println("DEBUG: PredicaoResponse - Data.Classificacao: " + data.getClassificacao());
        }
    }
    
    // Getters e Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public PredicaoData getData() {
        return data;
    }
    
    public void setData(PredicaoData data) {
        this.data = data;
    }
    
    public static class PredicaoData {
        private double probabilidadeSucesso;
        private String classificacao;
        private List<String> fatoresPositivos;
        private List<String> fatoresNegativos;
        private String recomendacao;
        private int empresasSimilares;
        private double capitalMedio;
        
        // Getters e Setters
        public double getProbabilidadeSucesso() {
            return probabilidadeSucesso;
        }
        
        public void setProbabilidadeSucesso(double probabilidadeSucesso) {
            this.probabilidadeSucesso = probabilidadeSucesso;
        }
        
        public String getClassificacao() {
            return classificacao;
        }
        
        public void setClassificacao(String classificacao) {
            this.classificacao = classificacao;
        }
        
        public List<String> getFatoresPositivos() {
            return fatoresPositivos;
        }
        
        public void setFatoresPositivos(List<String> fatoresPositivos) {
            this.fatoresPositivos = fatoresPositivos;
        }
        
        public List<String> getFatoresNegativos() {
            return fatoresNegativos;
        }
        
        public void setFatoresNegativos(List<String> fatoresNegativos) {
            this.fatoresNegativos = fatoresNegativos;
        }
        
        public String getRecomendacao() {
            return recomendacao;
        }
        
        public void setRecomendacao(String recomendacao) {
            this.recomendacao = recomendacao;
        }
        
        public int getEmpresasSimilares() {
            return empresasSimilares;
        }
        
        public void setEmpresasSimilares(int empresasSimilares) {
            this.empresasSimilares = empresasSimilares;
        }
        
        public double getCapitalMedio() {
            return capitalMedio;
        }
        
        public void setCapitalMedio(double capitalMedio) {
            this.capitalMedio = capitalMedio;
        }
    }
}