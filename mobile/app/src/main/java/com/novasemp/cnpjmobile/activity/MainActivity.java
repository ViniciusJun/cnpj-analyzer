package com.novasemp.cnpjmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.service.ApiService;
import com.novasemp.cnpjmobile.service.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText editCnae, editMunicipio, editCapitalSocial;
    private Button btnBuscar, btnAnaliseAvancada, btnHistorico, btnDashboard;
    private TextView textStatusBackend, textStatusML;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        testarConexaoBackend();
    }

    private void initViews() {
        editCnae = findViewById(R.id.editCnae);
        editMunicipio = findViewById(R.id.editMunicipio);
        editCapitalSocial = findViewById(R.id.editCapitalSocial);

        btnBuscar = findViewById(R.id.btnBuscar);
        btnAnaliseAvancada = findViewById(R.id.btnAnaliseAvancada);
        btnHistorico = findViewById(R.id.btnHistorico);
        btnDashboard = findViewById(R.id.btnDashboard);

        textStatusBackend = findViewById(R.id.textStatusBackend);
        textStatusML = findViewById(R.id.textStatusML);
    }

    private void setupClickListeners() {
        btnBuscar.setOnClickListener(v -> buscarDadosBasicos());
        btnAnaliseAvancada.setOnClickListener(v -> iniciarAnaliseAvancada());
        btnHistorico.setOnClickListener(v -> abrirHistorico());
        btnDashboard.setOnClickListener(v -> abrirDashboard());
    }

    private void buscarDadosBasicos() {
        String cnae = editCnae.getText().toString().trim();
        String municipio = editMunicipio.getText().toString().trim();
        String capitalStr = editCapitalSocial.getText().toString().trim();

        if (cnae.isEmpty() || municipio.isEmpty()) {
            Toast.makeText(this, "Preencha CNAE e Município", Toast.LENGTH_SHORT).show();
            return;
        }

        double capitalSocial = capitalStr.isEmpty() ? 0.0 : Double.parseDouble(capitalStr);

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("cnae", cnae);
        intent.putExtra("municipio", municipio);
        intent.putExtra("capitalSocial", capitalSocial);
        startActivity(intent);
    }

    private void iniciarAnaliseAvancada() {
        String cnae = editCnae.getText().toString().trim();
        String municipio = editMunicipio.getText().toString().trim();
        String capitalStr = editCapitalSocial.getText().toString().trim();

        if (cnae.isEmpty() || municipio.isEmpty()) {
            Toast.makeText(this, "Preencha CNAE e Município", Toast.LENGTH_SHORT).show();
            return;
        }

        double capitalSocial = capitalStr.isEmpty() ? 0.0 : Double.parseDouble(capitalStr);

        Intent intent = new Intent(this, DataAnalysisActivity.class);
        intent.putExtra("cnae", cnae);
        intent.putExtra("municipio", municipio);
        intent.putExtra("capitalSocial", capitalSocial);
        startActivity(intent);
    }

    private void abrirHistorico() {
        Intent intent = new Intent(this, HistoricoActivity.class);
        startActivity(intent);
    }

    private void abrirDashboard() {
        // Dashboard geral sem parâmetros específicos
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }

    private void testarConexaoBackend() {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.healthCheck().enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    textStatusBackend.setText("Conectado ✅");
                    textStatusBackend.setTextColor(getColor(android.R.color.holo_green_dark));
                    testarStatusML();
                } else {
                    textStatusBackend.setText("Erro ❌");
                    textStatusBackend.setTextColor(getColor(android.R.color.holo_red_dark));
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                textStatusBackend.setText("Offline ❌");
                textStatusBackend.setTextColor(getColor(android.R.color.holo_red_dark));
                textStatusML.setText("Indisponível");
                Log.e(TAG, "Falha na conexão: " + t.getMessage());
            }
        });
    }

    private void testarStatusML() {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.debugML().enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    textStatusML.setText("Online ✅");
                    textStatusML.setTextColor(getColor(android.R.color.holo_green_dark));
                } else {
                    textStatusML.setText("Erro ⚠️");
                    textStatusML.setTextColor(getColor(android.R.color.holo_orange_dark));
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                textStatusML.setText("Offline ❌");
                textStatusML.setTextColor(getColor(android.R.color.holo_red_dark));
            }
        });
    }
}