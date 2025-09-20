package com.novasemp.cnpj.model;

public class Empresa {
    private String cnpjBasico;
    private String cnaePrincipal;
    private String cep;
    private String bairro;
    private String municipio;
    private double capitalSocial;

    public Empresa() {}

    public Empresa(String cnpjBasico, String cnaePrincipal, String cep, String bairro, String municipio, double capitalSocial) {
        this.cnpjBasico = cnpjBasico;
        this.cnaePrincipal = cnaePrincipal;
        this.cep = cep;
        this.bairro = bairro;
        this.municipio = municipio;
        this.capitalSocial = capitalSocial;
    }

    // Getters e Setters
    public String getCnpjBasico() { return cnpjBasico; }
    public void setCnpjBasico(String cnpjBasico) { this.cnpjBasico = cnpjBasico; }

    public String getCnaePrincipal() { return cnaePrincipal; }
    public void setCnaePrincipal(String cnaePrincipal) { this.cnaePrincipal = cnaePrincipal; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public double getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(double capitalSocial) { this.capitalSocial = capitalSocial; }

    @Override
    public String toString() {
        return "Empresa{" +
                "cnpjBasico='" + cnpjBasico + '\'' +
                ", cnaePrincipal='" + cnaePrincipal + '\'' +
                ", cep='" + cep + '\'' +
                ", bairro='" + bairro + '\'' +
                ", municipio='" + municipio + '\'' +
                ", capitalSocial=" + capitalSocial +
                '}';
    }
}