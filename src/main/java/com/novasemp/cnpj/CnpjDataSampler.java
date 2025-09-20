package com.novasemp.cnpj;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CnpjDataSampler {
    
    public static void main(String[] args) {
        String estabelecimentosPath = "data/raw/K3241.K03200Y0.D50809.ESTABELE";
        String empresasPath = "data/raw/K3241.K03200Y0.D50809.EMPRECSV";
        String outputPath = "data/processed/sample_processado.csv";
        
        try {
            processarDados(estabelecimentosPath, empresasPath, outputPath);
            System.out.println("Processamento concluído com sucesso!");
        } catch (IOException | CsvException e) {
            System.err.println("Erro no processamento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void processarDados(String estabelecimentosPath, String empresasPath, String outputPath) 
            throws IOException, CsvException {
        
        int linhaLimite = 10000;
        
        // Configurar parser com separador personalizado
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        
        // Ler dados das empresas
        Map<String, String> capitalSocialMap = new HashMap<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new FileInputStream(empresasPath), StandardCharsets.ISO_8859_1))
                .withCSVParser(parser)
                .build()) {
            
            String[] linha;
            int count = 0;
            while ((linha = reader.readNext()) != null && count < linhaLimite) {
                if (linha.length >= 5) {
                    capitalSocialMap.put(linha[0], linha[4]);
                }
                count++;
            }
        }
        
        // Processar estabelecimentos
        List<String[]> dadosCombinados = new ArrayList<>();
        dadosCombinados.add(new String[]{
            "cnpj_basico", "cnae_principal", "cep", "bairro", "municipio", "capital_social"
        });
        
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new FileInputStream(estabelecimentosPath), StandardCharsets.ISO_8859_1))
                .withCSVParser(parser)
                .build()) {
            
            String[] est;
            int count = 0;
            while ((est = reader.readNext()) != null && count < linhaLimite) {
                if (est.length < 16) continue;
                
                String cnpjBasico = est[0];
                String capitalSocial = capitalSocialMap.getOrDefault(cnpjBasico, "0.0");
                
                dadosCombinados.add(new String[]{
                    cnpjBasico,
                    est[7],
                    est[15],
                    est[14],
                    est[13],
                    capitalSocial
                });
                count++;
            }
        }
        
        // Escrever arquivo processado
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath, StandardCharsets.UTF_8))) {
            writer.writeAll(dadosCombinados);
        }
    }
}