package com.novasemp.cnpjmobile.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.HistoricoBusca;
import com.novasemp.cnpjmobile.service.RetrofitClient;
import com.novasemp.cnpjmobile.util.HistoricoLocalManager;
import com.novasemp.cnpjmobile.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoricoActivity extends AppCompatActivity {

    private LinearLayout layoutHistorico;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button btnVoltar;
    private SessionManager sessionManager;
    private HistoricoLocalManager historicoLocalManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);
        historicoLocalManager = new HistoricoLocalManager(this);

        sessionManager = new SessionManager(this);
        initViews();
        carregarHistorico();
    }

    private void initViews() {
        layoutHistorico = findViewById(R.id.layoutHistorico);
        btnVoltar = findViewById(R.id.btnVoltar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        btnVoltar.setOnClickListener(v -> finish());

        // BOTÃO TEMPORÁRIO:
        Button btnLimparCache = new Button(this);
        btnLimparCache.setText("Limpar Cache (Teste)");
        btnLimparCache.setOnClickListener(v -> {
            historicoLocalManager.limparTodosTiposAnalise();
            carregarHistorico();
        });
        layoutHistorico.addView(btnLimparCache); // temporario

        // CONFIGURE O LISTENER:
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Recarregar o histórico
            carregarHistorico();
        });
    }

    private void carregarHistorico() {
        String sessionId = sessionManager.getSessionId();

        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, "Nenhum histórico encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // DEBUG: Mostrar todos os tipos armazenados
        historicoLocalManager.debugTodosTiposAnalise();

        RetrofitClient.getApiService().listarHistorico(sessionId)
                .enqueue(new Callback<List<HistoricoBusca>>() {
                    @Override
                    public void onResponse(Call<List<HistoricoBusca>> call, Response<List<HistoricoBusca>> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            System.out.println("DEBUG: HistoricoActivity - Recebidos " + response.body().size() + " itens do histórico");
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

        // CONFIGURE O LISTENER:
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Recarregar o histórico
            carregarHistorico();
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

        // AGORA CHAMA adicionarCardHistorico PARA CADA ITEM
        for (HistoricoBusca historico : historicos) {
            adicionarCardHistorico(historico);
        }

        System.out.println("DEBUG: HistoricoActivity - exibirHistorico concluído. " + historicos.size() + " itens processados.");
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

    private void adicionarCardHistorico(HistoricoBusca historico) {
        System.out.println("DEBUG: HistoricoActivity - adicionarCardHistorico CHAMADO!");

        // TEXTO DE TESTE TEMPORÁRIO - SEMPRE MOSTRAR
        TextView textDebug = new TextView(this);
        textDebug.setText("🔍 MÉTODO adicionarCardHistorico ESTÁ SENDO CHAMADO!");
        textDebug.setTextSize(10);
        textDebug.setTextColor(Color.RED);
        textDebug.setTypeface(null, Typeface.BOLD);
        // Vamos adicionar este texto ao card principal depois

        System.out.println("DEBUG: HistoricoActivity - Exibindo histórico:");
        System.out.println("DEBUG:   CNAE: " + historico.getCnae());
        System.out.println("DEBUG:   Municipio: " + historico.getMunicipio());
        System.out.println("DEBUG:   CapitalSocial: " + historico.getCapitalSocial());
        System.out.println("DEBUG:   DataBusca: " + historico.getDataBusca());
        System.out.println("DEBUG:   TipoAnalise do backend: " + historico.getTipoAnalise());
        System.out.println("DEBUG:   TipoAnalise é null? " + (historico.getTipoAnalise() == null));
        System.out.println("DEBUG:   TipoAnalise está vazio? " + (historico.getTipoAnalise() != null && historico.getTipoAnalise().isEmpty()));

        // Criar CardView
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setCardElevation(4);
        card.setRadius(8);
        card.setContentPadding(16, 16, 16, 16);


        // Layout interno do card - ESTA É A VARIÁVEL cardLayout
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        card.addView(cardLayout);

        // Localização (usando apenas os dados disponíveis)
        TextView textLocalizacao = new TextView(this);
        textLocalizacao.setText("CNAE: " + historico.getCnae() + " - Município: " + historico.getMunicipio());
        textLocalizacao.setTextSize(16);
        textLocalizacao.setTextColor(Color.BLACK);
        cardLayout.addView(textLocalizacao);

        // Data
        TextView textData = new TextView(this);
        textData.setText("Data: " + historico.getDataBusca());
        textData.setTextSize(12);
        textData.setTextColor(Color.GRAY);
        cardLayout.addView(textData);

        // Capital (se disponível e maior que 0)
        if (historico.getCapitalSocial() != null && historico.getCapitalSocial() > 0) {
            TextView textCapital = new TextView(this);
            textCapital.setText("Capital: R$ " + String.format("%.2f", historico.getCapitalSocial()));
            textCapital.setTextSize(14);
            textCapital.setTextColor(Color.BLACK);
            cardLayout.addView(textCapital);
        }

        // TIPO DE ANÁLISE - BUSCAR DO ARMAZENAMENTO LOCAL
        String chave = HistoricoLocalManager.gerarChave(
                historico.getSessionId(),
                historico.getCnae(),
                historico.getMunicipio(),
                historico.getDataBusca()
        );

        String tipoAnaliseLocal = historicoLocalManager.getTipoAnalise(chave);
        System.out.println("DEBUG: HistoricoActivity - TipoAnalise local: " + tipoAnaliseLocal);

        // Se não encontrou localmente, tentar usar o do backend como fallback
        String tipoAnaliseParaExibir = tipoAnaliseLocal != null ? tipoAnaliseLocal :
                (historico.getTipoAnalise() != null ? historico.getTipoAnalise() : "BUSCA_BASICA");

        TextView textTipoAnalise = new TextView(this);

        // Formatar o tipo de análise para melhor legibilidade
        String tipoFormatado = tipoAnaliseParaExibir
                .replace("_", " ")
                .toLowerCase();
        tipoFormatado = tipoFormatado.substring(0, 1).toUpperCase() + tipoFormatado.substring(1);

        textTipoAnalise.setText("Tipo: " + tipoFormatado);
        textTipoAnalise.setTextSize(12);
        textTipoAnalise.setTextColor(Color.parseColor("#2196F3")); // Azul primário
        textTipoAnalise.setTypeface(null, Typeface.BOLD);
        cardLayout.addView(textTipoAnalise);

        // Botões
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button btnAbrir = new Button(this);
        btnAbrir.setText("Abrir");
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        btnParams.setMargins(0, 0, 8, 0);
        btnAbrir.setLayoutParams(btnParams);
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
        cardLayout.addView(buttonLayout);

        // Adicionar o card completo ao layout principal
        layoutHistorico.addView(card);
    }

    private void excluirHistorico(Integer id) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar exclusão")
                .setMessage("Deseja realmente excluir este item do histórico?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    executarExclusao(id);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void executarExclusao(Integer id) {
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