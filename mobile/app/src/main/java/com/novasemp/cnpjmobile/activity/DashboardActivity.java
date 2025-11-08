package com.novasemp.cnpjmobile.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.model.HistoricoBusca;
import com.novasemp.cnpjmobile.service.RetrofitClient;
import com.novasemp.cnpjmobile.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView textQuantidadeEmpresas;
    private TextView textCapitalMedio;
    private TextView textProbabilidadeSucesso;
    private TextView textClassificacao;
    private TextView textFatoresCriticos;
    private TextView textEstrategias;

    private SessionManager sessionManager;
    private boolean fromAnalysis = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        // Verificar se veio da análise avançada (para evitar duplicação)
        fromAnalysis = getIntent().getBooleanExtra("FROM_ANALYSIS", false);

        initViews();
        carregarDadosDashboard();

        // SALVAR APENAS SE NÃO VEIO DA ANÁLISE AVANÇADA
        if (!fromAnalysis) {
            salvarBuscaNoHistorico();
        } else {
            System.out.println("DEBUG: DashboardActivity - Veio da análise avançada, não salvando duplicado");
        }
    }

    private void initViews() {
        textQuantidadeEmpresas = findViewById(R.id.textQuantidadeEmpresas);
        textCapitalMedio = findViewById(R.id.textCapitalMedio);
        textProbabilidadeSucesso = findViewById(R.id.textProbabilidadeSucesso);
        textClassificacao = findViewById(R.id.textClassificacao);
        textFatoresCriticos = findViewById(R.id.textFatoresCriticos);
        textEstrategias = findViewById(R.id.textEstrategias);

        System.out.println("DEBUG: DashboardActivity - Views inicializados:");
        System.out.println("DEBUG:   fromAnalysis: " + fromAnalysis);
    }

    private void carregarDadosDashboard() {
        String cnae = getIntent().getStringExtra("CNAE");
        String municipio = getIntent().getStringExtra("MUNICIPIO");

        if (cnae == null || municipio == null) {
            Toast.makeText(this, "Dados insuficientes para carregar dashboard", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Exibir CNAE e Município no título
        String title = "Dashboard - CNAE: " + cnae + " | Município: " + municipio;
        if (fromAnalysis) {
            title += " (Análise Avançada)";
        }
        setTitle(title);

        RetrofitClient.getApiService().getDashboardData(cnae, municipio)
                .enqueue(new Callback<DashboardData>() {
                    @Override
                    public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            System.out.println("DEBUG: DashboardActivity - Dados recebidos com sucesso");
                            exibirDadosDashboard(response.body());
                        } else {
                            System.out.println("DEBUG: DashboardActivity - Erro na resposta: " + response.code());
                            Toast.makeText(DashboardActivity.this,
                                    "Erro ao carregar dados do dashboard", Toast.LENGTH_SHORT).show();
                            exibirDadosSimulados();
                        }
                    }

                    @Override
                    public void onFailure(Call<DashboardData> call, Throwable t) {
                        System.out.println("DEBUG: DashboardActivity - Falha na conexão: " + t.getMessage());
                        Toast.makeText(DashboardActivity.this,
                                "Falha na conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        exibirDadosSimulados();
                    }
                });
    }

    private void exibirDadosDashboard(DashboardData data) {
        System.out.println("DEBUG: DashboardActivity - Exibindo dados reais do dashboard");

        // Quantidade de Empresas
        if (textQuantidadeEmpresas != null) {
            textQuantidadeEmpresas.setText(String.valueOf(data.getQuantidadeEmpresas()));
        }

        // Capital Social Médio
        if (textCapitalMedio != null) {
            textCapitalMedio.setText(String.format("R$ %.2f", data.getCapitalSocialMedio()));
        }

        // Probabilidade de Sucesso
        if (textProbabilidadeSucesso != null) {
            textProbabilidadeSucesso.setText(String.format("%.1f%%", data.getProbabilidadeSucesso() * 100));
        }

        // Classificação
        if (textClassificacao != null) {
            textClassificacao.setText(data.getClassificacaoSucesso());
        }

        // Fatores Críticos
        if (textFatoresCriticos != null) {
            if (data.getFatoresCriticos() != null && data.getFatoresCriticos().length > 0) {
                StringBuilder fatores = new StringBuilder();
                for (String fator : data.getFatoresCriticos()) {
                    fatores.append("• ").append(fator).append("\n");
                }
                textFatoresCriticos.setText(fatores.toString());
            } else {
                textFatoresCriticos.setText("Nenhum fator crítico identificado");
            }
        }

        // Estratégias
        if (textEstrategias != null) {
            if (data.getEstrategias() != null && data.getEstrategias().length > 0) {
                StringBuilder estrategias = new StringBuilder();
                for (String estrategia : data.getEstrategias()) {
                    estrategias.append("• ").append(estrategia).append("\n");
                }
                textEstrategias.setText(estrategias.toString());
            } else {
                textEstrategias.setText("Estratégias sendo analisadas...");
            }
        }
    }

    private void exibirDadosSimulados() {
        System.out.println("DEBUG: DashboardActivity - Exibindo dados simulados");

        if (textQuantidadeEmpresas != null) {
            textQuantidadeEmpresas.setText("245");
        }

        if (textCapitalMedio != null) {
            textCapitalMedio.setText("R$ 120.450,00");
        }

        if (textProbabilidadeSucesso != null) {
            textProbabilidadeSucesso.setText("72.0%");
        }

        if (textClassificacao != null) {
            textClassificacao.setText("Potencial Moderado");
        }

        if (textFatoresCriticos != null) {
            textFatoresCriticos.setText("• Concorrência estabelecida\n• Necessidade de investimento inicial");
        }

        if (textEstrategias != null) {
            textEstrategias.setText("• Focar em nicho específico\n• Diferenciação por qualidade\n• Parcerias locais");
        }
    }

    private void salvarBuscaNoHistorico() {
        System.out.println("DEBUG: DashboardActivity - Salvando BUSCA BÁSICA no histórico");

        String sessionId = sessionManager.getSessionId();
        String cnae = getIntent().getStringExtra("CNAE");
        String municipio = getIntent().getStringExtra("MUNICIPIO");
        Double capital = getIntent().hasExtra("CAPITAL") ? getIntent().getDoubleExtra("CAPITAL", 0.0) : null;

        System.out.println("DEBUG: DashboardActivity - Dados para histórico:");
        System.out.println("DEBUG:   SessionId: " + sessionId);
        System.out.println("DEBUG:   CNAE: " + cnae);
        System.out.println("DEBUG:   Municipio: " + municipio);
        System.out.println("DEBUG:   Capital: " + capital);

        if (sessionId == null || sessionId.isEmpty()) {
            System.out.println("DEBUG: DashboardActivity - ERRO: SessionId é nulo ou vazio!");
            return;
        }

        if (cnae == null || cnae.isEmpty() || municipio == null || municipio.isEmpty()) {
            System.out.println("DEBUG: DashboardActivity - ERRO: CNAE ou Municipio são nulos ou vazios!");
            return;
        }

        try {
            HistoricoBusca historico = new HistoricoBusca();
            historico.setSessionId(sessionId);
            historico.setCnae(cnae);
            historico.setMunicipio(municipio);

            if (capital != null && capital > 0) {
                historico.setCapitalSocial(capital);
            } else {
                historico.setCapitalSocial(0.0);
            }

            historico.setDataBuscaAtual();

            RetrofitClient.getApiService().salvarHistorico(historico).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    System.out.println("DEBUG: DashboardActivity - Resposta do salvar histórico:");
                    System.out.println("DEBUG:   Código HTTP: " + response.code());
                    System.out.println("DEBUG:   Sucesso: " + response.isSuccessful());

                    if (response.isSuccessful()) {
                        System.out.println("DEBUG: DashboardActivity - ✅ BUSCA BÁSICA salva no histórico com SUCESSO!");
                    } else {
                        System.out.println("DEBUG: DashboardActivity - ❌ Erro ao salvar busca básica no histórico. Código: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    System.out.println("DEBUG: DashboardActivity - ❌ FALHA ao salvar busca básica no histórico: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            System.out.println("DEBUG: DashboardActivity - ❌ ERRO inesperado ao salvar busca básica no histórico: " + e.getMessage());
            e.printStackTrace();
        }
    }
}