package com.novasemp.cnpj.ml.model;

public class EmpresaFeatures {
    private String cnaePrincipal;
    private String municipio;
    private double capitalSocial;
    private int quantidadeEmpresasRegiao;
    private double capitalMedioRegiao;
    private double densidadeEmpresarial;
    private int faixaCapitalSocial;
    
    // ✅ NOVAS FEATURES AVANÇADAS
    private double variacaoCapital;
    private int totalMunicipio;
    private int totalSetor;
    private double capitalVsMedia;
    private double concentracaoMercado;
    private double scoreLocalizacao;
    private double nivelConcorrencia;
    private double maturidadeMercado;
    
    public EmpresaFeatures() {}
    
    // Construtor básico
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
        
        // Calcular features avançadas automaticamente
        calcularFeaturesAvancadas();
    }
    
    private void calcularFeaturesAvancadas() {
        // Capital vs Média
        this.capitalVsMedia = this.capitalSocial / (this.capitalMedioRegiao + 0.001);
        
        // Concentração de Mercado
        this.concentracaoMercado = Math.min(this.quantidadeEmpresasRegiao / 100.0, 1.0);
        
        // Score de Localização (baseado no hash do município)
        this.scoreLocalizacao = (double) (Math.abs(this.municipio.hashCode()) % 100) / 100.0;
        
        // Nível de Concorrência
        this.nivelConcorrencia = Math.min(this.quantidadeEmpresasRegiao * this.densidadeEmpresarial / 50.0, 1.0);
        
        // Maturidade do Mercado (baseado no CNAE)
        this.maturidadeMercado = (double) (Math.abs(this.cnaePrincipal.hashCode()) % 100) / 100.0;
    }
    
    // Getters e Setters para campos básicos
    public String getCnaePrincipal() { return cnaePrincipal; }
    public void setCnaePrincipal(String cnaePrincipal) { 
        this.cnaePrincipal = cnaePrincipal; 
        calcularFeaturesAvancadas();
    }
    
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { 
        this.municipio = municipio; 
        calcularFeaturesAvancadas();
    }
    
    public double getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(double capitalSocial) { 
        this.capitalSocial = capitalSocial; 
        calcularFeaturesAvancadas();
    }
    
    public int getQuantidadeEmpresasRegiao() { return quantidadeEmpresasRegiao; }
    public void setQuantidadeEmpresasRegiao(int quantidadeEmpresasRegiao) { 
        this.quantidadeEmpresasRegiao = quantidadeEmpresasRegiao; 
        calcularFeaturesAvancadas();
    }
    
    public double getCapitalMedioRegiao() { return capitalMedioRegiao; }
    public void setCapitalMedioRegiao(double capitalMedioRegiao) { 
        this.capitalMedioRegiao = capitalMedioRegiao; 
        calcularFeaturesAvancadas();
    }
    
    public double getDensidadeEmpresarial() { return densidadeEmpresarial; }
    public void setDensidadeEmpresarial(double densidadeEmpresarial) { 
        this.densidadeEmpresarial = densidadeEmpresarial; 
        calcularFeaturesAvancadas();
    }
    
    public int getFaixaCapitalSocial() { return faixaCapitalSocial; }
    public void setFaixaCapitalSocial(int faixaCapitalSocial) { this.faixaCapitalSocial = faixaCapitalSocial; }
    
    // ✅ NOVOS GETTERS E SETTERS PARA FEATURES AVANÇADAS
    public double getVariacaoCapital() { return variacaoCapital; }
    public void setVariacaoCapital(double variacaoCapital) { this.variacaoCapital = variacaoCapital; }
    
    public int getTotalMunicipio() { return totalMunicipio; }
    public void setTotalMunicipio(int totalMunicipio) { this.totalMunicipio = totalMunicipio; }
    
    public int getTotalSetor() { return totalSetor; }
    public void setTotalSetor(int totalSetor) { this.totalSetor = totalSetor; }
    
    public double getCapitalVsMedia() { return capitalVsMedia; }
    public void setCapitalVsMedia(double capitalVsMedia) { this.capitalVsMedia = capitalVsMedia; }
    
    public double getConcentracaoMercado() { return concentracaoMercado; }
    public void setConcentracaoMercado(double concentracaoMercado) { this.concentracaoMercado = concentracaoMercado; }
    
    public double getScoreLocalizacao() { return scoreLocalizacao; }
    public void setScoreLocalizacao(double scoreLocalizacao) { this.scoreLocalizacao = scoreLocalizacao; }
    
    public double getNivelConcorrencia() { return nivelConcorrencia; }
    public void setNivelConcorrencia(double nivelConcorrencia) { this.nivelConcorrencia = nivelConcorrencia; }
    
    public double getMaturidadeMercado() { return maturidadeMercado; }
    public void setMaturidadeMercado(double maturidadeMercado) { this.maturidadeMercado = maturidadeMercado; }
    
    // Método para converter features em array para o modelo (ATUALIZADO)
    public double[] toFeatureArray() {
        // Converter CNAE e município para valores numéricos de forma segura
        double cnaeNumerico = 0.0;
        double municipioNumerico = 0.0;
        
        try {
            String cnaeClean = cnaePrincipal.replaceAll("[^0-9]", "");
            if (!cnaeClean.isEmpty()) {
                cnaeNumerico = Double.parseDouble(cnaeClean);
            } else {
                cnaeNumerico = (double) cnaePrincipal.hashCode();
            }
            
            municipioNumerico = (double) municipio.hashCode();
        } catch (NumberFormatException e) {
            cnaeNumerico = (double) cnaePrincipal.hashCode();
            municipioNumerico = (double) municipio.hashCode();
        }
        
        // ✅ ARRAY COM TODAS AS FEATURES (BÁSICAS + AVANÇADAS)
        return new double[] {
            // Features básicas
            cnaeNumerico,
            municipioNumerico,
            capitalSocial,
            (double) quantidadeEmpresasRegiao,
            capitalMedioRegiao,
            densidadeEmpresarial,
            (double) faixaCapitalSocial,
            
            // ✅ Features avançadas
            capitalVsMedia,
            concentracaoMercado,
            variacaoCapital,
            scoreLocalizacao,
            nivelConcorrencia,
            maturidadeMercado
        };
    }
    
    @Override
    public String toString() {
        return String.format(
            "EmpresaFeatures[CNAE=%s, Municipio=%s, Capital=%.2f, Empresas=%d, CapitalVsMedia=%.2f, Concentracao=%.2f]",
            cnaePrincipal, municipio, capitalSocial, quantidadeEmpresasRegiao, capitalVsMedia, concentracaoMercado
        );
    }
}