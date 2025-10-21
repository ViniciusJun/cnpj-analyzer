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
        
        // Query para obter features das empresas
        String sql = """
            SELECT 
                e.cnae_principal,
                e.municipio,
                e.capital_social,
                COUNT(*) OVER (PARTITION BY e.municipio, e.cnae_principal) as quantidade_empresas_regiao,
                AVG(e.capital_social) OVER (PARTITION BY e.municipio, e.cnae_principal) as capital_medio_regiao,
                (COUNT(*) OVER (PARTITION BY e.municipio, e.cnae_principal) * 1.0 / 
                 COUNT(*) OVER (PARTITION BY e.municipio)) as densidade_empresarial,
                CASE 
                    WHEN e.capital_social < 10000 THEN 0
                    WHEN e.capital_social BETWEEN 10000 AND 50000 THEN 1
                    ELSE 2
                END as faixa_capital
            FROM empresas e
            LIMIT 10000
            """;
            
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
            }
        }
        
        return dados;
    }
    
    public List<EmpresaFeatures> obterDadosParaPredicao(String cnae, String municipio, double capitalSocial) throws SQLException {
        List<EmpresaFeatures> dados = new ArrayList<>();
        
        String sql = """
            SELECT 
                ? as cnae_principal,
                ? as municipio,
                ? as capital_social,
                COUNT(*) as quantidade_empresas_regiao,
                AVG(capital_social) as capital_medio_regiao,
                (COUNT(*) * 1.0 / 
                 (SELECT COUNT(*) FROM empresas e2 WHERE e2.municipio = ?)) as densidade_empresarial,
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
            pstmt.setString(4, municipio);
            pstmt.setDouble(5, capitalSocial);
            pstmt.setDouble(6, capitalSocial);
            pstmt.setString(7, municipio);
            pstmt.setString(8, cnae);
            
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
        
        return dados;
    }
}
