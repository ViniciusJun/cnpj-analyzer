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
    
    // Método para converter features em array para o modelo (CORRIGIDO)
    public double[] toFeatureArray() {
        // Converter CNAE e município para valores numéricos de forma segura
        double cnaeNumerico = 0.0;
        double municipioNumerico = 0.0;
        
        try {
            // Extrair parte numérica do CNAE (ex: "4721102" -> 4721102.0)
            String cnaeClean = cnaePrincipal.replaceAll("[^0-9]", "");
            if (!cnaeClean.isEmpty()) {
                cnaeNumerico = Double.parseDouble(cnaeClean);
            } else {
                cnaeNumerico = (double) cnaePrincipal.hashCode();
            }
            
            // Converter município para hash numérico
            municipioNumerico = (double) municipio.hashCode();
        } catch (NumberFormatException e) {
            System.err.println("Erro na conversão de CNAE: " + cnaePrincipal);
            cnaeNumerico = (double) cnaePrincipal.hashCode();
            municipioNumerico = (double) municipio.hashCode();
        }
        
        return new double[] {
            cnaeNumerico,
            municipioNumerico,
            capitalSocial,
            (double) quantidadeEmpresasRegiao,
            capitalMedioRegiao,
            densidadeEmpresarial,
            (double) faixaCapitalSocial
        };
    }
}