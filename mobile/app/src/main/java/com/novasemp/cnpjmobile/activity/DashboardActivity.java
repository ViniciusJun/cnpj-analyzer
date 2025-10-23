// mobile/app/src/main/java/com/novasemp/cnpjmobile/activity/DashboardActivity.java
package com.novasemp.cnpjmobile.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.model.HistoricoBusca;
import com.novasemp.cnpjmobile.service.RetrofitClient;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Obter parâmetros da MainActivity
        String cnae = getIntent().getStringExtra("CNAE");
        String municipio = getIntent().getStringExtra("MUNICIPIO");

        initViews();
        loadDashboardData(cnae, municipio);
    }

    private void initViews() {
        textQuantidadeEmpresas = findViewById(R.id.textQuantidadeEmpresas);
        textCapitalMedio = findViewById(R.id.textCapitalMedio);
        textProbabilidadeSucesso = findViewById(R.id.textProbabilidadeSucesso);
        textClassificacao = findViewById(R.id.textClassificacao);
        textFatoresCriticos = findViewById(R.id.textFatoresCriticos);
        textEstrategias = findViewById(R.id.textEstrategias);
    }

    private void loadDashboardData(String cnae, String municipio) {
        // Mostrar loading
        Toast.makeText(this, "Carregando dados...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getApiService().getDashboardData(cnae, municipio)
                .enqueue(new Callback<DashboardData>() {
                    @Override
                    public void onResponse(Call<DashboardData> call, Response<DashboardData> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            DashboardData data = response.body();
                            updateUI(data);

                            // Salvar no histórico
                            salvarNoHistorico(data);
                        } else {
                            Toast.makeText(DashboardActivity.this,
                                    "Erro ao carregar dados", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<DashboardData> call, Throwable t) {
                        Toast.makeText(DashboardActivity.this,
                                "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void salvarNoHistorico(DashboardData data) {
        System.out.println("DEBUG: DashboardActivity - Iniciando salvarNoHistorico");

        String sessionId = getIntent().getStringExtra("SESSION_ID");
        String cnae = getIntent().getStringExtra("CNAE");
        String municipio = getIntent().getStringExtra("MUNICIPIO");
        Double capital = getIntent().hasExtra("CAPITAL") ?
                getIntent().getDoubleExtra("CAPITAL", 0) : null;

        System.out.println("DEBUG: DashboardActivity - Dados para histórico:");
        System.out.println("DEBUG:   SessionId: " + sessionId);
        System.out.println("DEBUG:   CNAE: " + cnae);
        System.out.println("DEBUG:   Municipio: " + municipio);
        System.out.println("DEBUG:   Capital: " + capital);
        System.out.println("DEBUG:   DashboardData: " + (data != null ? "Não nulo" : "Nulo"));

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
            historico.setCapitalSocial(capital);
            historico.setDataBuscaAtual();

            System.out.println("DEBUG: DashboardActivity - HistoricoBusca criado:");
            System.out.println("DEBUG:   SessionId: " + historico.getSessionId());
            System.out.println("DEBUG:   CNAE: " + historico.getCnae());
            System.out.println("DEBUG:   Municipio: " + historico.getMunicipio());
            System.out.println("DEBUG:   Capital: " + historico.getCapitalSocial());
            System.out.println("DEBUG:   DataBusca: " + historico.getDataBusca());

            System.out.println("DEBUG: DashboardActivity - Chamando API para salvar histórico...");

            RetrofitClient.getApiService().salvarHistorico(historico).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    System.out.println("DEBUG: DashboardActivity - Resposta do salvar histórico:");
                    System.out.println("DEBUG:   Código HTTP: " + response.code());
                    System.out.println("DEBUG:   Sucesso: " + response.isSuccessful());
                    System.out.println("DEBUG:   Mensagem: " + response.message());

                    if (response.isSuccessful()) {
                        System.out.println("DEBUG: DashboardActivity - ✅ Histórico salvo com SUCESSO!");
                    } else {
                        System.out.println("DEBUG: DashboardActivity - ❌ Erro ao salvar histórico. Código: " + response.code());
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                System.out.println("DEBUG: DashboardActivity - Corpo do erro: " + errorBody);
                            }
                        } catch (Exception e) {
                            System.out.println("DEBUG: DashboardActivity - Erro ao ler corpo de erro: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    System.out.println("DEBUG: DashboardActivity - ❌ FALHA completa ao salvar histórico: " + t.getMessage());
                    t.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.out.println("DEBUG: DashboardActivity - ❌ ERRO inesperado ao salvar histórico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateUI(DashboardData data) {
        // Dados básicos
        textQuantidadeEmpresas.setText(String.valueOf(data.getQuantidadeEmpresas()));
        textCapitalMedio.setText(String.format("R$ %.2f", data.getCapitalSocialMedio()));
        textProbabilidadeSucesso.setText(String.format("%.0f%%", data.getProbabilidadeSucesso() * 100));

        // Classificação com cor baseada no nível
        String classificacao = data.getClassificacaoSucesso();
        textClassificacao.setText(classificacao);

        // Definir cor baseada na classificação
        int corClassificacao;
        if (classificacao != null) {
            switch (classificacao.toUpperCase()) {
                case "ALTA":
                    corClassificacao = Color.parseColor("#4CAF50"); // Verde
                    break;
                case "MEDIA":
                    corClassificacao = Color.parseColor("#FF9800"); // Laranja
                    break;
                case "BAIXA":
                    corClassificacao = Color.parseColor("#F44336"); // Vermelho
                    break;
                default:
                    corClassificacao = Color.parseColor("#757575"); // Cinza
            }
        } else {
            corClassificacao = Color.parseColor("#757575"); // Cinza
        }
        textClassificacao.setTextColor(corClassificacao);

        // Fatores críticos
        if (data.getFatoresCriticos() != null && data.getFatoresCriticos().length > 0) {
            StringBuilder fatoresText = new StringBuilder();
            for (String fator : data.getFatoresCriticos()) {
                fatoresText.append("• ").append(fator).append("\n");
            }
            textFatoresCriticos.setText(fatoresText.toString());
        } else {
            textFatoresCriticos.setText("Nenhum fator crítico identificado");
        }

        // Estratégias (incluindo a recomendação do ML)
        if (data.getEstrategias() != null && data.getEstrategias().length > 0) {
            StringBuilder estrategiasText = new StringBuilder();
            for (String estrategia : data.getEstrategias()) {
                estrategiasText.append("• ").append(estrategia).append("\n");
            }
            textEstrategias.setText(estrategiasText.toString());
        } else {
            textEstrategias.setText("Carregando estratégias...");
        }

        // Feedback de sucesso
        Toast.makeText(this, "Análise concluída!", Toast.LENGTH_SHORT).show();
    }
}