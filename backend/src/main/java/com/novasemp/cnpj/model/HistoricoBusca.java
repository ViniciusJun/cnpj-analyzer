package com.novasemp.cnpj.model;

import java.util.Date;

public class HistoricoBusca {
    private int id;
    private String sessionId;
    private String cnae;
    private String descricaoCnae;
    private String municipio;
    private String municipioNome;
    private Double capitalSocial;
    private Date dataBusca;
    private int quantidadeEmpresas;
    private double capitalSocialMedio;
    private double probabilidadeSucesso;

    public HistoricoBusca() {}

    public HistoricoBusca(String sessionId, String cnae, String descricaoCnae, 
                         String municipio, String municipioNome, Double capitalSocial,
                         int quantidadeEmpresas, double capitalSocialMedio, 
                         double probabilidadeSucesso) {
        this.sessionId = sessionId;
        this.cnae = cnae;
        this.descricaoCnae = descricaoCnae;
        this.municipio = municipio;
        this.municipioNome = municipioNome;
        this.capitalSocial = capitalSocial;
        this.dataBusca = new Date();
        this.quantidadeEmpresas = quantidadeEmpresas;
        this.capitalSocialMedio = capitalSocialMedio;
        this.probabilidadeSucesso = probabilidadeSucesso;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getCnae() { return cnae; }
    public void setCnae(String cnae) { this.cnae = cnae; }

    public String getDescricaoCnae() { return descricaoCnae; }
    public void setDescricaoCnae(String descricaoCnae) { this.descricaoCnae = descricaoCnae; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getMunicipioNome() { return municipioNome; }
    public void setMunicipioNome(String municipioNome) { this.municipioNome = municipioNome; }

    public Double getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(Double capitalSocial) { this.capitalSocial = capitalSocial; }

    public Date getDataBusca() { return dataBusca; }
    public void setDataBusca(Date dataBusca) { this.dataBusca = dataBusca; }

    public int getQuantidadeEmpresas() { return quantidadeEmpresas; }
    public void setQuantidadeEmpresas(int quantidadeEmpresas) { this.quantidadeEmpresas = quantidadeEmpresas; }

    public double getCapitalSocialMedio() { return capitalSocialMedio; }
    public void setCapitalSocialMedio(double capitalSocialMedio) { this.capitalSocialMedio = capitalSocialMedio; }

    public double getProbabilidadeSucesso() { return probabilidadeSucesso; }
    public void setProbabilidadeSucesso(double probabilidadeSucesso) { this.probabilidadeSucesso = probabilidadeSucesso; }
}