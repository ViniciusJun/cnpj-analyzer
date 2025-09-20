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
                "cep TEXT, " +
                "bairro TEXT, " +
                "municipio TEXT, " +
                "capital_social REAL)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void inserir(Empresa empresa) throws SQLException {
        String sql = "INSERT INTO empresas (cnpj_basico, cnae_principal, cep, bairro, municipio, capital_social) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, empresa.getCnpjBasico());
            pstmt.setString(2, empresa.getCnaePrincipal());
            pstmt.setString(3, empresa.getCep());
            pstmt.setString(4, empresa.getBairro());
            pstmt.setString(5, empresa.getMunicipio());
            pstmt.setDouble(6, empresa.getCapitalSocial());
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
                    rs.getString("cep"),
                    rs.getString("bairro"),
                    rs.getString("municipio"),
                    rs.getDouble("capital_social")
                );
                empresas.add(empresa);
            }
        }
        return empresas;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
