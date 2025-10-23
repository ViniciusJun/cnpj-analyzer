package com.novasemp.cnpjmobile.model;

import java.util.List;

public class MlAnalysisResponse {
    private boolean success;
    private String message;
    private AnalysisData data;
    
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
    
    public AnalysisData getData() {
        return data;
    }
    
    public void setData(AnalysisData data) {
        this.data = data;
    }
    
    public static class AnalysisData {
        private int totalEmpresas;
        private double capitalMedio;
        private double densidadeEmpresarial;
        private String nivelConcorrencia;
        private double taxaCrescimento;
        private List<String> recomendacoes;
        private String potencialMercado;
        
        // Getters e Setters
        public int getTotalEmpresas() {
            return totalEmpresas;
        }
        
        public void setTotalEmpresas(int totalEmpresas) {
            this.totalEmpresas = totalEmpresas;
        }
        
        public double getCapitalMedio() {
            return capitalMedio;
        }
        
        public void setCapitalMedio(double capitalMedio) {
            this.capitalMedio = capitalMedio;
        }
        
        public double getDensidadeEmpresarial() {
            return densidadeEmpresarial;
        }
        
        public void setDensidadeEmpresarial(double densidadeEmpresarial) {
            this.densidadeEmpresarial = densidadeEmpresarial;
        }
        
        public String getNivelConcorrencia() {
            return nivelConcorrencia;
        }
        
        public void setNivelConcorrencia(String nivelConcorrencia) {
            this.nivelConcorrencia = nivelConcorrencia;
        }
        
        public double getTaxaCrescimento() {
            return taxaCrescimento;
        }
        
        public void setTaxaCrescimento(double taxaCrescimento) {
            this.taxaCrescimento = taxaCrescimento;
        }
        
        public List<String> getRecomendacoes() {
            return recomendacoes;
        }
        
        public void setRecomendacoes(List<String> recomendacoes) {
            this.recomendacoes = recomendacoes;
        }
        
        public String getPotencialMercado() {
            return potencialMercado;
        }
        
        public void setPotencialMercado(String potencialMercado) {
            this.potencialMercado = potencialMercado;
        }
    }
}