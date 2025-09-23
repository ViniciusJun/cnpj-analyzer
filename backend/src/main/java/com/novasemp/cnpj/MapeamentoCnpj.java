package com.novasemp.cnpj;

import com.opencsv.CSVReader;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MapeamentoCnpj {
    
    public static Map<String, String> carregarMapeamentoCnae(String arquivoCnae) throws IOException, CsvException {
        Map<String, String> mapeamento = new HashMap<>();
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new FileInputStream(arquivoCnae), StandardCharsets.ISO_8859_1))
                .withCSVParser(parser)
                .build()) {
            
            String[] linha;
            while ((linha = reader.readNext()) != null) {
                if (linha.length >= 2) {
                    mapeamento.put(linha[0], linha[1]); // Código -> Descrição
                }
            }
        }
        return mapeamento;
    }
    
    public static Map<String, String> carregarMapeamentoMunicipios(String arquivoMunicipios) throws IOException, CsvException {
        Map<String, String> mapeamento = new HashMap<>();
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new FileInputStream(arquivoMunicipios), StandardCharsets.ISO_8859_1))
                .withCSVParser(parser)
                .build()) {
            
            String[] linha;
            while ((linha = reader.readNext()) != null) {
                if (linha.length >= 2) {
                    mapeamento.put(linha[0], linha[1]); // Código IBGE -> Nome Município
                }
            }
        }
        return mapeamento;
    }
}
