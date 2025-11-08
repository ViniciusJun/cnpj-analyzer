package com.novasemp.cnpj.ml.repository;

import com.novasemp.cnpj.ml.model.EmpresaFeatures;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrainingDataRepository {
    private Connection connection;
    
    public TrainingDataRepository(Connection connection) {
        this.connection = connection;
    }
    
    public List<EmpresaFeatures> obterDadosTreinamento() throws SQLException {
        List<EmpresaFeatures> dados = new ArrayList<>();
        
        try {
            String sql = """
                SELECT 
                    e.cnae_principal,
                    e.municipio,
                    e.capital_social,
                    COUNT(*) OVER (PARTITION BY e.municipio, e.cnae_principal) as quantidade_empresas_regiao,
                    AVG(e.capital_social) OVER (PARTITION BY e.municipio, e.cnae_principal) as capital_medio_regiao,
                    (COUNT(*) OVER (PARTITION BY e.municipio, e.cnae_principal) * 1.0 / 
                     NULLIF(COUNT(*) OVER (PARTITION BY e.municipio), 0)) as densidade_empresarial,
                    CASE 
                        WHEN e.capital_social < 10000 THEN 0
                        WHEN e.capital_social BETWEEN 10000 AND 50000 THEN 1
                        ELSE 2
                    END as faixa_capital
                FROM empresas e
                WHERE e.cnae_principal IS NOT NULL 
                  AND e.municipio IS NOT NULL
                  AND e.capital_social IS NOT NULL
                  AND e.capital_social > 0
                ORDER BY RANDOM()
                LIMIT 10000
                """;
                
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                int count = 0;
                while (rs.next()) {
                    EmpresaFeatures features = new EmpresaFeatures(
                        rs.getString("cnae_principal"),
                        rs.getString("municipio"),
                        rs.getDouble("capital_social"),
                        rs.getInt("quantidade_empresas_regiao"),
                        rs.getDouble("capital_medio_regiao"),
                        rs.getDouble("densidade_empresarial"),
                        rs.getInt("faixa_capital")
                    );
                    dados.add(features);
                    count++;
                }
                System.out.println("✅ " + count + " registros reais carregados para treinamento ML");
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Erro ao obter dados de treinamento: " + e.getMessage());
        }
        
        return dados;
    }
    
    public List<EmpresaFeatures> obterDadosTreinamentoAvancado() throws SQLException {
        List<EmpresaFeatures> dados = new ArrayList<>();
        
        try {
            String sql = """
                SELECT 
                    e.cnae_principal,
                    e.municipio,
                    e.capital_social,
                    COUNT(*) OVER (PARTITION BY e.municipio, e.cnae_principal) as quantidade_empresas_regiao,
                    AVG(e.capital_social) OVER (PARTITION BY e.municipio, e.cnae_principal) as capital_medio_regiao,
                    (COUNT(*) OVER (PARTITION BY e.municipio, e.cnae_principal) * 1.0 / 
                     NULLIF(COUNT(*) OVER (PARTITION BY e.municipio), 0)) as densidade_empresarial,
                    CASE 
                        WHEN e.capital_social < 10000 THEN 0
                        WHEN e.capital_social BETWEEN 10000 AND 50000 THEN 1
                        ELSE 2
                    END as faixa_capital,
                    -- ✅ NOVAS FEATURES AVANÇADAS
                    COALESCE(STDDEV(e.capital_social) OVER (PARTITION BY e.municipio, e.cnae_principal), 0) as variacao_capital,
                    COUNT(*) OVER (PARTITION BY e.municipio) as total_municipio,
                    COUNT(*) OVER (PARTITION BY e.cnae_principal) as total_setor,
                    -- Novas métricas calculadas
                    (e.capital_social / NULLIF(AVG(e.capital_social) OVER (PARTITION BY e.municipio, e.cnae_principal), 0)) as capital_vs_media,
                    (COUNT(*) OVER (PARTITION BY e.municipio, e.cnae_principal) * 1.0 / 
                     NULLIF(COUNT(*) OVER (PARTITION BY e.municipio), 0)) as concentracao_mercado
                FROM empresas e
                WHERE e.cnae_principal IS NOT NULL 
                  AND e.municipio IS NOT NULL
                  AND e.capital_social IS NOT NULL
                  AND e.capital_social > 0
                ORDER BY RANDOM()
                LIMIT 15000
                """;
                
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                int count = 0;
                while (rs.next()) {
                    EmpresaFeatures features = new EmpresaFeatures(
                        rs.getString("cnae_principal"),
                        rs.getString("municipio"),
                        rs.getDouble("capital_social"),
                        rs.getInt("quantidade_empresas_regiao"),
                        rs.getDouble("capital_medio_regiao"),
                        rs.getDouble("densidade_empresarial"),
                        rs.getInt("faixa_capital")
                    );
                    
                    // ✅ SETAR NOVAS FEATURES AVANÇADAS
                    features.setVariacaoCapital(rs.getDouble("variacao_capital"));
                    features.setTotalMunicipio(rs.getInt("total_municipio"));
                    features.setTotalSetor(rs.getInt("total_setor"));
                    features.setCapitalVsMedia(rs.getDouble("capital_vs_media"));
                    features.setConcentracaoMercado(rs.getDouble("concentracao_mercado"));
                    
                    dados.add(features);
                    count++;
                }
                System.out.println("✅ " + count + " registros avançados carregados para treinamento ML");
                
                // Log de amostras para debug
                if (!dados.isEmpty()) {
                    System.out.println("📊 Amostra de dados avançados:");
                    EmpresaFeatures amostra = dados.get(0);
                    System.out.println("   CNAE: " + amostra.getCnaePrincipal() + 
                                     ", Capital vs Média: " + String.format("%.2f", amostra.getCapitalVsMedia()) +
                                     ", Variação Capital: " + String.format("%.2f", amostra.getVariacaoCapital()));
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Erro ao obter dados de treinamento avançado: " + e.getMessage());
            e.printStackTrace();
        }
        
        return dados;
    }
    
    public List<EmpresaFeatures> obterDadosParaPredicao(String cnae, String municipio, double capitalSocial) throws SQLException {
        List<EmpresaFeatures> dados = new ArrayList<>();
        
        try {
            String sql = """
                SELECT 
                    ? as cnae_principal,
                    ? as municipio,
                    ? as capital_social,
                    COUNT(*) as quantidade_empresas_regiao,
                    COALESCE(AVG(capital_social), ?) as capital_medio_regiao,
                    (COUNT(*) * 1.0 / 
                     NULLIF((SELECT COUNT(*) FROM empresas e2 WHERE e2.municipio = ?), 0)) as densidade_empresarial,
                    CASE 
                        WHEN ? < 10000 THEN 0
                        WHEN ? BETWEEN 10000 AND 50000 THEN 1
                        ELSE 2
                    END as faixa_capital,
                    -- ✅ NOVAS FEATURES PARA PREDIÇÃO
                    COALESCE(STDDEV(capital_social), 0) as variacao_capital,
                    (SELECT COUNT(*) FROM empresas e2 WHERE e2.municipio = ?) as total_municipio,
                    (SELECT COUNT(*) FROM empresas e3 WHERE e3.cnae_principal = ?) as total_setor,
                    (? / NULLIF(COALESCE(AVG(capital_social), ?), 0)) as capital_vs_media,
                    (COUNT(*) * 1.0 / 
                     NULLIF((SELECT COUNT(*) FROM empresas e2 WHERE e2.municipio = ?), 0)) as concentracao_mercado
                FROM empresas 
                WHERE municipio = ? AND cnae_principal = ?
                """;
                
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, cnae);
                pstmt.setString(2, municipio);
                pstmt.setDouble(3, capitalSocial);
                pstmt.setDouble(4, capitalSocial);
                pstmt.setString(5, municipio);
                pstmt.setDouble(6, capitalSocial);
                pstmt.setDouble(7, capitalSocial);
                pstmt.setString(8, municipio);
                pstmt.setString(9, cnae);
                pstmt.setDouble(10, capitalSocial);
                pstmt.setDouble(11, capitalSocial);
                pstmt.setString(12, municipio);
                pstmt.setString(13, municipio);
                pstmt.setString(14, cnae);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        EmpresaFeatures features = new EmpresaFeatures(
                            rs.getString("cnae_principal"),
                            rs.getString("municipio"),
                            rs.getDouble("capital_social"),
                            rs.getInt("quantidade_empresas_regiao"),
                            rs.getDouble("capital_medio_regiao"),
                            rs.getDouble("densidade_empresarial"),
                            rs.getInt("faixa_capital")
                        );
                        
                        // ✅ SETAR NOVAS FEATURES AVANÇADAS
                        features.setVariacaoCapital(rs.getDouble("variacao_capital"));
                        features.setTotalMunicipio(rs.getInt("total_municipio"));
                        features.setTotalSetor(rs.getInt("total_setor"));
                        features.setCapitalVsMedia(rs.getDouble("capital_vs_media"));
                        features.setConcentracaoMercado(rs.getDouble("concentracao_mercado"));
                        
                        dados.add(features);
                        
                        System.out.println("📊 Dados para predição - " +
                                         "Empresas: " + features.getQuantidadeEmpresasRegiao() +
                                         ", Capital vs Média: " + String.format("%.2f", features.getCapitalVsMedia()) +
                                         ", Concentração: " + String.format("%.2f", features.getConcentracaoMercado()));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Erro ao obter dados para predição: " + e.getMessage());
            throw e;
        }
        
        return dados;
    }
}