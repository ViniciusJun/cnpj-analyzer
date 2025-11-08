package com.novasemp.cnpjmobile.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.model.HistoricoBusca;
import com.novasemp.cnpjmobile.model.PredicaoResponse;
import com.novasemp.cnpjmobile.service.ApiService;
import com.novasemp.cnpjmobile.service.RetrofitClient;
import com.novasemp.cnpjmobile.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataAnalysisActivity extends AppCompatActivity {

    private EditText editTextCnae;
    private EditText editTextMunicipio;
    private EditText editTextCapital;
    private Button buttonAnalisar;
    private ProgressBar progressBar;
    private TextView textViewResultados;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("DEBUG: DataAnalysisActivity - onCreate iniciado");

        try {
            System.out.println("DEBUG: Tentando carregar layout...");
            setContentView(R.layout.activity_data_analysis);
            System.out.println("DEBUG: Layout carregado com sucesso");

            sessionManager = new SessionManager(this);
            System.out.println("DEBUG: DataAnalysisActivity - SessionManager inicializado");

            initializeUIComponents();
            System.out.println("DEBUG: Componentes UI inicializados");

            setupButtonListeners();
            System.out.println("DEBUG: Listeners configurados");

            System.out.println("DEBUG: Tentando inicializar ApiService...");
            try {
                apiService = RetrofitClient.getApiService();
                System.out.println("DEBUG: ApiService inicializado: " + (apiService != null));

                if (apiService != null) {
                    System.out.println("DEBUG: Iniciando teste do endpoint ML...");
                    testarEndpointML();
                } else {
                    System.out.println("DEBUG: ApiService é null - não foi possível testar endpoint ML");
                }

            } catch (Exception e) {
                System.out.println("DEBUG: AVISO - ApiService não pôde ser inicializado: " + e.getMessage());
                apiService = null;
                Toast.makeText(this, "Funcionalidade online indisponível. Usando modo offline.", Toast.LENGTH_LONG).show();
            }

            System.out.println("DEBUG: DataAnalysisActivity - onCreate concluído com SUCESSO");

        } catch (Exception e) {
            System.out.println("DEBUG: DataAnalysisActivity - ERRO CRÍTICO: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = "Erro: " + e.getClass().getSimpleName();
            if (e.getMessage() != null) {
                errorMsg += " - " + e.getMessage();
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void testarEndpointML() {
        System.out.println("DEBUG: DataAnalysisActivity - Testando endpoint de ML...");

        if (apiService == null) {
            System.out.println("DEBUG: ApiService não disponível para teste ML");
            return;
        }

        Call<PredicaoResponse> testCall = apiService.getPredicao("4711301", "3550308", 50000.0);

        testCall.enqueue(new Callback<PredicaoResponse>() {
            @Override
            public void onResponse(Call<PredicaoResponse> call, Response<PredicaoResponse> response) {
                System.out.println("DEBUG: Teste ML - Código: " + response.code());
                System.out.println("DEBUG: Teste ML - Sucesso: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    PredicaoResponse testResponse = response.body();
                    System.out.println("DEBUG: Teste ML - Success: " + testResponse.isSuccess());
                    System.out.println("DEBUG: Teste ML - Message: " + testResponse.getMessage());

                    if (testResponse.isSuccess() && testResponse.getData() != null) {
                        System.out.println("DEBUG: ✅ Endpoint ML funcionando corretamente!");
                        System.out.println("DEBUG: Probabilidade: " + testResponse.getData().getProbabilidadeSucesso());
                        System.out.println("DEBUG: Classificação: " + testResponse.getData().getClassificacao());
                    } else {
                        System.out.println("DEBUG: ⚠️ Endpoint ML retornou success=false");
                    }
                } else {
                    System.out.println("DEBUG: ❌ Endpoint ML não respondeu corretamente");
                }
            }

            @Override
            public void onFailure(Call<PredicaoResponse> call, Throwable t) {
                System.out.println("DEBUG: ❌ Endpoint ML inacessível: " + t.getMessage());
            }
        });
    }

    private void initializeUIComponents() {
        editTextCnae = findViewById(R.id.editTextCnae);
        editTextMunicipio = findViewById(R.id.editTextMunicipio);
        editTextCapital = findViewById(R.id.editTextCapital);
        buttonAnalisar = findViewById(R.id.buttonAnalisar);
        textViewResultados = findViewById(R.id.textViewResultados);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupButtonListeners() {
        buttonAnalisar.setOnClickListener(v -> performAdvancedAnalysis());
    }

    private void performAdvancedAnalysis() {
        String cnae = editTextCnae.getText().toString().trim();
        String municipio = editTextMunicipio.getText().toString().trim();
        String capitalStr = editTextCapital.getText().toString().trim();

        if (cnae.isEmpty() || municipio.isEmpty()) {
            Toast.makeText(this, "Preencha CNAE e Município", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cnae.matches("\\d+") || !municipio.matches("\\d+")) {
            Toast.makeText(this, "CNAE e Município devem ser códigos numéricos", Toast.LENGTH_SHORT).show();
            return;
        }

        double capital = 0.0;
        if (!capitalStr.isEmpty()) {
            try {
                capital = Double.parseDouble(capitalStr);
                if (capital < 0) {
                    Toast.makeText(this, "Capital social não pode ser negativo", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Capital social deve ser um número válido", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        buttonAnalisar.setEnabled(false);
        textViewResultados.setText("");

        final String finalCnae = cnae;
        final String finalMunicipio = municipio;
        final double finalCapital = capital;

        textViewResultados.setText("Executando análise preditiva...\n\nAguarde...");
        buttonAnalisar.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        if (apiService == null) {
            System.out.println("DEBUG: ApiService não disponível - usando modo simulador");
            new android.os.Handler().postDelayed(() -> {
                String simulationResult = simulateAdvancedAnalysis(cnae, municipio, finalCapital);
                textViewResultados.setText("Modo Offline - Dados Simulados:\n\n" + simulationResult);
                buttonAnalisar.setEnabled(true);
                progressBar.setVisibility(View.GONE);

                // APENAS SALVAR NO HISTÓRICO - NÃO ABRIR DASHBOARD
                salvarAnaliseNoHistorico(finalCnae, finalMunicipio, finalCapital);
            }, 2000);
            return;
        }

        System.out.println("DEBUG: Iniciando análise com ML...");
        tryPredictionML(finalCnae, finalMunicipio, finalCapital);
    }

    private void tryPredictionML(String cnae, String municipio, double capital) {
        System.out.println("DEBUG: Tentando endpoint de predição ML...");

        Call<PredicaoResponse> call = apiService.getPredicao(cnae, municipio, capital);

        call.enqueue(new Callback<PredicaoResponse>() {
            @Override
            public void onResponse(Call<PredicaoResponse> call, Response<PredicaoResponse> response) {
                System.out.println("DEBUG: Resposta da predição - Código: " + response.code());
                buttonAnalisar.setEnabled(true);
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    PredicaoResponse predicaoResponse = response.body();
                    predicaoResponse.logDebug();

                    if (predicaoResponse.isSuccess() && predicaoResponse.getData() != null) {
                        System.out.println("DEBUG: ✅ Predição ML bem-sucedida!");
                        displayPredictionResults(predicaoResponse);
                        // APENAS SALVAR NO HISTÓRICO - NÃO ABRIR DASHBOARD
                        salvarAnaliseNoHistorico(cnae, municipio, capital);
                    } else {
                        System.out.println("DEBUG: Predição ML retornou success=false, usando predição local...");
                        obterDadosParaPredicaoLocal(cnae, municipio, capital);
                    }
                } else {
                    System.out.println("DEBUG: Resposta não sucedida - usando predição local...");
                    obterDadosParaPredicaoLocal(cnae, municipio, capital);
                }
            }

            @Override
            public void onFailure(Call<PredicaoResponse> call, Throwable t) {
                System.out.println("DEBUG: Falha na predição ML: " + t.getMessage());
                tryDashboardFallback(cnae, municipio, capital);
                buttonAnalisar.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void obterDadosParaPredicaoLocal(String cnae, String municipio, double capital) {
        System.out.println("DEBUG: Obtendo dados do dashboard para predição local...");

        Call<DashboardData> dashboardCall = apiService.getDashboardData(cnae, municipio);
        dashboardCall.enqueue(new Callback<DashboardData>() {
            @Override
            public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardData dashboardData = response.body();
                    System.out.println("DEBUG: Dados do dashboard obtidos para predição local");

                    PredicaoResponse predicaoLocal = criarPredicaoLocal(dashboardData, cnae, municipio, capital);
                    displayPredictionResults(predicaoLocal);

                    // APENAS SALVAR NO HISTÓRICO - NÃO ABRIR DASHBOARD
                    salvarAnaliseNoHistorico(cnae, municipio, capital);
                } else {
                    System.out.println("DEBUG: Falha ao obter dados do dashboard - usando simulação básica");
                    String simulationResult = simulateAdvancedAnalysis(cnae, municipio, capital);
                    textViewResultados.setText("📊 ANÁLISE PREDITIVA (Simulação)\n\n" + simulationResult);
                    buttonAnalisar.setEnabled(true);

                    // APENAS SALVAR NO HISTÓRICO - NÃO ABRIR DASHBOARD
                    salvarAnaliseNoHistorico(cnae, municipio, capital);
                }
            }

            @Override
            public void onFailure(Call<DashboardData> call, Throwable t) {
                System.out.println("DEBUG: Falha ao obter dados do dashboard - usando simulação básica");
                String simulationResult = simulateAdvancedAnalysis(cnae, municipio, capital);
                textViewResultados.setText("📊 ANÁLISE PREDITIVA (Simulação)\n\n" + simulationResult);
                buttonAnalisar.setEnabled(true);

                // APENAS SALVAR NO HISTÓRICO - NÃO ABRIR DASHBOARD
                salvarAnaliseNoHistorico(cnae, municipio, capital);
            }
        });
    }

    private void tryDashboardFallback(String cnae, String municipio, double capital) {
        System.out.println("DEBUG: Usando fallback do dashboard...");
        fetchDashboardDataAsFallback(cnae, municipio, capital);
    }

    private void displayPredictionResults(PredicaoResponse response) {
        PredicaoResponse.PredicaoData data = response.getData();

        StringBuilder results = new StringBuilder();
        results.append("🎯 PREDIÇÃO COM MACHINE LEARNING\n\n");

        double probabilidade = data.getProbabilidadeSucesso();
        String corProbabilidade;
        if (probabilidade >= 0.7) {
            corProbabilidade = "🟢";
        } else if (probabilidade >= 0.5) {
            corProbabilidade = "🟡";
        } else {
            corProbabilidade = "🔴";
        }

        results.append("📊 PROBABILIDADE DE SUCESSO: ")
                .append(String.format("%.1f", data.getProbabilidadeSucesso() * 100))
                .append("%\n")
                .append(corProbabilidade)
                .append(" ")
                .append(String.format("%.1f", probabilidade * 100))
                .append("%\n");

        results.append("🏷️ CLASSIFICAÇÃO: ").append(data.getClassificacao()).append("\n\n");

        results.append("📈 DADOS DO MERCADO:\n");
        results.append("• ").append(data.getEmpresasSimilares()).append(" empresas similares\n");
        results.append("• Capital médio: R$ ").append(String.format("%.2f", data.getCapitalMedio())).append("\n\n");

        if (data.getFatoresPositivos() != null && !data.getFatoresPositivos().isEmpty()) {
            results.append("✅ FATORES POSITIVOS:\n");
            for (String fator : data.getFatoresPositivos()) {
                results.append("• ").append(fator).append("\n");
            }
            results.append("\n");
        }

        if (data.getFatoresNegativos() != null && !data.getFatoresNegativos().isEmpty()) {
            results.append("⚠️ FATORES DE ATENÇÃO:\n");
            for (String fator : data.getFatoresNegativos()) {
                results.append("• ").append(fator).append("\n");
            }
            results.append("\n");
        }

        results.append("💡 RECOMENDAÇÃO:\n").append(data.getRecomendacao());

        SpannableStringBuilder spannableResults = new SpannableStringBuilder(results.toString());

        String classificacao = data.getClassificacao();
        int startIndex = results.indexOf("CLASSIFICAÇÃO: ");
        if (startIndex != -1) {
            startIndex += "CLASSIFICAÇÃO: ".length();
            int endIndex = results.indexOf("\n", startIndex);
            if (endIndex == -1) endIndex = results.length();

            int corClassificacao;
            switch (classificacao != null ? classificacao.toUpperCase() : "") {
                case "ALTA":
                    corClassificacao = Color.parseColor("#4CAF50");
                    break;
                case "MODERADA":
                    corClassificacao = Color.parseColor("#FF9800");
                    break;
                case "BAIXA":
                    corClassificacao = Color.parseColor("#F44336");
                    break;
                default:
                    corClassificacao = Color.parseColor("#757575");
            }

            spannableResults.setSpan(new ForegroundColorSpan(corClassificacao),
                    startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textViewResultados.setText(spannableResults);
        buttonAnalisar.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private void fetchDashboardDataAsFallback(String cnae, String municipio, double capital) {
        Call<DashboardData> dashboardCall = apiService.getDashboardData(cnae, municipio);

        dashboardCall.enqueue(new Callback<DashboardData>() {
            @Override
            public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardData dashboardData = response.body();
                    String simulationResult = createEnhancedSimulation(cnae, municipio, capital, dashboardData);
                    textViewResultados.setText("📊 ANÁLISE AVANÇADA (Dados Consolidados)\n\n" + simulationResult);

                    // APENAS SALVAR NO HISTÓRICO - NÃO ABRIR DASHBOARD
                    salvarAnaliseNoHistorico(cnae, municipio, capital);
                } else {
                    String simulationResult = simulateAdvancedAnalysis(cnae, municipio, capital);
                    textViewResultados.setText("📊 ANÁLISE PREDITIVA (Simulação)\n\n" + simulationResult);

                    // APENAS SALVAR NO HISTÓRICO - NÃO ABRIR DASHBOARD
                    salvarAnaliseNoHistorico(cnae, municipio, capital);
                }
                buttonAnalisar.setEnabled(true);
            }

            @Override
            public void onFailure(Call<DashboardData> call, Throwable t) {
                String simulationResult = simulateAdvancedAnalysis(cnae, municipio, capital);
                textViewResultados.setText("📊 ANÁLISE PREDITIVA (Simulação)\n\n" + simulationResult);
                buttonAnalisar.setEnabled(true);

                // APENAS SALVAR NO HISTÓRICO - NÃO ABRIR DASHBOARD
                salvarAnaliseNoHistorico(cnae, municipio, capital);
            }
        });
    }

    private String createEnhancedSimulation(String cnae, String municipio, double capital, DashboardData dashboardData) {
        int totalEmpresas = dashboardData.getQuantidadeEmpresas();
        double capitalMedio = dashboardData.getCapitalSocialMedio();
        double probabilidadeSucesso = dashboardData.getProbabilidadeSucesso();
        String classificacao = dashboardData.getClassificacaoSucesso();
        String[] fatoresCriticos = dashboardData.getFatoresCriticos();
        String recomendacao = dashboardData.getRecomendacao();
        String[] estrategias = dashboardData.getEstrategias();

        StringBuilder results = new StringBuilder();

        results.append("📊 PROBABILIDADE DE SUCESSO: ")
                .append(String.format("%.1f", probabilidadeSucesso * 100))
                .append("%\n");

        results.append("🏷️ CLASSIFICAÇÃO: ").append(classificacao).append("\n\n");

        results.append("📈 DADOS REAIS DO MERCADO:\n");
        results.append("• ").append(totalEmpresas).append(" empresas no segmento\n");
        results.append("• Capital médio: R$ ").append(String.format("%.2f", capitalMedio)).append("\n\n");

        if (fatoresCriticos != null && fatoresCriticos.length > 0) {
            results.append("⚠️ FATORES CRÍTICOS:\n");
            for (String fator : fatoresCriticos) {
                results.append("• ").append(fator).append("\n");
            }
            results.append("\n");
        }

        if (estrategias != null && estrategias.length > 0) {
            results.append("💡 ESTRATÉGIAS SUGERIDAS:\n");
            for (String estrategia : estrategias) {
                results.append("• ").append(estrategia).append("\n");
            }
            results.append("\n");
        }

        results.append("📝 RECOMENDAÇÃO:\n").append(recomendacao != null ? recomendacao : "Análise em andamento.");

        return results.toString();
    }

    private String simulateAdvancedAnalysis(String cnae, String municipio, double capital) {
        return "📊 PROBABILIDADE DE SUCESSO: 72.0%\n" +
                "🏷️ CLASSIFICAÇÃO: Potencial Moderado\n\n" +
                "📈 DADOS DO MERCADO:\n" +
                "• 245 empresas no segmento\n" +
                "• Capital médio: R$ 120.450,00\n\n" +
                "✅ FATORES POSITIVOS:\n" +
                "• Mercado em crescimento\n" +
                "• Demanda consistente\n" +
                "• Boa infraestrutura local\n\n" +
                "⚠️ FATORES DE ATENÇÃO:\n" +
                "• Concorrência estabelecida\n" +
                "• Necessidade de investimento inicial\n\n" +
                "💡 RECOMENDAÇÃO:\n" +
                "Mercado viável com potencial de crescimento. " +
                "Recomenda-se estudo de viabilidade financeira detalhado.";
    }

    private PredicaoResponse criarPredicaoLocal(DashboardData dashboardData, String cnae, String municipio, double capital) {
        System.out.println("DEBUG: Criando predição local baseada em dados do dashboard");

        PredicaoResponse response = new PredicaoResponse();
        response.setSuccess(true);
        response.setMessage("Predição baseada em análise local dos dados");

        PredicaoResponse.PredicaoData data = new PredicaoResponse.PredicaoData();

        double probabilidadeBase = dashboardData.getProbabilidadeSucesso();
        int totalEmpresas = dashboardData.getQuantidadeEmpresas();
        double capitalMedio = dashboardData.getCapitalSocialMedio();

        double probabilidadeAjustada = calcularProbabilidadeAjustada(probabilidadeBase, totalEmpresas, capitalMedio, capital);
        data.setProbabilidadeSucesso(probabilidadeAjustada);

        data.setClassificacao(determinarClassificacao(probabilidadeAjustada));
        data.setEmpresasSimilares(totalEmpresas);
        data.setCapitalMedio(capitalMedio);

        data.setFatoresPositivos(gerarFatoresPositivos(totalEmpresas, capitalMedio));
        data.setFatoresNegativos(gerarFatoresNegativos(totalEmpresas, capitalMedio));
        data.setRecomendacao(gerarRecomendacaoLocal(probabilidadeAjustada, totalEmpresas, capital));

        response.setData(data);
        return response;
    }

    private double calcularProbabilidadeAjustada(double probabilidadeBase, int totalEmpresas, double capitalMedio, double capitalUsuario) {
        double ajuste = 0.0;

        if (totalEmpresas < 50) {
            ajuste += 0.1;
        } else if (totalEmpresas > 200) {
            ajuste -= 0.05;
        }

        if (capitalUsuario > 0) {
            if (capitalUsuario > capitalMedio * 1.5) {
                ajuste += 0.15;
            } else if (capitalUsuario < capitalMedio * 0.5) {
                ajuste -= 0.1;
            }
        }

        return Math.max(0.1, Math.min(0.95, probabilidadeBase + ajuste));
    }

    private String determinarClassificacao(double probabilidade) {
        if (probabilidade >= 0.8) return "ALTA";
        if (probabilidade >= 0.6) return "MODERADA";
        if (probabilidade >= 0.4) return "BAIXA";
        return "MUITO BAIXA";
    }

    private List<String> gerarFatoresPositivos(int totalEmpresas, double capitalMedio) {
        List<String> fatores = new ArrayList<>();

        if (totalEmpresas > 100) {
            fatores.add("Mercado estabelecido e consolidado");
        } else {
            fatores.add("Oportunidade em mercado em crescimento");
        }

        if (capitalMedio > 50000) {
            fatores.add("Segmento com bom potencial de retorno");
        }

        fatores.add("Base de dados robusta para análise");
        return fatores;
    }

    private List<String> gerarFatoresNegativos(int totalEmpresas, double capitalMedio) {
        List<String> fatores = new ArrayList<>();

        if (totalEmpresas > 200) {
            fatores.add("Alta concorrência no segmento");
        }

        if (capitalMedio < 10000) {
            fatores.add("Baixa barreira de entrada pode aumentar concorrência");
        }

        fatores.add("Recomendada análise de localização específica");
        return fatores;
    }

    private String gerarRecomendacaoLocal(double probabilidade, int totalEmpresas, double capital) {
        if (probabilidade >= 0.7) {
            return "Mercado altamente promissor. Recomenda-se planejamento estratégico para diferenciação.";
        } else if (probabilidade >= 0.5) {
            return "Mercado viável. Estudo de nichos específicos pode aumentar as chances de sucesso.";
        } else {
            return "Recomenda-se análise mais detalhada e consideração de fatores locais adicionais.";
        }
    }

    private void salvarAnaliseNoHistorico(String cnae, String municipio, double capital) {
        System.out.println("DEBUG: DataAnalysisActivity - Salvando ANÁLISE AVANÇADA no histórico");

        String sessionId = sessionManager.getSessionId();

        System.out.println("DEBUG: DataAnalysisActivity - Dados para histórico:");
        System.out.println("DEBUG:   SessionId: " + sessionId);
        System.out.println("DEBUG:   CNAE: " + cnae);
        System.out.println("DEBUG:   Municipio: " + municipio);
        System.out.println("DEBUG:   Capital: " + capital);

        if (sessionId == null || sessionId.isEmpty()) {
            System.out.println("DEBUG: DataAnalysisActivity - ERRO: SessionId é nulo ou vazio!");
            return;
        }

        if (cnae == null || cnae.isEmpty() || municipio == null || municipio.isEmpty()) {
            System.out.println("DEBUG: DataAnalysisActivity - ERRO: CNAE ou Municipio são nulos ou vazios!");
            return;
        }

        try {
            HistoricoBusca historico = new HistoricoBusca();
            historico.setSessionId(sessionId);
            historico.setCnae(cnae);
            historico.setMunicipio(municipio);
            historico.setCapitalSocial(capital);
            historico.setDataBuscaAtual();

            System.out.println("DEBUG: DataAnalysisActivity - Análise avançada salva no histórico");

            RetrofitClient.getApiService().salvarHistorico(historico).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    System.out.println("DEBUG: DataAnalysisActivity - Resposta do salvar histórico:");
                    System.out.println("DEBUG:   Código HTTP: " + response.code());
                    System.out.println("DEBUG:   Sucesso: " + response.isSuccessful());

                    if (response.isSuccessful()) {
                        System.out.println("DEBUG: DataAnalysisActivity - ✅ ANÁLISE AVANÇADA salva no histórico com SUCESSO!");
                    } else {
                        System.out.println("DEBUG: DataAnalysisActivity - ❌ Erro ao salvar análise avançada no histórico. Código: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    System.out.println("DEBUG: DataAnalysisActivity - ❌ FALHA ao salvar análise avançada no histórico: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            System.out.println("DEBUG: DataAnalysisActivity - ❌ ERRO inesperado ao salvar análise avançada no histórico: " + e.getMessage());
            e.printStackTrace();
        }
    }
}