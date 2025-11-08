package com.novasemp.cnpjmobile.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.service.ApiService;
import com.novasemp.cnpjmobile.service.RetrofitClient;
import com.novasemp.cnpjmobile.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private TextView textQuantidadeEmpresas, textCapitalMedio, textProbabilidadeSucesso,
            textClassificacao, textFatoresCriticos, textRecomendacao, textEstrategias;

    private SessionManager sessionManager;
    private String cnae;
    private String municipio;
    private double capitalSocial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Obter dados da intent
        cnae = getIntent().getStringExtra("cnae");
        municipio = getIntent().getStringExtra("municipio");
        capitalSocial = getIntent().getDoubleExtra("capitalSocial", 0.0);

        sessionManager = new SessionManager(this);

        Log.d(TAG, "Dashboard - CNAE: " + cnae + ", Município: " + municipio + ", Capital: " + capitalSocial);

        initViews();
        carregarDadosDashboard();
    }

    private void initViews() {
        textQuantidadeEmpresas = findViewById(R.id.textQuantidadeEmpresas);
        textCapitalMedio = findViewById(R.id.textCapitalMedio);
        textProbabilidadeSucesso = findViewById(R.id.textProbabilidadeSucesso);
        textClassificacao = findViewById(R.id.textClassificacao);
        textFatoresCriticos = findViewById(R.id.textFatoresCriticos);
        textRecomendacao = findViewById(R.id.textRecomendacao);
        textEstrategias = findViewById(R.id.textEstrategias);
    }

    private void carregarDadosDashboard() {
        Log.d(TAG, "Carregando dados do dashboard...");

        ApiService apiService = RetrofitClient.getApiService();

        Call<DashboardData> call = apiService.getDashboardData(
                cnae,
                municipio,
                capitalSocial,
                sessionManager.getSessionId()
        );

        call.enqueue(new Callback<DashboardData>() {
            @Override
            public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                Log.d(TAG, "Resposta dashboard - Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    DashboardData dashboardData = response.body();
                    Log.d(TAG, "Dashboard data recebido: " + dashboardData.toString());

                    atualizarDashboardComDadosReais(dashboardData);

                } else {
                    Log.e(TAG, "Resposta não sucedida: " + response.code());
                    usarDashboardFallback("Erro no servidor: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DashboardData> call, Throwable t) {
                Log.e(TAG, "Falha no dashboard: " + t.getMessage());
                usarDashboardFallback("Falha na conexão: " + t.getMessage());
            }
        });
    }

    private void atualizarDashboardComDadosReais(DashboardData data) {
        try {
            runOnUiThread(() -> {
                // Quantidade de empresas
                textQuantidadeEmpresas.setText(
                        String.format("Empresas na região: %d", data.getQuantidadeEmpresas())
                );

                // Capital social médio
                textCapitalMedio.setText(
                        String.format("Capital social médio: R$ %.2f", data.getCapitalSocialMedio())
                );

                // Probabilidade de sucesso
                double probabilidadePercent = data.getProbabilidadeSucesso() * 100;
                textProbabilidadeSucesso.setText(
                        String.format("Probabilidade de sucesso: %.1f%%", probabilidadePercent)
                );

                // Classificação
                String classificacao = data.getClassificacaoSucesso();
                textClassificacao.setText(
                        String.format("Classificação: %s", classificacao)
                );

                // Cor baseada na classificação
                int cor = getColorForClassification(classificacao);
                textClassificacao.setTextColor(cor);

                // Fatores críticos
                if (data.getFatoresCriticos() != null && data.getFatoresCriticos().length > 0) {
                    StringBuilder fatores = new StringBuilder();
                    for (String fator : data.getFatoresCriticos()) {
                        fatores.append("• ").append(fator).append("\n");
                    }
                    textFatoresCriticos.setText(fatores.toString());
                } else {
                    textFatoresCriticos.setText("• Análise em andamento");
                }

                // Recomendação
                textRecomendacao.setText(data.getRecomendacao());

                // Estratégias
                if (data.getEstrategias() != null && data.getEstrategias().length > 0) {
                    StringBuilder estrategias = new StringBuilder();
                    for (String estrategia : data.getEstrategias()) {
                        estrategias.append("• ").append(estrategia).append("\n");
                    }
                    textEstrategias.setText(estrategias.toString());
                } else {
                    textEstrategias.setText("• Analisar concorrência local");
                }

                Log.d(TAG, "Dashboard atualizado com dados reais do backend");
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar dashboard: " + e.getMessage());
            runOnUiThread(() ->
                    Toast.makeText(this, "Erro ao processar dados", Toast.LENGTH_LONG).show()
            );
        }
    }

    private int getColorForClassification(String classificacao) {
        if (classificacao == null) return getResources().getColor(android.R.color.black);

        switch (classificacao.toUpperCase()) {
            case "ALTA":
                return getResources().getColor(android.R.color.holo_green_dark);
            case "MEDIA":
                return getResources().getColor(android.R.color.holo_orange_dark);
            case "BAIXA":
                return getResources().getColor(android.R.color.holo_red_dark);
            default:
                return getResources().getColor(android.R.color.black);
        }
    }

    private void usarDashboardFallback(String motivo) {
        Log.w(TAG, "Usando dashboard fallback: " + motivo);

        runOnUiThread(() -> {
            try {
                textQuantidadeEmpresas.setText("Empresas na região: Calculando...");
                textCapitalMedio.setText("Capital social médio: Calculando...");
                textProbabilidadeSucesso.setText("Probabilidade de sucesso: 65.0%");
                textClassificacao.setText("Classificação: MÉDIA");
                textClassificacao.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                textFatoresCriticos.setText("• Dados temporariamente indisponíveis");
                textRecomendacao.setText("Aguarde e tente novamente");
                textEstrategias.setText("• Analisar concorrência local\n• Estudar perfil demográfico");

                Toast.makeText(this, "Dados locais - " + motivo, Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Log.e(TAG, "Erro no fallback do dashboard: " + e.getMessage());
            }
        });
    }
}