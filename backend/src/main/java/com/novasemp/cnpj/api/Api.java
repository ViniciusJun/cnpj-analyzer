package com.novasemp.cnpj.api;

import com.google.gson.Gson;
import com.novasemp.cnpj.dao.EmpresaDAO;
import com.novasemp.cnpj.dao.HistoricoDAO;
import com.novasemp.cnpj.model.Empresa;
import com.novasemp.cnpj.model.HistoricoBusca;
import com.novasemp.cnpj.ml.service.MLModelService;
import com.novasemp.cnpj.ml.model.PredictionResult;
import com.novasemp.cnpj.util.CacheManager;
import spark.Spark;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.File;

public class Api {

    private static void logDadosDisponiveis(EmpresaDAO empresaDAO) {
        try {
            String sql = "SELECT DISTINCT cnae_principal, municipio FROM empresas LIMIT 5";
            
            try (Statement stmt = empresaDAO.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                System.out.println("\n📊 Dados disponíveis para teste:");
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
        // CONFIGURAÇÃO PARA PRODUÇÃO NO RAILWAY
        // ======================================
        
        // Porta dinâmica para Railway - usar final
        final int port;
        if (System.getenv("PORT") != null) {
            port = Integer.parseInt(System.getenv("PORT"));
        } else {
            port = 8080;
        }
        
        // Configurar caminho do banco para produção
        String dbPath = "data/processed/cnpj_data.db";
        
        // Garantir que diretórios existam
        new File("data/processed").mkdirs();
        
        System.out.println("🚀 Iniciando CNPJ Analyzer Backend...");
        System.out.println("📍 Porta: " + port);
        System.out.println("🗃️  Banco: " + dbPath);
        
        final EmpresaDAO[] empresaDAOHolder = new EmpresaDAO[1];
        final MLModelService[] mlServiceHolder = new MLModelService[1];
        final HistoricoDAO[] historicoDAOHolder = new HistoricoDAO[1];
        
        try {
            empresaDAOHolder[0] = new EmpresaDAO(dbPath);
            System.out.println("✅ Conectado ao banco de dados com sucesso!");
            
            // Inicializar serviço de ML com tratamento robusto
            try {
                mlServiceHolder[0] = new MLModelService(empresaDAOHolder[0].getConnection());
                if (mlServiceHolder[0].isModeloTreinado()) {
                    System.out.println("✅ Serviço de ML inicializado com sucesso!");
                    System.out.println("📊 Status ML: " + mlServiceHolder[0].getStatus());
                } else {
                    System.out.println("⚠️ Serviço de ML inicializado em modo fallback");
                    System.out.println("📊 Status ML: " + mlServiceHolder[0].getStatus());
                }
            } catch (Exception e) {
                System.err.println("❌ Erro ao inicializar serviço de ML: " + e.getMessage());
                System.out.println("🔄 Continuando com análise inteligente...");
                mlServiceHolder[0] = new MLModelService(empresaDAOHolder[0].getConnection());
            }

            historicoDAOHolder[0] = new HistoricoDAO(empresaDAOHolder[0].getConnection());
            System.out.println("✅ Sistema de histórico inicializado com sucesso!");
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar ao banco de dados: " + e.getMessage());
            return;
        }

        logDadosDisponiveis(empresaDAOHolder[0]);

        Gson gson = new Gson();
        final CacheManager cacheManager = CacheManager.getInstance();

        // CONFIGURAÇÃO SPARK PARA PRODUÇÃO
        // ================================
        Spark.port(port);
        Spark.ipAddress("0.0.0.0"); // Importante para Railway

        // Configurar CORS para produção
        Spark.options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept, X-Requested-With");
            response.header("Access-Control-Allow-Credentials", "true");
            response.header("Access-Control-Max-Age", "3600");
        });

        // Error Handler Global
        Spark.exception(Exception.class, (exception, request, response) -> {
            System.err.println("❌ Erro não tratado: " + exception.getMessage());
            exception.printStackTrace();
            
            response.status(500);
            response.type("application/json");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("erro", "Erro interno do servidor");
            errorResponse.put("codigo", 500);
            errorResponse.put("mensagem", "Tente novamente em alguns instantes");
            
            response.body(gson.toJson(errorResponse));
        });

        // ENDPOINT DE HEALTH CHECK MELHORADO
        // ===================================
        Spark.get("/health", (req, res) -> {
            res.type("application/json");
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "API funcionando");
            health.put("service", "CNPJ Analyzer Backend");
            health.put("version", "1.0.0");
            health.put("environment", "production");
            health.put("ml_online", mlServiceHolder[0] != null && mlServiceHolder[0].isModeloTreinado());
            health.put("ml_status", mlServiceHolder[0] != null ? mlServiceHolder[0].getStatus() : "NÃO INICIALIZADO");
            health.put("database", empresaDAOHolder[0] != null ? "CONECTADO" : "ERRO");
            health.put("timestamp", System.currentTimeMillis());
            health.put("port", port); // ✅ Agora pode usar port porque é final
            
            // Verificar banco
            try {
                String testSql = "SELECT COUNT(*) as count FROM sqlite_master WHERE type='table'";
                try (Statement stmt = empresaDAOHolder[0].getConnection().createStatement();
                     ResultSet rs = stmt.executeQuery(testSql)) {
                    if (rs.next()) {
                        health.put("database_tables", rs.getInt("count"));
                    }
                }
            } catch (Exception e) {
                health.put("database_error", e.getMessage());
            }
            
            return gson.toJson(health);
        });

        // Endpoint de diagnóstico ML (mantido igual)
        Spark.get("/debug/ml", (req, res) -> {
            Map<String, Object> debugInfo = new HashMap<>();
            
            if (mlServiceHolder[0] != null) {
                debugInfo.put("ml_status", mlServiceHolder[0].getStatus());
                debugInfo.put("ml_treinado", mlServiceHolder[0].isModeloTreinado());
                debugInfo.put("ml_amostras", mlServiceHolder[0].getTotalAmostras());
                debugInfo.put("timestamp", System.currentTimeMillis());
            } else {
                debugInfo.put("ml_status", "SERVIÇO NÃO INICIALIZADO");
            }
            
            try {
                int totalEmpresas = empresaDAOHolder[0].countEmpresasPorCnaeEMunicipio("4721102", "3550308");
                debugInfo.put("dados_teste_cnae_4721102", totalEmpresas);
                debugInfo.put("banco_conectado", true);
                
                // Contar total de empresas
                String countSql = "SELECT COUNT(*) as total FROM empresas";
                try (Statement stmt = empresaDAOHolder[0].getConnection().createStatement();
                     ResultSet rs = stmt.executeQuery(countSql)) {
                    if (rs.next()) {
                        debugInfo.put("total_empresas_banco", rs.getInt("total"));
                    }
                }
            } catch (Exception e) {
                debugInfo.put("banco_conectado", false);
                debugInfo.put("erro_banco", e.getMessage());
            }
            
            res.type("application/json");
            return gson.toJson(debugInfo);
        });

        // Endpoint específico para análise ML (mantido igual)
        Spark.get("/ml/predicao", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");
            String capitalStr = req.queryParams("capital_social");
            
            System.out.println("🎯 Recebida requisição ML - CNAE: " + cnae + ", Município: " + municipio + ", Capital: " + capitalStr);
            
            String cacheKey = "predicao_" + cnae + "_" + municipio + "_" + capitalStr;
            Map<String, Object> cachedResponse = (Map<String, Object>) cacheManager.get(cacheKey);
            if (cachedResponse != null) {
                System.out.println("⚡ Retornando do cache");
                res.type("application/json");
                return gson.toJson(cachedResponse);
            }
            
            if (cnae == null || municipio == null || capitalStr == null) {
                res.status(400);
                return "{\"erro\": \"Parâmetros 'cnae', 'municipio' e 'capital_social' são obrigatórios\"}";
            }
            
            try {
                double capitalSocial = Double.parseDouble(capitalStr);
                PredictionResult predicao;
                
                if (mlServiceHolder[0] != null) {
                    predicao = mlServiceHolder[0].preverSucesso(cnae, municipio, capitalSocial, empresaDAOHolder[0].getConnection());
                } else {
                    predicao = new PredictionResult(0.5, "MEDIA", 
                        new String[]{"Serviço ML indisponível"}, 
                        "Usando análise básica");
                }
                
                // Response format otimizado para Android
                Map<String, Object> response = new HashMap<>();
                response.put("sucesso", true);
                response.put("scoreML", (int)(predicao.getProbabilidadeSucesso() * 1000));
                response.put("probabilidadeSucesso", predicao.getProbabilidadeSucesso());
                response.put("modeloUtilizado", mlServiceHolder[0] != null && mlServiceHolder[0].isModeloTreinado() ? "RandomForest_CNPJ" : "Análise_Inteligente");
                response.put("classificacao", predicao.getClassificacao());
                response.put("fatoresCriticos", predicao.getFatoresCriticos());
                response.put("recomendacao", predicao.getRecomendacao());
                response.put("timestamp", System.currentTimeMillis());
                response.put("mlOnline", mlServiceHolder[0] != null && mlServiceHolder[0].isModeloTreinado());
                
                cacheManager.put(cacheKey, response, 300000);
                
                System.out.println("✅ Predição concluída: " + predicao.getClassificacao() + " (" + (int)(predicao.getProbabilidadeSucesso() * 100) + "%)");
                
                res.type("application/json");
                return gson.toJson(response);
                
            } catch (Exception e) {
                System.err.println("❌ Erro na predição: " + e.getMessage());
                res.status(500);
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("sucesso", false);
                errorResponse.put("erro", "Erro na predição");
                errorResponse.put("mensagem", e.getMessage());
                errorResponse.put("fallback", true);
                
                return gson.toJson(errorResponse);
            }
        });

        // Endpoint para análise de mercado (mantido igual)
        Spark.get("/ml/analise-mercado", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");
            
            String cacheKey = "analise_" + cnae + "_" + municipio;
            Map<String, Object> cachedResponse = (Map<String, Object>) cacheManager.get(cacheKey);
            if (cachedResponse != null) {
                res.type("application/json");
                return gson.toJson(cachedResponse);
            }
            
            if (cnae == null || municipio == null) {
                res.status(400);
                return "{\"erro\": \"Parâmetros 'cnae' e 'municipio' são obrigatórios\"}";
            }
            
            try {
                Map<String, Object> analise = new HashMap<>();
                
                int totalEmpresas = empresaDAOHolder[0].countEmpresasPorCnaeEMunicipio(cnae, municipio);
                double capitalMedio = empresaDAOHolder[0].avgCapitalSocialPorCnaeEMunicipio(cnae, municipio);
                int totalMunicipio = 0;
                
                try {
                    totalMunicipio = empresaDAOHolder[0].countEmpresasPorCnaeEMunicipio("", municipio);
                } catch (Exception e) {
                    totalMunicipio = totalEmpresas * 10; // Estimativa
                }
                
                analise.put("totalEmpresasRegiao", totalEmpresas);
                analise.put("capitalSocialMedio", capitalMedio);
                analise.put("saturacaoMercado", calcularSaturacao(totalEmpresas));
                analise.put("participacaoSetor", totalMunicipio > 0 ? (double) totalEmpresas / totalMunicipio : 0);
                analise.put("concorrentesProximos", Math.min(totalEmpresas / 10, 50));
                analise.put("tendencia", analisarTendencia(cnae, municipio));
                analise.put("timestamp", System.currentTimeMillis());
                
                cacheManager.put(cacheKey, analise, 600000);
                
                res.type("application/json");
                return gson.toJson(analise);
                
            } catch (Exception e) {
                res.status(500);
                return "{\"erro\": \"Erro na análise: " + e.getMessage() + "\"}";
            }
        });

        // Endpoint de tendência (mantido igual)
        Spark.get("/ml/tendencia-setor", (req, res) -> {
            String cnae = req.queryParams("cnae");
            
            if (cnae == null) {
                res.status(400);
                return "{\"erro\": \"Parâmetro cnae é obrigatório\"}";
            }
            
            Map<String, Object> tendencia = new HashMap<>();
            tendencia.put("cnae", cnae);
            tendencia.put("tendencia", "CRESCIMENTO");
            tendencia.put("variacao", 12.5);
            tendencia.put("periodo", "ULTIMO_ANO");
            tendencia.put("timestamp", System.currentTimeMillis());
            
            res.type("application/json");
            return gson.toJson(tendencia);
        });

        // Endpoint de feedback (mantido igual)
        Spark.post("/ml/feedback", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");
            String capitalStr = req.queryParams("capital_social");
            String probabilidadeStr = req.queryParams("probabilidade_real");
            String sucessoStr = req.queryParams("sucesso_real");
            
            System.out.println("📝 Feedback recebido - CNAE: " + cnae + ", Sucesso: " + sucessoStr);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "feedback recebido");
            response.put("timestamp", System.currentTimeMillis());
            
            res.type("application/json");
            return gson.toJson(response);
        });

        // Endpoints de histórico (mantidos iguais)
        Spark.post("/historico", (req, res) -> {
            try {
                HistoricoBusca historico = gson.fromJson(req.body(), HistoricoBusca.class);
                historicoDAOHolder[0].salvarBusca(historico);
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "salvo");
                response.put("timestamp", System.currentTimeMillis());
                
                res.type("application/json");
                return gson.toJson(response);
                
            } catch (Exception e) {
                res.status(500);
                return "{\"error\": \"Erro ao salvar histórico: " + e.getMessage() + "\"}";
            }
        });

        Spark.get("/historico", (req, res) -> {
            String sessionId = req.queryParams("sessionId");
            
            if (sessionId == null) {
                res.status(400);
                return "{\"error\": \"Parâmetro sessionId é obrigatório\"}";
            }
            
            try {
                List<HistoricoBusca> historicos = historicoDAOHolder[0].listarPorSession(sessionId);
                res.type("application/json");
                return gson.toJson(historicos);
                
            } catch (SQLException e) {
                res.status(500);
                return "{\"error\": \"Erro ao carregar histórico: " + e.getMessage() + "\"}";
            }
        });

        Spark.delete("/historico", (req, res) -> {
            try {
                String idStr = req.queryParams("id");
                String sessionId = req.queryParams("sessionId");
                
                if (idStr == null || sessionId == null) {
                    res.status(400);
                    return "{\"error\": \"Parâmetros id e sessionId são obrigatórios\"}";
                }
                
                int id = Integer.parseInt(idStr);
                boolean deletado = historicoDAOHolder[0].deletarHistorico(id, sessionId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("deletado", deletado);
                response.put("timestamp", System.currentTimeMillis());
                
                res.type("application/json");
                return gson.toJson(response);
                
            } catch (Exception e) {
                res.status(500);
                return "{\"error\": \"Erro ao deletar histórico: " + e.getMessage() + "\"}";
            }
        });

        Spark.get("/ml/metricas", (req, res) -> {
            if (mlServiceHolder[0] != null && mlServiceHolder[0].isModeloTreinado()) {
                Map<String, Object> metricas = mlServiceHolder[0].getMetricasModelo();
                
                // Adicionar informações do sistema
                metricas.put("timestamp", System.currentTimeMillis());
                metricas.put("endpoint", "/ml/metricas");
                
                res.type("application/json");
                return gson.toJson(metricas);
            } else {
                res.status(503);
                Map<String, Object> erro = new HashMap<>();
                erro.put("erro", "Serviço ML não disponível");
                erro.put("status", mlServiceHolder[0] != null ? mlServiceHolder[0].getStatus() : "NÃO INICIALIZADO");
                return gson.toJson(erro);
            }
        });

        // Endpoint dashboard (mantido igual)
        Spark.get("/dashboard", (req, res) -> {
            String cnae = req.queryParams("cnae");
            String municipio = req.queryParams("municipio");
            String capitalStr = req.queryParams("capital");
            String sessionId = req.queryParams("sessionId");

            if (cnae == null || municipio == null) {
                res.status(400);
                return "{\"error\": \"Parâmetros 'cnae' e 'municipio' são obrigatórios\"}";
            }

            try {
                int count = empresaDAOHolder[0].countEmpresasPorCnaeEMunicipio(cnae, municipio);
                double avgCapital = empresaDAOHolder[0].avgCapitalSocialPorCnaeEMunicipio(cnae, municipio);
                
                double capitalSocial = capitalStr != null ? Double.parseDouble(capitalStr) : avgCapital;
                
                Map<String, Object> dashboardData = new HashMap<>();
                dashboardData.put("quantidade_empresas", count);
                dashboardData.put("capital_social_medio", avgCapital);
                dashboardData.put("cnae", cnae);
                dashboardData.put("municipio", municipio);
                
                if (mlServiceHolder[0] != null) {
                    try {
                        PredictionResult predicao = mlServiceHolder[0].preverSucesso(cnae, municipio, capitalSocial, empresaDAOHolder[0].getConnection());
                        dashboardData.put("probabilidade_sucesso", predicao.getProbabilidadeSucesso());
                        dashboardData.put("classificacao_sucesso", predicao.getClassificacao());
                        dashboardData.put("fatores_criticos", predicao.getFatoresCriticos());
                        dashboardData.put("recomendacao", predicao.getRecomendacao());
                        
                        String[] estrategias = new String[]{
                            "Analisar concorrência local",
                            "Estudar perfil demográfico da região",
                            "Avaliar sazonalidade do negócio",
                            predicao.getRecomendacao()
                        };
                        dashboardData.put("estrategias", estrategias);
                        
                    } catch (Exception e) {
                        System.err.println("Erro na predição ML: " + e.getMessage());
                        dashboardData.put("probabilidade_sucesso", 0.65);
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
                    dashboardData.put("probabilidade_sucesso", 0.65);
                    dashboardData.put("classificacao_sucesso", "MEDIA");
                    dashboardData.put("fatores_criticos", new String[]{"Serviço de análise não disponível"});
                    dashboardData.put("recomendacao", "Realize uma pesquisa de mercado tradicional");
                    dashboardData.put("estrategias", new String[]{
                        "Analisar concorrência local",
                        "Estudar perfil demográfico da região",
                        "Avaliar sazonalidade do negócio"
                    });
                }
                
                if (sessionId != null && !sessionId.isEmpty()) {
                    try {
                        HistoricoBusca historico = new HistoricoBusca(
                            sessionId,
                            cnae,
                            "Desconhecido",
                            municipio, 
                            "Desconhecido",
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
                return "{\"error\": \"Erro ao acessar o banco de dados: " + e.getMessage() + "\"}";
            } catch (NumberFormatException e) {
                res.status(400);
                return "{\"error\": \"Parâmetro 'capital' deve ser um número válido\"}";
            }
        });

        System.out.println("\n🎉 API CNPJ Analyzer rodando na porta: " + port);
        System.out.println("🚀 PRONTO PARA PRODUÇÃO NO RAILWAY!");
        System.out.println("📊 Status do ML: " + (mlServiceHolder[0] != null ? mlServiceHolder[0].getStatus() : "NÃO INICIALIZADO"));
        System.out.println("\n🔗 Endpoints disponíveis:");
        System.out.println("  GET  /health                          - Status da API");
        System.out.println("  GET  /debug/ml                        - Diagnóstico ML");
        System.out.println("  GET  /ml/predicao                     - Predição ML");
        System.out.println("  GET  /ml/analise-mercado              - Análise de mercado");
        System.out.println("  GET  /ml/tendencia-setor              - Tendência do setor");
        System.out.println("  POST /ml/feedback                     - Enviar feedback");
        System.out.println("  GET  /dashboard                       - Dashboard completo");
        System.out.println("  POST /historico                       - Salvar histórico");
        System.out.println("  GET  /historico                       - Listar histórico");
        System.out.println("  DELETE /historico                     - Deletar histórico\n");
    }

    private static String calcularSaturacao(int totalEmpresas) {
        if (totalEmpresas < 10) return "BAIXA";
        if (totalEmpresas < 50) return "MEDIA";
        return "ALTA";
    }

    private static String analisarTendencia(String cnae, String municipio) {
        return "CRESCIMENTO_MODERADO";
    }
}