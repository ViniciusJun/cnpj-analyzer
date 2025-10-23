package com.novasemp.cnpj.api;

import com.google.gson.Gson;
import com.novasemp.cnpj.dao.EmpresaDAO;
import com.novasemp.cnpj.dao.HistoricoDAO;
import com.novasemp.cnpj.model.Empresa;
import com.novasemp.cnpj.model.HistoricoBusca;
import com.novasemp.cnpj.ml.service.MLModelService;
import com.novasemp.cnpj.ml.model.PredictionResult;
import spark.Spark;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Api {

    // Método auxiliar para logar dados disponíveis
    private static void logDadosDisponiveis(EmpresaDAO empresaDAO) {
        try {
            String sql = "SELECT DISTINCT cnae_principal, municipio FROM empresas LIMIT 5";
            
            try (Statement stmt = empresaDAO.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                System.out.println("\nDados disponíveis para teste:");
                System.out.println("CNAE\t\tMunicípio");
                System.out.println("-------------------------");
                
                while (rs.next()) {
                    String cnae = rs.getString("cnae_principal");
                    String municipio = rs.getString("municipio");
                    System.out.println(cnae + "\t" + municipio);
                }
                System.out.println("-------------------------");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao consultar dados: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Configurar o banco de dados
        String dbPath = "data/processed/cnpj_data.db";
        
        // Usar arrays finais para contornar a restrição de variáveis em lambdas
        final EmpresaDAO[] empresaDAOHolder = new EmpresaDAO[1];
        final MLModelService[] mlServiceHolder = new MLModelService[1];
        final HistoricoDAO[] historicoDAOHolder = new HistoricoDAO[1];
        
        try {
            empresaDAOHolder[0] = new EmpresaDAO(dbPath);
            System.out.println("Conectado ao banco de dados com sucesso!");
            
            // Inicializar serviço de ML
            try {
                mlServiceHolder[0] = new MLModelService(empresaDAOHolder[0].getConnection());
                System.out.println("Serviço de ML inicializado com sucesso!");
            } catch (Exception e) {
                System.err.println("Erro ao inicializar serviço de ML: " + e.getMessage());
                System.out.println("Continuando sem serviço de ML...");
            }

            // Inicializar histórico
            historicoDAOHolder[0] = new HistoricoDAO(empresaDAOHolder[0].getConnection());
            System.out.println("Sistema de histórico inicializado com sucesso!");
            
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            return;
        }

        // Log dos dados disponíveis
        logDadosDisponiveis(empresaDAOHolder[0]);

        Gson gson = new Gson();

        // Configurar porta
        Spark.port(8081);
        
        // Permite conexões externas
        Spark.ipAddress("0.0.0.0");

        // Habilitar CORS
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
        });

        // Endpoint de health check
        Spark.get("/health", (req, res) -> {
            res.type("application/json");
            return "{\"status\": \"API está funcionando\", \"endpoints\": [\"/empresas/count\", \"/empresas/avg-capital\", \"/empresas\", \"/dashboard\", \"/predicao\", \"/historico/*\"]}";
        });

        // Definir endpoints
        Spark.get("/empresas/count", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");

            if (cnae == null || municipio == null) {
                res.status(400);
                res.type("application/json");
                return "{\"error\": \"Parâmetros 'cnae' e 'municipio' são obrigatórios\"}";
            }

            try {
                int count = empresaDAOHolder[0].countEmpresasPorCnaeEMunicipio(cnae, municipio);
                res.type("application/json");
                return "{\"count\": " + count + ", \"cnae\": \"" + cnae + "\", \"municipio\": \"" + municipio + "\"}";
            } catch (SQLException e) {
                res.status(500);
                res.type("application/json");
                return "{\"error\": \"Erro ao acessar o banco de dados: " + e.getMessage() + "\"}";
            }
        });

        Spark.get("/empresas/avg-capital", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");

            if (cnae == null || municipio == null) {
                res.status(400);
                res.type("application/json");
                return "{\"error\": \"Parâmetros 'cnae' e 'municipio' são obrigatórios\"}";
            }

            try {
                double avg = empresaDAOHolder[0].avgCapitalSocialPorCnaeEMunicipio(cnae, municipio);
                res.type("application/json");
                return "{\"avg_capital_social\": " + avg + ", \"cnae\": \"" + cnae + "\", \"municipio\": \"" + municipio + "\"}";
            } catch (SQLException e) {
                res.status(500);
                res.type("application/json");
                return "{\"error\": \"Erro ao acessar o banco de dados: " + e.getMessage() + "\"}";
            }
        });

        Spark.get("/empresas", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");

            if (cnae == null || municipio == null) {
                res.status(400);
                res.type("application/json");
                return "{\"error\": \"Parâmetros 'cnae' e 'municipio' são obrigatórios\"}";
            }

            try {
                List<Empresa> empresas = empresaDAOHolder[0].listarEmpresasPorCnaeEMunicipio(cnae, municipio);
                res.type("application/json");
                return gson.toJson(empresas);
            } catch (SQLException e) {
                res.status(500);
                res.type("application/json");
                return "{\"error\": \"Erro ao acessar o banco de dados: " + e.getMessage() + "\"}";
            }
        });

        // Novo endpoint para predição de ML
        Spark.get("/predicao", (req, res) -> {
            if (mlServiceHolder[0] == null) {
                res.status(503);
                res.type("application/json");
                return "{\"error\": \"Serviço de ML não disponível\"}";
            }
            
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");
            String capitalStr = req.queryParams("capital");
            
            if (cnae == null || municipio == null || capitalStr == null) {
                res.status(400);
                res.type("application/json");
                return "{\"error\": \"Parâmetros 'cnae', 'municipio' e 'capital' são obrigatórios\"}";
            }
            
            try {
                double capitalSocial = Double.parseDouble(capitalStr);
                PredictionResult predicao = mlServiceHolder[0].preverSucesso(cnae, municipio, capitalSocial, empresaDAOHolder[0].getConnection());
                
                res.type("application/json");
                return gson.toJson(predicao);
                
            } catch (NumberFormatException e) {
                res.status(400);
                res.type("application/json");
                return "{\"error\": \"Parâmetro 'capital' deve ser um número válido\"}";
            } catch (Exception e) {
                res.status(500);
                res.type("application/json");
                return "{\"error\": \"Erro na predição: " + e.getMessage() + "\"}";
            }
        });

        // Endpoint para salvar no histórico
        Spark.post("/historico/salvar", (req, res) -> {
            try {
                HistoricoBusca historico = gson.fromJson(req.body(), HistoricoBusca.class);
                historicoDAOHolder[0].salvarBusca(historico);
                
                res.type("application/json");
                return "{\"status\": \"salvo\"}";
                
            } catch (Exception e) {
                res.status(500);
                res.type("application/json");
                return "{\"error\": \"Erro ao salvar histórico: " + e.getMessage() + "\"}";
            }
        });

        // Endpoint para listar histórico
        Spark.get("/historico/:sessionId", (req, res) -> {
            String sessionId = req.params(":sessionId");
            
            try {
                List<HistoricoBusca> historicos = historicoDAOHolder[0].listarPorSession(sessionId);
                res.type("application/json");
                return gson.toJson(historicos);
                
            } catch (SQLException e) {
                res.status(500);
                res.type("application/json");
                return "{\"error\": \"Erro ao carregar histórico: " + e.getMessage() + "\"}";
            }
        });

        // Endpoint para deletar do histórico
        Spark.delete("/historico/:id/:sessionId", (req, res) -> {
            try {
                int id = Integer.parseInt(req.params(":id"));
                String sessionId = req.params(":sessionId");
                
                boolean deletado = historicoDAOHolder[0].deletarHistorico(id, sessionId);
                
                res.type("application/json");
                return "{\"deletado\": " + deletado + "}";
                
            } catch (Exception e) {
                res.status(500);
                res.type("application/json");
                return "{\"error\": \"Erro ao deletar histórico: " + e.getMessage() + "\"}";
            }
        });

        // Endpoint dashboard atualizado com ML
        Spark.get("/dashboard", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");
            String capitalStr = req.queryParams("capital");
            String sessionId = req.queryParams("sessionId");

            if (cnae == null || municipio == null) {
                res.status(400);
                res.type("application/json");
                return "{\"error\": \"Parâmetros 'cnae' e 'municipio' são obrigatórios\"}";
            }

            try {
                int count = empresaDAOHolder[0].countEmpresasPorCnaeEMunicipio(cnae, municipio);
                double avgCapital = empresaDAOHolder[0].avgCapitalSocialPorCnaeEMunicipio(cnae, municipio);
                
                // Usar capital fornecido ou capital médio como fallback
                double capitalSocial = capitalStr != null ? Double.parseDouble(capitalStr) : avgCapital;
                
                Map<String, Object> dashboardData = new HashMap<>();
                dashboardData.put("quantidade_empresas", count);
                dashboardData.put("capital_social_medio", avgCapital);
                dashboardData.put("cnae", cnae);
                dashboardData.put("municipio", municipio);
                
                // Se o serviço de ML estiver disponível, usar predição real
                if (mlServiceHolder[0] != null) {
                    try {
                        PredictionResult predicao = mlServiceHolder[0].preverSucesso(cnae, municipio, capitalSocial, empresaDAOHolder[0].getConnection());
                        dashboardData.put("probabilidade_sucesso", predicao.getProbabilidadeSucesso());
                        dashboardData.put("classificacao_sucesso", predicao.getClassificacao());
                        dashboardData.put("fatores_criticos", predicao.getFatoresCriticos());
                        dashboardData.put("recomendacao", predicao.getRecomendacao());
                        
                        // Combinar estratégias padrão com recomendação do ML
                        String[] estrategias = new String[]{
                            "Analisar concorrência local",
                            "Estudar perfil demográfico da região",
                            "Avaliar sazonalidade do negócio",
                            predicao.getRecomendacao()
                        };
                        dashboardData.put("estrategias", estrategias);
                        
                    } catch (Exception e) {
                        System.err.println("Erro na predição ML: " + e.getMessage());
                        // Fallback para valores padrão se ML falhar
                        dashboardData.put("probabilidade_sucesso", 0.75);
                        dashboardData.put("classificacao_sucesso", "MEDIA");
                        dashboardData.put("fatores_criticos", new String[]{"Análise em andamento"});
                        dashboardData.put("recomendacao", "Considere uma análise de mercado detalhada");
                        dashboardData.put("estrategias", new String[]{
                            "Analisar concorrência local",
                            "Estudar perfil demográfico da região",
                            "Avaliar sazonalidade do negócio"
                        });
                    }
                } else {
                    // Fallback se ML não estiver disponível
                    dashboardData.put("probabilidade_sucesso", 0.75);
                    dashboardData.put("classificacao_sucesso", "MEDIA");
                    dashboardData.put("fatores_criticos", new String[]{"Serviço de análise não disponível"});
                    dashboardData.put("recomendacao", "Realize uma pesquisa de mercado tradicional");
                    dashboardData.put("estrategias", new String[]{
                        "Analisar concorrência local",
                        "Estudar perfil demográfico da região",
                        "Avaliar sazonalidade do negócio"
                    });
                }
                
                // Salvar no histórico se sessionId foi fornecido
                if (sessionId != null && !sessionId.isEmpty()) {
                    try {
                        HistoricoBusca historico = new HistoricoBusca(
                            sessionId,
                            cnae,
                            "Desconhecido", // Descrição do CNAE - precisaríamos carregar o mapeamento
                            municipio, 
                            "Desconhecido", // Nome do município - precisaríamos carregar o mapeamento
                            capitalSocial,
                            count,
                            avgCapital,
                            (Double) dashboardData.get("probabilidade_sucesso")
                        );
                        historicoDAOHolder[0].salvarBusca(historico);
                    } catch (SQLException e) {
                        System.err.println("Erro ao salvar no histórico: " + e.getMessage());
                    }
                }
                
                res.type("application/json");
                return gson.toJson(dashboardData);
                
            } catch (SQLException e) {
                res.status(500);
                res.type("application/json");
                return "{\"error\": \"Erro ao acessar o banco de dados: " + e.getMessage() + "\"}";
            } catch (NumberFormatException e) {
                res.status(400);
                res.type("application/json");
                return "{\"error\": \"Parâmetro 'capital' deve ser um número válido\"}";
            }
        });

        System.out.println("API rodando em http://localhost:8081");
        System.out.println("Endpoints disponíveis:");
        System.out.println("  GET /health");
        System.out.println("  GET /empresas/count?cnae=XXXXXXX&municipio=NOME");
        System.out.println("  GET /empresas/avg-capital?cnae=XXXXXXX&municipio=NOME");
        System.out.println("  GET /empresas?cnae=XXXXXXX&municipio=NOME");
        System.out.println("  GET /dashboard?cnae=XXXXXXX&municipio=NOME&capital=YYYYY&sessionId=ZZZZZ");
        System.out.println("  GET /predicao?cnae=XXXXXXX&municipio=NOME&capital=YYYYY");
        System.out.println("  POST /historico/salvar");
        System.out.println("  GET /historico/:sessionId");
        System.out.println("  DELETE /historico/:id/:sessionId");
    }
}