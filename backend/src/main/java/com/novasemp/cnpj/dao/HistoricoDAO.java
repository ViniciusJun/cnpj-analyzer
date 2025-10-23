package com.novasemp.cnpj.dao;

import com.novasemp.cnpj.model.HistoricoBusca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HistoricoDAO {
    private Connection connection;

    public HistoricoDAO(Connection connection) throws SQLException {
        this.connection = connection;
        criarTabela();
    }

    private void criarTabela() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS historico_buscas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "session_id TEXT, " +
                "cnae TEXT, " +
                "descricao_cnae TEXT, " +
                "municipio TEXT, " +
                "municipio_nome TEXT, " +
                "capital_social REAL, " +
                "data_busca DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "quantidade_empresas INTEGER, " +
                "capital_social_medio REAL, " +
                "probabilidade_sucesso REAL)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void salvarBusca(HistoricoBusca historico) throws SQLException {
        String sql = "INSERT INTO historico_buscas (session_id, cnae, descricao_cnae, municipio, " +
                     "municipio_nome, capital_social, quantidade_empresas, capital_social_medio, " +
                     "probabilidade_sucesso) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, historico.getSessionId());
            pstmt.setString(2, historico.getCnae());
            pstmt.setString(3, historico.getDescricaoCnae());
            pstmt.setString(4, historico.getMunicipio());
            pstmt.setString(5, historico.getMunicipioNome());
            pstmt.setDouble(6, historico.getCapitalSocial());
            pstmt.setInt(7, historico.getQuantidadeEmpresas());
            pstmt.setDouble(8, historico.getCapitalSocialMedio());
            pstmt.setDouble(9, historico.getProbabilidadeSucesso());
            pstmt.executeUpdate();
        }
    }

    public List<HistoricoBusca> listarPorSession(String sessionId) throws SQLException {
        List<HistoricoBusca> historicos = new ArrayList<>();
        String sql = "SELECT * FROM historico_buscas WHERE session_id = ? ORDER BY data_busca DESC LIMIT 10";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    HistoricoBusca historico = new HistoricoBusca();
                    historico.setId(rs.getInt("id"));
                    historico.setSessionId(rs.getString("session_id"));
                    historico.setCnae(rs.getString("cnae"));
                    historico.setDescricaoCnae(rs.getString("descricao_cnae"));
                    historico.setMunicipio(rs.getString("municipio"));
                    historico.setMunicipioNome(rs.getString("municipio_nome"));
                    historico.setCapitalSocial(rs.getDouble("capital_social"));
                    historico.setDataBusca(rs.getTimestamp("data_busca"));
                    historico.setQuantidadeEmpresas(rs.getInt("quantidade_empresas"));
                    historico.setCapitalSocialMedio(rs.getDouble("capital_social_medio"));
                    historico.setProbabilidadeSucesso(rs.getDouble("probabilidade_sucesso"));
                    
                    historicos.add(historico);
                }
            }
        }
        return historicos;
    }

    public boolean deletarHistorico(int id, String sessionId) throws SQLException {
        String sql = "DELETE FROM historico_buscas WHERE id = ? AND session_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, sessionId);
            return pstmt.executeUpdate() > 0;
        }
    }
}