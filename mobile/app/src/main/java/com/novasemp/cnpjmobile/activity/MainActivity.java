package com.novasemp.cnpjmobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.novasemp.cnpjmobile.R;
import com.novasemp.cnpjmobile.util.SessionManager;

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

        sessionManager = new SessionManager(this);
        String sessionId = sessionManager.getSessionId();
        System.out.println("DEBUG: MainActivity - SessionId gerado: " + sessionId);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        editTextCnae = findViewById(R.id.editTextCnae);
        editTextMunicipio = findViewById(R.id.editTextMunicipio);
        editTextCapital = findViewById(R.id.editTextCapital);
        buttonBuscar = findViewById(R.id.buttonBuscar);
        buttonHistorico = findViewById(R.id.btnHistorico);
    }

    private void setupClickListeners() {
        buttonHistorico.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoricoActivity.class);
            startActivity(intent);
        });

        Button buttonAnaliseAvancada = findViewById(R.id.buttonAnaliseAvancada);
        buttonAnaliseAvancada.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DataAnalysisActivity.class);
            startActivity(intent);
        });

        buttonBuscar.setOnClickListener(v -> {
            String cnae = editTextCnae.getText().toString().trim();
            String municipio = editTextMunicipio.getText().toString().trim();
            String capitalStr = editTextCapital.getText().toString().trim();

            if (cnae.isEmpty() || municipio.isEmpty()) {
                Toast.makeText(this, "Preencha pelo menos CNAE e Município", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cnae.matches("\\d+") || !municipio.matches("\\d+")) {
                Toast.makeText(this, "CNAE e Município devem ser códigos numéricos", Toast.LENGTH_SHORT).show();
                return;
            }

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

            String sessionId = sessionManager.getSessionId();

            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putExtra("CNAE", cnae);
            intent.putExtra("MUNICIPIO", municipio);
            intent.putExtra("SESSION_ID", sessionId);

            if (capital != null) {
                intent.putExtra("CAPITAL", capital);
            }

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}