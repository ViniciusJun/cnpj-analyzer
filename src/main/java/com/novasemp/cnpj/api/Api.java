package com.novasemp.cnpj.api;

import com.google.gson.Gson;
import com.novasemp.cnpj.dao.EmpresaDAO;
import com.novasemp.cnpj.model.Empresa;
import spark.Spark;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Api {
    public static void main(String[] args) {
        // Configurar o banco de dados
        String dbPath = "data/processed/cnpj_data.db";
        EmpresaDAO empresaDAO;
        try {
            empresaDAO = new EmpresaDAO(dbPath);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            return;
        }

        Gson gson = new Gson();

        // Configurar porta
        Spark.port(4567);

        // Habilitar CORS
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
        });

        // Definir endpoints
        Spark.get("/empresas/count", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");

            if (cnae == null || municipio == null) {
                res.status(400);
                return "Parâmetros 'cnae' e 'municipio' são obrigatórios";
            }

            try {
                int count = empresaDAO.countEmpresasPorCnaeEMunicipio(cnae, municipio);
                return gson.toJson(count);
            } catch (SQLException e) {
                res.status(500);
                return "Erro ao acessar o banco de dados: " + e.getMessage();
            }
        });

        Spark.get("/empresas/avg-capital", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");

            if (cnae == null || municipio == null) {
                res.status(400);
                return "Parâmetros 'cnae' e 'municipio' são obrigatórios";
            }

            try {
                double avg = empresaDAO.avgCapitalSocialPorCnaeEMunicipio(cnae, municipio);
                return gson.toJson(avg);
            } catch (SQLException e) {
                res.status(500);
                return "Erro ao acessar o banco de dados: " + e.getMessage();
            }
        });

        Spark.get("/empresas", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");

            if (cnae == null || municipio == null) {
                res.status(400);
                return "Parâmetros 'cnae' e 'municipio' são obrigatórios";
            }

            try {
                List<Empresa> empresas = empresaDAO.listarEmpresasPorCnaeEMunicipio(cnae, municipio);
                res.type("application/json");
                return gson.toJson(empresas);
            } catch (SQLException e) {
                res.status(500);
                return "Erro ao acessar o banco de dados: " + e.getMessage();
            }
        });

        Spark.get("/dashboard", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");

            if (cnae == null || municipio == null) {
                res.status(400);
                return "Parâmetros 'cnae' e 'municipio' são obrigatórios";
            }

            try {
                int count = empresaDAO.countEmpresasPorCnaeEMunicipio(cnae, municipio);
                double avgCapital = empresaDAO.avgCapitalSocialPorCnaeEMunicipio(cnae, municipio);
                
                Map<String, Object> dashboardData = new HashMap<>();
                dashboardData.put("quantidade_empresas", count);
                dashboardData.put("capital_social_medio", avgCapital);
                dashboardData.put("cnae", cnae);
                dashboardData.put("municipio", municipio);
                
                // TODO: Adicionar probabilidade e estratégias em iterações futuras
                dashboardData.put("probabilidade_sucesso", 0.75);
                dashboardData.put("estrategias", new String[]{
                    "Analisar concorrência local",
                    "Estudar perfil demográfico da região",
                    "Avaliar sazonalidade do negócio"
                });
                
                res.type("application/json");
                return gson.toJson(dashboardData);
            } catch (SQLException e) {
                res.status(500);
                return "Erro ao acessar o banco de dados: " + e.getMessage();
            }
        });

        System.out.println("API rodando em http://localhost:4567");
        System.out.println("Endpoints disponíveis:");
        System.out.println("  GET /empresas/count?cnae=XXXXXXX&municipio=NOME");
        System.out.println("  GET /empresas/avg-capital?cnae=XXXXXXX&municipio=NOME");
        System.out.println("  GET /empresas?cnae=XXXXXXX&municipio=NOME");
        System.out.println("  GET /dashboard?cnae=XXXXXXX&municipio=NOME");
    }
}
