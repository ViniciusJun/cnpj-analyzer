package com.novasemp.cnpj.dao;

import com.novasemp.cnpj.model.Empresa;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpresaDAO {
    private Connection connection;

    public EmpresaDAO(String dbPath) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        criarTabela();
    }

    private void criarTabela() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS empresas (" +
                "cnpj_basico TEXT, " +
                "cnae_principal TEXT, " +
                "descricao_cnae TEXT, " +
                "cep TEXT, " +
                "bairro TEXT, " +
                "municipio TEXT, " +
                "municipio_nome TEXT, " +
                "capital_social REAL)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'empresas' criada com sucesso com as novas colunas!");
        }
    }

    public void inserir(Empresa empresa) throws SQLException {
        String sql = "INSERT INTO empresas (cnpj_basico, cnae_principal, descricao_cnae, cep, bairro, municipio, municipio_nome, capital_social) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, empresa.getCnpjBasico());
            pstmt.setString(2, empresa.getCnaePrincipal());
            pstmt.setString(3, empresa.getDescricaoCnae());
            pstmt.setString(4, empresa.getCep());
            pstmt.setString(5, empresa.getBairro());
            pstmt.setString(6, empresa.getMunicipio());
            pstmt.setString(7, empresa.getMunicipioNome());
            pstmt.setDouble(8, empresa.getCapitalSocial());
            pstmt.executeUpdate();
        }
    }

    public List<Empresa> listarTodas() throws SQLException {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT * FROM empresas";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Empresa empresa = new Empresa(
                    rs.getString("cnpj_basico"),
                    rs.getString("cnae_principal"),
                    rs.getString("descricao_cnae"),
                    rs.getString("cep"),
                    rs.getString("bairro"),
                    rs.getString("municipio"),
                    rs.getString("municipio_nome"),
                    rs.getDouble("capital_social")
                );
                empresas.add(empresa);
            }
        }
        return empresas;
    }

    public int countEmpresasPorCnaeEMunicipio(String cnae, String municipio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM empresas WHERE cnae_principal = ? AND municipio = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cnae);
            pstmt.setString(2, municipio);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public double avgCapitalSocialPorCnaeEMunicipio(String cnae, String municipio) throws SQLException {
        String sql = "SELECT AVG(capital_social) FROM empresas WHERE cnae_principal = ? AND municipio = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cnae);
            pstmt.setString(2, municipio);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public List<Empresa> listarEmpresasPorCnaeEMunicipio(String cnae, String municipio) throws SQLException {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT * FROM empresas WHERE cnae_principal = ? AND municipio = ? LIMIT 100";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cnae);
            pstmt.setString(2, municipio);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Empresa empresa = new Empresa(
                        rs.getString("cnpj_basico"),
                        rs.getString("cnae_principal"),
                        rs.getString("descricao_cnae"),
                        rs.getString("cep"),
                        rs.getString("bairro"),
                        rs.getString("municipio"),
                        rs.getString("municipio_nome"),
                        rs.getDouble("capital_social")
                    );
                    empresas.add(empresa);
                }
            }
        }
        return empresas;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}