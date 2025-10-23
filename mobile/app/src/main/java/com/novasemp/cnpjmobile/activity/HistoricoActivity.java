package com.novasemp.cnpjmobile.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.HistoricoBusca;
import com.novasemp.cnpjmobile.service.RetrofitClient;
import com.novasemp.cnpjmobile.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoActivity extends AppCompatActivity {

    private LinearLayout layoutHistorico;
    private Button btnVoltar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        sessionManager = new SessionManager(this);
        initViews();
        carregarHistorico();
    }

    private void initViews() {
        layoutHistorico = findViewById(R.id.layoutHistorico);
        btnVoltar = findViewById(R.id.btnVoltar);

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarHistorico() {
        String sessionId = sessionManager.getSessionId();

        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, "Nenhum histórico encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().listarHistorico(sessionId)
                .enqueue(new Callback<List<HistoricoBusca>>() {
                    @Override
                    public void onResponse(Call<List<HistoricoBusca>> call, Response<List<HistoricoBusca>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            exibirHistorico(response.body());
                        } else {
                            Toast.makeText(HistoricoActivity.this,
                                    "Erro ao carregar histórico", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<HistoricoBusca>> call, Throwable t) {
                        Toast.makeText(HistoricoActivity.this,
                                "Falha na conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void exibirHistorico(List<HistoricoBusca> historicos) {
        layoutHistorico.removeAllViews();

        if (historicos.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Nenhuma busca realizada");
            emptyText.setTextSize(16);
            emptyText.setTextColor(Color.GRAY);
            emptyText.setPadding(0, 32, 0, 0);
            layoutHistorico.addView(emptyText);
            return;
        }

        for (HistoricoBusca historico : historicos) {
            // Criar CardView manualmente (usando LinearLayout com fundo e sombra)
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.card_background); // Você precisa criar este drawable
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardParams);
            card.setPadding(16, 16, 16, 16);

            // Localização (CNAE e Município - códigos)
            TextView textLocalizacao = new TextView(this);
            textLocalizacao.setText("CNAE: " + historico.getCnae() + " - Município: " + historico.getMunicipio());
            textLocalizacao.setTextSize(16);
            textLocalizacao.setTextColor(Color.BLACK);
            card.addView(textLocalizacao);

            // Data
            TextView textData = new TextView(this);
            textData.setText("Data: " + historico.getDataBusca());
            textData.setTextSize(12);
            textData.setTextColor(Color.GRAY);
            card.addView(textData);

            // Capital (se disponível)
            if (historico.getCapitalSocial() != null) {
                TextView textCapital = new TextView(this);
                textCapital.setText("Capital: R$ " + String.format("%.2f", historico.getCapitalSocial()));
                textCapital.setTextSize(14);
                textCapital.setTextColor(Color.BLACK);
                card.addView(textCapital);
            }

            // Botões
            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            Button btnAbrir = new Button(this);
            btnAbrir.setText("Abrir");
            btnAbrir.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            btnAbrir.setOnClickListener(v -> abrirDashboard(historico));

            Button btnExcluir = new Button(this);
            btnExcluir.setText("Excluir");
            btnExcluir.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            btnExcluir.setOnClickListener(v -> excluirHistorico(historico.getId()));

            buttonLayout.addView(btnAbrir);
            buttonLayout.addView(btnExcluir);
            card.addView(buttonLayout);

            layoutHistorico.addView(card);
        }
    }

    private void abrirDashboard(HistoricoBusca historico) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("CNAE", historico.getCnae());
        intent.putExtra("MUNICIPIO", historico.getMunicipio());
        if (historico.getCapitalSocial() != null) {
            intent.putExtra("CAPITAL", historico.getCapitalSocial());
        }
        startActivity(intent);
    }

    private void excluirHistorico(Integer id) {
        String sessionId = sessionManager.getSessionId();
        if (sessionId == null) {
            Toast.makeText(this, "Erro ao excluir", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().deletarHistorico(id, sessionId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(HistoricoActivity.this,
                                    "Item excluído", Toast.LENGTH_SHORT).show();
                            carregarHistorico(); // Recarregar a lista
                        } else {
                            Toast.makeText(HistoricoActivity.this,
                                    "Erro ao excluir", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(HistoricoActivity.this,
                                "Falha na conexão", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}