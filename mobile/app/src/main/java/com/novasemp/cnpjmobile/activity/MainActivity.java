package com.novasemp.cnpjmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.model.HistoricoBusca;
import com.novasemp.cnpjmobile.service.RetrofitClient;
import com.novasemp.cnpjmobile.util.SessionManager;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCnae;
    private EditText editTextMunicipio;
    private EditText editTextCapital;
    private Button buttonBuscar;
    private Button buttonHistorico;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar gerenciador de sessão
        sessionManager = new SessionManager(this);
        String sessionId = sessionManager.getSessionId();
        System.out.println("DEBUG: MainActivity - SessionId gerado: " + sessionId);
        System.out.println("DEBUG: MainActivity - SessionManager é null? " + (sessionManager == null));

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        editTextCnae = findViewById(R.id.editTextCnae);
        editTextMunicipio = findViewById(R.id.editTextMunicipio);
        editTextCapital = findViewById(R.id.editTextCapital);
        buttonBuscar = findViewById(R.id.buttonBuscar);
        buttonHistorico = findViewById(R.id.btnHistorico);
        
        // Botão para análise avançada
        Button buttonAnaliseAvancada = findViewById(R.id.buttonAnaliseAvancada);
    }

    private void setupClickListeners() {
        
        // Botão para ver histórico
        buttonHistorico.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoricoActivity.class);
            startActivity(intent);
        });

        // Botão para análise avançada
        Button buttonAnaliseAvancada = findViewById(R.id.buttonAnaliseAvancada);
        buttonAnaliseAvancada.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DataAnalysisActivity.class);
            startActivity(intent);
        });

        // Botão para buscar dados
        buttonBuscar.setOnClickListener(v -> {
            String cnae = editTextCnae.getText().toString().trim();
            String municipio = editTextMunicipio.getText().toString().trim();
            String capitalStr = editTextCapital.getText().toString().trim();

            // Validações básicas
            if (cnae.isEmpty() || municipio.isEmpty()) {
                Toast.makeText(this, "Preencha pelo menos CNAE e Município", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar se CNAE e Município são numéricos (códigos)
            if (!cnae.matches("\\d+") || !municipio.matches("\\d+")) {
                Toast.makeText(this, "CNAE e Município devem ser códigos numéricos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar capital social se fornecido
            Double capital = null;
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

            // Obter session ID
            String sessionId = sessionManager.getSessionId();

            // Navegar para a tela de dashboard
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putExtra("CNAE", cnae);
            intent.putExtra("MUNICIPIO", municipio);
            intent.putExtra("SESSION_ID", sessionId);
            
            if (capital != null) {
                intent.putExtra("CAPITAL", capital);
            }
            
            startActivity(intent);
        });

        buttonAnaliseAvancada.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DataAnalysisActivity.class);
            startActivity(intent);
        });
    }

    // METODO TEMPORÁRIO:
    private void testarEndpointHistorico() {
        new Thread(() -> {
            try {
                System.out.println("DEBUG: Testando endpoint de histórico...");
                Response<Void> response = RetrofitClient.getApiService()
                        .salvarHistorico(new HistoricoBusca("test", "123", "456", 1000.0))
                        .execute();

                System.out.println("DEBUG: Teste histórico - Código: " + response.code());
                System.out.println("DEBUG: Teste histórico - Sucesso: " + response.isSuccessful());
            } catch (Exception e) {
                System.out.println("DEBUG: Teste histórico - ERRO: " + e.getMessage());
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Limpar campos ao retornar para a tela principal (opcional)
        // editTextCnae.setText("");
        // editTextMunicipio.setText("");
        // editTextCapital.setText("");
    }
}