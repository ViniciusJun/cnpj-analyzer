package com.novasemp.cnpjmobile.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.PredicaoResponse;
import com.novasemp.cnpjmobile.service.ApiService;
import com.novasemp.cnpjmobile.service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataAnalysisActivity extends AppCompatActivity {

    private static final String TAG = "DataAnalysisActivity";

    private TextView textScoreML, textProbabilidade, textClassificacao,
            textModeloUtilizado, textFatoresCriticos, textRecomendacao,
            textStatusML;
    private ProgressBar progressBarScore;

    private String cnae;
    private String municipio;
    private double capitalSocial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_analysis);

        // Obter dados da intent
        cnae = getIntent().getStringExtra("cnae");
        municipio = getIntent().getStringExtra("municipio");
        capitalSocial = getIntent().getDoubleExtra("capitalSocial", 0.0);

        Log.d(TAG, "Iniciando análise - CNAE: " + cnae + ", Município: " + municipio + ", Capital: " + capitalSocial);

        initViews();
        realizarAnaliseAvancada();
    }

    private void initViews() {
        textScoreML = findViewById(R.id.textScoreML);
        textProbabilidade = findViewById(R.id.textProbabilidade);
        textClassificacao = findViewById(R.id.textClassificacao);
        textModeloUtilizado = findViewById(R.id.textModeloUtilizado);
        textFatoresCriticos = findViewById(R.id.textFatoresCriticos);
        textRecomendacao = findViewById(R.id.textRecomendacao);
        textStatusML = findViewById(R.id.textStatusML);
        progressBarScore = findViewById(R.id.progressBarScore);
    }

    private void realizarAnaliseAvancada() {
        Log.d(TAG, "Iniciando análise avançada com ML...");

        ApiService apiService = RetrofitClient.getApiService();

        // ✅ CORREÇÃO: Usar o endpoint correto com parâmetros corretos
        Call<PredicaoResponse> call = apiService.getPredicaoML(
                cnae,
                municipio,
                capitalSocial
        );

        call.enqueue(new Callback<PredicaoResponse>() {
            @Override
            public void onResponse(Call<PredicaoResponse> call, Response<PredicaoResponse> response) {
                Log.d(TAG, "Resposta recebida - Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    PredicaoResponse predicao = response.body();
                    Log.d(TAG, "Predição recebida: " + predicao.toString());

                    // ✅ ATUALIZAR UI COM DADOS REAIS DO BACKEND
                    atualizarUIComDadosReais(predicao);

                } else {
                    Log.e(TAG, "Resposta não sucedida: " + response.code() + " - " + response.message());
                    usarFallbackLocal("Erro na resposta do servidor: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PredicaoResponse> call, Throwable t) {
                Log.e(TAG, "Falha na chamada API: " + t.getMessage());
                t.printStackTrace();
                usarFallbackLocal("Falha na conexão: " + t.getMessage());
            }
        });
    }

    private void atualizarUIComDadosReais(PredicaoResponse predicao) {
        try {
            // ✅ ATUALIZAR TODOS OS CAMPOS COM DADOS REAIS
            runOnUiThread(() -> {
                // Score ML (0-1000)
                int score = predicao.getScoreML();
                textScoreML.setText(String.format("Score ML: %d/1000", score));
                progressBarScore.setProgress(score / 10); // Converter para 0-100

                // Probabilidade (0-100%)
                double probabilidade = predicao.getProbabilidadeSucesso() * 100;
                textProbabilidade.setText(String.format("Probabilidade de Sucesso: %.1f%%", probabilidade));

                // Classificação
                String classificacao = predicao.getClassificacao();
                textClassificacao.setText(String.format("Classificação: %s", classificacao));

                // Cor baseada na classificação
                int cor = getColorForClassification(classificacao);
                textClassificacao.setTextColor(cor);

                // Modelo Utilizado
                textModeloUtilizado.setText(String.format("Modelo: %s", predicao.getModeloUtilizado()));

                // Status ML
                textStatusML.setText(predicao.isMlOnline() ? "ML Online ✅" : "ML Offline ⚠️");
                textStatusML.setTextColor(predicao.isMlOnline() ?
                        getColor(android.R.color.holo_green_dark) :
                        getColor(android.R.color.holo_orange_dark));

                // Fatores Críticos
                if (predicao.getFatoresCriticos() != null && predicao.getFatoresCriticos().length > 0) {
                    StringBuilder fatores = new StringBuilder();
                    for (String fator : predicao.getFatoresCriticos()) {
                        fatores.append("• ").append(fator).append("\n");
                    }
                    textFatoresCriticos.setText(fatores.toString());
                } else {
                    textFatoresCriticos.setText("• Nenhum fator crítico identificado");
                }

                // Recomendação
                textRecomendacao.setText(predicao.getRecomendacao());

                // Log para debug
                Log.d(TAG, "UI atualizada com dados reais:");
                Log.d(TAG, " - Score: " + score);
                Log.d(TAG, " - Probabilidade: " + probabilidade);
                Log.d(TAG, " - Classificação: " + classificacao);
                Log.d(TAG, " - Modelo: " + predicao.getModeloUtilizado());
                Log.d(TAG, " - ML Online: " + predicao.isMlOnline());
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar UI: " + e.getMessage());
            runOnUiThread(() ->
                    Toast.makeText(this, "Erro ao processar dados do ML", Toast.LENGTH_LONG).show()
            );
        }
    }

    private int getColorForClassification(String classificacao) {
        if (classificacao == null) return getColor(android.R.color.black);

        switch (classificacao.toUpperCase()) {
            case "ALTA":
                return getColor(android.R.color.holo_green_dark);
            case "MEDIA":
                return getColor(android.R.color.holo_orange_dark);
            case "BAIXA":
                return getColor(android.R.color.holo_red_dark);
            default:
                return getColor(android.R.color.black);
        }
    }

    private void usarFallbackLocal(String motivo) {
        Log.w(TAG, "Usando fallback local: " + motivo);

        runOnUiThread(() -> {
            try {
                // ✅ FALLBACK MELHORADO
                textScoreML.setText("Score ML: 500/1000");
                progressBarScore.setProgress(50);

                textProbabilidade.setText("Probabilidade de Sucesso: 50.0%");
                textClassificacao.setText("Classificação: MÉDIA");
                textClassificacao.setTextColor(getColor(android.R.color.holo_orange_dark));

                textModeloUtilizado.setText("Modelo: Análise Local");
                textStatusML.setText("ML Offline ⚠️");
                textStatusML.setTextColor(getColor(android.R.color.holo_orange_dark));

                textFatoresCriticos.setText("• Serviço ML temporariamente indisponível\n• Usando análise básica\n• Tente novamente em instantes");
                textRecomendacao.setText("Realize uma pesquisa de mercado tradicional para validar os dados");

                Toast.makeText(this, "Análise local - " + motivo, Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Log.e(TAG, "Erro no fallback: " + e.getMessage());
                Toast.makeText(this, "Erro na análise", Toast.LENGTH_LONG).show();
            }
        });
    }
}