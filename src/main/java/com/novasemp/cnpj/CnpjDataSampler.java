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
        String cnaeMappingPath = "data/raw/F.K03200$Z.D50809.CNAECSV";
        String municipiosMappingPath = "data/raw/F.K03200$Z.D50809.MUNICCSV";
        String outputPath = "data/processed/sample_processado.csv";
        String dbPath = "data/processed/cnpj_data.db";
        
        try {
            processarDados(estabelecimentosPath, empresasPath, cnaeMappingPath, municipiosMappingPath, outputPath, dbPath);
            System.out.println("Processamento concluído com sucesso!");
        } catch (IOException | CsvException | SQLException e) {
            System.err.println("Erro no processamento: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void processarDados(String estabelecimentosPath, String empresasPath, String cnaeMappingPath, 
             String municipiosMappingPath, String outputPath, String dbPath) 
            throws IOException, CsvException, SQLException {
        
        int linhaLimite = 10000;
        
        // Carregar mapeamentos de CNAE e municípios
        Map<String, String> mapeamentoCnae = MapeamentoCnpj.carregarMapeamentoCnae(cnaeMappingPath);
        Map<String, String> mapeamentoMunicipios = MapeamentoCnpj.carregarMapeamentoMunicipios(municipiosMappingPath);
        
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
                if (linha.length >= 10) { // Verificar se tem pelo menos 10 colunas
                    capitalSocialMap.put(linha[0], linha[9]); // CNPJ básico -> Capital Social (coluna 10)
                }
                count++;
            }
        }
        
        // Processar estabelecimentos e salvar no banco de dados
        EmpresaDAO empresaDAO = new EmpresaDAO(dbPath);
        List<String[]> dadosCombinados = new ArrayList<>();
        dadosCombinados.add(new String[]{
            "cnpj_basico", "cnae_principal", "descricao_cnae", "cep", "bairro", "municipio", "municipio_nome", "capital_social"
        });
        
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(new FileInputStream(estabelecimentosPath), StandardCharsets.ISO_8859_1))
                .withCSVParser(parser)
                .build()) {
            
            String[] est;
            int count = 0;
            while ((est = reader.readNext()) != null && count < linhaLimite) {
                if (est.length < 23) continue; // Verificar se tem pelo menos 23 colunas
                
                String cnpjBasico = est[0];
                String capitalSocialStr = capitalSocialMap.getOrDefault(cnpjBasico, "0.0");
                double capitalSocial = Double.parseDouble(capitalSocialStr.replace(",", "."));
                
                // Usar índices corretos baseados no layout oficial
                String cnaePrincipal = est[15];     // Coluna 16 - CNAE Principal
                String cep = est[20];               // Coluna 21 - CEP
                String bairro = est[19];            // Coluna 20 - Bairro
                String municipioCode = est[21];     // Coluna 22 - Código do Município
                
                // Obter descrições dos mapeamentos
                String descricaoCnae = mapeamentoCnae.getOrDefault(cnaePrincipal, "Desconhecido");
                String nomeMunicipio = mapeamentoMunicipios.getOrDefault(municipioCode, "Desconhecido");
                
                // Criar objeto Empresa
                Empresa empresa = new Empresa(
                    cnpjBasico,
                    cnaePrincipal,
                    descricaoCnae,
                    cep,
                    bairro,
                    municipioCode,
                    nomeMunicipio,
                    capitalSocial
                );
                
                // Salvar no banco de dados
                empresaDAO.inserir(empresa);
                
                // Adicionar à lista para o CSV (opcional)
                dadosCombinados.add(new String[]{
                    cnpjBasico,
                    cnaePrincipal,
                    descricaoCnae,
                    cep,
                    bairro,
                    municipioCode,
                    nomeMunicipio,
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