package com.novasemp.cnpjmobile.model;

import com.google.gson.annotations.SerializedName;

public class EmpresaResponse {
    @SerializedName("cnpj_basico")
    private String cnpjBasico;

    @SerializedName("cnae_principal")
    private String cnaePrincipal;

    @SerializedName("descricao_cnae")
    private String descricaoCnae;

    @SerializedName("cep")
    private String cep;

    @SerializedName("bairro")
    private String bairro;

    @SerializedName("municipio")
    private String municipio;

    @SerializedName("municipio_nome")
    private String municipioNome;

    @SerializedName("capital_social")
    private double capitalSocial;

    // Getters e Setters
    public String getCnpjBasico() { return cnpjBasico; }
    public void setCnpjBasico(String cnpjBasico) { this.cnpjBasico = cnpjBasico; }

    public String getCnaePrincipal() { return cnaePrincipal; }
    public void setCnaePrincipal(String cnaePrincipal) { this.cnaePrincipal = cnaePrincipal; }

    public String getDescricaoCnae() { return descricaoCnae; }
    public void setDescricaoCnae(String descricaoCnae) { this.descricaoCnae = descricaoCnae; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getMunicipioNome() { return municipioNome; }
    public void setMunicipioNome(String municipioNome) { this.municipioNome = municipioNome; }

    public double getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(double capitalSocial) { this.capitalSocial = capitalSocial; }
}