package com.novasemp.cnpjmobile.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoricoBusca {
    private Integer id;
    private String sessionId;
    private String cnae;
    private String municipio;
    private Double capital;
    private String dataBusca;

    // Construtor vazio necessário para o Retrofit
    public HistoricoBusca() {}

    // Construtor com parâmetros
    public HistoricoBusca(String sessionId, String cnae, String municipio, Double capital) {
        this.sessionId = sessionId;
        this.cnae = cnae;
        this.municipio = municipio;
        this.capital = capital;
        // Formatar a data como string
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.dataBusca = sdf.format(new Date());
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCane() {
        return cnae;
    }

    public void setCane(String cnae) {
        this.cnae = cnae;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public Double getCapital() {
        return capital;
    }

    public void setCapital(Double capital) {
        this.capital = capital;
    }

    public String getDataBusca() {
        return dataBusca;
    }

    public void setDataBusca(String dataBusca) {
        this.dataBusca = dataBusca;
    }

    // Método auxiliar para setar a data atual
    public void setDataBuscaAtual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.dataBusca = sdf.format(new Date());
    }
}