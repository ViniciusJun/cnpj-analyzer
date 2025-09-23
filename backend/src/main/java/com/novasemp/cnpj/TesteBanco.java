package com.novasemp.cnpj;

import com.novasemp.cnpj.dao.EmpresaDAO;

import java.sql.SQLException;

public class TesteBanco {
    public static void main(String[] args) {
        try {
            EmpresaDAO dao = new EmpresaDAO("data/processed/cnpj_data.db");
            
            // Testar se a tabela existe e tem dados
            int count = dao.countEmpresasPorCnaeEMunicipio("4721102", "SÃO PAULO");
            System.out.println("Quantidade de empresas encontradas: " + count);
            
            dao.close();
        } catch (SQLException e) {
            System.err.println("Erro ao testar o banco: " + e.getMessage());
            e.printStackTrace();
        }
    }
}