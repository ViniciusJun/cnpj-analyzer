package com.novasemp.cnpjmobile.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HistoricoBusca {
    private Integer id;
    private String sessionId;
    private String cnae;
    private String municipio;
    private Double capitalSocial;
    private String dataBusca;

    // Construtores
    public HistoricoBusca() {}

    public HistoricoBusca(String sessionId, String cnae, String municipio, Double capitalSocial) {
        this.sessionId = sessionId;
        this.cnae = cnae;
        this.municipio = municipio;
        this.capitalSocial = capitalSocial;
        this.dataBusca = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getCnae() { return cnae; }
    public void setCnae(String cnae) { this.cnae = cnae; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public Double getCapitalSocial() { return capitalSocial; }
    public void setCapitalSocial(Double capitalSocial) { this.capitalSocial = capitalSocial; }

    public String getDataBusca() { return dataBusca; }
    public void setDataBusca(String dataBusca) { this.dataBusca = dataBusca; }

    public void setDataBuscaAtual() {
        // Tentar diferentes formatos que o backend pode aceitar
        String[] formatos = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // ISO com milliseconds
                "yyyy-MM-dd'T'HH:mm:ss'Z'",       // ISO sem milliseconds
                "yyyy-MM-dd HH:mm:ss",            // Formato SQL
                "yyyy-MM-dd",                     // Apenas data
                "dd/MM/yyyy HH:mm:ss",            // Formato brasileiro
                "MM/dd/yyyy HH:mm:ss"             // Formato americano
        };

        for (String formato : formatos) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(formato, Locale.getDefault());
                if (formato.contains("'Z'")) {
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                this.dataBusca = sdf.format(new Date());
                System.out.println("DEBUG: HistoricoBusca - Data formatada com '" + formato + "': " + this.dataBusca);
                return; // Se funcionou, sair do loop
            } catch (Exception e) {
                System.out.println("DEBUG: HistoricoBusca - Formato '" + formato + "' falhou: " + e.getMessage());
            }
        }

        // Se todos os formatos falharem, usar timestamp
        this.dataBusca = String.valueOf(System.currentTimeMillis());
        System.out.println("DEBUG: HistoricoBusca - Usando timestamp: " + this.dataBusca);
    }
}