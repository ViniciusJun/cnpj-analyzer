package com.novasemp.cnpj.ml.model;

public class EmpresaFeatures {
    private String cnaePrincipal;
    private String municipio;
    private double capitalSocial;
    private int quantidadeEmpresasRegiao;
    private double capitalMedioRegiao;
    private double densidadeEmpresarial;
    private int faixaCapitalSocial; // 0: Baixo, 1: Médio, 2: Alto
    
    public EmpresaFeatures() {}
    
    public EmpresaFeatures(String cnaePrincipal, String municipio, double capitalSocial, 
                          int quantidadeEmpresasRegiao, double capitalMedioRegiao, 
                          double densidadeEmpresarial, int faixaCapitalSocial) {
        this.cnaePrincipal = cnaePrincipal;
        this.municipio = municipio;
        this.capitalSocial = capitalSocial;
        this.quantidadeEmpresasRegiao = quantidadeEmpresasRegiao;
        this.capitalMedioRegiao = capitalMedioRegiao;
        this.densidadeEmpresarial = densidadeEmpresarial;
        this.faixaCapitalSocial = faixaCapitalSocial;
    }
    
    // Getters e Setters
    public String getCnaePrincipal() { return cnaePrincipal; }
    public void setCnaePrincipal(String cnaePrincipal) { this.cnaePrincipal = cnaePrincipal; }
    
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    
    public double getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(double capitalSocial) { this.capitalSocial = capitalSocial; }
    
    public int getQuantidadeEmpresasRegiao() { return quantidadeEmpresasRegiao; }
    public void setQuantidadeEmpresasRegiao(int quantidadeEmpresasRegiao) { this.quantidadeEmpresasRegiao = quantidadeEmpresasRegiao; }
    
    public double getCapitalMedioRegiao() { return capitalMedioRegiao; }
    public void setCapitalMedioRegiao(double capitalMedioRegiao) { this.capitalMedioRegiao = capitalMedioRegiao; }
    
    public double getDensidadeEmpresarial() { return densidadeEmpresarial; }
    public void setDensidadeEmpresarial(double densidadeEmpresarial) { this.densidadeEmpresarial = densidadeEmpresarial; }
    
    public int getFaixaCapitalSocial() { return faixaCapitalSocial; }
    public void setFaixaCapitalSocial(int faixaCapitalSocial) { this.faixaCapitalSocial = faixaCapitalSocial; }
    
    // Método para converter features em array para o modelo
    public double[] toFeatureArray() {
        return new double[] {
            Double.parseDouble(cnaePrincipal),
            Double.parseDouble(municipio),
            capitalSocial,
            quantidadeEmpresasRegiao,
            capitalMedioRegiao,
            densidadeEmpresarial,
            faixaCapitalSocial
        };
    }
}
