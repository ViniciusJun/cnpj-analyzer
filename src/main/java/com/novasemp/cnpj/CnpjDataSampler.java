package com.novasemp.cnpj;

import com.novasemp.cnpj.dao.EmpresaDAO;
import com.novasemp.cnpj.model.Empresa;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

public class CnpjDataSampler {
    
    public static void main(String[] args) {
        String estabelecimentosPath = "data/raw/K3241.K03200Y0.D50809.ESTABELE";
        String empresasPath = "data/raw/K3241.K03200Y0.D50809.EMPRECSV";
        String outputPath = "data/processed/sample_processado.csv";
        String dbPath = "data/processed/cnpj_data.db";
        
        try {
            processarDados(estabelecimentosPath, empresasPath, outputPath, dbPath);
            System.out.println("Processamento concluído com sucesso!");
        } catch (IOException | CsvException | SQLException e) {
            System.err.println("Erro no processamento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void processarDados(String estabelecimentosPath, String empresasPath, String outputPath, String dbPath) 
            throws IOException, CsvException, SQLException {
        
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
        
        // Processar estabelecimentos e salvar no banco de dados
        EmpresaDAO empresaDAO = new EmpresaDAO(dbPath);
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
                String capitalSocialStr = capitalSocialMap.getOrDefault(cnpjBasico, "0.0");
                double capitalSocial = Double.parseDouble(capitalSocialStr.replace(",", "."));
                
                // Criar objeto Empresa
                Empresa empresa = new Empresa(
                    cnpjBasico,
                    est[7],
                    est[15],
                    est[14],
                    est[13],
                    capitalSocial
                );
                
                // Salvar no banco de dados
                empresaDAO.inserir(empresa);
                
                // Adicionar à lista para o CSV (opcional)
                dadosCombinados.add(new String[]{
                    cnpjBasico,
                    est[7],
                    est[15],
                    est[14],
                    est[13],
                    capitalSocialStr
                });
                
                count++;
            }
        }
        
        // Escrever arquivo processado (opcional)
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath, StandardCharsets.UTF_8))) {
            writer.writeAll(dadosCombinados);
        }
        
        // Testar recuperação de dados
        List<Empresa> empresas = empresaDAO.listarTodas();
        System.out.println("Total de empresas inseridas: " + empresas.size());
        
        if (!empresas.isEmpty()) {
            System.out.println("Primeira empresa: " + empresas.get(0));
        }
        
        empresaDAO.close();
    }
}