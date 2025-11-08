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
            // ✅ CONSULTA MELHORADA: Mais robusta com fallback
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
            // Não propaga a exceção para não quebrar a inicialização
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
                    END as faixa_capital
                FROM empresas 
                WHERE municipio = ? AND cnae_principal = ?
                """;
                
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, cnae);
                pstmt.setString(2, municipio);
                pstmt.setDouble(3, capitalSocial);
                pstmt.setDouble(4, capitalSocial); // Fallback para capital médio
                pstmt.setString(5, municipio);
                pstmt.setDouble(6, capitalSocial);
                pstmt.setDouble(7, capitalSocial);
                pstmt.setString(8, municipio);
                pstmt.setString(9, cnae);
                
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
                        dados.add(features);
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