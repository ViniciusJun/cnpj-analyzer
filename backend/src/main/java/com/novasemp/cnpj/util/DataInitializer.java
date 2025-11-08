package com.novasemp.cnpj.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DataInitializer {
    
    public static void initializeDatabase() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:cnpj_light.db");
            Statement stmt = connection.createStatement();
            
            // Criar tabela vazia
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS empresas (
                    cnpj_basico TEXT,
                    cnae_principal TEXT,
                    municipio TEXT,
                    capital_social REAL
                )
            """);
            
            // Inserir alguns dados de exemplo (opcional)
            stmt.execute("""
                INSERT INTO empresas (cnae_principal, municipio, capital_social) VALUES
                ('4721102', '3550308', 50000.0),
                ('4711301', '3550308', 75000.0),
                ('5611201', '3509502', 30000.0)
            """);
            
            stmt.close();
            connection.close();
            System.out.println("✅ Banco de dados inicializado com dados de exemplo");
            
        } catch (Exception e) {
            System.err.println("⚠️ Usando fallback sem banco de dados");
        }
    }
}