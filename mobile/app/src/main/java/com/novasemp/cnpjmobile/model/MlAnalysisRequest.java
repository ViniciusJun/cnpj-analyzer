package com.novasemp.cnpjmobile.model;

public class MlAnalysisRequest {

    private String cnae;
    private String municipio;
    private String uf;
    
    public MlAnalysisRequest(String cnae, String municipio, String uf) {
        this.cnae = cnae;
        this.municipio = municipio;
        this.uf = uf;
    }

    // Getters e Setters
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
    
    public String getUf() {
        return uf;
    }
    
    public void setUf(String uf) {
        this.uf = uf;
    }
}
