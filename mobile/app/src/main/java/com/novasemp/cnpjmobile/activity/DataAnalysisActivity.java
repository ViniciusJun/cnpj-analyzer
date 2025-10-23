package com.novasemp.cnpjmobile.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.model.PredicaoResponse;
import com.novasemp.cnpjmobile.service.ApiService;
import com.novasemp.cnpjmobile.service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataAnalysisActivity extends AppCompatActivity {

    private EditText editTextCnae;
    private EditText editTextMunicipio;
    private EditText editTextCapital;
    private Button buttonAnalisar;
    private TextView textViewResultados;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("DEBUG: DataAnalysisActivity - onCreate iniciado");

        try {
            System.out.println("DEBUG: Tentando carregar layout...");
            setContentView(R.layout.activity_data_analysis);
            System.out.println("DEBUG: Layout carregado com sucesso");

            // Inicializar componentes UI PRIMEIRO (sem dependências externas)
            initializeUIComponents();
            System.out.println("DEBUG: Componentes UI inicializados");

            // Configurar listeners
            setupButtonListeners();
            System.out.println("DEBUG: Listeners configurados");

            // Tentar inicializar ApiService APÓS a UI estar pronta
            System.out.println("DEBUG: Tentando inicializar ApiService...");
            try {
                apiService = RetrofitClient.getApiService();
                System.out.println("DEBUG: ApiService inicializado: " + (apiService != null));
            } catch (Exception e) {
                System.out.println("DEBUG: AVISO - ApiService não pôde ser inicializado: " + e.getMessage());
                apiService = null;
                // Não crasha a app, apenas mostra aviso
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

    private void initializeUIComponents() {
        editTextCnae = findViewById(R.id.editTextCnae);
        editTextMunicipio = findViewById(R.id.editTextMunicipio);
        editTextCapital = findViewById(R.id.editTextCapital);
        buttonAnalisar = findViewById(R.id.buttonAnalisar);
        textViewResultados = findViewById(R.id.textViewResultados);
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

        // Criar cópias finais para usar no callback
        final String finalCnae = cnae;
        final String finalMunicipio = municipio;
        final double finalCapital = capital;

        textViewResultados.setText("Executando análise preditiva...\n\nAguarde...");
        buttonAnalisar.setEnabled(false);

        // Se ApiService não está disponível, usar apenas simulação
        if (apiService == null) {
            System.out.println("DEBUG: ApiService não disponível - usando modo simulador");
            double finalCapital1 = capital;
            new android.os.Handler().postDelayed(() -> {
                String simulationResult = simulateAdvancedAnalysis(cnae, municipio, finalCapital1);
                textViewResultados.setText("Modo Offline - Dados Simulados:\n\n" + simulationResult);
                buttonAnalisar.setEnabled(true);
            }, 2000); // Simular delay de 2 segundos
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
                System.out.println("DEBUG: Resposta da predição - Body: " + response.body());
                System.out.println("DEBUG: Resposta da predição - É bem-sucedida: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    PredicaoResponse predicaoResponse = response.body();
                    System.out.println("DEBUG: PredicaoResponse - Success: " + predicaoResponse.isSuccess());
                    System.out.println("DEBUG: PredicaoResponse - Message: " + predicaoResponse.getMessage());

                    // LOG ADICIONAL: Verificar se o objeto data existe
                    if (predicaoResponse.getData() != null) {
                        System.out.println("DEBUG: PredicaoResponse - Data: " + predicaoResponse.getData());
                    } else {
                        System.out.println("DEBUG: PredicaoResponse - Data é NULL");
                    }

                    if (predicaoResponse.isSuccess() && predicaoResponse.getData() != null) {
                        System.out.println("DEBUG: Predição ML bem-sucedida!");
                        displayPredictionResults(predicaoResponse);
                    } else {
                        System.out.println("DEBUG: Predição ML falhou, tentando dashboard...");
                        // Se ML falhar, tentar dashboard
                        textViewResultados.setText("Otimizando análise...\n\nUsando dados consolidados...");
                        tryDashboardFallback(cnae, municipio, capital);
                    }
                } else {
                    System.out.println("DEBUG: Resposta não sucedida ou body nulo - tentando dashboard");
                    tryDashboardFallback(cnae, municipio, capital);
                }
            }

            @Override
            public void onFailure(Call<PredicaoResponse> call, Throwable t) {
                System.out.println("DEBUG: Falha na predição ML: " + t.getMessage());
                tryDashboardFallback(cnae, municipio, capital);
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

        results.append("📊 PROBABILIDADE DE SUCESSO: ")
                .append(String.format("%.1f", data.getProbabilidadeSucesso() * 100))
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

        textViewResultados.setText(results.toString());
        buttonAnalisar.setEnabled(true);
    }

    private void fetchDashboardDataAsFallback(String cnae, String municipio, double capital) {
        // Fallback: buscar dados do dashboard
        Call<DashboardData> dashboardCall = apiService.getDashboardData(cnae, municipio);

        dashboardCall.enqueue(new Callback<DashboardData>() {
            @Override
            public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardData dashboardData = response.body();
                    String simulationResult = createEnhancedSimulation(cnae, municipio, capital, dashboardData);
                    textViewResultados.setText("📊 ANÁLISE AVANÇADA (Dados Consolidados)\n\n" + simulationResult);
                } else {
                    String simulationResult = simulateAdvancedAnalysis(cnae, municipio, capital);
                    textViewResultados.setText("📊 ANÁLISE AVANÇADA (Dados Simulados)\n\n" + simulationResult);
                }
                buttonAnalisar.setEnabled(true);
            }

            @Override
            public void onFailure(Call<DashboardData> call, Throwable t) {
                String simulationResult = simulateAdvancedAnalysis(cnae, municipio, capital);
                textViewResultados.setText("📊 ANÁLISE AVANÇADA (Dados Simulados)\n\n" + simulationResult);
                buttonAnalisar.setEnabled(true);
            }
        });
    }

    private String createEnhancedSimulation(String cnae, String municipio, double capital, DashboardData dashboardData) {
        // Usar dados reais do dashboard quando disponíveis
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
}