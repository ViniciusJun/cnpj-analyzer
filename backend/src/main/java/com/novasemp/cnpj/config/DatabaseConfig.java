package com.novasemp.cnpj.config;

import com.novasemp.cnpj.util.DataInitializer;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        try {
            // Para produção - usar caminho relativo
            String dbPath = "data/processed/cnpj_data.db";
            
            // Criar diretório se não existir
            new File("data/processed").mkdirs();
            
            return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco: " + e.getMessage());
            throw e;
        }
    }
}