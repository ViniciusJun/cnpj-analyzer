package com.novasemp.cnpjmobile.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.DashboardData;
import com.novasemp.cnpjmobile.service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView textQuantidadeEmpresas;
    private TextView textCapitalMedio;
    private TextView textProbabilidadeSucesso;
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
                            updateUI(response.body());
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

    private void updateUI(DashboardData data) {
        textQuantidadeEmpresas.setText(String.valueOf(data.getQuantidadeEmpresas()));
        textCapitalMedio.setText(String.format("R$ %.2f", data.getCapitalSocialMedio()));
        textProbabilidadeSucesso.setText(String.format("%.0f%%", data.getProbabilidadeSucesso() * 100));

        // Formatando estratégias
        StringBuilder estrategiasText = new StringBuilder();
        for (String estrategia : data.getEstrategias()) {
            estrategiasText.append("• ").append(estrategia).append("\n");
        }
        textEstrategias.setText(estrategiasText.toString());
    }
}